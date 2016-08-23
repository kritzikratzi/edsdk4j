package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonConstants.EdsAFMode;
import edsdk.utils.CanonConstants.EdsEvfAFMode;
import edsdk.utils.CanonConstants.EdsPropertyID;
import edsdk.utils.CanonUtils;

/**
 * Performs a focus operation on the camera.
 * 
 * Copyright © 2014 Hansi Raber <super@superduper.org>, Ananta Palani
 * <anantapalani@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 * 
 * @author hansi
 * @author Ananta Palani
 * 
 */
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
