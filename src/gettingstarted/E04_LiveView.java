package gettingstarted;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edsdk.utils.CanonCamera;

/**
 * A live view example
 * 
 * @author hansi
 *
 */
public class E04_LiveView {
	public static void main(String[] args) throws InterruptedException {
		final CanonCamera cam = new CanonCamera(); 
		cam.openSession(); 
		cam.beginLiveView(); 
		
		JFrame frame = new JFrame( "Live view" ); 
		JLabel label = new JLabel(); 
		frame.getContentPane().add( label, BorderLayout.CENTER ); 
		frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE ); 
		frame.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cam.endLiveView(); 
				cam.closeSession(); 
				CanonCamera.close(); 
				System.exit( 0 ); 
			} 
		}); 
		frame.setVisible( true ); 
		
		while( true ){
			Thread.sleep( 50 ); 
			BufferedImage image = cam.downloadLiveView(); 
			if( image != null ){
				label.setIcon( new ImageIcon( image ) ); 
				frame.pack(); 
				image.flush(); 
			}
		}
	}
}
