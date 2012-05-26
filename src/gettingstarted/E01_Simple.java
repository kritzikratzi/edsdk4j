package gettingstarted;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.NativeLongByReference;

import edsdk.CanonSDK;
import edsdk.CanonSDK.EdsObjectEventHandler;
import edsdk.CanonSDK.EdsVoid;
import edsdk.CanonSDK.__EdsObject;
import edsdk.utils.CanonUtils;

/** Simple example of JNA interface mapping and usage. */
public class E01_Simple {

	public static CanonSDK EDSDK = CanonSDK.INSTANCE; 
	static final User32 lib = User32.INSTANCE;
	
	public static void main(String[] args) throws InterruptedException {
		int result = 0; 
		
		result = EDSDK.EdsInitializeSDK(); 
		check( result ); 
		
		__EdsObject list[] = new __EdsObject[1]; 
		debug( list ); 
		result = EDSDK.EdsGetCameraList( list );		
		debug( list ); 
		check( result ); 
		
		NativeLongByReference outRef = new NativeLongByReference(); 
		result = EDSDK.EdsGetChildCount( list[0], outRef ); 
		check( result ); 
		System.out.println( "Cameras: " + outRef.getValue().longValue() );
		long numCams = outRef.getValue().longValue(); 
		if( numCams == 0 ){
			System.out.println( "no camera found" ); 
		}
		
		__EdsObject camera[] = new __EdsObject[1]; 
		debug( camera ); 
		result = EDSDK.EdsGetChildAtIndex( list[0], new NativeLong( 0 ), camera ); 
		debug( camera ); 
		check( result ); 
		
		EdsVoid context = new EdsVoid( new Pointer( 0 ) );
		EdsObjectEventHandler handler = new EdsObjectEventHandler() {
			@Override
			public NativeLong invoke(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext) {
				System.out.println( "Event!!!" + inEvent.doubleValue() + ", " + inContext ); 
				return new NativeLong( -1 );
				//return -1;
			}
		}; 
		
		EDSDK.EdsSetObjectEventHandler( camera[0], new NativeLong( CanonSDK.kEdsObjectEvent_All ), handler, context );
		
		result = EDSDK.EdsOpenSession( camera[0] ); 
		check( result ); 
		
		// Do stuff here, like ... take an image... 
		//result = EDSDK.EdsSendCommand( camera[0], new NativeLong( TestLibrary.kEdsCameraCommand_TakePicture ), new NativeLong( 0 ) ); 
		//check( result ); 
		
		// Wait a little bit! 
		dispatchMessages();
	}
	
	public static void check( int result ){
		if( result != EDSDK.EDS_ERR_OK ){
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
		int count = 0; 
		int result;
		MSG msg = new MSG();

		while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {

			if (result == -1) {
				System.err.println("error in get message");
				break;
			}

			else {
				count ++; 
				lib.TranslateMessage(msg);
				try{lib.DispatchMessage(msg);}
				catch( Error e ){ e.printStackTrace(); }
			}
		}

	}
}