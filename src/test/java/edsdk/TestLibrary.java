package edsdk;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.sun.jna.Platform;

import edsdk.api.BaseCanonCamera;

/**
 * test whether the Library is available
 * @author wf
 *
 */
public class TestLibrary {

  @Test
  public void testLibrary() {
    BaseCanonCamera.initLibrary();
    File libFile = BaseCanonCamera.getEdSdkLibraryFile();
    assertTrue(BaseCanonCamera.edsdkHint+libFile.getAbsolutePath()+" should exist",libFile.exists());
    if (Platform.isWindows()) {
      PowerShellResponse response = PowerShell.executeSingleCommand("(Get-Item "+libFile.getAbsolutePath()+").VersionInfo");
    }
  }

}
