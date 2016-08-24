package edsdk.api;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import edsdk.bindings.EdSdkLibrary;

/**
 * this class is a preparationof  EDSDK Version 3.4 implementation that should work on MacOSX
 * as of 2016-08-24 it doesn't work yet
 * @author wf
 *
 */
public class CanonCameraMac extends BaseCanonCamera {
 // This gives you direct access to the EDSDK
    public static EdSdkLibrary EDSDK = null;

    static {
        initLibrary();
        options.put(Library.OPTION_CALLING_CONVENTION, Function.ALT_CONVENTION);
        // options.put(Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION);
        EDSDK = (EdSdkLibrary) Native.loadLibrary( BaseCanonCamera.libraryInfo.dllLoc, EdSdkLibrary.class, options );
        if (Platform.isMac()) {
        	String javalibpath = System.getProperty("java.library.path");
        	System.out.println(javalibpath);
        }
    }

}
