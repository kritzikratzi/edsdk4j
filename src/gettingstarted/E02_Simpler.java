package gettingstarted;

import java.io.File;
import java.io.IOException;

import edsdk.api.CanonCamera;
import edsdk.utils.CanonConstant.EdsSaveTo;

/** Simple example of JNA interface mapping and usage. */
public class E02_Simpler {

    public static void main( final String[] args ) throws InterruptedException, IOException {
        final CanonCamera cam = new CanonCamera();

        if ( cam.openSession() ) {
            final File[] photos = cam.shoot( EdsSaveTo.kEdsSaveTo_Camera );

            if ( photos != null ) {
                for ( final File photo : photos ) {
                    if ( photo != null ) {
                        System.out.println( photo.getCanonicalPath() );
                    }
                }
            }

            cam.closeSession();
        }

        CanonCamera.close();
    }
}
