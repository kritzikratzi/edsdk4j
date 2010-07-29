package edsdk.commands;

import java.io.File;

import com.sun.jna.NativeLong;

import edsdk.SLRCommand;
import edsdk.CanonSDK;
import edsdk.CanonSDK.EdsVoid;
import edsdk.CanonSDK.__EdsObject;

public class ShootCommand extends SLRCommand<File>{

	@Override
	public void run() {
		sendCommand( CanonSDK.kEdsCameraCommand_TakePicture, 0 ); 
		notYetFinished(); 
	}
	
	@Override
	public NativeLong invoke( NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ) {
		if( inEvent.intValue() == CanonSDK.kEdsObjectEvent_DirItemCreated ){
			System.out.println( "Looks like we got a file!" ); 
			finish(); 
		}
		
		return null;
	}
}
