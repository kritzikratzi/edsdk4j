package jnatest;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.User32.MSG;
import com.sun.jna.platform.win32.W32API.HMODULE;
import com.sun.jna.ptr.NativeLongByReference;

import old.EDSDKTools;
import edsdk.TestLibrary;
import edsdk.TestLibrary.EdsObjectEventHandler;
import edsdk.TestLibrary.EdsVoid;
import edsdk.TestLibrary.__EdsObject;

/** Simple example of JNA interface mapping and usage. */
public class Main {

	public static TestLibrary EDSDK = TestLibrary.INSTANCE; 
	static final User32 lib = User32.INSTANCE;
	static final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle("Me");
	
	public static void main(String[] args) throws InterruptedException {
		long result = 0; 
		
		result = EDSDK.EdsInitializeSDK(); 
		check( result ); 
		
		__EdsObject list[] = new __EdsObject[10]; 
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
		EDSDK.EdsSetObjectEventHandler( camera[0], new NativeLong( TestLibrary.kEdsObjectEvent_All ), new EdsObjectEventHandler() {
			@Override
			public int invoke(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext) {
				System.out.println( "Event!!!" + inRef + ", " + inContext ); 
				//return new NativeLong( TestLibrary.EDS_ERR_OK );
				return 1;
			}
		}, null );
		
		result = EDSDK.EdsOpenSession( camera[0] ); 
		check( result ); 
		
		result = EDSDK.EdsSendCommand( camera[0], new NativeLong( TestLibrary.kEdsCameraCommand_TakePicture ), new NativeLong( 0 ) ); 
		check( result ); 
		
		// Wait a little bit! 
		dispatchMessages();
		
		result = EDSDK.EdsCloseSession( camera[0] ); 
		check( result ); 
	}
	
	public static void check( long result ){
		System.out.println( EDSDKTools.toString( result ) ); 
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
				lib.DispatchMessage(msg);
			}
		}

	}
}