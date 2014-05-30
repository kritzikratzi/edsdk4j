package probe;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.imgscalr.Scalr;

import edsdk.api.CanonCamera;
import edsdk.utils.CanonConstant.EdsSaveTo;

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
public class SinglePhoto {

    public static final int shotAttempts = 5;

    public static void main( final String[] args ) {

        SwingUtilities.invokeLater( new Runnable() {

            final CanonCamera cam = new CanonCamera();
            JFrame frame;
            JLabel label;
            JButton button;

            @Override
            public void run() {
                frame = new JFrame( "Canon EDSDK - Shoot Single Photo" );
                frame.setLayout( new FlowLayout() );
                frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
                frame.addWindowListener( new WindowAdapter() {

                    @Override
                    public void windowClosing( final WindowEvent e ) {
                        CanonCamera.close();
                    }
                } );
                initialize();
                frame.pack();
                frame.setVisible( true );
            }

            private void initialize() {
                label = new JLabel();
                frame.getContentPane().add( label, BorderLayout.CENTER );

                button = new JButton( "Take Photo" );
                button.addActionListener( new ButtonListener() );
                frame.getContentPane().add( button );
            }

            final class ButtonListener implements ActionListener {

                @Override
                public void actionPerformed( final ActionEvent e ) {
                    final AbstractButton button = (AbstractButton) e.getSource();
                    final String oldText = button.getText();
                    button.setText( "working..." );
                    button.setEnabled( false );
                    new SwingWorker<File[], Object>() {

                        @Override
                        protected File[] doInBackground() throws Exception {
                            return SinglePhoto.takePhoto( cam );
                        }

                        @Override
                        protected void done() {
                            try {
                                SinglePhoto.updateImage( frame, label, get() );
                                button.setText( oldText );
                                button.setEnabled( true );
                            }
                            catch ( final Exception e ) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }.execute();
                }
            }
        } );
    }

    public static void updateImage( final JFrame frame, final JLabel label,
                                    final File[] photos ) throws IOException {
        File photo;
        if ( photos != null ) {
            if ( photos.length == 2 ) {
                if ( photos[1].getCanonicalPath().toLowerCase().endsWith( ".jpg" ) ) {
                    photo = photos[1];
                } else {
                    photo = photos[0];
                }
            } else {
                photo = photos[0];
            }

            //TODO: handle raw photo
            if ( photo != null ) {
                BufferedImage image = ImageIO.read( photo );
                if ( image != null ) {
                    image = Scalr.resize( image, Scalr.Mode.FIT_TO_WIDTH, 768, Scalr.OP_ANTIALIAS );
                    label.setIcon( new ImageIcon( image ) );
                    frame.pack();
                    image.flush();
                }
            }

            for ( final File p : photos ) {
                if ( p != null ) {
                    System.out.println( p.getCanonicalPath() );
                }
            }
        }
    }

    public static File[] takePhoto( final CanonCamera cam ) {
        if ( cam.openSession() ) {
            final File[] photos = cam.shoot( EdsSaveTo.kEdsSaveTo_Host, SinglePhoto.shotAttempts ).get();
            cam.closeSession();
            return photos;
        }
        return null;
    }

}
