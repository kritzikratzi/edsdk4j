package edsdk.utils.commands;

import java.awt.image.BufferedImage;

import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

// TODO - probably a bad idea that Begin/End/Download are static
public class LiveViewTask {

    public static class Begin extends CanonTask<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.beginLiveView( camera.getEdsCamera() ) );
        }
    }

    public static class End extends CanonTask<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.endLiveView( camera.getEdsCamera() ) );
        }
    }

    public static class Download extends CanonTask<BufferedImage> {

        @Override
        public void run() {
            setResult( CanonUtils.downloadLiveViewImage( camera.getEdsCamera() ) );
        }
    }

    public static class IsLiveViewEnabled extends CanonTask<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), false ) );
        }
    }

    public static class IsLiveViewActive extends CanonTask<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), true ) );
        }
    }

}
