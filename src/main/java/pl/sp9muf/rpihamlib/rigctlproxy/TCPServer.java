package pl.sp9muf.rpihamlib.rigctlproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
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


public class TCPServer implements Callable<Void> {

	private final Socket socket;
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final TCPClient client;
	private final UDPClient rpiClient; 
	private static final String gqrx_dump_state_response =
					"0\n" 
					+ "2\n"
					+ "1\n"
					+"0.000000 10000000000.000000 0xef -1 -1 0x1 0x0\n"
					+"0 0 0 0 0 0 0\n"
					+"0 0 0 0 0 0 0\n"
					+"0xef 1\n"
					+"0xef 0\n"
					+"0 0\n"
					+"0x82 500\n"
					+"0x82 200\n"
					+"0x82 2000\n"
					+"0x21 10000\n"
					+"0x21 5000\n"
					+"0x21 20000\n"
					+"0x0c 2700\n"
					+"0x0c 1400\n"
					+"0x0c 3900\n"
					+"0x40 160000\n"
					+"0x40 120000\n"
					+"0x40 200000\n"
					+"0 0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0\n"
					+"0x40000020\n"
					+"0x20\n"
					+"0\n"
					+"0\n";
	public TCPServer(Socket socket, TCPClient client, UDPClient rpiClient) {
		this.socket = socket;
		this.client = client;
		this.rpiClient = rpiClient;
	}

	@Override
	public Void call() throws Exception {
		socket.setSoTimeout(10000);
		try(InputStream is = socket.getInputStream(); 
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); 
			BufferedReader br = new BufferedReader(new InputStreamReader(is))) 
		{			
			log.info("starting TCP serving thread");
			
			main: while (!Thread.interrupted())
			{
				String rsp = "";
				try{
					String rigctlcmd = br.readLine();
					if (rigctlcmd == null) break;
					log.debug(" received: " + rigctlcmd);
					switch(rigctlcmd.trim().charAt(0)){
					case 'q':
						log.info("quit received");
						break main;
					case 't':
					case 'T':
						rsp = rpiClient.sendCmd(rigctlcmd);
						break;
					case 'F':
						rsp = client.sendCmd(rigctlcmd);
						rpiClient.issueCmd(rigctlcmd);
						break;
					case 'f':
						rsp = client.sendCmd(rigctlcmd);
						rpiClient.issueCmd("F " + rsp);
						break;
					case '\\':
						if (rigctlcmd.contains("dump_state")){
							rsp = gqrx_dump_state_response;
							log.info("sending fixed dump_state");
							break;
						} else {
							//fall through to receiver
						}
					default:
						rsp = client.sendCmd(rigctlcmd);
						break;						
					}
					bw.write(rsp);
					bw.newLine();
					bw.flush();
				} catch (SocketTimeoutException e) {
					log.trace("u");
				} catch (Exception e) {
					log.error("after sendcmd: " + e.getLocalizedMessage(),e);
					throw e;
				}
			}
			return null;
		} finally {
			socket.close();
			log.info("done TCP serving thread");
		}
	}

}
