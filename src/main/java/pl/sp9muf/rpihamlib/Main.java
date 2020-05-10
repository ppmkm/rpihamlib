package pl.sp9muf.rpihamlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.sp9muf.udpserver.UdpServer;

/**
 * Hello world!
 *
 */
public class Main 
{
	public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static void main( String[] args )
    {
    	log.trace("starting...");
    	Thread server = new Thread(new UdpServer(args[0],Integer.parseInt(args[1]),10000));
    	server.setName("udpserver");
    	server.setDaemon(false);
    	server.start();
    }
}
