package edsdk.api;

import java.io.File;
import java.util.ArrayList;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsDirectoryItemRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonConstant.EdsCameraCommand;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsObjectEvent;
import edsdk.utils.CanonUtils;

/**
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
public class QuickShootManager implements EdsObjectEventHandler {

    ArrayList<EdsDirectoryItemRef> refs = new ArrayList<EdsDirectoryItemRef>();
    private final CanonCamera camera;
    int wanted = 0;

    //TODO: bring up to date with ShootCommand.run
    public QuickShootManager( final CanonCamera camera ) {
        this.camera = camera;
        // disable liveview... 
        camera.execute( new CanonCommand<Void>() {

            @Override
            public void run() {
                CanonUtils.endLiveView( camera.getEdsCamera() );
            }
        } );
    }

    public CanonCommand<Void> trigger() {
        if ( wanted == 0 ) {
            camera.addObjectEventHandler( this );
        }

        wanted++;
        return camera.execute( new Shutter() );
    }

    public CanonCommand<ArrayList<File>> downloadAll() {
        // wait until we have enough files ... 
        while ( refs.size() < wanted ) {
            try {
                Thread.sleep( 1 );
                Thread.yield();
            }
            catch ( final InterruptedException e ) {
                e.printStackTrace();
                return null;
            }
        }

        return camera.execute( new Downloader() );
    }

    public void reset() {
        refs.clear();
        camera.removeObjectEventHandler( this );
    }

    //TODO: bring up to date with ShootCommand.run
    private class Shutter extends CanonCommand<Void> {

        //		private boolean oldEvfMode;
        @Override
        public void run() {
            EdsError result = EdsError.EDS_ERR_UNIMPLEMENTED;
            while ( result != EdsError.EDS_ERR_OK ) {
                //				oldEvfMode = CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), true ); 
                //				if( oldEvfMode ) CanonUtils.endLiveView( camera.getEdsCamera() );

                result = sendCommand( EdsCameraCommand.kEdsCameraCommand_TakePicture, 0 );
            }
        }
    }

    // TODO: bring up to date with ShootCommand.apply
    private class Downloader extends CanonCommand<ArrayList<File>> {

        @Override
        public void run() {
            final ArrayList<File> results = new ArrayList<File>();
            for ( final EdsDirectoryItemRef ref : refs ) {
                results.add( CanonUtils.download( ref, null, true ) );
            }

            setResult( results );
            reset();
        }
    }

    @Override
    public NativeLong apply( final NativeLong inEvent, final EdsBaseRef inRef,
                             final Pointer inContext ) {
        return apply( inEvent, new EdsDirectoryItemRef( inRef.getPointer() ), inContext );
    }

    //TODO: bring up to date with ShootCommand.apply
    public EdsError apply( final EdsObjectEvent inEvent,
                           final EdsDirectoryItemRef inRef,
                           final Pointer inContext ) {
        if ( inEvent == EdsObjectEvent.kEdsObjectEvent_DirItemCreated ||
             inEvent == EdsObjectEvent.kEdsObjectEvent_DirItemRequestTransfer ) {
            refs.add( inRef );
        }

        return null;
    }
}
