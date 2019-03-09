package evolution.mapviewer;

import javax.swing.*;

import com.jgoodies.looks.Options;
import evolution.mapviewer.gui.MainFrame;

public class Main
{
    public static void main(String[] args) 
    {
        // init Cfg
        Cfg cfg = new Cfg();
        
        if ( args.length > 1 )
        {
            cfg.caveFileURL = args[0];
        }


        try
        {
            UIManager.setLookAndFeel( Options.PLASTICXP_NAME );
        }
        catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException exp)
        {
            exp.printStackTrace();
        }

        MainFrame frame = new MainFrame(cfg);
        if ( !cfg.caveFileGz.exists() )
        {
            int val = JOptionPane.showOptionDialog(frame,
                "Es wurde keine H\u00f6hlen Datei gefunden.\nDatei runterladen oder \u00f6ffnen?",
                "Keine H\u00f6hlendatei", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[] {"Runterladen", "\u00d6ffnen"}, "Runterladen" );
            switch ( val )
            {
            case 0:
                FileIO worker = new FileIO();
                System.out.println("Checking for updates:");
                worker.download(cfg.caveFileURL, cfg.httpProxyHost, cfg.httpProxyPort,
                    cfg.caveFileGz,  false);
                break;
            case 1:
                JFileChooser chooser = new JFileChooser( cfg.caveFileGz );
                chooser.setVisible( true );
                cfg.caveFileGz = chooser.getSelectedFile();
                break;
            }
        }
        else
        {
            long h6 = 1000 * 60 * 60 * 6;
            long age = System.currentTimeMillis() - cfg.caveFileGz.lastModified();
            if ( cfg.caveFileGz.exists() && age > h6 )
            {
                int val = JOptionPane.showOptionDialog(frame,
                    "Die H\u00f6hlen Datei ist " + (age / 1000 / 60 / 60) + " Stunden alt.\nNeue Datei runterladen?",
                    "Datei Alter", JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, null, null, null );
                switch ( val )
                {
                case JOptionPane.YES_OPTION:
                    FileIO worker = new FileIO();
                    System.out.println("Checking for updates:");
                    worker.download(cfg.caveFileURL, cfg.httpProxyHost, cfg.httpProxyPort,
                        cfg.caveFileGz,  false);
                    break;
                }
            }
        }
        
        frame.loadMapData();
        
        frame.setVisible(true);
    }
}
