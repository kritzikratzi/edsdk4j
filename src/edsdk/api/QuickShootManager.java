package edsdk.api;

import java.io.File;
import java.util.ArrayList;

import com.sun.jna.NativeLong;

import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.utils.CanonUtils;

public class QuickShootManager implements EdsObjectEventHandler{
	ArrayList<__EdsObject> refs = new ArrayList<EdSdkLibrary.__EdsObject>(); 
	private CanonCamera camera;
	int wanted = 0; 
	
	public QuickShootManager( CanonCamera camera ){
		this.camera = camera; 
		// disable liveview... 
		camera.execute( new CanonCommand<Void>() {
			@Override
			public void run() {
				CanonUtils.endLiveView(edsCamera);
			}
		}); 
	}
	
	public CanonCommand<Void> trigger(){
		if( wanted == 0 ){
			camera.addObjectEventHandler( this );
		}
		
		wanted ++; 
		return camera.execute( new Shutter() );  
	}
	
	public CanonCommand<ArrayList<File>> downloadAll(){
		// wait until we have enough files ... 
		while( refs.size() < wanted ){
			try {
				Thread.sleep( 1 );
				Thread.yield(); 
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null; 
			}
		}
		
		return camera.execute( new Downloader() ); 
	}
	
	public void reset(){
		refs.clear(); 
		camera.removeObjectEventHandler( this );
	}
	
	private class Shutter extends CanonCommand<Void>{
		private boolean oldEvfMode;
		@Override
		public void run() {
			int result = -1; 
			while( result != EdSdkLibrary.EDS_ERR_OK ){
//				oldEvfMode = CanonUtils.isLiveViewEnabled( edsCamera ); 
//				if( oldEvfMode ) CanonUtils.endLiveView( edsCamera );
				
				result = sendCommand( EdSdkLibrary.kEdsCameraCommand_TakePicture, 0 ); 
			}
		}
	}

	
	private class Downloader extends CanonCommand<ArrayList<File>>{
		@Override
		public void run() {
			ArrayList<File> results = new ArrayList<File>();
			for( __EdsObject ref : refs ){
				results.add( CanonUtils.download( ref, null, true ) );
			}
			
			setResult( results );
			reset(); 
		}
	}


	@Override
	public NativeLong apply(NativeLong inEvent, __EdsObject inRef,
			EdsVoid inContext) {
		
		if( inEvent.intValue() == EdSdkLibrary.kEdsObjectEvent_DirItemCreated ){
			refs.add( inRef ); 
		}
			
		return null; 
	}
}
