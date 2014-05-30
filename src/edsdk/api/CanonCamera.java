package edsdk.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;

import edsdk.api.commands.DriveLensCommand;
import edsdk.api.commands.FocusModeCommand;
import edsdk.api.commands.GetPropertyCommand;
import edsdk.api.commands.GetPropertyDescCommand;
import edsdk.api.commands.LiveViewCommand;
import edsdk.api.commands.SetPropertyCommand;
import edsdk.api.commands.ShootCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraListRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsAEMode;
import edsdk.utils.CanonConstant.EdsAv;
import edsdk.utils.CanonConstant.EdsColorSpace;
import edsdk.utils.CanonConstant.EdsCustomFunction;
import edsdk.utils.CanonConstant.EdsDriveMode;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsEvfDriveLens;
import edsdk.utils.CanonConstant.EdsEvfOutputDevice;
import edsdk.utils.CanonConstant.EdsEvfZoom;
import edsdk.utils.CanonConstant.EdsExposureCompensation;
import edsdk.utils.CanonConstant.EdsFilterEffect;
import edsdk.utils.CanonConstant.EdsISOSpeed;
import edsdk.utils.CanonConstant.EdsImageQuality;
import edsdk.utils.CanonConstant.EdsMeteringMode;
import edsdk.utils.CanonConstant.EdsObjectEvent;
import edsdk.utils.CanonConstant.EdsPictureStyle;
import edsdk.utils.CanonConstant.EdsPropertyID;
import edsdk.utils.CanonConstant.EdsSaveTo;
import edsdk.utils.CanonConstant.EdsTonigEffect;
import edsdk.utils.CanonConstant.EdsTv;
import edsdk.utils.CanonConstant.EdsWhiteBalance;
import edsdk.utils.CanonUtils;

/**
 * This class should be the easiest way to use the canon sdk.
 * Please note that you _can_ use the sdk directly or also
 * use this class to get the basic communication running, and then
 * communicate with the edsdk directly.
 * 
 * Either way, one of the most important things to remember is that
 * edsdk is not multithreaded so your vm might crash if you just call functions
 * from the library.
 * Instead I suggest you use the static method SLR.invoke( Runnable r );
 * or the method canonCamera.invoke( CanonCommand cmd );
 * 
 * The latter is basically the same, but allows you to easily get a return
 * integer value, like:
 * 
 * <pre>
 * int result = SLR.invoke(
 *     new CanonCommand() {
 *         public int run() {
 *             return ...;
 *         }
 *     }
 * );
 * </pre>
 * 
 * This class also automatically processes and forwards all windows-style
 * messages.
 * This is required to forward camera events into the edsdk. Currently there is
 * no way to disable this if it conflicts with your software.
 * 
 * Copyright U+00A9 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 * @author Ananta Palani
 */
public class CanonCamera implements EdsObjectEventHandler {

    public static final Map<String, Integer> options = new LinkedHashMap<String, Integer>();
    public static final String edsdkDllLoc;

    static {
        URL url = null;

        try {
            url = CanonCamera.class.getProtectionDomain().getCodeSource().getLocation();
        }
        catch ( final Exception e ) {
            url = null;
        }
        if ( url == null ) {
            try {
                url = CanonCamera.class.getResource( CanonCamera.class.getSimpleName() +
                                                     ".class" );
                url = new URL( url.getPath().substring( 0, url.getPath().indexOf( '!' ) ) );
            }
            catch ( final Exception e ) {
                url = null;
            }
        }
        if ( url != null ) {
            try {
                final File file = new File( url.toURI() );
                final String dir = file.getParentFile().getPath();
                System.setProperty( "jna.library.path", dir );
                //System.out.println("jna.library.path: "+dir);
            }
            catch ( final Exception e ) {
                e.printStackTrace();
            }
        }
        CanonCamera.options.put( Library.OPTION_CALLING_CONVENTION, StdCallLibrary.STDCALL_CONVENTION );

        String arch = System.getProperty( "os.arch" );
        if ( arch == null ) {
            arch = System.getProperty( "com.ibm.vm.bitmode" );
        }

        if ( arch != null && arch.endsWith( "64" ) ) {
            edsdkDllLoc = "EDSDK_64/EDSDK.dll";
        } else {
            edsdkDllLoc = "EDSDK/Dll/EDSDK.dll";
        }
        System.err.println( "Java Architecture: " + arch +
                            " - Using EDSDK DLL: " + CanonCamera.edsdkDllLoc );
    }

    // This gives you direct access to the EDSDK
    public static final EdSdkLibrary EDSDK = (EdSdkLibrary) Native.loadLibrary( CanonCamera.edsdkDllLoc, EdSdkLibrary.class, CanonCamera.options );

    // Libraries needed to forward windows messages
    private static final User32 lib = User32.INSTANCE;
    //private static final HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle("");

    // The queue of commands that need to be run. 
    private static ConcurrentLinkedQueue<CanonCommand<?>> queue = new ConcurrentLinkedQueue<CanonCommand<?>>();

    // Object Event Handlers
    private static ArrayList<EdsObjectEventHandler> objectEventHandlers = new ArrayList<EdsObjectEventHandler>( 10 );

    private static Thread dispatcherThread;
    static {
        // Tells the app to throw an error instead of crashing entirely. 
        // Native.setProtected( true ); 
        // We actually want our apps to crash, because something very dramatic 
        // is going on when the user receives this kind of crash message from 
        // the os and it puts the developer under pressure to fix the issue. 
        // If we enable Native.setProtected the app might just freeze, 
        // which is imho more annoying than a proper crash. 
        // Anyways, if you want the exception-throwing-instead-crashing behaviour
        // just call the above code as early as possible in your main method. 

        // Start the dispatch thread
        CanonCamera.dispatcherThread = new Thread() {

            @Override
            public void run() {
                CanonCamera.dispatchMessages();
            }
        };
        CanonCamera.dispatcherThread.start();

        // people are sloppy! 
        // so we add a shutdown hook to close camera connections
        Runtime.getRuntime().addShutdownHook( new Thread() {

            @Override
            public void run() {
                CanonCamera.close();
            }
        } );
    }

    /////////////////////////////////////////////
    // From here on it's instance variables

    private EdsCameraRef edsCamera;

    private String errorMessage;
    private EdsError errorCode;

    public CanonCamera() {}

    public boolean openSession() {
        final Boolean result = executeNow( new OpenSessionCommand() );
        return result != null && result;
    }

    public boolean closeSession() {
        final Boolean result = executeNow( new CloseSessionCommand() );
        return result != null && result;
    }

    public EdsCameraRef getEdsCamera() {
        return edsCamera;
    }

    public ShootCommand shoot() {
        return execute( new ShootCommand() );
    }

    public ShootCommand shoot( final EdsSaveTo saveTo ) {
        return execute( new ShootCommand( saveTo ) );
    }

    public ShootCommand shoot( final EdsSaveTo saveTo, final int shotAttempts ) {
        return execute( new ShootCommand( saveTo, shotAttempts ) );
    }

    public ShootCommand shoot( final EdsSaveTo saveTo, final int shotAttempts,
                               final File dest ) {
        return execute( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public ShootCommand shoot( final EdsSaveTo saveTo, final int shotAttempts,
                               final File[] dest ) {
        return execute( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public ShootCommand shoot( final EdsSaveTo saveTo, final int shotAttempts,
                               final File[] dest,
                               final boolean appendFileExtension ) {
        return execute( new ShootCommand( saveTo, shotAttempts, dest, appendFileExtension ) );
    }

    /**
     * It's better to use the specific getX/setX method instead
     * 
     * @param property
     * @param value
     * @return
     */
    public SetPropertyCommand.EnumData setProperty( final EdsPropertyID property,
                                                    final DescriptiveEnum<? extends Number> value ) {
        return execute( new SetPropertyCommand.EnumData( property, value ) );
    }

    // some external users of edsdk4j (like Matlab) don't realize they can use setProperty(EdsPropertyID, long) with Integer values
    public SetPropertyCommand.Data setProperty( final EdsPropertyID property,
                                                final int value ) {
        return execute( new SetPropertyCommand.Data( property, value ) );
    }

    public SetPropertyCommand.Data setProperty( final EdsPropertyID property,
                                                final long value ) {
        return execute( new SetPropertyCommand.Data( property, value ) );
    }

    public GetPropertyCommand.Data getProperty( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Data( property ) );
    }

    public GetPropertyCommand.Size getPropertySize( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Size( property ) );
    }

    public GetPropertyCommand.Type getPropertyType( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Type( property ) );
    }

    public GetPropertyDescCommand getPropertyDesc( final EdsPropertyID property ) {
        return execute( new GetPropertyDescCommand( property ) );
    }

    public <T extends CanonCommand<?>> T execute( final T cmd ) {
        cmd.setCamera( this );
        CanonCamera.queue.add( cmd );
        return cmd;
    }

    public <T> T executeNow( final CanonCommand<T> cmd ) {
        if ( CanonCamera.dispatcherThread != null &&
             CanonCamera.dispatcherThread.isAlive() ) {
            return execute( cmd ).get();
        }
        return null;
    }

    public boolean setError( final EdsError result, final String message ) {
        errorCode = result;
        errorMessage = message + " (error " + errorCode.value() + ": " +
                       errorCode.name() + " - " + errorCode.description() + ")";

        System.err.println( errorMessage );

        return false;
    }

    public EdsError getLastError() {
        return errorCode;
    }

    public String getLastErrorMessage() {
        return errorMessage;
    }

    /**
     * Adds an object event handler
     */
    public void addObjectEventHandler( final EdsObjectEventHandler handler ) {
        CanonCamera.objectEventHandlers.add( handler );
    }

    /**
     * Removes an object event handler
     */
    public void removeObjectEventHandler( final EdsObjectEventHandler handler ) {
        CanonCamera.objectEventHandlers.remove( handler );
    }

    @Override
    public NativeLong apply( final NativeLong inEvent, final EdsBaseRef inRef,
                             final Pointer inContext ) {
        /*
         * final EdsObjectEvent event = EdsObjectEvent.enumOfValue(
         * inEvent.intValue() );
         * System.out.println( "Event " + event.value() + ": " + event.name() +
         * " - " + event.description() + ", " + inContext );
         */

        for ( final EdsObjectEventHandler handler : CanonCamera.objectEventHandlers ) {
            handler.apply( inEvent, inRef, inContext );
        }

        return new NativeLong( 0 );
    }

    /**
     * Dispatches windows messages and executes commands
     */
    private static void dispatchMessages() {
        // Do some initializing
        final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsInitializeSDK() );
        if ( err != EdsError.EDS_ERR_OK ) {
            System.err.println( "EDSDK failed to initialize, most likely you won't be able to speak to your camera (ERROR: " +
                                err.description() + " )" );
        }

        final MSG msg = new MSG();

        CanonCommand<?> cmd = null;

        while ( !Thread.currentThread().isInterrupted() ) {
            // do we have a new message? 
            final boolean hasMessage = CanonCamera.lib.PeekMessage( msg, null, 0, 0, 1 ); // peek and remove
            if ( hasMessage ) {
                CanonCamera.lib.TranslateMessage( msg );
                CanonCamera.lib.DispatchMessage( msg );
            }

            // is there a command we're currently working on? 
            if ( cmd != null ) {
                if ( cmd.finished() ) {
                    //System.out.println( "Command finished" );
                    // great!
                    cmd.camera.removeObjectEventHandler( cmd );
                    cmd = null;
                }
            }

            // are we free to do new work, and is there even new work to be done? 
            if ( !CanonCamera.queue.isEmpty() && cmd == null ) {
                cmd = CanonCamera.queue.poll();
                /*
                 * System.out.println( "\nReceived new command, processing " +
                 * cmd.getClass().getCanonicalName().substring(
                 * cmd.getClass().getPackage().getName().length() + 1 ) );
                 */
                if ( ! ( cmd instanceof OpenSessionCommand ) ) {
                    cmd.camera.addObjectEventHandler( cmd );
                }
                cmd.run();
                cmd.ran();
            }

            try {
                Thread.sleep( 1 );
            }
            catch ( final InterruptedException e ) {
                System.out.println( "\nInterrupt received in CanonCamera, stopping..." );
                Thread.currentThread().interrupt(); // restore interrupted status
                break;
            }
        }

        CanonCamera.EDSDK.EdsTerminateSDK();
        System.out.println( "EDSDK Dispatcher thread says bye!" );
    }

    public static void close() {
        if ( CanonCamera.dispatcherThread != null &&
             CanonCamera.dispatcherThread.isAlive() ) {
            CanonCamera.dispatcherThread.interrupt();

            try {
                CanonCamera.dispatcherThread.join();
            }
            catch ( final InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    private class OpenSessionCommand extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( connect() );
        }

        private boolean connect() {
            EdsError err;

            final EdsCameraListRef.ByReference list = new EdsCameraListRef.ByReference();
            err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetCameraList( list ) );
            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Camera failed to initialize" );
            }

            final NativeLongByReference outRef = new NativeLongByReference();
            err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildCount( list.getValue(), outRef ) );
            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Number of attached cameras couldn't be read" );
            }

            final long numCams = outRef.getValue().longValue();
            if ( numCams <= 0 ) {
                return setError( EdsError.EDS_ERR_DEVICE_NOT_FOUND, "No cameras found. Have you tried turning it off and on again?" );
            }

            final EdsCameraRef.ByReference cameras = new EdsCameraRef.ByReference();
            err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildAtIndex( list.getValue(), new NativeLong( 0 ), cameras ) );
            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Access to camera failed" );
            }

            final Pointer context = new Pointer( 0 );
            edsCamera = new EdsCameraRef( cameras.getValue().getPointer() );
            err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSetObjectEventHandler( edsCamera, new NativeLong( EdsObjectEvent.kEdsObjectEvent_All.value() ), CanonCamera.this, context ) );
            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Callback handler couldn't be added" );
            }

            err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsOpenSession( edsCamera ) );
            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Couldn't open camera session" );
            }

            return true;
        }

    }

    private class CloseSessionCommand extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( close() );
        }

        private boolean close() {
            //System.out.println( "closing session" );
            final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsCloseSession( edsCamera ) );

            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Couldn't close camera session" );
            }

            return true;
        }
    }

    public LiveViewCommand.Begin beginLiveView() {
        return execute( new LiveViewCommand.Begin() );
    }

    public LiveViewCommand.End endLiveView() {
        return execute( new LiveViewCommand.End() );
    }

    public LiveViewCommand.Download downloadLiveView() {
        return execute( new LiveViewCommand.Download() );
    }

    /**
     * This only checks whether live view is allowed to be activated (in the
     * context of this program, this will always be true when using LiveView,
     * but the camera remembers this state even after being switched off, so
     * another program may have left the live view system enabled (even if the
     * shutter is not actually open), so use
     * {@link CanonCamera#isLiveViewActive()} to be certain live view is active
     * and transmitting images
     * 
     * @return true if live view is allowed be active
     */
    public LiveViewCommand.IsLiveViewEnabled isLiveViewEnabled() {
        return execute( new LiveViewCommand.IsLiveViewEnabled() );
    }

    /**
     * This checks whether the live view system is currently active by
     * downloading one frame. This may return false immediately after live view
     * has been activated as it takes the camera some time to start transmitting
     * live view images, so even if {@link CanonCamera#isLiveViewEnabled()}
     * returns true, this may return false, but can be much more certain that
     * live view is actually on
     * 
     * @return true if live view currently active
     */
    public LiveViewCommand.IsLiveViewActive isLiveViewActive() {
        return execute( new LiveViewCommand.IsLiveViewActive() );
    }

    public FocusModeCommand setFocusMode( final FocusModeCommand.Mode mode ) {
        return execute( new FocusModeCommand( mode ) );
    }

    public FocusModeCommand useAutoFocus() {
        return execute( new FocusModeCommand( FocusModeCommand.Mode.AUTO ) );
    }

    public FocusModeCommand useManualFocus() {
        return execute( new FocusModeCommand( FocusModeCommand.Mode.MANUAL ) );
    }

    public DriveLensCommand driveLens( final EdsEvfDriveLens mode ) {
        return execute( new DriveLensCommand( mode ) );
    }

    public GetPropertyDescCommand.DriveMode getAvailableDriveMode() {
        return execute( new GetPropertyDescCommand.DriveMode() );
    }

    public GetPropertyDescCommand.ISOSpeed getAvailableISOSpeed() {
        return execute( new GetPropertyDescCommand.ISOSpeed() );
    }

    public GetPropertyDescCommand.MeteringMode getAvailableMeteringMode() {
        return execute( new GetPropertyDescCommand.MeteringMode() );
    }

    public GetPropertyDescCommand.AutoFocusMode getAvailableAutoFocusMode() {
        return execute( new GetPropertyDescCommand.AutoFocusMode() );
    }

    public GetPropertyDescCommand.ApertureValue getAvailableApertureValue() {
        return execute( new GetPropertyDescCommand.ApertureValue() );
    }

    public GetPropertyDescCommand.ShutterSpeed getAvailableShutterSpeed() {
        return execute( new GetPropertyDescCommand.ShutterSpeed() );
    }

    public GetPropertyDescCommand.ExposureCompensation getAvailableExposureCompensation() {
        return execute( new GetPropertyDescCommand.ExposureCompensation() );
    }

    public GetPropertyDescCommand.ShootingMode getAvailableShootingMode() {
        return execute( new GetPropertyDescCommand.ShootingMode() );
    }

    public GetPropertyDescCommand.ImageQuality getAvailableImageQuality() {
        return execute( new GetPropertyDescCommand.ImageQuality() );
    }

    public GetPropertyDescCommand.WhiteBalance getAvailableWhiteBalance() {
        return execute( new GetPropertyDescCommand.WhiteBalance() );
    }

    public GetPropertyDescCommand.ColorSpace getAvailableColorSpace() {
        return execute( new GetPropertyDescCommand.ColorSpace() );
    }

    public GetPropertyDescCommand.PictureStyle getAvailablePictureStyle() {
        return execute( new GetPropertyDescCommand.PictureStyle() );
    }

    public GetPropertyDescCommand.LiveViewWhiteBalance getAvailableLiveViewWhiteBalance() {
        return execute( new GetPropertyDescCommand.LiveViewWhiteBalance() );
    }

    public GetPropertyDescCommand.LiveViewAutoFocusMode getAvailableLiveViewAutoFocusMode() {
        return execute( new GetPropertyDescCommand.LiveViewAutoFocusMode() );
    }

    public GetPropertyCommand.DriveMode getDriveMode() {
        return execute( new GetPropertyCommand.DriveMode() );
    }

    public GetPropertyCommand.ISOSpeed getISOSpeed() {
        return execute( new GetPropertyCommand.ISOSpeed() );
    }

    public GetPropertyCommand.MeteringMode getMeteringMode() {
        return execute( new GetPropertyCommand.MeteringMode() );
    }

    public GetPropertyCommand.AutoFocusMode getAutoFocusMode() {
        return execute( new GetPropertyCommand.AutoFocusMode() );
    }

    public GetPropertyCommand.ApertureValue getApertureValue() {
        return execute( new GetPropertyCommand.ApertureValue() );
    }

    public GetPropertyCommand.ShutterSpeed getShutterSpeed() {
        return execute( new GetPropertyCommand.ShutterSpeed() );
    }

    public GetPropertyCommand.ExposureCompensation getExposureCompensation() {
        return execute( new GetPropertyCommand.ExposureCompensation() );
    }

    public GetPropertyCommand.ShootingMode getShootingMode() {
        return execute( new GetPropertyCommand.ShootingMode() );
    }

    public GetPropertyCommand.ImageQuality getImageQuality() {
        return execute( new GetPropertyCommand.ImageQuality() );
    }

    public GetPropertyCommand.WhiteBalance getWhiteBalance() {
        return execute( new GetPropertyCommand.WhiteBalance() );
    }

    public GetPropertyCommand.ColorSpace getColorSpace() {
        return execute( new GetPropertyCommand.ColorSpace() );
    }

    public GetPropertyCommand.PictureStyle getPictureStyle() {
        return execute( new GetPropertyCommand.PictureStyle() );
    }

    public GetPropertyCommand.LiveViewWhiteBalance getLiveViewWhiteBalance() {
        return execute( new GetPropertyCommand.LiveViewWhiteBalance() );
    }

    public GetPropertyCommand.LiveViewAutoFocusMode getLiveViewAutoFocusMode() {
        return execute( new GetPropertyCommand.LiveViewAutoFocusMode() );
    }

    public GetPropertyCommand.CustomFunction getCustomFunction( final EdsCustomFunction customFunction ) {
        return execute( new GetPropertyCommand.CustomFunction( customFunction ) );
    }

    public GetPropertyCommand.ProductName getProductName() {
        return execute( new GetPropertyCommand.ProductName() );
    }

    public GetPropertyCommand.DateTime getDateTime() {
        return execute( new GetPropertyCommand.DateTime() );
    }

    public GetPropertyCommand.FirmwareVersion getFirmwareVersion() {
        return execute( new GetPropertyCommand.FirmwareVersion() );
    }

    public GetPropertyCommand.BatteryLevel getBatteryLevel() {
        return execute( new GetPropertyCommand.BatteryLevel() );
    }

    public GetPropertyCommand.CurrentStorage getCurrentStorage() {
        return execute( new GetPropertyCommand.CurrentStorage() );
    }

    public GetPropertyCommand.CurrentFolder getCurrentFolder() {
        return execute( new GetPropertyCommand.CurrentFolder() );
    }

    public GetPropertyCommand.BatteryQuality getBatteryQuality() {
        return execute( new GetPropertyCommand.BatteryQuality() );
    }

    public GetPropertyCommand.BodyIDEx getBodyIDEx() {
        return execute( new GetPropertyCommand.BodyIDEx() );
    }

    public GetPropertyCommand.FocusInfo getFocusInfo() {
        return execute( new GetPropertyCommand.FocusInfo() );
    }

    public GetPropertyCommand.FlashCompensation getFlashCompensation() {
        return execute( new GetPropertyCommand.FlashCompensation() );
    }

    public GetPropertyCommand.AvailableShots getAvailableShots() {
        return execute( new GetPropertyCommand.AvailableShots() );
    }

    public GetPropertyCommand.Bracket getBracket() {
        return execute( new GetPropertyCommand.Bracket() );
    }

    public GetPropertyCommand.WhiteBalanceBracket getWhiteBalanceBracket() {
        return execute( new GetPropertyCommand.WhiteBalanceBracket() );
    }

    public GetPropertyCommand.LensStatus getLensStatus() {
        return execute( new GetPropertyCommand.LensStatus() );
    }

    public GetPropertyCommand.Artist getArtist() {
        return execute( new GetPropertyCommand.Artist() );
    }

    public GetPropertyCommand.Copyright getCopyright() {
        return execute( new GetPropertyCommand.Copyright() );
    }

    public GetPropertyCommand.OwnerName getOwnerName() {
        return execute( new GetPropertyCommand.OwnerName() );
    }

    public GetPropertyCommand.SaveTo getSaveTo() {
        return execute( new GetPropertyCommand.SaveTo() );
    }

    public GetPropertyCommand.HardDriveDirectoryStructure getHardDriveDirectoryStructure() {
        return execute( new GetPropertyCommand.HardDriveDirectoryStructure() );
    }

    public GetPropertyCommand.JPEGQuality getJPEGQuality() {
        return execute( new GetPropertyCommand.JPEGQuality() );
    }

    public GetPropertyCommand.ColorTemperature getColorTemperature() {
        return execute( new GetPropertyCommand.ColorTemperature() );
    }

    public GetPropertyCommand.WhiteBalanceShift getWhiteBalanceShift() {
        return execute( new GetPropertyCommand.WhiteBalanceShift() );
    }

    public GetPropertyCommand.ParameterSet getParameterSet() {
        return execute( new GetPropertyCommand.ParameterSet() );
    }

    public GetPropertyCommand.PictureStyleDescription getPictureStyleDescription() {
        return execute( new GetPropertyCommand.PictureStyleDescription() );
    }

    public GetPropertyCommand.MovieShootingStatus getMovieShootingStatus() {
        return execute( new GetPropertyCommand.MovieShootingStatus() );
    }

    //TODO: figure out why this generally returns null unless queried just after the output device has been changed
    public GetPropertyCommand.LiveViewOutputDevice getLiveViewOutputDevice() {
        return execute( new GetPropertyCommand.LiveViewOutputDevice() );
    }

    public GetPropertyCommand.LiveViewMode getLiveViewMode() {
        return execute( new GetPropertyCommand.LiveViewMode() );
    }

    public GetPropertyCommand.LiveViewColorTemperature getLiveViewColorTemperature() {
        return execute( new GetPropertyCommand.LiveViewColorTemperature() );
    }

    public GetPropertyCommand.LiveViewDepthOfFieldInPreview getLiveViewDepthOfFieldInPreview() {
        return execute( new GetPropertyCommand.LiveViewDepthOfFieldInPreview() );
    }

    public GetPropertyCommand.LiveViewZoomRatio getLiveViewZoomRatio() {
        return execute( new GetPropertyCommand.LiveViewZoomRatio() );
    }

    public GetPropertyCommand.LiveViewZoomPosition getLiveViewZoomPosition() {
        return execute( new GetPropertyCommand.LiveViewZoomPosition() );
    }

    public GetPropertyCommand.LiveViewHistogram getLiveViewHistogram() {
        return execute( new GetPropertyCommand.LiveViewHistogram() );
    }

    public GetPropertyCommand.LiveViewHistogramY getLiveViewHistogramY() {
        return execute( new GetPropertyCommand.LiveViewHistogramY() );
    }

    public GetPropertyCommand.LiveViewHistogramR getLiveViewHistogramR() {
        return execute( new GetPropertyCommand.LiveViewHistogramR() );
    }

    public GetPropertyCommand.LiveViewHistogramG getLiveViewHistogramG() {
        return execute( new GetPropertyCommand.LiveViewHistogramG() );
    }

    public GetPropertyCommand.LiveViewHistogramB getLiveViewHistogramB() {
        return execute( new GetPropertyCommand.LiveViewHistogramB() );
    }

    public GetPropertyCommand.LiveViewCropPosition getLiveViewCropPosition() {
        return execute( new GetPropertyCommand.LiveViewCropPosition() );
    }

    public GetPropertyCommand.LiveViewHistogramStatus getLiveViewHistogramStatus() {
        return execute( new GetPropertyCommand.LiveViewHistogramStatus() );
    }

    public GetPropertyCommand.LiveViewCoordinateSystem getLiveViewCoordinateSystem() {
        return execute( new GetPropertyCommand.LiveViewCoordinateSystem() );
    }

    public GetPropertyCommand.LiveViewZoomRectangle getLiveViewZoomRectangle() {
        return execute( new GetPropertyCommand.LiveViewZoomRectangle() );
    }

    public GetPropertyCommand.LiveViewCropRectangle getLiveViewCropRectangle() {
        return execute( new GetPropertyCommand.LiveViewCropRectangle() );
    }

    public SetPropertyCommand.CustomFunction setCustomFunction( final EdsCustomFunction customFunction,
                                                                final long value ) {
        return execute( new SetPropertyCommand.CustomFunction( customFunction, value ) );
    }

    public SetPropertyCommand.Artist setArtist( final String value ) {
        return execute( new SetPropertyCommand.Artist( value ) );
    }

    public SetPropertyCommand.Copyright setCopyright( final String value ) {
        return execute( new SetPropertyCommand.Copyright( value ) );
    }

    public SetPropertyCommand.OwnerName setOwnerName( final String value ) {
        return execute( new SetPropertyCommand.OwnerName( value ) );
    }

    public SetPropertyCommand.SaveTo setSaveTo( final EdsSaveTo value ) {
        return execute( new SetPropertyCommand.SaveTo( value ) );
    }

    public SetPropertyCommand.HardDriveDirectoryStructure setHardDriveDirectoryStructure( final String value ) {
        return execute( new SetPropertyCommand.HardDriveDirectoryStructure( value ) );
    }

    public SetPropertyCommand.ImageQuality setImageQuality( final EdsImageQuality value ) {
        return execute( new SetPropertyCommand.ImageQuality( value ) );
    }

    public SetPropertyCommand.JPEGQuality setJPEGQuality( final long value ) {
        return execute( new SetPropertyCommand.JPEGQuality( value ) );
    }

    public SetPropertyCommand.WhiteBalance setWhiteBalance( final EdsWhiteBalance value ) {
        return execute( new SetPropertyCommand.WhiteBalance( value ) );
    }

    public SetPropertyCommand.ColorTemperature setColorTemperature( final long value ) {
        return execute( new SetPropertyCommand.ColorTemperature( value ) );
    }

    public SetPropertyCommand.WhiteBalanceShift setWhiteBalanceShift( final int[] value ) {
        return execute( new SetPropertyCommand.WhiteBalanceShift( value ) );
    }

    public SetPropertyCommand.ColorSpace setColorSpace( final EdsColorSpace value ) {
        return execute( new SetPropertyCommand.ColorSpace( value ) );
    }

    public SetPropertyCommand.ParameterSet setParameterSet( final long value ) {
        return execute( new SetPropertyCommand.ParameterSet( value ) );
    }

    public SetPropertyCommand.PictureStyle setPictureStyle( final EdsPictureStyle value ) {
        return execute( new SetPropertyCommand.PictureStyle( value ) );
    }

    public SetPropertyCommand.PictureStyleDescription setPictureStyleDescription( final EdsPictureStyleDesc value ) {
        return execute( new SetPropertyCommand.PictureStyleDescription( value ) );
    }

    public SetPropertyCommand.PictureStyleDescription setPictureStyleDescription( final long contrast,
                                                                                  final long sharpness,
                                                                                  final long saturation,
                                                                                  final long colorTone,
                                                                                  final EdsFilterEffect filterEffect,
                                                                                  final EdsTonigEffect toningEffect ) {
        return execute( new SetPropertyCommand.PictureStyleDescription( contrast, sharpness, saturation, colorTone, filterEffect, toningEffect ) );
    }

    public SetPropertyCommand.DriveMode setDriveMode( final EdsDriveMode value ) {
        return execute( new SetPropertyCommand.DriveMode( value ) );
    }

    public SetPropertyCommand.ISOSpeed setISOSpeed( final EdsISOSpeed value ) {
        return execute( new SetPropertyCommand.ISOSpeed( value ) );
    }

    public SetPropertyCommand.MeteringMode setMeteringMode( final EdsMeteringMode value ) {
        return execute( new SetPropertyCommand.MeteringMode( value ) );
    }

    public SetPropertyCommand.ApertureValue setApertureValue( final EdsAv value ) {
        return execute( new SetPropertyCommand.ApertureValue( value ) );
    }

    public SetPropertyCommand.ShutterSpeed setShutterSpeed( final EdsTv value ) {
        return execute( new SetPropertyCommand.ShutterSpeed( value ) );
    }

    public SetPropertyCommand.ExposureCompensation setExposureCompensation( final EdsExposureCompensation value ) {
        return execute( new SetPropertyCommand.ExposureCompensation( value ) );
    }

    public SetPropertyCommand.MovieShootingStatus setMovieShootingStatus( final long value ) {
        return execute( new SetPropertyCommand.MovieShootingStatus( value ) );
    }

    public SetPropertyCommand.LiveViewOutputDevice setLiveViewOutputDevice( final EdsEvfOutputDevice value ) {
        return execute( new SetPropertyCommand.LiveViewOutputDevice( value ) );
    }

    public SetPropertyCommand.LiveViewMode setLiveViewMode( final boolean value ) {
        return execute( new SetPropertyCommand.LiveViewMode( value ) );
    }

    public SetPropertyCommand.LiveViewWhiteBalance setLiveViewWhiteBalance( final EdsWhiteBalance value ) {
        return execute( new SetPropertyCommand.LiveViewWhiteBalance( value ) );
    }

    public SetPropertyCommand.LiveViewColorTemperature setLiveViewColorTemperature( final long value ) {
        return execute( new SetPropertyCommand.LiveViewColorTemperature( value ) );
    }

    public SetPropertyCommand.LiveViewDepthOfFieldInPreview setLiveViewDepthOfFieldInPreview( final boolean value ) {
        return execute( new SetPropertyCommand.LiveViewDepthOfFieldInPreview( value ) );
    }

    public SetPropertyCommand.LiveViewAutoFocusMode setLiveViewAutoFocusMode( final EdsEvfAFMode value ) {
        return execute( new SetPropertyCommand.LiveViewAutoFocusMode( value ) );
    }

    public SetPropertyCommand.LiveViewZoomRatio setLiveViewZoomRatio( final EdsEvfZoom value ) {
        return execute( new SetPropertyCommand.LiveViewZoomRatio( value ) );
    }

    public SetPropertyCommand.LiveViewZoomPosition setLiveViewZoomPosition( final EdsPoint value ) {
        return execute( new SetPropertyCommand.LiveViewZoomPosition( value ) );
    }

    public SetPropertyCommand.LiveViewZoomPosition setLiveViewZoomPosition( final long x,
                                                                            final long y ) {
        return execute( new SetPropertyCommand.LiveViewZoomPosition( x, y ) );
    }

    //TODO: Test if the following can be written to.. documentation lists kEdsPropID_AEModeSelect constants, but these are no longer present in include files
    public SetPropertyCommand.ShootingMode setShootingMode( final EdsAEMode value ) {
        return execute( new SetPropertyCommand.ShootingMode( value ) );
    }

}
