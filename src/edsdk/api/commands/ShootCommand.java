package edsdk.api.commands;

import java.io.File;

import com.sun.jna.NativeLong;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.utils.CanonUtils;

/**
 * Takes an image and downloads it to the file system
 * @author hansi
 *
 */
public class ShootCommand extends CanonCommand<File>{
	private File dest = null; 
	private boolean deleteAfterDownload;
	private boolean oldEvfMode;
	
	public ShootCommand(){
		this( null, false ); 
	}
	
	public ShootCommand( File dest ){
		this( dest, true ); 
	}
	
	public ShootCommand( File dest, boolean deleteAfterDownload ){
		this.dest = dest; 
		this.deleteAfterDownload = deleteAfterDownload; 
	}
	
	
	
	
	@Override
	public void run() {
		int result = -1; 
		while( result != EdSdkLibrary.EDS_ERR_OK ){
			oldEvfMode = CanonUtils.isLiveViewEnabled( edsCamera ); 
			if( oldEvfMode ) CanonUtils.endLiveView( edsCamera );  
			result = sendCommand( EdSdkLibrary.kEdsCameraCommand_TakePicture, 0 ); 
			try {
				Thread.sleep( 1000 );
			}
			catch( InterruptedException e ){
				e.printStackTrace();
			} 
		}
		notYetFinished(); 
	}
	
	@Override
	public NativeLong apply( NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ) {
		if( inEvent.intValue() == EdSdkLibrary.kEdsObjectEvent_DirItemCreated ){
			setResult( CanonUtils.download( inRef, dest, deleteAfterDownload ) );
			
			if( oldEvfMode ) CanonUtils.beginLiveView( edsCamera ); 
			finish(); 
		}
		
		return null;
	}
}
