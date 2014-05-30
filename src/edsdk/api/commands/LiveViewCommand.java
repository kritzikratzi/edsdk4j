package edsdk.api.commands;

import java.awt.image.BufferedImage;

import edsdk.api.CanonCommand;
import edsdk.utils.CanonUtils;

/**
 * Performs a live view command on the camera.
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
//TODO: probably a bad idea that Begin/End/Download are static
public class LiveViewCommand {

    public static class Begin extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.beginLiveView( camera.getEdsCamera() ) );
        }
    }

    public static class End extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.endLiveView( camera.getEdsCamera() ) );
        }
    }

    public static class Download extends CanonCommand<BufferedImage> {

        @Override
        public void run() {
            setResult( CanonUtils.downloadLiveViewImage( camera.getEdsCamera() ) );
        }
    }

    public static class IsLiveViewEnabled extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), false ) );
        }
    }

    public static class IsLiveViewActive extends CanonCommand<Boolean> {

        @Override
        public void run() {
            setResult( CanonUtils.isLiveViewEnabled( camera.getEdsCamera(), true ) );
        }
    }

}
