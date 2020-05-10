package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class EchoHamlibResponder extends HamlibResponder {


	public EchoHamlibResponder(DatagramSocket respSocket, DatagramPacket packet) {
		super(respSocket, packet);
	}

	@Override
	byte[] processHamlibCmd(byte[] data, int offset, int length) {
		byte[] response = Arrays.copyOfRange(data, offset, offset + length); 
		log.trace("echoing...." + new String(response));
		return response;
	}

}
