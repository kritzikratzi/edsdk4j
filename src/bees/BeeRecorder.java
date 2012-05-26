package bees;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import edsdk.utils.CanonCamera;
import edsdk.utils.commands.ShootTask;
import static edsdk.CanonSDK.*; 
import static edsdk.utils.CanonConstants.*; 

public class BeeRecorder {

	public static void main(String[] args) throws InterruptedException {
		CanonCamera camera = new CanonCamera(); 
		camera.openSession(); 
		camera.setProperty( kEdsPropID_Av, Av_10 ); 
		camera.setProperty( kEdsPropID_Tv, Tv_1by100 ); 
		camera.setProperty( kEdsPropID_ISOSpeed, ISO_100 ); 
		
		while( true ){
			System.out.println( "=========================================" ); 
			System.out.println( "Battery Level = " + camera.getProperty( kEdsPropID_BatteryLevel ) ); 
			
			File dest = filename(); 
			dest.getParentFile().mkdirs(); 
			camera.execute( new ShootTask( dest ) ); 
			
			Thread.sleep( 30000 ); 
		}
	}
	
	
	public static File filename(){
		return new File( "images/" + new SimpleDateFormat( "yyyy/MM/dd/HH-mm-ss" ).format( new Date() ) + ".jpg" ); 
	}
}
