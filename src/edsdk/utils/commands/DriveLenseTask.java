package edsdk.utils.commands;

import edsdk.EdSdkLibrary;
import edsdk.utils.CanonTask;

/**
 * Drives the lense in a direction. 
 * 
 * @author hansi
 *
 */
public class DriveLenseTask extends CanonTask<Boolean> {

	private long direction;
	
	/**
	 * @param direction pick a value {@link EdSdkLibrary.EdsEvfDriveLens}
	 */
	public DriveLenseTask( long direction ){
		this.direction = direction; 
	}
	
	@Override
	public void run() {
		int result = sendCommand( EdSdkLibrary.kEdsCameraCommand_DriveLensEvf, direction ); 
		setResult( result == EdSdkLibrary.EDS_ERR_OK ); 
	}

}
