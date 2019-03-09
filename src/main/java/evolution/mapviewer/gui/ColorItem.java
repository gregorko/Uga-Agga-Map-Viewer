package evolution.mapviewer.gui;

import java.awt.Color;
import java.io.Serializable;

public class ColorItem implements Serializable
{
    private Color color;
    private String searchStr;
    
    public ColorItem(  )
    {
    }
    
    public ColorItem( Color col, String str )
    {
        color = col;
        searchStr = str;
    }
    
    public Color getColor()
    {
        return color;
    }

    public String getSearchStr()
    {
        return searchStr;
    }
    
    public void setColor( Color color )
    {
        this.color = color;
    }

    public void setSearchStr( String searchStr )
    {
        this.searchStr = searchStr;
    }

    public boolean equals( Object obj )
    {
        if ( !(obj instanceof ColorItem) )
        {
            return false;
        }
        ColorItem itm = (ColorItem) obj;
        return color.equals( itm.color ) && searchStr.equals( itm.searchStr );
    }
    
    public int hashCode()
    {
        return 17 * color.hashCode() + 17 * searchStr.hashCode();
    }
}
