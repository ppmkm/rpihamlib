package pl.sp9muf.rpihamlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.sp9muf.rpihamlib.rigctlproxy.RigCtlProxy;
import pl.sp9muf.rpihamlib.udpserver.UdpServer;


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



/**
 *
 */
public class Main 
{
	public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main( String[] args )
    {
    	log.trace("starting...");
    	switch (args[0]){
    	case "udpserver":
        	Thread server = new Thread(new UdpServer(args[1],Integer.parseInt(args[2]),10000));
        	server.setName("udpserver");
        	server.setDaemon(false);
        	server.start();
        	break;
    	case "rigctlproxy":
        	server = new Thread(new RigCtlProxy(args[1],Integer.parseInt(args[2]),10000, args[3] , Integer.parseInt(args[4]), args[5],Integer.parseInt(args[6])));
        	server.setName("rigctlproxyr");
        	server.setDaemon(false);
        	server.start();
        	break;
        default:
        	printUsage();
        	break;
    	}
    }
	private static void printUsage() {
		// TODO Auto-generated method stub
		
	}
}
