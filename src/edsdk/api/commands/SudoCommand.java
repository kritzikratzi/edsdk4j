package edsdk.api.commands;

import edsdk.api.CanonCommand;

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
