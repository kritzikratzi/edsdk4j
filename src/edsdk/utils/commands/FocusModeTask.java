package edsdk.utils.commands;

import edsdk.EdSdkLibrary;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

public class FocusModeTask extends CanonTask<Boolean>{
	
	public enum Mode{
		AUTO, MANUAL
	}
	
	private Mode mode;
	
	public FocusModeTask( Mode mode ){
		this.mode = mode; 
	}
	
	@Override
	public void run() {
		System.out.println( "AAA" ); 
		switch( mode ){
		case AUTO: 
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_AFMode, CanonConstants.AFMode_OneShot );
			sendCommand( EdSdkLibrary.kEdsCameraCommand_DoEvfAf, EdSdkLibrary.EdsEvfAf.kEdsCameraCommand_EvfAf_ON );
			break; 
		case MANUAL:
			System.out.println( "Set to manual!!" );
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_AFMode, CanonConstants.AFMode_Manual );
			CanonUtils.setPropertyData( edsCamera, EdSdkLibrary.kEdsPropID_Evf_AFMode, EdSdkLibrary.EdsEvfAFMode.Evf_AFMode_Live );
			sendCommand( EdSdkLibrary.kEdsCameraCommand_DoEvfAf, EdSdkLibrary.EdsEvfAf.kEdsCameraCommand_EvfAf_OFF );
			break; 
		}
		
		System.out.println( "DONE!" ); 
		setResult( true ); 
	}
}
