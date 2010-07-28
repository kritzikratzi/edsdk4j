package old;

import com.sun.jna.Structure;

public interface EDSDKTypes {
	/******************************************************************************
	*                                                                             *
	*   PROJECT : EOS Digital Software Development Kit EDSDK                      *
	*      NAME : EdsTypes.h                                                      *
	*                                                                             *
	*   Description: COMMON DEFINITION FOR EDSDK                                  *
	*                                                                             *
	*******************************************************************************
	*                                                                             *
	*   Written and developed by Canon Inc.                                       *
	*   Copyright Canon Inc. 2006-2008 All Rights Reserved                        *
	*                                                                             *
	*******************************************************************************
	*   File Update Information:                                                  *
	*     DATE      Identify    Comment                                           *
	*   -----------------------------------------------------------------------   *
	*   06-03-16    F-001       create first version.                             *
	*                                                                             *
	******************************************************************************/

	/*----------------------------------------------------------------------------*/


	/******************************************************************************
	 Definition of Constants
	******************************************************************************/
	public static int EDS_MAX_NAME            = 256; 
	public static int EDS_TRANSFER_BLOCK_SIZE = 512; 

	/******************************************************************************
	 Definition of Data Types
	******************************************************************************/
	/*-----------------------------------------------------------------------------
	 Callback Types
	-----------------------------------------------------------------------------*/
	// not needed

	/*-----------------------------------------------------------------------------
	 Error Types
	-----------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------
	 Reference Types
	-----------------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------------
	 Data Types
	-----------------------------------------------------------------------------*/
	public static class EdsDataType extends Structure
	{
	    public final static long kEdsDataType_Unknown         = 0,
	    kEdsDataType_Bool            = 1,
	    kEdsDataType_String          = 2,
	    kEdsDataType_Int8            = 3,
	    kEdsDataType_UInt8           = 6,
	    kEdsDataType_Int16           = 4,
	    kEdsDataType_UInt16          = 7,
	    kEdsDataType_Int32           = 8,
	    kEdsDataType_UInt32          = 9,
	    kEdsDataType_Int64           = 10,
	    kEdsDataType_UInt64          = 11,
	    kEdsDataType_Float           = 12,
	    kEdsDataType_Double          = 13,
	    kEdsDataType_ByteBlock       = 14,
	    kEdsDataType_Rational        = 20,
	    kEdsDataType_Point           = 21,
	    kEdsDataType_Rect            = 22,
	    kEdsDataType_Time            = 23,

	    kEdsDataType_Bool_Array      = 30,
	    kEdsDataType_Int8_Array      = 31,
	    kEdsDataType_Int16_Array     = 32,
	    kEdsDataType_Int32_Array     = 33,
	    kEdsDataType_UInt8_Array     = 34,
	    kEdsDataType_UInt16_Array    = 35,
	    kEdsDataType_UInt32_Array    = 36,
	    kEdsDataType_Rational_Array  = 37,

	    kEdsDataType_FocusInfo        = 101; 
	    
	    public long kEdsDataType_PictureStyleDesc; 

	} ;


	/*-----------------------------------------------------------------------------
	 Property IDs
	-----------------------------------------------------------------------------*/

	/*----------------------------------
	 Camera Setting Properties
	----------------------------------*/
	public final static long kEdsPropID_Unknown                 = 0x0000ffffL;

	public final static long kEdsPropID_ProductName             = 0x00000002L;
	public final static long kEdsPropID_BodyID                  = 0x00000003L;
	public final static long kEdsPropID_OwnerName               = 0x00000004l;
	public final static long kEdsPropID_MakerName               = 0x00000005L;
	public final static long kEdsPropID_DateTime                = 0x00000006L;
	public final static long kEdsPropID_FirmwareVersion         = 0x00000007L;
	public final static long kEdsPropID_BatteryLevel            = 0x00000008L;
	public final static long kEdsPropID_CFn                     = 0x00000009L;
	public final static long kEdsPropID_SaveTo                  = 0x0000000bL;
	public final static long kEdsPropID_CurrentStorage          = 0x0000000cL;
	public final static long kEdsPropID_CurrentFolder           = 0x0000000dL;
	public final static long kEdsPropID_MyMenu		           = 0x0000000eL;

	public final static long kEdsPropID_BatteryQuality          = 0x00000010L;

	public final static long kEdsPropID_HDDirectoryStructure    = 0x00000020L;


	/*----------------------------------
	 Image Properties
	----------------------------------*/
	public final static long kEdsPropID_ImageQuality            = 0x00000100L;
	public final static long kEdsPropID_JpegQuality             = 0x00000101L;
	public final static long kEdsPropID_Orientation             = 0x00000102L;
	public final static long kEdsPropID_ICCProfile              = 0x00000103L;
	public final static long kEdsPropID_FocusInfo               = 0x00000104L;
	public final static long kEdsPropID_DigitalExposure         = 0x00000105L;
	public final static long kEdsPropID_WhiteBalance            = 0x00000106L;
	public final static long kEdsPropID_ColorTemperature        = 0x00000107L;
	public final static long kEdsPropID_WhiteBalanceShift       = 0x00000108L;
	public final static long kEdsPropID_Contrast                = 0x00000109L;
	public final static long kEdsPropID_ColorSaturation         = 0x0000010aL;
	public final static long kEdsPropID_ColorTone               = 0x0000010bL;
	public final static long kEdsPropID_Sharpness               = 0x0000010cL;
	public final static long kEdsPropID_ColorSpace              = 0x0000010dL;
	public final static long kEdsPropID_ToneCurve               = 0x0000010eL;
	public final static long kEdsPropID_PhotoEffect             = 0x0000010fL;
	public final static long kEdsPropID_FilterEffect            = 0x00000110L;
	public final static long kEdsPropID_ToningEffect            = 0x00000111L;
	public final static long kEdsPropID_ParameterSet            = 0x00000112L;
	public final static long kEdsPropID_ColorMatrix             = 0x00000113L;
	public final static long kEdsPropID_PictureStyle            = 0x00000114L;
	public final static long kEdsPropID_PictureStyleDesc        = 0x00000115L;
	public final static long kEdsPropID_ETTL2Mode			   = 0x00000117L;
	public final static long kEdsPropID_PictureStyleCaption     = 0x00000200L;

	/*----------------------------------
	 Image Processing Properties
	----------------------------------*/
	public final static long kEdsPropID_Linear                  = 0x00000300L;
	public final static long kEdsPropID_ClickWBPoint            = 0x00000301L;
	public final static long kEdsPropID_WBCoeffs                = 0x00000302L;


	/*----------------------------------
	 Image GPS Properties
	----------------------------------*/
	public final static long kEdsPropID_GPSVersionID			   = 0x00000800L;
	public final static long kEdsPropID_GPSLatitudeRef		   = 0x00000801L;
	public final static long kEdsPropID_GPSLatitude			   = 0x00000802L;
	public final static long kEdsPropID_GPSLongitudeRef		   = 0x00000803L;
	public final static long kEdsPropID_GPSLongitude			   = 0x00000804L;
	public final static long kEdsPropID_GPSAltitudeRef		   = 0x00000805L;
	public final static long kEdsPropID_GPSAltitude			   = 0x00000806L;
	public final static long kEdsPropID_GPSTimeStamp			   = 0x00000807L;
	public final static long kEdsPropID_GPSSatellites		   = 0x00000808L;
	public final static long kEdsPropID_GPSStatus			   = 0x00000809L;
	public final static long kEdsPropID_GPSMapDatum			   = 0x00000812L;
	public final static long kEdsPropID_GPSDateStamp			   = 0x0000081DL;


	/*----------------------------------
	 Property Mask
	----------------------------------*/
	public final static long kEdsPropID_AtCapture_Flag          = 0x80000000L;


	/*----------------------------------
	 Capture Properties
	----------------------------------*/
	public final static long kEdsPropID_AEMode                  = 0x00000400L;
	public final static long kEdsPropID_DriveMode               = 0x00000401L;
	public final static long kEdsPropID_ISOSpeed                = 0x00000402L;
	public final static long kEdsPropID_MeteringMode            = 0x00000403L;
	public final static long kEdsPropID_AFMode                  = 0x00000404L;
	public final static long kEdsPropID_Av                      = 0x00000405L;
	public final static long kEdsPropID_Tv                      = 0x00000406L;
	public final static long kEdsPropID_ExposureCompensation    = 0x00000407L;
	public final static long kEdsPropID_FlashCompensation       = 0x00000408L;
	public final static long kEdsPropID_FocalLength             = 0x00000409L;
	public final static long kEdsPropID_AvailableShots          = 0x0000040aL;
	public final static long kEdsPropID_Bracket                 = 0x0000040bL;
	public final static long kEdsPropID_WhiteBalanceBracket     = 0x0000040cL;
	public final static long kEdsPropID_LensName                = 0x0000040dL;
	public final static long kEdsPropID_AEBracket               = 0x0000040eL;
	public final static long kEdsPropID_FEBracket               = 0x0000040fL;
	public final static long kEdsPropID_ISOBracket              = 0x00000410L;
	public final static long kEdsPropID_NoiseReduction          = 0x00000411L;
	public final static long kEdsPropID_FlashOn                 = 0x00000412L;
	public final static long kEdsPropID_RedEye                  = 0x00000413L;
	public final static long kEdsPropID_FlashMode               = 0x00000414L;
	public final static long kEdsPropID_LensStatus              = 0x00000416L;

	public final static long kEdsPropID_Artist	               = 0x00000418L;
	public final static long kEdsPropID_Copyright	           = 0x00000419L;
	public final static long kEdsPropID_DepthOfField	           = 0x0000041bL;
	public final static long kEdsPropID_EFCompensation          = 0x0000041eL;


	/*----------------------------------
	 EVF Properties
	----------------------------------*/
	public final static long kEdsPropID_Evf_OutputDevice         = 0x00000500L;
	public final static long kEdsPropID_Evf_Mode                 = 0x00000501L;
	public final static long kEdsPropID_Evf_WhiteBalance         = 0x00000502L;
	public final static long kEdsPropID_Evf_ColorTemperature     = 0x00000503L;
	public final static long kEdsPropID_Evf_DepthOfFieldPreview  = 0x00000504L;

	// EVF IMAGE DATA Properties
	public final static long kEdsPropID_Evf_Zoom                 = 0x00000507L;
	public final static long kEdsPropID_Evf_ZoomPosition         = 0x00000508L;
	public final static long kEdsPropID_Evf_FocusAid             = 0x00000509L;
	public final static long kEdsPropID_Evf_Histogram            = 0x0000050AL;
	public final static long kEdsPropID_Evf_ImagePosition        = 0x0000050BL;
	public final static long kEdsPropID_Evf_HistogramStatus      = 0x0000050CL;
	public final static long kEdsPropID_Evf_AFMode               = 0x0000050EL;

	public final static long kEdsPropID_Evf_CoordinateSystem     = 0x00000540L;
	public final static long kEdsPropID_Evf_ZoomRect             = 0x00000541L;

	/*-----------------------------------------------------------------------------
	 Camera Commands
	-----------------------------------------------------------------------------*/

	/*----------------------------------
	 Send Commands
	----------------------------------*/
	public final static long kEdsCameraCommand_TakePicture                      = 0x00000000L;
	public final static long kEdsCameraCommand_ExtendShutDownTimer              = 0x00000001L;
	public final static long kEdsCameraCommand_BulbStart						   = 0x00000002L;
	public final static long kEdsCameraCommand_BulbEnd						   = 0x00000003L;
	public final static long kEdsCameraCommand_DoEvfAf                          = 0x00000102L;
	public final static long kEdsCameraCommand_DriveLensEvf                     = 0x00000103L;
	public final static long kEdsCameraCommand_DoClickWBEvf                     = 0x00000104L;

	public final static long kEdsCameraCommand_PressShutterButton			   = 0x00000004L;

	public static class EdsEvfAf extends Structure{
		public static long kEdsCameraCommand_EvfAf_OFF		= 0;
		public static long kEdsCameraCommand_EvfAf_ON		= 1;
	} ;

	public static class EdsShutterButton extends Structure{
		public static long kEdsCameraCommand_ShutterButton_OFF					= 0x00000000;
		public static long kEdsCameraCommand_ShutterButton_Halfway				= 0x00000001;
		public static long kEdsCameraCommand_ShutterButton_Completely			= 0x00000003;
		public static long kEdsCameraCommand_ShutterButton_Halfway_NonAF		= 0x00010001;
		public static long kEdsCameraCommand_ShutterButton_Completely_NonAF	= 0x00010003;
	};



	/*----------------------------------
	 Camera Status Commands
	----------------------------------*/
	public final static long kEdsCameraStatusCommand_UILock                     = 0x00000000L;
	public final static long kEdsCameraStatusCommand_UIUnLock                   = 0x00000001L;
	public final static long kEdsCameraStatusCommand_EnterDirectTransfer        = 0x00000002L;
	public final static long kEdsCameraStatusCommand_ExitDirectTransfer         = 0x00000003L;

	/*-----------------------------------------------------------------------------
	 Camera Events
	-----------------------------------------------------------------------------*/

	/*----------------------------------
	 Property Event
	----------------------------------*/

	/* Notifies all property events. */
	public final static long kEdsPropertyEvent_All                        = 0x00000100L;

	/* Notifies that a camera property value has been changed.
	 The changed property can be retrieved from event data.
	 The changed value can be retrieved by means of EdsGetPropertyData.
	 In the case of type 1 protocol standard cameras,
	 notification of changed properties can only be issued for custom functions (CFn).
	 If the property type is 0x0000FFFF, the changed property cannot be identified.
	 Thus, retrieve all required properties repeatedly. */
	public final static long kEdsPropertyEvent_PropertyChanged            = 0x00000101L;

	/* Notifies of changes in the list of camera properties with configurable values.
	 The list of configurable values for property IDs indicated in event data
	  can be retrieved by means of EdsGetPropertyDesc.
	 For type 1 protocol standard cameras, the property ID is identified as "Unknown"
	  during notification.
	  Thus, you must retrieve a list of configurable values for all properties and
	  retrieve the property values repeatedly.
	 (For details on properties for which you can retrieve a list of configurable
	  properties,
	  see the description of EdsGetPropertyDesc). */
	public final static long kEdsPropertyEvent_PropertyDescChanged        = 0x00000102L;

	/*----------------------------------
	 Object Event
	----------------------------------*/

	/* Notifies all object events. */
	public final static long kEdsObjectEvent_All                          = 0x00000200L;

	/* Notifies that the volume object (memory card) state (VolumeInfo)
	  has been changed.
	 Changed objects are indicated by event data.
	 The changed value can be retrieved by means of EdsGetVolumeInfo.
	 Notification of this event is not issued for type 1 protocol standard cameras. */
	public final static long kEdsObjectEvent_VolumeInfoChanged            = 0x00000201L;

	/* Notifies if the designated volume on a camera has been formatted.
	 If notification of this event is received, get sub-items of the designated
	  volume again as needed.
	 Changed volume objects can be retrieved from event data.
	 Objects cannot be identified on cameras earlier than the D30
	  if files are added or deleted.
	 Thus, these events are subject to notification. */
	public final static long kEdsObjectEvent_VolumeUpdateItems            = 0x00000202L;

	/* Notifies if many images are deleted in a designated folder on a camera.
	 If notification of this event is received, get sub-items of the designated
	  folder again as needed.
	 Changed folders (specifically, directory item objects) can be retrieved
	  from event data. */
	public final static long kEdsObjectEvent_FolderUpdateItems            = 0x00000203L;

	/* Notifies of the creation of objects such as new folders or files
	  on a camera compact flash card or the like.
	 This event is generated if the camera has been set to store captured
	  images simultaneously on the camera and a computer,
	  for example, but not if the camera is set to store images
	  on the computer alone.
	 Newly created objects are indicated by event data.
	 Because objects are not indicated for type 1 protocol standard cameras,
	  (that is, objects are indicated as NULL),
	 you must again retrieve child objects under the camera object to
	 identify the new objects. */
	public final static long kEdsObjectEvent_DirItemCreated               = 0x00000204L;

	/* Notifies of the deletion of objects such as folders or files on a camera
	  compact flash card or the like.
	 Deleted objects are indicated in event data.
	 Because objects are not indicated for type 1 protocol standard cameras,
	 you must again retrieve child objects under the camera object to
	  identify deleted objects. */
	public final static long kEdsObjectEvent_DirItemRemoved               = 0x00000205L;

	/* Notifies that information of DirItem objects has been changed.
	 Changed objects are indicated by event data.
	 The changed value can be retrieved by means of EdsGetDirectoryItemInfo.
	 Notification of this event is not issued for type 1 protocol standard cameras. */
	public final static long kEdsObjectEvent_DirItemInfoChanged           = 0x00000206L;

	/* Notifies that header information has been updated, as for rotation information
	  of image files on the camera.
	 If this event is received, get the file header information again, as needed.
	 This function is for type 2 protocol standard cameras only. */
	public final static long kEdsObjectEvent_DirItemContentChanged        = 0x00000207L;

	/* Notifies that there are objects on a camera to be transferred to a computer.
	 This event is generated after remote release from a computer or local release
	  from a camera.
	 If this event is received, objects indicated in the event data must be downloaded.
	  Furthermore, if the application does not require the objects, instead
	  of downloading them,
	   execute EdsDownloadCancel and release resources held by the camera.
	 The order of downloading from type 1 protocol standard cameras must be the order
	  in which the events are received. */
	public final static long kEdsObjectEvent_DirItemRequestTransfer       = 0x00000208L;

	/* Notifies if the camera's direct transfer button is pressed.
	 If this event is received, objects indicated in the event data must be downloaded.
	 Furthermore, if the application does not require the objects, instead of
	  downloading them,
	  execute EdsDownloadCancel and release resources held by the camera.
	 Notification of this event is not issued for type 1 protocol standard cameras. */
	public final static long kEdsObjectEvent_DirItemRequestTransferDT     = 0x00000209L;

	/* Notifies of requests from a camera to cancel object transfer
	  if the button to cancel direct transfer is pressed on the camera.
	 If the parameter is 0, it means that cancellation of transfer is requested for
	  objects still not downloaded,
	  with these objects indicated by kEdsObjectEvent_DirItemRequestTransferDT.
	 Notification of this event is not issued for type 1 protocol standard cameras. */
	public final static long kEdsObjectEvent_DirItemCancelTransferDT      = 0x0000020aL;




	public final static long kEdsObjectEvent_VolumeAdded                  = 0x0000020cL;
	public final static long kEdsObjectEvent_VolumeRemoved                = 0x0000020dL;

	/*----------------------------------
	 State Event
	----------------------------------*/

	/* Notifies all state events. */
	public final static long kEdsStateEvent_All                           = 0x00000300L;

	/* Indicates that a camera is no longer connected to a computer,
	 whether it was disconnected by unplugging a cord, opening
	  the compact flash compartment,
	  turning the camera off, auto shut-off, or by other means. */
	public final static long kEdsStateEvent_Shutdown                      = 0x00000301L;

	/* Notifies of whether or not there are objects waiting to
	  be transferred to a host computer.
	 This is useful when ensuring all shot images have been transferred
	 when the application is closed.
	 Notification of this event is not issued for type 1 protocol
	 standard cameras. */
	public final static long kEdsStateEvent_JobStatusChanged              = 0x00000302L;

	/* Notifies that the camera will shut down after a specific period.
	 Generated only if auto shut-off is set.
	 Exactly when notification is issued (that is, the number of
	  seconds until shutdown) varies depending on the camera model.
	 To continue operation without having the camera shut down,
	 use EdsSendCommand to extend the auto shut-off timer.
	 The time in seconds until the camera shuts down is returned
	  as the initial value. */
	public final static long kEdsStateEvent_WillSoonShutDown              = 0x00000303L;

	/* As the counterpart event to kEdsStateEvent_WillSoonShutDown,
	 this event notifies of updates to the number of seconds until
	  a camera shuts down.
	 After the update, the period until shutdown is model-dependent. */
	public final static long kEdsStateEvent_ShutDownTimerUpdate           = 0x00000304L;

	/* Notifies that a requested release has failed, due to focus
	  failure or similar factors. */
	public final static long kEdsStateEvent_CaptureError                  = 0x00000305L;

	/* Notifies of internal SDK errors.
	 If this error event is received, the issuing device will probably
	  not be able to continue working properly,
	  so cancel the remote connection. */
	public final static long kEdsStateEvent_InternalError                 = 0x00000306L;


	public final static long kEdsStateEvent_AfResult                      = 0x00000309L;


	public final static long kEdsStateEvent_BulbExposureTime              = 0x00000310L;

	/*-----------------------------------------------------------------------------
	 Drive Lens
	-----------------------------------------------------------------------------*/
	public static class EdsEvfDriveLens{
		public static long kEdsEvfDriveLens_Near1	= 0x00000001;
		public static long kEdsEvfDriveLens_Near2	= 0x00000002;
		public static long kEdsEvfDriveLens_Near3	= 0x00000003;
		public static long kEdsEvfDriveLens_Far1	= 0x00008001;
		public static long kEdsEvfDriveLens_Far2	= 0x00008002;
		public static long kEdsEvfDriveLens_Far3	= 0x00008003;
	};



	/*-----------------------------------------------------------------------------
	 Depth of Field Preview
	-----------------------------------------------------------------------------*/
	public static class EdsEvfDepthOfFieldPreview{
		public static long kEdsEvfDepthOfFieldPreview_OFF	= 0x00000000;
		public static long kEdsEvfDepthOfFieldPreview_ON 	= 0x00000001;
	};


	/*-----------------------------------------------------------------------------
	 Stream Seek Origins
	-----------------------------------------------------------------------------*/
	public static class EdsSeekOrigin{
	    public long kEdsSeek_Cur     = 0;
	    public long kEdsSeek_Begin; 
	    public long kEdsSeek_End; 

	};

	/*-----------------------------------------------------------------------------
	 File and Propaties Access
	-----------------------------------------------------------------------------*/
	public static class EdsAccess extends Structure
	{
	    public long kEdsAccess_Read          = 0;
	    public long kEdsAccess_Write            ;
	    public long kEdsAccess_ReadWrite        ;
	    public long kEdsAccess_Error         = 0xFFFFFFFF;
	};

	/*-----------------------------------------------------------------------------
	 File Create Disposition
	-----------------------------------------------------------------------------*/
	public static class EdsFileCreateDisposition extends Structure{
		public long kEdsFileCreateDisposition_CreateNew          = 0;
		public long kEdsFileCreateDisposition_CreateAlways          ;
		public long kEdsFileCreateDisposition_OpenExisting          ;
	    public long kEdsFileCreateDisposition_OpenAlways            ;
	    public long kEdsFileCreateDisposition_TruncateExsisting     ;

	};



	/*-----------------------------------------------------------------------------
	 Image Types
	-----------------------------------------------------------------------------*/
	public static class EdsImageType extends Structure
	{
	    public static long kEdsImageType_Unknown       = 0x00000000;
	    public static long kEdsImageType_Jpeg          = 0x00000001;
	    public static long kEdsImageType_CRW           = 0x00000002;
	    public static long kEdsImageType_RAW           = 0x00000004;
	    public static long kEdsImageType_CR2           = 0x00000006;

	};

	/*-----------------------------------------------------------------------------
	 Image Size
	-----------------------------------------------------------------------------*/
	public static class EdsImageSize extends Structure
	{
		public static long kEdsImageSize_Large         = 0;
		public static long kEdsImageSize_Middle        = 1;
		public static long kEdsImageSize_Small         = 2;
		public static long kEdsImageSize_Middle1       = 5;
		public static long kEdsImageSize_Middle2       = 6;
		public static long kEdsImageSize_Unknown       = 0xffffffff;

	};

	/*-----------------------------------------------------------------------------
	 Image Compress Quality
	-----------------------------------------------------------------------------*/
	public static class EdsCompressQuality extends Structure
	{
		public static long kEdsCompressQuality_Normal     = 2;
		public static long kEdsCompressQuality_Fine       = 3;
		public static long kEdsCompressQuality_Lossless   = 4;
		public static long kEdsCompressQuality_SuperFine  = 5;
		public static long kEdsCompressQuality_Unknown    = 0xffffffff;

	};

	/*-----------------------------------------------------------------------------
	 Image Source
	-----------------------------------------------------------------------------*/
	public static class EdsImageSource extends Structure
	{
	    public static long kEdsImageSrc_FullView       = 0 ;
	    public static long kEdsImageSrc_Thumbnail          ;
	    public static long kEdsImageSrc_Preview            ;
	    public static long kEdsImageSrc_RAWThumbnail       ;
	    public static long kEdsImageSrc_RAWFullView        ;

	} ;


	/*-----------------------------------------------------------------------------
	 Target Image Types
	-----------------------------------------------------------------------------*/
	public static class EdsTargetImageType extends Structure
	{
	    public static long kEdsTargetImageType_Unknown = 0x00000000;
	    public static long kEdsTargetImageType_Jpeg    = 0x00000001;
	    public static long kEdsTargetImageType_TIFF    = 0x00000007;
	    public static long kEdsTargetImageType_TIFF16  = 0x00000008;
	    public static long kEdsTargetImageType_RGB     = 0x00000009;
	    public static long kEdsTargetImageType_RGB16   = 0x0000000A;
	    public static long kEdsTargetImageType_DIB     = 0x0000000B; 
	} ;

	/*-----------------------------------------------------------------------------
	 Progress Option
	-----------------------------------------------------------------------------*/
	public static class EdsProgressOption extends Structure
	{
	    public static long kEdsProgressOption_NoReport      = 0;
	    public static long kEdsProgressOption_Done             ;
	    public static long kEdsProgressOption_Periodically     ;

	} ;


	/*-----------------------------------------------------------------------------
	 File attribute
	-----------------------------------------------------------------------------*/
	public static class EdsFileAttributes  extends Structure
	{
	    public static long kEdsFileAttribute_Normal    = 0x00000000;
	    public static long kEdsFileAttribute_ReadOnly  = 0x00000001;
	    public static long kEdsFileAttribute_Hidden    = 0x00000002;
	    public static long kEdsFileAttribute_System    = 0x00000004;
	    public static long kEdsFileAttribute_Archive   = 0x00000020;

	} ;


	/*-----------------------------------------------------------------------------
	 Battery level
	-----------------------------------------------------------------------------*/
	public static class EdsBatteryLevel2 extends Structure
	{
	   public static long kEdsBatteryLevel2_Empty      = 0;
	   public static long kEdsBatteryLevel2_Low        = 9;
	   public static long kEdsBatteryLevel2_Half       = 49;
	   public static long kEdsBatteryLevel2_Normal     = 80;
	   public static long kEdsBatteryLevel2_Hi         = 69;
	   public static long kEdsBatteryLevel2_Quarter    = 19;
	   public static long kEdsBatteryLevel2_Error      = 0;
	   public static long kEdsBatteryLevel2_BCLevel    = 0;
	   public static long kEdsBatteryLevel2_AC         = 0xFFFFFFFF;
	} ;

	/*-----------------------------------------------------------------------------
	 Save To
	-----------------------------------------------------------------------------*/
	public static class EdsSaveTo extends Structure
	{
	    public static long kEdsSaveTo_Camera       =   1;
	    public static long kEdsSaveTo_Host         =   2;
	    public static long kEdsSaveTo_Both         =   kEdsSaveTo_Camera | kEdsSaveTo_Host;

	} ;

	/*-----------------------------------------------------------------------------
	 StorageType
	-----------------------------------------------------------------------------*/
	public static class EdsStorageType extends Structure
	{
	    public static long kEdsStorageType_Non = 0;
	    public static long kEdsStorageType_CF  = 1;
	    public static long kEdsStorageType_SD  = 2;
	    public static long kEdsStorageType_HD  = 4;

	} ;

	/*-----------------------------------------------------------------------------
	 White Balance
	-----------------------------------------------------------------------------*/
	public static class EdsWhiteBalance extends Structure
	{
	    public static long kEdsWhiteBalance_Auto         = 0;
	    public static long kEdsWhiteBalance_Daylight     = 1;
	    public static long kEdsWhiteBalance_Cloudy       = 2;
	    public static long kEdsWhiteBalance_Tangsten     = 3;
	    public static long kEdsWhiteBalance_Fluorescent  = 4;
	    public static long kEdsWhiteBalance_Strobe       = 5;
	    public static long kEdsWhiteBalance_WhitePaper   = 6;
	    public static long kEdsWhiteBalance_Shade        = 8;
	    public static long kEdsWhiteBalance_ColorTemp    = 9;
	    public static long kEdsWhiteBalance_PCSet1       = 10;
	    public static long kEdsWhiteBalance_PCSet2       = 11;
	    public static long kEdsWhiteBalance_PCSet3       = 12;
		public static long kEdsWhiteBalance_WhitePaper2  = 15;
		public static long kEdsWhiteBalance_WhitePaper3  = 16;
		public static long kEdsWhiteBalance_WhitePaper4  = 18;
		public static long kEdsWhiteBalance_WhitePaper5  = 19;
	    public static long kEdsWhiteBalance_PCSet4       = 20;
	    public static long kEdsWhiteBalance_PCSet5       = 21;
	    public static long kEdsWhiteBalance_Click        = -1;
	    public static long kEdsWhiteBalance_Pasted       = -2;

	} ;

	/*-----------------------------------------------------------------------------
	 Photo Effects
	-----------------------------------------------------------------------------*/
	public static class EdsPhotoEffect extends Structure
	{
	    public static long kEdsPhotoEffect_Off         = 0;
	    public static long kEdsPhotoEffect_Monochrome  = 5;

	} ;

	/*-----------------------------------------------------------------------------
	 Color Matrix
	-----------------------------------------------------------------------------*/
	public static class EdsColorMatrix extends Structure
	{
	    public static long kEdsColorMatrix_Custom          = 0;
	    public static long kEdsColorMatrix_1               = 1;
	    public static long kEdsColorMatrix_2               = 2;
	    public static long kEdsColorMatrix_3               = 3;
	    public static long kEdsColorMatrix_4               = 4;
	    public static long kEdsColorMatrix_5               = 5;
	    public static long kEdsColorMatrix_6               = 6;
	    public static long kEdsColorMatrix_7               = 7;

	} ;

	/*-----------------------------------------------------------------------------
	 Filter Effects
	-----------------------------------------------------------------------------*/
	public static class EdsFilterEffect extends Structure
	{
	    public static long kEdsFilterEffect_None           = 0;
	    public static long kEdsFilterEffect_Yellow         = 1;
	    public static long kEdsFilterEffect_Orange         = 2;
	    public static long kEdsFilterEffect_Red            = 3;
	    public static long kEdsFilterEffect_Green          = 4;

	} ;

	/*-----------------------------------------------------------------------------
	 Toning Effects
	-----------------------------------------------------------------------------*/
	public static class EdsTonigEffect extends Structure
	{
	    public static long kEdsTonigEffect_None            = 0;
	    public static long kEdsTonigEffect_Sepia           = 1;
	    public static long kEdsTonigEffect_Blue            = 2;
	    public static long kEdsTonigEffect_Purple          = 3;
	    public static long kEdsTonigEffect_Green           = 4;

	} ;

	/*-----------------------------------------------------------------------------
	 Color Space
	-----------------------------------------------------------------------------*/
	public static class EdsColorSpace extends Structure
	{
	    public static long kEdsColorSpace_sRGB       = 1;
	    public static long kEdsColorSpace_AdobeRGB   = 2;
	    public static long kEdsColorSpace_Unknown    = 0xffffffff;

	} ;

	/*-----------------------------------------------------------------------------
	 PictureStyle
	-----------------------------------------------------------------------------*/
	public static class EdsPictureStyle extends Structure
	{
	    public static long kEdsPictureStyle_Standard     = 0x0081;
	    public static long kEdsPictureStyle_Portrait     = 0x0082;
	    public static long kEdsPictureStyle_Landscape    = 0x0083;
	    public static long kEdsPictureStyle_Neutral      = 0x0084;
	    public static long kEdsPictureStyle_Faithful     = 0x0085;
	    public static long kEdsPictureStyle_Monochrome   = 0x0086;
	    public static long kEdsPictureStyle_User1        = 0x0021;
	    public static long kEdsPictureStyle_User2        = 0x0022;
	    public static long kEdsPictureStyle_User3        = 0x0023;
	    public static long kEdsPictureStyle_PC1          = 0x0041;
	    public static long kEdsPictureStyle_PC2          = 0x0042;
	    public static long kEdsPictureStyle_PC3          = 0x0043;

	} ;

	/*-----------------------------------------------------------------------------
	 Transfer Option
	-----------------------------------------------------------------------------*/
	public static class EdsTransferOption extends Structure
	{
	    public static long kEdsTransferOption_ByDirectTransfer    = 1;
	    public static long kEdsTransferOption_ByRelease           = 2;
	    public static long kEdsTransferOption_ToDesktop           = 0x00000100;

	} ;

	/*-----------------------------------------------------------------------------
	 AE Mode
	-----------------------------------------------------------------------------*/
	public static class EdsAEMode extends Structure
	{
	    public static long kEdsAEMode_Program          = 0 ;
	    public static long kEdsAEMode_Tv               = 1;
	    public static long kEdsAEMode_Av               = 2;
	    public static long kEdsAEMode_Manual           = 3;
	    public static long kEdsAEMode_Bulb             = 4;
	    public static long kEdsAEMode_A_DEP            = 5;
	    public static long kEdsAEMode_DEP              = 6;
	    public static long kEdsAEMode_Custom           = 7;
	    public static long kEdsAEMode_Lock             = 8;
	    public static long kEdsAEMode_Green            = 9;
	    public static long kEdsAEMode_NightPortrait    = 10;
	    public static long kEdsAEMode_Sports           = 11;
	    public static long kEdsAEMode_Portrait         = 12;
	    public static long kEdsAEMode_Landscape        = 13;
	    public static long kEdsAEMode_Closeup          = 14;
	    public static long kEdsAEMode_FlashOff         = 15;
	    public static long kEdsAEMode_CreativeAuto     = 19;
		public static long kEdsAEMode_Movie			= 20;
		public static long kEdsAEMode_PhotoInMovie		= 21;
	    public static long kEdsAEMode_Unknown          = 0xffffffff;

	} ;

	/*-----------------------------------------------------------------------------
	 Bracket
	-----------------------------------------------------------------------------*/
	public static class EdsBracket extends Structure
	{
	    public static long kEdsBracket_AEB             = 0x01;
	    public static long kEdsBracket_ISOB            = 0x02;
	    public static long kEdsBracket_WBB             = 0x04;
	    public static long kEdsBracket_FEB             = 0x08;
	    public static long kEdsBracket_Unknown         = 0xffffffff;

	} ;

	/*-----------------------------------------------------------------------------
	 EVF Output Device [Flag]
	-----------------------------------------------------------------------------*/
	public static class EdsEvfOutputDevice extends Structure
	{
		public static long kEdsEvfOutputDevice_TFT			= 1;
		public static long kEdsEvfOutputDevice_PC			= 2;
	} ;

	/*-----------------------------------------------------------------------------
	 EVF Zoom
	-----------------------------------------------------------------------------*/
	public static class EdsEvfZoom extends Structure
	{
		public static long kEdsEvfZoom_Fit			= 1;
		public static long kEdsEvfZoom_x5			= 5;
		public static long kEdsEvfZoom_x10			= 10;
	} ;

	/*-----------------------------------------------------------------------------
	 EVF AF Mode
	-----------------------------------------------------------------------------*/
	public static class EdsEvfAFMode extends Structure
	{
		public static long Evf_AFMode_Quick = 0;
		public static long Evf_AFMode_Live = 1;
		public static long Evf_AFMode_LiveFace = 2;
	} ;

	/*-----------------------------------------------------------------------------
	 Strobo Mode
	-----------------------------------------------------------------------------*/
	public static class EdsStroboMode extends Structure
	{
		public static long kEdsStroboModeInternal			= 0;
		public static long kEdsStroboModeExternalETTL		= 1;
		public static long kEdsStroboModeExternalATTL		= 2;
		public static long kEdsStroboModeExternalTTL		= 3;
		public static long kEdsStroboModeExternalAuto		= 4;
		public static long kEdsStroboModeExternalManual	= 5;
		public static long kEdsStroboModeManual			= 6;
	};

	/*-----------------------------------------------------------------------------
	 ETTL-II Mode
	-----------------------------------------------------------------------------*/
	public static class EdsETTL2Mode extends Structure
	{
		public static long kEdsETTL2ModeEvaluative		= 0;
		public static long kEdsETTL2ModeAverage		= 1;
	};

	/******************************************************************************
	 Definition of base Structures
	******************************************************************************/
	/*-----------------------------------------------------------------------------
	 Point
	-----------------------------------------------------------------------------*/
	public static class EdsPoint extends Structure
	{
	    public long x;
	    public long y;

	} ;

	/*-----------------------------------------------------------------------------
	 Size
	-----------------------------------------------------------------------------*/
	public static class EdsSize extends Structure
	{
	    public long    width;
	    public long    height;

	} ;

	/*-----------------------------------------------------------------------------
	 Rectangle
	-----------------------------------------------------------------------------*/
	public static class EdsRect extends Structure
	{
	    public EdsPoint    point;
	    public EdsSize     size;

	} ;

	/*-----------------------------------------------------------------------------
	 Rational
	-----------------------------------------------------------------------------*/
	public static class EdsRational extends Structure
	{
	    public long  numerator;
	    public long denominator;
	};

	/*-----------------------------------------------------------------------------
	 Time
	-----------------------------------------------------------------------------*/
	public static class EdsTime extends Structure
	{
	    public long    year;
	    public long    month;
	    public long    day;
	    public long    hour;
	    public long    minute;
	    public long    second;
	    public long    milliseconds;

	} ;

	/*-----------------------------------------------------------------------------
	 Device Info
	-----------------------------------------------------------------------------*/
	public static class EdsDeviceInfo extends Structure
	{
	    public char szPortName[] = new char[EDS_MAX_NAME ];
	    public char szDeviceDescription[] = new char[EDS_MAX_NAME ];
	    public long deviceSubType;
		public long reserved;
	} ;

	/*-----------------------------------------------------------------------------
	 Volume Info
	-----------------------------------------------------------------------------*/
	public static class EdsVolumeInfo extends Structure
	{
	    public long    storageType;
	    public EdsAccess   access;
	    public long   maxCapacity;
	    public long freeSpaceInBytes;
	    public char     szVolumeLabel[] = new char[ EDS_MAX_NAME ];

	} ;

	/*-----------------------------------------------------------------------------
	 DirectoryItem Info
	-----------------------------------------------------------------------------*/
	public static class EdsDirectoryItemInfo extends Structure
	{
	    public long    size;
	    public boolean isFolder;
	    public long    groupID;
	    public long    option;
	    public char szFileName[] = new char[ EDS_MAX_NAME ];

	} ;

	/*-----------------------------------------------------------------------------
	 Image Info
	-----------------------------------------------------------------------------*/
	public static class EdsImageInfo extends Structure
	{
	    public long    width;
	    public long    height;
	    public long    numOfComponents;
	    public long    componentDepth;
	    public EdsRect     effectiveRect;
	    public long    reserved1;
	    public long    reserved2;

	} ;

	/*-----------------------------------------------------------------------------
	 SaveImage Setting
	-----------------------------------------------------------------------------*/
	public static class EdsSaveImageSetting extends Structure
	{
	    public long        JPEGQuality;
	    public long    iccProfileStream;
	    public long        reserved;

	} ;

	/*-----------------------------------------------------------------------------
	 Property Desc
	-----------------------------------------------------------------------------*/
	public static class EdsPropertyDesc extends Structure
	{
	    public long        form;
	    public long		access;
	    public long        numElements;
	    public long        propDesc[] = new long[128];

	} ;

	/*-----------------------------------------------------------------------------
	 Picture Style Desc
	-----------------------------------------------------------------------------*/
	public static class EdsPictureStyleDesc extends Structure
	{
		public long    contrast;
	    public long    sharpness;
	    public long    saturation;
	    public long    colorTone;
	    public long    filterEffect;
	    public long    toningEffect;

	} ;

	/*-----------------------------------------------------------------------------
	 Focus Info
	-----------------------------------------------------------------------------*/
	public static class EdsFocusPoint extends Structure
	{
	    public long        valid;
		public long        selected;
	    public long        justFocus;
	    public EdsRect         rect;
	    public long        reserved;

	} ;

	public static class EdsFocusInfo extends Structure
	{
	    public EdsRect         imageRect;
	    public long        pointNumber;
	    public EdsFocusPoint   focusPoint[] = new EdsFocusPoint[128];
		public long        executeMode;

	} ;

	/*-----------------------------------------------------------------------------
	 User WhiteBalance (PC set1,2,3)/ User ToneCurve / User PictureStyle dataset
	-----------------------------------------------------------------------------*/
	public static class EdsUsersetData extends Structure
	{
	    public long        valid;
	    public long        dataSize;
	    public char[]         szCaption = new char[32];
	    public int[]        data = new int[1];

	} ;

	/*-----------------------------------------------------------------------------
	 Capacity
	-----------------------------------------------------------------------------*/
	public static class EdsCapacity extends Structure
	{
	    public long        numberOfFreeClusters;
	    public long        bytesPerSector;
	    public boolean         reset;

	} ;


	/******************************************************************************
	 Callback Functions
	******************************************************************************/
	/*-----------------------------------------------------------------------------
	 EdsProgressCallback
	-----------------------------------------------------------------------------*/
/*	typedef EdsError ( EDSCALLBACK *EdsProgressCallback )(
	                    public long            inPercent,
	                    EdsVoid *           inContext,
	                    boolean *           outCancel );

	/*-----------------------------------------------------------------------------
	 EdsCameraAddedHandler
	-----------------------------------------------------------------------------*/
/*	typedef EdsError ( EDSCALLBACK *EdsCameraAddedHandler )(
	                    EdsVoid *inContext );

	/*-----------------------------------------------------------------------------
	 EdsPropertyEventHandler
	-----------------------------------------------------------------------------*/
/*	typedef EdsError ( EDSCALLBACK *EdsPropertyEventHandler )(
	                    EdsPropertyEvent        inEvent,
	                    EdsPropertyID           inPropertyID,
	                    public long                inParam,
	                    EdsVoid *               inContext );

	/*-----------------------------------------------------------------------------
	 EdsObjectEventHandler
	-----------------------------------------------------------------------------*/
/*	typedef EdsError ( EDSCALLBACK *EdsObjectEventHandler )(
	                    EdsObjectEvent          inEvent,
	                    EdsBaseRef              inRef,
	                    EdsVoid *               inContext );

	/*-----------------------------------------------------------------------------
	 EdsStateEventHandler
	-----------------------------------------------------------------------------*/
/*	typedef EdsError ( EDSCALLBACK *EdsStateEventHandler )(
	                    EdsStateEvent           inEvent,
	                    public long                inEventData,
	                    EdsVoid *               inContext );


	/*----------------------------------------------------------------------------*/
/*	typedef EdsError EDSSTDCALL EdsReadStream (void *inContext, public long  inReadSize, EdsVoid* outBuffer, public long * outReadSize);
	typedef EdsError EDSSTDCALL EdsWriteStream (void *inContext, public long  inWriteSize, const EdsVoid* inBuffer, public long * outWrittenSize);
	typedef EdsError EDSSTDCALL EdsSeekStream (void *inContext, long inSeekOffset, Origin inSeekOrigin);
	typedef EdsError EDSSTDCALL EdsTellStream (void *inContext, long *outPosition);
	typedef EdsError EDSSTDCALL EdsGetStreamLength (void *inContext, public long  *outLength);
*//*
	public static class EdsIStream extends Structure
	{
	    //void              *context;

	    EdsReadStream       *read;
	    EdsWriteStream      *write;
	    EdsSeekStream       *seek;
	    EdsTellStream       *tell;
	    EdsGetStreamLength  *getLength;
	} ;
*/
}
