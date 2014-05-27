package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonConstant;
import edsdk.utils.CanonConstant.EdsCameraCommand;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfDriveLens;

/**
 * Drives the lens in a direction.
 * 
 * @author hansi
 * 
 */
public class DriveLensCommand extends CanonCommand<Boolean> {

    private final EdsEvfDriveLens direction;

    /**
     * @param direction pick a value {@link CanonConstant.EdsEvfDriveLens}
     */
    public DriveLensCommand( final EdsEvfDriveLens direction ) {
        this.direction = direction;
    }

    @Override
    public void run() {
        final EdsError result = sendCommand( EdsCameraCommand.kEdsCameraCommand_DriveLensEvf, direction );
        setResult( result == EdsError.EDS_ERR_OK );
    }

}
