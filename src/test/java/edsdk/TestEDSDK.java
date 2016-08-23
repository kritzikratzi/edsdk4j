/**
 * 
 */
package edsdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

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
		String productName=camera.getProductName();
		assertNotNull(productName);
		System.out.println("Canon Camera: "+productName+" is connected");
	}

	@Test
	public void testPhoto() throws Exception {
		final File[] photos = camera.shoot(EdsSaveTo.kEdsSaveTo_Host);
		assertNotNull(photos);
		if (photos != null) {
			assertEquals(1,photos.length);
			for (final File photo : photos) {
				if (photo != null) {
					System.out.println("Saved photo as: " + photo.getCanonicalPath());
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
