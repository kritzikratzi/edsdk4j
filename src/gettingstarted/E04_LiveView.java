package gettingstarted;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import edsdk.api.CanonCamera;

/**
 * A live view example
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
public class E04_LiveView {

    public static void main( final String[] args ) throws InterruptedException {
        final CanonCamera cam = new CanonCamera();
        if ( cam.openSession() ) {
            if ( cam.beginLiveView().get() ) {
                final JFrame frame = new JFrame( "Live view" );
                final JLabel label = new JLabel();
                frame.getContentPane().add( label, BorderLayout.CENTER );
                frame.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
                frame.addWindowListener( new WindowAdapter() {

                    @Override
                    public void windowClosing( final WindowEvent e ) {
                        cam.endLiveView();
                        cam.closeSession();
                        CanonCamera.close();
                        System.exit( 0 );
                    }
                } );
                frame.setVisible( true );

                while ( true ) {
                    Thread.sleep( 50 );
                    final BufferedImage image = cam.downloadLiveView().get();
                    if ( image != null ) {
                        label.setIcon( new ImageIcon( image ) );
                        frame.pack();
                        image.flush();
                    }

                }
            }
            cam.closeSession();
        }
        CanonCamera.close();
        System.exit( 0 );
    }
}
