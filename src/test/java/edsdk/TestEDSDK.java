/**
 * 
 */
package edsdk;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import edsdk.api.BaseCanonCamera;
import edsdk.api.CanonCamera;
import edsdk.api.CanonCameraMac;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsCameraListRef;

/**
 * test the generated EDSDK files
 * @author wf
 *
 */
public class TestEDSDK extends EDSDKBaseTest {

    @Test
    public void testInitialization() {
        BaseCanonCamera.debug=true;
        EdSdkLibrary edsdk = CanonCamera.EDSDK;
        assertNotNull(edsdk);
        NativeLong callResult = edsdk.EdsInitializeSDK();
        check( callResult );
        PointerByReference listRef=new PointerByReference();
        callResult=edsdk.EdsGetCameraList( listRef );
        check(callResult);
        final NativeLongByReference outRef = new NativeLongByReference();
        EdsCameraListRef inRef=new EdsCameraListRef(listRef.getPointer());
        callResult=edsdk.EdsGetChildCount(listRef.getPointer(), outRef );
        check(callResult);
    }
    

}
