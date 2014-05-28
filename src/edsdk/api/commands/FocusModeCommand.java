package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonConstant.EdsAFMode;
import edsdk.utils.CanonConstant.EdsEvfAFMode;
import edsdk.utils.CanonConstant.EdsPropertyID;
import edsdk.utils.CanonUtils;

public class FocusModeCommand extends CanonCommand<Boolean> {

    public enum Mode {
        AUTO,
        MANUAL
    }

    private final Mode mode;

    public FocusModeCommand( final Mode mode ) {
        this.mode = mode;
    }

    @Override
    public void run() {
        System.out.println( "Begin setting camera to " +
                            mode.name().toLowerCase() + " focus" );

        switch ( mode ) {
            case AUTO:
                CanonUtils.setPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_AFMode, EdsAFMode.kEdsAFMode_OneShot );
                // sendCommand( EdsCameraCommand.kEdsCameraCommand_DoEvfAf, EdsEvfAf.kEdsCameraCommand_EvfAf_ON );
                break;
            case MANUAL:
                CanonUtils.setPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_AFMode, EdsAFMode.kEdsAFMode_Manual );
                CanonUtils.setPropertyData( camera.getEdsCamera(), EdsPropertyID.kEdsPropID_Evf_AFMode, EdsEvfAFMode.Evf_AFMode_Live );
                // sendCommand( EdsCameraCommand.kEdsCameraCommand_DoEvfAf, EdsEvfAf.kEdsCameraCommand_EvfAf_OFF );
                break;
        }

        System.out.println( "DONE!" );
        setResult( true );
    }

}
