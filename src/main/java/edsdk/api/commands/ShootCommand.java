package edsdk.api.commands;

import java.io.File;
import java.io.IOException;

import com.sun.jna.Pointer;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsDirectoryItemRef;
import edsdk.utils.CanonConstants.EdsCameraCommand;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonConstants.EdsImageType;
import edsdk.utils.CanonConstants.EdsObjectEvent;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsSaveTo;
import edsdk.utils.CanonConstants.EdsShutterButton;
import edsdk.utils.CanonUtils;

/**
 * Takes an image and downloads it to the file system.
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
public class ShootCommand extends CanonCommand<File[]> {

    private final EdsSaveTo saveTo;
    private final boolean appendFileExtension;
    private File[] dest = null;
    private boolean oldEvfMode;
    private int shotAttempts;
    private int count;

    public ShootCommand() {
        this( EdsSaveTo.kEdsSaveTo_Both );
    }

    public ShootCommand( final EdsSaveTo saveTo ) {
        this( saveTo, Integer.MAX_VALUE, (File[]) null, false );
    }

    public ShootCommand( final EdsSaveTo saveTo, final int shotAttempts ) {
        this( saveTo, shotAttempts, (File[]) null, false );
    }

    public ShootCommand( final EdsSaveTo saveTo, final int shotAttempts,
                         final File dest ) {
        this( saveTo, shotAttempts, new File[] { dest }, false );
    }

    public ShootCommand( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest ) {
        this( saveTo, shotAttempts, dest, false );
    }

    public ShootCommand( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest, final boolean appendFileExtension ) {
        this.saveTo = saveTo;
        this.shotAttempts = shotAttempts;
        this.appendFileExtension = appendFileExtension;
        count = 1;

        if ( dest != null && ( dest.length < 0 || dest.length > 2 ) ) {
            throw new IllegalArgumentException( "dest must contain one or two file paths (depending on camera image quality settings)" );
        }
        this.dest = dest;
    }

    @Override
    public void run() {
        EdsError err = EdsError.EDS_ERR_OK;

        if ( camera.getEdsCamera() != null ) {

            // Check if there is more than one image
            final long imageQuality;
            try {
                imageQuality = CanonUtils.getPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_ImageQuality );
            }
            catch ( final IllegalArgumentException e ) {
                System.err.println( e.getMessage() );
                return;
            }

            final EdsImageType secondaryImageType = EdsImageType.enumOfValue( (int) ( imageQuality >>> 4 & 0xf ) );
            if ( secondaryImageType != EdsImageType.kEdsImageType_Unknown ) {
                count = 2;
            }

            if ( dest == null ) {
                dest = new File[count];
            } else if ( dest.length < count ) {
                dest = new File[] { dest[0], null };
            }

            for ( final File f : dest ) {
                if ( f != null ) {
                    try {
                        f.getCanonicalPath();
                    }
                    catch ( final IOException e ) {
                        System.err.println( "The file path \"" + f.getPath() +
                                            "\" contains characters that are invalid for this filesystem, stopping..." );
                        return;
                    }
                }
            }

            err = CanonUtils.setPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_SaveTo, saveTo );
            if ( err == EdsError.EDS_ERR_OK &&
                 !EdsSaveTo.kEdsSaveTo_Camera.equals( saveTo ) ) {
                CanonUtils.setCapacity( camera.getEdsCamera() );
            }

            err = EdsError.EDS_ERR_UNIMPLEMENTED;
            while ( shotAttempts > 0 && err != EdsError.EDS_ERR_OK ) {
                oldEvfMode = CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), true );
                if ( oldEvfMode ) {
                    CanonUtils.endLiveView( camera.getEdsCamera() );
                }
                err = sendCommand( EdsCameraCommand.kEdsCameraCommand_TakePicture, 0 );
                // System.out.println( "> result " + err.value() + ": " + err.name() + " - " + err.description() );
                if ( err != EdsError.EDS_ERR_OK ) {
                    try {
                        Thread.sleep( 1000 );
                    }
                    catch ( final InterruptedException e ) {
                        Thread.currentThread().interrupt(); // restore interrupted status
                        return;
                    }
                } else {
                    if ( CanonUtils.isMirrorLockupEnabled( camera.getEdsCamera() ) ) {
                        sendCommand( EdsCameraCommand.kEdsCameraCommand_PressShutterButton, EdsShutterButton.kEdsCameraCommand_ShutterButton_Completely_NonAF );
                        sendCommand( EdsCameraCommand.kEdsCameraCommand_PressShutterButton, EdsShutterButton.kEdsCameraCommand_ShutterButton_OFF );
                    }
                }
                shotAttempts--;
            }
        }
        if ( err == EdsError.EDS_ERR_OK ) {
            //System.out.println( "Took image, waiting for camera" );
            notYetFinished();
        }/*
          * else {
          * System.out.println( "No image could be taken, stopping..." );
          * }
          */
    }

    @Override
    public EdsError apply( final EdsObjectEvent inEvent,
                           final EdsBaseRef inRef, final Pointer inContext ) {
        return apply( inEvent, new EdsDirectoryItemRef( inRef.getPointer() ), inContext );
    }

    public EdsError apply( final EdsObjectEvent inEvent,
                           final EdsDirectoryItemRef inRef,
                           final Pointer inContext ) {
        if ( inEvent == EdsObjectEvent.kEdsObjectEvent_DirItemCreated ||
             inEvent == EdsObjectEvent.kEdsObjectEvent_DirItemRequestTransfer ) {
            count--;
            System.out.println( "Camera saved an image file" +
                                ( count > 0 ? ", " + count + " file remains"
                                           : "" ) );
            if ( !EdsSaveTo.kEdsSaveTo_Camera.equals( saveTo ) ) {
                dest[dest.length - count - 1] = CanonUtils.download( inRef, dest[dest.length -
                                                                                 count -
                                                                                 1], appendFileExtension );
            }
            if ( count == 0 ) {
                setResult( dest );
                if ( oldEvfMode ) {
                    CanonUtils.beginLiveView( camera.getEdsCamera() );
                }
                finish();
            }
        }

        return EdsError.EDS_ERR_OK;
    }
}
