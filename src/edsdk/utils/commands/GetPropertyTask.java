package edsdk.utils.commands;

import java.lang.reflect.Array;

import com.sun.jna.NativeLong;

import edsdk.EdSdkLibrary.EdsBaseRef;
import edsdk.EdsFocusInfo;
import edsdk.EdsPictureStyleDesc;
import edsdk.EdsPoint;
import edsdk.EdsRect;
import edsdk.EdsSize;
import edsdk.EdsTime;
import edsdk.utils.CanonConstant;
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsAEMode;
import edsdk.utils.CanonConstant.EdsAFMode;
import edsdk.utils.CanonConstant.EdsAv;
import edsdk.utils.CanonConstant.EdsBatteryQuality;
import edsdk.utils.CanonConstant.EdsBracket;
import edsdk.utils.CanonConstant.EdsColorSpace;
import edsdk.utils.CanonConstant.EdsCustomFunction;
import edsdk.utils.CanonConstant.EdsDataType;
import edsdk.utils.CanonConstant.EdsDriveMode;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsEvfHistogramStatus;
import edsdk.utils.CanonConstant.EdsEvfOutputDevice;
import edsdk.utils.CanonConstant.EdsEvfZoom;
import edsdk.utils.CanonConstant.EdsExposureCompensation;
import edsdk.utils.CanonConstant.EdsISOSpeed;
import edsdk.utils.CanonConstant.EdsImageQuality;
import edsdk.utils.CanonConstant.EdsMeteringMode;
import edsdk.utils.CanonConstant.EdsPictureStyle;
import edsdk.utils.CanonConstant.EdsPropertyID;
import edsdk.utils.CanonConstant.EdsSaveTo;
import edsdk.utils.CanonConstant.EdsTv;
import edsdk.utils.CanonConstant.EdsWhiteBalance;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

// TODO - These are defined in EdSdkLibrary but are not described in the API
// Docs:
// kEdsPropID_DepthOfField (EdsUInt32),
// kEdsPropID_EFCompensation (??),
// kEdsPropID_Evf_FocusAid (??),
// kEdsPropID_MyMenu (kEdsDataType_UInt32_Array - EdsUInt32[])
//
// TODO - Should better handle kEdsDataType_Unknown, which seems to be returned
// if the camera doesn't support a property. Could have CanonTask have an
// EdsError field, and if null is returned by the task, the error could be read
// by the user
//
// If return type T differs from data type for property (for instance,
// conversion for EdsUInt32 to a CanonConstant enum), the Class<T> must be
// provided by the constructor
public abstract class GetPropertyTask<T> extends CanonTask<T> {

    private final EdsPropertyID property;
    private final long param;
    private final Class<T> klass;
    private final boolean isLiveViewTask;
    private final int liveViewRetryCount = 2;

    public GetPropertyTask( final EdsPropertyID property ) {
        this( property, 0, null, false );
    }

    public GetPropertyTask( final EdsPropertyID property, final long param ) {
        this( property, param, null, false );
    }

    public GetPropertyTask( final EdsPropertyID property,
                            final boolean isLiveViewTask ) {
        this( property, 0, null, isLiveViewTask );
    }

    public GetPropertyTask( final EdsPropertyID property, final long param,
                            final boolean isLiveViewTask ) {
        this( property, param, null, isLiveViewTask );
    }

    public GetPropertyTask( final EdsPropertyID property, final Class<T> klass ) {
        this( property, 0, klass, false );
    }

    public GetPropertyTask( final EdsPropertyID property, final long param,
                            final Class<T> klass ) {
        this( property, param, klass, false );
    }

    public GetPropertyTask( final EdsPropertyID property, final Class<T> klass,
                            final boolean isLiveViewTask ) {
        this( property, 0, klass, isLiveViewTask );
    }

    public GetPropertyTask( final EdsPropertyID property, final long param,
                            final Class<T> klass, final boolean isLiveViewTask ) {
        this.property = property;
        this.param = param;
        this.klass = klass;
        this.isLiveViewTask = isLiveViewTask;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void run() {
        Throwable t = null;
        EdsBaseRef.ByReference[] references = null;
        try {
            final EdsBaseRef baseRef;
            if ( isLiveViewTask ) {
                if ( CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), false ) ) {
                    for ( int i = 0; i < liveViewRetryCount &&
                                     references == null; i++ ) {
                        if ( i > 0 ) {
                            Thread.sleep( 100 );
                        }
                        references = CanonUtils.getLiveViewImageReference( camera.getEdsCamera() );
                    }
                    if ( references != null ) {
                        baseRef = references[0].getValue();
                    } else {
                        //TODO - it may take several seconds for live view to start, so this might happen every time.. perhaps the previous should be tried for a few seconds
                        //throw new IllegalStateException( "Could not retrieve live view image reference!" );
                        System.err.println( "Could not retrieve live view image reference!" );
                        setResult( null );
                        return;
                    }
                } else {
                    //throw new IllegalStateException( "Live view is not enabled!" );
                    System.err.println( "Live view is not enabled!" );
                    setResult( null );
                    return;
                }
            } else {
                baseRef = camera.getEdsCamera();
            }

            final EdsDataType type = CanonUtils.getPropertyType( baseRef, property, param );

            T result = null;
            switch ( type ) {
                case kEdsDataType_Int32: //EdsInt32
                case kEdsDataType_UInt32: { //EdsUInt32
                    final Long data = CanonUtils.getPropertyData( baseRef, property, param );

                    if ( data != null ) {
                        if ( klass != null &&
                             Boolean.class.isAssignableFrom( klass ) ) {
                            // Boolean
                            result = (T) Boolean.valueOf( data == 1l );
                        } else if ( klass != null &&
                                    DescriptiveEnum.class.isAssignableFrom( klass ) ) {
                            // DescriptiveEnum
                            result = (T) CanonConstant.enumOfValue( (Class<? extends DescriptiveEnum<?>>) klass, data.intValue() );
                        } else {
                            // Long
                            result = (T) Long.valueOf( data );
                        }
                    }

                    break;
                }
                case kEdsDataType_String: { //EdsChar[]
                    final String data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_Point: { //EdsPoint
                    final EdsPoint data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_Rect: { //EdsRect
                    final EdsRect data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_Time: { //EdsTime
                    final EdsTime data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_FocusInfo: { //EdsFocusInfo
                    final EdsFocusInfo data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_PictureStyleDesc: { //EdsPictureStyleDesc
                    final EdsPictureStyleDesc data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );
                    result = (T) data;
                    break;
                }
                case kEdsDataType_ByteBlock: //EdsUInt32[]
                case kEdsDataType_Int32_Array: //EdsInt32[]
                case kEdsDataType_UInt32_Array: { //EdsUInt32[]
                    final int[] data = CanonUtils.getPropertyDataAdvanced( baseRef, property, param );

                    if ( data != null ) {
                        if ( klass != null &&
                             DescriptiveEnum[].class.isAssignableFrom( klass ) ) {
                            // DescriptiveEnum[]
                            final DescriptiveEnum<?>[] array = (DescriptiveEnum<?>[]) Array.newInstance( klass.getComponentType(), data.length );
                            for ( int i = 0; i < data.length; i++ ) {
                                array[i] = CanonConstant.enumOfValue( (Class<? extends DescriptiveEnum<?>>) klass.getComponentType(), data[i] );
                            }
                            result = (T) array;
                        } else if ( klass != null &&
                                    DescriptiveEnum.class.isAssignableFrom( klass ) ) {
                            // DescriptiveEnum
                            if ( data.length > 1 ) {
                                throw new IllegalStateException( "Only single result expected but multiple results returned!" );
                            }
                            result = (T) CanonConstant.enumOfValue( (Class<? extends DescriptiveEnum<?>>) klass, data[0] );
                        } else if ( klass != null &&
                                    EdsRect.class.isAssignableFrom( klass ) ) {
                            // EdsRect
                            if ( data.length != 4 ) {
                                throw new IllegalStateException( "Four values expected for an EdsRect!" );
                            }
                            result = (T) new EdsRect( new EdsPoint( new NativeLong( data[0] ), new NativeLong( data[1] ) ), new EdsSize( new NativeLong( data[2] ), new NativeLong( data[3] ) ) );
                        } else if ( klass != null &&
                                    EdsSize.class.isAssignableFrom( klass ) ) {
                            // EdsSize
                            if ( data.length != 2 ) {
                                throw new IllegalStateException( "Two values expected for an EdsSize!" );
                            }
                            result = (T) new EdsSize( new NativeLong( data[0] ), new NativeLong( data[1] ) );
                        } else {
                            // int[]
                            result = (T) data;
                        }
                    }

                    break;
                }
                default:
                    System.err.println( type.description() +
                                        " (" +
                                        type.name() +
                                        ") is not currently supported by GetPropertyTask. Likely this camera does not support property " +
                                        property.name() +
                                        " in the current mode or at all." );

                    //                    throw new IllegalStateException( type.description() + " (" +
                    //                                                     type.name() +
                    //                                                     ") is not currently supported by GetPropertyTask. Likely this camera does not support property " + property.name() + " in the current mode or at all." );
            }

            setResult( result );
            return;
        }
        catch ( final IllegalArgumentException e ) {
            t = e;
        }
        catch ( final InterruptedException e ) {
            t = e;
        }
        finally {
            if ( references != null ) {
                CanonUtils.release( references );
            }
        }
        System.err.println( t.getMessage() );
        setResult( null );
    }

    public static class Data extends GetPropertyTask<Long> {

        public Data( final EdsPropertyID property, final boolean isLiveViewTask ) {
            super( property, true );
        }

        public Data( final EdsPropertyID property ) {
            super( property );
        }

    }

    public static class Size extends CanonTask<Long> {

        private final EdsPropertyID property;

        public Size( final EdsPropertyID property ) {
            this.property = property;
        }

        @Override
        public void run() {
            setResult( CanonUtils.getPropertySize( camera.getEdsCamera(), property ) );

        }

    }

    public static class Type extends CanonTask<EdsDataType> {

        private final EdsPropertyID property;

        public Type( final EdsPropertyID property ) {
            this.property = property;
        }

        @Override
        public void run() {
            setResult( CanonUtils.getPropertyType( camera.getEdsCamera(), property ) );

        }

    }

    /*
     * Specific Property ID Tasks
     */

    public static class CustomFunction extends GetPropertyTask<Long> {

        public CustomFunction( final EdsCustomFunction customFunction ) {
            super( EdsPropertyID.kEdsPropID_CFn, customFunction.value() );
        }

    }

    public static class ProductName extends GetPropertyTask<String> {

        public ProductName() {
            super( EdsPropertyID.kEdsPropID_ProductName );
        }

    }

    public static class DateTime extends GetPropertyTask<EdsTime> {

        public DateTime() {
            super( EdsPropertyID.kEdsPropID_DateTime );
        }

    }

    public static class FirmwareVersion extends GetPropertyTask<String> {

        public FirmwareVersion() {
            super( EdsPropertyID.kEdsPropID_FirmwareVersion );
        }

    }

    public static class BatteryLevel extends GetPropertyTask<Long> {

        public BatteryLevel() {
            super( EdsPropertyID.kEdsPropID_BatteryLevel );
        }

    }

    public static class CurrentStorage extends GetPropertyTask<String> {

        public CurrentStorage() {
            super( EdsPropertyID.kEdsPropID_CurrentStorage );
        }

    }

    public static class CurrentFolder extends GetPropertyTask<String> {

        public CurrentFolder() {
            super( EdsPropertyID.kEdsPropID_CurrentFolder );
        }

    }

    public static class BatteryQuality extends GetPropertyTask<EdsBatteryQuality> {

        public BatteryQuality() {
            super( EdsPropertyID.kEdsPropID_BatteryQuality, EdsBatteryQuality.class );
        }

    }

    public static class BodyIDEx extends GetPropertyTask<String> {

        public BodyIDEx() {
            super( EdsPropertyID.kEdsPropID_BodyIDEx );
        }

    }

    public static class FocusInfo extends GetPropertyTask<EdsFocusInfo> {

        public FocusInfo() {
            super( EdsPropertyID.kEdsPropID_FocusInfo );
        }

    }

    public static class FlashCompensation extends GetPropertyTask<EdsExposureCompensation> {

        public FlashCompensation() {
            super( EdsPropertyID.kEdsPropID_FlashCompensation, EdsExposureCompensation.class );
        }

    }

    public static class AvailableShots extends GetPropertyTask<Long> {

        public AvailableShots() {
            super( EdsPropertyID.kEdsPropID_AvailableShots );
        }

    }

    public static class Bracket extends GetPropertyTask<EdsBracket> {

        public Bracket() {
            super( EdsPropertyID.kEdsPropID_Bracket, EdsBracket.class );
        }

    }

    public static class WhiteBalanceBracket extends GetPropertyTask<int[]> {

        public WhiteBalanceBracket() {
            super( EdsPropertyID.kEdsPropID_WhiteBalanceBracket );
        }

    }

    // true if attached, false if not
    public static class LensStatus extends GetPropertyTask<Boolean> {

        public LensStatus() {
            super( EdsPropertyID.kEdsPropID_LensStatus, Boolean.class );
        }

    }

    public static class Artist extends GetPropertyTask<String> {

        public Artist() {
            super( EdsPropertyID.kEdsPropID_Artist );
        }

    }

    public static class Copyright extends GetPropertyTask<String> {

        public Copyright() {
            super( EdsPropertyID.kEdsPropID_Copyright );
        }

    }

    public static class OwnerName extends GetPropertyTask<String> {

        public OwnerName() {
            super( EdsPropertyID.kEdsPropID_OwnerName );
        }

    }

    public static class SaveTo extends GetPropertyTask<EdsSaveTo> {

        public SaveTo() {
            super( EdsPropertyID.kEdsPropID_SaveTo, EdsSaveTo.class );
        }

    }

    public static class HardDriveDirectoryStructure extends GetPropertyTask<String> {

        public HardDriveDirectoryStructure() {
            super( EdsPropertyID.kEdsPropID_HDDirectoryStructure );
        }

    }

    public static class JPEGQuality extends GetPropertyTask<Long> {

        public JPEGQuality() {
            super( EdsPropertyID.kEdsPropID_JpegQuality );
        }

    }

    public static class ColorTemperature extends GetPropertyTask<Long> {

        public ColorTemperature() {
            super( EdsPropertyID.kEdsPropID_ColorTemperature );
        }

    }

    public static class WhiteBalanceShift extends GetPropertyTask<int[]> {

        public WhiteBalanceShift() {
            super( EdsPropertyID.kEdsPropID_WhiteBalanceShift );
        }

    }

    public static class ParameterSet extends GetPropertyTask<Long> {

        public ParameterSet() {
            super( EdsPropertyID.kEdsPropID_ParameterSet );
        }

    }

    public static class PictureStyleDescription extends GetPropertyTask<EdsPictureStyleDesc> {

        public PictureStyleDescription() {
            super( EdsPropertyID.kEdsPropID_PictureStyleDesc );
        }

    }

    public static class MovieShootingStatus extends GetPropertyTask<Long> {

        public MovieShootingStatus() {
            super( EdsPropertyID.kEdsPropID_Record );
        }

    }

    public static class LiveViewOutputDevice extends GetPropertyTask<EdsEvfOutputDevice> {

        public LiveViewOutputDevice() {
            super( EdsPropertyID.kEdsPropID_Evf_OutputDevice, EdsEvfOutputDevice.class );
        }

    }

    // true if live view enabled, false if disabled
    public static class LiveViewMode extends GetPropertyTask<Boolean> {

        public LiveViewMode() {
            super( EdsPropertyID.kEdsPropID_Evf_Mode, Boolean.class );
        }

    }

    public static class LiveViewColorTemperature extends GetPropertyTask<Long> {

        public LiveViewColorTemperature() {
            super( EdsPropertyID.kEdsPropID_Evf_ColorTemperature );
        }

    }

    // true if preview on, false if off
    public static class LiveViewDepthOfFieldInPreview extends GetPropertyTask<Boolean> {

        public LiveViewDepthOfFieldInPreview() {
            super( EdsPropertyID.kEdsPropID_Evf_DepthOfFieldPreview, Boolean.class );
        }

    }

    public static class DriveMode extends GetPropertyTask<EdsDriveMode> {

        public DriveMode() {
            super( EdsPropertyID.kEdsPropID_DriveMode, EdsDriveMode.class );
        }

    }

    public static class ISOSpeed extends GetPropertyTask<EdsISOSpeed> {

        public ISOSpeed() {
            super( EdsPropertyID.kEdsPropID_ISOSpeed, EdsISOSpeed.class );
        }

    }

    public static class MeteringMode extends GetPropertyTask<EdsMeteringMode> {

        public MeteringMode() {
            super( EdsPropertyID.kEdsPropID_MeteringMode, EdsMeteringMode.class );
        }

    }

    /**
     * AutoFocusMode = AFMode
     * 
     */
    public static class AutoFocusMode extends GetPropertyTask<EdsAFMode> {

        public AutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_AFMode, EdsAFMode.class );
        }

    }

    /**
     * ApertureValue = Av
     * 
     */
    public static class ApertureValue extends GetPropertyTask<EdsAv> {

        public ApertureValue() {
            super( EdsPropertyID.kEdsPropID_Av, EdsAv.class );
        }

    }

    /**
     * ShutterSpeed = Tv
     * 
     */
    public static class ShutterSpeed extends GetPropertyTask<EdsTv> {

        public ShutterSpeed() {
            super( EdsPropertyID.kEdsPropID_Tv, EdsTv.class );
        }

    }

    public static class ExposureCompensation extends GetPropertyTask<EdsExposureCompensation> {

        public ExposureCompensation() {
            super( EdsPropertyID.kEdsPropID_ExposureCompensation, EdsExposureCompensation.class );
        }

    }

    /**
     * ShootingMode = AEMode
     * 
     */
    public static class ShootingMode extends GetPropertyTask<EdsAEMode> {

        public ShootingMode() {
            super( EdsPropertyID.kEdsPropID_AEMode, EdsAEMode.class );
        }

    }

    public static class ImageQuality extends GetPropertyTask<EdsImageQuality> {

        public ImageQuality() {
            super( EdsPropertyID.kEdsPropID_ImageQuality, EdsImageQuality.class );
        }

    }

    public static class WhiteBalance extends GetPropertyTask<EdsWhiteBalance> {

        public WhiteBalance() {
            super( EdsPropertyID.kEdsPropID_WhiteBalance, EdsWhiteBalance.class );
        }

    }

    public static class ColorSpace extends GetPropertyTask<EdsColorSpace> {

        public ColorSpace() {
            super( EdsPropertyID.kEdsPropID_ColorSpace, EdsColorSpace.class );
        }

    }

    public static class PictureStyle extends GetPropertyTask<EdsPictureStyle> {

        public PictureStyle() {
            super( EdsPropertyID.kEdsPropID_PictureStyle, EdsPictureStyle.class );
        }

    }

    /**
     * LiveViewWhiteBalance = Evf_WhiteBalance
     * 
     */
    public static class LiveViewWhiteBalance extends GetPropertyTask<EdsWhiteBalance> {

        public LiveViewWhiteBalance() {
            super( EdsPropertyID.kEdsPropID_Evf_WhiteBalance, EdsWhiteBalance.class );
        }

    }

    /**
     * LiveViewAutoFocusMode = Evf_AFMode
     * 
     */
    public static class LiveViewAutoFocusMode extends GetPropertyTask<EdsEvfAFMode> {

        public LiveViewAutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_Evf_AFMode, EdsEvfAFMode.class );
        }

    }

    /**
     * although EDSDK API v2.13.2 lists this, it seems not to work any more, so
     * use the LiveViewHistogramY/R/G/B methods instead
     * 
     */
    public static class LiveViewHistogram extends GetPropertyTask<int[]> {

        public LiveViewHistogram() {
            super( EdsPropertyID.kEdsPropID_Evf_Histogram, true );
        }

    }

    public static class LiveViewHistogramY extends GetPropertyTask<int[]> {

        public LiveViewHistogramY() {
            super( EdsPropertyID.kEdsPropID_Evf_HistogramY, true );
        }

    }

    public static class LiveViewHistogramR extends GetPropertyTask<int[]> {

        public LiveViewHistogramR() {
            super( EdsPropertyID.kEdsPropID_Evf_HistogramR, true );
        }

    }

    public static class LiveViewHistogramG extends GetPropertyTask<int[]> {

        public LiveViewHistogramG() {
            super( EdsPropertyID.kEdsPropID_Evf_HistogramG, true );
        }

    }

    public static class LiveViewHistogramB extends GetPropertyTask<int[]> {

        public LiveViewHistogramB() {
            super( EdsPropertyID.kEdsPropID_Evf_HistogramB, true );
        }

    }

    public static class LiveViewZoomRatio extends GetPropertyTask<EdsEvfZoom> {

        public LiveViewZoomRatio() {
            super( EdsPropertyID.kEdsPropID_Evf_Zoom, EdsEvfZoom.class, true );
        }

    }

    public static class LiveViewHistogramStatus extends GetPropertyTask<EdsEvfHistogramStatus> {

        public LiveViewHistogramStatus() {
            super( EdsPropertyID.kEdsPropID_Evf_HistogramStatus, EdsEvfHistogramStatus.class, true );
        }

    }

    public static class LiveViewCoordinateSystem extends GetPropertyTask<EdsSize> {

        public LiveViewCoordinateSystem() {
            super( EdsPropertyID.kEdsPropID_Evf_CoordinateSystem, EdsSize.class, true );
        }

    }

    public static class LiveViewZoomPosition extends GetPropertyTask<EdsPoint> {

        public LiveViewZoomPosition() {
            super( EdsPropertyID.kEdsPropID_Evf_ZoomPosition, true );
        }

    }

    public static class LiveViewZoomRectangle extends GetPropertyTask<EdsRect> {

        public LiveViewZoomRectangle() {
            super( EdsPropertyID.kEdsPropID_Evf_ZoomRect, EdsRect.class, true );
        }

    }

    public static class LiveViewCropPosition extends GetPropertyTask<EdsPoint> {

        public LiveViewCropPosition() {
            super( EdsPropertyID.kEdsPropID_Evf_ImagePosition, true );
        }

    }

    public static class LiveViewCropRectangle extends GetPropertyTask<int[]> {

        public LiveViewCropRectangle() {
            super( EdsPropertyID.kEdsPropID_Evf_ImageClipRect, true );
        }

    }

}
