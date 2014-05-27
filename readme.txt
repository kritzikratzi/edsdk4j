Notes:

To regenerate the JNA wrapper classes:

set EDSDK_HOME=<path-to-edsdk>
ant generate-wrapper


Outstanding Issues:

- Currently JNAerator 0.11 is not properly handling pointers by 
  reference for descendents of EdsBaseRef (i.e. EdsBaseRef*, which
  is technically a struct**). So EdsBaseRef and it's descendents have
  to be modified and anywhere they are passed by reference should be
  adjusted so that they actually are passed by reference. See the end
  of src/edsdk/EdSdkLibrary.java for the modified objects. Descendency
  has also been reinstated (JNAerator previously tried to make all the
  objects extend PointerType instead of EdsBaseRef, which makes using
  the library more annoying, since the pointers would have to be
  unwrapped and then rewrapped in the appropriate classes). A generic
  object could have be used instead. However, this would not help to
  ensure that the correct pointer is passed to EdSDK. JNAerator also
  does not create a Pointer constructor for Structures, so these have
  to be added manually.
