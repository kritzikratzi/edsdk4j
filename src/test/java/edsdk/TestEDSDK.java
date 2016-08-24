/**
 * 
 */
package edsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edsdk.api.CanonCamera;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsSaveTo;

/**
 * test the generated EDSDK files
 * 
 * @author wf
 *
 */
public class TestEDSDK extends EDSDKBaseTest {
	static CanonCamera camera;

	@BeforeClass
	public static void openCamera() {
		camera = new CanonCamera();
		if (!camera.openSession()) {
			fail("Couldn't open camera session!");
		}
	}

	@AfterClass
	public static void closeCamera() {
		camera.closeSession();
		CanonCamera.close();

	}

	@Test
	public void testCameraInfo() throws Exception {
		String productName = camera.getProductName();
		assertNotNull(productName);
		System.out.println("Canon Camera: " + productName + " is connected");
	}

	@Test
	public void testPhoto() throws Exception {
		final File[] photos = camera.shoot(EdsSaveTo.kEdsSaveTo_Host);
		assertNotNull(photos);
		if (photos != null) {
			assertEquals(1, photos.length);
			for (final File photo : photos) {
				if (photo != null) {
					System.out.println("Saved photo as: " + photo.getCanonicalPath());
				}
			}
		}

	}

	@Test
	/**
	 * test Live view
	 * @throws InterruptedException
	 */
	public void testLiveView() throws InterruptedException {
        if ( camera.beginLiveView() ) {
            final JFrame frame = new JFrame( "Live view" );
            final JLabel label = new JLabel();
            frame.getContentPane().add( label, BorderLayout.CENTER );
            frame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
            frame.addWindowListener( new WindowAdapter() {

                @Override
                public void windowClosing( final WindowEvent e ) {
                    camera.endLiveView();
                    camera.closeSession();
                    CanonCamera.close();
                    System.exit( 0 );
                }
            } );
            frame.setVisible( true );

            int count=0;
            // do this for one second (20 frames)
            while ( count++<20 ) {
                Thread.sleep( 50 ); // 50 millisecs=20fps 
                final BufferedImage image = camera.downloadLiveView();
                if ( image != null ) {
                    label.setIcon( new ImageIcon( image ) );
                    frame.pack();
                    image.flush();
                }

            }
        }
	}

	@Test
	public void testListImageQualities() {

		// if you check out the CanonCamera class you'll find that there
		// are getAvailableXXX methods for all kinds of things!
		EdsImageQuality[] sizes = camera.getAvailableImageQualities();
		assertNotNull(sizes);
		for (EdsImageQuality size : sizes) {
			System.out.println(size.name() + "/" + size.value());
		}
	}

}
