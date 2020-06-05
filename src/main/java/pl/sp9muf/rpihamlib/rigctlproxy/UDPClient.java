package pl.sp9muf.rpihamlib.rigctlproxy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.sp9muf.rpihamlib.common.DaemonThreadFactory;

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


public class UDPClient  implements AutoCloseable{

	private static final int BUFFERSIZE = 2048;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final DatagramSocket dgramSocket;
	private final InetSocketAddress address;
	private final ExecutorService callExecutor; 
	
	
	public String sendCmd(String cmd) throws IOException, InterruptedException, ExecutionException, TimeoutException {
		log.trace("issuing send of: "  +cmd);
		Future<String> resp = issueCmd(cmd);
		String r = resp.get(15, TimeUnit.SECONDS);
		log.trace("returning response: " + r);
		return r;
		
	}

	/**
	 * @param cmd
	 * @return
	 */
	public Future<String> issueCmd(String cmd) {
		Future<String> resp = callExecutor.submit(()->{
		log.trace("sending: " + cmd);
		DatagramPacket packet = new DatagramPacket(cmd.getBytes(), cmd.getBytes().length,address);
		DatagramPacket respPacket = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);		
		dgramSocket.send(packet);
		log.trace("sent: " + cmd);
		dgramSocket.receive(respPacket);
		String r = new String(respPacket.getData(),respPacket.getOffset(), respPacket.getLength());
		log.trace("got response: " + r);
		return r;
		});
		return resp;
	}

	public UDPClient(String host, int port) throws SocketException {
		log.info("<init>: " + host + ":" + port);
		address = new InetSocketAddress(host,port);
		dgramSocket = new DatagramSocket();
		dgramSocket.setSoTimeout(10000);
		callExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("udpclient-"));
	}

	@Override
	public void close() throws Exception {
		log.info("closing...");
		if (dgramSocket != null) dgramSocket.close();
		callExecutor.shutdownNow();
		callExecutor.awaitTermination(1, TimeUnit.SECONDS);
		log.info("done...");
	}

	
}
