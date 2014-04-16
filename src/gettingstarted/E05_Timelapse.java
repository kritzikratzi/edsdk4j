package gettingstarted;

import static edsdk.EdSdkLibrary.kEdsPropID_Av;
import static edsdk.EdSdkLibrary.kEdsPropID_BatteryLevel;
import static edsdk.EdSdkLibrary.kEdsPropID_ISOSpeed;
import static edsdk.EdSdkLibrary.kEdsPropID_Tv;
import static edsdk.utils.CanonConstants.Av_7_1;
import static edsdk.utils.CanonConstants.ISO_800;
import static edsdk.utils.CanonConstants.Tv_1by100;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edsdk.utils.CanonCamera;
import edsdk.utils.CanonConstants;
import edsdk.utils.commands.ShootTask;

public class E05_Timelapse {

	public static void main(String[] args) throws InterruptedException {
		//Native.setProtected( true ); 
		CanonCamera camera = new CanonCamera();
		camera.openSession(); 
		camera.setProperty( kEdsPropID_Av, Av_7_1 ); 
		camera.setProperty( kEdsPropID_Tv, Tv_1by100 ); 
		camera.setProperty( kEdsPropID_ISOSpeed, ISO_800 ); 

		createUI( camera ); 
		
		while( true ){
			System.out.println( "=========================================" ); 
			System.out.println( "Battery Level = " + camera.getProperty( kEdsPropID_BatteryLevel ) ); 
			camera.execute( new ShootTask( filename() ) ); 

			try {
				Thread.sleep( 15000 );
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		

	}
	
	public static File filename(){
		return new File( "images\\" + new SimpleDateFormat( "yyyy\\MM\\dd\\HH-mm-ss" ).format( new Date() ) + ".jpg" ); 
	}
	
	
	
	private static void createUI( final CanonCamera camera ){
		JFrame frame = new JFrame(); 
		JPanel content = new JPanel( new GridBagLayout() ); 
		content.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ); 
		GridBagConstraints gbc = new GridBagConstraints(); 
		
		gbc.anchor = GridBagConstraints.EAST; 
		gbc.fill = GridBagConstraints.HORIZONTAL; 
		gbc.insets = new Insets( 3, 3, 3, 3 ); 
		gbc.gridy = 1; 
		
		addCombobox( content, gbc, "Shutter Speed", "Tv_", new Callback(){
			public void call( int value ){
				camera.setProperty( kEdsPropID_Tv, value ); 
			}
		}); 
		addCombobox( content, gbc, "Aperature", "Av_", new Callback(){
			public void call( int value ){
				camera.setProperty( kEdsPropID_Av, value ); 
			}
		});
		addCombobox( content, gbc, "Iso", "ISO_", new Callback(){
			public void call( int value ){
				camera.setProperty( kEdsPropID_ISOSpeed, value ); 
			}
		});

		
		
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
		frame.setContentPane( content ); 
		frame.setSize( 500, 400 ); 
		frame.setVisible( true ); 
	}
	
	
	private static void addCombobox( JPanel content, GridBagConstraints gbc, String label, String prefix, final Callback callback ){
		gbc.gridx = 0; 
		gbc.weightx = 0; 
		content.add( new JLabel( label ), gbc ); 
		
		gbc.gridx = 1; 
		gbc.weightx = 1; 
		// find the items ... 
	
		LinkedList<String> items = new LinkedList<String>();
		for( Field field : CanonConstants.class.getDeclaredFields() ){
			if( field.getName().startsWith( prefix ) ){
				items.add( field.getName() ); 
			}
		}
		
		final JComboBox<String> combo = new JComboBox<String>( items.toArray( new String[]{} ) );
		combo.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed( ActionEvent event ) {
				try {
					int value = CanonConstants.class.getDeclaredField( combo.getSelectedItem().toString() ).getInt( null );
					callback.call( value ); 
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}); 
		gbc.gridx = 1; 
		gbc.weightx = 1; 
		content.add( combo, gbc ); 
		
		gbc.gridy ++; 
	}
	
	interface Callback{
		public void call( int value ); 
	}
}
