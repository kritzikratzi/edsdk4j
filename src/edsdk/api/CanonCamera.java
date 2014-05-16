package edsdk.api;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.javafx.tk.FocusCause;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.User32.MSG;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;

import edsdk.api.commands.DriveLenseCommand;
import edsdk.api.commands.FocusModeCommand;
import edsdk.api.commands.GetPropertyCommand;
import edsdk.api.commands.LiveViewCommand;
import edsdk.api.commands.SetPropertyCommand;
import edsdk.api.commands.ShootCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsEvfDriveLens;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.processing.ProcessingCanonCamera;
import edsdk.utils.CanonUtils;
/**
 * This class should be the easiest way to use the canon sdk. 
 * Please note that you _can_ use the sdk directly or also 
 * use this class to get the basic communication running, and then 
 * communicate with the edsdk directly. 
 * 
 * Either way, one of the most important things to remember is that 
 * edsdk is not multithreaded so your vm might crash if you just call functions
 * from the library. 
 * Instead I suggest you use the static method SLR.invoke( Runnable r ); 
 * or the method canonCamera.invoke( CanonTask task ); 
 * 
 * The later is basically the same, but allows you to easily get a return integer value, 
 * like int result = SLR.invoke( new CanonTask(){ public int run(){ return ...; } } );
 * 
 * 
 * This class also automatically processes and forwards all windows-style messages. 
 * This is required to forward camera events into the edsdk. Currently there is no 
 * way to disable this if it conflicts with your software. 
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 */
public class CanonCamera implements EdsObjectEventHandler {
	// This gives you direct access to the EDSDK
	public static EdSdkLibrary EDSDK = null;
	
	// Libraries needed to forward windows messages
	private static final User32 lib = User32.INSTANCE;
	//private static final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle("");
	
	// The queue of commands that need to be run. 
	private static ConcurrentLinkedQueue<CanonCommand<?>> queue = new ConcurrentLinkedQueue<CanonCommand<?>>( ); 
	
	// Object Event Handlers
	private static ArrayList<EdsObjectEventHandler> objectEventHandlers = new ArrayList<EdsObjectEventHandler>( 10 ); 
	
	
	private static Thread dispatcherThread = null; 
	static{
		// Tells the app to throw an error instead of crashing entirely. 
		// Native.setProtected( true ); 
		// We actually want our apps to crash, because something very dramatic 
		// is going on when the user receives this kind of crash message from 
		// the os and it puts the developer under pressure to fix the issue. 
		// If we enable Native.setProtected the app might just freeze, 
		// which is imho more annoying than a proper crash. 
		// Anyways, if you want the exception-throwing-instead-crashing behaviour
		// just call the above code as early as possible in your main method.
		
		initNativeLibrary();
	}
	
	public static void initNativeLibrary(){
		if( EDSDK != null ){
			// already initialized
			return; 
		}
		
		
		boolean result = loadLibrary( new File( "" ) ); 
		
		if( !result ){
			try {
				result = loadLibrary( new File( ProcessingCanonCamera.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart() ).getParentFile() );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		
		
		if( result ){
			dispatcherThread = new Thread(){
				public void run(){
					dispatchMessages(); 
				}
			}; 
			dispatcherThread.start(); 
		}
		else{
			System.err.println( "Native Library for EDSDK could not be loaded. This is bad! " ); 
			throw new UnsatisfiedLinkError( "EDSDK library not found" ); 
		}
	}
	
	private static boolean loadLibrary( File baseDir ){
		String path = baseDir.getAbsolutePath() + "\\EDSDK\\Dll\\EDSDK.dll";  
		try{
			Map<String, Integer> options = new HashMap<String, Integer>();
			options.put(Library.OPTION_CALLING_CONVENTION, StdCallLibrary.STDCALL_CONVENTION);
			EDSDK = (EdSdkLibrary)Native.loadLibrary( path, EdSdkLibrary.class, options);
			System.out.println( "Found EDSDK.dll in " + path ); 
			return true; 
		}
		catch( UnsatisfiedLinkError e ){
			// nope... not here
			return false; 
		}
	}
	
	
	/////////////////////////////////////////////
	// From here on it's instance variables
	
	private __EdsObject edsCamera;

	private String errorMessage;
	private int errorCode; 
	
	
	public CanonCamera(){
	}
	
	public boolean openSession(){
		return executeNow( new OpenSessionCommand() ); 
	}
	
	public boolean closeSession() {
		return executeNow( new CloseSessionCommand() ); 
	}

	public __EdsObject getEdsCamera(){
		return edsCamera; 
	}
	
	public ShootCommand shoot(){
		return execute( new ShootCommand() ); 
	}
	
	public ShootCommand shoot( File dest ){
		return execute( new ShootCommand( dest ) ); 
	}
	
	public SetPropertyCommand setProperty( long property, long value ){
		return execute( new SetPropertyCommand( property, value ) ); 
	}
	
	public GetPropertyCommand getProperty( long property ){
		return execute( new GetPropertyCommand( property ) ); 
	}
	

	
	public <T extends CanonCommand<?>> T execute( T cmd ){
		cmd.setSLR( this ); 
		queue.add( cmd );
		
		return cmd; 
	}
	
	public <T> T executeNow( CanonCommand<T> cmd ){
		execute( cmd ); 
		return cmd.get(); 
	}
	
	public boolean setError( int result, String message ){
		errorMessage = message + " (code=" + result + ", _might_ mean " + CanonUtils.toString( result ) + ")"; 
		errorCode = result; 
		
		System.err.println( errorMessage ); 
		
		return false; 
	}
	
	/**
	 * Adds an object event handler
	 */
	public void addObjectEventHandler( EdsObjectEventHandler handler ){
		objectEventHandlers.add( handler ); 
	}
	
	/**
	 * Removes an object event handler
	 */
	public void removeObjectEventHandler( EdsObjectEventHandler handler ){
		objectEventHandlers.remove( handler ); 
	}
	
	
	@Override
	public NativeLong apply(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext ){
		for( EdsObjectEventHandler handler : objectEventHandlers ){
			handler.apply( inEvent, inRef, inContext ); 
		}
		
		return new NativeLong( 0 ); 
	}

	/**
	 * Dispatches windows messages and executes tasks
	 */
	private static void dispatchMessages() {
		// Do some initializing
		int err = EDSDK.EdsInitializeSDK().intValue(); 
		if( err != EdSdkLibrary.EDS_ERR_OK ){
			System.err.println( "EDSDK failed to initialize, most likely you won't be able to speak to your slr :(" ); 
		}
		
		
		MSG msg = new MSG();
	
		CanonCommand<?> cmd = null; 
		
		while( !Thread.currentThread().isInterrupted() ){
			// do we have a new message? 
			boolean hasMessage = lib.PeekMessage( msg, null, 0, 0, 1 ); // peek and remove
			if( hasMessage ){
				lib.TranslateMessage( msg ); 
				lib.DispatchMessage( msg ); 
			}
			
			// is there a command we're currently working on? 
			if( cmd != null ){
				if( cmd.finished() ){
					// great! 
					cmd.camera.removeObjectEventHandler( cmd ); 
					cmd = null; 
				}
			}
			
			// are we free to do new work, and is there even new work to be done? 
			if( !queue.isEmpty() && cmd == null ){
				cmd = queue.poll(); 
				if( !(cmd instanceof OpenSessionCommand) )
					cmd.camera.addObjectEventHandler( cmd );
				cmd.run(); 
				cmd.ran(); 
			}
			
			try {
				Thread.sleep( 1 );
				Thread.yield();
			}
			catch( InterruptedException e ){
				// we don't mind being interrupted
				//e.printStackTrace();
				break; 
			}
		}
		
		EDSDK.EdsTerminateSDK();
		System.out.println( "EDSDK Dispatcher thread says bye!" ); 
	}
	
	
	public static void close() {
		if( dispatcherThread != null && dispatcherThread.isAlive() ){
			dispatcherThread.interrupt(); 
			try {
				dispatcherThread.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	private class OpenSessionCommand extends CanonCommand<Boolean>{
		public void run(){
			setResult( connect() ); 
		}
		
		private boolean connect(){
			int result; 
			
			__EdsObject list[] = new __EdsObject[10]; 
			result = EDSDK.EdsGetCameraList( list ).intValue();		
			if( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Camera failed to initialize" ); 
			}
			
			
			NativeLongByReference outRef = new NativeLongByReference(); 
			result = EDSDK.EdsGetChildCount( list[0], outRef ).intValue(); 
			if( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Number of attached cameras couldn't be read" ); 
			}
			
			long numCams = outRef.getValue().longValue(); 
			if( numCams <= 0 ){
				return setError( 0, "No cameras found. Have you tried turning it off and on again?" ); 
			}
			
			__EdsObject cameras[] = new __EdsObject[1]; 
			result = EDSDK.EdsGetChildAtIndex( list[0], new NativeLong( 0 ), cameras ).intValue(); 
			if ( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Access to camera failed" ); 
			}
			

			EdsVoid context = new EdsVoid( new Pointer( 0 ) );
			CanonCamera.this.edsCamera = edsCamera = cameras[0]; 
			result = EDSDK.EdsSetObjectEventHandler( edsCamera, new NativeLong( EdSdkLibrary.kEdsObjectEvent_All ), CanonCamera.this, context ).intValue();
			if( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Callback handler couldn't be added. " ); 
			}
			
			result = EDSDK.EdsOpenSession( edsCamera ).intValue(); 
			if( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Couldn't open camera session" ); 
			}
			
			return true; 
		}
	}
	
	
	private class CloseSessionCommand extends CanonCommand<Boolean>{
		
		public void run(){
			setResult( close() ); 
		}
		
		private boolean close(){
			System.out.println( "closing session" ); 
			int result = EDSDK.EdsCloseSession( edsCamera ).intValue(); 
			
			if( result != EdSdkLibrary.EDS_ERR_OK ){
				return setError( result, "Couldn't close camera session" ); 
			}
			
			return true; 
		}
	}


	public LiveViewCommand.Begin beginLiveView() {
		return execute( new LiveViewCommand.Begin() ); 
	}
	
	public LiveViewCommand.End endLiveView(){
		return execute( new LiveViewCommand.End() ); 
	}
	
	public  LiveViewCommand.Download downloadLiveView(){
		return execute( new LiveViewCommand.Download() ); 
	}
	
	public FocusModeCommand setFocusMode( FocusModeCommand.Mode mode ){
		return execute( new FocusModeCommand( mode ) );
	}
	
	public FocusModeCommand useAutoFocus(){
		return execute( new FocusModeCommand( FocusModeCommand.Mode.AUTO ) );
	}
	
	public FocusModeCommand useManualFocus(){
		return execute( new FocusModeCommand( FocusModeCommand.Mode.MANUAL ) );
	}
	
	public DriveLenseCommand driveLensNear1(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Near1 ) ); 
	}
	
	public DriveLenseCommand driveLensNear2(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Near2 ) ); 
	}
	
	public DriveLenseCommand driveLensNear3(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Near3 ) ); 
	}
	
	public DriveLenseCommand driveLensFar1(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Far1 ) ); 
	}
	
	public DriveLenseCommand driveLensFar2(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Far2 ) ); 
	}
	
	public DriveLenseCommand driveLensFar3(){
		return execute( new DriveLenseCommand( EdsEvfDriveLens.kEdsEvfDriveLens_Far3 ) ); 
	}
	
	public SetPropertyCommand setAv( int av ){
		return setProperty( EdSdkLibrary.kEdsPropID_Av, av ); 
	}
	
	public SetPropertyCommand setTv( int tv ){
		return setProperty( EdSdkLibrary.kEdsPropID_Tv, tv ); 
	}
	
	public SetPropertyCommand setISO( int iso ){
		return setProperty( EdSdkLibrary.kEdsPropID_ISOSpeed, iso ); 
	}
	
	
	
	public int lastErrorCode(){
		return errorCode; 
	}
	
	public String lastErrorMessage(){
		return errorMessage; 
	}
}