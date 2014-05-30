package edsdk.api.commands;

import edsdk.api.CanonCommand;

/**
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
public class SudoCommand extends CanonCommand<Boolean> {

    private boolean superpower = false;

    public SudoCommand() {}

    @Override
    public void run() {

        notYetFinished();
        superpower = true;
    }

    /**
     * Call this to wait until you have power over the camera
     */
    public void begin() {
        try {
            while ( !superpower ) {
                Thread.sleep( 10 );
            }
        }
        catch ( final InterruptedException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Call this when you're done
     */
    public void end() {
        setResult( true );
    }

}
