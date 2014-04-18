package gettingstarted;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.User32.MSG;
import com.sun.jna.platform.win32.W32API.HMODULE;
import com.sun.jna.ptr.NativeLongByReference;

import edsdk.api.CanonCamera;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.utils.CanonUtils;

/** Simple example of JNA interface mapping and usage. */
public class E01_Simple {

	public static EdSdkLibrary EDSDK = CanonCamera.EDSDK; 
	static final User32 lib = User32.INSTANCE;
	static final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle("Me");
	
	public static void main(String[] args) throws InterruptedException {
		int result = 0; 
		
		result = EDSDK.EdsInitializeSDK().intValue(); 
		check( result ); 
		
		__EdsObject list[] = new __EdsObject[1]; 
		debug( list ); 
		result = EDSDK.EdsGetCameraList( list ).intValue();		
		debug( list ); 
		check( result ); 
		
		NativeLongByReference outRef = new NativeLongByReference(); 
		result = EDSDK.EdsGetChildCount( list[0], outRef ).intValue(); 
		check( result ); 
		System.out.println( "Cameras: " + outRef.getValue().longValue() );
		long numCams = outRef.getValue().longValue(); 
		if( numCams == 0 ){
			System.out.println( "no camera found" ); 
		}
		
		__EdsObject camera[] = new __EdsObject[1]; 
		debug( camera ); 
		result = EDSDK.EdsGetChildAtIndex( list[0], new NativeLong( 0 ), camera ).intValue(); 
		debug( camera ); 
		check( result ); 
		
		EdsVoid context = new EdsVoid( new Pointer( 0 ) );
		EdsObjectEventHandler handler = new EdsObjectEventHandler() {
			@Override
			public NativeLong apply(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext) {
				System.out.println( "Event!!!" + inEvent.doubleValue() + ", " + inContext ); 
				if( inEvent.intValue() == 516 ){
					CanonUtils.download( inRef, null, true );
				}
				return new NativeLong( -1 );
				//return -1;
			}
		}; 
		
		EDSDK.EdsSetObjectEventHandler( camera[0], new NativeLong( EdSdkLibrary.kEdsObjectEvent_All ), handler, context );
		
		result = EDSDK.EdsOpenSession( camera[0] ).intValue(); 
		check( result ); 
		
		// Do stuff here, like ... take an image... 
		//result = EDSDK.EdsSendCommand( camera[0], new NativeLong( TestLibrary.kEdsCameraCommand_TakePicture ), new NativeLong( 0 ) ); 
		//check( result ); 
		
		// Wait a little bit! 
		dispatchMessages();
	}
	
	public static void check( int result ){
		if( result != EdSdkLibrary.EDS_ERR_OK ){
			System.out.println( "Error: " + CanonUtils.toString( result ) ); 
		}
	}
	
	public static void debug( __EdsObject[] obj ){
		System.out.println("----------------"); 
		for( __EdsObject o : obj ){
			if( o != null ){
				System.out.println( o + ": " + o.getPointer().getLong(0) );; 
			}
		}
	}

	public static void dispatchMessages() {
		// This bit never returns from GetMessage
		int result;
		MSG msg = new MSG();

		while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {

			if (result == -1) {
				System.err.println("error in get message");
				break;
			}

			else {
				lib.TranslateMessage(msg);
				try{lib.DispatchMessage(msg);}
				catch( Error e ){ e.printStackTrace(); }
			}
		}

	}
}