package edsdk;

import static org.junit.Assert.fail;

import com.sun.jna.NativeLong;

import edsdk.bindings.EdSdkLibrary;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonUtils;

/**
 * base Class for Testing Canon EDSDK
 * 
 * @author wf
 *
 */
public class EDSDKBaseTest {
  boolean debug = true;

  /**
   * check the given longResult
   * 
   * @param longResult
   */
  public static void check(NativeLong longResult) {
    int result = longResult.intValue();
    if (result != EdSdkLibrary.EDS_ERR_OK) {
      final EdsError err = CanonUtils.toEdsError(result);
      fail("Error " + err.value() + ": " + err.name() + " - "
          + err.description());
    }
  }

}
