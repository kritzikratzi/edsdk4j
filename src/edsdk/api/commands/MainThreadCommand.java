package edsdk.api.commands;

import edsdk.api.CanonCommand;

/**
 * Get access to the main thread. 
 * This is used by CanonCamera.beginDirect() / CanonCamera.endDirect() 
 * @author hansi
 *
 */
public class MainThreadCommand extends CanonCommand<Void>{
	private boolean superpower = false; 
	
	public MainThreadCommand(){
	}
	
	@Override
	public void run() {
		notYetFinished();
		superpower = true; 
	}
	
	/**
	 * Call this to wait until you have power over the camera
	 */
	public void begin(){
			try {
				while( !superpower )
					Thread.sleep( 10 );
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * Call this when you're done
	 */
	public void end(){
		setResult( null ); 
	}
}
