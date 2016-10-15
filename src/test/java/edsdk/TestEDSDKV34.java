package edsdk;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import edsdk.api.Version;
import edsdk.bindingsv34.EdSdkLibrary;
import edsdk.utils.DLL_Setup;
import edsdk.utils.DLL_Setup.LibraryInfo;

public class TestEDSDKV34 {

  @Test
//  @Ignore
  public void testv34Bindings() {
    if (Platform.isMac()) {
      final Map<String, Integer> options = new LinkedHashMap<String, Integer>();
      LibraryInfo libraryInfo = DLL_Setup.initLibrary("EdSdk");
      options.put( Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION );          
      EdSdkLibrary EDSDK = (EdSdkLibrary) Native.loadLibrary( libraryInfo.dllLoc, EdSdkLibrary.class, options );
      EDSDK.EdsInitializeSDK();
    }
  }
  
  @Test 
  public void testLoadversion(){
	  String filePath = "EDSDK_64\\DLL\\EDSDK.dll";

	  int[] v=Version.getDllVersion(filePath);

	  /**
	   * This checks the dll version to be 
	   * EDSDK3.5.0
	   * 09/07/2016
	   * 
	   * Adjust it if you have a different version in use.
	   * 
	   */
	  assertEquals(3,v[0]);
	  assertEquals(5,v[1]);
	  assertEquals(0,v[2]);
	  assertEquals(6404,v[3]);
	  
      System.out.println(
              String.valueOf(v[0]) + "." +
                      String.valueOf(v[1]) + "." +
                      String.valueOf(v[2]) + "." +
                      String.valueOf(v[3]));
  }

}
