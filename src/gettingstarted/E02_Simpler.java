package gettingstarted;

import java.io.File;

import edsdk.api.CanonCamera;

/** Simple example of JNA interface mapping and usage. */
public class E02_Simpler {
	public static void main(String[] args) throws InterruptedException {
		CanonCamera slr = new CanonCamera(); 
		slr.openSession();
		File file = slr.shoot().get();
		System.out.println( "Saved file as: " + file.getAbsolutePath() ); 
		slr.closeSession();
		
		CanonCamera.close(); 
	}
}