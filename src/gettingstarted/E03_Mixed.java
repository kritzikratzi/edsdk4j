package gettingstarted;

import edsdk.api.CanonCamera;
import edsdk.api.CanonCommand;

/**
 * In most situations you'll want to mix/match the raw power of the EDSDK,
 * but use the the CanonCamera class to take care of all the heavy lifting
 * like threaded access and the other small issues that need to be taken care
 * of.
 * 
 * This example here doesn't do much, but if you ever need to accomplish
 * something that's not possible with the higher level api from
 * edsdk.utils.* directly then this is the place to start!
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
public class E03_Mixed {

    public static void main( final String[] args ) throws InterruptedException {
        final CanonCamera camera = new CanonCamera();
        if ( camera.openSession() ) {

            final boolean result = camera.executeNow( new CanonCommand<Boolean>() {

                @Override
                public void run() {
                    // Do your thing here, like bulb shooting, setting properties, 
                    // whatever you like! 
                    // 
                    // you have access to all edsdk methods using
                    // EDSDK.*
                    // Access to the above camera object using camera
                    // or if you need the edsObject that the camera is linked to 
                    // use camera.getEdsCamera()
                    //
                    // when you're done set the result that should be returned.
                    setResult( true );
                }
            } );

            // Now do something with the result
            if ( !result ) {
                System.out.println( "oh, it didn't work... " );
            }

            camera.closeSession();
        }

        CanonCamera.close();
    }
}
