package gettingstarted;

import edsdk.SLR;

/** Simple example of JNA interface mapping and usage. */
public class Simpler {
	public static void main(String[] args) throws InterruptedException {
		SLR slr = new SLR(); 
		slr.openSession();
		slr.shoot(); 
		slr.closeSession();
	}	
}