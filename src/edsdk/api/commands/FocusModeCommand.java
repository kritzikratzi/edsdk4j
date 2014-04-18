package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonUtils;

public class FocusModeCommand extends CanonCommand<Boolean>{
	
	public enum Mode{
		AUTO, MANUAL
	}
	
	private Mode mode;
	
	public FocusModeCommand( Mode mode ){
		this.mode = mode; 
	}
	
	@Override
	public void run() {
		switch( mode ){
		case AUTO: 
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_AFMode, CanonConstants.AFMode_OneShot );
			// sendCommand( EdSdkLibrary.kEdsCameraCommand_DoEvfAf, EdSdkLibrary.EdsEvfAf.kEdsCameraCommand_EvfAf_ON );
			break; 
		case MANUAL:
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_AFMode, CanonConstants.AFMode_Manual );
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_Evf_AFMode, EdSdkLibrary.EdsEvfAFMode.Evf_AFMode_Live );
			// sendCommand( EdSdkLibrary.kEdsCameraCommand_DoEvfAf, EdSdkLibrary.EdsEvfAf.kEdsCameraCommand_EvfAf_OFF );
			break; 
		}
		
		setResult( true ); 
	}
}
