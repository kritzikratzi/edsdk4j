package old;

/**
 * This is a very direct copy of EDSDKErros.h
 * @author hansi
 */
public interface EDSDKConstants {
	/******************************************************************************
	*                                                                             *
	*   PROJECT : EOS Digital Software Development Kit EDSDK                      *
	*      NAME : EDSDKErros.h                                                    *
	*                                                                             *
	*   Description: ERROR DEFINITION FOR EDSDK                                   *
	*                                                                             *
	*******************************************************************************
	*                                                                             *
	*   Written and developed by Canon Inc.                                       *
	*   Copyright Canon Inc. 2006-2007 All Rights Reserved                        *
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
	 Definition of error Codes
	******************************************************************************/
	/*-----------------------------------------------------------------------
	   ED-SDK Error Code Masks
	------------------------------------------------------------------------*/
	public final static long EDS_ISSPECIFIC_MASK                                 =0x80000000L;
	public final static long EDS_COMPONENTID_MASK                                =0x7F000000L;
	public final static long EDS_RESERVED_MASK                                   =0x00FF0000L;
	public final static long EDS_ERRORID_MASK                                    =0x0000FFFFL;

	/*-----------------------------------------------------------------------
	   ED-SDK Base Component IDs
	------------------------------------------------------------------------*/
	public final static long EDS_CMP_ID_CLIENT_COMPONENTID                       =0x01000000L;
	public final static long EDS_CMP_ID_LLSDK_COMPONENTID                        =0x02000000L;
	public final static long EDS_CMP_ID_HLSDK_COMPONENTID                        =0x03000000L;

	/*-----------------------------------------------------------------------
	   ED-SDK Functin Success Code
	------------------------------------------------------------------------*/
	public final static long EDS_ERR_OK                                          =0x00000000L;

	/*-----------------------------------------------------------------------
	   ED-SDK Generic Error IDs
	------------------------------------------------------------------------*/
	/* Miscellaneous errors */
	public final static long EDS_ERR_UNIMPLEMENTED                               =0x00000001L;
	public final static long EDS_ERR_INTERNAL_ERROR                              =0x00000002L;
	public final static long EDS_ERR_MEM_ALLOC_FAILED                            =0x00000003L;
	public final static long EDS_ERR_MEM_FREE_FAILED                             =0x00000004L;
	public final static long EDS_ERR_OPERATION_CANCELLED                         =0x00000005L;
	public final static long EDS_ERR_INCOMPATIBLE_VERSION                        =0x00000006L;
	public final static long EDS_ERR_NOT_SUPPORTED                               =0x00000007L;
	public final static long EDS_ERR_UNEXPECTED_EXCEPTION                        =0x00000008L;
	public final static long EDS_ERR_PROTECTION_VIOLATION                        =0x00000009L;
	public final static long EDS_ERR_MISSING_SUBCOMPONENT                        =0x0000000AL;
	public final static long EDS_ERR_SELECTION_UNAVAILABLE                       =0x0000000BL;

	/* File errors */
	public final static long EDS_ERR_FILE_IO_ERROR                               =0x00000020L;
	public final static long EDS_ERR_FILE_TOO_MANY_OPEN                          =0x00000021L;
	public final static long EDS_ERR_FILE_NOT_FOUND                              =0x00000022L;
	public final static long EDS_ERR_FILE_OPEN_ERROR                             =0x00000023L;
	public final static long EDS_ERR_FILE_CLOSE_ERROR                            =0x00000024L;
	public final static long EDS_ERR_FILE_SEEK_ERROR                             =0x00000025L;
	public final static long EDS_ERR_FILE_TELL_ERROR                             =0x00000026L;
	public final static long EDS_ERR_FILE_READ_ERROR                             =0x00000027L;
	public final static long EDS_ERR_FILE_WRITE_ERROR                            =0x00000028L;
	public final static long EDS_ERR_FILE_PERMISSION_ERROR                       =0x00000029L;
	public final static long EDS_ERR_FILE_DISK_FULL_ERROR                        =0x0000002AL;
	public final static long EDS_ERR_FILE_ALREADY_EXISTS                         =0x0000002BL;
	public final static long EDS_ERR_FILE_FORMAT_UNRECOGNIZED                    =0x0000002CL;
	public final static long EDS_ERR_FILE_DATA_CORRUPT                           =0x0000002DL;
	public final static long EDS_ERR_FILE_NAMING_NA                              =0x0000002EL;

	/* Directory errors */
	public final static long EDS_ERR_DIR_NOT_FOUND                               =0x00000040L;
	public final static long EDS_ERR_DIR_IO_ERROR                                =0x00000041L;
	public final static long EDS_ERR_DIR_ENTRY_NOT_FOUND                         =0x00000042L;
	public final static long EDS_ERR_DIR_ENTRY_EXISTS                            =0x00000043L;
	public final static long EDS_ERR_DIR_NOT_EMPTY                               =0x00000044L;

	/* Property errors */
	public final static long EDS_ERR_PROPERTIES_UNAVAILABLE                      =0x00000050L;
	public final static long EDS_ERR_PROPERTIES_MISMATCH                         =0x00000051L;
	public final static long EDS_ERR_PROPERTIES_NOT_LOADED                       =0x00000053L;

	/* Function Parameter errors */
	public final static long EDS_ERR_INVALID_PARAMETER                           =0x00000060L;
	public final static long EDS_ERR_INVALID_HANDLE                              =0x00000061L;
	public final static long EDS_ERR_INVALID_POINTER                             =0x00000062L;
	public final static long EDS_ERR_INVALID_INDEX                               =0x00000063L;
	public final static long EDS_ERR_INVALID_LENGTH                              =0x00000064L;
	public final static long EDS_ERR_INVALID_FN_POINTER                          =0x00000065L;
	public final static long EDS_ERR_INVALID_SORT_FN                             =0x00000066L;

	/* Device errors */
	public final static long EDS_ERR_DEVICE_NOT_FOUND                            =0x00000080L;
	public final static long EDS_ERR_DEVICE_BUSY                                 =0x00000081L;
	public final static long EDS_ERR_DEVICE_INVALID                              =0x00000082L;
	public final static long EDS_ERR_DEVICE_EMERGENCY                            =0x00000083L;
	public final static long EDS_ERR_DEVICE_MEMORY_FULL                          =0x00000084L;
	public final static long EDS_ERR_DEVICE_INTERNAL_ERROR                       =0x00000085L;
	public final static long EDS_ERR_DEVICE_INVALID_PARAMETER                    =0x00000086L;
	public final static long EDS_ERR_DEVICE_NO_DISK                              =0x00000087L;
	public final static long EDS_ERR_DEVICE_DISK_ERROR                           =0x00000088L;
	public final static long EDS_ERR_DEVICE_CF_GATE_CHANGED                      =0x00000089L;
	public final static long EDS_ERR_DEVICE_DIAL_CHANGED                         =0x0000008AL;
	public final static long EDS_ERR_DEVICE_NOT_INSTALLED                        =0x0000008BL;
	public final static long EDS_ERR_DEVICE_STAY_AWAKE                           =0x0000008CL;
	public final static long EDS_ERR_DEVICE_NOT_RELEASED                         =0x0000008DL;


	/* Stream errors */
	public final static long EDS_ERR_STREAM_IO_ERROR                             =0x000000A0L;
	public final static long EDS_ERR_STREAM_NOT_OPEN                             =0x000000A1L;
	public final static long EDS_ERR_STREAM_ALREADY_OPEN                         =0x000000A2L;
	public final static long EDS_ERR_STREAM_OPEN_ERROR                           =0x000000A3L;
	public final static long EDS_ERR_STREAM_CLOSE_ERROR                          =0x000000A4L;
	public final static long EDS_ERR_STREAM_SEEK_ERROR                           =0x000000A5L;
	public final static long EDS_ERR_STREAM_TELL_ERROR                           =0x000000A6L;
	public final static long EDS_ERR_STREAM_READ_ERROR                           =0x000000A7L;
	public final static long EDS_ERR_STREAM_WRITE_ERROR                          =0x000000A8L;
	public final static long EDS_ERR_STREAM_PERMISSION_ERROR                     =0x000000A9L;
	public final static long EDS_ERR_STREAM_COULDNT_BEGIN_THREAD                 =0x000000AAL;
	public final static long EDS_ERR_STREAM_BAD_OPTIONS                          =0x000000ABL;
	public final static long EDS_ERR_STREAM_END_OF_STREAM                        =0x000000ACL;

	/* Communications errors */
	public final static long EDS_ERR_COMM_PORT_IS_IN_USE                         =0x000000C0L;
	public final static long EDS_ERR_COMM_DISCONNECTED                           =0x000000C1L;
	public final static long EDS_ERR_COMM_DEVICE_INCOMPATIBLE                    =0x000000C2L;
	public final static long EDS_ERR_COMM_BUFFER_FULL                            =0x000000C3L;
	public final static long EDS_ERR_COMM_USB_BUS_ERR                            =0x000000C4L;

	/* Lock/Unlock */
	public final static long EDS_ERR_USB_DEVICE_LOCK_ERROR                       =0x000000D0L;
	public final static long EDS_ERR_USB_DEVICE_UNLOCK_ERROR                     =0x000000D1L;

	/* STI/WIA */
	public final static long EDS_ERR_STI_UNKNOWN_ERROR                           =0x000000E0L;
	public final static long EDS_ERR_STI_INTERNAL_ERROR                          =0x000000E1L;
	public final static long EDS_ERR_STI_DEVICE_CREATE_ERROR                     =0x000000E2L;
	public final static long EDS_ERR_STI_DEVICE_RELEASE_ERROR                    =0x000000E3L;
	public final static long EDS_ERR_DEVICE_NOT_LAUNCHED                         =0x000000E4L;

	public final static long EDS_ERR_ENUM_NA                                     =0x000000F0L;
	public final static long EDS_ERR_INVALID_FN_CALL                             =0x000000F1L;
	public final static long EDS_ERR_HANDLE_NOT_FOUND                            =0x000000F2L;
	public final static long EDS_ERR_INVALID_ID                                  =0x000000F3L;
	public final static long EDS_ERR_WAIT_TIMEOUT_ERROR                          =0x000000F4L;

	/* PTP */
	public final static long EDS_ERR_SESSION_NOT_OPEN                            =0x00002003L;
	public final static long EDS_ERR_INVALID_TRANSACTIONID                       =0x00002004L;
	public final static long EDS_ERR_INCOMPLETE_TRANSFER                         =0x00002007L;
	public final static long EDS_ERR_INVALID_STRAGEID                            =0x00002008L;
	public final static long EDS_ERR_DEVICEPROP_NOT_SUPPORTED                    =0x0000200AL;
	public final static long EDS_ERR_INVALID_OBJECTFORMATCODE                    =0x0000200BL;
	public final static long EDS_ERR_SELF_TEST_FAILED                            =0x00002011L;
	public final static long EDS_ERR_PARTIAL_DELETION                            =0x00002012L;
	public final static long EDS_ERR_SPECIFICATION_BY_FORMAT_UNSUPPORTED         =0x00002014L;
	public final static long EDS_ERR_NO_VALID_OBJECTINFO                         =0x00002015L;
	public final static long EDS_ERR_INVALID_CODE_FORMAT                         =0x00002016L;
	public final static long EDS_ERR_UNKNOWN_VENDOR_CODE                         =0x00002017L;
	public final static long EDS_ERR_CAPTURE_ALREADY_TERMINATED                  =0x00002018L;
	public final static long EDS_ERR_INVALID_PARENTOBJECT                        =0x0000201AL;
	public final static long EDS_ERR_INVALID_DEVICEPROP_FORMAT                   =0x0000201BL;
	public final static long EDS_ERR_INVALID_DEVICEPROP_VALUE                    =0x0000201CL;
	public final static long EDS_ERR_SESSION_ALREADY_OPEN                        =0x0000201EL;
	public final static long EDS_ERR_TRANSACTION_CANCELLED                       =0x0000201FL;
	public final static long EDS_ERR_SPECIFICATION_OF_DESTINATION_UNSUPPORTED    =0x00002020L;

	/* PTP Vendor */
	public final static long EDS_ERR_UNKNOWN_COMMAND                             =0x0000A001L;
	public final static long EDS_ERR_OPERATION_REFUSED                           =0x0000A005L;
	public final static long EDS_ERR_LENS_COVER_CLOSE                            =0x0000A006L;
	public final static long EDS_ERR_LOW_BATTERY									=0x0000A101L;
	public final static long EDS_ERR_OBJECT_NOTREADY								=0x0000A102L;




	public final static long EDS_ERR_TAKE_PICTURE_AF_NG                          =0x00008D01L;
	public final static long EDS_ERR_TAKE_PICTURE_RESERVED                       =0x00008D02L;
	public final static long EDS_ERR_TAKE_PICTURE_MIRROR_UP_NG                   =0x00008D03L;
	public final static long EDS_ERR_TAKE_PICTURE_SENSOR_CLEANING_NG             =0x00008D04L;
	public final static long EDS_ERR_TAKE_PICTURE_SILENCE_NG                     =0x00008D05L;
	public final static long EDS_ERR_TAKE_PICTURE_NO_CARD_NG                     =0x00008D06L;
	public final static long EDS_ERR_TAKE_PICTURE_CARD_NG                        =0x00008D07L;
	public final static long EDS_ERR_TAKE_PICTURE_CARD_PROTECT_NG                =0x00008D08L;
	public final static long EDS_ERR_TAKE_PICTURE_MOVIE_CROP_NG					=0x00008D09L;
	public final static long EDS_ERR_TAKE_PICTURE_STROBO_CHARGE_NG				=0x00008D0AL;


	public final static long EDS_ERR_LAST_GENERIC_ERROR_PLUS_ONE                 =0x000000F5L;


	/*----------------------------------------------------------------------------*/
}
