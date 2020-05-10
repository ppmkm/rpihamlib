package pl.sp9muf.rpihamlib.rigctlproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
rpihamlib,  set of tools (glue) to create rigctl cotrollable  transceiver 
from rictl controllable receiver and rpitx as transmittter
Copyright (C) 2020 Piotr Mis

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


public class TCPClient implements Callable<Void> {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final InetSocketAddress address; 
	private volatile boolean stopping = false;
	private volatile BufferedReader br = null;
	private volatile BufferedWriter bw = null;
	private volatile boolean nullReceived = false;
	/**
	 * @return the stopping
	 */
	public boolean isStopping() {
		return stopping;
	}

	/**
	 * @param stopping the stopping to set
	 */
	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}
	
	public synchronized String sendCmd(String cmd){
		log.trace("sending: " + cmd);
		char[] buf = new char[4096];
		if (bw == null || bw == null){
			throw new IllegalStateException("not connected");
		}
		try {
			bw.write(cmd);
			bw.newLine();
			bw.flush();
			int l = br.read(buf,0,buf.length);
			String r = new String(buf,0,l);
			log.trace("got response: " + r);
			if (null == r) {
				nullReceived  = true;
				r = "RPRT -1";
			}
			return r;
		} catch (IOException e) {
			log.warn(e.getLocalizedMessage(),e);
			nullReceived = true;
			return "";
		}
	}

	public TCPClient(String host, int port) {
		log.info("<init>: " + host + ":" + port);
		this.address = new InetSocketAddress(host, port);
	}

	@Override
	public Void call() throws Exception {
		try {
			log.info("starting TCP client thread");		
			while (!Thread.interrupted() && !stopping)
			{
				try(Socket socket = new Socket(address.getAddress(),address.getPort())){
					bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					log.info("connected to " + address);
					while (!Thread.interrupted() && !stopping && !socket.isClosed() && !nullReceived){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							log.info("interrupted");
							break;
						}
					}
				} catch (ConnectException e) {
					log.warn("cannot connect to receiver: " + e.getLocalizedMessage());
				}finally{
					if (bw != null) bw.close();
					bw = null;
					if (br != null) br.close();
					br = null;
					nullReceived = false;
					log.info("disconnected from " + address);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.info("interrupted");
					break;
				}
			}
			return null;
		}catch (Exception e){
		  log.error(e.getLocalizedMessage(),e);
		  throw e;
		} finally {
			log.info("done TCP client thread");
		}
	}

}
