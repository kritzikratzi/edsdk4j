package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;

/**
 * Drives the lense in a direction. 
 * 
 * @author hansi
 *
 */
public class DriveLenseCommand extends CanonCommand<Boolean> {

	private long direction;
	
	/**
	 * @param direction pick a value {@link EdSdkLibrary.EdsEvfDriveLens}
	 */
	public DriveLenseCommand( long direction ){
		this.direction = direction; 
	}
	
	@Override
	public void run() {
		int result = sendCommand( EdSdkLibrary.kEdsCameraCommand_DriveLensEvf, direction ); 
		setResult( result == EdSdkLibrary.EDS_ERR_OK ); 
	}

}
