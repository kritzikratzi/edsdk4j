package edsdk.api.commands;

import java.util.Arrays;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsAEMode;
import edsdk.utils.CanonConstants.EdsAFMode;
import edsdk.utils.CanonConstants.EdsAv;
import edsdk.utils.CanonConstants.EdsColorSpace;
import edsdk.utils.CanonConstants.EdsDriveMode;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsExposureCompensation;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsMeteringMode;
import edsdk.utils.CanonConstants.EdsPictureStyle;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsTv;
import edsdk.utils.CanonConstants.EdsWhiteBalance;
import edsdk.utils.CanonUtils;

/**
 * Gets a property description from the camera.
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
//TODO: Should better handle kEdsDataType_Unknown, which seems to be returned
//if the camera doesn't support a property. Could have CanonCommand have an
//EdsError field, and if null is returned by the command, the error could be
//read by the user
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
     * Specific Property ID Commands
     */

    private static class GetEnumPropertyDescCommand<T extends DescriptiveEnum<?>> extends CanonCommand<T[]> {

        private final EdsPropertyID property;
        private final Class<T[]> klass;

        public GetEnumPropertyDescCommand( final EdsPropertyID property,
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

    public static class DriveMode extends GetEnumPropertyDescCommand<EdsDriveMode> {

        public DriveMode() {
            super( EdsPropertyID.kEdsPropID_DriveMode, EdsDriveMode[].class );
        }

    }

    public static class ISOSpeed extends GetEnumPropertyDescCommand<EdsISOSpeed> {

        public ISOSpeed() {
            super( EdsPropertyID.kEdsPropID_ISOSpeed, EdsISOSpeed[].class );
        }

    }

    public static class MeteringMode extends GetEnumPropertyDescCommand<EdsMeteringMode> {

        public MeteringMode() {
            super( EdsPropertyID.kEdsPropID_MeteringMode, EdsMeteringMode[].class );
        }

    }

    /**
     * AutoFocusMode = AFMode
     * 
     */
    public static class AutoFocusMode extends GetEnumPropertyDescCommand<EdsAFMode> {

        public AutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_AFMode, EdsAFMode[].class );
        }

    }

    /**
     * ApertureValue = Av
     * 
     */
    public static class ApertureValue extends GetEnumPropertyDescCommand<EdsAv> {

        public ApertureValue() {
            super( EdsPropertyID.kEdsPropID_Av, EdsAv[].class );
        }

    }

    /**
     * ShutterSpeed = Tv
     * 
     */
    public static class ShutterSpeed extends GetEnumPropertyDescCommand<EdsTv> {

        public ShutterSpeed() {
            super( EdsPropertyID.kEdsPropID_Tv, EdsTv[].class );
        }

    }

    public static class ExposureCompensation extends GetEnumPropertyDescCommand<EdsExposureCompensation> {

        public ExposureCompensation() {
            super( EdsPropertyID.kEdsPropID_ExposureCompensation, EdsExposureCompensation[].class );
        }

    }

    /**
     * ShootingMode = AEMode
     * 
     */
    public static class ShootingMode extends GetEnumPropertyDescCommand<EdsAEMode> {

        public ShootingMode() {
            super( EdsPropertyID.kEdsPropID_AEMode, EdsAEMode[].class );
        }

    }

    public static class ImageQuality extends GetEnumPropertyDescCommand<EdsImageQuality> {

        public ImageQuality() {
            super( EdsPropertyID.kEdsPropID_ImageQuality, EdsImageQuality[].class );
        }

    }

    public static class WhiteBalance extends GetEnumPropertyDescCommand<EdsWhiteBalance> {

        public WhiteBalance() {
            super( EdsPropertyID.kEdsPropID_WhiteBalance, EdsWhiteBalance[].class );
        }

    }

    public static class ColorSpace extends GetEnumPropertyDescCommand<EdsColorSpace> {

        public ColorSpace() {
            super( EdsPropertyID.kEdsPropID_ColorSpace, EdsColorSpace[].class );
        }

    }

    public static class PictureStyle extends GetEnumPropertyDescCommand<EdsPictureStyle> {

        public PictureStyle() {
            super( EdsPropertyID.kEdsPropID_PictureStyle, EdsPictureStyle[].class );
        }

    }

    /**
     * LiveViewWhiteBalance = Evf_WhiteBalance
     * 
     */
    public static class LiveViewWhiteBalance extends GetEnumPropertyDescCommand<EdsWhiteBalance> {

        public LiveViewWhiteBalance() {
            super( EdsPropertyID.kEdsPropID_Evf_WhiteBalance, EdsWhiteBalance[].class );
        }

    }

    /**
     * LiveViewAutoFocusMode = Evf_AFMode
     * 
     */
    public static class LiveViewAutoFocusMode extends GetEnumPropertyDescCommand<EdsEvfAFMode> {

        public LiveViewAutoFocusMode() {
            super( EdsPropertyID.kEdsPropID_Evf_AFMode, EdsEvfAFMode[].class );
        }

    }

}
