package edsdk.utils.commands;

import java.io.File;

import com.sun.jna.NativeLong;

import edsdk.CanonSDK;
import edsdk.CanonSDK.EdsVoid;
import edsdk.CanonSDK.__EdsObject;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

/**
 * Takes an image and downloads it to the file system
 * @author hansi
 *
 */
public class ShootTask extends CanonTask<File>{

	@Override
	public void run() {
		int result = -1; 
		while( result != CanonSDK.EDS_ERR_OK ){
			System.out.println( "Trying to take image..." ); 
			result = sendCommand( CanonSDK.kEdsCameraCommand_TakePicture, 0 ); 
		}
		System.out.println( "Took image, waiting for file" ); 
		notYetFinished(); 
	}
	
	@Override
	public NativeLong invoke( NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ) {
		if( inEvent.intValue() == CanonSDK.kEdsObjectEvent_DirItemCreated ){
			System.out.println( "Looks like we got a file!" ); 
			setResult( CanonUtils.download( inRef, null, false ) ); 
			finish(); 
		}
		
		return null;
	}
}
