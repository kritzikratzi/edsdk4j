package edsdk.utils.commands;

import com.sun.jna.NativeLong;

import edsdk.EdSdkLibrary.EdsBaseRef;
import edsdk.EdsPictureStyleDesc;
import edsdk.EdsPoint;
import edsdk.EdsRect;
import edsdk.EdsSize;
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsAEMode;
import edsdk.utils.CanonConstant.EdsAFMode;
import edsdk.utils.CanonConstant.EdsAv;
import edsdk.utils.CanonConstant.EdsColorSpace;
import edsdk.utils.CanonConstant.EdsCustomFunction;
import edsdk.utils.CanonConstant.EdsDataType;
import edsdk.utils.CanonConstant.EdsDriveMode;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsEvfOutputDevice;
import edsdk.utils.CanonConstant.EdsEvfZoom;
import edsdk.utils.CanonConstant.EdsExposureCompensation;
import edsdk.utils.CanonConstant.EdsFilterEffect;
import edsdk.utils.CanonConstant.EdsISOSpeed;
import edsdk.utils.CanonConstant.EdsImageQuality;
import edsdk.utils.CanonConstant.EdsMeteringMode;
import edsdk.utils.CanonConstant.EdsPictureStyle;
import edsdk.utils.CanonConstant.EdsPropertyID;
import edsdk.utils.CanonConstant.EdsSaveTo;
import edsdk.utils.CanonConstant.EdsTonigEffect;
import edsdk.utils.CanonConstant.EdsTv;
import edsdk.utils.CanonConstant.EdsWhiteBalance;
import edsdk.utils.CanonTask;
import edsdk.utils.CanonUtils;

// TODO - These are defined in EdSdkLibrary but are not described in the API
// Docs:
// kEdsPropID_DepthOfField (EdsUInt32),
// kEdsPropID_EFCompensation,
// kEdsPropID_Evf_FocusAid,
// kEdsPropID_MyMenu (kEdsDataType_UInt32_Array - EdsUInt32[])
//
// TODO - This is no longer in the EdSdkLibrary and is not described by the
// API Docs.. seem to be able to get the values (see GetPropertyTask)...
// haven't tried setting it. Can it be set?
// kEdsPropID_Evf_ImageClipRect (EdsDataType.kEdsDataType_ByteBlock -
// EdsUInt32[])
//
// TODO - Should better handle kEdsDataType_Unknown, which seems to be returned
// if the camera doesn't support a property. Could have CanonTask have an
// EdsError field, and if null is returned by the task, the error could be read
// by the user
//
public abstract class SetPropertyTask<T> extends CanonTask<Boolean> {

    private final EdsPropertyID property;
    private final long param;
    private final T value;
    private final Class<T> klass;
    private final boolean isLiveViewTask;
    private final int liveViewRetryCount = 2;

    public SetPropertyTask( final EdsPropertyID property, final T value ) {
        this( property, 0, value, false );
    }

    public SetPropertyTask( final EdsPropertyID property, final long param,
                            final T value ) {
        this( property, param, value, false );
    }

    public SetPropertyTask( final EdsPropertyID property, final T value,
                            final boolean isLiveViewTask ) {
        this( property, 0, value, isLiveViewTask );
    }

    @SuppressWarnings( "unchecked" )
    public SetPropertyTask( final EdsPropertyID property, final long param,
                            final T value, final boolean isLiveViewTask ) {
        this.property = property;
        this.param = param;
        this.value = value;
        if ( value != null ) {
            klass = (Class<T>) value.getClass();
        } else {
            klass = null;
        }
        this.isLiveViewTask = isLiveViewTask;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void run() {
        if ( value == null ) {
            throw new IllegalStateException( "Cannot set a null value!" );
        }

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

            EdsError err = null;
            switch ( type ) {
                case kEdsDataType_Int32: //EdsInt32
                case kEdsDataType_UInt32: { //EdsUInt32

                    final long longValue;
                    if ( Boolean.class.isAssignableFrom( klass ) ) {
                        // Boolean
                        longValue = (Boolean) value ? 1l : 0l;
                    } else if ( DescriptiveEnum.class.isAssignableFrom( klass ) ) {
                        // DescriptiveEnum
                        longValue = ( (DescriptiveEnum<? extends Number>) value ).value().longValue();
                    } else {
                        // Long
                        longValue = (Long) value;
                    }

                    err = CanonUtils.setPropertyData( baseRef, property, param, longValue );
                    break;
                }
                case kEdsDataType_String: //EdsChar[]
                case kEdsDataType_Point: //EdsPoint
                case kEdsDataType_Rect: //EdsRect
                case kEdsDataType_Time: //EdsTime
                case kEdsDataType_FocusInfo: //EdsFocusInfo
                case kEdsDataType_PictureStyleDesc: { //EdsPictureStyleDesc
                    err = CanonUtils.setPropertyDataAdvanced( baseRef, property, param, value );
                    break;
                }
                case kEdsDataType_ByteBlock: //EdsUInt32[]
                case kEdsDataType_Int32_Array: //EdsInt32[]
                case kEdsDataType_UInt32_Array: { //EdsUInt32[]
                    final int[] array;

                    if ( DescriptiveEnum[].class.isAssignableFrom( klass ) ) {
                        // DescriptiveEnum[]
                        final DescriptiveEnum<? extends Number>[] valueArray = (DescriptiveEnum<? extends Number>[]) value;
                        array = new int[valueArray.length];
                        for ( int i = 0; i < valueArray.length; i++ ) {
                            array[i] = valueArray[i].value().intValue();
                        }
                    } else if ( DescriptiveEnum.class.isAssignableFrom( klass ) ) {
                        // DescriptiveEnum
                        array = new int[] { ( (DescriptiveEnum<? extends Number>) value ).value().intValue() };
                    } else if ( EdsRect.class.isAssignableFrom( klass ) ) {
                        // EdsRect
                        final EdsRect edsRect = (EdsRect) value;
                        array = new int[] { edsRect.point.x.intValue(),
                                           edsRect.point.y.intValue(),
                                           edsRect.size.width.intValue(),
                                           edsRect.size.height.intValue() };
                    } else if ( EdsSize.class.isAssignableFrom( klass ) ) {
                        // EdsSize
                        final EdsSize edsSize = (EdsSize) value;
                        array = new int[] { edsSize.width.intValue(),
                                           edsSize.height.intValue() };
                    } else {
                        // int[]
                        array = (int[]) value;
                    }

                    err = CanonUtils.setPropertyDataAdvanced( baseRef, property, param, array );
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
            System.out.println( "Set property: " + property.name() + " - " +
                                property.description() +
                                ( param > 0l ? param : "" ) + " = " + value +
                                ", result " + err.value() + ": " + err.name() +
                                " - " + err.description() );
            setResult( err == EdsError.EDS_ERR_OK );
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

    public static class Data extends SetPropertyTask<Long> {

        public Data( final EdsPropertyID property, final long value,
                     final boolean isLiveViewTask ) {
            super( property, value, true );
        }

        public Data( final EdsPropertyID property, final long value ) {
            super( property, value );
        }

    }

    public static class EnumData extends SetPropertyTask<DescriptiveEnum<? extends Number>> {

        public EnumData( final EdsPropertyID property,
                         final DescriptiveEnum<? extends Number> value,
                         final boolean isLiveViewTask ) {
            super( property, value, true );
        }

        public EnumData( final EdsPropertyID property,
                         final DescriptiveEnum<? extends Number> value ) {
            super( property, value );
        }

    }

    /*
     * Specific Property ID Tasks
     */

    public static class CustomFunction extends SetPropertyTask<Long> {

        public CustomFunction( final EdsCustomFunction customFunction,
                               final long value ) {
            super( EdsPropertyID.kEdsPropID_CFn, customFunction.value(), value );
        }

    }

    public static class Artist extends SetPropertyTask<String> {

        public Artist( final String value ) {
            super( EdsPropertyID.kEdsPropID_Artist, value );
        }

    }

    public static class Copyright extends SetPropertyTask<String> {

        public Copyright( final String value ) {
            super( EdsPropertyID.kEdsPropID_Copyright, value );
        }

    }

    public static class OwnerName extends SetPropertyTask<String> {

        public OwnerName( final String value ) {
            super( EdsPropertyID.kEdsPropID_OwnerName, value );
        }

    }

    public static class SaveTo extends SetPropertyTask<EdsSaveTo> {

        public SaveTo( final EdsSaveTo value ) {
            super( EdsPropertyID.kEdsPropID_SaveTo, value );
        }

    }

    public static class HardDriveDirectoryStructure extends SetPropertyTask<String> {

        public HardDriveDirectoryStructure( final String value ) {
            super( EdsPropertyID.kEdsPropID_HDDirectoryStructure, value );
        }

    }

    public static class JPEGQuality extends SetPropertyTask<Long> {

        public JPEGQuality( final long value ) {
            super( EdsPropertyID.kEdsPropID_JpegQuality, value );
        }

    }

    public static class ColorTemperature extends SetPropertyTask<Long> {

        public ColorTemperature( final long value ) {
            super( EdsPropertyID.kEdsPropID_ColorTemperature, value );
        }

    }

    public static class WhiteBalanceShift extends SetPropertyTask<int[]> {

        public WhiteBalanceShift( final int[] value ) {
            super( EdsPropertyID.kEdsPropID_WhiteBalanceShift, value );
        }

    }

    public static class ParameterSet extends SetPropertyTask<Long> {

        public ParameterSet( final long value ) {
            super( EdsPropertyID.kEdsPropID_ParameterSet, value );
        }

    }

    public static class PictureStyleDescription extends SetPropertyTask<EdsPictureStyleDesc> {

        public PictureStyleDescription( final EdsPictureStyleDesc value ) {
            super( EdsPropertyID.kEdsPropID_PictureStyleDesc, value );
        }

        public PictureStyleDescription( final long contrast,
                                        final long sharpness,
                                        final long saturation,
                                        final long colorTone,
                                        final EdsFilterEffect filterEffect,
                                        final EdsTonigEffect toningEffect ) {
            super( EdsPropertyID.kEdsPropID_PictureStyleDesc, new EdsPictureStyleDesc( new NativeLong( contrast ), new NativeLong( sharpness ), new NativeLong( saturation ), new NativeLong( colorTone ), new NativeLong( filterEffect.value() ), new NativeLong( toningEffect.value() ) ) );
        }

    }

    public static class MovieShootingStatus extends SetPropertyTask<Long> {

        public MovieShootingStatus( final long value ) {
            super( EdsPropertyID.kEdsPropID_Record, value );
        }

    }

    public static class LiveViewOutputDevice extends SetPropertyTask<EdsEvfOutputDevice> {

        public LiveViewOutputDevice( final EdsEvfOutputDevice value ) {
            super( EdsPropertyID.kEdsPropID_Evf_OutputDevice, value );
        }

    }

    // true if live view enabled, false if disabled
    public static class LiveViewMode extends SetPropertyTask<Boolean> {

        public LiveViewMode( final boolean value ) {
            super( EdsPropertyID.kEdsPropID_Evf_Mode, value );
        }

    }

    public static class LiveViewColorTemperature extends SetPropertyTask<Long> {

        public LiveViewColorTemperature( final long value ) {
            super( EdsPropertyID.kEdsPropID_Evf_ColorTemperature, value );
        }

    }

    // true if preview on, false if off
    public static class LiveViewDepthOfFieldInPreview extends SetPropertyTask<Boolean> {

        public LiveViewDepthOfFieldInPreview( final boolean value ) {
            super( EdsPropertyID.kEdsPropID_Evf_DepthOfFieldPreview, value );
        }

    }

    public static class DriveMode extends SetPropertyTask<EdsDriveMode> {

        public DriveMode( final EdsDriveMode value ) {
            super( EdsPropertyID.kEdsPropID_DriveMode, value );
        }

    }

    public static class ISOSpeed extends SetPropertyTask<EdsISOSpeed> {

        public ISOSpeed( final EdsISOSpeed value ) {
            super( EdsPropertyID.kEdsPropID_ISOSpeed, value );
        }

    }

    public static class MeteringMode extends SetPropertyTask<EdsMeteringMode> {

        public MeteringMode( final EdsMeteringMode value ) {
            super( EdsPropertyID.kEdsPropID_MeteringMode, value );
        }

    }

    /**
     * AutoFocusMode = AFMode
     * 
     */
    //TODO - API indicates this is a read only property - test if this works
    public static class AutoFocusMode extends SetPropertyTask<EdsAFMode> {

        public AutoFocusMode( final EdsAFMode value ) {
            super( EdsPropertyID.kEdsPropID_AFMode, value );
        }

    }

    /**
     * ApertureValue = Av
     * 
     */
    public static class ApertureValue extends SetPropertyTask<EdsAv> {

        public ApertureValue( final EdsAv value ) {
            super( EdsPropertyID.kEdsPropID_Av, value );
        }

    }

    /**
     * ShutterSpeed = Tv
     * 
     */
    public static class ShutterSpeed extends SetPropertyTask<EdsTv> {

        public ShutterSpeed( final EdsTv value ) {
            super( EdsPropertyID.kEdsPropID_Tv, value );
        }

    }

    public static class ExposureCompensation extends SetPropertyTask<EdsExposureCompensation> {

        public ExposureCompensation( final EdsExposureCompensation value ) {
            super( EdsPropertyID.kEdsPropID_ExposureCompensation, value );
        }

    }

    /**
     * ShootingMode = AEMode
     * 
     * According to the EDSDK API docs this should only work on cameras without
     * dials.
     * However, it seems to work on the EOS 550D and may work on other models,
     * but note
     * that it takes about half a second to change the mode on the EOS 550D, so
     * you may
     * want to do a loop that waits until the mode has changed by testing
     * whether the
     * current shooting mode is equal to the target shooting mode.
     * 
     * Oddly, if you remove the USB cable without turning the camera off, the
     * shooting
     * mode stated on the camera display will be the same as that set by this
     * function
     * and may be different from that set on the dial. It is not reset by
     * unplugging
     * the USB cable.
     * 
     * Switching the dial to another value will change the current mode
     * regardless of
     * whether the USB cable is plugged in or not.
     * 
     */
    public static class ShootingMode extends SetPropertyTask<EdsAEMode> {

        public ShootingMode( final EdsAEMode value ) {
            super( EdsPropertyID.kEdsPropID_AEModeSelect, value );
        }

    }

    public static class ImageQuality extends SetPropertyTask<EdsImageQuality> {

        public ImageQuality( final EdsImageQuality value ) {
            super( EdsPropertyID.kEdsPropID_ImageQuality, value );
        }

    }

    public static class WhiteBalance extends SetPropertyTask<EdsWhiteBalance> {

        public WhiteBalance( final EdsWhiteBalance value ) {
            super( EdsPropertyID.kEdsPropID_WhiteBalance, value );
        }

    }

    public static class ColorSpace extends SetPropertyTask<EdsColorSpace> {

        public ColorSpace( final EdsColorSpace value ) {
            super( EdsPropertyID.kEdsPropID_ColorSpace, value );
        }

    }

    public static class PictureStyle extends SetPropertyTask<EdsPictureStyle> {

        public PictureStyle( final EdsPictureStyle value ) {
            super( EdsPropertyID.kEdsPropID_PictureStyle, value );
        }

    }

    /**
     * LiveViewWhiteBalance = Evf_WhiteBalance
     * 
     */
    public static class LiveViewWhiteBalance extends SetPropertyTask<EdsWhiteBalance> {

        public LiveViewWhiteBalance( final EdsWhiteBalance value ) {
            super( EdsPropertyID.kEdsPropID_Evf_WhiteBalance, value );
        }

    }

    /**
     * LiveViewAutoFocusMode = Evf_AFMode
     * 
     */
    public static class LiveViewAutoFocusMode extends SetPropertyTask<EdsEvfAFMode> {

        public LiveViewAutoFocusMode( final EdsEvfAFMode value ) {
            super( EdsPropertyID.kEdsPropID_Evf_AFMode, value );
        }

    }

    public static class LiveViewZoomRatio extends SetPropertyTask<EdsEvfZoom> {

        public LiveViewZoomRatio( final EdsEvfZoom value ) {
            super( EdsPropertyID.kEdsPropID_Evf_Zoom, value );
        }

    }

    public static class LiveViewZoomPosition extends SetPropertyTask<EdsPoint> {

        public LiveViewZoomPosition( final EdsPoint value ) {
            super( EdsPropertyID.kEdsPropID_Evf_ZoomPosition, value );
        }

        public LiveViewZoomPosition( final long x, final long y ) {
            super( EdsPropertyID.kEdsPropID_Evf_ZoomPosition, new EdsPoint( new NativeLong( x ), new NativeLong( y ) ) );
        }

    }

}
