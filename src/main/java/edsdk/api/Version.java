package edsdk.api;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class Version {

	/**
	 * compute dll version
	 * 
	 * @param filePath
	 * @return
	 */
	public static int[] getDllVersion(String filePath){
		int[] result=new int[4];
		 IntByReference dwDummy = new IntByReference();
	      dwDummy.setValue(0);

	      int versionlength =
	              com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfoSize(
	                      filePath, dwDummy);

	      byte[] bufferarray = new byte[versionlength];
	      Pointer lpData = new Memory(bufferarray.length);
	      PointerByReference lplpBuffer = new PointerByReference();
	      IntByReference puLen = new IntByReference();

	      boolean fileInfoResult =
	              com.sun.jna.platform.win32.Version.INSTANCE.GetFileVersionInfo(
	                      filePath, 0, versionlength, lpData);

	      boolean verQueryVal =
	              com.sun.jna.platform.win32.Version.INSTANCE.VerQueryValue(
	                      lpData, "\\", lplpBuffer, puLen);

	      VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
	      lplpBufStructure.read();

	      result[0] = (lplpBufStructure.dwFileVersionMS).intValue() >> 16;
	      result[1] = (lplpBufStructure.dwFileVersionMS).intValue() & 0xffff;
	      result[2]= (lplpBufStructure.dwFileVersionLS).intValue() >> 16;
	      result[3]= (lplpBufStructure.dwFileVersionLS).intValue() & 0xffff;
		return result;
	}
}
