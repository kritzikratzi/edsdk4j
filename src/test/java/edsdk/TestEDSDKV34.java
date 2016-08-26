package edsdk;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import edsdk.bindingsv34.EdSdkLibrary;
import edsdk.utils.DLL_Setup;
import edsdk.utils.DLL_Setup.LibraryInfo;

public class TestEDSDKV34 {

  //@Test
  @Ignore
  public void testv34Bindings() {
    if (Platform.isMac()) {
      final Map<String, Integer> options = new LinkedHashMap<String, Integer>();
      LibraryInfo libraryInfo = DLL_Setup.initLibrary("EdSdk");
      options.put( Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION );          
      EdSdkLibrary EDSDK = (EdSdkLibrary) Native.loadLibrary( libraryInfo.dllLoc, EdSdkLibrary.class, options );
      EDSDK.EdsInitializeSDK();
    }
  }

}
