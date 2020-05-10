package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinState;

public class tHamlibResponder extends HamlibResponder {

	private final GpioPinDigitalMultipurpose pttPin;

	public tHamlibResponder(DatagramSocket respSocket, DatagramPacket packet, GpioPinDigitalMultipurpose pttPin) {
		super(respSocket, packet);
		this.pttPin = pttPin;
	}

	@Override
	byte[] processHamlibCmd(byte[] data, int offset, int length) {
		log.debug("processing: " + new String(data,offset,length));
		if (data[0] != 't') return "RPRT 1".getBytes();
		PinState pttState = pttPin.getState();
		byte[] response = new byte[2];
		response[0] = pttState == PinState.LOW ? (byte)'0':(byte)'1';
		response[1] = '\n';
		return response;		
		
	}

}
