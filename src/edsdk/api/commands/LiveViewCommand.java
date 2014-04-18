package edsdk.api.commands;

import java.awt.image.BufferedImage;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonUtils;

public class LiveViewCommand {

	public static class Begin extends CanonCommand<Boolean>{
		@Override
		public void run() {
			setResult( CanonUtils.beginLiveView( camera.getEdsCamera() ) ); 
		}
	}
	
	
	public static class End extends CanonCommand<Boolean>{
		@Override
		public void run() {
			setResult( CanonUtils.endLiveView( camera.getEdsCamera() ) ); 
		}
	}
	
	public static class Download extends CanonCommand<BufferedImage>{
		@Override
		public void run() {
			setResult( CanonUtils.downloadLiveViewImage( camera.getEdsCamera() ) ); 
		}
		
	}
}
