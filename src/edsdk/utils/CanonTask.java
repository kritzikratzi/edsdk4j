package edsdk.utils;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import edsdk.EdSdkLibrary.EdsBaseRef;
import edsdk.EdSdkLibrary.EdsObjectEventHandler;
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsCameraCommand;
import edsdk.utils.CanonConstant.EdsCameraStatusCommand;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsObjectEvent;

/**
 * The SLRCommand class tries to make your life a whole lot easier when you
 * would like
 * to create new commands for your camera.
 * 
 * E.g. one default command implemented with this is the take image cycle.
 * This is a lot of work because you need to do all the following:
 * - tell the camera to take an image
 * - test if the image was actually taken, e.g. if autofocus failed or flash
 * wasn't loaded
 * - wait for the notification from the camera that the file was stored
 * - transfer the image to the disk
 * 
 * 
 * @author hansi
 * 
 */
public abstract class CanonTask<T> implements EdsObjectEventHandler {

    public CanonCamera camera;
    private boolean finished = false;
    private boolean waitForFinish = false;
    private boolean ran = false;
    private T result;

    public CanonTask() {}

    /**
     * The camera is set by the dispatch thread automatically just before run is
     * called.
     * 
     * @param camera
     */
    public void setCamera( final CanonCamera camera ) {
        this.camera = camera;
    }

    /**
     * This should be short and sweet!
     * If your command needs to wait for events
     */
    public abstract void run();

    /**
     * Sets the result
     */
    public void setResult( final T result ) {
        this.result = result;
    }

    /**
     * By default a SLRCommand will be marked as finished as soon as
     * the run method completed. If you attached listens inside run
     * and are waiting for a special event to happen before you're done
     * please call the notYetFinished() at the end of run().
     * 
     * This will tell the dispatcher that it should start forwarding event
     * messages again, and also it'll wait with the execution of further
     * commands
     * until your command somehow calls finish() on itself to let the dispatcher
     * know that it's done.
     * <
     */
    public void notYetFinished() {
        waitForFinish = true;
    }

    /**
     * Only used in combination with notYetFinished.
     * Call this when your commands work is done (e.g. you successfully
     * shot and downloaded an image).
     * 
     * @see CanonTask#notYetFinished()
     */
    public void finish() {
        finished = true;
    }

    /**
     * Don't _ever_ call this, promise!
     */
    protected void ran() {
        ran = true;
    }

    /**
     * Checks if this command finished it's work. Only useful in combination
     * with
     * finish() and notYetFinished().
     * 
     * @see CanonTask#notYetFinished()
     * @return
     */
    public boolean finished() {
        return waitForFinish ? finished : ran;
    }

    /**
     * Sends a command to the camera
     * 
     * @return
     */
    public EdsError sendCommand( final EdsCameraCommand command,
                                 final DescriptiveEnum<? extends Number> params ) {
        return sendCommand( command, params.value().longValue() );
    }

    /**
     * Sends a command to the camera
     * 
     * @return
     */
    public EdsError sendCommand( final EdsCameraCommand command,
                                 final long params ) {
        return CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSendCommand( camera.getEdsCamera(), new NativeLong( command.value() ), new NativeLong( params ) ) );
    }

    /**
     * Sends a status command to the camera
     * 
     * @return
     */
    public EdsError sendStatusCommand( final EdsCameraStatusCommand command,
                                       final DescriptiveEnum<? extends Number> params ) {
        return sendStatusCommand( command, params.value().longValue() );
    }

    /**
     * Sends a status command to the camera
     * 
     * @return
     */
    public EdsError sendStatusCommand( final EdsCameraStatusCommand command,
                                       final long params ) {
        return CanonUtils.toEdsError( CanonCamera.EDSDK.EdsSendStatusCommand( camera.getEdsCamera(), new NativeLong( command.value() ), new NativeLong( params ) ) );
    }

    /**
     * This is a default implementation of the invoke method.
     * Just override it if you need to use events, the dispatcher will
     * take care of (un-)registering the listeners for you.
     * 
     * It's better to implement the EdsError apply() and use that instead since
     * values are conveniently wrapped as EdsObjectEvents for you
     * 
     * Also don't worry about the return value, you can use null or
     * EdsError.EDS_ERR_OK!
     */
    @Override
    public NativeLong apply( final NativeLong inEvent, final EdsBaseRef inRef,
                             final Pointer inContext ) {
        return new NativeLong( apply( EdsObjectEvent.enumOfValue( inEvent.intValue() ), inRef, inContext ).value() );
    }

    /**
     * If you don't need to use events just return EdsError.EDS_ERR_OK
     * 
     * The dispatcher will take care of (un-)registering the listeners for you.
     */
    public EdsError apply( final EdsObjectEvent inEvent,
                           final EdsBaseRef inRef, final Pointer inContext ) {
        return EdsError.EDS_ERR_OK;
    }

    public T result() {
        while ( !finished() ) {
            try {
                Thread.sleep( 10 );
            }
            catch ( final InterruptedException e ) {
                System.out.println( "Interrupt received by CanonTask, stopping..." );
                Thread.currentThread().interrupt(); // restore interrupted status
                return null;
            }
        }

        return result;
    }
}
