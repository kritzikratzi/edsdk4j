package edsdk.api;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

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
	public static int MAX_FPS=60;
	public static int LIVE_VIEW_PREPARATION_TIME_MS=100;
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
				final BufferedImage image = camera.downloadLiveView();
				// if wee didn't download and image
				if (image == null) {
					// then we are going to wait a few milliSeconds - approximately have the time for a frame
					Thread.sleep(1000/MAX_FPS/2);
					loopCount++;
				} else {
					ImageIO.write(image, "jpg", mjpegStream);
					mjpegStream.flush();
					// count frames
					frameCount++;
				}
			} catch (InterruptedException | IOException thrown) {
				msg="stopping recorder due to exception";
				LOGGER.log(Level.WARNING, msg, thrown);
				keepRunning = false;
			}
		}
		if (debug) {
			msg="Recorded "+frameCount+" frames with "+loopCount+" idle loops ";
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
