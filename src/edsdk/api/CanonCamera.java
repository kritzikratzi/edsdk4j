package edsdk.api;

import java.awt.image.BufferedImage;
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

import edsdk.api.commands.FocusModeCommand;
import edsdk.api.commands.GetPropertyDescCommand;
import edsdk.api.commands.GetPropertyCommand;
import edsdk.api.commands.LiveViewCommand;
import edsdk.api.commands.SetPropertyCommand;
import edsdk.api.commands.ShootCommand;
import edsdk.bindings.EdSdkLibrary;
import edsdk.bindings.EdsFocusInfo;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.bindings.EdsRect;
import edsdk.bindings.EdsSize;
import edsdk.bindings.EdsTime;
import edsdk.bindings.EdSdkLibrary.EdsBaseRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraListRef;
import edsdk.bindings.EdSdkLibrary.EdsCameraRef;
import edsdk.bindings.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonUtils;
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
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsEvfHistogramStatus;
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
 * or the method canonCamera.invoke( CanonTask task );
 * 
 * The latter is basically the same, but allows you to easily get a return
 * integer value,
 * like int result = SLR.invoke( new CanonTask(){ public int run(){ return ...;
 * } } );
 * 
 * 
 * This class also automatically processes and forwards all windows-style
 * messages.
 * This is required to forward camera events into the edsdk. Currently there is
 * no way to disable this if it conflicts with your software.
 * 
 * @author hansi
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
            catch ( final Exception e1 ) {
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
    public static EdSdkLibrary EDSDK = (EdSdkLibrary) Native.loadLibrary( CanonCamera.edsdkDllLoc, EdSdkLibrary.class, CanonCamera.options );

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

    public File[] shoot() {
        return executeNow( new ShootCommand() );
    }

    public File[] shoot( final EdsSaveTo saveTo ) {
        return executeNow( new ShootCommand( saveTo ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File dest ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest, final boolean appendFileExtension ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest, appendFileExtension ) );
    }

    /**
     * It's better to use the specific getX/setX method instead
     * 
     * @param property
     * @param value
     * @return
     */
    public Boolean setProperty( final EdsPropertyID property,
                                final DescriptiveEnum<? extends Number> value ) {
        return executeNow( new SetPropertyCommand.EnumData( property, value ) );
    }

    // some external users of edsdk4j (like Matlab) don't realize they can use setProperty(EdsPropertyID, long) with Integer values
    public Boolean setProperty( final EdsPropertyID property, final int value ) {
        return executeNow( new SetPropertyCommand.Data( property, value ) );
    }

    public Boolean setProperty( final EdsPropertyID property, final long value ) {
        return executeNow( new SetPropertyCommand.Data( property, value ) );
    }

    public Long getProperty( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Data( property ) );
    }

    public Long getPropertySize( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Size( property ) );
    }

    public EdsDataType getPropertyType( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Type( property ) );
    }

    public EdsPropertyDesc getPropertyDesc( final EdsPropertyID property ) {
        return executeNow( new GetPropertyDescCommand( property ) );
    }

    public <T extends CanonCommand<?>> T execute( final T cmd ) {
        cmd.setCamera( this );
        CanonCamera.queue.add( cmd );
        return cmd;
    }

    public <T> T executeNow( final CanonCommand<T> cmd ) {
        if ( CanonCamera.dispatcherThread != null &&
             CanonCamera.dispatcherThread.isAlive() ) {
            execute( cmd );
            return cmd.result();
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
        final EdsObjectEvent event = EdsObjectEvent.enumOfValue( inEvent.intValue() );
        System.out.println( "Event " + event.value() + ": " + event.name() +
                            " - " + event.description() + ", " + inContext );

        for ( final EdsObjectEventHandler handler : CanonCamera.objectEventHandlers ) {
            handler.apply( inEvent, inRef, inContext );
        }

        return new NativeLong( 0 );
    }

    /**
     * Dispatches windows messages and executes tasks
     */
    private static void dispatchMessages() {
        // Do some initializing
        final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsInitializeSDK() );
        if ( err != EdsError.EDS_ERR_OK ) {
            System.err.println( "EDSDK failed to initialize, most likely you won't be able to speak to your camera (ERROR: " +
                                err.description() + " )" );
        }

        final MSG msg = new MSG();

        CanonCommand<?> task = null;

        while ( !Thread.currentThread().isInterrupted() ) {
            // do we have a new message? 
            final boolean hasMessage = CanonCamera.lib.PeekMessage( msg, null, 0, 0, 1 ); // peek and remove
            if ( hasMessage ) {
                CanonCamera.lib.TranslateMessage( msg );
                CanonCamera.lib.DispatchMessage( msg );
            }

            // is there a command we're currently working on? 
            if ( task != null ) {
                if ( task.finished() ) {
                    System.out.println( "Command finished" );
                    // great!
                    task.camera.removeObjectEventHandler( task );
                    task = null;
                }
            }

            // are we free to do new work, and is there even new work to be done? 
            if ( !CanonCamera.queue.isEmpty() && task == null ) {
                task = CanonCamera.queue.poll();
                System.out.println( "\nReceived new command, processing " +
                                    task.getClass().getCanonicalName().substring( task.getClass().getPackage().getName().length() + 1 ) );
                if ( ! ( task instanceof OpenSessionCommand ) ) {
                    task.camera.addObjectEventHandler( task );
                }
                task.run();
                task.ran();
            }

            try {
                Thread.sleep( 10 );
            }
            catch ( final InterruptedException e ) {
                System.out.println( "\nInterrupt received in CanonCamera, stopping..." );
                Thread.currentThread().interrupt(); // restore interrupted status
                break;
            }
        }

        CanonCamera.EDSDK.EdsTerminateSDK();
        System.out.println( "Dispatcher thread says bye!" );
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
            System.out.println( "closing session" );
            final EdsError err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsCloseSession( edsCamera ) );

            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Couldn't close camera session" );
            }

            return true;
        }
    }

    public Boolean beginLiveView() {
        return executeNow( new LiveViewCommand.Begin() );
    }

    public Boolean endLiveView() {
        return executeNow( new LiveViewCommand.End() );
    }

    public BufferedImage downloadLiveView() {
        return executeNow( new LiveViewCommand.Download() );
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
    public Boolean isLiveViewEnabled() {
        return executeNow( new LiveViewCommand.IsLiveViewEnabled() );
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
    public Boolean isLiveViewActive() {
        return executeNow( new LiveViewCommand.IsLiveViewActive() );
    }

    public Boolean setFocusMode( final FocusModeCommand.Mode mode ) {
        return executeNow( new FocusModeCommand( mode ) );
    }

    public EdsDriveMode[] getAvailableDriveMode() {
        return executeNow( new GetPropertyDescCommand.DriveMode() );
    }

    public EdsISOSpeed[] getAvailableISOSpeed() {
        return executeNow( new GetPropertyDescCommand.ISOSpeed() );
    }

    public EdsMeteringMode[] getAvailableMeteringMode() {
        return executeNow( new GetPropertyDescCommand.MeteringMode() );
    }

    public EdsAFMode[] getAvailableAutoFocusMode() {
        return executeNow( new GetPropertyDescCommand.AutoFocusMode() );
    }

    public EdsAv[] getAvailableApertureValue() {
        return executeNow( new GetPropertyDescCommand.ApertureValue() );
    }

    public EdsTv[] getAvailableShutterSpeed() {
        return executeNow( new GetPropertyDescCommand.ShutterSpeed() );
    }

    public EdsExposureCompensation[] getAvailableExposureCompensation() {
        return executeNow( new GetPropertyDescCommand.ExposureCompensation() );
    }

    public EdsAEMode[] getAvailableShootingMode() {
        return executeNow( new GetPropertyDescCommand.ShootingMode() );
    }

    public EdsImageQuality[] getAvailableImageQuality() {
        return executeNow( new GetPropertyDescCommand.ImageQuality() );
    }

    public EdsWhiteBalance[] getAvailableWhiteBalance() {
        return executeNow( new GetPropertyDescCommand.WhiteBalance() );
    }

    public EdsColorSpace[] getAvailableColorSpace() {
        return executeNow( new GetPropertyDescCommand.ColorSpace() );
    }

    public EdsPictureStyle[] getAvailablePictureStyle() {
        return executeNow( new GetPropertyDescCommand.PictureStyle() );
    }

    public EdsWhiteBalance[] getAvailableLiveViewWhiteBalance() {
        return executeNow( new GetPropertyDescCommand.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode[] getAvailableLiveViewAutoFocusMode() {
        return executeNow( new GetPropertyDescCommand.LiveViewAutoFocusMode() );
    }

    public EdsDriveMode getDriveMode() {
        return executeNow( new GetPropertyCommand.DriveMode() );
    }

    public EdsISOSpeed getISOSpeed() {
        return executeNow( new GetPropertyCommand.ISOSpeed() );
    }

    public EdsMeteringMode getMeteringMode() {
        return executeNow( new GetPropertyCommand.MeteringMode() );
    }

    public EdsAFMode getAutoFocusMode() {
        return executeNow( new GetPropertyCommand.AutoFocusMode() );
    }

    public EdsAv getApertureValue() {
        return executeNow( new GetPropertyCommand.ApertureValue() );
    }

    public EdsTv getShutterSpeed() {
        return executeNow( new GetPropertyCommand.ShutterSpeed() );
    }

    public EdsExposureCompensation getExposureCompensation() {
        return executeNow( new GetPropertyCommand.ExposureCompensation() );
    }

    public EdsAEMode getShootingMode() {
        return executeNow( new GetPropertyCommand.ShootingMode() );
    }

    public EdsImageQuality getImageQuality() {
        return executeNow( new GetPropertyCommand.ImageQuality() );
    }

    public EdsWhiteBalance getWhiteBalance() {
        return executeNow( new GetPropertyCommand.WhiteBalance() );
    }

    public EdsColorSpace getColorSpace() {
        return executeNow( new GetPropertyCommand.ColorSpace() );
    }

    public EdsPictureStyle getPictureStyle() {
        return executeNow( new GetPropertyCommand.PictureStyle() );
    }

    public EdsWhiteBalance getLiveViewWhiteBalance() {
        return executeNow( new GetPropertyCommand.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode getLiveViewAutoFocusMode() {
        return executeNow( new GetPropertyCommand.LiveViewAutoFocusMode() );
    }

    public Long getCustomFunction( final EdsCustomFunction customFunction ) {
        return executeNow( new GetPropertyCommand.CustomFunction( customFunction ) );
    }

    public String getProductName() {
        return executeNow( new GetPropertyCommand.ProductName() );
    }

    public EdsTime getDateTime() {
        return executeNow( new GetPropertyCommand.DateTime() );
    }

    public String getFirmwareVersion() {
        return executeNow( new GetPropertyCommand.FirmwareVersion() );
    }

    public Long getBatteryLevel() {
        return executeNow( new GetPropertyCommand.BatteryLevel() );
    }

    public String getCurrentStorage() {
        return executeNow( new GetPropertyCommand.CurrentStorage() );
    }

    public String getCurrentFolder() {
        return executeNow( new GetPropertyCommand.CurrentFolder() );
    }

    public EdsBatteryQuality getBatteryQuality() {
        return executeNow( new GetPropertyCommand.BatteryQuality() );
    }

    public String getBodyIDEx() {
        return executeNow( new GetPropertyCommand.BodyIDEx() );
    }

    public EdsFocusInfo getFocusInfo() {
        return executeNow( new GetPropertyCommand.FocusInfo() );
    }

    public EdsExposureCompensation getFlashCompensation() {
        return executeNow( new GetPropertyCommand.FlashCompensation() );
    }

    public Long getAvailableShots() {
        return executeNow( new GetPropertyCommand.AvailableShots() );
    }

    public EdsBracket getBracket() {
        return executeNow( new GetPropertyCommand.Bracket() );
    }

    public int[] getWhiteBalanceBracket() {
        return executeNow( new GetPropertyCommand.WhiteBalanceBracket() );
    }

    public Boolean getLensStatus() {
        return executeNow( new GetPropertyCommand.LensStatus() );
    }

    public String getArtist() {
        return executeNow( new GetPropertyCommand.Artist() );
    }

    public String getCopyright() {
        return executeNow( new GetPropertyCommand.Copyright() );
    }

    public String getOwnerName() {
        return executeNow( new GetPropertyCommand.OwnerName() );
    }

    public EdsSaveTo getSaveTo() {
        return executeNow( new GetPropertyCommand.SaveTo() );
    }

    public String getHardDriveDirectoryStructure() {
        return executeNow( new GetPropertyCommand.HardDriveDirectoryStructure() );
    }

    public Long getJPEGQuality() {
        return executeNow( new GetPropertyCommand.JPEGQuality() );
    }

    public Long getColorTemperature() {
        return executeNow( new GetPropertyCommand.ColorTemperature() );
    }

    public int[] getWhiteBalanceShift() {
        return executeNow( new GetPropertyCommand.WhiteBalanceShift() );
    }

    public Long getParameterSet() {
        return executeNow( new GetPropertyCommand.ParameterSet() );
    }

    public EdsPictureStyleDesc getPictureStyleDescription() {
        return executeNow( new GetPropertyCommand.PictureStyleDescription() );
    }

    public Long getMovieShootingStatus() {
        return executeNow( new GetPropertyCommand.MovieShootingStatus() );
    }

    //TODO - figure out why this generally returns null unless queried just after the output device has been changed
    public EdsEvfOutputDevice getLiveViewOutputDevice() {
        return executeNow( new GetPropertyCommand.LiveViewOutputDevice() );
    }

    public Boolean getLiveViewMode() {
        return executeNow( new GetPropertyCommand.LiveViewMode() );
    }

    public Long getLiveViewColorTemperature() {
        return executeNow( new GetPropertyCommand.LiveViewColorTemperature() );
    }

    public Boolean getLiveViewDepthOfFieldInPreview() {
        return executeNow( new GetPropertyCommand.LiveViewDepthOfFieldInPreview() );
    }

    public EdsEvfZoom getLiveViewZoomRatio() {
        return executeNow( new GetPropertyCommand.LiveViewZoomRatio() );
    }

    public EdsPoint getLiveViewZoomPosition() {
        return executeNow( new GetPropertyCommand.LiveViewZoomPosition() );
    }

    public int[] getLiveViewHistogram() {
        return executeNow( new GetPropertyCommand.LiveViewHistogram() );
    }

    public int[] getLiveViewHistogramY() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramY() );
    }

    public int[] getLiveViewHistogramR() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramR() );
    }

    public int[] getLiveViewHistogramG() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramG() );
    }

    public int[] getLiveViewHistogramB() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramB() );
    }

    public EdsPoint getLiveViewCropPosition() {
        return executeNow( new GetPropertyCommand.LiveViewCropPosition() );
    }

    public EdsEvfHistogramStatus getLiveViewHistogramStatus() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramStatus() );
    }

    public EdsSize getLiveViewCoordinateSystem() {
        return executeNow( new GetPropertyCommand.LiveViewCoordinateSystem() );
    }

    public EdsRect getLiveViewZoomRectangle() {
        return executeNow( new GetPropertyCommand.LiveViewZoomRectangle() );
    }

    public int[] getLiveViewCropRectangle() {
        return executeNow( new GetPropertyCommand.LiveViewCropRectangle() );
    }

    public Boolean setCustomFunction( final EdsCustomFunction customFunction,
                                      final long value ) {
        return executeNow( new SetPropertyCommand.CustomFunction( customFunction, value ) );
    }

    public Boolean setArtist( final String value ) {
        return executeNow( new SetPropertyCommand.Artist( value ) );
    }

    public Boolean setCopyright( final String value ) {
        return executeNow( new SetPropertyCommand.Copyright( value ) );
    }

    public Boolean setOwnerName( final String value ) {
        return executeNow( new SetPropertyCommand.OwnerName( value ) );
    }

    public Boolean setSaveTo( final EdsSaveTo value ) {
        return executeNow( new SetPropertyCommand.SaveTo( value ) );
    }

    public Boolean setHardDriveDirectoryStructure( final String value ) {
        return executeNow( new SetPropertyCommand.HardDriveDirectoryStructure( value ) );
    }

    public Boolean setImageQuality( final EdsImageQuality value ) {
        return executeNow( new SetPropertyCommand.ImageQuality( value ) );
    }

    public Boolean setJPEGQuality( final long value ) {
        return executeNow( new SetPropertyCommand.JPEGQuality( value ) );
    }

    public Boolean setWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyCommand.WhiteBalance( value ) );
    }

    public Boolean setColorTemperature( final long value ) {
        return executeNow( new SetPropertyCommand.ColorTemperature( value ) );
    }

    public Boolean setWhiteBalanceShift( final int[] value ) {
        return executeNow( new SetPropertyCommand.WhiteBalanceShift( value ) );
    }

    public Boolean setColorSpace( final EdsColorSpace value ) {
        return executeNow( new SetPropertyCommand.ColorSpace( value ) );
    }

    public Boolean setParameterSet( final long value ) {
        return executeNow( new SetPropertyCommand.ParameterSet( value ) );
    }

    public Boolean setPictureStyle( final EdsPictureStyle value ) {
        return executeNow( new SetPropertyCommand.PictureStyle( value ) );
    }

    public Boolean setPictureStyleDescription( final EdsPictureStyleDesc value ) {
        return executeNow( new SetPropertyCommand.PictureStyleDescription( value ) );
    }

    public Boolean setPictureStyleDescription( final long contrast,
                                               final long sharpness,
                                               final long saturation,
                                               final long colorTone,
                                               final EdsFilterEffect filterEffect,
                                               final EdsTonigEffect toningEffect ) {
        return executeNow( new SetPropertyCommand.PictureStyleDescription( contrast, sharpness, saturation, colorTone, filterEffect, toningEffect ) );
    }

    public Boolean setDriveMode( final EdsDriveMode value ) {
        return executeNow( new SetPropertyCommand.DriveMode( value ) );
    }

    public Boolean setISOSpeed( final EdsISOSpeed value ) {
        return executeNow( new SetPropertyCommand.ISOSpeed( value ) );
    }

    public Boolean setMeteringMode( final EdsMeteringMode value ) {
        return executeNow( new SetPropertyCommand.MeteringMode( value ) );
    }

    public Boolean setApertureValue( final EdsAv value ) {
        return executeNow( new SetPropertyCommand.ApertureValue( value ) );
    }

    public Boolean setShutterSpeed( final EdsTv value ) {
        return executeNow( new SetPropertyCommand.ShutterSpeed( value ) );
    }

    public Boolean setExposureCompensation( final EdsExposureCompensation value ) {
        return executeNow( new SetPropertyCommand.ExposureCompensation( value ) );
    }

    public Boolean setMovieShootingStatus( final long value ) {
        return executeNow( new SetPropertyCommand.MovieShootingStatus( value ) );
    }

    public Boolean setLiveViewOutputDevice( final EdsEvfOutputDevice value ) {
        return executeNow( new SetPropertyCommand.LiveViewOutputDevice( value ) );
    }

    public Boolean setLiveViewMode( final boolean value ) {
        return executeNow( new SetPropertyCommand.LiveViewMode( value ) );
    }

    public Boolean setLiveViewWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyCommand.LiveViewWhiteBalance( value ) );
    }

    public Boolean setLiveViewColorTemperature( final long value ) {
        return executeNow( new SetPropertyCommand.LiveViewColorTemperature( value ) );
    }

    public Boolean setLiveViewDepthOfFieldInPreview( final boolean value ) {
        return executeNow( new SetPropertyCommand.LiveViewDepthOfFieldInPreview( value ) );
    }

    public Boolean setLiveViewAutoFocusMode( final EdsEvfAFMode value ) {
        return executeNow( new SetPropertyCommand.LiveViewAutoFocusMode( value ) );
    }

    public Boolean setLiveViewZoomRatio( final EdsEvfZoom value ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomRatio( value ) );
    }

    public Boolean setLiveViewZoomPosition( final EdsPoint value ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomPosition( value ) );
    }

    public Boolean setLiveViewZoomPosition( final long x, final long y ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomPosition( x, y ) );
    }

    //TODO - Test if the following can be written to.. documentation lists kEdsPropID_AEModeSelect constants, but these are no longer present in include files
    public Boolean setShootingMode( final EdsAEMode value ) {
        return executeNow( new SetPropertyCommand.ShootingMode( value ) );
    }

}
