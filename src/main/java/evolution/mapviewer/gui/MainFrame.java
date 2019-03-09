package evolution.mapviewer.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import evolution.mapviewer.Cfg;
import evolution.mapviewer.FileIO;
import evolution.mapviewer.Hoehle;
import evolution.mapviewer.MapData;

public class MainFrame extends JFrame
{
    private final Cfg cfg;
    private MapData mapData;
    
    //---------------------Entferungsberechnung-und-Ausgabe---------------------//
    Hoehle startHoehle = null;
    Hoehle zielHoehle = null;
    TextField startTxt = new TextField ("Start Hoehle");
    TextField zielTxt = new TextField ("Ziel Hoehle");
    TextField entfernungTxt = new TextField ("Entfernung: 0 min");
    JDialog entfernungDialog;

    //---------------------aboutDialog---------------------//
    TextArea aboutTxt = new TextArea("", 10, 30, TextArea.SCROLLBARS_NONE);
    JDialog aboutDialog;

    //---------------------MainWindowLayout---------------------//
    private FileDialog dateiAuswahl;
    private MapPanel mapPanel;
    
    //---------------------NorthButtons---------------------//
    JSpinner zoomSpinner;
    
    JTextField searchTF;
    
    //---------------------WestInfoElemente---------------------//
    private JList<ColorItem> searchSet;
    private JTextField hoehleLab = new JTextField("hier wird dies stehen: Hoehle in x|y");
    private JTextField spielerLab = new JTextField("Spieler");
    private JTextField clanLab = new JTextField("Stamm");

    private SearchListModel searchListModel;
    
    public MainFrame(Cfg cfg)
    {
        super ( "UGA AGGA Map Viewer - Version 2.0" );
        this.cfg = cfg;
        prepareMenuBar();
        prepareComponent();
    }
    
    private void prepareMenuBar()
    {
        JMenuBar bar = new JMenuBar();
        setJMenuBar( bar );
        JMenu actionMenu = new JMenu( "Aktionen" );
        bar.add( actionMenu );
        
        
        JMenuItem saveItem = new JMenuItem( "Bild speichern..." );
        saveItem.addActionListener(e -> {
            dateiAuswahl.setFile("");
            dateiAuswahl.setVisible(true);
            File bildDatei = new File(dateiAuswahl.getDirectory()+dateiAuswahl.getFile()+".png");

            try
            {
                ScreenImageFactory.createImage( mapPanel, bildDatei.getAbsolutePath() );
            }
            catch (IOException exp)
            {
                exp.printStackTrace();
            }
        });
        actionMenu.add( saveItem );
        
        actionMenu.addSeparator();
        
        JMenuItem loadMapItem = new JMenuItem( "Karte herunterladen" );
        loadMapItem.addActionListener(e -> {
            FileIO worker = new FileIO();
            worker.download(cfg.caveFileURL, cfg.httpProxyHost, cfg.httpProxyPort,
                cfg.caveFileGz,  false);
            loadMapData();
        });
        actionMenu.add( loadMapItem );
        
        actionMenu.addSeparator();
        
        JMenuItem configItem = new JMenuItem( "Konfiguration..." );
        configItem.addActionListener(e -> {
            ConfigDialog config = new ConfigDialog( MainFrame.this, cfg );
            config.setVisible(true);
        });
        actionMenu.add( configItem );

        JMenu aboutMnu = new JMenu( "About" );
        bar.add(aboutMnu);
        JMenuItem aboutItem = new JMenuItem( "About..." );
        aboutMnu.add( aboutItem );
        aboutItem.addActionListener(e -> aboutDialog.setVisible(true));
    }
    
    /**
     * 
     */
    private void prepareComponent()
    {
        addWindowListener( new WindowHandler() );
        
        // build center Panel with map
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(1,1));
        center.setMinimumSize(new Dimension(0,0));        
        mapPanel = new MapPanel(cfg);
        mapPanel.addPropertyChangeListener(evt -> {
            if ( evt.getPropertyName().equals("zoom") )
            {
                zoomSpinner.setValue( evt.getNewValue() );
            }
        });
        mapPanel.setBorder( new EmptyBorder(10, 10, 10, 10) );
        center.add( mapPanel.getParentScrollPane() );
        
        CellConstraints cc = new CellConstraints();
        JPanel altWest = new JPanel( );
        FormLayout layout = new FormLayout("2dlu, d, 4dlu, d, 2dlu, d, fill:d:grow, 2dlu", // columns
            "4dlu, p, 2dlu, p, 8dlu, p, 2dlu, p, 6dlu, p, 2dlu, p, 2dlu, p, 6dlu, p, 2dlu, p, 2dlu, p, " +
            "fill:1dlu:grow, p, 2dlu, p, 2dlu, p, 4dlu"); //row
        PanelBuilder westPB = new PanelBuilder( layout, altWest );
        
        //-------------------------------------
        
        westPB.addSeparator( "Zoom", cc.xywh( 2, 2, 6, 1) );
        zoomSpinner = new JSpinner( new SpinnerNumberModel( mapPanel.getZoom(), 1, 100, 1 ) );
        zoomSpinner.setToolTipText( "Zoomen auch mit: Alt + Mausrad" );
        zoomSpinner.addChangeListener(
            ev -> mapPanel.setZoom((Integer) zoomSpinner.getValue(), null ));
        westPB.add( zoomSpinner, cc.xy( 4, 4 ) );
        westPB.addLabel( "oder MausRad", cc.xy( 6, 4 ) );
        
        //-------------------------------------
        
        westPB.addSeparator( "Zentrieren", cc.xywh( 2, 6, 6, 1) );
        JButton resetCenterButton = new JButton("Zur\u00fccksetzen");
        resetCenterButton.addActionListener(e -> mapPanel.resetRoot());
        westPB.add( resetCenterButton, cc.xy( 4, 8 ) );
        westPB.addLabel( "<html>Mitte \u00e4ndern: Drag'n'Drop<br>oder Rechte Maustaste</html>", cc.xy( 6, 8 ) );
        
        //-------------------------------------
        
        westPB.addSeparator( "Suche", cc.xywh( 2, 10, 6, 1) );
        
        westPB.addLabel( "Spieler/Stamm: ", cc.xywh( 4, 12, 1, 1 ) );
        searchTF = new JTextField( 20 );
        westPB.add( searchTF, cc.xywh( 6, 12, 2, 1 ) );
        
        final JButton colorButton = new JButton( "Farbe..." );
        colorButton.addActionListener(e -> {
            Color result = JColorChooser.showDialog( MainFrame.this,
                "Farbe w\u00e4hlen", colorButton.getBackground() );
            if ( result != null )
            {
                Color resultInv = new Color( 255-result.getRed(),
                    255-result.getGreen(), 255-result.getBlue());
                colorButton.setBackground( result );
                colorButton.setForeground( resultInv );
            }
        });
        JButton searchButton = new JButton("Suche");
        searchButton.addActionListener(e -> {
            Color now = colorButton.getBackground();
            String searchPhrase = searchTF.getText().trim().toLowerCase();
            ColorItem item = new ColorItem( now, searchPhrase );
            mapData.addColorItem(item);
            searchListModel.fireChange();
            triggerRepaint();
        });

        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addButton(colorButton, searchButton);
        JPanel btnBar = builder.getPanel();
        westPB.add( btnBar, cc.xywh( 4, 14, 4, 1 ) );
        
        westPB.addSeparator( "Suchliste", cc.xywh( 2, 16, 6, 1) );
        
        searchListModel = new SearchListModel();
        searchSet = new JList<>( searchListModel );
        searchSet.setFont( searchSet.getFont().deriveFont( Font.BOLD ) );
        searchSet.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent( JList list, 
                Object value, int index, boolean isSelected, boolean cellHasFocus )
            {
                super.getListCellRendererComponent(list, value, index, 
                    isSelected, cellHasFocus);
                ColorItem item = (ColorItem) value;
                setForeground( item.getColor() );
                setText( item.getSearchStr() );
                return this;
            }});
        westPB.add( new JScrollPane( searchSet ), cc.xywh( 4, 18, 4, 1 ) );
        
        JButton delButton = new JButton("Entfernen");
        delButton.addActionListener(e -> {
            ColorItem item = searchSet.getSelectedValue();
            if ( item != null)
            {
                mapData.removeColorItem(item);
                searchListModel.fireChange();
                mapPanel.repaint();
            }
        });

        btnBar = new ButtonBarBuilder().addButton(delButton).addGlue().getPanel();
        westPB.add( btnBar, cc.xywh( 4, 20, 4, 1 ) );
        
        hoehleLab.setEditable(false);
        westPB.add( hoehleLab, cc.xywh( 4, 22, 4, 1 ) );
        spielerLab.setEditable(false);
        westPB.add( spielerLab, cc.xywh( 4, 24, 4, 1 ) );
        clanLab.setEditable(false);
        westPB.add( clanLab, cc.xywh( 4, 26, 4, 1 ) );
        
        
        
        
        
        JSplitPane west2center= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,altWest,center);
        getContentPane().add(west2center);
        
        //configure JSplitPanes
        west2center.setOneTouchExpandable(true);
        west2center.setContinuousLayout(true);
        //west2center.setResizeWeight(1.0);

        //setup Dialog classes
        dateiAuswahl = new FileDialog(this);
        dateiAuswahl.setMode(FileDialog.LOAD);

        entfernungDialog = new JDialog(this);
        entfernungDialog.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
        entfernungDialog.getContentPane().setLayout(new GridLayout(3,1));
        entfernungDialog.getContentPane().add(startTxt);
        startTxt.setEditable(false);
        entfernungDialog.getContentPane().add(zielTxt);
        zielTxt.setEditable(false);
        entfernungDialog.getContentPane().add(entfernungTxt);
        entfernungTxt.setEditable(false);
        entfernungDialog.setTitle("Entfernung:");

        aboutDialog = new JDialog(this);
        aboutDialog.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
        aboutDialog.getContentPane().add(aboutTxt);
        aboutDialog.setTitle("\u00fcber den UA - Map Viewer");
        aboutDialog.setLocationByPlatform(true);
        aboutDialog.setSize(400,400);



//        mapPanel.addMouseListener( new MouseAdapter() {
//            public void mousePressed(MouseEvent m) 
//            {
//                if ( m.isPopupTrigger() )
//                {
//                    return;
//                }
//                startHoehle = mapPanel.getCaveForPoint( m.getPoint() );
//            }
//            public void mouseReleased(MouseEvent m) 
//            {
//                if ( m.isPopupTrigger() )
//                {
//                    return;
//                }
//                zielHoehle = mapPanel.getCaveForPoint( m.getPoint() );
//                int time = mapData.getTimeDistance(startHoehle.x, startHoehle.y, zielHoehle.x, zielHoehle.y);
//                int stunden= time/60;
//                int minuten= time%60;
//                
//                startTxt.setText("Start: "+startHoehle);
//                zielTxt.setText("Ziel: "+zielHoehle);
//                entfernungTxt.setText("Entfernung: "+stunden+" h "+minuten+" min");
//                entfernungDialog.pack();
//                entfernungDialog.setVisible( true );
//             }
//        });
        mapPanel.addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent m) 
            {
                try
                {
                    Point koord = m.getPoint();
                    Hoehle cave = mapPanel.getCaveForPoint( koord );
                    if (cave!=null)
                    {
                        String hoehle= cave.getName() +" in "+cave.x+" | "+cave.y;
                        hoehleLab.setText(hoehle);
                        StringBuilder playerTxt = new StringBuilder( "Spieler: ")
                            .append( cave.spieler );
                        if ( cave.rang > 0 )
                        {
                            playerTxt.append( " (" )
                                .append( cave.rang )
                                .append( ")" );
                        }
                        spielerLab.setText( playerTxt.toString() );
                        clanLab.setText("Stamm: "+cave.clan);
                    }
                    else
                    {
                        hoehleLab.setText("gibts net");
                        spielerLab.setText("");
                        clanLab.setText("");
                    }
                }
                catch (Exception e)
                {
                    hoehleLab.setText("gibts net");
                    spielerLab.setText("");
                    clanLab.setText("");
                }
            }
        });
                
        String about = "Geschrieben von Clan Mitgliedern der Evolution:\n" +
            "Frank 'bruesie' Brueseke, Thorben 'tj99de' Janssen.\nVersion vom 26. Dezember 2004\n" +
            "Pflege und Überarbeitung durch squirrel (2005-2019)\n\n" +
            "Der Source Code steht zur Verfügung unter https://github.com/gregorko/Uga-Agga-Map-Viewer\n\n" +
            "Dieses Programm verwendet Code aus dem Uga-Agga - Projekt (http://sourceforge.net/projects/ugaagga/).";
        aboutTxt.setText(about);
        aboutTxt.setEditable(false);



        Panel southWest = new Panel();

        
        southWest.setLayout(new BorderLayout());
        
        pack();        
        MainFrame.centerAndSizeWindow(this, 7, 8);
    }
    
    public void loadMapData()
    {
        mapData = new MapData(cfg);
        mapData.readData();
        mapPanel.setMapModel( new MapModel( mapData, cfg) );
        searchListModel.fireChange();
        mapPanel.repaint();        
    }
    
    public void triggerRepaint()
    {
        mapPanel.repaint();
    }
        
    // Center Window on screen
    public static void centerAndSizeWindow( Window win, int fraction, int base)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width  = screenSize.width * fraction / base;
        int height = screenSize.height * fraction / base;
    
        Rectangle rect = new Rectangle( (screenSize.width - width) / 2,
            (screenSize.height - height) / 2, width, height );
        win.setBounds(rect);
    }
    
    private final class SearchListModel extends AbstractListModel<ColorItem>
    {
        public int getSize()
        {
            return mapData == null ? 0 : cfg.colorItemList.size();
        }

        public ColorItem getElementAt( int index )
        {
            return cfg.colorItemList.get( index );
        }

        public void fireChange()
        {
            fireContentsChanged(this, 0, Integer.MAX_VALUE );
        }
    }

    /**
     * Class to handle the WindowClosing event on the main frame.
     */
    private class WindowHandler extends WindowAdapter
    {
        /**
         * Just delegate to the ExitPhexAction acion.
         */
        public void windowClosing(WindowEvent e)
        {
            cfg.saveCfg();
            System.exit(0);
        }
    }
}
