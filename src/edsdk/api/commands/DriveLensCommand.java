package edsdk.api.commands;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonConstant;
import edsdk.utils.CanonConstant.EdsCameraCommand;
import edsdk.utils.CanonConstant.EdsError;
import edsdk.utils.CanonConstant.EdsEvfDriveLens;

/**
 * Drives the lens in a direction.
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
