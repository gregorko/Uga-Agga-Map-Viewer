package evolution.mapviewer.gui;

import java.awt.Rectangle;

import evolution.mapviewer.Hoehle;

public class CavePaintingModel
{
    private Hoehle cave;
    
    // paintdata
    private Rectangle rec;
    private String drawString;
    
    public CavePaintingModel( Hoehle cave )
    {
        this.cave = cave;
        rec = new Rectangle( );
    }
    
    public String getDrawString()
    {
        if ( drawString == null )
        {
            drawString = cave.spieler.substring( 0, Math.min( cave.spieler.length(), 3 ) );
        }
        return drawString;
    }
    
    public Rectangle getPaintRec()
    {
        return rec;
    }
    
    public void validatePaintingModel( MapModel map )
    {
        if ( updateRec( map ) )
        {
            calculate( map );
        }
    }
    
    private boolean updateRec( MapModel map )
    {
        int cx = map.translateXm2v( cave.x );
        int cy = map.translateYm2v( cave.y );
        int px = cx * ( map.getZoom() + map.getGridSize() ) + 1;
        int py = cy * ( map.getZoom() + map.getGridSize() ) + 1;
        
        if ( rec.x != px || rec.y != py || rec.width != map.getZoom() )
        {
            rec.setBounds( px, py, map.getZoom(), map.getZoom() );
            return true;
        }
        return false;
    }
    
    private void calculate( MapModel map )
    {
    }
}