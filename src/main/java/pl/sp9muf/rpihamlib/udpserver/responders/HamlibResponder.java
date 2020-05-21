package pl.sp9muf.rpihamlib.udpserver.responders;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
