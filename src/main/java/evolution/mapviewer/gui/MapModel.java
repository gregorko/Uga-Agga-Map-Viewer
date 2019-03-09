package evolution.mapviewer.gui;

import java.awt.Color;

import evolution.mapviewer.Cfg;
import evolution.mapviewer.Hoehle;
import evolution.mapviewer.MapData;

public class MapModel
{
    private final MapData data;
    private final Cfg cfg;
    
    private int zoom;
    private int topX;
    private int topY;
    
    
    public MapModel(MapData data, Cfg cfg)
    {
        this.data = data;
        this.cfg = cfg;
        topX = 1;
        topY = 1;
    }
    
    public void setNewRoot( int x, int y )
    {
        topX = x;
        topY = y;
    }
    
    public int getRootX()
    {
        return topX;
    }
    
    public int getRootY()
    {
        return topY;
    }
    
    public int getCaveCountX()
    {
        return data.getMaxX() - data.getMinX() + 1;
    }
    
    public int getCaveCountY()
    {
        return data.getMaxY() - data.getMinY() + 1;
    }    
    
    public int getZoom()
    {
        return zoom;
    }
    
    public void setZoom( int z )
    {
        this.zoom = z;
    }
    
    public Hoehle getCave( int cx, int cy )
    {
        if ( cx < 0 || cx > getCaveCountX() || cy < 0 || cy > getCaveCountY() )
        {
            return null;
        }
        int ncx = translateXv2m( cx );
        int ncy = translateYv2m( cy );
        return data.getCave( ncx, ncy );
    }
    
    public int translateXv2m( int cx )
    {
        int tx = cx - data.getMinX() + topX - 1;
        if ( tx >= getCaveCountX() )
        {
            tx -= getCaveCountX();
        }
        return tx;
    }
    public int translateYv2m( int cy )
    {
        int ty = cy - data.getMinY() + topY - 1;
        if ( ty >= getCaveCountY() )
        {
            ty -= getCaveCountY();
        }
        return ty;
    }
    
    public int translateXm2v( int cx )
    {
        int tx = cx - data.getMinX() - topX + 1;
        if ( tx < 0 )
        {
            tx += getCaveCountX();
        }
        return tx;
    }
    public int translateYm2v( int cy )
    {
        int ty = cy - data.getMinY() - topY + 1;
        if ( ty < 0 )
        {
            ty += getCaveCountY();
        }
        return ty;
    }
    
    public Color getCaveDrawingColor( Hoehle cave )
    {
        Color color = null;
        ColorItem item = data.getColorItem( cave.spieler );
        if ( item == null )
        {
            item = data.getColorItem( cave.clan );
        }
        if ( item != null )
        {
            color = item.getColor();
        }
        return color;
    }
    
    public int getGridSize()
    {
        return cfg.isGridShown ? 1 : 0;
    }
}
