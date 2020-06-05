package pl.sp9muf.rpihamlib.udpserver.responders;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
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

public class FHamlibResponder extends HamlibResponder {
	
	public static class FilterGpio {
		private final long minFreq;
		/**
		 * @return the minFreq
		 */
		public long getMinFreq() {
			return minFreq;
		}
		/**
		 * @return the maxFreq
		 */
		public long getMaxFreq() {
			return maxFreq;
		}
		/**
		 * @return the gpio
		 */
		public GpioPinDigitalOutput getGpio() {
			return gpio;
		}
		private final long maxFreq;
		private final GpioPinDigitalOutput gpio;
		public FilterGpio(long minFreq, long maxFreq, Pin  pin){
			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			
	        // create gpio controller
	        GpioController gpioFactory = GpioFactory.getInstance();
	        
	        //provision pins
	        gpio = gpioFactory.provisionDigitalOutputPin(pin, PinState.HIGH);
	        gpio.setShutdownOptions(true, PinState.HIGH, PinPullResistance.PULL_UP);

		}	
	}


    private final FilterGpio[] filterArray;
	private final DatagramSocket sendiqSocket;    				

	public FHamlibResponder(DatagramSocket respSocket, DatagramPacket packet , FilterGpio[] filterArray, DatagramSocket sendiqSocket) {
		super(respSocket, packet);
		this.filterArray = filterArray;
		this.sendiqSocket = sendiqSocket;
	}

	@Override
	byte[] processHamlibCmd(byte[] data, int offset, int length) {
		String dataString = new String(data,offset,length);
		log.debug("processing: " + dataString);
		if (data[0] != 'F') return "RPRT 1".getBytes();
		String[] tokens = dataString.split(" ");
		if (tokens.length < 2){
			return "RPRT 1".getBytes();
		}
		double freq = Double.parseDouble(tokens[1]);
		for (FilterGpio filter: filterArray){
				filter.getGpio().setState(!(freq >= filter.getMinFreq() && freq <= filter.getMaxFreq())); //inverter controls this
		}
		try {
			sendiqSocket.send(new DatagramPacket(data,offset,length,new InetSocketAddress(InetAddress.getLoopbackAddress(),7356)));
		} catch (IOException e) {
			log.error(" cannot send to sendiq: " + e.getLocalizedMessage(),e);
		}
		return "RPRT 0".getBytes();
	}
		

}
