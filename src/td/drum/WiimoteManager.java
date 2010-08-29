package td.drum;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import javazoom.jl.decoder.JavaLayerException;

import td.sound.BassDrum;
import td.sound.Cymbal;
import td.sound.HiHat;
import td.sound.SnareDrum;
import td.sound.Tom;

import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteExtension;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.WRAccelerationEvent;
import wiiremotej.event.WRButtonEvent;
import wiiremotej.event.WRCombinedEvent;
import wiiremotej.event.WRExtensionEvent;
import wiiremotej.event.WRIREvent;
import wiiremotej.event.WRNunchukExtensionEvent;
import wiiremotej.event.WRStatusEvent;
import wiiremotej.event.WiiRemoteListener;

public class WiimoteManager extends Thread implements WiiRemoteListener{
	private Logger logger;
	
	private WiiRemote wiiRemote = null;
	private boolean isPressed = false;
	
	//private BassDrum bassDrum = new BassDrum();
	private Tom tom = new Tom();
	private SnareDrum snareDrum = new SnareDrum();
	private HiHat hiHat = new HiHat();
	private Cymbal cymbal = new Cymbal();
	
	
	public WiimoteManager(){
		this.logger = Logger.getLogger(this.getClass().getName());
		this.start();
	}
	
	public void run(){
		try {
			this.wiiRemote = WiiRemoteJ.findRemote();
			if(this.wiiRemote!=null){
				logger.info("Detected your wiimote.");
			} else {
				logger.warning("Failed to detect your wiimote.");
				System.exit(1);
				return;
			}
			this.wiiRemote.addWiiRemoteListener(this);
			this.wiiRemote.setAccelerometerEnabled(true);
		
		} catch (IllegalStateException e){
			logger.log(Level.SEVERE, "IllegalStateException", e);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "InterruptedException", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException", e);
			System.exit(1);
			return;
		} catch (NullPointerException e){
			logger.log(Level.SEVERE, "NullException. We could not find your Wiimote.", e);
			System.exit(1);
			return;
		}
	}

	public void IRInputReceived(WRIREvent evt) {
		logger.info("WRIREvent");
	}

	public void accelerationInputReceived(WRAccelerationEvent evt) {
		double th = 0.2;
		double xAcceleration = evt.getXAcceleration();
		double yAcceleration = evt.getYAcceleration();
		double zAcceleration = evt.getZAcceleration();
		
		if(xAcceleration>th && yAcceleration>th && zAcceleration>th){
			if(!isPressed) {
				hiHat.play();
			}
		}
		return;			
	}

	private void nunchukEventReceived(WRNunchukExtensionEvent evt) throws JavaLayerException{
		double th = 1.8;
		WRAccelerationEvent accelerationEvent = evt.getAcceleration();
		double nAccelerationX = accelerationEvent.getXAcceleration();
		double nAccelerationY = accelerationEvent.getYAcceleration();
		double nAccelerationZ = accelerationEvent.getZAcceleration();
		if(nAccelerationX>th && nAccelerationY>th && nAccelerationZ>th){
			logger.info("nunchuk acceleration:" + nAccelerationX + "," + nAccelerationY + "," + nAccelerationZ);			
			snareDrum.play();
			/*
			AnalogStickData stick = evt.getAnalogStickData();
			System.out.println("stick angle : "  + stick.getAngle());
			System.out.println("stick x : "  + stick.getX());
			System.out.println("stick y : "  + stick.getY());
			*/
		}
	}
	
	public void buttonInputReceived(WRButtonEvent evt) {
		isPressed = true;
		if(evt.wasPressed(WRButtonEvent.A)){
			tom.play();
		} else if(evt.wasPressed(WRButtonEvent.B)) {
			cymbal.play();
		}
		isPressed = false;
	}

	public void combinedInputReceived(WRCombinedEvent evt) {
		
	}

	public void disconnected() {
		logger.info("disconnect");	
		System.exit(MAX_PRIORITY);
	}

	public void extensionConnected(WiiRemoteExtension evt) {
		try {
			this.wiiRemote.setExtensionEnabled(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extensionDisconnected(WiiRemoteExtension evt) {
	}

	public void extensionInputReceived(WRExtensionEvent evt) {
		if (evt instanceof WRNunchukExtensionEvent) {
			try{
				nunchukEventReceived((WRNunchukExtensionEvent)evt);
				return;
			} catch(JavaLayerException e){
				e.printStackTrace();
				logger.warning("Play sound Error.");
			}
		}
	}
	
	public void extensionPartiallyInserted() {
	}

	public void extensionUnknown() {		
	}

	public void statusReported(WRStatusEvent evt) {
	}
}
