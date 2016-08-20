package gettingstarted;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edsdk.api.CanonCamera;
import edsdk.api.commands.ShootCommand;
import edsdk.utils.CanonConstants.DescriptiveEnum;
import edsdk.utils.CanonConstants.EdsAv;
import edsdk.utils.CanonConstants.EdsISOSpeed;
import edsdk.utils.CanonConstants.EdsSaveTo;
import edsdk.utils.CanonConstants.EdsTv;

/**
 * An example of taking multiple sequential shots.
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
public class E05_Timelapse {

    public static void main( final String[] args ) throws InterruptedException {
        //Native.setProtected( true );
        final CanonCamera camera = new CanonCamera();
        if ( camera.openSession() ) {

            E05_Timelapse.createUI( camera );

            while ( true ) {
                System.out.println( "=========================================" );
                final long level = camera.getBatteryLevel();
                if ( level != 0xffffffff ) {
                    System.out.println( "Battery Level = " + level );
                }

                camera.execute( new ShootCommand( EdsSaveTo.kEdsSaveTo_Host, 20, E05_Timelapse.filename() ) );

                try {
                    Thread.sleep( 15000 );
                }
                catch ( final InterruptedException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        CanonCamera.close();
    }

    public static File filename() {
        return new File( "images\\" +
                         new SimpleDateFormat( "yyyy\\MM\\dd\\HH-mm-ss" ).format( new Date() ) +
                         ".jpg" );
    }

    private static void createUI( final CanonCamera camera ) {
        final JFrame frame = new JFrame();
        final JPanel content = new JPanel( new GridBagLayout() );
        content.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        gbc.gridy = 1;

        final EdsTv currentShutterSpeed = camera.getShutterSpeed();
        final EdsAv currentApertureValue = camera.getApertureValue();
        final EdsISOSpeed currentISOSpeed = camera.getISOSpeed();

        final EdsTv[] availableShutterSpeeds = camera.getAvailableShutterSpeeds();
        final EdsAv[] availableApertureValues = camera.getAvailableApertureValues();
        final EdsISOSpeed[] availableISOSpeeds = camera.getAvailableISOSpeeds();

        E05_Timelapse.addCombobox( content, gbc, "Shutter Speed", availableShutterSpeeds, currentShutterSpeed, new Callback() {

            @Override
            public void call( final String name ) {
                camera.setShutterSpeed( EdsTv.enumOfDescription( name ) );
            }
        } );
        E05_Timelapse.addCombobox( content, gbc, "Aperture", availableApertureValues, currentApertureValue, new Callback() {

            @Override
            public void call( final String name ) {
                camera.setApertureValue( EdsAv.enumOfDescription( name ) );
            }
        } );
        E05_Timelapse.addCombobox( content, gbc, "ISO", availableISOSpeeds, currentISOSpeed, new Callback() {

            @Override
            public void call( final String name ) {
                camera.setISOSpeed( EdsISOSpeed.enumOfDescription( name ) );
            }
        } );

        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setContentPane( content );
        frame.setSize( 500, 400 );
        frame.setVisible( true );
    }

    private static void addCombobox( final JPanel content,
                                     final GridBagConstraints gbc,
                                     final String label,
                                     final DescriptiveEnum<?>[] enums,
                                     final DescriptiveEnum<?> selected,
                                     final Callback callback ) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        content.add( new JLabel( label ), gbc );

        gbc.gridx = 1;
        gbc.weightx = 1;
        // find the items ... 
        if ( enums == null ) {
            content.add( new JLabel( "not available with current mode / lens" ), gbc );
        } else {
            final LinkedList<String> items = new LinkedList<String>();
            for ( final DescriptiveEnum<?> e : enums ) {
                items.add( e.description() );
            }

            // In Java 6 JCombBox is not generic, so to compile in Java > 6 have to do this
            @SuppressWarnings( { "rawtypes", "unchecked" } )
            final JComboBox combo = new JComboBox( items.toArray( new String[] {} ) );
            combo.setSelectedItem( selected.description() );
            combo.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed( final ActionEvent event ) {
                    callback.call( combo.getSelectedItem().toString() );
                }
            } );
            content.add( combo, gbc );
        }

        gbc.gridy++;
    }

    interface Callback {

        public void call( String name );
    }
}
