/**
 * 
 */
package edsdk;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sun.jna.NativeLong;

import edsdk.api.BaseCanonCamera;
import edsdk.api.CanonCameraMac;
import edsdk.bindings.EdSdkLibrary;

/**
 * test the generated EDSDK files
 * @author wf
 *
 */
public class TestEDSDK extends EDSDKBaseTest {

    @Test
    public void testInitialization() {
        BaseCanonCamera.debug=true;
        EdSdkLibrary edsdk = CanonCameraMac.EDSDK;
        assertNotNull(edsdk);
        System.out.println(edsdk.getClass().getName());
        NativeLong callResult = edsdk.EdsInitializeSDK();
        int result = callResult.intValue();
        check( result );
    }
    

}
