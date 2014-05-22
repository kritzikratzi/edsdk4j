package gettingstarted;

import java.nio.ByteBuffer;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;

import edsdk.api.CanonCamera;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.__EdsObject;
import edsdk.bindings.EdsSaveImageSetting;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonUtils;

/**
 * This example uses the EdsImage library to convert a raw .cr2 file to a .jpeg file
 *  
 * @author hansi
 *
 */
public class E06_ConvertRaw {

	public static void main(String[] args) {
		// let the CanonCamera class do the work of finding the Dll 
		// and starting the dispatch thread
		CanonCamera cam = new CanonCamera();
		EdSdkLibrary EDSDK = CanonCamera.EDSDK;  
		//cam.beginDirect();  
		
		__EdsObject inStream[] = new __EdsObject[1];
		ByteBuffer name = ByteBuffer.allocate(100);
		name.put(Native.toByteArray("Test.CR2"));
		EDSDK.EdsCreateFileStream(name, EdSdkLibrary.EdsFileCreateDisposition.kEdsFileCreateDisposition_OpenExisting, EdSdkLibrary.EdsAccess.kEdsAccess_Read, inStream);

		__EdsObject imgRef[] = new __EdsObject[1];
		EDSDK.EdsCreateImageRef(inStream[0], imgRef);

		__EdsObject outStream[] = new __EdsObject[1];
		name = ByteBuffer.allocate(100);
		name.put(Native.toByteArray("output.jpg"));
		EDSDK.EdsCreateFileStream(name, EdSdkLibrary.EdsFileCreateDisposition.kEdsFileCreateDisposition_CreateAlways, EdSdkLibrary.EdsAccess.kEdsAccess_Write, outStream);

		EdsSaveImageSetting.ByValue set = new EdsSaveImageSetting.ByValue();
		set.JPEGQuality = new NativeLong(8);
		set.iccProfileStream = null;
		NativeLong err = EDSDK.EdsSaveImage(imgRef[0], EdSdkLibrary.EdsTargetImageType.kEdsTargetImageType_Jpeg, set, outStream[0]);
		System.out.printf("Save image error = 0x%x %s\n", err.intValue(), CanonUtils.toString( err.intValue() ) );
		
		//cam.endDirect(); 
	}
}
