package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;

public class TsetHamlibResponder extends HamlibResponder {

	private final GpioPinDigitalMultipurpose pttPin;

	public TsetHamlibResponder(DatagramSocket respSocket, DatagramPacket packet, GpioPinDigitalMultipurpose pttPin) {
		super(respSocket, packet);
		this.pttPin = pttPin;
	}

	@Override
	byte[] processHamlibCmd(byte[] data, int offset, int length) {
		String cmd = new String(data,offset,length);
		log.debug("processing: " + cmd);
		byte [] response = "RPRT 0\n".getBytes();
		switch (cmd.trim())
		{
			case "T 0": 
				log.debug("PTT off");
				pttPin.setState(false);
				break;
			case "T 1": 
			case "T 2": 
			case "T 3": 
				log.debug("PTT on");
				pttPin.setState(true);
				break;
			default:
				log.error("cmd rejected: " + cmd.trim());
				response = "RPRT -1\n".getBytes();
		}
		return response;		
		
	}

}
