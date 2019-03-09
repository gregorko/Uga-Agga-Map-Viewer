package evolution.mapviewer;

import java.awt.Color;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import evolution.mapviewer.gui.ColorItem;

public class Cfg
{
    public static final String DEFAULT_CAVE_FILE_URL = "https://game.uga-agga.de/upload/cave.gz";
    public static final transient File CONFIG_DIR = new File(
        System.getProperty("user.home") + File.separator + ".ua_map_viewer" );
    public static final transient File CONFIG_FILE = new File(
        CONFIG_DIR + File.separator + "ua_map_viewer2.cfg" );
    
    public Color terrain0;
    public Color terrain1;
    public Color terrain2;
    public Color terrain3;
    public Color terrain4;
    public Color terrain5;
    public Color mapBackground;
    public boolean isGridShown;
    // Raster -> Unterteilung der Karte in 2*2; 3*3; usw.
    public int raster;
    public int minLetterSize;
    public int mapZoom;
    public File caveFileGz;
    public List<ColorItem> colorItemList;
    
    public String caveFileURL;
    public String httpProxyHost;
    public int httpProxyPort;
     
    
    public Cfg()
    {
        loadDefaults();
        loadCfg();
    }
    
    private void loadDefaults()
    {
        CONFIG_DIR.mkdirs();
        terrain0 = getTerrain0Default();
        terrain2 = getTerrain2Default();
        terrain1 = getTerrain1Default();
        terrain3 = getTerrain3Default();
        terrain4 = getTerrain4Default();
        terrain5 = getTerrain5Default();
        mapBackground = getMapBackgroundDefault();
        isGridShown = isGridShownDefault();
        raster = getRasterDefault();
        mapZoom = 5;
        minLetterSize = getMinLetterSizeDefault();
        caveFileGz = new File( CONFIG_DIR, "caves.gz" );
        colorItemList = new ArrayList<>();
        caveFileURL = DEFAULT_CAVE_FILE_URL;
        httpProxyHost = "";
        httpProxyPort = 0;
    }
    
    private void loadCfg()
    {
        File xmlConfig = new File( CONFIG_FILE.getAbsolutePath()+".xml");
        
        if (!xmlConfig.exists())
        {
            System.out.println("Konfigurationsdatei '"+xmlConfig.getAbsolutePath()+"' existiert nicht");
        }
        try
        {
            FileInputStream filein= new FileInputStream(xmlConfig);
            XMLDecoder xmlin = new XMLDecoder(filein);
            
            terrain0 = (Color) xmlin.readObject();
            terrain1 = (Color) xmlin.readObject();
            terrain2 = (Color) xmlin.readObject();
            terrain3 = (Color) xmlin.readObject( );
            terrain4 = (Color) xmlin.readObject();
            terrain5 = (Color) xmlin.readObject();
            mapBackground = (Color) xmlin.readObject();
            isGridShown = (Boolean) xmlin.readObject();
            raster = (Integer) xmlin.readObject();
            minLetterSize = (Integer) xmlin.readObject();
            String caveFileGzName = (String) xmlin.readObject();
            if ( caveFileGzName.length() > 0)
            {
                caveFileGz = new File( caveFileGzName );
            }
            colorItemList = (List<ColorItem>)xmlin.readObject();
            
            caveFileURL = (String)xmlin.readObject();
            httpProxyHost = (String)xmlin.readObject();
            httpProxyPort = (Integer) xmlin.readObject();
            mapZoom = (Integer) xmlin.readObject();

            xmlin.close();  
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            aioobe.printStackTrace();
            System.out.println("Fehler beim Lesen der Konfigurationsdatei: Es sollten mehr Werte gelesen werden als in der Datei stehen.");
            System.out.println("Vermutlich haben sie k\u00fcrzlich ein Versionupdate gemacht ;)");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //Vermutlich ist das der erste Start der Anwendung.
            System.out.println("Fehler beim Lesen der Konfigurationsdatei:"+e.toString());
        }
    }
    
    public void saveCfg()
    {
        File xmlConfig = new File( CONFIG_FILE.getAbsolutePath() + ".xml" );
        try
        {
            xmlConfig.delete();
            xmlConfig.createNewFile();
            FileOutputStream fileout= new FileOutputStream(xmlConfig);
            XMLEncoder xout = new XMLEncoder( fileout );
            
            xout.writeObject( terrain0 );
            xout.writeObject( terrain1 );
            xout.writeObject( terrain2 );
            xout.writeObject( terrain3 );
            xout.writeObject( terrain4 );
            xout.writeObject( terrain5 );
            xout.writeObject( mapBackground );
            xout.writeObject(isGridShown);
            xout.writeObject(raster);
            xout.writeObject(minLetterSize);
            if ( caveFileGz == null )
            {
                xout.writeObject( "" );
            }
            else
            {
                xout.writeObject( caveFileGz.getAbsolutePath() );
            }
            xout.writeObject( colorItemList );
            xout.writeObject( caveFileURL );
            xout.writeObject( httpProxyHost );
            xout.writeObject(httpProxyPort);
            xout.writeObject(mapZoom);
            
            xout.flush();
            xout.close();   
        }
        catch (Exception exp)
        {
            exp.printStackTrace();
            System.out.println("Fehler beim Schreiben der Konfigurationsdatei:"
                + exp.toString());
        }
    }
    
    
    public Color getTerrain5Default()
    {
        return new Color(255, 223, 123);
    }

    public Color getTerrain4Default()
    {
        return new Color(204, 204, 153);
    }

    public Color getTerrain3Default()
    {
        return new Color(231, 195, 165);
    }

    public Color getTerrain1Default()
    {
        return new Color(198, 227, 156);
    }

    public Color getTerrain2Default()
    {
        return new Color(198, 195, 165);
    }

    public Color getTerrain0Default()
    {
        return new Color(247, 243, 198);
    }
    
    public int getMinLetterSizeDefault()
    {
        return 6;
    }

    public int getRasterDefault()
    {
        return 1;
    }

    public boolean isGridShownDefault()
    {
        return true;
    }

    public Color getMapBackgroundDefault()
    {
        return Color.WHITE;
    }
}
