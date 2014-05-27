package edsdk.api.commands;

import java.util.Arrays;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsAEMode;
import edsdk.utils.CanonConstant.EdsAFMode;
import edsdk.utils.CanonConstant.EdsAv;
import edsdk.utils.CanonConstant.EdsColorSpace;
import edsdk.utils.CanonConstant.EdsDriveMode;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsExposureCompensation;
import edsdk.utils.CanonConstant.EdsISOSpeed;
import edsdk.utils.CanonConstant.EdsImageQuality;
import edsdk.utils.CanonConstant.EdsMeteringMode;
import edsdk.utils.CanonConstant.EdsPictureStyle;
import edsdk.utils.CanonConstant.EdsPropertyID;
import edsdk.utils.CanonConstant.EdsTv;
import edsdk.utils.CanonConstant.EdsWhiteBalance;
import edsdk.utils.CanonUtils;

// TODO - Should better handle kEdsDataType_Unknown, which seems to be returned
// if the camera doesn't support a property. Could have CanonTask have an
// EdsError field, and if null is returned by the task, the error could be read
// by the user
public class GetPropertyDescCommand extends CanonCommand<EdsPropertyDesc> {

    private final EdsPropertyID property;

    public GetPropertyDescCommand( final EdsPropertyID property ) {
        this.property = property;
    }

    @Override
    public void run() {
        Throwable t = null;
        try {
            final EdsPropertyDesc properties = CanonUtils.getPropertyDesc( (EdsBaseRef) camera.getEdsCamera(), property );
            setResult( properties );
            return;
        }
        catch ( final IllegalArgumentException e ) {
            t = e;
        }
        System.err.println( t.getMessage() );
        setResult( null );
    }

    /*
     * Specific Property ID Tasks
     */

    private static class GetEnumPropertyDescTask<T extends DescriptiveEnum<?>> extends CanonCommand<T[]> {

        private final EdsPropertyID property;
        private final Class<T[]> klass;

        public GetEnumPropertyDescTask( final EdsPropertyID property,
                                        final Class<T[]> klass ) {
            this.property = property;
            this.klass = klass;
        }

        @Override
        public void run() {
            Throwable t = null;
            try {
                final DescriptiveEnum<?>[] properties = CanonUtils.getPropertyDesc( camera.getEdsCamera(), property );
                if ( properties != null ) {
                    setResult( Arrays.copyOf( properties, properties.length, klass ) );
                } else {
                    setResult( null );
                }
                return;
            }
            catch ( final IllegalStateException e ) {
                t = e;
            }
            catch ( final IllegalArgumentException e ) {
                t = e;
            }
            System.err.println( t.getMessage() );
            setResult( null );
        }

    }

    public static class DriveMode extends GetEnumPropertyDescTask<EdsDriveMode> {

        public DriveMode() {
            super( EdsPropertyID.kEdsPropID_DriveMode, EdsDriveMode[].class );
        }

    }

    public static class ISOSpeed extends GetEnumPropertyDescTask<EdsISOSpeed> {

        public ISOSpeed() {
            super( EdsPropertyID.kEdsPropID_ISOSpeed, EdsISOSpeed[].class );
        }

    }

    public static class MeteringMode extends GetEnumPropertyDescTask<EdsMeteringMode> {

        public MeteringMode() {
            super( EdsPropertyID.kEdsPropID_MeteringMode, EdsMeteringMode[].class );
        }

    }

    /**
     * AutoFocusMode = AFMode
     * 
     */
    public static class AutoFocusMode extends GetEnumPropertyDescTask<EdsAFMode> {

        public AutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_AFMode, EdsAFMode[].class );
        }

    }

    /**
     * ApertureValue = Av
     * 
     */
    public static class ApertureValue extends GetEnumPropertyDescTask<EdsAv> {

        public ApertureValue() {
            super( EdsPropertyID.kEdsPropID_Av, EdsAv[].class );
        }

    }

    /**
     * ShutterSpeed = Tv
     * 
     */
    public static class ShutterSpeed extends GetEnumPropertyDescTask<EdsTv> {

        public ShutterSpeed() {
            super( EdsPropertyID.kEdsPropID_Tv, EdsTv[].class );
        }

    }

    public static class ExposureCompensation extends GetEnumPropertyDescTask<EdsExposureCompensation> {

        public ExposureCompensation() {
            super( EdsPropertyID.kEdsPropID_ExposureCompensation, EdsExposureCompensation[].class );
        }

    }

    /**
     * ShootingMode = AEMode
     * 
     */
    public static class ShootingMode extends GetEnumPropertyDescTask<EdsAEMode> {

        public ShootingMode() {
            super( EdsPropertyID.kEdsPropID_AEMode, EdsAEMode[].class );
        }

    }

    public static class ImageQuality extends GetEnumPropertyDescTask<EdsImageQuality> {

        public ImageQuality() {
            super( EdsPropertyID.kEdsPropID_ImageQuality, EdsImageQuality[].class );
        }

    }

    public static class WhiteBalance extends GetEnumPropertyDescTask<EdsWhiteBalance> {

        public WhiteBalance() {
            super( EdsPropertyID.kEdsPropID_WhiteBalance, EdsWhiteBalance[].class );
        }

    }

    public static class ColorSpace extends GetEnumPropertyDescTask<EdsColorSpace> {

        public ColorSpace() {
            super( EdsPropertyID.kEdsPropID_ColorSpace, EdsColorSpace[].class );
        }

    }

    public static class PictureStyle extends GetEnumPropertyDescTask<EdsPictureStyle> {

        public PictureStyle() {
            super( EdsPropertyID.kEdsPropID_PictureStyle, EdsPictureStyle[].class );
        }

    }

    /**
     * LiveViewWhiteBalance = Evf_WhiteBalance
     * 
     */
    public static class LiveViewWhiteBalance extends GetEnumPropertyDescTask<EdsWhiteBalance> {

        public LiveViewWhiteBalance() {
            super( EdsPropertyID.kEdsPropID_Evf_WhiteBalance, EdsWhiteBalance[].class );
        }

    }

    /**
     * LiveViewAutoFocusMode = Evf_AFMode
     * 
     */
    public static class LiveViewAutoFocusMode extends GetEnumPropertyDescTask<EdsEvfAFMode> {

        public LiveViewAutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_Evf_AFMode, EdsEvfAFMode[].class );
        }

    }

}
