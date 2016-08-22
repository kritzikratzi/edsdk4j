package edsdk.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import edsdk.api.commands.DriveLensCommand;
import edsdk.api.commands.FocusModeCommand;
import edsdk.api.commands.GetPropertyCommand;
import edsdk.api.commands.GetPropertyDescCommand;
import edsdk.api.commands.LiveViewCommand;
import edsdk.api.commands.SetPropertyCommand;
import edsdk.api.commands.ShootCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraListRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdsFocusInfo;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.bindings.EdsRect;
import edsdk.bindings.EdsSize;
import edsdk.bindings.EdsTime;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsAEMode;
import edsdk.utils.CanonConstants.EdsAFMode;
import edsdk.utils.CanonConstants.EdsAv;
import edsdk.utils.CanonConstants.EdsBatteryQuality;
import edsdk.utils.CanonConstants.EdsBracket;
import edsdk.utils.CanonConstants.EdsColorSpace;
import edsdk.utils.CanonConstants.EdsCustomFunction;
import edsdk.utils.CanonConstants.EdsDataType;
import edsdk.utils.CanonConstants.EdsDriveMode;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsEvfDriveLens;
import edsdk.utils.CanonConstants.EdsEvfHistogramStatus;
import edsdk.utils.CanonConstants.EdsEvfOutputDevice;
import edsdk.utils.CanonConstants.EdsEvfZoom;
import edsdk.utils.CanonConstants.EdsExposureCompensation;
import edsdk.utils.CanonConstants.EdsFilterEffect;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsMeteringMode;
import edsdk.utils.CanonConstants.EdsObjectEvent;
import edsdk.utils.CanonConstants.EdsPictureStyle;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsSaveTo;
import edsdk.utils.CanonConstants.EdsTonigEffect;
import edsdk.utils.CanonConstants.EdsTv;
import edsdk.utils.CanonConstants.EdsWhiteBalance;
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
 * or the method canonCamera.invoke( CanonCommand cmd );
 * 
 * The latter is basically the same, but allows you to easily get a return
 * integer value, like:
 * 
 * <pre>
 * int result = SLR.invoke(
 *     new CanonCommand() {
 *         public int run() {
 *             return ...;
 *         }
 *     }
 * );
 * </pre>
 * 
 * This class also automatically processes and forwards all windows-style
 * messages.
 * This is required to forward camera events into the edsdk. Currently there is
 * no way to disable this if it conflicts with your software.
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 * @author Ananta Palani
 */
public class CanonCamera extends BaseCanonCamera implements EdsObjectEventHandler {
    // Libraries needed to forward windows messages
    private static User32 lib;

    // This gives you direct access to the EDSDK
    public static EdSdkLibrary EDSDK = null;

    static {
        options.put( Library.OPTION_CALLING_CONVENTION, StdCallLibrary.STDCALL_CONVENTION );
        initLibrary();
        EDSDK = (EdSdkLibrary) Native.loadLibrary( CanonCamera.edsdkDllLoc, EdSdkLibrary.class, CanonCamera.options );
        lib = User32.INSTANCE;
    }
    static {
        // Tells the app to throw an error instead of crashing entirely. 
        // Native.setProtected( true ); 
        // We actually want our apps to crash, because something very dramatic 
        // is going on when the user receives this kind of crash message from 
        // the os and it puts the developer under pressure to fix the issue. 
        // If we enable Native.setProtected the app might just freeze, 
        // which is imho more annoying than a proper crash. 
        // Anyways, if you want the exception-throwing-instead-crashing behaviour
        // just call the above code as early as possible in your main method. 

        // Start the dispatch thread
        CanonCamera.dispatcherThread = new Thread() {

            @Override
            public void run() {
                CanonCamera.dispatchMessages();
            }
        };
        CanonCamera.dispatcherThread.start();

        // people are sloppy! 
        // so we add a shutdown hook to close camera connections
        Runtime.getRuntime().addShutdownHook( new Thread() {

            @Override
            public void run() {
                CanonCamera.close();
            }
        } );
    }

    /**
     * Dispatches windows messages and executes commands
     */
    private static void dispatchMessages() {
        // Do some initializing
        final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsInitializeSDK() );
        if ( err != EdsError.EDS_ERR_OK ) {
            System.err.println( "EDSDK failed to initialize, most likely you won't be able to speak to your camera (ERROR: " +
                                err.description() + " )" );
        }

        final MSG msg = new MSG();

        CanonCommand<?> cmd = null;

        while ( !Thread.currentThread().isInterrupted() ) {
            // do we have a new message? 
            final boolean hasMessage = CanonCamera.lib.PeekMessage( msg, null, 0, 0, 1 ); // peek and remove
            if ( hasMessage ) {
                CanonCamera.lib.TranslateMessage( msg );
                CanonCamera.lib.DispatchMessage( msg );
            }

            // is there a command we're currently working on? 
            if ( cmd != null ) {
                if ( cmd.finished() ) {
                    //System.out.println( "Command finished" );
                    // great!
                    cmd.camera.removeObjectEventHandler( cmd );
                    cmd = null;
                }
            }

            // are we free to do new work, and is there even new work to be done? 
            if ( !CanonCamera.queue.isEmpty() && cmd == null ) {
                cmd = CanonCamera.queue.poll();
                /*
                 * System.out.println( "\nReceived new command, processing " +
                 * cmd.getClass().getCanonicalName().substring(
                 * cmd.getClass().getPackage().getName().length() + 1 ) );
                 */
                if ( ! ( cmd instanceof OpenSessionCommand ) ) {
                    cmd.camera.addObjectEventHandler( cmd );
                }
                cmd.run();
                cmd.ran();
            }

            try {
                Thread.sleep( 1 );
            }
            catch ( final InterruptedException e ) {
                System.out.println( "\nInterrupt received in CanonCamera, stopping..." );
                Thread.currentThread().interrupt(); // restore interrupted status
                break;
            }
        }

        CanonCamera.EDSDK.EdsTerminateSDK();
        System.out.println( "EDSDK Dispatcher thread says bye!" );
    }

    public static void close() {
        if ( CanonCamera.dispatcherThread != null &&
             CanonCamera.dispatcherThread.isAlive() ) {
            CanonCamera.dispatcherThread.interrupt();

            try {
                CanonCamera.dispatcherThread.join();
            }
            catch ( final InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    private class OpenSessionCommand extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( connect() );
        }

        private boolean connect() {
            EdsError err = EdsError.EDS_ERR_OK;
            
            final EdsCameraListRef.ByReference listRef = new EdsCameraListRef.ByReference();
            final EdsCameraRef.ByReference cameraRef = new EdsCameraRef.ByReference();
            //PointerByReference listRef2=new PointerByReference();
            //PointerByReference cameraRef2=new PointerByReference();
            try {
            	
            	err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetCameraList( listRef ) );
                //err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetCameraList( listRef2 ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception("Camera failed to initialize");
                }

                final NativeLongByReference outRef = new NativeLongByReference();
                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildCount( listRef.getValue(), outRef ) );
                //EdsCameraListRef inRef=new EdsCameraListRef(listRef2.getPointer());
                //err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildCount(inRef, outRef ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Number of attached cameras couldn't be read" );
                }

                final long numCams = outRef.getValue().longValue();
                if ( numCams <= 0 ) {
                    err = EdsError.EDS_ERR_DEVICE_NOT_FOUND;
                    throw new Exception( "No cameras found. Have you tried turning it off and on again?" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildAtIndex( listRef.getValue(), new NativeLong( 0 ), cameraRef ) );
                //err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildAtIndex( listRef2.getPointer(), new NativeLong( 0 ), cameraRef2 ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Access to camera failed" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSetObjectEventHandler( cameraRef.getValue(), new NativeLong( EdsObjectEvent.kEdsObjectEvent_All.value() ), CanonCamera.this, new Pointer( 0 ) ) );
                //EdsVoid voidNull=new EdsVoid(new Pointer(0));
                //err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSetObjectEventHandler( cameraRef2.getPointer(), new NativeLong( EdsObjectEvent.kEdsObjectEvent_All.value() ), CanonCamera.this, voidNull ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Callback handler couldn't be added" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsOpenSession( cameraRef.getValue() ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Couldn't open camera session" );
                }
                
                CanonCamera.this.edsCamera = cameraRef.getValue();
            } catch (Exception e) {
                CanonUtils.release( cameraRef );
                setError( err, e.getMessage() );
            } finally {
                CanonUtils.release( listRef );
            }

            return err == EdsError.EDS_ERR_OK;
        }

    }

    private class CloseSessionCommand extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( close() );
        }

        private boolean close() {
            //System.out.println( "closing session" );
            final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsCloseSession( edsCamera ) );
            CanonUtils.release( edsCamera );

            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Couldn't close camera session" );
            }

            return true;
        }
    }

    public boolean openSession() {
        final Boolean result = executeNow( new OpenSessionCommand() );
        return result != null && result;
    }

    public boolean closeSession() {
        final Boolean result = executeNow( new CloseSessionCommand() );
        return result != null && result;
    }
}
