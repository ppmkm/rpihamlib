package pl.sp9muf.udpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import pl.sp9muf.udpserver.responders.EchoHamlibResponder;
import pl.sp9muf.udpserver.responders.TsetHamlibResponder;
import pl.sp9muf.udpserver.responders.tHamlibResponder;

public class UdpServer implements Runnable{
	private static final int BUFFERSIZE = 2048;
	public final Logger log = LoggerFactory.getLogger(getClass());
	private final String hostname;
	private final int port;
	private final int timeout;
	private volatile boolean stopping = false;
	private final ExecutorService respondingExecutor;
	private final GpioController gpio;
	private final GpioPinDigitalMultipurpose pttPin;
	public UdpServer(String hostname, int port, int timeout)
	{
		log.info("UdpServer <init> :" + hostname + ":" + port, ", timeout: " + timeout);
		this.hostname = hostname;
		this.port = port;
		this.timeout = timeout;
        // we can use this utility method to pre-check to determine if
        // privileged access is required on the running system
//        if(GpioUtil.isPrivilegedAccessRequired()){
//            throw new RuntimeException("Privileged access is required on this system to access GPIO pins!");
//        }

        // ----------------------
        // ATTENTION
        // ----------------------
        // YOU CANNOT USE ANY HARDWARE PWM OR CLOCK FUNCTIONS WHILE ACCESSING NON-PRIVILEGED GPIO.
        // THIS METHOD MUST BE INVOKED BEFORE CREATING A GPIO CONTROLLER INSTANCE.
//        GpioUtil.enableNonPrivilegedAccess();
        // create gpio controller
        gpio = GpioFactory.getInstance();
        
        //provision pins
        pttPin = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_00, PinMode.DIGITAL_OUTPUT, PinPullResistance.OFF);
        pttPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.PULL_DOWN);
        
        

		respondingExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("responder-"));
                
		
	}
	
	public void stopServer()
	{
		log.info("UdpServer stopping...");
		stopping = true;		
	}

	@Override
	public void run() {
		try {
			log.info("starting");
			try( DatagramSocket dgramsocket = new DatagramSocket(new InetSocketAddress(hostname,port)))
			{
				dgramsocket.setSoTimeout(timeout);
				while (!stopping && !Thread.interrupted())
				{
					try {
					DatagramPacket packet = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);
					dgramsocket.receive(packet);
					byte[] data = packet.getData();
					log.trace("received: " + new String(data));
					Callable<Void> call = null;
					switch(data[0])
					{
						case 't':
							call = new tHamlibResponder(dgramsocket,packet,pttPin);
							break;
						case 'T':
							call = new TsetHamlibResponder(dgramsocket,packet,pttPin);
							break;
						default:
							call = new EchoHamlibResponder(dgramsocket, packet);
							break;
					
					}
					respondingExecutor.submit(call);					
					} catch (SocketTimeoutException e) {
						// this is well expected
						log.trace("u");
					}
					
				}
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(),e);
			}
			respondingExecutor.shutdownNow();
		} finally {
			log.info("stopped");
		}
		
	}

}
