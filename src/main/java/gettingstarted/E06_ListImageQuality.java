package gettingstarted;

import edsdk.api.CanonCamera;
import edsdk.utils.CanonConstants.EdsImageQuality;

public class E06_ListImageQuality {

	public static void main(String[] args) {
		CanonCamera cam = new CanonCamera(); 
		cam.openSession(); 
		
		// if you check out the CanonCamera class you'll find that there
		// are getAvailableXXX methods for all kinds of things! 
		EdsImageQuality[] sizes = cam.getAvailableImageQualities();  
		for( EdsImageQuality size : sizes ){
			System.out.println( size.name() + "/" + size.value() ); 
		}
		cam.closeSession(); 
		CanonCamera.close(); 
	}
}
