# EDSDK4J

This is a Java wrapper around the EDSDK that gives you full access to your Canon SLR camera via the EDSDK on Windows*. The features include: 

- Taking and downloading pictures
- Manually adjusting focus
- Setting apperature, exposure, ISO
- Accessing live view

(*) With a few changes it could also work on Mac OSX. 

## Applying for SDK access
Before you can use this library you need to obtain the EDSDK native library from Canon. You can do so via their developers program: 

- [Canon Europe](http://www.didp.canon-europa.com/)
- [Canon USA](http://www.usa.canon.com/cusa/consumer/standard_display/sdk_homepage)
- [Canon Asia](http://www.canon-asia.com/personal/web/developerresource)
- [Canon Oceania](https://www.canon.co.nz/en-NZ/Personal/Support-Help/Support-News/Canon-SDK)

Once you were granted access - this may take a few days - download the latest version of their library and follow the usage instructions. 

## Architecture 

<img src="arch.png" width="100%" alt="edsdk4j architecture overview" style="max-width:1112 px;">

Using the EDSDK from Java directly is possible using the ```edsdk.native``` package, 
but because of threading difficulties and the involved data types this can be a bit painful. 
In the ```edsdk.utils``` package you find a small layer built on top of the EDSDK 
that gives you more Java-like examples. 

Most notably, all calls have synchroneous and async calls. The async variants give you a CanonTask 
and you can choose if you want to wait for the result or be notified when it's done. 

	CanonCamera slr = new CanonCamera(); 
	slr.openSession();
	
	// Use the blocking variant to get a result immediately. 
	File file = slr.shoot()[0]; 
	System.out.println( "File: " + file.getAbsolutePath() );
	
	// Use async handlers to continue your code immediately 
	slr.shootAsync().whenDone( f -> System.out.println( f ) ); 
	
	// close session is always blocking.
	// because commands are queued this won't be executed 
	// until the above slr.shoot() finished it's work. 
	slr.closeSession(); 


## Getting started

		CanonCamera slr = new CanonCamera(); 
		slr.openSession();
		File file = slr.shoot()[0]; // (*)
		slr.closeSession();
		
		(*) If you have raw+jpeg enabled you'll have two 
		images in that array. Usually it's just one. 

For more look at the examples in src/gettingstarted. 


## Notes

To regenerate the JNA wrapper classes:

	set EDSDK_HOME=path-to-edsdk
	ant generate-wrapper


## Issues:

- Currently JNAerator is not detecting __stdcall correctly so the Callbacks 
  defined in EdSdkLibrary need to be manually modified to extend 
  StdCallCallback.  Technically EdSdkLibrary itself should also extend 
  StdCallLibrary but it's possible to workaround this using options passed
  into the Native.loadLibrary() method (see CanonCamera class for this
  workaround).

 
