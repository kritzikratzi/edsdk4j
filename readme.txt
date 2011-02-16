Notes:

To regenerate the JNA wrapper classes:

set EDSDK_HOME=<path-to-edsdk>
ant generate-wrapper


Outstanding Issues:

- Currently JNAerator is not detecting __stdcall correctly so the Callbacks 
  defined in EdSdkLibrary need to be manually modified to extend 
  StdCallCallback.  Technically EdSdkLibrary itself should also extend 
  StdCallLibrary but it's possible to workaround this using options passed
  into the Native.loadLibrary() method (see CanonCamera class for this
  workaround).

 
