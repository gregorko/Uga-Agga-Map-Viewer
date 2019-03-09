package evolution.mapviewer.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import evolution.mapviewer.Cfg;

public class ConfigDialog extends JDialog
{
    private final MainFrame mainFrame;
    private final Cfg cfg;
    
    private JButton terrain0;
    private JButton terrain1;
    private JButton terrain2;
    private JButton terrain3;
    private JButton terrain4;
    private JButton terrain5;
    private JButton mapBgBtn;
    private JCheckBox gitterCb;
    private JSpinner rasterSpin;
    private JSpinner letterSize;
    private JTextField caveFileTF;
    private JTextField httpProxyHostTF;
    private JTextField httpProxyPortTF;

    public ConfigDialog( MainFrame main, Cfg cfg )
    {
        super( main, "Konfiguration" );
        this.cfg = cfg;
        mainFrame = main;
        prepareComponent();
    }
    
    /**
     * 
     */
    private void prepareComponent()
    {
        CloseEventHandler closeEventHandler = new CloseEventHandler();
        addWindowListener( closeEventHandler );
        
        Container contentPane = getContentPane();
        contentPane.setLayout( new BorderLayout() );
        JPanel contentPanel = new JPanel();
        //JPanel contentPanel = new FormDebugPanel();
        contentPane.add(contentPanel, BorderLayout.CENTER);
        
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("6dlu, 4dlu, d, 2dlu, d:grow, 6dlu", // columns
            "4dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, " + // terraion farben
            "4dlu, p, 2dlu, p, " + // map bg
            "4dlu, p, 2dlu, p, 2dlu, p, " + // gitter raster
            "4dlu, p, 2dlu, p, 2dlu, p, 2dlu, p, " + // proxy
            "4dlu, p, 2dlu, p, " + // verschiedenes
            "4dlu, p, 4dlu, p, 4dlu"); //row
        PanelBuilder contentPB = new PanelBuilder(layout, contentPanel);
        
        contentPB.addSeparator( "Terrain Farben", cc.xywh( 2, 2, 4, 1 ) );
        
        terrain0 = new JButton( "Terrain: 0" );
        adjustBtnColors( cfg.terrain0, terrain0 );
        terrain0.addActionListener(new ColorChooserHandler(terrain0));
        contentPB.add( terrain0, cc.xywh( 3, 4, 3, 1 ) );

        terrain1 = new JButton( "Terrain: 1" );
        adjustBtnColors( cfg.terrain1, terrain1 );
        terrain1.addActionListener(new ColorChooserHandler(terrain1));
        contentPB.add( terrain1, cc.xywh( 3, 6, 3, 1 ) );

        terrain2 = new JButton( "Terrain: 2" );
        adjustBtnColors( cfg.terrain2, terrain2 );
        terrain2.addActionListener(new ColorChooserHandler(terrain2));
        contentPB.add( terrain2, cc.xywh( 3, 8, 3, 1 ) );

        terrain3 = new JButton( "Terrain: 3" );
        adjustBtnColors( cfg.terrain3, terrain3 );
        terrain3.addActionListener(new ColorChooserHandler(terrain3));
        contentPB.add( terrain3, cc.xywh( 3, 10, 3, 1 ) );
        
        terrain4 = new JButton( "Terrain: 4" );
        adjustBtnColors( cfg.terrain4, terrain4 );
        terrain4.addActionListener(new ColorChooserHandler(terrain4));
        contentPB.add( terrain4, cc.xywh( 3, 12, 3, 1 ) );
        
        terrain5 = new JButton( "Terrain: 5" );
        adjustBtnColors( cfg.terrain5, terrain5 );
        terrain5.addActionListener(new ColorChooserHandler(terrain5));
        contentPB.add( terrain5, cc.xywh( 3, 14, 3, 1 ) );
        
        contentPB.addSeparator( "Kartenhintergrund f\u00fcr Raster", cc.xywh( 2, 16, 4, 1 ) );
        
        mapBgBtn = new JButton( "Kartenhintergrund" );
        adjustBtnColors( cfg.mapBackground, mapBgBtn );
        mapBgBtn.addActionListener(new ColorChooserHandler(mapBgBtn));
        contentPB.add( mapBgBtn, cc.xywh( 3, 18, 3, 1 ) );
        
        contentPB.addSeparator( "Gitter und Raster", cc.xywh( 2, 20, 4, 1 ) );
        
        gitterCb = new JCheckBox( "Gitter", cfg.isGridShown );
        contentPB.add( gitterCb, cc.xywh( 3, 22, 3, 1 ) );
        
        contentPB.addLabel( "Raster", cc.xywh( 3, 24, 1, 1 ) );
        rasterSpin = new JSpinner( new SpinnerNumberModel( 
                    cfg.raster, 0, 15, 1 ) );
        contentPB.add( rasterSpin, cc.xywh( 5, 24, 1, 1 ) );
        
        contentPB.addSeparator( "Download", cc.xywh( 2, 26, 4, 1 ) );

        contentPB.addLabel( "H\u00f6hlendatei: ", cc.xywh( 3, 28, 1, 1 ) );
        caveFileTF = new JTextField( cfg.caveFileURL, 20 );
        contentPB.add( caveFileTF, cc.xywh( 5, 28, 1, 1 ) );
        
        contentPB.addLabel( "Proxy Host: ", cc.xywh( 3, 30, 1, 1 ) );
        httpProxyHostTF = new JTextField( cfg.httpProxyHost, 20 );
        contentPB.add( httpProxyHostTF, cc.xywh( 5, 30, 1, 1 ) );
        
        contentPB.addLabel( "Proxy Port: ", cc.xywh( 3, 32, 1, 1 ) );
        httpProxyPortTF = new JTextField( 
            String.valueOf( cfg.httpProxyPort ), 20 );
        contentPB.add( httpProxyPortTF, cc.xywh( 5, 32, 1, 1 ) );
        
        
        contentPB.addSeparator( "Verschiedenes", cc.xywh( 2, 34, 4, 1 ) );
        
        contentPB.addLabel( "min. Buchstabengr\u00f6\u00dfe", cc.xywh( 3, 36, 1, 1 ) );
        letterSize = new JSpinner( new SpinnerNumberModel( 
                    cfg.minLetterSize, 1, 50, 1 ) );
        contentPB.add( letterSize, cc.xywh( 5, 36, 1, 1 ) );
        
        contentPB.add( new JSeparator(), cc.xywh( 1, 38, 6, 1 ) );
        JButton okBtn = new JButton( "OK" );
        okBtn.addActionListener(e -> {
            updateCfg();
            closeDialog();
        });
        JButton cancelBtn = new JButton( "Cancel" );
        cancelBtn.addActionListener(closeEventHandler);
        
        JButton defaultBtn = new JButton( "Default" );
        defaultBtn.addActionListener(e -> {
            adjustBtnColors( cfg.getTerrain0Default(), terrain0 );
            adjustBtnColors( cfg.getTerrain1Default(), terrain1 );
            adjustBtnColors( cfg.getTerrain2Default(), terrain2 );
            adjustBtnColors( cfg.getTerrain3Default(), terrain3 );
            adjustBtnColors( cfg.getTerrain4Default(), terrain4 );
            adjustBtnColors( cfg.getTerrain5Default(), terrain5 );
            adjustBtnColors( cfg.getMapBackgroundDefault(), mapBgBtn );
            gitterCb.setSelected( cfg.isGridShownDefault() );
            rasterSpin.getModel().setValue(cfg.getRasterDefault());
            letterSize.getModel().setValue(cfg.getMinLetterSizeDefault());
            caveFileTF.setText(Cfg.DEFAULT_CAVE_FILE_URL);
        });

        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addButton(okBtn, cancelBtn, defaultBtn);
        JPanel panel = builder.getPanel();
        contentPB.add( panel, cc.xywh( 2, 40, 4, 1 ) );
        
        pack();
        setLocationRelativeTo(getParent());
    }
    
    private void updateCfg()
    {
        cfg.terrain0 = terrain0.getBackground();
        cfg.terrain1 = terrain1.getBackground();
        cfg.terrain2 = terrain2.getBackground();
        cfg.terrain3 = terrain3.getBackground();
        cfg.terrain4 = terrain4.getBackground();
        cfg.terrain5 = terrain5.getBackground();
        cfg.mapBackground = mapBgBtn.getBackground();
        cfg.isGridShown = gitterCb.isSelected();
        cfg.raster = (Integer) rasterSpin.getValue();
        cfg.minLetterSize = (Integer) letterSize.getValue();
        cfg.caveFileURL = caveFileTF.getText().trim();
        cfg.httpProxyHost = httpProxyHostTF.getText();
        try
        {
            cfg.httpProxyPort = Integer.parseInt( httpProxyPortTF.getText() );
        }
        catch ( NumberFormatException exp )
        {
            exp.printStackTrace();
        }
        
        mainFrame.triggerRepaint();
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
    private static void adjustBtnColors( Color bgColor, AbstractButton btn )
    {
        btn.setBackground(bgColor);
        Color invCol = new Color( 255-bgColor.getRed(),
            255-bgColor.getGreen(), 255-bgColor.getBlue());
        btn.setForeground( invCol );
    }
    
    private final class ColorChooserHandler implements ActionListener
    {
        private JButton actionBtn;
        
        public ColorChooserHandler( JButton btn )
        {
            actionBtn = btn;
        }
        
        public void actionPerformed( ActionEvent e )
        {
            Color result = JColorChooser.showDialog(ConfigDialog.this, 
                "Farbe w\u00e4hlen", actionBtn.getBackground() );
            if ( result != null )
            {
                adjustBtnColors(result, actionBtn);
            }
        }
    }

    private final class CloseEventHandler extends WindowAdapter implements ActionListener
    {
        public void windowClosing(WindowEvent evt)
        {
            closeDialog();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            closeDialog();
        }
    }
    
}
