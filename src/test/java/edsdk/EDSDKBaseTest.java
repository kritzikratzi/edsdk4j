package edsdk;

import edsdk.bindings.EdSdkLibrary;
import edsdk.utils.CanonUtils;
import edsdk.utils.CanonConstants.EdsError;

/**
 * base Class for Testing Canon EDSDK
 * @author wf
 *
 */
public class EDSDKBaseTest {

    public static void check( final int result ) {
        if ( result != EdSdkLibrary.EDS_ERR_OK ) {
            final EdsError err = CanonUtils.toEdsError( result );
            System.err.println( "Error " + err.value() + ": " + err.name() +
                                " - " + err.description() );
        }
    }
}
