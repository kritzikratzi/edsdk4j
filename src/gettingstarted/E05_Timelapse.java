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
import edsdk.utils.CanonConstant.DescriptiveEnum;
import edsdk.utils.CanonConstant.EdsAv;
import edsdk.utils.CanonConstant.EdsISOSpeed;
import edsdk.utils.CanonConstant.EdsSaveTo;
import edsdk.utils.CanonConstant.EdsTv;

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
                System.out.println( "Battery Level = " +
                                    camera.getBatteryLevel() );

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

        final EdsTv currentShutterSpeed = camera.getShutterSpeed().get();
        final EdsAv currentApertureValue = camera.getApertureValue().get();
        final EdsISOSpeed currentISOSpeed = camera.getISOSpeed().get();

        final EdsTv[] availableShutterSpeed = camera.getAvailableShutterSpeed().get();
        final EdsAv[] availableApertureValue = camera.getAvailableApertureValue().get();
        final EdsISOSpeed[] availableISOSpeed = camera.getAvailableISOSpeed().get();

        E05_Timelapse.addCombobox( content, gbc, "Shutter Speed", availableShutterSpeed, currentShutterSpeed, new Callback() {

            @Override
            public void call( final String name ) {
                camera.setShutterSpeed( EdsTv.enumOfDescription( name ) );
            }
        } );
        E05_Timelapse.addCombobox( content, gbc, "Aperture", availableApertureValue, currentApertureValue, new Callback() {

            @Override
            public void call( final String name ) {
                camera.setApertureValue( EdsAv.enumOfDescription( name ) );
            }
        } );
        E05_Timelapse.addCombobox( content, gbc, "ISO", availableISOSpeed, currentISOSpeed, new Callback() {

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
        final LinkedList<String> items = new LinkedList<String>();
        for ( final DescriptiveEnum<?> e : enums ) {
            items.add( e.description() );
        }

        final JComboBox<Object> combo = new JComboBox<Object>( items.toArray( new String[] {} ) );
        combo.setSelectedItem( selected.description() );
        combo.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( final ActionEvent event ) {
                callback.call( combo.getSelectedItem().toString() );
            }
        } );
        gbc.gridx = 1;
        gbc.weightx = 1;
        content.add( combo, gbc );

        gbc.gridy++;
    }

    interface Callback {

        public void call( String name );
    }
}
