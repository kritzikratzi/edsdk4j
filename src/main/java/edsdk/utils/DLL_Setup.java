package edsdk.utils;

import java.io.File;
import java.net.URI;
import java.net.URL;

import com.sun.jna.Platform;

import edsdk.api.BaseCanonCamera;

/**
 * Library Helper class
 * @author wf
 *
 */
public class DLL_Setup {
  public static boolean debug=false;

  /**
   * set the jna.library.path 
   */
  public static void setJNALibraryPath() {
    URL url = null;
    Class<BaseCanonCamera> clazz = BaseCanonCamera.class;

    try {
      url = clazz.getProtectionDomain().getCodeSource().getLocation();
    } catch (final Exception e) {
      url = null;
    }
    if (url == null) {
      try {
        url = clazz.getResource(clazz.getSimpleName() + ".class");
        url = new URL(url.getPath().substring(0, url.getPath().indexOf('!')));
      } catch (final Exception e) {
        url = null;
      }
    }
    if (url != null) {
      try {
        // handle unc paths (pre java7 :/ )
        URI uri = url.toURI();
        if (uri.getAuthority() != null && uri.getAuthority().length() > 0) {
          uri = new URL("file://" + url.toString().substring(5)).toURI();
        }
        final File file = new File(uri);
        final String dir = file.getParentFile().getPath();
        System.setProperty("jna.library.path", dir);
        if (debug) {
          System.out.println("jna.library.path: " + dir);
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * structure to hold LibraryInformation
   * @author wf
   *
   */
  public static class LibraryInfo {
    public String libName;
    public String libpath;
    public String dllLoc;
    public String hint;
    public File libFile;
    /**
     * create this Library
     * @param libName
     */
    public LibraryInfo(String libName) {
      this.libName=libName;
    }
  }
  
  /**
   * initialize the DDL Library
   * @param libName
   * @return the LibraryInfo for this library
   */
  public static LibraryInfo initLibrary(String libName) {
    LibraryInfo result=new LibraryInfo(libName);
    setJNALibraryPath();
    String libpath;
    String arch = System.getProperty("os.arch");
    if (arch == null) {
      arch = System.getProperty("com.ibm.vm.bitmode");
    }

    // let's find the EDSDK native libraries
    // first we need to determine the operating system being in use
    if (Platform.isMac()) {
      // see
      // stackoverflow.com/questions/15695786/how-to-define-paths-to-frameworks-on-a-mac-in-java
      // make the library available in your home directory e.g. with:
      // cd $HOME/Library/Frameworks
      // ln -s /Applications/Canon\ Utilities/EOS\ Utility/EU2/EOS\ Utility\
      // 2.app/Contents/Frameworks/EDSDK.framework/Versions/Current/EDSDK EDSDK
      // check the content e.g. with
      // nm EDSDK
      //
      libpath = System.getProperty("user.home") + "/Library/Frameworks/";
      System.setProperty("jna.library.path", libpath);
      result.dllLoc = libpath + libName;
      result.libFile = new File(result.dllLoc);
      if (!(result.libFile.exists()) && !libName.endsWith(".dylib")) {
        result.dllLoc=result.dllLoc+".dylib";
      }
      result.hint = libName+" Dynamic Link Library from ";
    } else {
      libName=libName.toUpperCase();
      result.hint= libName+" DLL";
      if (arch != null && arch.endsWith("64")) {
        // e.g. EDSDK_64/EDSDK.dll
        result.dllLoc = libName+"_64/"+libName+".dll";
      } else {
        // e.g. EDSDK/Dll/EDSDK.dll
        result.dllLoc = libName+"/Dll/"+libName+".dll";
      }
    }
    result.libFile = new File(result.dllLoc);
    if (debug)
    System.err.println("Java Architecture: " + arch + " - Using " + result.hint
        + ": " + result.dllLoc);
    return result;
  }

}
