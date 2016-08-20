package edsdk.api;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsCameraCommand;
import edsdk.utils.CanonConstants.EdsCameraStatusCommand;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonConstants.EdsObjectEvent;
import edsdk.utils.CanonUtils;

/**
 * The CanonCommand class tries to make your life a whole lot easier when you
 * would like
 * to create new commands for your camera.
 * 
 * E.g. one default command implemented with this is the take image cycle.
 * This is a lot of work because you need to do all the following:
 * - tell the camera to take an image
 * - test if the image was actually taken, e.g. if autofocus failed or flash
 * wasn't loaded
 * - wait for the notification from the camera that the file was stored
 * - transfer the image to the disk
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 * @author Ananta Palani
 * 
 */
public abstract class CanonCommand<T> implements EdsObjectEventHandler {

    public BaseCanonCamera camera;
    // edsCamera can be reached also through camera.getEdsCamera(), 
    // but it's needed so much that this is a handy shortcut. 
    public EdsCameraRef edsCamera; 
    private boolean finished = false;
    private boolean waitForFinish = false;
    private boolean ran = false;
    private T result;
    private final ReentrantLock lock = new ReentrantLock();
    private ArrayList<CanonCommandListener<T>> listeners = null;

    public CanonCommand() {}

    /**
     * The camera is set by the dispatch thread automatically just before run is
     * called.
     * 
     * @param baseCanonCamera
     */
    public void setCamera( final BaseCanonCamera baseCanonCamera ) {
        this.camera = baseCanonCamera;
        this.edsCamera = baseCanonCamera.getEdsCamera(); 
    }

    /**
     * This should be short and sweet!
     * If your command needs to wait for events
     */
    public abstract void run();

    /**
     * Sets the result
     */
    public void setResult( final T result ) {
        this.result = result;
    }

    /**
     * By default a CanonCommand will be marked as finished as soon as
     * the run method completed. If you attached listens inside run
     * and are waiting for a special event to happen before you're done
     * please call the notYetFinished() at the end of run().
     * 
     * This will tell the dispatcher that it should start forwarding event
     * messages again, and also it'll wait with the execution of further
     * commands until your command somehow calls finish() on itself to
     * let the dispatcher know that it's done.
     * <
     */
    public void notYetFinished() {
        waitForFinish = true;
    }

    /**
     * Only used in combination with notYetFinished.
     * Call this when your command's work is done (e.g. you successfully
     * shot and downloaded an image).
     * 
     * @see CanonCommand#notYetFinished()
     */
    public void finish() {
        finished = true;
        notifyListenersIfDone();
    }

    /**
     * Don't _ever_ call this, promise!
     */
    protected void ran() {
        ran = true;
        notifyListenersIfDone();
    }

    /**
     * Checks if this command finished it's work. Only useful in combination
     * with
     * finish() and notYetFinished().
     * 
     * @see CanonCommand#notYetFinished()
     * @return
     */
    public boolean finished() {
        return waitForFinish ? finished : ran;
    }

    /**
     * Sends a command to the camera
     * 
     * @return
     */
    public EdsError sendCommand( final EdsCameraCommand command,
                                 final DescriptiveEnum<? extends Number> params ) {
        return sendCommand( command, params.value().longValue() );
    }

    /**
     * Sends a command to the camera
     * 
     * @return
     */
    public EdsError sendCommand( final EdsCameraCommand command,
                                 final long params ) {
        return CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSendCommand( camera.getEdsCamera(), new NativeLong( command.value() ), new NativeLong( params ) ) );
    }

    /**
     * Sends a status command to the camera
     * 
     * @return
     */
    public EdsError sendStatusCommand( final EdsCameraStatusCommand command,
                                       final DescriptiveEnum<? extends Number> params ) {
        return sendStatusCommand( command, params.value().longValue() );
    }

    /**
     * Sends a status command to the camera
     * 
     * @return
     */
    public EdsError sendStatusCommand( final EdsCameraStatusCommand command,
                                       final long params ) {
        return CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSendStatusCommand( camera.getEdsCamera(), new NativeLong( command.value() ), new NativeLong( params ) ) );
    }

    /**
     * This is a default implementation of the invoke method.
     * Just override it if you need to use events, the dispatcher will
     * take care of (un-)registering the listeners for you.
     * 
     * It's better to implement the EdsError apply() and use that instead since
     * values are conveniently wrapped as EdsObjectEvents for you
     * 
     * Also don't worry about the return value, you can use null or
     * EdsError.EDS_ERR_OK!
     */
    @Override
    public NativeLong apply( final NativeLong inEvent, final EdsBaseRef inRef,
                             final Pointer inContext ) {
        return new NativeLong( apply( EdsObjectEvent.enumOfValue( inEvent.intValue() ), inRef, inContext ).value() );
    }

    /**
     * If you don't need to use events just return EdsError.EDS_ERR_OK
     * 
     * The dispatcher will take care of (un-)registering the listeners for you.
     */
    public EdsError apply( final EdsObjectEvent inEvent,
                           final EdsBaseRef inRef, final Pointer inContext ) {
        return EdsError.EDS_ERR_OK;
    }

    /**
     * Waits until the command is completed and returns the result.
     * 
     * @return
     */
    public T get() {
        try {
            return get( 0 );
        }
        catch ( final InterruptedException e ) {
            // this shouldn't happen since get( 0 ) internally handles InterruptedException when timeout=0.
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Waits until the command is completed and returns the result.
     * If the result is not returned in time an interrupted exception is thrown.
     * 
     * @throws InterruptedException
     */
    public T get( final long timeout ) throws InterruptedException {
        if ( this.camera == null ) {
            System.err.println( "Attention: " + getClass() );
            System.err.println( "  This command was not yet added to a queue " );
            System.err.println( "  with CanonCamera.execute( ... )" );
            System.err.println( "  This way you might wait forever until .get() returns. " );
        }

        final long startTime = System.currentTimeMillis();
        try {
            while ( !finished() &&
                    ( timeout == 0 || System.currentTimeMillis() - startTime < timeout ) ) {
                Thread.sleep( 1 );
            }
        }
        catch ( final InterruptedException e ) {
            System.out.println( "Interrupt received by CanonCommand, stopping..." );
            Thread.currentThread().interrupt(); // restore interrupted status
            return null;
        }

        if ( finished() ) {
            return result;
        } else {
            //TODO: should we just interrupt the thread instead?
            throw new InterruptedException( "edsdkp5 - command didn't return the result in time" );
        }
    }

    /**
     * An alias for get()
     * 
     * @return
     */
    public T now() {
        return get();
    }

    /**
     * Add a done listener
     * 
     * @param listener
     */
    public void whenDone( final CanonCommandListener<T> listener ) {
        lock.lock();
        if ( finished() ) {
            listener.success( result );
        } else {
            if ( listeners == null ) {
                listeners = new ArrayList<CanonCommandListener<T>>();
            }
            listeners.add( listener );
        }
        lock.unlock();
    }

    private void notifyListenersIfDone() {
        lock.lock();
        if ( finished() && listeners != null ) {
            for ( final CanonCommandListener<T> listener : listeners ) {
                listener.success( result );
            }
            listeners = null;
        }
        lock.unlock();
    }
}
