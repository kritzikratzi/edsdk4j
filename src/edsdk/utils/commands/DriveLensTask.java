package edsdk.utils.commands;

import edsdk.utils.CanonConstant;
import edsdk.utils.CanonConstant.EdsCameraCommand;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfDriveLens;
import edsdk.utils.CanonTask;

/**
 * Drives the lense in a direction.
 * 
 * @author hansi
 * 
 */
public class DriveLensTask extends CanonTask<Boolean> {

    private final EdsEvfDriveLens direction;

    /**
     * @param direction pick a value {@link CanonConstant.EdsEvfDriveLens}
     */
    public DriveLensTask( final EdsEvfDriveLens direction ) {
        this.direction = direction;
    }

    @Override
    public void run() {
        final EdsError result = sendCommand( EdsCameraCommand.kEdsCameraCommand_DriveLensEvf, direction );
        setResult( result == EdsError.EDS_ERR_OK );
    }

}
