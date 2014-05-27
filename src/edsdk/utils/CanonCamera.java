package edsdk.utils;

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

import edsdk.EdSdkLibrary;
import edsdk.EdSdkLibrary.EdsBaseRef;
import edsdk.EdSdkLibrary.EdsCameraListRef;
import edsdk.EdSdkLibrary.EdsCameraRef;
import edsdk.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.EdsFocusInfo;
import edsdk.EdsPictureStyleDesc;
import edsdk.EdsPoint;
import edsdk.EdsPropertyDesc;
import edsdk.EdsRect;
import edsdk.EdsSize;
import edsdk.EdsTime;
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
import edsdk.utils.commands.FocusModeTask;
import edsdk.utils.commands.GetPropertyDescTask;
import edsdk.utils.commands.GetPropertyTask;
import edsdk.utils.commands.LiveViewTask;
import edsdk.utils.commands.SetPropertyTask;
import edsdk.utils.commands.ShootTask;

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
    private static ConcurrentLinkedQueue<CanonTask<?>> queue = new ConcurrentLinkedQueue<CanonTask<?>>();

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
        return executeNow( new ShootTask() );
    }

    public File[] shoot( final EdsSaveTo saveTo ) {
        return executeNow( new ShootTask( saveTo ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts ) {
        return executeNow( new ShootTask( saveTo, shotAttempts ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File dest ) {
        return executeNow( new ShootTask( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest ) {
        return executeNow( new ShootTask( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest, final boolean appendFileExtension ) {
        return executeNow( new ShootTask( saveTo, shotAttempts, dest, appendFileExtension ) );
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
        return executeNow( new SetPropertyTask.EnumData( property, value ) );
    }

    // some external users of edsdk4j (like Matlab) don't realize they can use setProperty(EdsPropertyID, long) with Integer values
    public Boolean setProperty( final EdsPropertyID property, final int value ) {
        return executeNow( new SetPropertyTask.Data( property, value ) );
    }

    public Boolean setProperty( final EdsPropertyID property, final long value ) {
        return executeNow( new SetPropertyTask.Data( property, value ) );
    }

    public Long getProperty( final EdsPropertyID property ) {
        return executeNow( new GetPropertyTask.Data( property ) );
    }

    public Long getPropertySize( final EdsPropertyID property ) {
        return executeNow( new GetPropertyTask.Size( property ) );
    }

    public EdsDataType getPropertyType( final EdsPropertyID property ) {
        return executeNow( new GetPropertyTask.Type( property ) );
    }

    public EdsPropertyDesc getPropertyDesc( final EdsPropertyID property ) {
        return executeNow( new GetPropertyDescTask( property ) );
    }

    public <T extends CanonTask<?>> T execute( final T cmd ) {
        cmd.setCamera( this );
        CanonCamera.queue.add( cmd );
        return cmd;
    }

    public <T> T executeNow( final CanonTask<T> cmd ) {
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

        CanonTask<?> task = null;

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

    private class OpenSessionCommand extends CanonTask<Boolean> {

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

    private class CloseSessionCommand extends CanonTask<Boolean> {

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
        return executeNow( new LiveViewTask.Begin() );
    }

    public Boolean endLiveView() {
        return executeNow( new LiveViewTask.End() );
    }

    public BufferedImage downloadLiveView() {
        return executeNow( new LiveViewTask.Download() );
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
        return executeNow( new LiveViewTask.IsLiveViewEnabled() );
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
        return executeNow( new LiveViewTask.IsLiveViewActive() );
    }

    public Boolean setFocusMode( final FocusModeTask.Mode mode ) {
        return executeNow( new FocusModeTask( mode ) );
    }

    public EdsDriveMode[] getAvailableDriveMode() {
        return executeNow( new GetPropertyDescTask.DriveMode() );
    }

    public EdsISOSpeed[] getAvailableISOSpeed() {
        return executeNow( new GetPropertyDescTask.ISOSpeed() );
    }

    public EdsMeteringMode[] getAvailableMeteringMode() {
        return executeNow( new GetPropertyDescTask.MeteringMode() );
    }

    public EdsAFMode[] getAvailableAutoFocusMode() {
        return executeNow( new GetPropertyDescTask.AutoFocusMode() );
    }

    public EdsAv[] getAvailableApertureValue() {
        return executeNow( new GetPropertyDescTask.ApertureValue() );
    }

    public EdsTv[] getAvailableShutterSpeed() {
        return executeNow( new GetPropertyDescTask.ShutterSpeed() );
    }

    public EdsExposureCompensation[] getAvailableExposureCompensation() {
        return executeNow( new GetPropertyDescTask.ExposureCompensation() );
    }

    public EdsAEMode[] getAvailableShootingMode() {
        return executeNow( new GetPropertyDescTask.ShootingMode() );
    }

    public EdsImageQuality[] getAvailableImageQuality() {
        return executeNow( new GetPropertyDescTask.ImageQuality() );
    }

    public EdsWhiteBalance[] getAvailableWhiteBalance() {
        return executeNow( new GetPropertyDescTask.WhiteBalance() );
    }

    public EdsColorSpace[] getAvailableColorSpace() {
        return executeNow( new GetPropertyDescTask.ColorSpace() );
    }

    public EdsPictureStyle[] getAvailablePictureStyle() {
        return executeNow( new GetPropertyDescTask.PictureStyle() );
    }

    public EdsWhiteBalance[] getAvailableLiveViewWhiteBalance() {
        return executeNow( new GetPropertyDescTask.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode[] getAvailableLiveViewAutoFocusMode() {
        return executeNow( new GetPropertyDescTask.LiveViewAutoFocusMode() );
    }

    public EdsDriveMode getDriveMode() {
        return executeNow( new GetPropertyTask.DriveMode() );
    }

    public EdsISOSpeed getISOSpeed() {
        return executeNow( new GetPropertyTask.ISOSpeed() );
    }

    public EdsMeteringMode getMeteringMode() {
        return executeNow( new GetPropertyTask.MeteringMode() );
    }

    public EdsAFMode getAutoFocusMode() {
        return executeNow( new GetPropertyTask.AutoFocusMode() );
    }

    public EdsAv getApertureValue() {
        return executeNow( new GetPropertyTask.ApertureValue() );
    }

    public EdsTv getShutterSpeed() {
        return executeNow( new GetPropertyTask.ShutterSpeed() );
    }

    public EdsExposureCompensation getExposureCompensation() {
        return executeNow( new GetPropertyTask.ExposureCompensation() );
    }

    public EdsAEMode getShootingMode() {
        return executeNow( new GetPropertyTask.ShootingMode() );
    }

    public EdsImageQuality getImageQuality() {
        return executeNow( new GetPropertyTask.ImageQuality() );
    }

    public EdsWhiteBalance getWhiteBalance() {
        return executeNow( new GetPropertyTask.WhiteBalance() );
    }

    public EdsColorSpace getColorSpace() {
        return executeNow( new GetPropertyTask.ColorSpace() );
    }

    public EdsPictureStyle getPictureStyle() {
        return executeNow( new GetPropertyTask.PictureStyle() );
    }

    public EdsWhiteBalance getLiveViewWhiteBalance() {
        return executeNow( new GetPropertyTask.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode getLiveViewAutoFocusMode() {
        return executeNow( new GetPropertyTask.LiveViewAutoFocusMode() );
    }

    public Long getCustomFunction( final EdsCustomFunction customFunction ) {
        return executeNow( new GetPropertyTask.CustomFunction( customFunction ) );
    }

    public String getProductName() {
        return executeNow( new GetPropertyTask.ProductName() );
    }

    public EdsTime getDateTime() {
        return executeNow( new GetPropertyTask.DateTime() );
    }

    public String getFirmwareVersion() {
        return executeNow( new GetPropertyTask.FirmwareVersion() );
    }

    public Long getBatteryLevel() {
        return executeNow( new GetPropertyTask.BatteryLevel() );
    }

    public String getCurrentStorage() {
        return executeNow( new GetPropertyTask.CurrentStorage() );
    }

    public String getCurrentFolder() {
        return executeNow( new GetPropertyTask.CurrentFolder() );
    }

    public EdsBatteryQuality getBatteryQuality() {
        return executeNow( new GetPropertyTask.BatteryQuality() );
    }

    public String getBodyIDEx() {
        return executeNow( new GetPropertyTask.BodyIDEx() );
    }

    public EdsFocusInfo getFocusInfo() {
        return executeNow( new GetPropertyTask.FocusInfo() );
    }

    public EdsExposureCompensation getFlashCompensation() {
        return executeNow( new GetPropertyTask.FlashCompensation() );
    }

    public Long getAvailableShots() {
        return executeNow( new GetPropertyTask.AvailableShots() );
    }

    public EdsBracket getBracket() {
        return executeNow( new GetPropertyTask.Bracket() );
    }

    public int[] getWhiteBalanceBracket() {
        return executeNow( new GetPropertyTask.WhiteBalanceBracket() );
    }

    public Boolean getLensStatus() {
        return executeNow( new GetPropertyTask.LensStatus() );
    }

    public String getArtist() {
        return executeNow( new GetPropertyTask.Artist() );
    }

    public String getCopyright() {
        return executeNow( new GetPropertyTask.Copyright() );
    }

    public String getOwnerName() {
        return executeNow( new GetPropertyTask.OwnerName() );
    }

    public EdsSaveTo getSaveTo() {
        return executeNow( new GetPropertyTask.SaveTo() );
    }

    public String getHardDriveDirectoryStructure() {
        return executeNow( new GetPropertyTask.HardDriveDirectoryStructure() );
    }

    public Long getJPEGQuality() {
        return executeNow( new GetPropertyTask.JPEGQuality() );
    }

    public Long getColorTemperature() {
        return executeNow( new GetPropertyTask.ColorTemperature() );
    }

    public int[] getWhiteBalanceShift() {
        return executeNow( new GetPropertyTask.WhiteBalanceShift() );
    }

    public Long getParameterSet() {
        return executeNow( new GetPropertyTask.ParameterSet() );
    }

    public EdsPictureStyleDesc getPictureStyleDescription() {
        return executeNow( new GetPropertyTask.PictureStyleDescription() );
    }

    public Long getMovieShootingStatus() {
        return executeNow( new GetPropertyTask.MovieShootingStatus() );
    }

    //TODO - figure out why this generally returns null unless queried just after the output device has been changed
    public EdsEvfOutputDevice getLiveViewOutputDevice() {
        return executeNow( new GetPropertyTask.LiveViewOutputDevice() );
    }

    public Boolean getLiveViewMode() {
        return executeNow( new GetPropertyTask.LiveViewMode() );
    }

    public Long getLiveViewColorTemperature() {
        return executeNow( new GetPropertyTask.LiveViewColorTemperature() );
    }

    public Boolean getLiveViewDepthOfFieldInPreview() {
        return executeNow( new GetPropertyTask.LiveViewDepthOfFieldInPreview() );
    }

    public EdsEvfZoom getLiveViewZoomRatio() {
        return executeNow( new GetPropertyTask.LiveViewZoomRatio() );
    }

    public EdsPoint getLiveViewZoomPosition() {
        return executeNow( new GetPropertyTask.LiveViewZoomPosition() );
    }

    public int[] getLiveViewHistogram() {
        return executeNow( new GetPropertyTask.LiveViewHistogram() );
    }

    public int[] getLiveViewHistogramY() {
        return executeNow( new GetPropertyTask.LiveViewHistogramY() );
    }

    public int[] getLiveViewHistogramR() {
        return executeNow( new GetPropertyTask.LiveViewHistogramR() );
    }

    public int[] getLiveViewHistogramG() {
        return executeNow( new GetPropertyTask.LiveViewHistogramG() );
    }

    public int[] getLiveViewHistogramB() {
        return executeNow( new GetPropertyTask.LiveViewHistogramB() );
    }

    public EdsPoint getLiveViewCropPosition() {
        return executeNow( new GetPropertyTask.LiveViewCropPosition() );
    }

    public EdsEvfHistogramStatus getLiveViewHistogramStatus() {
        return executeNow( new GetPropertyTask.LiveViewHistogramStatus() );
    }

    public EdsSize getLiveViewCoordinateSystem() {
        return executeNow( new GetPropertyTask.LiveViewCoordinateSystem() );
    }

    public EdsRect getLiveViewZoomRectangle() {
        return executeNow( new GetPropertyTask.LiveViewZoomRectangle() );
    }

    public int[] getLiveViewCropRectangle() {
        return executeNow( new GetPropertyTask.LiveViewCropRectangle() );
    }

    public Boolean setCustomFunction( final EdsCustomFunction customFunction,
                                      final long value ) {
        return executeNow( new SetPropertyTask.CustomFunction( customFunction, value ) );
    }

    public Boolean setArtist( final String value ) {
        return executeNow( new SetPropertyTask.Artist( value ) );
    }

    public Boolean setCopyright( final String value ) {
        return executeNow( new SetPropertyTask.Copyright( value ) );
    }

    public Boolean setOwnerName( final String value ) {
        return executeNow( new SetPropertyTask.OwnerName( value ) );
    }

    public Boolean setSaveTo( final EdsSaveTo value ) {
        return executeNow( new SetPropertyTask.SaveTo( value ) );
    }

    public Boolean setHardDriveDirectoryStructure( final String value ) {
        return executeNow( new SetPropertyTask.HardDriveDirectoryStructure( value ) );
    }

    public Boolean setImageQuality( final EdsImageQuality value ) {
        return executeNow( new SetPropertyTask.ImageQuality( value ) );
    }

    public Boolean setJPEGQuality( final long value ) {
        return executeNow( new SetPropertyTask.JPEGQuality( value ) );
    }

    public Boolean setWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyTask.WhiteBalance( value ) );
    }

    public Boolean setColorTemperature( final long value ) {
        return executeNow( new SetPropertyTask.ColorTemperature( value ) );
    }

    public Boolean setWhiteBalanceShift( final int[] value ) {
        return executeNow( new SetPropertyTask.WhiteBalanceShift( value ) );
    }

    public Boolean setColorSpace( final EdsColorSpace value ) {
        return executeNow( new SetPropertyTask.ColorSpace( value ) );
    }

    public Boolean setParameterSet( final long value ) {
        return executeNow( new SetPropertyTask.ParameterSet( value ) );
    }

    public Boolean setPictureStyle( final EdsPictureStyle value ) {
        return executeNow( new SetPropertyTask.PictureStyle( value ) );
    }

    public Boolean setPictureStyleDescription( final EdsPictureStyleDesc value ) {
        return executeNow( new SetPropertyTask.PictureStyleDescription( value ) );
    }

    public Boolean setPictureStyleDescription( final long contrast,
                                               final long sharpness,
                                               final long saturation,
                                               final long colorTone,
                                               final EdsFilterEffect filterEffect,
                                               final EdsTonigEffect toningEffect ) {
        return executeNow( new SetPropertyTask.PictureStyleDescription( contrast, sharpness, saturation, colorTone, filterEffect, toningEffect ) );
    }

    public Boolean setDriveMode( final EdsDriveMode value ) {
        return executeNow( new SetPropertyTask.DriveMode( value ) );
    }

    public Boolean setISOSpeed( final EdsISOSpeed value ) {
        return executeNow( new SetPropertyTask.ISOSpeed( value ) );
    }

    public Boolean setMeteringMode( final EdsMeteringMode value ) {
        return executeNow( new SetPropertyTask.MeteringMode( value ) );
    }

    public Boolean setApertureValue( final EdsAv value ) {
        return executeNow( new SetPropertyTask.ApertureValue( value ) );
    }

    public Boolean setShutterSpeed( final EdsTv value ) {
        return executeNow( new SetPropertyTask.ShutterSpeed( value ) );
    }

    public Boolean setExposureCompensation( final EdsExposureCompensation value ) {
        return executeNow( new SetPropertyTask.ExposureCompensation( value ) );
    }

    public Boolean setMovieShootingStatus( final long value ) {
        return executeNow( new SetPropertyTask.MovieShootingStatus( value ) );
    }

    public Boolean setLiveViewOutputDevice( final EdsEvfOutputDevice value ) {
        return executeNow( new SetPropertyTask.LiveViewOutputDevice( value ) );
    }

    public Boolean setLiveViewMode( final boolean value ) {
        return executeNow( new SetPropertyTask.LiveViewMode( value ) );
    }

    public Boolean setLiveViewWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyTask.LiveViewWhiteBalance( value ) );
    }

    public Boolean setLiveViewColorTemperature( final long value ) {
        return executeNow( new SetPropertyTask.LiveViewColorTemperature( value ) );
    }

    public Boolean setLiveViewDepthOfFieldInPreview( final boolean value ) {
        return executeNow( new SetPropertyTask.LiveViewDepthOfFieldInPreview( value ) );
    }

    public Boolean setLiveViewAutoFocusMode( final EdsEvfAFMode value ) {
        return executeNow( new SetPropertyTask.LiveViewAutoFocusMode( value ) );
    }

    public Boolean setLiveViewZoomRatio( final EdsEvfZoom value ) {
        return executeNow( new SetPropertyTask.LiveViewZoomRatio( value ) );
    }

    public Boolean setLiveViewZoomPosition( final EdsPoint value ) {
        return executeNow( new SetPropertyTask.LiveViewZoomPosition( value ) );
    }

    public Boolean setLiveViewZoomPosition( final long x, final long y ) {
        return executeNow( new SetPropertyTask.LiveViewZoomPosition( x, y ) );
    }

    //TODO - Test if the following can be written to.. documentation lists kEdsPropID_AEModeSelect constants, but these are no longer present in include files
    public Boolean setShootingMode( final EdsAEMode value ) {
        return executeNow( new SetPropertyTask.ShootingMode( value ) );
    }

}
