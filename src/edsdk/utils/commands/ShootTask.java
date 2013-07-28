package edsdk.utils.commands;

import java.io.File;

import com.sun.jna.NativeLong;

import edsdk.EdSdkLibrary;
import edsdk.EdSdkLibrary.EdsVoid;
import edsdk.EdSdkLibrary.__EdsObject;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

/**
 * Takes an image and downloads it to the file system
 * @author hansi
 *
 */
public class ShootTask extends CanonTask<File>{
	private File dest = null; 
	private boolean deleteAfterDownload;
	private boolean oldEvfMode;
	
	public ShootTask(){
		this( null, false ); 
	}
	
	public ShootTask( File dest ){
		this( dest, false ); 
	}
	public ShootTask( File dest, boolean deleteAfterDownload ){
		this.dest = dest; 
		this.deleteAfterDownload = deleteAfterDownload; 
	}
	
	
	
	@Override
	public void run() {
		int result = -1; 
		while( result != EdSdkLibrary.EDS_ERR_OK ){
			System.out.println( "Trying to take image..." );
			oldEvfMode = CanonUtils.isLiveViewEnabled( edsCamera ); 
			System.out.println( "> LV on? " + oldEvfMode ); 
			
			if( oldEvfMode ) CanonUtils.endLiveView( edsCamera );  
			
			result = sendCommand( EdSdkLibrary.kEdsCameraCommand_TakePicture, 0 ); 
			System.out.println( "> result= " + result + ", might mean " + CanonUtils.toString( result ) ); 
			try {
				Thread.sleep( 1000 );
			}
			catch( InterruptedException e ){
				e.printStackTrace();
			} 
		}
		System.out.println( "Took image, waiting for file" ); 
		notYetFinished(); 
	}
	
	@Override
	public NativeLong apply( NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ) {
		if( inEvent.intValue() == EdSdkLibrary.kEdsObjectEvent_DirItemCreated ){
			System.out.println( "Looks like we got a file!" ); 
			setResult( CanonUtils.download( inRef, dest, deleteAfterDownload ) );
			
			if( oldEvfMode ) CanonUtils.beginLiveView( edsCamera ); 
			finish(); 
		}
		
		return null;
	}
}
