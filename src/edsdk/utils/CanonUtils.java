package edsdk.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import edsdk.CanonSDK;
import edsdk.CanonSDK.__EdsObject;
import edsdk.EdsDirectoryItemInfo;

/**
 * Here are some great helpers. 
 * _All_ the functions in here are not thread save, so you'll want to encapsulate them in 
 * a CanonTask and then send them to the camera, like so for instance : 
 * 
 * 
 * canonCamera.executeNow( new CanonTask<Boolean>(){
 * 	 public void run(){
 * 		CanonUtils.doSomethingLikeDownloadOrWhatever(); 
 *   }
 * }
 * 
 * @author hansi
 *
 */
public class CanonUtils {
	/**
	 * Converts a bunch of bytes to a string. 
	 * This is a little different from new String( myBytes ) because
	 * byte-arrays received from C will be crazy long and just have a null-terminator
	 * somewhere in the middle. 
	 */
	public static String toString( byte bytes[] ){
		for( int i = 0; i < bytes.length; i++ ){
			System.out.println( "bytes[" + i + "] = " + bytes[i] ); 
			if( bytes[i] == 0 ){
				return new String( bytes, 0, i ); 
			}
		}
		
		return new String( bytes ); 
	}
	
	/**
	 * Tries to find name of an error code. 
	 * 
	 * @param errorCode
	 * @return
	 */
	public static String toString( int errorCode ){
		Field[] fields = CanonSDK.class.getFields();
		for( Field field : fields ){
			try {
				if( field.getType().toString().equals( "int" ) && field.getInt( CanonSDK.class ) == errorCode ){
					return field.getName(); 
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return "unknown error code"; 
	}
	
	
	/**
	 * Downloads an image and saves it somewhere
	 * @param directoryItem The item you want to download
	 * @param destination A path in the filesystem where you want to save the file. Can also be null or a directory. In case of null the temp directory will be used, in case of a directory the file name of the item will be used. 
	 * @param deleteAfterDownload Should the image be deleted right after successful download  
	 * @return Either null, or the location the file was ultimately saved to on success. 
	 */
	public static File download( __EdsObject directoryItem, File destination, boolean deleteAfterDownload ){
		int err = CanonSDK.EDS_ERR_OK;
		__EdsObject[] stream = new __EdsObject[1]; 
		EdsDirectoryItemInfo dirItemInfo = new EdsDirectoryItemInfo();

		boolean success = false;

		long timeStart = System.currentTimeMillis();

		err = CanonSDK.INSTANCE.EdsGetDirectoryItemInfo(directoryItem, dirItemInfo);
		if (err == CanonSDK.EDS_ERR_OK) {
			if( destination == null ){
				destination = new File( System.getProperty("java.io.tmpdir") ); 
			}
			if (destination.isDirectory()) {
				destination = new File(destination, toString( dirItemInfo.szFileName ) );
			}
			
			System.out.println("Downloading image "
					+ toString(dirItemInfo.szFileName) + " to "
					+ destination.getAbsolutePath());

			err = CanonSDK.INSTANCE.EdsCreateFileStream(
					ByteBuffer.wrap( destination.getAbsolutePath().getBytes() ), 
					CanonSDK.EdsFileCreateDisposition.kEdsFileCreateDisposition_CreateAlways,
					CanonSDK.EdsAccess.kEdsAccess_ReadWrite, 
					stream
			);
		}

		if(err == CanonSDK.EDS_ERR_OK ){
			err = CanonSDK.INSTANCE.EdsDownload( directoryItem, dirItemInfo.size, stream[0] );
		}

		if( err == CanonSDK.EDS_ERR_OK ){
			System.out.println( "Image downloaded in " +  ( System.currentTimeMillis() - timeStart ) );

			err = CanonSDK.INSTANCE.EdsDownloadComplete( directoryItem );
			if( deleteAfterDownload ){
				System.out.println( "Image deleted" );
				CanonSDK.INSTANCE.EdsDeleteDirectoryItem( directoryItem );
			}
			
			success = true;
		}
		
		if( stream[0] != null ){
			CanonSDK.INSTANCE.EdsRelease( stream[0] ); 
		}
		
		return success? destination : null;
	}
}
