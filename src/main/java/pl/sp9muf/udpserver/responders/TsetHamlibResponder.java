package pl.sp9muf.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;

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
