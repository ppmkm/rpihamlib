package pl.sp9muf.rpihamlib.rigctlproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


public class RigCtlProxy implements Runnable{
	public static final Logger log = LoggerFactory.getLogger(RigCtlProxy.class);
	private final String hostname;
	private final int port;
	private final int timeout;
	private volatile boolean stopping = false;
	private final ExecutorService respondingExecutor;
	private final String rpihost;
	private final int rpiport;
	private final String receiverHost;
	private final int receiverPort;
	private final ExecutorService servingExecutor;
	private final ExecutorService clientExecutor;
	private TCPClient client;
	private UDPClient rpiClient;
	public RigCtlProxy(String hostname, int port, int timeout, String rpihost, int rpiport, String receiverHost, int receiverPort)
	{
		log.info("RigCtlProxy <init> :" + hostname + ":" + port, ", timeout: " + timeout + ", rcv: " + receiverHost +":"+ receiverPort + ", rpi: " + rpihost +":"+ rpiport);
		this.hostname = hostname;
		this.port = port;
		this.timeout = timeout;
		this.rpihost = rpihost;
		this.rpiport = rpiport;
		this.receiverHost = receiverHost;
		this.receiverPort = receiverPort;


		respondingExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("hamlibresponder-"));
		
		servingExecutor = Executors.newCachedThreadPool(new DaemonThreadFactory("hamlib-tcp-server-"));

		clientExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("hamlib-tcp-client-"));
		

                		
	}
	
	public void stopServer()
	{
		log.info("RigCtlProxy stopping...");
		stopping = true;		
	}

	@Override
	public void run() {
		try {
			log.info("starting");
			try( ServerSocket serversocket = new ServerSocket(port, 1,  InetAddress.getByName(hostname)))
			{
				serversocket.setSoTimeout(timeout);
				client = new TCPClient(receiverHost, receiverPort);
				rpiClient = new UDPClient(rpihost,rpiport);
				clientExecutor.submit(client);
				while (!stopping && !Thread.interrupted())
				{
					try {
					Socket socket = serversocket.accept();	
					log.info("connection from " + socket.toString());
					Callable<Void> call = new TCPServer(socket, client, rpiClient);
					servingExecutor.submit(call);					
					} catch (SocketTimeoutException e) {
						// this is well expected
						log.trace("u");
					}
					
				}
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(),e);
			}
			respondingExecutor.shutdownNow();
			servingExecutor.shutdownNow();
			clientExecutor.shutdownNow();
			respondingExecutor.awaitTermination(1, TimeUnit.SECONDS);			
			servingExecutor.awaitTermination(1, TimeUnit.SECONDS);			
			clientExecutor.awaitTermination(1, TimeUnit.SECONDS);			
		} catch (InterruptedException e) {
			log.warn("interrupted");
		} finally {
			try {
				rpiClient.close();
			} catch (Exception e) {
				log.error("error closing UDP client: " + e.getLocalizedMessage(),e);
			}
			log.info("stopped");
		}
		
	}
	

    public static void main( String[] args )
    {
    	log.trace("starting...");
    	Thread server = new Thread(new RigCtlProxy(args[0],Integer.parseInt(args[1]),10000, args[2] , Integer.parseInt(args[3]), args[4],Integer.parseInt(args[5])));
    	server.setName("rigctlproxyr");
    	server.setDaemon(false);
    	server.start();
    }
	
	
}
