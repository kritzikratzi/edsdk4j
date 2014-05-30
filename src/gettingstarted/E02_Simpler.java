package gettingstarted;

import java.io.File;
import java.io.IOException;

import edsdk.api.CanonCamera;
import edsdk.utils.CanonConstant.EdsSaveTo;

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
public class E02_Simpler {

    public static void main( final String[] args ) throws InterruptedException, IOException {
        final CanonCamera cam = new CanonCamera();

        if ( cam.openSession() ) {
            final File[] photos = cam.shoot( EdsSaveTo.kEdsSaveTo_Host ).get();

            if ( photos != null ) {
                for ( final File photo : photos ) {
                    if ( photo != null ) {
                        System.out.println( "Saved photo as: " +
                                            photo.getCanonicalPath() );
                    }
                }
            }

            cam.closeSession();
        }

        CanonCamera.close();
    }
}
