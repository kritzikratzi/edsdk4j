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
 * something
 * that's not possible with the higher level api from edsdk.utils.* directly
 * then
 * this is the place to start!
 * 
 * @author hansi
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
