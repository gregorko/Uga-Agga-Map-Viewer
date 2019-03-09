package evolution.mapviewer.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import evolution.mapviewer.Cfg;
import evolution.mapviewer.Hoehle;

public class MapPanel extends JComponent implements MouseMotionListener, MouseWheelListener, MouseListener
{
    private final Cfg cfg;

    private MapModel map;

    //caves will be a square with length of variable zoom
    private int zoom;
    
    private JScrollPane parentScrollPane;
    
    public MapPanel(Cfg cfg)
    {
        super();
        this.cfg = cfg;

        setOpaque(true);
        setAutoscrolls(true);
        zoom = cfg.mapZoom;
        zoom = Math.max( 1, Math.min( zoom, 100 ) );
        
        addMouseMotionListener( this );
        addMouseWheelListener( this );
        addMouseListener( this );
    }
    
    public JScrollPane getParentScrollPane()
    {
        if ( parentScrollPane == null )
        {
            parentScrollPane = new JScrollPane( this );
            parentScrollPane.getHorizontalScrollBar().setUnitIncrement(zoom);
            parentScrollPane.getVerticalScrollBar().setUnitIncrement(zoom);
        }
        return parentScrollPane;
    }
    
    public void resetRoot()
    {
        map.setNewRoot( 1, 1 );
        revalidate();
        repaint();
    }
    
    public int getZoom()
    {
        return zoom;
    }
    
    public void setZoom( int val, Hoehle centerCave )
    {
        if ( zoom == val )
        {
            return;
        }
        // ensure 1-100
        val = Math.max( 1, Math.min( val, 100 ) );
        
        Rectangle lastVisibleRect = getVisibleRect();
        
        if ( centerCave == null )
        {
            Dimension prefSize = getPreferredSize();
            // make sure our calculated middle is in bounds...
            lastVisibleRect.width = Math.min(lastVisibleRect.width, prefSize.width );
            lastVisibleRect.height = Math.min(lastVisibleRect.height, prefSize.height );
            Point middle = new Point( (int)lastVisibleRect.getCenterX(),
                (int)lastVisibleRect.getCenterY() );
            centerCave = getCaveForPoint(middle);
        }
        
        // in case our parent is a JScrollPane we need to temporarly change the
        // scroll mode to simple to prevent double redraws with blit copied 
        // viewport that has not zoomed yet
        if ( getParent() instanceof JViewport )
        {
            ((JViewport)getParent()).setScrollMode( JViewport.SIMPLE_SCROLL_MODE ); 
        }
        
        int oldVal = zoom;
        zoom = val;
        map.setZoom( zoom );
        if ( parentScrollPane != null )
        {
            parentScrollPane.getHorizontalScrollBar().setUnitIncrement(zoom);
            parentScrollPane.getVerticalScrollBar().setUnitIncrement(zoom);
        }
        cfg.mapZoom = zoom;
        
        if ( centerCave != null )
        {
            Rectangle caveRect = centerCave.getPaintingModel().getPaintRec();            
            double relX = ((double)(caveRect.x-lastVisibleRect.x) / (double)lastVisibleRect.width);
            double relY = ((double)(caveRect.y-lastVisibleRect.y) / (double)lastVisibleRect.height);
            lastVisibleRect.translate( (int)((lastVisibleRect.width / oldVal)*relX),
                  (int)((lastVisibleRect.height / oldVal)*relY) );
        }
        
        revalidate();
        repaint();
        scrollRectToVisible( lastVisibleRect );
        
        // reset viewport scroll mode
        if ( getParent() instanceof JViewport )
        {
            ((JViewport)getParent()).setScrollMode( JViewport.BLIT_SCROLL_MODE ); 
        }
        
        firePropertyChange("zoom", oldVal, zoom );
    }
    
    public void setMapModel( MapModel map )
    {
        this.map = map;
        map.setZoom( zoom );
    }
    
    public Hoehle getCaveForPoint( Point p )
    {
        Insets insets = getInsets();
        int innerX = (int) p.getX() - 1 - insets.left;
        if ( innerX < 0 )
        {
            return null;
        }
        int innerY = (int) p.getY() - 1 - insets.top;
        if ( innerY < 0 )
        {
            return null;
        }
        
        int cx = innerX / (zoom + map.getGridSize()) + 1;
        int cy = innerY / (zoom + map.getGridSize()) + 1;
        
        return map.getCave( cx, cy );
    }
    
    public Color getBackground( )
    {
        return cfg.mapBackground;
    }

    public void paintComponent( Graphics g )
    {
        super.paintComponent(g);
        
        g.getClipBounds( clipBounds );
        // first paint bg if opaque
        if ( isOpaque() )
        {
            g.setColor( getBackground() );
            g.fillRect( clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height );
        }
        
        // honor possible insets for border
        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight()- insets.top - insets.bottom;
        Graphics2D g2 = (Graphics2D) g.create(insets.left, insets.top, w, h);
        try
        {
            insideBorderBounds.setBounds(0, 0, w, h);
            paintInsideBorder( g2 );
        } 
        finally
        {
            g2.dispose();
        }
    }
    
    //buffered to prevent recreation
    private Rectangle insideBorderBounds = new Rectangle();
    private Rectangle clipBounds = new Rectangle();
    
    private void paintInsideBorder( Graphics2D g2 )
    {
        // only the dirty clipping area needs repaint 
        Shape clip = g2.getClip();
        g2.getClipBounds( clipBounds );
        
        if ( map == null )
        {
            return;
        }
        
        // contains the caves to draw names for.
        List<Hoehle> markedCaves = new ArrayList<>();
        for ( int cx = 1; cx < map.getCaveCountX()+1; cx++ )
        {
            for ( int cy = 1; cy < map.getCaveCountY()+1; cy++ )
            {
                paintCave( cx, cy, markedCaves, clip, g2 );
            }
        }
        
        
        if ( zoom >= cfg.minLetterSize && markedCaves.size() > 0)
        {
            //drawCaveMarkings( g2, markedCaves );
            drawCaveMarkingsColl( g2, markedCaves );
        }
        
        //zeichne Raster
        int raster = cfg.raster;
        if ( raster >= 1 )
        {
            g2.setColor( Color.BLACK );
            for (int i = 0; i < raster + 1; ++i)
            {                
                // horizontal
                int fromX = 0;
                int fromY = ( map.getCaveCountY() * ( zoom + map.getGridSize() ) )* i / raster;
                int toX = map.getCaveCountX() * ( zoom + map.getGridSize() );
                int toY = fromY;
                g2.drawLine( fromX, fromY, toX, toY );

                // vertical
                fromX = ( map.getCaveCountX() * ( zoom + map.getGridSize() ) ) * i / raster;
                fromY = 0;
                toX = fromX;
                toY = map.getCaveCountY() * ( zoom + map.getGridSize() );
                g2.drawLine( fromX, fromY, toX, toY );
            }
        }
    }
    
    private void paintCave( int cx, int cy, List<Hoehle> markedCaves, Shape clip, Graphics2D g2 )
    {
        Hoehle cave = map.getCave( cx, cy );
        if ( cave == null )
        {
            return;
        }
        CavePaintingModel paintingModel = cave.getPaintingModel();
        paintingModel.validatePaintingModel( map );
        
        Rectangle caveRect = paintingModel.getPaintRec();

        // only draw enlarged caves in the clipping area. we use a enlargement
        // to make sure text from caves bordering to the clipping area is drawn. 
        if ( !clip.intersects( caveRect.x - (zoom - map.getGridSize())*5, 
                               caveRect.y - (zoom - map.getGridSize())*5, 
                               (zoom + map.getGridSize() ) * 11, 
                               (zoom + map.getGridSize() ) * 11 ) )
        {
            return;
        }
        Color terrainCol = getTerrainColor( cave.terrain );
        Color caveCol = map.getCaveDrawingColor( cave );
        if ( caveCol != null )
        {
            markedCaves.add( cave );
        }
        g2.setColor( caveCol == null ? terrainCol : caveCol );
        g2.fill( caveRect );
    }

    /**
     * @param g2
     * @param markedCaves
     */
    private void drawCaveMarkings( Graphics2D g2, ArrayList markedCaves )
    {
        Iterator iterator;
        // paint in inverted color over background.
        g2.setColor( Color.WHITE );
        g2.setXORMode( Color.BLACK );
        
        // anti alias makes small chars appear too fat....
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
        //    RenderingHints.VALUE_ANTIALIAS_ON);
        float fontSize = zoom + 3;
        Font font = g2.getFont().deriveFont( fontSize );
        g2.setFont( font );
        FontMetrics metrics = g2.getFontMetrics();
        
        // iterate over caves to draw names for
        iterator = markedCaves.iterator();
        while ( iterator.hasNext() )
        {
            Hoehle cave = (Hoehle) iterator.next();
            Rectangle caveRec = cave.getPaintingModel().getPaintRec();
            
            String text = cave.spieler.substring( 0, 
                Math.min( 3, cave.spieler.length() ) );
            Rectangle2D bound =  metrics.getStringBounds(text, g2);                
            
            int xOff = (int)(caveRec.x + zoom/2.0 - bound.getWidth()/2.0);
            int yOff = (int)(caveRec.y + zoom/2.0 + bound.getMaxY() );
            
            g2.drawString( text, xOff, yOff);
        }
        
        // release inverted color paint
        g2.setPaintMode();
    }
    
    /**
     * @param g2
     * @param markedCaves
     */
    // TODO this algorithm can be highly performance optimized by using a buffer object
    // like a CavePaintingModel attached to the Cave object that stores calculated values.
    private void drawCaveMarkingsColl( Graphics2D g2, List<Hoehle> markedCaves )
    {
        Iterator iterator;
        // paint in inverted color over background.
        
        // XOR is very slow since 1.6u10 -> http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6635462
        g2.setXORMode( Color.BLACK );
        
        // anti alias makes small chars appear too fat....
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
        //    RenderingHints.VALUE_ANTIALIAS_ON);
        float fontSize = zoom + 3;
        Font font = g2.getFont().deriveFont( fontSize );
        g2.setFont( font );
        
        
        //long start = System.currentTimeMillis();
        List<TextBounds> textBoundsList = new ArrayList<>();
        // iterate over caves to draw names for
        iterator = markedCaves.iterator();
        while ( iterator.hasNext() )
        {
            Hoehle cave = (Hoehle) iterator.next();
            CavePaintingModel paintingModel = cave.getPaintingModel();
            Rectangle caveRect = paintingModel.getPaintRec();
            
            String text = paintingModel.getDrawString();
            GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), text );
            Rectangle2D visualBounds = gv.getVisualBounds();
            
            double txtX = caveRect.getCenterX() - visualBounds.getWidth()/2.0;
            if ( txtX < 0 ) txtX = 0;
            if ( txtX + visualBounds.getWidth() > insideBorderBounds.width )
            {
                txtX = insideBorderBounds.width - visualBounds.getWidth() - 1;
            }
            double txtY = caveRect.getCenterY() + visualBounds.getHeight()/2.0;
            
            Point2D drawingPoint = new Point2D.Float( (float)txtX, (float)txtY );
            
            Shape textOutline = gv.getOutline( (float)txtX, (float)txtY);
            Rectangle2D textBounds = textOutline.getBounds2D();
            Rectangle2D intrusionBounds = textBounds.getBounds2D();
            intrusionBounds.setRect(
                textBounds.getX() - textBounds.getWidth(),
                textBounds.getY() - textBounds.getHeight(),
                textBounds.getWidth() * 3, textBounds.getHeight() * 3);
            
            
            // basically we could start painting here already... but we want
            // to have a simple collision detection.
            ///// start of test mode painting ////
            //g2.setPaintMode();
            //g2.setColor(Color.RED);
            //g2.draw( intrusionBounds );
            //g2.draw( textBounds );
            //g2.fill( textOutline );
            //g2.setXORMode( Color.BLACK );
            ////// end of test mode painting ////
            
            TextBounds textBound = new TextBounds( cave, text, gv, drawingPoint, textBounds, 
                visualBounds, intrusionBounds );
            for (TextBounds oponentTb : textBoundsList)
            {
                textBound.findPossibleIntruder(oponentTb);
            }
            textBoundsList.add( textBound );
        }
        
        Set<TextBounds> visited = new HashSet<>();
        for (TextBounds textBounds : textBoundsList)
        {
            textBounds.handleIntrusions(visited);
            visited.clear();

            Hoehle cave = textBounds.cave;
            Rectangle caveRect = cave.getPaintingModel().getPaintRec();
            double midX = caveRect.getCenterX();
            double midY = caveRect.getCenterY();
            if (!textBounds.currentTextBounds.contains(midX, midY))
            {
                // draw a line from the text into the cave rectangle
                int destX = (int) textBounds.currentTextBounds.getCenterX();
                int destY = (int) textBounds.currentTextBounds.getCenterY();
                g2.drawLine(destX, destY, (int) midX, (int) midY);
            }

//            Shape outline = textBounds.gv.getOutline( (float)textBounds.curDrawingPoint.getX(), 
//                (float)textBounds.curDrawingPoint.getY() );
//            g2.fill( outline );
            g2.drawString(textBounds.text, (float) textBounds.curDrawingPoint.getX(),
                (float) textBounds.curDrawingPoint.getY());
        }
        
        //long end = System.currentTimeMillis();
        
        //System.out.println( "coll: " + (end-start) );
        
        // release inverted color paint
        g2.setPaintMode();
    }
    
    ////////////// start standard size methods /////////////////////
    public Dimension getPreferredSize() {
        if ( isPreferredSizeSet() )
            return super.getPreferredSize();
        else
            return computePreferredSize();
    }
    public Dimension getMaximumSize() {
        if ( isMaximumSizeSet() )
            return super.getMaximumSize();
        else
            return computeMaximumSize();
    }
    public Dimension getMinimumSize() {
        if ( isMinimumSizeSet() )
            return super.getMinimumSize();
        else
            return computeMinimumSize();
    }
    private boolean isPreferredSizeSet = false;
    private boolean isMaximumSizeSet = false;
    private boolean isMinimumSizeSet = false;
    public boolean isPreferredSizeSet() {
        return isPreferredSizeSet;
    }
    public void setPreferredSize(Dimension sz) {
        isPreferredSizeSet = (sz!=null);
        super.setPreferredSize(sz);
    }
    public boolean isMaximumSizeSet() {
        return isMaximumSizeSet;
    }
    public void setMaximumSize(Dimension sz) {
        isMaximumSizeSet = (sz!=null);
        super.setMaximumSize(sz);
    }
    public boolean isMinimumSizeSet() {
        return isMinimumSizeSet;
    }
    public void setMinimumSize(Dimension sz) {
        isMinimumSizeSet = (sz!=null);
            super.setMinimumSize(sz);
    }

    
    private Dimension prefSize = new Dimension();
    protected Dimension computePreferredSize()
    {
        if ( map == null )
        {
            prefSize.height = 0;
            prefSize.width = 0;
            return prefSize;
        }
        Insets insets = getInsets();
        int h = insets.top + insets.bottom + 2 
            + map.getCaveCountY() * ( zoom + map.getGridSize() ); 
        int w = insets.left + insets.right + 2 
            + map.getCaveCountX() * ( zoom + map.getGridSize() );
        prefSize.height = h;
        prefSize.width = w;
        return prefSize;
    }
    
    //Usually my panels don't have different max/min sizes, hence the code below...
    protected Dimension computeMaximumSize()
    {
        return computePreferredSize();
    }

    protected Dimension computeMinimumSize()
    {
        return computePreferredSize();
    }
    //////////////end standard size methods /////////////////////
    
    private Color getTerrainColor( int terrain )
    {
        switch ( terrain )
        {
        case 0:
            return cfg.terrain0;
        case 1:
            return cfg.terrain1;
        case 2:
            return cfg.terrain2;
        case 3:
            return cfg.terrain3;
        case 4:
            return cfg.terrain4;
        case 5:
            return cfg.terrain5;
        default:
            //System.out.println( "Unbekanntes terrain " + terrain);
            return Color.PINK;
        }
    }
    
    // START MouseMotionListener, MouseWheelListener, MouseListener ////////////
    
    private Hoehle mouseDragCave;
    
    public void mouseDragged( MouseEvent e )
    {
        if ( !e.isPopupTrigger() )
        {
            if ( mouseDragCave == null )
            {
                mouseDragCave = getCaveForPoint( e.getPoint() );
            }
            if ( mouseDragCave == null )
            {
                return;
            }
            Hoehle cave = getCaveForPoint( e.getPoint() );
            if ( cave == null )
            {
                return;
            }
            
            int diffX = mouseDragCave.x - cave.x;
            int diffY = mouseDragCave.y - cave.y;
            
            int newX = map.getRootX() + diffX;
            if ( newX > map.getCaveCountX() )
            {
                newX -= map.getCaveCountX();
            }
            else if ( newX <= 0 )
            {
                newX += map.getCaveCountX();
            }
            int newY = map.getRootY() + diffY;
            if ( newY > map.getCaveCountY() )
            {
                newY -= map.getCaveCountY();
            }
            else if ( newY <= 0 )
            {
                newY += map.getCaveCountY();
            }
            map.setNewRoot( newX, newY );
            revalidate();
            repaint();
        }
    }

    public void mouseMoved( MouseEvent evt )
    {
        try
        {
            Point koord = evt.getPoint();
            Hoehle cave = getCaveForPoint( koord );
            if (cave!=null)
            {
                String txt = "<html><B>" + cave.getName() + "</b> in " + cave.x + " | " + cave.y +
                    "<br><b>Spieler:</b> " +cave.spieler + "<br><b>Stamm:</b> " + cave.clan + "</html>";
                setToolTipText(txt);
            }
            else
            {
                setToolTipText(null);
            }
        }
        catch (Exception e)
        {
            setToolTipText(null);
        }
    }
    
    public void mousePressed(MouseEvent e)
    {
        if ( !e.isPopupTrigger() )
        {
            mouseDragCave = getCaveForPoint( e.getPoint() );
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if ( e.isPopupTrigger() )
        {
            e.consume();
            Hoehle cave = getCaveForPoint( e.getPoint() );
            int midx = map.getCaveCountX()/2;
            int midy = map.getCaveCountY()/2;
            int topX = cave.x - midx;
            if ( topX < 1 )
            {
                topX += map.getCaveCountX();
            }
            int topY = cave.y - midy;
            if ( topY < 1 )
            {
                topY += map.getCaveCountY();
            }
            map.setNewRoot( topX, topY );
            revalidate();
            repaint();
        }
        else
        {
            mouseDragCave = null;
        }
    }
    
    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    
    
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        //if ( e.isAltDown() )
        {
            e.consume();
            int direction = e.getWheelRotation() < 0 ? 1 : -1;
            Hoehle cave = getCaveForPoint( e.getPoint() );
            setZoom( zoom + direction, cave );
        }
//        else
//        {
//            // forward to possible JViewport parent
//            if ( getParent() instanceof JViewport )
//            {
//                getParent().dispatchEvent(e); 
//            }
//        }
    }
    
    
    // END MouseMotionListener, MouseWheelListener, MouseListener ////////////
    
    
    private class TextBounds
    {
        private ArrayList<TextBounds> possibleIntruders;
        private GlyphVector gv;
        private Point2D orgDrawingPoint;
        private Point2D curDrawingPoint;
        private Rectangle2D currentTextBounds;
        private Rectangle2D visualBounds;
        private Rectangle2D intrusionBounds;
        private String text;
        private Hoehle cave;
        private boolean moved;
        
        public TextBounds( Hoehle cave, String text, GlyphVector gv,
            Point2D drawingPoint, Rectangle2D currentTextBounds,
            Rectangle2D visualBounds, Rectangle2D intrusionBounds )
        {
            this.cave = cave;
            this.text = text;
            this.gv = gv;
            this.orgDrawingPoint = drawingPoint;
            this.curDrawingPoint = drawingPoint;
            this.currentTextBounds = currentTextBounds;
            this.visualBounds = visualBounds;
            this.intrusionBounds = intrusionBounds;
        }

        public void findPossibleIntruder( TextBounds oponentTb )
        {
            if ( !oponentTb.intrusionBounds.intersects( intrusionBounds ) )
            {
                return;
            }
            if ( possibleIntruders == null )
            {   //lazy init..
                possibleIntruders = new ArrayList<>();
            }
            possibleIntruders.add( oponentTb );
            if ( oponentTb.possibleIntruders == null )
            {   //lazy init..
                oponentTb.possibleIntruders = new ArrayList<>();
            }
            oponentTb.possibleIntruders.add( this );
        }
        
        public void handleIntrusions( Set<TextBounds> visited )
        {
            // our algorithem basically works as follows:
            // - we try to find the most outer bounds of the intrusion chain
            //   by using a simple visitor pattern.
            // - Starting from the outer bounds we place the labels the 9 different
            //   position inside the starting area with two different lengths away
            //   away from the middle.
            // - Once we found a spot without intrusion we move back inwards.
            if ( possibleIntruders == null )
            {
                return;
            }
            // add me to the visited obj so I dont get handled again
            visited.add( this );
            for (TextBounds posInt : possibleIntruders)
            {
                if (!posInt.currentTextBounds.intersects(currentTextBounds))
                {
                    continue;
                }
                if (visited.contains(posInt))
                {
                    continue;
                }
                posInt.handleIntrusions(visited);
            }
            // we now handled all outer bounds...
            // now solve myself
            solveIntrusion( );
        }
      
        private void solveIntrusion()
        {
            Point2D bestPoint = (Point2D) orgDrawingPoint.clone();
            Rectangle2D testBounds = gv.getOutline( 
                (float)bestPoint.getX(), (float)bestPoint.getY()).getBounds2D();
            Rectangle2D bestTextBounds = testBounds;

            // validate if our current position is intruding..
            double bestIntrusionVal = getIntrusionVal( testBounds );
            
            // now we try to find a better position.
            double newX = 0;
            double newY = 0;
            // outer loop handles the distance from the center.
            for( int dist = 1; dist <= 4 && bestIntrusionVal > 0; dist++ )
            {
                // inner loop handles the position corner of the square
                for ( int pos = 0; pos < 8 && bestIntrusionVal > 0; pos++ )
                {
                    switch ( pos )
                    {
                    case 0:
                        newX = orgDrawingPoint.getX();
                        newY = orgDrawingPoint.getY() - visualBounds.getHeight() * dist / 4 ;
                        break;
                    case 1:
                        newX = orgDrawingPoint.getX() + visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY() - visualBounds.getHeight() * dist / 4;
                        break;
                    case 2:
                        newX = orgDrawingPoint.getX() + visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY();
                        break;
                    case 3:
                        newX = orgDrawingPoint.getX() + visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY() + visualBounds.getHeight() * dist / 4;
                        break;
                    case 4:
                        newX = orgDrawingPoint.getX();
                        newY = orgDrawingPoint.getY() + visualBounds.getHeight() * dist / 4;
                        break;
                    case 5:
                        newX = orgDrawingPoint.getX() - visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY() + visualBounds.getHeight() * dist / 4;
                        break;
                    case 6:
                        newX = orgDrawingPoint.getX() - visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY();
                        break;
                    case 7:
                        newX = orgDrawingPoint.getX() - visualBounds.getWidth() * dist / 4;
                        newY = orgDrawingPoint.getY() - visualBounds.getHeight()/2 * dist / 4;
                        break;
                    }
                    testBounds = gv.getOutline( (float)newX, (float)newY ).getBounds2D();
                    double testVal = getIntrusionVal( testBounds );
                    if ( testVal < bestIntrusionVal )
                    {
                        bestIntrusionVal = testVal;
                        bestPoint.setLocation( newX, newY );
                        bestTextBounds = testBounds;
                    }
                }
            }
            curDrawingPoint = bestPoint;
            currentTextBounds = bestTextBounds;
        }

        private double getIntrusionVal( Rectangle2D bestPos )
        {
            double intrusionVal = 0;
            for (Object possibleIntruder : possibleIntruders)
            {
                TextBounds posInt = (TextBounds) possibleIntruder;
                if (!bestPos.intersects(posInt.currentTextBounds))
                {
                    continue;
                }

                Rectangle2D intersec = bestPos.createIntersection(posInt.currentTextBounds);
                intrusionVal += intersec.getWidth();
                intrusionVal += intersec.getHeight();
            }
            return intrusionVal;
        }        
    }
 }