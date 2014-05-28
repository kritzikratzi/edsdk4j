package edsdk.api;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jna.NativeLong;

import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdSdkLibrary.EdsVoid;
import edsdk.bindings.EdSdkLibrary.__EdsObject;

/**
 * The SLRCommand class tries to make your life a whole lot easier when you would like 
 * to create new commands for your camera. 
 * 
 * E.g. one default command implemented with this is the take image cycle. 
 * This is a lot of work because you need to do all the following: 
 * - tell the camera to take an image
 * - test if the image was actually taken, e.g. if autofocus failed or flash wasn't loaded
 * - wait for the notification from the camera that the file was stored 
 * - transfer the image to the disk
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 *
 */
public abstract class CanonCommand<T> implements EdsObjectEventHandler{

	public CanonCamera camera;
	public __EdsObject edsCamera; 
	public static EdSdkLibrary EDSDK = CanonCamera.EDSDK; 
	private boolean finished = false; 
	private boolean waitForFinish = false;
	private boolean ran = false; 
	private T result;
	private ReentrantLock lock = new ReentrantLock();
	private ArrayList<CanonCommandListener<T>> listeners = null; 
	
	public CanonCommand(){
	}
	
	/**
	 * The SLR is just by the dispatch thread automatically just before run is called. 
	 * @param slr
	 */
	public void setSLR( CanonCamera slr ){
		this.camera = slr;
		this.edsCamera = slr.getEdsCamera(); 
	}
	
	/**
	 * This should be short and sweet! 
	 * If your command needs to wait for events 
	 */
	public abstract void run(); 
	
	/**
	 * Sets the result
	 */
	public void setResult( T result ){
		this.result = result; 
	}
	
	/**
	 * By default a SLRCommand will be marked as finished as soon as 
	 * the run method completed. If you attached listens inside run 
	 * and are waiting for a special event to happen before you're done 
	 * please call the notYetFinished() at the end of run(). 
	 * 
	 * This will tell the dispatcher that it should start forwarding event 
	 * messages again, and also it'll wait with the execution of further commands 
	 * until your command somehow calls finish() on itself to let the dispatcher
	 * know that it's done. 
<	 */
	public void notYetFinished(){
		waitForFinish = true; 
	}
	
	/**
	 * Only used in combination with notYetFinished. 
	 * Call this when your command's work is done (e.g. you successfully 
	 * shot and downloaded an image). 
	 * @see CanonCommand#notYetFinished()
	 */
	public void finish(){
		finished = true; 
		notifyListenersIfDone();
	}
	
	/**
	 * Don't _ever_ call this, promise! 
	 */
	protected void ran(){
		ran = true; 
		notifyListenersIfDone();
	}
	
	/**
	 * Checks if this command finished it's work. Only useful in combination with 
	 * finish() and notYetFinished(). 
	 * @see CanonCommand#notYetFinished()
	 * @return
	 */
	protected boolean finished(){
		return waitForFinish? finished : ran; 
	}
	
	/**
	 * Sends a command to the camera
	 * @return 
	 */
	public int sendCommand( long command, long params ){
		return EDSDK.EdsSendCommand( camera.getEdsCamera(), new NativeLong( command ), new NativeLong( params ) ).intValue();
	}
	
	/**
	 * This is a default implementation of the invoke method. 
	 * Just override it if you need to use events, the dispatcher will 
	 * take care of (un-)registering the listeners for you. 
	 * 
	 * Also don't worry about the return value, just use null! 
	 */
	@Override
	public NativeLong apply(NativeLong inEvent, __EdsObject inRef, EdsVoid inContext) {
		return new NativeLong( 0 ); 
	}
	
	
	/**
	 * Waits until the task is completed and returns the result. 
	 * If the result is not returned in time an interrupted exception is thrown. 
	 * @throws InterruptedException 
	 */
	public T get( long timeout ) throws InterruptedException{
		long now = System.currentTimeMillis(); 
		try{
			while( !finished() && ( timeout == 0 || System.currentTimeMillis() - now < timeout ) ){
				Thread.sleep( 1 );
			}
		}
		catch( InterruptedException e ){
			throw e; 
		}
		
		if( finished() ){
			return result; 
		}
		else{
			throw new InterruptedException("edsdkp5 - command didn't return the result in time" ); 
		}
	}
	
	
	/**
	 * Waits until the task is completed and returns the result. 
	 * @return
	 */
	public T get(){
		if( this.camera == null ){
			System.err.println( "Attention: " + getClass() ); 
			System.err.println( "  This command was not yet added to a queue " ); 
			System.err.println( "  with CanonCamera.execute( ... )" );
			System.err.println( "  This way you might wait forever until .get() returns. " ); 
		}
		
		while( !finished() ){
			try {
				Thread.sleep( 1 );
			}
			catch ( InterruptedException e ) {
				return null; 
			} 
		}
		
		return result; 
	}
	
	/** 
	 * An alias for get() 
	 * 
	 * @return
	 */
	public T now(){
		return get(); 
	}
	
	
	/**
	 * Add a done listener 
	 * @param listener
	 */
	public void whenDone( CanonCommandListener<T> listener ){
		lock.lock(); 
		if( finished() ){
			listener.success( result ); 
		}
		else if( listeners == null ){
			listeners = new ArrayList<CanonCommandListener<T>>(); 
			listeners.add( listener );
		}
		else{
			listeners.add( listener );
		}
		lock.unlock(); 
	}
	
	private void notifyListenersIfDone(){
		lock.lock();
		if( finished() && listeners != null ){
			for( CanonCommandListener<T> listener : listeners ){
				listener.success( result );
			}
			listeners = null;  
		}
		lock.unlock(); 
	}
}
