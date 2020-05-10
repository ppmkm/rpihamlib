package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HamlibResponder implements Callable<Void> {
	
	private final DatagramPacket packet;
	public final Logger log = LoggerFactory.getLogger(getClass());
	private final DatagramSocket respSocket;
	
	public HamlibResponder(DatagramSocket respSocket, DatagramPacket packet)
	{
		this.packet = packet; 
		this.respSocket = respSocket;
	}

	@Override
	public Void call() throws Exception {
		byte [] response = processHamlibCmd(packet.getData(),packet.getOffset(),packet.getLength());
		DatagramPacket responsePacket = new DatagramPacket(response,response.length,packet.getSocketAddress());
		respSocket.send(responsePacket);
		log.trace(" response sent to " + packet.getSocketAddress());
		return null;
	}

	abstract byte[] processHamlibCmd(byte[] data, int offset, int length);

}
