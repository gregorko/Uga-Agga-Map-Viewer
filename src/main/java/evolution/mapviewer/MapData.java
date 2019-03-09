package evolution.mapviewer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import evolution.mapviewer.gui.ColorItem;

public class MapData
{
    private final Cfg cfg;

    //---------------------Groesse-der-Karte---------------------//
    private int min_x = Integer.MAX_VALUE;
    private int max_x = 0;
    private int dim_x = 0;
    
    private int min_y = Integer.MAX_VALUE;
    private int max_y = 0;
    private int dim_y = 0;
    
    private List<Hoehle> caveList = new ArrayList<>();
    private Hoehle[][] caveArray = null;
    
    private Map<String, ColorItem> colorItemMap = new HashMap<>();
    

    public MapData(Cfg cfg)
    {
        this.cfg = cfg;
        for (ColorItem item : cfg.colorItemList)
        {
            if (item.getColor() == null || item.getSearchStr() == null)
            {
                continue;
            }
            ColorItem avail = colorItemMap.get(item.getSearchStr());
            if (avail != null)
            {
                colorItemMap.remove(item.getSearchStr());
            }
            colorItemMap.put(item.getSearchStr(), item);
        }
    }
    
    public void addColorItem( ColorItem item )
    {
        System.out.println("addColorItem");
        ColorItem avail = colorItemMap.get( item.getSearchStr() );
        if ( avail != null )
        {
            removeColorItem(avail);
        }
        colorItemMap.put( item.getSearchStr(), item );
        cfg.colorItemList.add( item );
    }
    
    public void removeColorItem( ColorItem item )
    {
        System.out.println("removeColorItem");
        colorItemMap.remove( item.getSearchStr() );
        cfg.colorItemList.remove(item);
    }
    
    public ColorItem getColorItem( String searchStr )
    {
        return colorItemMap.get(searchStr);
    }   
    
    public List<Hoehle> getCaveList()
    {
        return caveList;
    }
    
    public Hoehle getCave( int x, int y )
    {
        return caveArray[x][y];
    }
    
    public void readData()
    {
        /* Diese Methode liest den Uga-Agga CSV aus:
         * 
         * >Uga Agga Karte
         * >Ab und zu wird die Karte von Uga Agga in dieser Datei abgelegt und zwar mit folgendem Format:
         * >caveID, xCoord, yCoord, 'cave_name', terrain, 'player_name', 'tribe', rank
         * >Die einzelnen Felder sind dabei Tab getrennt und jede Zeile wird mit einem Newline abgeschlossen.
         * http://www.uga-agga.org/upload/caves.tar.gz 
         */
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(cfg.caveFileGz )))))
        {
            // skip first line (contains time)
            reader.readLine();
        
            String help = reader.readLine();
            Date start = new Date();
            int prevCaveId = 0;
            while ( (help!=null) && (!help.equals("")) )
            {
                //new csv_data(interpretCSV(help));
                int stelle1=0;
                int stelle2=help.indexOf('\t');
                int caveId = Integer.parseInt(help.substring(stelle1,stelle2));

                Hoehle temp = new Hoehle();
                temp.id = caveId;
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                temp.x=Integer.parseInt(help.substring(stelle1+1,stelle2));
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                //System.out.println("debug2: "+stelle1+":"+stelle2);
                temp.y=Integer.parseInt(help.substring(stelle1 +1,stelle2));
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                temp.setName( aufbereiten(help.substring(stelle1 +1,stelle2),false) );
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                temp.terrain=Integer.parseInt(help.substring(stelle1 +1,stelle2));
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                temp.spieler = aufbereiten(help.substring(stelle1 +1,stelle2),true);
                
                stelle1=stelle2;
                stelle2=help.indexOf('\t',stelle1 +1);
                temp.clan = aufbereiten(help.substring(stelle1 +1,stelle2),true);
                
                stelle1=stelle2;
                //stelle2=help.indexOf('\t',stelle1 +1);
                if (help.substring(stelle1 +1).equals(""))
                    temp.rang=-1;
                else
                    temp.rang=Integer.parseInt(help.substring(stelle1 +1));
                
                caveList.add( temp );
                help= reader.readLine();
                
                if (temp.x < min_x) min_x = temp.x;
                if (temp.x > max_x) max_x = temp.x;
                if (temp.y < min_y) min_y = temp.y;
                if (temp.y > max_y) max_y = temp.y;
            }
            Date ende = new Date();
            //System.out.println("Datei eingelesen in "+(ende.getTime()-start.getTime())+"ms");
            //System.out.println("min.x: "+min_x+"\n"+"max.x: "+max_x+"\n"+"min.y: "+min_y+"\n"+"max.y: "+max_y+"\n");

            int diffx = max_x-min_x;
            int diffy = max_y-min_y;
            
            //vielen Dank an das UA Team ... war aber gut versteckt.
            dim_x=(diffx+1)/2;
            dim_y=(diffy+1)/2;
            caveArray = new Hoehle[diffx+1][diffy+1];

            for (Hoehle cave : caveList)
            {
                caveArray[cave.x - min_x][cave.y - min_y] = cave;
            }
            System.out.println("Datenstrukturen sind aufbereitet...");
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
    
    public int getDeltaX()
    {
        return max_x - min_x;
    }
    
    public int getDeltaY()
    {
        return max_y - min_y;
    }
    
    public int getMaxX()
    {
        return max_x;
    }

    public int getMaxY()
    {
        return max_y;
    }

    public int getMinX()
    {
        return min_x;
    }

    public int getMinY()
    {
        return min_y;
    }
    
    public int getDim_x()
    {
        return dim_x;
    }

    public int getDim_y()
    {
        return dim_y;
    }
    
    public int getTimeDistance( int x1, int y1, int x2, int y2 )
    {
        //Danke an die UA- Macher fuer diese beiden Zeilen 
        int diffx = Math.abs(x2 - x1);
        int diffy = Math.abs(y2 - y1);
        
        Double weg = Math.ceil(Math.sqrt(diffx * diffx + diffy * diffy) * 15);
        return weg.intValue();
    }

    private String aufbereiten (String word, boolean lowercase)
    {
        word = MapData.decode(word).replace('\'',' ').trim();
        if (lowercase) word = word.toLowerCase();
        return word;
    }
    
    
    
    ///////////////// HTML decode routines ///////////////////////////////
    
    
    public static String decode(String string)
    {
        StringBuilder ret = new StringBuilder(string.length());
        int index = 0;
        int length;
        int i;
        for(length = string.length(); index < length && -1 != (i = string.indexOf('&', index));)
        {
            ret.append(string, index, i);
            index = i + 1;
            char character;
            if(i < length - 1)
            {
                int semi = string.indexOf(';', i);
                String code;
                if(-1 != semi)
                    code = string.substring(i, semi + 1);
                else
                    code = string.substring(i);
                if('\0' != (character = convertToChar(code)))
                    index += code.length() - 1;
                else
                    character = '&';
            } else
            {
                character = '&';
            }
            ret.append(character);
        }

        if(index < length)
            ret.append(string.substring(index));
        return ret.toString();
    }
    
    public static char convertToChar(String string)
    {
        char ret = '\0';
        int length = string.length();
        if(0 < length)
        {
            if('&' == string.charAt(0))
            {
                string = string.substring(1);
                length--;
            }
            if(0 < length)
            {
                if(';' == string.charAt(length - 1))
                    string = string.substring(0, --length);
                if(0 < length)
                    if('#' == string.charAt(0))
                    {
                        try
                        {
                            ret = (char)Integer.parseInt(string.substring(1));
                        }
                        catch(NumberFormatException nfe) { }
                    } else
                    {
                        Character item = refChar.get(string);
                        if(null != item)
                            ret = item;
                    }
            }
        }
        return ret;
    }
    
    private static final Map<String, Character> refChar;

    static 
    {
        refChar = new HashMap<>(1000);
        refChar.put("nbsp", '\240');
        refChar.put("iexcl", '\241');
        refChar.put("cent", '\242');
        refChar.put("pound", '\243');
        refChar.put("curren", '\244');
        refChar.put("yen", '\245');
        refChar.put("brvbar", '\246');
        refChar.put("sect", '\247');
        refChar.put("uml", '\250');
        refChar.put("copy", '\251');
        refChar.put("ordf", '\252');
        refChar.put("laquo", '\253');
        refChar.put("not", '\254');
        refChar.put("shy", '\255');
        refChar.put("reg", '\256');
        refChar.put("macr", '\257');
        refChar.put("deg", '\260');
        refChar.put("plusmn", '\261');
        refChar.put("sup2", '\262');
        refChar.put("sup3", '\263');
        refChar.put("acute", '\264');
        refChar.put("micro", '\265');
        refChar.put("para", '\266');
        refChar.put("middot", '\267');
        refChar.put("cedil", '\270');
        refChar.put("sup1", '\271');
        refChar.put("ordm", '\272');
        refChar.put("raquo", '\273');
        refChar.put("frac14", '\274');
        refChar.put("frac12", '\275');
        refChar.put("frac34", '\276');
        refChar.put("iquest", '\277');
        refChar.put("Agrave", '\300');
        refChar.put("Aacute", '\301');
        refChar.put("Acirc", '\302');
        refChar.put("Atilde", '\303');
        refChar.put("Auml", '\304');
        refChar.put("Aring", '\305');
        refChar.put("AElig", '\306');
        refChar.put("Ccedil", '\307');
        refChar.put("Egrave", '\310');
        refChar.put("Eacute", '\311');
        refChar.put("Ecirc", '\312');
        refChar.put("Euml", '\313');
        refChar.put("Igrave", '\314');
        refChar.put("Iacute", '\315');
        refChar.put("Icirc", '\316');
        refChar.put("Iuml", '\317');
        refChar.put("ETH", '\320');
        refChar.put("Ntilde", '\321');
        refChar.put("Ograve", '\322');
        refChar.put("Oacute", '\323');
        refChar.put("Ocirc", '\324');
        refChar.put("Otilde", '\325');
        refChar.put("Ouml", '\326');
        refChar.put("times", '\327');
        refChar.put("Oslash", '\330');
        refChar.put("Ugrave", '\331');
        refChar.put("Uacute", '\332');
        refChar.put("Ucirc", '\333');
        refChar.put("Uuml", '\334');
        refChar.put("Yacute", '\335');
        refChar.put("THORN", '\336');
        refChar.put("szlig", '\337');
        refChar.put("agrave", '\340');
        refChar.put("aacute", '\341');
        refChar.put("acirc", '\342');
        refChar.put("atilde", '\343');
        refChar.put("auml", '\344');
        refChar.put("aring", '\345');
        refChar.put("aelig", '\346');
        refChar.put("ccedil", '\347');
        refChar.put("egrave", '\350');
        refChar.put("eacute", '\351');
        refChar.put("ecirc", '\352');
        refChar.put("euml", '\353');
        refChar.put("igrave", '\354');
        refChar.put("iacute", '\355');
        refChar.put("icirc", '\356');
        refChar.put("iuml", '\357');
        refChar.put("eth", '\360');
        refChar.put("ntilde", '\361');
        refChar.put("ograve", '\362');
        refChar.put("oacute", '\363');
        refChar.put("ocirc", '\364');
        refChar.put("otilde", '\365');
        refChar.put("ouml", '\366');
        refChar.put("divide", '\367');
        refChar.put("oslash", '\370');
        refChar.put("ugrave", '\371');
        refChar.put("uacute", '\372');
        refChar.put("ucirc", '\373');
        refChar.put("uuml", '\374');
        refChar.put("yacute", '\375');
        refChar.put("thorn", '\376');
        refChar.put("yuml", '\377');
        refChar.put("fnof", '\u0192');
        refChar.put("Alpha", '\u0391');
        refChar.put("Beta", '\u0392');
        refChar.put("Gamma", '\u0393');
        refChar.put("Delta", '\u0394');
        refChar.put("Epsilon", '\u0395');
        refChar.put("Zeta", '\u0396');
        refChar.put("Eta", '\u0397');
        refChar.put("Theta", '\u0398');
        refChar.put("Iota", '\u0399');
        refChar.put("Kappa", '\u039A');
        refChar.put("Lambda", '\u039B');
        refChar.put("Mu", '\u039C');
        refChar.put("Nu", '\u039D');
        refChar.put("Xi", '\u039E');
        refChar.put("Omicron", '\u039F');
        refChar.put("Pi", '\u03A0');
        refChar.put("Rho", '\u03A1');
        refChar.put("Sigma", '\u03A3');
        refChar.put("Tau", '\u03A4');
        refChar.put("Upsilon", '\u03A5');
        refChar.put("Phi", '\u03A6');
        refChar.put("Chi", '\u03A7');
        refChar.put("Psi", '\u03A8');
        refChar.put("Omega", '\u03A9');
        refChar.put("alpha", '\u03B1');
        refChar.put("beta", '\u03B2');
        refChar.put("gamma", '\u03B3');
        refChar.put("delta", '\u03B4');
        refChar.put("epsilon", '\u03B5');
        refChar.put("zeta", '\u03B6');
        refChar.put("eta", '\u03B7');
        refChar.put("theta", '\u03B8');
        refChar.put("iota", '\u03B9');
        refChar.put("kappa", '\u03BA');
        refChar.put("lambda", '\u03BB');
        refChar.put("mu", '\u03BC');
        refChar.put("nu", '\u03BD');
        refChar.put("xi", '\u03BE');
        refChar.put("omicron", '\u03BF');
        refChar.put("pi", '\u03C0');
        refChar.put("rho", '\u03C1');
        refChar.put("sigmaf", '\u03C2');
        refChar.put("sigma", '\u03C3');
        refChar.put("tau", '\u03C4');
        refChar.put("upsilon", '\u03C5');
        refChar.put("phi", '\u03C6');
        refChar.put("chi", '\u03C7');
        refChar.put("psi", '\u03C8');
        refChar.put("omega", '\u03C9');
        refChar.put("thetasym", '\u03D1');
        refChar.put("upsih", '\u03D2');
        refChar.put("piv", '\u03D6');
        refChar.put("bull", '\u2022');
        refChar.put("hellip", '\u2026');
        refChar.put("prime", '\u2032');
        refChar.put("Prime", '\u2033');
        refChar.put("oline", '\u203E');
        refChar.put("frasl", '\u2044');
        refChar.put("weierp", '\u2118');
        refChar.put("image", '\u2111');
        refChar.put("real", '\u211C');
        refChar.put("trade", '\u2122');
        refChar.put("alefsym", '\u2135');
        refChar.put("larr", '\u2190');
        refChar.put("uarr", '\u2191');
        refChar.put("rarr", '\u2192');
        refChar.put("darr", '\u2193');
        refChar.put("harr", '\u2194');
        refChar.put("crarr", '\u21B5');
        refChar.put("lArr", '\u21D0');
        refChar.put("uArr", '\u21D1');
        refChar.put("rArr", '\u21D2');
        refChar.put("dArr", '\u21D3');
        refChar.put("hArr", '\u21D4');
        refChar.put("forall", '\u2200');
        refChar.put("part", '\u2202');
        refChar.put("exist", '\u2203');
        refChar.put("empty", '\u2205');
        refChar.put("nabla", '\u2207');
        refChar.put("isin", '\u2208');
        refChar.put("notin", '\u2209');
        refChar.put("ni", '\u220B');
        refChar.put("prod", '\u220F');
        refChar.put("sum", '\u2211');
        refChar.put("minus", '\u2212');
        refChar.put("lowast", '\u2217');
        refChar.put("radic", '\u221A');
        refChar.put("prop", '\u221D');
        refChar.put("infin", '\u221E');
        refChar.put("ang", '\u2220');
        refChar.put("and", '\u2227');
        refChar.put("or", '\u2228');
        refChar.put("cap", '\u2229');
        refChar.put("cup", '\u222A');
        refChar.put("int", '\u222B');
        refChar.put("there4", '\u2234');
        refChar.put("sim", '\u223C');
        refChar.put("cong", '\u2245');
        refChar.put("asymp", '\u2248');
        refChar.put("ne", '\u2260');
        refChar.put("equiv", '\u2261');
        refChar.put("le", '\u2264');
        refChar.put("ge", '\u2265');
        refChar.put("sub", '\u2282');
        refChar.put("sup", '\u2283');
        refChar.put("nsub", '\u2284');
        refChar.put("sube", '\u2286');
        refChar.put("supe", '\u2287');
        refChar.put("oplus", '\u2295');
        refChar.put("otimes", '\u2297');
        refChar.put("perp", '\u22A5');
        refChar.put("sdot", '\u22C5');
        refChar.put("lceil", '\u2308');
        refChar.put("rceil", '\u2309');
        refChar.put("lfloor", '\u230A');
        refChar.put("rfloor", '\u230B');
        refChar.put("lang", '\u2329');
        refChar.put("rang", '\u232A');
        refChar.put("loz", '\u25CA');
        refChar.put("spades", '\u2660');
        refChar.put("clubs", '\u2663');
        refChar.put("hearts", '\u2665');
        refChar.put("diams", '\u2666');
        refChar.put("quot", '"');
        refChar.put("amp", '&');
        refChar.put("lt", '<');
        refChar.put("gt", '>');
        refChar.put("OElig", '\u0152');
        refChar.put("oelig", '\u0153');
        refChar.put("Scaron", '\u0160');
        refChar.put("scaron", '\u0161');
        refChar.put("Yuml", '\u0178');
        refChar.put("circ", '\u02C6');
        refChar.put("tilde", '\u02DC');
        refChar.put("ensp", '\u2002');
        refChar.put("emsp", '\u2003');
        refChar.put("thinsp", '\u2009');
        refChar.put("zwnj", '\u200C');
        refChar.put("zwj", '\u200D');
        refChar.put("lrm", '\u200E');
        refChar.put("rlm", '\u200F');
        refChar.put("ndash", '\u2013');
        refChar.put("mdash", '\u2014');
        refChar.put("lsquo", '\u2018');
        refChar.put("rsquo", '\u2019');
        refChar.put("sbquo", '\u201A');
        refChar.put("ldquo", '\u201C');
        refChar.put("rdquo", '\u201D');
        refChar.put("bdquo", '\u201E');
        refChar.put("dagger", '\u2020');
        refChar.put("Dagger", '\u2021');
        refChar.put("permil", '\u2030');
        refChar.put("lsaquo", '\u2039');
        refChar.put("rsaquo", '\u203A');
        refChar.put("euro", '\u20AC');
    }
}