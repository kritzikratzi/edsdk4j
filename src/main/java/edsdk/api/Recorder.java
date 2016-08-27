package edsdk.api;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Video Recording Helper
 *
 */
public class Recorder extends Thread {
	protected Logger LOGGER=Logger.getLogger("edsdk.api");

	int frameCount = 0;
	int loopCount = 0;
	public static boolean debug = false;
	// set the maximum possible frame Rate here
	public static int MAX_FPS=25;
	public static int LIVE_VIEW_PREPARATION_TIME_MS=1200;
	public String msg;

	boolean keepRunning;
	private OutputStream mjpegStream;
	private BaseCanonCamera camera;

	/**
	 * create a Recorder
	 * 
	 * @param camera
	 *            - the camera to use for recording
	 * @param mjpegStream
	 */
	public Recorder(BaseCanonCamera camera, OutputStream mjpegStream) {
		this.camera = camera;
		this.mjpegStream = new BufferedOutputStream(mjpegStream);
	}

	/**
	 * run this thread
	 */
	@Override
	public void run() {
		keepRunning=true;
		if (!camera.beginLiveView()) {
			msg="start of LiveView failed";
			LOGGER.log(Level.SEVERE, msg);
			keepRunning=false;
		};
		try {
			Thread.sleep(LIVE_VIEW_PREPARATION_TIME_MS);
		} catch (InterruptedException thrown) {
			msg="stopping recorder due to exception while waiting for Live View preparation";
			LOGGER.log(Level.WARNING, msg, thrown);
			keepRunning = false;
		}
		while (keepRunning) {
			try {
				final byte[] imageBuffer = camera.downloadLiveViewBuffer();
				// if wee didn't download and image
				if (imageBuffer == null) {
					// then we are going to wait a few milliSeconds - approximately have the time for a frame
					Thread.sleep(1000/MAX_FPS/2);
					loopCount++;
					if (debug)
						LOGGER.log(Level.INFO,"loop: "+loopCount);
				} else {
					mjpegStream.write(imageBuffer);
					mjpegStream.flush();
					// count frames
					frameCount++;
					if (debug)
						LOGGER.log(Level.INFO,"frame: "+frameCount);
				}
			} catch (InterruptedException | IOException thrown) {
				msg="stopping recorder due to exception";
				LOGGER.log(Level.WARNING, msg, thrown);
				keepRunning = false;
			}
		}
		if (debug) {
			msg="Recorded "+frameCount+" frames with "+loopCount+" idle loops and "+LIVE_VIEW_PREPARATION_TIME_MS+" msecs Live view preparation time";
			LOGGER.log(Level.INFO,msg);		}
		try {
			mjpegStream.close();
		} catch (IOException thrown) {
			String msg="closing mjpegstream failed";
			LOGGER.log(Level.SEVERE, msg, thrown);
		}
		camera.endLiveView();
	}

	/**
	 * allow stopping the recording from outside
	 */
	public void stopRunning() {
		keepRunning = false;
	}
}
