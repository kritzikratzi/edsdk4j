package edsdk.api.commands;

import java.lang.reflect.Array;

import com.sun.jna.NativeLong;

import edsdk.api.CanonCommand;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdsFocusInfo;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.bindings.EdsRect;
import edsdk.bindings.EdsSize;
import edsdk.bindings.EdsTime;
import edsdk.utils.CanonConstants;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsAEMode;
import edsdk.utils.CanonConstants.EdsAFMode;
import edsdk.utils.CanonConstants.EdsAv;
import edsdk.utils.CanonConstants.EdsBatteryQuality;
import edsdk.utils.CanonConstants.EdsBracket;
import edsdk.utils.CanonConstants.EdsColorSpace;
import edsdk.utils.CanonConstants.EdsCustomFunction;
import edsdk.utils.CanonConstants.EdsDataType;
import edsdk.utils.CanonConstants.EdsDriveMode;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsEvfHistogramStatus;
import edsdk.utils.CanonConstants.EdsEvfOutputDevice;
import edsdk.utils.CanonConstants.EdsEvfZoom;
import edsdk.utils.CanonConstants.EdsExposureCompensation;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsMeteringMode;
import edsdk.utils.CanonConstants.EdsPictureStyle;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsSaveTo;
import edsdk.utils.CanonConstants.EdsTv;
import edsdk.utils.CanonConstants.EdsWhiteBalance;
import edsdk.utils.CanonUtils;

/**
 * Gets a property from the camera.
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com> This work is free. You can redistribute it and/or
 * modify it under the terms of the Do What The Fuck You Want To Public License,
 * Version 2, as published by Sam Hocevar. See the COPYING file for more
 * details.
 * 
 * @author hansi
 * @author Ananta Palani
 * 
 */
// TODO: These are defined in EdSdkLibrary but are not described in the API
// Docs:
// kEdsPropID_DepthOfField (EdsUInt32),
// kEdsPropID_EFCompensation (??),
// kEdsPropID_Evf_FocusAid (??),
// kEdsPropID_MyMenu (kEdsDataType_UInt32_Array - EdsUInt32[])
//
// TODO: Should better handle kEdsDataType_Unknown, which seems to be returned
// if the camera doesn't support a property. Could have CanonCommand have an
// EdsError field, and if null is returned by the command, the error could be
// read by the user
//
// If return type T differs from data type for property (for instance,
// conversion for EdsUInt32 to a CanonConstants enum), the Class<T> must be
// provided by the constructor
public abstract class GetPropertyCommand<T> extends CanonCommand<T> {

	private final EdsPropertyID property;
	private final long param;
	private final Class<T> klass;
	private final boolean isLiveViewCommand;
	private final int liveViewRetryCount = 2;

	public GetPropertyCommand(final EdsPropertyID property) {
		this(property, 0, null, false);
	}

	public GetPropertyCommand(final EdsPropertyID property, final long param) {
		this(property, param, null, false);
	}

	public GetPropertyCommand(final EdsPropertyID property, final boolean isLiveViewCommand) {
		this(property, 0, null, isLiveViewCommand);
	}

	public GetPropertyCommand(final EdsPropertyID property, final long param, final boolean isLiveViewCommand) {
		this(property, param, null, isLiveViewCommand);
	}

	public GetPropertyCommand(final EdsPropertyID property, final Class<T> klass) {
		this(property, 0, klass, false);
	}

	public GetPropertyCommand(final EdsPropertyID property, final long param, final Class<T> klass) {
		this(property, param, klass, false);
	}

	public GetPropertyCommand(final EdsPropertyID property, final Class<T> klass, final boolean isLiveViewCommand) {
		this(property, 0, klass, isLiveViewCommand);
	}

	public GetPropertyCommand(final EdsPropertyID property, final long param, final Class<T> klass,
			final boolean isLiveViewCommand) {
		this.property = property;
		this.param = param;
		this.klass = klass;
		this.isLiveViewCommand = isLiveViewCommand;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Throwable t = null;
		EdsBaseRef.ByReference[] references = null;
		try {
			final EdsBaseRef baseRef;
			if (isLiveViewCommand) {
				if (CanonUtils.isLiveViewEnabled(camera.getEdsCamera(), false)) {
					for (int i = 0; i < liveViewRetryCount && references == null; i++) {
						if (i > 0) {
							Thread.sleep(100);
						}
						references = CanonUtils.getLiveViewImageReference(camera.getEdsCamera());
					}
					if (references != null) {
						baseRef = references[0].getValue();
					} else {
						// TODO: it may take several seconds for live view to
						// start, so this might happen every time.. perhaps the
						// previous should be tried for a few seconds
						// throw new IllegalStateException( "Could not retrieve
						// live view image reference!" );
						System.err.println("Could not retrieve live view image reference!");
						setResult(null);
						return;
					}
				} else {
					// throw new IllegalStateException( "Live view is not
					// enabled!" );
					System.err.println("Live view is not enabled!");
					setResult(null);
					return;
				}
			} else {
				baseRef = camera.getEdsCamera();
			}

			final EdsDataType type = CanonUtils.getPropertyType(baseRef, property, param);

			T result = null;
			if (type == null) {
				System.err.println(property+" is not currently supported by GetPropertyCommand. Likely this camera does not support property "
				+ property.name() + " in the current mode or at all.");
			} else {
				switch (type) {
				case kEdsDataType_Int32: // EdsInt32
				case kEdsDataType_UInt32: { // EdsUInt32
					final Long data = CanonUtils.getPropertyData(baseRef, property, param);

					if (data != null) {
						if (klass != null && Boolean.class.isAssignableFrom(klass)) {
							// Boolean
							result = (T) Boolean.valueOf(data == 1l);
						} else if (klass != null && DescriptiveEnum.class.isAssignableFrom(klass)) {
							// DescriptiveEnum
							result = (T) CanonConstants.enumOfValue((Class<? extends DescriptiveEnum<?>>) klass,
									data.intValue());
						} else {
							// Long
							result = (T) Long.valueOf(data);
						}
					}

					break;
				}
				case kEdsDataType_String: { // EdsChar[]
					final String data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_Point: { // EdsPoint
					final EdsPoint data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_Rect: { // EdsRect
					final EdsRect data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_Time: { // EdsTime
					final EdsTime data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_FocusInfo: { // EdsFocusInfo
					final EdsFocusInfo data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_PictureStyleDesc: { // EdsPictureStyleDesc
					final EdsPictureStyleDesc data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);
					result = (T) data;
					break;
				}
				case kEdsDataType_ByteBlock: // EdsUInt32[]
				case kEdsDataType_Int32_Array: // EdsInt32[]
				case kEdsDataType_UInt32_Array: { // EdsUInt32[]
					final int[] data = CanonUtils.getPropertyDataAdvanced(baseRef, property, param);

					if (data != null) {
						if (klass != null && DescriptiveEnum[].class.isAssignableFrom(klass)) {
							// DescriptiveEnum[]
							final DescriptiveEnum<?>[] array = (DescriptiveEnum<?>[]) Array
									.newInstance(klass.getComponentType(), data.length);
							for (int i = 0; i < data.length; i++) {
								array[i] = CanonConstants.enumOfValue(
										(Class<? extends DescriptiveEnum<?>>) klass.getComponentType(), data[i]);
							}
							result = (T) array;
						} else if (klass != null && DescriptiveEnum.class.isAssignableFrom(klass)) {
							// DescriptiveEnum
							if (data.length > 1) {
								throw new IllegalStateException(
										"Only single result expected but multiple results returned!");
							}
							result = (T) CanonConstants.enumOfValue((Class<? extends DescriptiveEnum<?>>) klass,
									data[0]);
						} else if (klass != null && EdsRect.class.isAssignableFrom(klass)) {
							// EdsRect
							if (data.length != 4) {
								throw new IllegalStateException("Four values expected for an EdsRect!");
							}
							result = (T) new EdsRect(new EdsPoint(new NativeLong(data[0]), new NativeLong(data[1])),
									new EdsSize(new NativeLong(data[2]), new NativeLong(data[3])));
						} else if (klass != null && EdsSize.class.isAssignableFrom(klass)) {
							// EdsSize
							if (data.length != 2) {
								throw new IllegalStateException("Two values expected for an EdsSize!");
							}
							result = (T) new EdsSize(new NativeLong(data[0]), new NativeLong(data[1]));
						} else {
							// int[]
							result = (T) data;
						}
					}

					break;
				}
				default:
					System.err.println(type.description() + " (" + type.name()
							+ ") is not currently supported by GetPropertyCommand. Likely this camera does not support property "
							+ property.name() + " in the current mode or at all.");

					// throw new IllegalStateException( type.description() + "
					// (" +
					// type.name() +
					// ") is not currently supported by GetPropertyCommand.
					// Likely this camera does not support property " +
					// property.name() + " in the current mode or at all." );
				}
			}
			setResult(result);
			return;
		} catch (final IllegalArgumentException e) {
			t = e;
		} catch (final InterruptedException e) {
			t = e;
		} finally {
			if (references != null) {
				CanonUtils.release(references);
			}
		}
		System.err.println(t.getMessage());
		setResult(null);
	}

	public static class Data extends GetPropertyCommand<Long> {

		public Data(final EdsPropertyID property, final boolean isLiveViewCommand) {
			super(property, true);
		}

		public Data(final EdsPropertyID property) {
			super(property);
		}

	}

	public static class Size extends CanonCommand<Long> {

		private final EdsPropertyID property;

		public Size(final EdsPropertyID property) {
			this.property = property;
		}

		@Override
		public void run() {
			setResult(CanonUtils.getPropertySize(camera.getEdsCamera(), property));

		}

	}

	public static class Type extends CanonCommand<EdsDataType> {

		private final EdsPropertyID property;

		public Type(final EdsPropertyID property) {
			this.property = property;
		}

		@Override
		public void run() {
			setResult(CanonUtils.getPropertyType(camera.getEdsCamera(), property));

		}

	}

	/*
	 * Specific Property ID Commands
	 */

	public static class CustomFunction extends GetPropertyCommand<Long> {

		public CustomFunction(final EdsCustomFunction customFunction) {
			super(EdsPropertyID.kEdsPropID_CFn, customFunction.value());
		}

	}

	public static class ProductName extends GetPropertyCommand<String> {

		public ProductName() {
			super(EdsPropertyID.kEdsPropID_ProductName);
		}

	}

	public static class DateTime extends GetPropertyCommand<EdsTime> {

		public DateTime() {
			super(EdsPropertyID.kEdsPropID_DateTime);
		}

	}

	public static class FirmwareVersion extends GetPropertyCommand<String> {

		public FirmwareVersion() {
			super(EdsPropertyID.kEdsPropID_FirmwareVersion);
		}

	}

	public static class BatteryLevel extends GetPropertyCommand<Long> {

		public BatteryLevel() {
			super(EdsPropertyID.kEdsPropID_BatteryLevel);
		}

	}

	public static class CurrentStorage extends GetPropertyCommand<String> {

		public CurrentStorage() {
			super(EdsPropertyID.kEdsPropID_CurrentStorage);
		}

	}

	public static class CurrentFolder extends GetPropertyCommand<String> {

		public CurrentFolder() {
			super(EdsPropertyID.kEdsPropID_CurrentFolder);
		}

	}

	public static class BatteryQuality extends GetPropertyCommand<EdsBatteryQuality> {

		public BatteryQuality() {
			super(EdsPropertyID.kEdsPropID_BatteryQuality, EdsBatteryQuality.class);
		}

	}

	public static class BodyIDEx extends GetPropertyCommand<String> {

		public BodyIDEx() {
			super(EdsPropertyID.kEdsPropID_BodyIDEx);
		}

	}

	public static class FocusInfo extends GetPropertyCommand<EdsFocusInfo> {

		public FocusInfo() {
			super(EdsPropertyID.kEdsPropID_FocusInfo);
		}

	}

	public static class FlashCompensation extends GetPropertyCommand<EdsExposureCompensation> {

		public FlashCompensation() {
			super(EdsPropertyID.kEdsPropID_FlashCompensation, EdsExposureCompensation.class);
		}

	}

	public static class AvailableShots extends GetPropertyCommand<Long> {

		public AvailableShots() {
			super(EdsPropertyID.kEdsPropID_AvailableShots);
		}

	}

	public static class Bracket extends GetPropertyCommand<EdsBracket> {

		public Bracket() {
			super(EdsPropertyID.kEdsPropID_Bracket, EdsBracket.class);
		}

	}

	public static class WhiteBalanceBracket extends GetPropertyCommand<int[]> {

		public WhiteBalanceBracket() {
			super(EdsPropertyID.kEdsPropID_WhiteBalanceBracket);
		}

	}

	// true if attached, false if not
	public static class LensStatus extends GetPropertyCommand<Boolean> {

		public LensStatus() {
			super(EdsPropertyID.kEdsPropID_LensStatus, Boolean.class);
		}

	}

	public static class Artist extends GetPropertyCommand<String> {

		public Artist() {
			super(EdsPropertyID.kEdsPropID_Artist);
		}

	}

	public static class Copyright extends GetPropertyCommand<String> {

		public Copyright() {
			super(EdsPropertyID.kEdsPropID_Copyright);
		}

	}

	public static class OwnerName extends GetPropertyCommand<String> {

		public OwnerName() {
			super(EdsPropertyID.kEdsPropID_OwnerName);
		}

	}

	public static class SaveTo extends GetPropertyCommand<EdsSaveTo> {

		public SaveTo() {
			super(EdsPropertyID.kEdsPropID_SaveTo, EdsSaveTo.class);
		}

	}

	public static class HardDriveDirectoryStructure extends GetPropertyCommand<String> {

		public HardDriveDirectoryStructure() {
			super(EdsPropertyID.kEdsPropID_HDDirectoryStructure);
		}

	}

	public static class JPEGQuality extends GetPropertyCommand<Long> {

		public JPEGQuality() {
			super(EdsPropertyID.kEdsPropID_JpegQuality);
		}

	}

	public static class ColorTemperature extends GetPropertyCommand<Long> {

		public ColorTemperature() {
			super(EdsPropertyID.kEdsPropID_ColorTemperature);
		}

	}

	public static class WhiteBalanceShift extends GetPropertyCommand<int[]> {

		public WhiteBalanceShift() {
			super(EdsPropertyID.kEdsPropID_WhiteBalanceShift);
		}

	}

	public static class ParameterSet extends GetPropertyCommand<Long> {

		public ParameterSet() {
			super(EdsPropertyID.kEdsPropID_ParameterSet);
		}

	}

	public static class PictureStyleDescription extends GetPropertyCommand<EdsPictureStyleDesc> {

		public PictureStyleDescription() {
			super(EdsPropertyID.kEdsPropID_PictureStyleDesc);
		}

	}

	public static class MovieShootingStatus extends GetPropertyCommand<Long> {

		public MovieShootingStatus() {
			super(EdsPropertyID.kEdsPropID_Record);
		}

	}

	public static class LiveViewOutputDevice extends GetPropertyCommand<EdsEvfOutputDevice> {

		public LiveViewOutputDevice() {
			super(EdsPropertyID.kEdsPropID_Evf_OutputDevice, EdsEvfOutputDevice.class);
		}

	}

	// true if live view enabled, false if disabled
	public static class LiveViewMode extends GetPropertyCommand<Boolean> {

		public LiveViewMode() {
			super(EdsPropertyID.kEdsPropID_Evf_Mode, Boolean.class);
		}

	}

	public static class LiveViewColorTemperature extends GetPropertyCommand<Long> {

		public LiveViewColorTemperature() {
			super(EdsPropertyID.kEdsPropID_Evf_ColorTemperature);
		}

	}

	// true if preview on, false if off
	public static class LiveViewDepthOfFieldInPreview extends GetPropertyCommand<Boolean> {

		public LiveViewDepthOfFieldInPreview() {
			super(EdsPropertyID.kEdsPropID_Evf_DepthOfFieldPreview, Boolean.class);
		}

	}

	public static class DriveMode extends GetPropertyCommand<EdsDriveMode> {

		public DriveMode() {
			super(EdsPropertyID.kEdsPropID_DriveMode, EdsDriveMode.class);
		}

	}

	public static class ISOSpeed extends GetPropertyCommand<EdsISOSpeed> {

		public ISOSpeed() {
			super(EdsPropertyID.kEdsPropID_ISOSpeed, EdsISOSpeed.class);
		}

	}

	public static class MeteringMode extends GetPropertyCommand<EdsMeteringMode> {

		public MeteringMode() {
			super(EdsPropertyID.kEdsPropID_MeteringMode, EdsMeteringMode.class);
		}

	}

	/**
	 * AutoFocusMode = AFMode
	 * 
	 */
	public static class AutoFocusMode extends GetPropertyCommand<EdsAFMode> {

		public AutoFocusMode() {
			super(EdsPropertyID.kEdsPropID_AFMode, EdsAFMode.class);
		}

	}

	/**
	 * ApertureValue = Av
	 * 
	 */
	public static class ApertureValue extends GetPropertyCommand<EdsAv> {

		public ApertureValue() {
			super(EdsPropertyID.kEdsPropID_Av, EdsAv.class);
		}

	}

	/**
	 * ShutterSpeed = Tv
	 * 
	 */
	public static class ShutterSpeed extends GetPropertyCommand<EdsTv> {

		public ShutterSpeed() {
			super(EdsPropertyID.kEdsPropID_Tv, EdsTv.class);
		}

	}

	public static class ExposureCompensation extends GetPropertyCommand<EdsExposureCompensation> {

		public ExposureCompensation() {
			super(EdsPropertyID.kEdsPropID_ExposureCompensation, EdsExposureCompensation.class);
		}

	}

	/**
	 * ShootingMode = AEMode
	 * 
	 */
	public static class ShootingMode extends GetPropertyCommand<EdsAEMode> {

		public ShootingMode() {
			super(EdsPropertyID.kEdsPropID_AEMode, EdsAEMode.class);
		}

	}

	public static class ImageQuality extends GetPropertyCommand<EdsImageQuality> {

		public ImageQuality() {
			super(EdsPropertyID.kEdsPropID_ImageQuality, EdsImageQuality.class);
		}

	}

	public static class WhiteBalance extends GetPropertyCommand<EdsWhiteBalance> {

		public WhiteBalance() {
			super(EdsPropertyID.kEdsPropID_WhiteBalance, EdsWhiteBalance.class);
		}

	}

	public static class ColorSpace extends GetPropertyCommand<EdsColorSpace> {

		public ColorSpace() {
			super(EdsPropertyID.kEdsPropID_ColorSpace, EdsColorSpace.class);
		}

	}

	public static class PictureStyle extends GetPropertyCommand<EdsPictureStyle> {

		public PictureStyle() {
			super(EdsPropertyID.kEdsPropID_PictureStyle, EdsPictureStyle.class);
		}

	}

	/**
	 * LiveViewWhiteBalance = Evf_WhiteBalance
	 * 
	 */
	public static class LiveViewWhiteBalance extends GetPropertyCommand<EdsWhiteBalance> {

		public LiveViewWhiteBalance() {
			super(EdsPropertyID.kEdsPropID_Evf_WhiteBalance, EdsWhiteBalance.class);
		}

	}

	/**
	 * LiveViewAutoFocusMode = Evf_AFMode
	 * 
	 */
	public static class LiveViewAutoFocusMode extends GetPropertyCommand<EdsEvfAFMode> {

		public LiveViewAutoFocusMode() {
			super(EdsPropertyID.kEdsPropID_Evf_AFMode, EdsEvfAFMode.class);
		}

	}

	/**
	 * although EDSDK API v2.13.2 lists this, it seems not to work any more, so
	 * use the LiveViewHistogramY/R/G/B methods instead
	 * 
	 */
	public static class LiveViewHistogram extends GetPropertyCommand<int[]> {

		public LiveViewHistogram() {
			super(EdsPropertyID.kEdsPropID_Evf_Histogram, true);
		}

	}

	public static class LiveViewHistogramY extends GetPropertyCommand<int[]> {

		public LiveViewHistogramY() {
			super(EdsPropertyID.kEdsPropID_Evf_HistogramY, true);
		}

	}

	public static class LiveViewHistogramR extends GetPropertyCommand<int[]> {

		public LiveViewHistogramR() {
			super(EdsPropertyID.kEdsPropID_Evf_HistogramR, true);
		}

	}

	public static class LiveViewHistogramG extends GetPropertyCommand<int[]> {

		public LiveViewHistogramG() {
			super(EdsPropertyID.kEdsPropID_Evf_HistogramG, true);
		}

	}

	public static class LiveViewHistogramB extends GetPropertyCommand<int[]> {

		public LiveViewHistogramB() {
			super(EdsPropertyID.kEdsPropID_Evf_HistogramB, true);
		}

	}

	public static class LiveViewZoomRatio extends GetPropertyCommand<EdsEvfZoom> {

		public LiveViewZoomRatio() {
			super(EdsPropertyID.kEdsPropID_Evf_Zoom, EdsEvfZoom.class, true);
		}

	}

	public static class LiveViewHistogramStatus extends GetPropertyCommand<EdsEvfHistogramStatus> {

		public LiveViewHistogramStatus() {
			super(EdsPropertyID.kEdsPropID_Evf_HistogramStatus, EdsEvfHistogramStatus.class, true);
		}

	}

	public static class LiveViewCoordinateSystem extends GetPropertyCommand<EdsSize> {

		public LiveViewCoordinateSystem() {
			super(EdsPropertyID.kEdsPropID_Evf_CoordinateSystem, EdsSize.class, true);
		}

	}

	public static class LiveViewZoomPosition extends GetPropertyCommand<EdsPoint> {

		public LiveViewZoomPosition() {
			super(EdsPropertyID.kEdsPropID_Evf_ZoomPosition, true);
		}

	}

	public static class LiveViewZoomRectangle extends GetPropertyCommand<EdsRect> {

		public LiveViewZoomRectangle() {
			super(EdsPropertyID.kEdsPropID_Evf_ZoomRect, EdsRect.class, true);
		}

	}

	public static class LiveViewCropPosition extends GetPropertyCommand<EdsPoint> {

		public LiveViewCropPosition() {
			super(EdsPropertyID.kEdsPropID_Evf_ImagePosition, true);
		}

	}

	public static class LiveViewCropRectangle extends GetPropertyCommand<int[]> {

		public LiveViewCropRectangle() {
			super(EdsPropertyID.kEdsPropID_Evf_ImageClipRect, true);
		}

	}

}
