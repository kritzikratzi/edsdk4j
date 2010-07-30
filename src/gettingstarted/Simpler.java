package gettingstarted;

import edsdk.utils.CanonCamera;

/** Simple example of JNA interface mapping and usage. */
public class Simpler {
	public static void main(String[] args) throws InterruptedException {
		CanonCamera slr = new CanonCamera(); 
		slr.openSession();
		slr.shoot(); 
		slr.closeSession();
		
		CanonCamera.close(); 
	}
}