package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinState;

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
