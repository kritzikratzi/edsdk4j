package probe;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import edsdk.api.CanonCamera;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef.ByReference;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdsFocusInfo;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.bindings.EdsRational;
import edsdk.bindings.EdsRect;
import edsdk.bindings.EdsSize;
import edsdk.bindings.EdsTime;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsAEMode;
import edsdk.utils.CanonConstants.EdsAFMode;
import edsdk.utils.CanonConstants.EdsAv;
import edsdk.utils.CanonConstants.EdsColorSpace;
import edsdk.utils.CanonConstants.EdsCustomFunction;
import edsdk.utils.CanonConstants.EdsDataType;
import edsdk.utils.CanonConstants.EdsDriveMode;
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsEvfOutputDevice;
import edsdk.utils.CanonConstants.EdsExposureCompensation;
import edsdk.utils.CanonConstants.EdsFilterEffect;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsMeteringMode;
import edsdk.utils.CanonConstants.EdsPictureStyle;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsTonigEffect;
import edsdk.utils.CanonConstants.EdsTv;
import edsdk.utils.CanonConstants.EdsWhiteBalance;
import edsdk.utils.CanonUtils;

/**
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 * @author Ananta Palani
 * 
 */
public class Test {

    public static void main( final String[] args ) throws InterruptedException, IOException {
        final CanonCamera camera = new CanonCamera();

        if ( camera.openSession() ) {

            Long lvalue = Test.printProperty( camera, EdsPropertyID.kEdsPropID_BatteryLevel );
            System.out.println( lvalue == 0xffffffff
                                                    ? "On AC power"
                                                    : "On battery, value is percentage remaining." );

            lvalue = Test.printProperty( camera, EdsPropertyID.kEdsPropID_BatteryQuality );

            lvalue = Test.printProperty( camera, EdsPropertyID.kEdsPropID_DriveMode );

            for ( final EdsPropertyID id : EdsPropertyID.values() ) {
                Test.getPropertyDesc( camera.getEdsCamera(), id, true );
            }

            final EdsISOSpeed currentISOSpeed = EdsISOSpeed.enumOfValue( camera.getProperty( EdsPropertyID.kEdsPropID_ISOSpeed ).intValue() );
            final EdsISOSpeed[] availableISOSpeeds = camera.getAvailableISOSpeeds();
            if ( availableISOSpeeds != null ) {
                for ( final EdsISOSpeed e : availableISOSpeeds ) {
                    System.out.println( "ISO Speed " +
                                        e.description() +
                                        ( e.equals( currentISOSpeed )
                                                                     ? " *CURRENT* "
                                                                     : "" ) );
                }
            } else {
                System.out.println( "ISO Speed settings not configurable with current mode/lens" );
            }

            final EdsTv currentShutterSpeed = EdsTv.enumOfValue( camera.getProperty( EdsPropertyID.kEdsPropID_Tv ).intValue() );
            final EdsTv[] availableShutterSpeeds = camera.getAvailableShutterSpeeds();
            if ( availableShutterSpeeds != null ) {
                for ( final EdsTv e : availableShutterSpeeds ) {
                    System.out.println( "Shutter Speed " +
                                        e.description() +
                                        ( e.equals( currentShutterSpeed )
                                                                         ? " *CURRENT* "
                                                                         : "" ) );
                }
            } else {
                System.out.println( "Shutter speed is not configurable with current mode/lens" );
            }

            final EdsAv currentApertureValue = EdsAv.enumOfValue( camera.getProperty( EdsPropertyID.kEdsPropID_Av ).intValue() );
            final EdsAv[] availableApertureValues = camera.getAvailableApertureValues();
            if ( availableApertureValues != null ) {
                for ( final EdsAv e : availableApertureValues ) {
                    System.out.println( "Aperture " +
                                        e.description() +
                                        ( e.equals( currentApertureValue )
                                                                          ? " *CURRENT* "
                                                                          : "" ) );
                }
            } else {
                System.out.println( "Aperture value not configurable with current mode/lens" );
            }

            System.out.println( "Generic Array Test" );
            final EdsISOSpeed[] aa = Test.genericArrayTest( EdsISOSpeed[].class, new int[] {
                                                                                            0x30,
                                                                                            0x53,
                                                                                            0x68 } );
            for ( final EdsISOSpeed a : aa ) {
                System.out.println( " " + a.description() );
            }

            System.out.println( "\nMirror Lockup Setting: " +
                                camera.getCustomFunction( EdsCustomFunction.kEdsCustomFunction_MirrorLockup ) );

            camera.beginLiveView();

            // sleep a few seconds so live view can start
            Thread.sleep( 2000 );

            final ArrayList<EdsBaseRef> baserefs = new ArrayList<EdsBaseRef>();
            baserefs.add( camera.getEdsCamera() );

            final ByReference[] liverefs = CanonUtils.getLiveViewImageReference( camera.getEdsCamera() );
            if ( liverefs != null && liverefs.length > 0 && liverefs[0] != null ) {
                System.out.println( "\nlive view active so adding evf image reference" );
                baserefs.add( liverefs[0].getValue() );
            }

            System.out.println( "\nMirror Lockup Setting: " +
                                camera.getCustomFunction( EdsCustomFunction.kEdsCustomFunction_MirrorLockup ) );

            System.out.println( "\nLive View Histogram Status: " +
                                camera.getLiveViewHistogramStatus() );

            final boolean hideNegativeSizeProperties = true;
            final boolean hideNegativeSizeCustomFunctions = true;

            //TEST READING PROPERTIES FOR CAMERA AND LIVE VIEW
            System.out.println( "\nProperty Sizes" );
            System.out.println( "---------------------------------------" );
            for ( final EdsBaseRef baseref : baserefs ) {
                System.out.println( "\nGetting properties for: " +
                                    baseref.getClass().getSimpleName() );
                System.out.println( "---------------------------------------" );
                for ( final EdsPropertyID id : EdsPropertyID.values() ) {
                    final long size = CanonUtils.getPropertySize( baseref, id );

                    final EdsDataType type = CanonUtils.getPropertyType( baseref, id );

                    if ( size > -1 || !hideNegativeSizeProperties ) {
                        System.out.println( id.name() );
                        System.out.println( "  Size: " + size );
                        System.out.println( "  Type: " + type.description() );
                        if ( size == -1 &&
                             type.equals( EdsDataType.kEdsDataType_Unknown ) ) {
                            System.out.println( id.name() +
                                                " - VALUE NOT AVAILABLE ON THIS CAMERA AND/OR WITH CURRENT SETTINGS" );
                        } else {
                            final Object value;
                            switch ( type ) {
                                case kEdsDataType_String: {
                                    final String data = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = data;
                                    break;
                                }
                                case kEdsDataType_Rational: {
                                    final EdsRational struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_Point: {
                                    final EdsPoint struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_Rect: {
                                    final EdsRect struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_Time: {
                                    final EdsTime struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_FocusInfo: {
                                    final EdsFocusInfo struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_PictureStyleDesc: {
                                    final EdsPictureStyleDesc struct = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = struct;
                                    break;
                                }
                                case kEdsDataType_ByteBlock:
                                case kEdsDataType_Int8_Array:
                                case kEdsDataType_UInt8_Array:
                                case kEdsDataType_Int16_Array:
                                case kEdsDataType_UInt16_Array:
                                case kEdsDataType_Int32_Array:
                                case kEdsDataType_UInt32_Array: {
                                    final int[] array = CanonUtils.getPropertyDataAdvanced( baseref, id );
                                    value = array;
                                    break;
                                }
                                default:
                                    value = Long.valueOf( CanonUtils.getPropertyData( baseref, id ) );
                            }
                            System.out.println( " Value: " +
                                                Test.toString( value ) );
                        }
                    }
                }
                System.out.println( "\n" );
            }

            //TEST READING CUSTOM FUNCTIONS
            System.out.println( "Custom Functions" );
            System.out.println( "---------------------------------------" );
            for ( int i = -10000; i < 10000; i++ ) {
                final long size = CanonUtils.getPropertySize( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_CFn, i );

                final EdsDataType type = CanonUtils.getPropertyType( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_CFn, i );

                if ( size > -1 || !hideNegativeSizeCustomFunctions ) {
                    System.out.println( "Number: " + i + " (0x" +
                                        Integer.toHexString( i ) + ")" );
                    System.out.println( "  Type: " + type.description() );
                    System.out.println( "  Size: " + size );
                    if ( size == -1 &&
                         type.equals( EdsDataType.kEdsDataType_Unknown ) ) {
                        System.out.println( " VALUE NOT AVAILABLE ON THIS CAMERA AND/OR WITH CURRENT SETTINGS" );
                    } else {
                        final Long value = CanonUtils.getPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_CFn, i );
                        System.out.println( " Value: " + value );
                        final EdsCustomFunction e = EdsCustomFunction.enumOfValue( i );
                        if ( e != null ) {
                            System.out.println( " Known: Yes, " + e.name() +
                                                " (" + e.description() + ")" );
                        } else {
                            System.out.println( " Known: NO" );
                        }
                    }
                }
            }
            System.out.println( "\n" );

            if ( liverefs != null ) {
                CanonUtils.release( liverefs );
            }

            //TEST GETTERS FROM CanonCamera
            System.out.println( "Property Getters in CanonCamera" );
            System.out.println( "---------------------------------------" );
            final Method[] methods = camera.getClass().getMethods();
            for ( final Method method : methods ) {
                if ( method.getName().startsWith( "get" ) &&
                     method.getParameterTypes().length == 0 ) {
                    System.out.println( "Trying " + method.getName() );
                    try {
                        final Object o = method.invoke( camera, (Object[]) null );
                        System.out.println( " Result: " + Test.toString( o ) );
                    }
                    catch ( final IllegalAccessException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch ( final IllegalArgumentException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch ( final InvocationTargetException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            System.out.println( "\n\n" );

            //TEST CUSTOM FUNCTION FROM CanonCamera
            System.out.println( "EdsCustomFunction with CanonCamera.getCustomFunction" );
            System.out.println( "----------------------------------------------------" );
            for ( final EdsCustomFunction e : EdsCustomFunction.values() ) {
                System.out.println( "\nTrying " + e.description() + " (" +
                                    e.name() + ")" );
                final Long result = camera.getCustomFunction( e );
                System.out.println( "    Value: " + Test.toString( result ) );
            }
            System.out.println( "\n" );

            //TEST FOR MISSING EdsPropID CONSTANTS
            System.out.println( "Testing if its possible to get values from the camera for undefined property IDs from 0x0 to 0xFFFF" );
            System.out.println( "----------------------------------------------------" );
            for ( int i = 0; i < 0xFFFF; i++ ) {
                if ( null == EdsPropertyID.enumOfValue( i ) ) {
                    final int bufferSize = 1;
                    final IntBuffer type = IntBuffer.allocate( bufferSize );
                    final NativeLongByReference number = new NativeLongByReference( new NativeLong( bufferSize ) );
                    EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetPropertySize( camera.getEdsCamera(), new NativeLong( i ), new NativeLong( 0 ), type, number ) );
                    if ( !err.equals( EdsError.EDS_ERR_PROPERTIES_UNAVAILABLE ) ) {
                        System.out.println( i + " (0x" +
                                            Integer.toHexString( i ) + "): " +
                                            err.description() );
                    }
                    if ( err == EdsError.EDS_ERR_OK ) {
                        final int size = (int) number.getValue().longValue();
                        final EdsDataType edsType = EdsDataType.enumOfValue( type.get( 0 ) );
                        if ( size > -1 ) {
                            if ( edsType == null ||
                                 edsType.equals( EdsDataType.kEdsDataType_Unknown ) ) {
                                System.out.println( "WARNING: size is greater than -1 (" +
                                                    size +
                                                    "), but edsType is unknown!" );
                            } else {
                                final Memory memory = new Memory( size > 0
                                                                          ? size
                                                                          : 1 );
                                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetPropertyData( camera.getEdsCamera(), new NativeLong( i ), new NativeLong( 0 ), new NativeLong( size ), memory ) );
                                if ( err == EdsError.EDS_ERR_OK ) {
                                    if ( edsType.equals( EdsDataType.kEdsDataType_Int32 ) ||
                                         edsType.equals( EdsDataType.kEdsDataType_UInt32 ) ) {
                                        System.out.println( "    property: " +
                                                            i +
                                                            " (0x" +
                                                            Integer.toHexString( i ) +
                                                            "), value: " +
                                                            memory.getNativeLong( 0 ) +
                                                            ", data type: " +
                                                            edsType.description() +
                                                            ", size: " + size );
                                    } else if ( edsType.equals( EdsDataType.kEdsDataType_String ) ) {
                                        System.out.println( "    property: " +
                                                            i +
                                                            " (0x" +
                                                            Integer.toHexString( i ) +
                                                            "), value: " +
                                                            memory.getString( 0 ) +
                                                            ", data type: " +
                                                            edsType.description() +
                                                            ", size: " + size );
                                    } else {
                                        System.out.println( "    property: " +
                                                            i +
                                                            " (0x" +
                                                            Integer.toHexString( i ) +
                                                            "), value: NOT SUPPORTED, data type: " +
                                                            edsType.description() +
                                                            ", size: " + size );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            camera.endLiveView();

            //CanonUtils.isMirrorLockupEnabled( camera.getEdsCamera() );

            camera.closeSession();
        }

        CanonCamera.close();
    }

    public static final DescriptiveEnum<?>[] getPropertyDesc( final EdsCameraRef camera,
                                                              final EdsPropertyID property,
                                                              final boolean hideEmptyPropertyDesc ) {

        final EdsPropertyDesc propertyDesc = CanonUtils.getPropertyDesc( (EdsBaseRef) camera, property );

        if ( propertyDesc.numElements.intValue() > 0 || !hideEmptyPropertyDesc ) {
            System.out.println( "Getting available property values for " +
                                property.description() + " (" +
                                property.name() + ")" );
            DescriptiveEnum<?> currentProperty = null;
            try {
                final int currentPropertyValue = CanonUtils.getPropertyData( camera, property ).intValue();
                switch ( property ) {
                    case kEdsPropID_DriveMode:
                        currentProperty = EdsDriveMode.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_ISOSpeed:
                        currentProperty = EdsISOSpeed.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_MeteringMode:
                        currentProperty = EdsMeteringMode.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_AFMode:
                        currentProperty = EdsAFMode.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_Av:
                        currentProperty = EdsAv.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_Tv:
                        currentProperty = EdsTv.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_ExposureCompensation:
                        currentProperty = EdsExposureCompensation.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_AEMode:
                        currentProperty = EdsAEMode.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_ImageQuality:
                        currentProperty = EdsImageQuality.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_WhiteBalance:
                        currentProperty = EdsWhiteBalance.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_ColorSpace:
                        currentProperty = EdsColorSpace.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_PictureStyle:
                        currentProperty = EdsPictureStyle.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_Evf_OutputDevice:
                        currentProperty = EdsEvfOutputDevice.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_Evf_WhiteBalance:
                        currentProperty = EdsWhiteBalance.enumOfValue( currentPropertyValue );
                        break;
                    case kEdsPropID_Evf_AFMode:
                        currentProperty = EdsEvfAFMode.enumOfValue( currentPropertyValue );
                        break;
                }
            }
            catch ( final IllegalArgumentException e ) {
                //
            }
            if ( currentProperty == null ) {
                if ( propertyDesc.numElements.intValue() > 0 ) {
                    System.out.println( "WARNING: Could not get the current value for " +
                                        property.description() +
                                        " (" +
                                        property.name() + ")" );
                }
            } else {
                System.out.println( "Current Value: ( " +
                                    currentProperty.value() + " ) " +
                                    currentProperty.description() );
            }

            System.out.println( "Number of elements: " +
                                propertyDesc.numElements );

            final NativeLong[] propDesc = propertyDesc.propDesc;
            final DescriptiveEnum<?>[] properties = new DescriptiveEnum<?>[propertyDesc.numElements.intValue()];
            for ( int i = 0; i < propertyDesc.numElements.intValue(); i++ ) {
                DescriptiveEnum<?> e = null;
                switch ( property ) {
                    case kEdsPropID_DriveMode:
                        e = EdsDriveMode.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_ISOSpeed:
                        e = EdsISOSpeed.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_MeteringMode:
                        e = EdsMeteringMode.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_AFMode:
                        e = EdsAFMode.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_Av:
                        e = EdsAv.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_Tv:
                        e = EdsTv.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_ExposureCompensation:
                        e = EdsExposureCompensation.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_AEMode:
                        e = EdsAEMode.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_ImageQuality:
                        e = EdsImageQuality.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_WhiteBalance:
                        e = EdsWhiteBalance.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_ColorSpace:
                        e = EdsColorSpace.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_PictureStyle:
                        e = EdsPictureStyle.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_Evf_OutputDevice:
                        e = EdsEvfOutputDevice.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_Evf_WhiteBalance:
                        e = EdsWhiteBalance.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_Evf_AFMode:
                        e = EdsEvfAFMode.enumOfValue( propDesc[i].intValue() );
                        break;
                    case kEdsPropID_AEBracket:
                    case kEdsPropID_Evf_Mode:
                        return null;
                    default:
                        throw new IllegalArgumentException( "Property '" +
                                                            property.name() +
                                                            "' is not supported." );
                }
                if ( e == null ) {
                    System.out.println( "WARNING: Could not find " +
                                        property.name() +
                                        " enum with value of: " +
                                        propDesc[i].intValue() );
                } else {
                    System.out.println( e.name() + " ( " + e.value() + " ) " +
                                        e.description() );
                }
                properties[i] = e;
            }
            System.out.println( "DONE!\n" );

            return properties;
        }
        return null;
    }

    public static final Long printProperty( final CanonCamera camera,
                                            final EdsPropertyID property ) {
        final Long value = camera.getProperty( property );
        System.out.println( property.name() + " - " + property.description() +
                            ": " + value + "\n" );
        return value;
    }

    public static final String toString( final Object value ) {
        final String result;
        if ( value != null ) {
            final Class<?> klass = value.getClass();
            if ( String.class.isAssignableFrom( klass ) ) {
                result = (String) value;
            } else if ( DescriptiveEnum.class.isAssignableFrom( klass ) ) {
                final DescriptiveEnum<?> descEnum = (DescriptiveEnum<?>) value;
                result = descEnum.description() + " (" + descEnum.name() + ")";
            } else if ( DescriptiveEnum[].class.isAssignableFrom( klass ) ) {
                final DescriptiveEnum<?>[] array = (DescriptiveEnum<?>[]) value;
                String s = "\n";
                if ( array.length > 0 ) {
                    for ( final DescriptiveEnum<?> descEnum : array ) {
                        s += "\n    " + descEnum.description() + " (" +
                             descEnum.name() + ")";
                    }
                    s = s.substring( 1 );
                }
                result = s;
            } else if ( EdsRational.class.isAssignableFrom( klass ) ) {
                final EdsRational struct = (EdsRational) value;
                result = new String( struct.numerator.longValue() + " / " +
                                     struct.denominator.longValue() );
            } else if ( EdsPoint.class.isAssignableFrom( klass ) ) {
                final EdsPoint struct = (EdsPoint) value;
                result = new String( "(" + struct.x + ", " + struct.y + ")" );
            } else if ( EdsRect.class.isAssignableFrom( klass ) ) {
                final EdsRect struct = (EdsRect) value;
                result = new String( struct.size.width + "x" +
                                     struct.size.height + ", (" +
                                     struct.point.x + ", " + struct.point.y +
                                     ")" );
            } else if ( EdsSize.class.isAssignableFrom( klass ) ) {
                final EdsSize struct = (EdsSize) value;
                result = new String( struct.width + "x" + struct.height );
            } else if ( EdsTime.class.isAssignableFrom( klass ) ) {
                final EdsTime struct = (EdsTime) value;
                result = new String( struct.year.intValue() + "-" +
                                     struct.month.intValue() + "-" +
                                     struct.day.intValue() + " " +
                                     struct.hour.intValue() + ":" +
                                     struct.minute.intValue() + ":" +
                                     struct.second.intValue() + "." +
                                     struct.milliseconds.intValue() );
            } else if ( EdsFocusInfo.class.isAssignableFrom( klass ) ) {
                final EdsFocusInfo struct = (EdsFocusInfo) value;
                // TODO: handle struct output
                result = struct.toString();
            } else if ( EdsPictureStyleDesc.class.isAssignableFrom( klass ) ) {
                final EdsPictureStyleDesc struct = (EdsPictureStyleDesc) value;
                result = new String( "\n    Color tone: " +
                                     struct.colorTone.longValue() +
                                     "\n    Contrast: " +
                                     struct.contrast.longValue() +
                                     "\n    Filter effect: " +
                                     EdsFilterEffect.enumOfValue( struct.filterEffect.intValue() ).description() +
                                     "\n    Saturation: " +
                                     struct.saturation.longValue() +
                                     "\n    Sharpness: " +
                                     struct.sharpness.longValue() +
                                     "\n    Toning Effect: " +
                                     EdsTonigEffect.enumOfValue( struct.toningEffect.intValue() ).description() );
            } else if ( int[].class.isAssignableFrom( klass ) ) {
                final int[] array = (int[]) value;
                String s = "";
                if ( array != null && array.length > 0 ) {
                    for ( final int i : array ) {
                        s += ", " + i;
                    }
                    s = s.substring( 2 );
                }
                result = s;
            } else {
                result = value.toString();
            }
            return result;
        }
        return "";
    }

    @SuppressWarnings( "unchecked" )
    public static final <T> T genericArrayTest( final Class<T> klass,
                                                final int[] data ) {
        final DescriptiveEnum<?>[] array = (DescriptiveEnum<?>[]) Array.newInstance( klass.getComponentType(), data.length );
        for ( int i = 0; i < data.length; i++ ) {
            array[i] = CanonConstants.enumOfValue( (Class<? extends DescriptiveEnum<?>>) klass.getComponentType(), data[i] );
        }
        return (T) array;
    }

}
