package edsdk.utils;

import com.sun.jna.NativeLong;

import edsdk.CanonSDK;
import edsdk.CanonSDK.EdsObjectEventHandler;
import edsdk.CanonSDK.EdsVoid;
import edsdk.CanonSDK.__EdsObject;

/**
 * The SLRCommand class tries to make your life a whole lot easier when you would like 
 * to create new commands for your camera. 
 * 
 * E.g. one default command implemented with this is the take image cycle. 
 * This is a lot of work because you need to do all the following: 
 * - tell the camera to take an image
 * - test if the image was actually taken, e.g. if autofocus failed or flash wasn't loaded
 * - wait for the notification from the camera that the file was stored 
 * - transfer the image to the disk
 * 
 * 
 * @author hansi
 *
 */
public abstract class CanonTask<T> implements EdsObjectEventHandler{

	public CanonCamera camera;
	public static CanonSDK EDSDK = CanonSDK.INSTANCE; 
	private boolean finished = false; 
	private boolean waitForFinish = false;
	private boolean ran = false; 
	private T result; 
	
	public CanonTask(){
	}
	
	/**
	 * The SLR is just by the dispatch thread automatically just before run is called. 
	 * @param slr
	 */
	public void setSLR( CanonCamera slr ){
		this.camera = slr;
	}
	
	/**
	 * This should be short and sweet! 
	 * If your command needs to wait for events 
	 */
	public abstract void run(); 
	
	/**
	 * Sets the result
	 */
	public void setResult( T result ){
		this.result = result; 
	}
	
	/**
	 * By default a SLRCommand will be marked as finished as soon as 
	 * the run method completed. If you attached listens inside run 
	 * and are waiting for a special event to happen before you're done 
	 * please call the notYetFinished() at the end of run(). 
	 * 
	 * This will tell the dispatcher that it should start forwarding event 
	 * messages again, and also it'll wait with the execution of further commands 
	 * until your command somehow calls finish() on itself to let the dispatcher
	 * know that it's done. 
<	 */
	public void notYetFinished(){
		waitForFinish = true; 
	}
	
	/**
	 * Only used in combination with notYetFinished. 
	 * Call this when your commands work is done (e.g. you successfully 
	 * shot and downloaded an image). 
	 * @see CanonTask#notYetFinished()
	 */
	public void finish(){
		finished = true; 
	}
	
	/**
	 * Don't _ever_ call this, promise! 
	 */
	protected void ran(){
		ran = true; 
	}
	
	/**
	 * Checks if this command finished it's work. Only useful in combination with 
	 * finish() and notYetFinished(). 
	 * @see CanonTask#notYetFinished()
	 * @return
	 */
	protected boolean finished(){
		return waitForFinish? finished : ran; 
	}
	
	/**
	 * Sends a command to the camera
	 * @return 
	 */
	public int sendCommand( long command, long params ){
		return EDSDK.EdsSendCommand( camera.getEdsCamera(), new NativeLong( command ), new NativeLong( params ) );
	}
	
	/**
	 * This is a default implementation of the invoke method. 
	 * Just override it if you need to use events, the dispatcher will 
	 * take care of (un-)registering the listeners for you. 
	 * 
	 * Also don't worry about the return value, just use null! 
	 */
	@Override
	public NativeLong invoke(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext) {
		return new NativeLong( 0 ); 
	}
	
	public T result(){
		while( !finished() ){
			try {
				Thread.sleep( 10 );
			}
			catch ( InterruptedException e ) {
				return null; 
			} 
		}
		
		return result; 
	}
	
	public T getResultIfSet(){
		return result; 
	}
}
