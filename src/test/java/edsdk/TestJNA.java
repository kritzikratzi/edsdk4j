package edsdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import add.AddLibrary;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import edsdk.utils.DLL_Setup;
import edsdk.utils.DLL_Setup.LibraryInfo;

/**
 * test basic JNA see https://de.wikipedia.org/wiki/Java_Native_Access
 * 
 * @author wf
 *
 */
public class TestJNA {

  /**
   * Einfaches Beispiel einer Deklaration und Nutzung einer Dynamischen
   * Programmbibliothek bzw. "shared library".
   */
  public interface CLibrary extends Library {

    CLibrary INSTANCE = (CLibrary) Native.loadLibrary(
        (Platform.isWindows() ? "msvcrt" : "c"), CLibrary.class);

    void printf(String format, Object... args);
  }

  @Test
  public void testJNA() {
    String[] args = { "Welcome", "to", "JNA" };
    CLibrary.INSTANCE.printf("Hello, World\n");
    for (int i = 0; i < args.length; i++) {
      CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);
    }
  }

  // @Ignore - uncomment this if you didn't copy the add.dylib to your $HOME/Library/Frameworks directory
  @Test
  public void testAddLibraryExample() {
    // you could also run this test on Windows but you need to create the DLL then
    if (Platform.isMac()) {
      LibraryInfo libraryInfo = DLL_Setup.initLibrary("add");
      AddLibrary addLibrary = (AddLibrary) Native.loadLibrary(
          libraryInfo.dllLoc, AddLibrary.class);
      int addResult = addLibrary.addNumber(3, 4);
      assertEquals(7, addResult);
    }
  }

}
