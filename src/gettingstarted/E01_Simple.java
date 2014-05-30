package gettingstarted;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.NativeLongByReference;

import edsdk.api.CanonCamera;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraListRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdSdkLibrary.EdsDirectoryItemRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonUtils;

/**
 * Simple example of JNA interface mapping and usage.
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
public class E01_Simple {

    public static void main( final String[] args ) throws InterruptedException {
        int result;

        result = CanonCamera.EDSDK.EdsInitializeSDK().intValue();
        E01_Simple.check( result );

        final EdsCameraListRef.ByReference list = new EdsCameraListRef.ByReference();
        E01_Simple.debug( list );
        result = CanonCamera.EDSDK.EdsGetCameraList( list ).intValue();
        E01_Simple.debug( list );
        E01_Simple.check( result );

        final NativeLongByReference outRef = new NativeLongByReference();
        result = CanonCamera.EDSDK.EdsGetChildCount( list.getValue(), outRef ).intValue();
        E01_Simple.check( result );
        System.out.println( "Cameras: " + outRef.getValue().longValue() );
        final long numCams = outRef.getValue().longValue();
        if ( numCams == 0 ) {
            System.err.println( "no camera found" );
        }

        final EdsCameraRef.ByReference camera = new EdsCameraRef.ByReference();
        E01_Simple.debug( camera );
        result = CanonCamera.EDSDK.EdsGetChildAtIndex( list.getValue(), new NativeLong( 0 ), camera ).intValue();
        E01_Simple.debug( camera );
        E01_Simple.check( result );

        final Pointer context = new Pointer( 0 );
        final EdsObjectEventHandler handler = new EdsObjectEventHandler() {

            @Override
            public NativeLong apply( final NativeLong inEvent,
                                     final EdsBaseRef inRef,
                                     final Pointer inContext ) {
                System.out.println( "Event!!!" + inEvent.doubleValue() + ", " +
                                    inContext );
                if ( inEvent.intValue() == 516 ) {
                    CanonUtils.download( new EdsDirectoryItemRef( inRef.getPointer() ), null, false );
                }
                return new NativeLong( -1 );
                //return -1;
            }
        };

        result = CanonCamera.EDSDK.EdsSetObjectEventHandler( camera.getValue(), new NativeLong( EdSdkLibrary.kEdsObjectEvent_All ), handler, context ).intValue();
        E01_Simple.check( result );

        result = CanonCamera.EDSDK.EdsOpenSession( camera.getValue() ).intValue();
        E01_Simple.check( result );

        // Do stuff here, like ... take an image...
        result = CanonCamera.EDSDK.EdsSendCommand( camera.getValue(), new NativeLong( EdSdkLibrary.kEdsCameraCommand_TakePicture ), new NativeLong( 0 ) ).intValue();
        E01_Simple.check( result );

        // Wait a little bit!
        E01_Simple.dispatchMessages();

        CanonCamera.EDSDK.EdsTerminateSDK();
    }

    public static void check( final int result ) {
        if ( result != EdSdkLibrary.EDS_ERR_OK ) {
            final EdsError err = CanonUtils.toEdsError( result );
            System.err.println( "Error " + err.value() + ": " + err.name() +
                                " - " + err.description() );
        }
    }

    public static void debug( final EdsBaseRef.ByReference ... obj ) {
        System.out.println( "----------------" );
        for ( final EdsBaseRef.ByReference o : obj ) {
            if ( o != null && o.getPointer() != null ) {
                System.out.println( o + ": " + o.getPointer().getShort( 0 ) );
                E01_Simple.debug( o.getValue() );
            }
        }
    }

    public static void debug( final EdsBaseRef ... obj ) {
        System.out.println( "----------------" );
        for ( final EdsBaseRef o : obj ) {
            if ( o != null && o.getPointer() != null ) {
                System.out.println( o + ": " + o.getPointer().getLong( 0 ) );
            }
        }
    }

    public static void dispatchMessages() {
        // This bit never returns from GetMessage
        int result = -1;
        final MSG msg = new MSG();

        while ( result != 0 ) {
            result = User32.INSTANCE.GetMessage( msg, null, 0, 0 );
            if ( result == -1 ) {
                System.err.println( "error in get message" );
                break;
            } else {
                User32.INSTANCE.TranslateMessage( msg );
                try {
                    User32.INSTANCE.DispatchMessage( msg );
                }
                catch ( final Error e ) {
                    e.printStackTrace();
                }
            }
        }

    }

}
