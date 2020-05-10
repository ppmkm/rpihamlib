package pl.sp9muf.udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpServer implements Runnable{
	private static final int BUFFERSIZE = 2048;
	public final Logger log = LoggerFactory.getLogger(getClass());
	private final String hostname;
	private final int port;
	private final int timeout;
	private volatile boolean stopping = false;
	public UdpServer(String hostname, int port, int timeout)
	{
		log.info("UdpServer <init> :" + hostname + ":" + port, ", timeout: " + timeout);
		this.hostname = hostname;
		this.port = port;
		this.timeout = timeout;
	}
	
	public void stopServer()
	{
		log.info("UdpServer stopping...");
		stopping = true;		
	}

	@Override
	public void run() {
		try 
		{
			log.info("starting");
			try( DatagramSocket dgramsocket = new DatagramSocket(new InetSocketAddress(hostname,port)))
			{
				dgramsocket.setSoTimeout(timeout);
				while (!stopping && !Thread.interrupted())
				{
					DatagramPacket packet = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
					dgramsocket.receive(packet);
					log.trace("received: " + new String(packet.getData()));
				}
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(),e);
			}
		}
		finally 
		{
			log.info("stopped");
		}
		
	}

}
