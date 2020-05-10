package pl.sp9muf.rpihamlib.rigctlproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
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


public class TCPServer implements Callable<Void> {

	private final Socket socket;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final TCPClient client; 
	public TCPServer(Socket socket, TCPClient client) {
		this.socket = socket;
		this.client = client;
	}

	@Override
	public Void call() throws Exception {
		socket.setSoTimeout(10000);
		try(InputStream is = socket.getInputStream(); 
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); 
			BufferedReader br = new BufferedReader(new InputStreamReader(is))) 
		{			
			log.info("starting TCP serving thread");
			
			main: while (!Thread.interrupted())
			{
				String rsp = "";
				try{
					String rigctlcmd = br.readLine();
					if (rigctlcmd == null) break;
					log.debug(" received: " + rigctlcmd);
					switch(rigctlcmd.trim().charAt(0)){
					case 'q':
						log.info("quit received");
						break main;
					case 't':
					case 'T':
						log.info("TODO call rpi");
						//fall through
					default:
						rsp = client.sendCmd(rigctlcmd);
						break;						
					}
					bw.write(rsp);
					bw.newLine();
					bw.flush();
				} catch (SocketTimeoutException e) {
					log.trace("u");
				}
				
			}
			return null;
		} finally {
			socket.close();
			log.info("done TCP serving thread");
		}
	}

}
