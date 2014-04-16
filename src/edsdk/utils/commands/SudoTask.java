package edsdk.utils.commands;

import edsdk.utils.CanonTask;

public class SudoTask extends CanonTask<Boolean>{
	private boolean superpower = false; 
	
	public SudoTask(){
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
		setResult( true ); 
	}
}
