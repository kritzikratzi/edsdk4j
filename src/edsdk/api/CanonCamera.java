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
import edsdk.bindings.EdsFocusInfo;
import edsdk.bindings.EdsPictureStyleDesc;
import edsdk.bindings.EdsPoint;
import edsdk.bindings.EdsPropertyDesc;
import edsdk.bindings.EdsRect;
import edsdk.bindings.EdsSize;
import edsdk.bindings.EdsTime;
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
import edsdk.utils.CanonConstants.EdsError;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsEvfDriveLens;
import edsdk.utils.CanonConstants.EdsEvfHistogramStatus;
import edsdk.utils.CanonConstants.EdsEvfOutputDevice;
import edsdk.utils.CanonConstants.EdsEvfZoom;
import edsdk.utils.CanonConstants.EdsExposureCompensation;
import edsdk.utils.CanonConstants.EdsFilterEffect;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsImageQuality;
import edsdk.utils.CanonConstants.EdsMeteringMode;
import edsdk.utils.CanonConstants.EdsObjectEvent;
import edsdk.utils.CanonConstants.EdsPictureStyle;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonConstants.EdsSaveTo;
import edsdk.utils.CanonConstants.EdsTonigEffect;
import edsdk.utils.CanonConstants.EdsTv;
import edsdk.utils.CanonConstants.EdsWhiteBalance;
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
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
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

    private EdsCameraRef edsCamera = null;

    private String errorMessage = "No Errors Yet";
    private EdsError errorCode = EdsError.EDS_ERR_OK;

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

    public ShootCommand shootAsync() {
        return execute( new ShootCommand() );
    }

    public File[] shoot( final EdsSaveTo saveTo ) {
        return executeNow( new ShootCommand( saveTo ) );
    }

    public ShootCommand shootAsync( final EdsSaveTo saveTo ) {
        return execute( new ShootCommand( saveTo ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts ) );
    }

    public ShootCommand shootAsync( final EdsSaveTo saveTo,
                                    final int shotAttempts ) {
        return execute( new ShootCommand( saveTo, shotAttempts ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File dest ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public ShootCommand shootAsync( final EdsSaveTo saveTo,
                                    final int shotAttempts, final File dest ) {
        return execute( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public ShootCommand shootAsync( final EdsSaveTo saveTo,
                                    final int shotAttempts, final File[] dest ) {
        return execute( new ShootCommand( saveTo, shotAttempts, dest ) );
    }

    public File[] shoot( final EdsSaveTo saveTo, final int shotAttempts,
                         final File[] dest, final boolean appendFileExtension ) {
        return executeNow( new ShootCommand( saveTo, shotAttempts, dest, appendFileExtension ) );
    }

    public ShootCommand shootAsync( final EdsSaveTo saveTo,
                                    final int shotAttempts, final File[] dest,
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
    public Boolean setProperty( final EdsPropertyID property,
                                final DescriptiveEnum<? extends Number> value ) {
        return executeNow( new SetPropertyCommand.EnumData( property, value ) );
    }

    /**
     * It's better to use the specific getX/setX async method instead
     * 
     * @param property
     * @param value
     * @return
     */
    public SetPropertyCommand.EnumData setPropertyAsync( final EdsPropertyID property,
                                                         final DescriptiveEnum<? extends Number> value ) {
        return execute( new SetPropertyCommand.EnumData( property, value ) );
    }

    // some external users of edsdk4j (like Matlab) don't realize they can use setProperty(EdsPropertyID, long) with Integer values
    public Boolean setProperty( final EdsPropertyID property, final int value ) {
        return executeNow( new SetPropertyCommand.Data( property, value ) );
    }

    // some external users of edsdk4j (like Matlab) don't realize they can use setProperty(EdsPropertyID, long) with Integer values
    public SetPropertyCommand.Data setPropertyAsync( final EdsPropertyID property,
                                                     final int value ) {
        return execute( new SetPropertyCommand.Data( property, value ) );
    }

    public Boolean setProperty( final EdsPropertyID property, final long value ) {
        return executeNow( new SetPropertyCommand.Data( property, value ) );
    }

    public SetPropertyCommand.Data setPropertyAsync( final EdsPropertyID property,
                                                     final long value ) {
        return execute( new SetPropertyCommand.Data( property, value ) );
    }

    public Long getProperty( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Data( property ) );
    }

    public GetPropertyCommand.Data getPropertyAsync( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Data( property ) );
    }

    public Long getPropertySize( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Size( property ) );
    }

    public GetPropertyCommand.Size getPropertySizeAsync( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Size( property ) );
    }

    public EdsDataType getPropertyType( final EdsPropertyID property ) {
        return executeNow( new GetPropertyCommand.Type( property ) );
    }

    public GetPropertyCommand.Type getPropertyTypeAsync( final EdsPropertyID property ) {
        return execute( new GetPropertyCommand.Type( property ) );
    }

    public EdsPropertyDesc getPropertyDesc( final EdsPropertyID property ) {
        return executeNow( new GetPropertyDescCommand( property ) );
    }

    public GetPropertyDescCommand getPropertyDescAsync( final EdsPropertyID property ) {
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
            EdsError err = EdsError.EDS_ERR_OK;
            
            final EdsCameraListRef.ByReference listRef = new EdsCameraListRef.ByReference();
            final EdsCameraRef.ByReference cameraRef = new EdsCameraRef.ByReference();
            
            try {
                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetCameraList( listRef ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception("Camera failed to initialize");
                }

                final NativeLongByReference outRef = new NativeLongByReference();
                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildCount( listRef.getValue(), outRef ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Number of attached cameras couldn't be read" );
                }

                final long numCams = outRef.getValue().longValue();
                if ( numCams <= 0 ) {
                    err = EdsError.EDS_ERR_DEVICE_NOT_FOUND;
                    throw new Exception( "No cameras found. Have you tried turning it off and on again?" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsGetChildAtIndex( listRef.getValue(), new NativeLong( 0 ), cameraRef ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Access to camera failed" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSetObjectEventHandler( cameraRef.getValue(), new NativeLong( EdsObjectEvent.kEdsObjectEvent_All.value() ), CanonCamera.this, new Pointer( 0 ) ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Callback handler couldn't be added" );
                }

                err = CanonUtils.toEdsError( CanonCamera.EDSDK.EdsOpenSession( cameraRef.getValue() ) );
                if ( err != EdsError.EDS_ERR_OK ) {
                    throw new Exception( "Couldn't open camera session" );
                }
                
                CanonCamera.this.edsCamera = cameraRef.getValue();
            } catch (Exception e) {
                CanonUtils.release( cameraRef );
                setError( err, e.getMessage() );
            } finally {
                CanonUtils.release( listRef );
            }

            return err == EdsError.EDS_ERR_OK;
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
            CanonUtils.release( edsCamera );

            if ( err != EdsError.EDS_ERR_OK ) {
                return setError( err, "Couldn't close camera session" );
            }

            return true;
        }
    }

    public Boolean beginLiveView() {
        return executeNow( new LiveViewCommand.Begin() );
    }

    public LiveViewCommand.Begin beginLiveViewAsync() {
        return execute( new LiveViewCommand.Begin() );
    }

    public Boolean endLiveView() {
        return executeNow( new LiveViewCommand.End() );
    }

    public LiveViewCommand.End endLiveViewAsync() {
        return execute( new LiveViewCommand.End() );
    }

    public BufferedImage downloadLiveView() {
        return executeNow( new LiveViewCommand.Download() );
    }

    public LiveViewCommand.Download downloadLiveViewAsync() {
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
    public Boolean isLiveViewEnabled() {
        return executeNow( new LiveViewCommand.IsLiveViewEnabled() );
    }

    public LiveViewCommand.IsLiveViewEnabled isLiveViewEnabledAsync() {
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
    public Boolean isLiveViewActive() {
        return executeNow( new LiveViewCommand.IsLiveViewActive() );
    }

    public LiveViewCommand.IsLiveViewActive isLiveViewActiveAsync() {
        return execute( new LiveViewCommand.IsLiveViewActive() );
    }

    public Boolean setFocusMode( final FocusModeCommand.Mode mode ) {
        return executeNow( new FocusModeCommand( mode ) );
    }

    public FocusModeCommand setFocusModeAsync( final FocusModeCommand.Mode mode ) {
        return execute( new FocusModeCommand( mode ) );
    }

    public Boolean useAutoFocus() {
        return executeNow( new FocusModeCommand( FocusModeCommand.Mode.AUTO ) );
    }

    public FocusModeCommand useAutoFocusAsync() {
        return execute( new FocusModeCommand( FocusModeCommand.Mode.AUTO ) );
    }

    public Boolean useManualFocus() {
        return executeNow( new FocusModeCommand( FocusModeCommand.Mode.MANUAL ) );
    }

    public FocusModeCommand useManualFocusAsync() {
        return execute( new FocusModeCommand( FocusModeCommand.Mode.MANUAL ) );
    }

    public Boolean driveLens( final EdsEvfDriveLens mode ) {
        return executeNow( new DriveLensCommand( mode ) );
    }

    public DriveLensCommand driveLensAsync( final EdsEvfDriveLens mode ) {
        return execute( new DriveLensCommand( mode ) );
    }

    public EdsDriveMode[] getAvailableDriveModes() {
        return executeNow( new GetPropertyDescCommand.DriveMode() );
    }

    public GetPropertyDescCommand.DriveMode getAvailableDriveModesAsync() {
        return execute( new GetPropertyDescCommand.DriveMode() );
    }

    public EdsISOSpeed[] getAvailableISOSpeeds() {
        return executeNow( new GetPropertyDescCommand.ISOSpeed() );
    }

    public GetPropertyDescCommand.ISOSpeed getAvailableISOSpeedsAsync() {
        return execute( new GetPropertyDescCommand.ISOSpeed() );
    }

    public EdsMeteringMode[] getAvailableMeteringModes() {
        return executeNow( new GetPropertyDescCommand.MeteringMode() );
    }

    public GetPropertyDescCommand.MeteringMode getAvailableMeteringModesAsync() {
        return execute( new GetPropertyDescCommand.MeteringMode() );
    }

    public EdsAFMode[] getAvailableAutoFocusModes() {
        return executeNow( new GetPropertyDescCommand.AutoFocusMode() );
    }

    public GetPropertyDescCommand.AutoFocusMode getAvailableAutoFocusModesAsync() {
        return execute( new GetPropertyDescCommand.AutoFocusMode() );
    }

    public EdsAv[] getAvailableApertureValues() {
        return executeNow( new GetPropertyDescCommand.ApertureValue() );
    }

    public GetPropertyDescCommand.ApertureValue getAvailableApertureValuesAsync() {
        return execute( new GetPropertyDescCommand.ApertureValue() );
    }

    public EdsTv[] getAvailableShutterSpeeds() {
        return executeNow( new GetPropertyDescCommand.ShutterSpeed() );
    }

    public GetPropertyDescCommand.ShutterSpeed getAvailableShutterSpeedsAsync() {
        return execute( new GetPropertyDescCommand.ShutterSpeed() );
    }

    public EdsExposureCompensation[] getAvailableExposureCompensations() {
        return executeNow( new GetPropertyDescCommand.ExposureCompensation() );
    }

    public GetPropertyDescCommand.ExposureCompensation getAvailableExposureCompensationsAsync() {
        return execute( new GetPropertyDescCommand.ExposureCompensation() );
    }

    public EdsAEMode[] getAvailableShootingModes() {
        return executeNow( new GetPropertyDescCommand.ShootingMode() );
    }

    public GetPropertyDescCommand.ShootingMode getAvailableShootingModesAsync() {
        return execute( new GetPropertyDescCommand.ShootingMode() );
    }

    public EdsImageQuality[] getAvailableImageQualities() {
        return executeNow( new GetPropertyDescCommand.ImageQuality() );
    }

    public GetPropertyDescCommand.ImageQuality getAvailableImageQualitiesAsync() {
        return execute( new GetPropertyDescCommand.ImageQuality() );
    }

    public EdsWhiteBalance[] getAvailableWhiteBalances() {
        return executeNow( new GetPropertyDescCommand.WhiteBalance() );
    }

    public GetPropertyDescCommand.WhiteBalance getAvailableWhiteBalancesAsync() {
        return execute( new GetPropertyDescCommand.WhiteBalance() );
    }

    public EdsColorSpace[] getAvailableColorSpaces() {
        return executeNow( new GetPropertyDescCommand.ColorSpace() );
    }

    public GetPropertyDescCommand.ColorSpace getAvailableColorSpacesAsync() {
        return execute( new GetPropertyDescCommand.ColorSpace() );
    }

    public EdsPictureStyle[] getAvailablePictureStyles() {
        return executeNow( new GetPropertyDescCommand.PictureStyle() );
    }

    public GetPropertyDescCommand.PictureStyle getAvailablePictureStylesAsync() {
        return execute( new GetPropertyDescCommand.PictureStyle() );
    }

    public EdsWhiteBalance[] getAvailableLiveViewWhiteBalances() {
        return executeNow( new GetPropertyDescCommand.LiveViewWhiteBalance() );
    }

    public GetPropertyDescCommand.LiveViewWhiteBalance getAvailableLiveViewWhiteBalancesAsync() {
        return execute( new GetPropertyDescCommand.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode[] getAvailableLiveViewAutoFocusModes() {
        return executeNow( new GetPropertyDescCommand.LiveViewAutoFocusMode() );
    }

    public GetPropertyDescCommand.LiveViewAutoFocusMode getAvailableLiveViewAutoFocusModesAsync() {
        return execute( new GetPropertyDescCommand.LiveViewAutoFocusMode() );
    }

    public EdsDriveMode getDriveMode() {
        return executeNow( new GetPropertyCommand.DriveMode() );
    }

    public GetPropertyCommand.DriveMode getDriveModeAsync() {
        return execute( new GetPropertyCommand.DriveMode() );
    }

    public EdsISOSpeed getISOSpeed() {
        return executeNow( new GetPropertyCommand.ISOSpeed() );
    }

    public GetPropertyCommand.ISOSpeed getISOSpeedAsync() {
        return execute( new GetPropertyCommand.ISOSpeed() );
    }

    public EdsMeteringMode getMeteringMode() {
        return executeNow( new GetPropertyCommand.MeteringMode() );
    }

    public GetPropertyCommand.MeteringMode getMeteringModeAsync() {
        return execute( new GetPropertyCommand.MeteringMode() );
    }

    public EdsAFMode getAutoFocusMode() {
        return executeNow( new GetPropertyCommand.AutoFocusMode() );
    }

    public GetPropertyCommand.AutoFocusMode getAutoFocusModeAsync() {
        return execute( new GetPropertyCommand.AutoFocusMode() );
    }

    public EdsAv getApertureValue() {
        return executeNow( new GetPropertyCommand.ApertureValue() );
    }

    public GetPropertyCommand.ApertureValue getApertureValueAsync() {
        return execute( new GetPropertyCommand.ApertureValue() );
    }

    public EdsTv getShutterSpeed() {
        return executeNow( new GetPropertyCommand.ShutterSpeed() );
    }

    public GetPropertyCommand.ShutterSpeed getShutterSpeedAsync() {
        return execute( new GetPropertyCommand.ShutterSpeed() );
    }

    public EdsExposureCompensation getExposureCompensation() {
        return executeNow( new GetPropertyCommand.ExposureCompensation() );
    }

    public GetPropertyCommand.ExposureCompensation getExposureCompensationAsync() {
        return execute( new GetPropertyCommand.ExposureCompensation() );
    }

    public EdsAEMode getShootingMode() {
        return executeNow( new GetPropertyCommand.ShootingMode() );
    }

    public GetPropertyCommand.ShootingMode getShootingModeAsync() {
        return execute( new GetPropertyCommand.ShootingMode() );
    }

    public EdsImageQuality getImageQuality() {
        return executeNow( new GetPropertyCommand.ImageQuality() );
    }

    public GetPropertyCommand.ImageQuality getImageQualityAsync() {
        return execute( new GetPropertyCommand.ImageQuality() );
    }

    public EdsWhiteBalance getWhiteBalance() {
        return executeNow( new GetPropertyCommand.WhiteBalance() );
    }

    public GetPropertyCommand.WhiteBalance getWhiteBalanceAsync() {
        return execute( new GetPropertyCommand.WhiteBalance() );
    }

    public EdsColorSpace getColorSpace() {
        return executeNow( new GetPropertyCommand.ColorSpace() );
    }

    public GetPropertyCommand.ColorSpace getColorSpaceAsync() {
        return execute( new GetPropertyCommand.ColorSpace() );
    }

    public EdsPictureStyle getPictureStyle() {
        return executeNow( new GetPropertyCommand.PictureStyle() );
    }

    public GetPropertyCommand.PictureStyle getPictureStyleAsync() {
        return execute( new GetPropertyCommand.PictureStyle() );
    }

    public EdsWhiteBalance getLiveViewWhiteBalance() {
        return executeNow( new GetPropertyCommand.LiveViewWhiteBalance() );
    }

    public GetPropertyCommand.LiveViewWhiteBalance getLiveViewWhiteBalanceAsync() {
        return execute( new GetPropertyCommand.LiveViewWhiteBalance() );
    }

    public EdsEvfAFMode getLiveViewAutoFocusMode() {
        return executeNow( new GetPropertyCommand.LiveViewAutoFocusMode() );
    }

    public GetPropertyCommand.LiveViewAutoFocusMode getLiveViewAutoFocusModeAsync() {
        return execute( new GetPropertyCommand.LiveViewAutoFocusMode() );
    }

    public Long getCustomFunction( final EdsCustomFunction customFunction ) {
        return executeNow( new GetPropertyCommand.CustomFunction( customFunction ) );
    }

    public GetPropertyCommand.CustomFunction getCustomFunctionAsync( final EdsCustomFunction customFunction ) {
        return execute( new GetPropertyCommand.CustomFunction( customFunction ) );
    }

    public String getProductName() {
        return executeNow( new GetPropertyCommand.ProductName() );
    }

    public GetPropertyCommand.ProductName getProductNameAsync() {
        return execute( new GetPropertyCommand.ProductName() );
    }

    public EdsTime getDateTime() {
        return executeNow( new GetPropertyCommand.DateTime() );
    }

    public GetPropertyCommand.DateTime getDateTimeAsync() {
        return execute( new GetPropertyCommand.DateTime() );
    }

    public String getFirmwareVersion() {
        return executeNow( new GetPropertyCommand.FirmwareVersion() );
    }

    public GetPropertyCommand.FirmwareVersion getFirmwareVersionAsync() {
        return execute( new GetPropertyCommand.FirmwareVersion() );
    }

    public Long getBatteryLevel() {
        return executeNow( new GetPropertyCommand.BatteryLevel() );
    }

    public GetPropertyCommand.BatteryLevel getBatteryLevelAsync() {
        return execute( new GetPropertyCommand.BatteryLevel() );
    }

    public String getCurrentStorage() {
        return executeNow( new GetPropertyCommand.CurrentStorage() );
    }

    public GetPropertyCommand.CurrentStorage getCurrentStorageAsync() {
        return execute( new GetPropertyCommand.CurrentStorage() );
    }

    public String getCurrentFolder() {
        return executeNow( new GetPropertyCommand.CurrentFolder() );
    }

    public GetPropertyCommand.CurrentFolder getCurrentFolderAsync() {
        return execute( new GetPropertyCommand.CurrentFolder() );
    }

    public EdsBatteryQuality getBatteryQuality() {
        return executeNow( new GetPropertyCommand.BatteryQuality() );
    }

    public GetPropertyCommand.BatteryQuality getBatteryQualityAsync() {
        return execute( new GetPropertyCommand.BatteryQuality() );
    }

    public String getBodyIDEx() {
        return executeNow( new GetPropertyCommand.BodyIDEx() );
    }

    public GetPropertyCommand.BodyIDEx getBodyIDExAsync() {
        return execute( new GetPropertyCommand.BodyIDEx() );
    }

    public EdsFocusInfo getFocusInfo() {
        return executeNow( new GetPropertyCommand.FocusInfo() );
    }

    public GetPropertyCommand.FocusInfo getFocusInfoAsync() {
        return execute( new GetPropertyCommand.FocusInfo() );
    }

    public EdsExposureCompensation getFlashCompensation() {
        return executeNow( new GetPropertyCommand.FlashCompensation() );
    }

    public GetPropertyCommand.FlashCompensation getFlashCompensationAsync() {
        return execute( new GetPropertyCommand.FlashCompensation() );
    }

    public Long getAvailableShots() {
        return executeNow( new GetPropertyCommand.AvailableShots() );
    }

    public GetPropertyCommand.AvailableShots getAvailableShotsAsync() {
        return execute( new GetPropertyCommand.AvailableShots() );
    }

    public EdsBracket getBracket() {
        return executeNow( new GetPropertyCommand.Bracket() );
    }

    public GetPropertyCommand.Bracket getBracketAsync() {
        return execute( new GetPropertyCommand.Bracket() );
    }

    public int[] getWhiteBalanceBracket() {
        return executeNow( new GetPropertyCommand.WhiteBalanceBracket() );
    }

    public GetPropertyCommand.WhiteBalanceBracket getWhiteBalanceBracketAsync() {
        return execute( new GetPropertyCommand.WhiteBalanceBracket() );
    }

    public Boolean getLensStatus() {
        return executeNow( new GetPropertyCommand.LensStatus() );
    }

    public GetPropertyCommand.LensStatus getLensStatusAsync() {
        return execute( new GetPropertyCommand.LensStatus() );
    }

    public String getArtist() {
        return executeNow( new GetPropertyCommand.Artist() );
    }

    public GetPropertyCommand.Artist getArtistAsync() {
        return execute( new GetPropertyCommand.Artist() );
    }

    public String getCopyright() {
        return executeNow( new GetPropertyCommand.Copyright() );
    }

    public GetPropertyCommand.Copyright getCopyrightAsync() {
        return execute( new GetPropertyCommand.Copyright() );
    }

    public String getOwnerName() {
        return executeNow( new GetPropertyCommand.OwnerName() );
    }

    public GetPropertyCommand.OwnerName getOwnerNameAsync() {
        return execute( new GetPropertyCommand.OwnerName() );
    }

    public EdsSaveTo getSaveTo() {
        return executeNow( new GetPropertyCommand.SaveTo() );
    }

    public GetPropertyCommand.SaveTo getSaveToAsync() {
        return execute( new GetPropertyCommand.SaveTo() );
    }

    public String getHardDriveDirectoryStructure() {
        return executeNow( new GetPropertyCommand.HardDriveDirectoryStructure() );
    }

    public GetPropertyCommand.HardDriveDirectoryStructure getHardDriveDirectoryStructureAsync() {
        return execute( new GetPropertyCommand.HardDriveDirectoryStructure() );
    }

    public Long getJPEGQuality() {
        return executeNow( new GetPropertyCommand.JPEGQuality() );
    }

    public GetPropertyCommand.JPEGQuality getJPEGQualityAsync() {
        return execute( new GetPropertyCommand.JPEGQuality() );
    }

    public Long getColorTemperature() {
        return executeNow( new GetPropertyCommand.ColorTemperature() );
    }

    public GetPropertyCommand.ColorTemperature getColorTemperatureAsync() {
        return execute( new GetPropertyCommand.ColorTemperature() );
    }

    public int[] getWhiteBalanceShift() {
        return executeNow( new GetPropertyCommand.WhiteBalanceShift() );
    }

    public GetPropertyCommand.WhiteBalanceShift getWhiteBalanceShiftAsync() {
        return execute( new GetPropertyCommand.WhiteBalanceShift() );
    }

    public Long getParameterSet() {
        return executeNow( new GetPropertyCommand.ParameterSet() );
    }

    public GetPropertyCommand.ParameterSet getParameterSetAsync() {
        return execute( new GetPropertyCommand.ParameterSet() );
    }

    public EdsPictureStyleDesc getPictureStyleDescription() {
        return executeNow( new GetPropertyCommand.PictureStyleDescription() );
    }

    public GetPropertyCommand.PictureStyleDescription getPictureStyleDescriptionAsync() {
        return execute( new GetPropertyCommand.PictureStyleDescription() );
    }

    public Long getMovieShootingStatus() {
        return executeNow( new GetPropertyCommand.MovieShootingStatus() );
    }

    public GetPropertyCommand.MovieShootingStatus getMovieShootingStatusAsync() {
        return execute( new GetPropertyCommand.MovieShootingStatus() );
    }

    //TODO: figure out why this generally returns null unless queried just after the output device has been changed
    public EdsEvfOutputDevice getLiveViewOutputDevice() {
        return executeNow( new GetPropertyCommand.LiveViewOutputDevice() );
    }

    //TODO: figure out why this generally returns null unless queried just after the output device has been changed
    public GetPropertyCommand.LiveViewOutputDevice getLiveViewOutputDeviceAsync() {
        return execute( new GetPropertyCommand.LiveViewOutputDevice() );
    }

    public Boolean getLiveViewMode() {
        return executeNow( new GetPropertyCommand.LiveViewMode() );
    }

    public GetPropertyCommand.LiveViewMode getLiveViewModeAsync() {
        return execute( new GetPropertyCommand.LiveViewMode() );
    }

    public Long getLiveViewColorTemperature() {
        return executeNow( new GetPropertyCommand.LiveViewColorTemperature() );
    }

    public GetPropertyCommand.LiveViewColorTemperature getLiveViewColorTemperatureAsync() {
        return execute( new GetPropertyCommand.LiveViewColorTemperature() );
    }

    public Boolean getLiveViewDepthOfFieldInPreview() {
        return executeNow( new GetPropertyCommand.LiveViewDepthOfFieldInPreview() );
    }

    public GetPropertyCommand.LiveViewDepthOfFieldInPreview getLiveViewDepthOfFieldInPreviewAsync() {
        return execute( new GetPropertyCommand.LiveViewDepthOfFieldInPreview() );
    }

    public EdsEvfZoom getLiveViewZoomRatio() {
        return executeNow( new GetPropertyCommand.LiveViewZoomRatio() );
    }

    public GetPropertyCommand.LiveViewZoomRatio getLiveViewZoomRatioAsync() {
        return execute( new GetPropertyCommand.LiveViewZoomRatio() );
    }

    public EdsPoint getLiveViewZoomPosition() {
        return executeNow( new GetPropertyCommand.LiveViewZoomPosition() );
    }

    public GetPropertyCommand.LiveViewZoomPosition getLiveViewZoomPositionAsync() {
        return execute( new GetPropertyCommand.LiveViewZoomPosition() );
    }

    public int[] getLiveViewHistogram() {
        return executeNow( new GetPropertyCommand.LiveViewHistogram() );
    }

    public GetPropertyCommand.LiveViewHistogram getLiveViewHistogramAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogram() );
    }

    public int[] getLiveViewHistogramY() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramY() );
    }

    public GetPropertyCommand.LiveViewHistogramY getLiveViewHistogramYAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogramY() );
    }

    public int[] getLiveViewHistogramR() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramR() );
    }

    public GetPropertyCommand.LiveViewHistogramR getLiveViewHistogramRAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogramR() );
    }

    public int[] getLiveViewHistogramG() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramG() );
    }

    public GetPropertyCommand.LiveViewHistogramG getLiveViewHistogramGAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogramG() );
    }

    public int[] getLiveViewHistogramB() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramB() );
    }

    public GetPropertyCommand.LiveViewHistogramB getLiveViewHistogramBAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogramB() );
    }

    public EdsPoint getLiveViewCropPosition() {
        return executeNow( new GetPropertyCommand.LiveViewCropPosition() );
    }

    public GetPropertyCommand.LiveViewCropPosition getLiveViewCropPositionAsync() {
        return execute( new GetPropertyCommand.LiveViewCropPosition() );
    }

    public EdsEvfHistogramStatus getLiveViewHistogramStatus() {
        return executeNow( new GetPropertyCommand.LiveViewHistogramStatus() );
    }

    public GetPropertyCommand.LiveViewHistogramStatus getLiveViewHistogramStatusAsync() {
        return execute( new GetPropertyCommand.LiveViewHistogramStatus() );
    }

    public EdsSize getLiveViewCoordinateSystem() {
        return executeNow( new GetPropertyCommand.LiveViewCoordinateSystem() );
    }

    public GetPropertyCommand.LiveViewCoordinateSystem getLiveViewCoordinateSystemAsync() {
        return execute( new GetPropertyCommand.LiveViewCoordinateSystem() );
    }

    public EdsRect getLiveViewZoomRectangle() {
        return executeNow( new GetPropertyCommand.LiveViewZoomRectangle() );
    }

    public GetPropertyCommand.LiveViewZoomRectangle getLiveViewZoomRectangleAsync() {
        return execute( new GetPropertyCommand.LiveViewZoomRectangle() );
    }

    public int[] getLiveViewCropRectangle() {
        return executeNow( new GetPropertyCommand.LiveViewCropRectangle() );
    }

    public GetPropertyCommand.LiveViewCropRectangle getLiveViewCropRectangleAsync() {
        return execute( new GetPropertyCommand.LiveViewCropRectangle() );
    }

    public Boolean setCustomFunction( final EdsCustomFunction customFunction,
                                      final long value ) {
        return executeNow( new SetPropertyCommand.CustomFunction( customFunction, value ) );
    }

    public SetPropertyCommand.CustomFunction setCustomFunctionAsync( final EdsCustomFunction customFunction,
                                                                     final long value ) {
        return execute( new SetPropertyCommand.CustomFunction( customFunction, value ) );
    }

    public Boolean setArtist( final String value ) {
        return executeNow( new SetPropertyCommand.Artist( value ) );
    }

    public SetPropertyCommand.Artist setArtistAsync( final String value ) {
        return execute( new SetPropertyCommand.Artist( value ) );
    }

    public Boolean setCopyright( final String value ) {
        return executeNow( new SetPropertyCommand.Copyright( value ) );
    }

    public SetPropertyCommand.Copyright setCopyrightAsync( final String value ) {
        return execute( new SetPropertyCommand.Copyright( value ) );
    }

    public Boolean setOwnerName( final String value ) {
        return executeNow( new SetPropertyCommand.OwnerName( value ) );
    }

    public SetPropertyCommand.OwnerName setOwnerNameAsync( final String value ) {
        return execute( new SetPropertyCommand.OwnerName( value ) );
    }

    public Boolean setSaveTo( final EdsSaveTo value ) {
        return executeNow( new SetPropertyCommand.SaveTo( value ) );
    }

    public SetPropertyCommand.SaveTo setSaveToAsync( final EdsSaveTo value ) {
        return execute( new SetPropertyCommand.SaveTo( value ) );
    }

    public Boolean setHardDriveDirectoryStructure( final String value ) {
        return executeNow( new SetPropertyCommand.HardDriveDirectoryStructure( value ) );
    }

    public SetPropertyCommand.HardDriveDirectoryStructure setHardDriveDirectoryStructureAsync( final String value ) {
        return execute( new SetPropertyCommand.HardDriveDirectoryStructure( value ) );
    }

    public Boolean setImageQuality( final EdsImageQuality value ) {
        return executeNow( new SetPropertyCommand.ImageQuality( value ) );
    }

    public SetPropertyCommand.ImageQuality setImageQualityAsync( final EdsImageQuality value ) {
        return execute( new SetPropertyCommand.ImageQuality( value ) );
    }

    public Boolean setJPEGQuality( final long value ) {
        return executeNow( new SetPropertyCommand.JPEGQuality( value ) );
    }

    public SetPropertyCommand.JPEGQuality setJPEGQualityAsync( final long value ) {
        return execute( new SetPropertyCommand.JPEGQuality( value ) );
    }

    public Boolean setWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyCommand.WhiteBalance( value ) );
    }

    public SetPropertyCommand.WhiteBalance setWhiteBalanceAsync( final EdsWhiteBalance value ) {
        return execute( new SetPropertyCommand.WhiteBalance( value ) );
    }

    public Boolean setColorTemperature( final long value ) {
        return executeNow( new SetPropertyCommand.ColorTemperature( value ) );
    }

    public SetPropertyCommand.ColorTemperature setColorTemperatureAsync( final long value ) {
        return execute( new SetPropertyCommand.ColorTemperature( value ) );
    }

    public Boolean setWhiteBalanceShift( final int[] value ) {
        return executeNow( new SetPropertyCommand.WhiteBalanceShift( value ) );
    }

    public SetPropertyCommand.WhiteBalanceShift setWhiteBalanceShiftAsync( final int[] value ) {
        return execute( new SetPropertyCommand.WhiteBalanceShift( value ) );
    }

    public Boolean setColorSpace( final EdsColorSpace value ) {
        return executeNow( new SetPropertyCommand.ColorSpace( value ) );
    }

    public SetPropertyCommand.ColorSpace setColorSpaceAsync( final EdsColorSpace value ) {
        return execute( new SetPropertyCommand.ColorSpace( value ) );
    }

    public Boolean setParameterSet( final long value ) {
        return executeNow( new SetPropertyCommand.ParameterSet( value ) );
    }

    public SetPropertyCommand.ParameterSet setParameterSetAsync( final long value ) {
        return execute( new SetPropertyCommand.ParameterSet( value ) );
    }

    public Boolean setPictureStyle( final EdsPictureStyle value ) {
        return executeNow( new SetPropertyCommand.PictureStyle( value ) );
    }

    public SetPropertyCommand.PictureStyle setPictureStyleAsync( final EdsPictureStyle value ) {
        return execute( new SetPropertyCommand.PictureStyle( value ) );
    }

    public Boolean setPictureStyleDescription( final EdsPictureStyleDesc value ) {
        return executeNow( new SetPropertyCommand.PictureStyleDescription( value ) );
    }

    public SetPropertyCommand.PictureStyleDescription setPictureStyleDescriptionAsync( final EdsPictureStyleDesc value ) {
        return execute( new SetPropertyCommand.PictureStyleDescription( value ) );
    }

    public Boolean setPictureStyleDescription( final long contrast,
                                               final long sharpness,
                                               final long saturation,
                                               final long colorTone,
                                               final EdsFilterEffect filterEffect,
                                               final EdsTonigEffect toningEffect ) {
        return executeNow( new SetPropertyCommand.PictureStyleDescription( contrast, sharpness, saturation, colorTone, filterEffect, toningEffect ) );
    }

    public SetPropertyCommand.PictureStyleDescription setPictureStyleDescriptionAsync( final long contrast,
                                                                                       final long sharpness,
                                                                                       final long saturation,
                                                                                       final long colorTone,
                                                                                       final EdsFilterEffect filterEffect,
                                                                                       final EdsTonigEffect toningEffect ) {
        return execute( new SetPropertyCommand.PictureStyleDescription( contrast, sharpness, saturation, colorTone, filterEffect, toningEffect ) );
    }

    public Boolean setDriveMode( final EdsDriveMode value ) {
        return executeNow( new SetPropertyCommand.DriveMode( value ) );
    }

    public SetPropertyCommand.DriveMode setDriveModeAsync( final EdsDriveMode value ) {
        return execute( new SetPropertyCommand.DriveMode( value ) );
    }

    public Boolean setISOSpeed( final EdsISOSpeed value ) {
        return executeNow( new SetPropertyCommand.ISOSpeed( value ) );
    }

    public SetPropertyCommand.ISOSpeed setISOSpeedAsync( final EdsISOSpeed value ) {
        return execute( new SetPropertyCommand.ISOSpeed( value ) );
    }

    public Boolean setMeteringMode( final EdsMeteringMode value ) {
        return executeNow( new SetPropertyCommand.MeteringMode( value ) );
    }

    public SetPropertyCommand.MeteringMode setMeteringModeAsync( final EdsMeteringMode value ) {
        return execute( new SetPropertyCommand.MeteringMode( value ) );
    }

    public Boolean setApertureValue( final EdsAv value ) {
        return executeNow( new SetPropertyCommand.ApertureValue( value ) );
    }

    public SetPropertyCommand.ApertureValue setApertureValueAsync( final EdsAv value ) {
        return execute( new SetPropertyCommand.ApertureValue( value ) );
    }

    public Boolean setShutterSpeed( final EdsTv value ) {
        return executeNow( new SetPropertyCommand.ShutterSpeed( value ) );
    }

    public SetPropertyCommand.ShutterSpeed setShutterSpeedAsync( final EdsTv value ) {
        return execute( new SetPropertyCommand.ShutterSpeed( value ) );
    }

    public Boolean setExposureCompensation( final EdsExposureCompensation value ) {
        return executeNow( new SetPropertyCommand.ExposureCompensation( value ) );
    }

    public SetPropertyCommand.ExposureCompensation setExposureCompensationAsync( final EdsExposureCompensation value ) {
        return execute( new SetPropertyCommand.ExposureCompensation( value ) );
    }

    public Boolean setMovieShootingStatus( final long value ) {
        return executeNow( new SetPropertyCommand.MovieShootingStatus( value ) );
    }

    public SetPropertyCommand.MovieShootingStatus setMovieShootingStatusAsync( final long value ) {
        return execute( new SetPropertyCommand.MovieShootingStatus( value ) );
    }

    public Boolean setLiveViewOutputDevice( final EdsEvfOutputDevice value ) {
        return executeNow( new SetPropertyCommand.LiveViewOutputDevice( value ) );
    }

    public SetPropertyCommand.LiveViewOutputDevice setLiveViewOutputDeviceAsync( final EdsEvfOutputDevice value ) {
        return execute( new SetPropertyCommand.LiveViewOutputDevice( value ) );
    }

    public Boolean setLiveViewMode( final boolean value ) {
        return executeNow( new SetPropertyCommand.LiveViewMode( value ) );
    }

    public SetPropertyCommand.LiveViewMode setLiveViewModeAsync( final boolean value ) {
        return execute( new SetPropertyCommand.LiveViewMode( value ) );
    }

    public Boolean setLiveViewWhiteBalance( final EdsWhiteBalance value ) {
        return executeNow( new SetPropertyCommand.LiveViewWhiteBalance( value ) );
    }

    public SetPropertyCommand.LiveViewWhiteBalance setLiveViewWhiteBalanceAsync( final EdsWhiteBalance value ) {
        return execute( new SetPropertyCommand.LiveViewWhiteBalance( value ) );
    }

    public Boolean setLiveViewColorTemperature( final long value ) {
        return executeNow( new SetPropertyCommand.LiveViewColorTemperature( value ) );
    }

    public SetPropertyCommand.LiveViewColorTemperature setLiveViewColorTemperatureAsync( final long value ) {
        return execute( new SetPropertyCommand.LiveViewColorTemperature( value ) );
    }

    public Boolean setLiveViewDepthOfFieldInPreview( final boolean value ) {
        return executeNow( new SetPropertyCommand.LiveViewDepthOfFieldInPreview( value ) );
    }

    public SetPropertyCommand.LiveViewDepthOfFieldInPreview setLiveViewDepthOfFieldInPreviewAsync( final boolean value ) {
        return execute( new SetPropertyCommand.LiveViewDepthOfFieldInPreview( value ) );
    }

    public Boolean setLiveViewAutoFocusMode( final EdsEvfAFMode value ) {
        return executeNow( new SetPropertyCommand.LiveViewAutoFocusMode( value ) );
    }

    public SetPropertyCommand.LiveViewAutoFocusMode setLiveViewAutoFocusModeAsync( final EdsEvfAFMode value ) {
        return execute( new SetPropertyCommand.LiveViewAutoFocusMode( value ) );
    }

    public Boolean setLiveViewZoomRatio( final EdsEvfZoom value ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomRatio( value ) );
    }

    public SetPropertyCommand.LiveViewZoomRatio setLiveViewZoomRatioAsync( final EdsEvfZoom value ) {
        return execute( new SetPropertyCommand.LiveViewZoomRatio( value ) );
    }

    public Boolean setLiveViewZoomPosition( final EdsPoint value ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomPosition( value ) );
    }

    public SetPropertyCommand.LiveViewZoomPosition setLiveViewZoomPositionAsync( final EdsPoint value ) {
        return execute( new SetPropertyCommand.LiveViewZoomPosition( value ) );
    }

    public Boolean setLiveViewZoomPosition( final long x, final long y ) {
        return executeNow( new SetPropertyCommand.LiveViewZoomPosition( x, y ) );
    }

    public SetPropertyCommand.LiveViewZoomPosition setLiveViewZoomPositionAsync( final long x,
                                                                                 final long y ) {
        return execute( new SetPropertyCommand.LiveViewZoomPosition( x, y ) );
    }

    //TODO: Test if the following can be written to.. documentation lists kEdsPropID_AEModeSelect constants, but these are no longer present in include files
    public Boolean setShootingMode( final EdsAEMode value ) {
        return executeNow( new SetPropertyCommand.ShootingMode( value ) );
    }

    //TODO: Test if the following can be written to.. documentation lists kEdsPropID_AEModeSelect constants, but these are no longer present in include files
    public SetPropertyCommand.ShootingMode setShootingModeAsync( final EdsAEMode value ) {
        return execute( new SetPropertyCommand.ShootingMode( value ) );
    }

}
