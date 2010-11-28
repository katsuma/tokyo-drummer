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
	
	private boolean playingHihat = false;
	private long prevHihatTime = 0;
	private long prevSnareTime = 0;
	
	private double prevWiimoteAccelerometerX = 0;
	private double prevWiimoteAccelerometerY = 0;
	private double prevWiimoteAccelerometerZ = 0;

	private double prevNunchukAccelerometerX = 0;
	private double prevNunchukAccelerometerY = 0;
	private double prevNunchukAccelerometerZ = 0;

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

		long currentTime = System.currentTimeMillis();
		
		//double t = 0.1;
		double prevDiffAcceleration = 0.02;
		if (Math.abs(prevWiimoteAccelerometerX - xAcceleration) < prevDiffAcceleration || 
				Math.abs(prevWiimoteAccelerometerY - yAcceleration) < prevDiffAcceleration ||
				Math.abs(prevWiimoteAccelerometerZ - zAcceleration) < prevDiffAcceleration){
			return;
		}
//		if(xAcceleration>th && yAcceleration>th && zAcceleration>th){
			//System.out.println("  -- **  xA:" + xAcceleration + " yA:" + yAcceleration + " zA:" + zAcceleration);			
		//}
		if(xAcceleration>th && yAcceleration>th && zAcceleration>th && 
				/*xAcceleration > zAcceleration && yAcceleration > zAcceleration && */currentTime - this.prevHihatTime > 200){
			if(!isPressed && !playingHihat) {
				playingHihat = true;
				System.out.println("   xA:" + xAcceleration + " yA:" + yAcceleration + " zA:" + zAcceleration);
				hiHat.play();
				this.prevHihatTime = currentTime;
				playingHihat = false;
				prevWiimoteAccelerometerX = xAcceleration;
				prevWiimoteAccelerometerY = yAcceleration;
				prevWiimoteAccelerometerZ = zAcceleration;
			}
		}
		return;			
	}

	private void nunchukEventReceived(WRNunchukExtensionEvent evt) throws JavaLayerException{		
		double th = 0.4;
		WRAccelerationEvent accelerationEvent = evt.getAcceleration();
		double nAccelerationX = accelerationEvent.getXAcceleration();
		double nAccelerationY = accelerationEvent.getYAcceleration();
		double nAccelerationZ = accelerationEvent.getZAcceleration();

		double prevDiffAcceleration = 0.01;
		if (Math.abs(prevNunchukAccelerometerX - nAccelerationX) < prevDiffAcceleration || 
				Math.abs(prevNunchukAccelerometerY - nAccelerationY) < prevDiffAcceleration ||
				Math.abs(prevNunchukAccelerometerZ - nAccelerationZ) < prevDiffAcceleration){
			return;
		}

		//if(nAccelerationX>th && nAccelerationY>th && nAccelerationZ>th ){
		//System.out.println(" --@[x]" + nAccelerationX + " [y]" + nAccelerationY + " [z]" + nAccelerationZ + " [y2]" + nAccelerationY*0.7 + "[z2]" + nAccelerationZ*0.7);
		//}
		long currentTime = System.currentTimeMillis();
		if(nAccelerationX>th && nAccelerationY>th && nAccelerationZ>th &&
				currentTime - this.prevSnareTime > 250) {
			snareDrum.play();
			this.prevSnareTime = currentTime;
			
			prevNunchukAccelerometerX = nAccelerationX;
			prevNunchukAccelerometerY = nAccelerationY;
			prevNunchukAccelerometerZ = nAccelerationZ;
		}
	}
	
	public void buttonInputReceived(WRButtonEvent evt) {
		isPressed = true;
		if(evt.wasPressed(WRButtonEvent.A)){
			//snareDrum.play();
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
