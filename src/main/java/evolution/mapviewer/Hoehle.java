package evolution.mapviewer;

import java.io.Serializable;

import evolution.mapviewer.gui.CavePaintingModel;

public class Hoehle implements Serializable
{
    // Die Daten kommen hier rein:
    // x,y - Koordinaten
    // id - eine eindeutige ID für jede Höhle
    // name - Name der Höhle
    // spieler - Name des Spielers
    // clan - Clan bzw. Stamm dem der Spieler angehört
    // terrain - auf welchem Terrain befindet sich die Höhle: Wald, Ebene, Gebirge, Sumpf
    public int x,y=0;
    public int id=0;
    private String name = "";
    public int terrain = 1;
    public String spieler = "";
    public String clan = "";
    public int rang;
    
    private CavePaintingModel paintingModel;
    
    public Hoehle()
    {
        paintingModel = new CavePaintingModel( this );
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName( String name )
    {
        this.name = name;
    }
    
    public CavePaintingModel getPaintingModel()
    {
        return paintingModel;
    }
    
    public String toString()
    {
        return (spieler+" - "+clan+" aus "+name+" in ("+x+"|"+y+")");
    }
}