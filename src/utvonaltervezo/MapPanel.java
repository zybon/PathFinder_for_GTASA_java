package utvonaltervezo;

import utvonaltervezo.data.Utvonal;
import utvonaltervezo.data.Ut;
import utvonaltervezo.data.Pont;
import utvonaltervezo.data.CsomoPont;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import utvonaltervezo.EszkozTar.Parancs;
import static utvonaltervezo.UtvonalTervezo.frameInfo;
import utvonaltervezo.data.Teglalap;
import utvonaltervezo.data.UtHalozat;
import static utvonaltervezo.data.UtHalozat.CSOMOPONT_ATMERO;
import static utvonaltervezo.data.UtHalozat.TORESPONT_ATMERO;


/**
 *
 * @author Zybon
 * Created time: 2016.09.04 10:52:43
 */
public class MapPanel extends JPanel{
    
    private BufferedImage kep;
    
    private float kepSzel;
    private float kepMag;
    private int kepX = 0;//-4773;//-6430;
    private int kepY = 0;//-1010;//-3630;    
    private int egerX = -6430;
    private int egerY = -3630;       
    
    private Color betuSzin = new Color(0xff00ff);
    private final Font aFont = new Font("Times New Roman", Font.BOLD, 3);
    
    private final UtvonalTervezo frame;
    private int jelolendoIndex = -1;
    
    private float zoom = 1;//;13.65f;//5.1f;
//    public static final int CSOMOPONT_ATMERO = 5;
//    public static final int TORESPONT_ATMERO = 3;    
    private final Color kijeloesSzin = new Color(0xaa22ff22, true);

    
    Ut ujUt;
    private Ut utTemp;
    private Pont pontTemp;
    
    private final EgerEsemeny eger = new EgerEsemeny();
    private Point egerPos = new Point();
    private int kepDX;
    private int kepDY;
    
    
    Mutato mutato = new Mutato();
    private Cursor elozoCursor = Cursor.getDefaultCursor();
    private VillanoCsp villanoCsp = new VillanoCsp();
    
    final UtHalozat utHalozat = new UtHalozat(
        "res/cspok.zyb",
        "res/utak.zyb"
    );
    private Utvonal utvonal = new Utvonal(utHalozat);
    private final Stroke stroke2_5 = new BasicStroke(3.5f);
    private final Stroke stroke1_5 = new BasicStroke(1.5f);
    private final Stroke stroke0_5 = new BasicStroke(0.5f);
    private Color teglalapSzin = new Color(0xff00ff00, true);
    private Color szovegHatter = new Color(0x88000000, true);
    private boolean vanUtvonal;

//    private Info info = new Info();


    enum Folyamat{
        UresJarat,
        CsomoPontRajzolas,
        ToresPontRajzolas,
        PontMozgatas,
        KetUtEgyesites,
        UtKezdoCspKereses,
        UtPontRajzolas,
        UtEgyiranyusitasUtKereses,
        UtEgyiranyusitasKezdoCspKereses,
        KepernyoMozgatas,
        UtvonalKezdoCspKereses,
        UtvonalVegCspKereses,
        Torles,
        AutoUtRajzolasKp,
        AutoUtRajzolas, 
        UtMasolas
    }

    Folyamat folyamat = Folyamat.UresJarat;
    private Parancs utolsoParancs = Parancs.SEMMI;
    
    private final AutoRajzolo autoRajzolo;
    
    private Teglalap screen = new Teglalap();
    
    public MapPanel(UtvonalTervezo _frame) {
        frame = _frame;
//        setPreferredSize(new Dimension(1000,800));
        try {
            kep = ImageIO.read(new File("res/map.jpg"));
            this.kepSzel = kep.getWidth();
            this.kepMag = kep.getHeight();                
            autoRajzolo = new AutoRajzolo(this, kep);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        addMouseListener(eger);
        addMouseMotionListener(eger);
        addMouseWheelListener(eger);
        setBackground(new Color(0x66999a));
        mutato.setAlapSzin(Color.ORANGE);   
        
        zoom = getToolkit().getScreenSize().height/kepMag;
        
    }
    
    public void utHalozatBeolvasas(){
        utHalozat.beolvasas();
    }
    
    public void parancsKuldes(Parancs parancs){
        utolsoParancs = parancs;
        switch (parancs) {
            case UJ_CSOMOPONT:
                createUjCsp();
                break;
            case UJ_TORESPONT:
                createUjToresPont();
                break;                
            case UJ_UT:
                createUjUt();
                break;
            case UT_MASOLAS:
                utMasolasKezd();
                break;                
            case KET_UT_EGYESITES:
                egyesites();
                break;    
            case EGYIRANYU_UT:
                egyiranyusitas();
                break;                
            case TORLES:
                torlesKezd();
                break;
            case UTVONALTERVKP:
                utvonalTervezesKPkereses();
                break;
            case UTVONALTERVVP:
                utvonalTervezesVPkereses();
                break;
            case UTVONALTERVTERVEZ:
                utvonalTervez();
                break;    
            case UJRATOLT:
                utHalozat.reload();
//                setLathatoResz();
                break;
            case MENTES:
//                frameInfo("a mentés ki van kapcsolva");
                utHalozat.mentes();
                break; 
            case AUTOUTRAJZ:
                autoUtRajzKezd();
                break;                 
        }        
    }
    
    private void autoUtRajzKezd(){
        folyamat = Folyamat.AutoUtRajzolasKp;
        frameInfo("Kattints egy pontra ahol út csomópont lesz");
    }
    
   

    
    private void autoKezdoPont(){
        mutato.setAtmero(TORESPONT_ATMERO);
        folyamat = Folyamat.AutoUtRajzolas;
        autoRajzolo.start();
    }
 
    
    private void cursorValtas(Cursor c){
        elozoCursor = getCursor();
        setCursor(c);
    }

    private void kepKozepre(){
        zoom = (float)getHeight()/kepMag;
        if (kepSzel*zoom>getWidth()) { //
            zoom = (float)getWidth()/kepSzel;
        }
        kepPozEll(0, 0);            
    }   
    
    private void kepPozEll(int ujX, int ujY){
//        if (kepSzel*zoom<getWidth()) {
////            ujX = getWidth()/2-(int)(kepSzel*zoom/2);
//            ujX = 0;
//        }                
//        else {
//            if (ujX+kepSzel*zoom < getWidth()) {
//                ujX = getWidth()-(int)(kepSzel*zoom);
//            }                     
//            if (ujX>0) {
//                ujX = 0;
//            }
//        }
//        if (kepMag*zoom<getHeight()) {
////            ujY = getHeight()/2-(int)(kepMag*zoom/2);
//            ujY = 0;
//        }                
//        else {
//            if (ujY+kepMag*zoom < getHeight()) {
//                ujY = getHeight()-(int)(kepMag*zoom);
//            }                     
//            if (ujY>0) {
//                ujY = 0;
//            }
//        }                

        kepX = ujX;
        kepY = ujY;
        //repaint();
    }


    

    @Override
    public void paint(Graphics g) {
        super.paint(g); 
        screen.set((int)(-kepX/zoom), (int)(-kepY/zoom), (int)((-kepX+getWidth())/zoom), (int)((-kepY+getHeight())/zoom));
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON); 
        Font f = g.getFont();
        g2.setFont(aFont);
        g2.translate(kepX, kepY);
        g2.scale(zoom, zoom);
        g.drawImage(kep, 0, 0, null);   
//        g.drawImage(autoRajzolo.ffKep, 0, 0, null);   
        drawTartalom(g2);
        if (folyamat == Folyamat.CsomoPontRajzolas 
                || folyamat == Folyamat.ToresPontRajzolas
                || folyamat == Folyamat.UtKezdoCspKereses
                || folyamat == Folyamat.UtPontRajzolas) {
            mutato.draw(g);
        }
        g2.scale(1/zoom, 1/zoom);        
        
        g2.translate(-kepX, -kepY);

        g.setColor(szovegHatter);
        g.fillRect(0, 0, getWidth(), 100);
        g.setFont(f);
        g.setColor(Color.GREEN);
        g.drawString("kepX = "+kepX+", kepY = "+kepY+", zoom = "+zoom, 20, 20);
//        g.drawString("screen = "+screen, 20, 40);
        g.drawString("folyamat: "+folyamat, 20, 40);
        g.drawString("egerX = "+mutato.x+", egerY = "+mutato.y, 1000, 20);
        g.drawString("utRajzDb = "+utRajzDb, 1000, 40);
        g.drawString("cspRajzDb = "+cspRajzDb, 1000, 60);
        
//        info.draw(g2);
    }
    
    
    private int utRajzDb = 0;
    private int cspRajzDb = 0;
    private void drawTartalom(Graphics2D g) {
        Ut[] utak = new Ut[utHalozat.getUtak().size()];
        utHalozat.getUtak().toArray(utak);   
        utRajzDb = 0;
        for (Ut ut : utak) {
            drawUt(g, ut);
        }
        g.setStroke(stroke1_5);
        CsomoPont[] cspok = new CsomoPont[utHalozat.getCsomopontok().size()];
        utHalozat.getCsomopontok().toArray(cspok);
        cspRajzDb = 0;
        for (CsomoPont csp : cspok) {
            drawCsomoPont(g, csp);
        }        
        villanoCsp.draw(g);
        if (folyamat == Folyamat.UtKezdoCspKereses||
                folyamat == Folyamat.UtPontRajzolas ||
                folyamat == Folyamat.AutoUtRajzolas) {
            ujUtRajzolas(g);
            mutato.draw(g);
        }
        if (vanUtvonal){
            drawUtvonal(g);
        }
        if (utvonal.isStartBeallitva()){
            g.setColor(Color.RED);
            g.fillOval(utvonal.getStartPont().getX()-CSOMOPONT_ATMERO/2, 
                    utvonal.getStartPont().getY()-CSOMOPONT_ATMERO/2,
                    CSOMOPONT_ATMERO, CSOMOPONT_ATMERO);            
        }
        if (utvonal.isCelBeallitva()){
            g.setColor(Color.BLUE);
            g.fillOval(utvonal.getCelPont().getX()-CSOMOPONT_ATMERO/2, 
                    utvonal.getCelPont().getY()-CSOMOPONT_ATMERO/2,
                    CSOMOPONT_ATMERO, CSOMOPONT_ATMERO);            
        }        
        
       
    }
    
    public void ujUtRajzolas(Graphics2D g){
        if (ujUt == null) {return;}
        if (ujUt.getKezdoCsomopont() == null){return;}
        Pont tK = ujUt.getKezdoCsomopont();
        Pont tV = ujUt.getKezdoCsomopont();
        ArrayList<Pont> toresPontok = ujUt.getToresPontok();
        for (int i=0, n=toresPontok.size();i<n;i++){
            tV = toresPontok.get(i);
            drawToresPont(g, tV);
            g.setColor(Color.YELLOW);
            g.drawLine(tK.getX(), tK.getY(), tV.getX(), tV.getY());
            if (ujUt.isEgyiranyu()) {
                drawEgyIranyuJel(g, tK, tV);
            }             
            tK = toresPontok.get(i);
        }
        g.setColor(Color.YELLOW);
        g.drawLine(tV.getX(), tV.getY(), mutato.x, mutato.y);            
        
    }    
    
    
    private void drawUt(Graphics2D g, Ut ut){
        if (ut.getKezdoCsomopont() == null){return;}
//        if (!ut.isEgyiranyu()) {return;}
//        g.setColor(getUtSzin(ut));
        Pont tK = ut.getKezdoCsomopont();
        Pont tV = ut.getKezdoCsomopont();
        ArrayList<Pont> toresPontok = ut.getToresPontok();
        for (int i=0, n=toresPontok.size();i<n;i++){
            tV = toresPontok.get(i);
            if (!screen.benneVan(tK) && !screen.benneVan(tV)) {
                tK = toresPontok.get(i);
                continue;
            }
            drawToresPont(g, tV);
            g.setColor(getUtSzin(ut));
            g.setStroke(stroke1_5);
            g.drawLine(tK.getX(), tK.getY(), tV.getX(), tV.getY());
            if (ut.isEgyiranyu()) {
                g.setStroke(stroke0_5);
                drawEgyIranyuJel(g, tK, tV);
            }            
            tK = toresPontok.get(i);
            utRajzDb++;
        }
        if (!screen.benneVan(tV) && !screen.benneVan(ut.getVegCsomopont())) {return;}
        g.setColor(getUtSzin(ut));
        g.setStroke(stroke1_5);
        g.drawLine(tV.getX(), tV.getY(), ut.getVegCsomopont().getX(), ut.getVegCsomopont().getY());   
        if (ut.isEgyiranyu()) {
            g.setStroke(stroke0_5);
            drawEgyIranyuJel(g, tV, ut.getVegCsomopont());
        }
        g.setColor(Color.RED);
        g.drawString(""+(int)ut.getHossz(), (tV.x+ut.getVegCsomopont().x)/2-3, (tV.y+ut.getVegCsomopont().y)/2+2);
        utRajzDb++;
    }
    
    private void drawUtvonal(Graphics2D g){
        ArrayList<Pont> utv = utvonal.getUtvonal();
        g.setColor(Color.GREEN);
        g.setStroke(stroke2_5);
        Pont tK = utv.get(0);
        Pont tV = utv.get(1);
        for (int i=1, n=utv.size();i<n;i++){
            tV = utv.get(i);
            g.drawLine(tK.getX(), tK.getY(), tV.getX(), tV.getY());
            tK = utv.get(i);
        }
        g.drawLine(tK.getX(), tK.getY(), tV.getX(), tV.getY());
    }
        
    
    private void drawEgyIranyuJel(Graphics2D g, Pont kP, Pont vP) {
        Pont kozepPont = kP.getKozepPont(vP);
        double r = Math.atan2(vP.y-kP.y, vP.x-kP.x);
        int meret = 2;
        g.rotate(r, kozepPont.x, kozepPont.y);
            g.setColor(Color.CYAN);
            g.drawLine(kozepPont.x, kozepPont.y-meret, kozepPont.x, kozepPont.y+meret);
            g.drawLine(kozepPont.x+meret, kozepPont.y, kozepPont.x, kozepPont.y-meret);
            g.drawLine(kozepPont.x+meret, kozepPont.y, kozepPont.x, kozepPont.y+meret);
        g.rotate(-r, kozepPont.x, kozepPont.y);
    }    
    
    private Color getUtSzin(Ut ut){
        if (ut.isRejtett()) {
            return Color.GRAY;
        }        
        if (ut.isKijeloles()) {
            return Color.GREEN;
        }        
       if (ut.isUtvonalAtnezte()) {
            return Color.RED;
        }        

        return Color.YELLOW;
    }
    
    private void drawCsomoPont(Graphics g, CsomoPont csp){
        if (!screen.benneVan(csp)) {return;}
//        g.setColor(teglalapSzin);
//        g.fillRect(csp.getTeglalap().getLeft(), csp.getTeglalap().getTop(), 
//                csp.getTeglalap().getSzel()+1, csp.getTeglalap().getMag()+1);        
        g.setColor(csp.isKijeloles()?kijeloesSzin:Color.MAGENTA);
        g.fillOval(csp.getX()-CSOMOPONT_ATMERO/2, csp.getY()-CSOMOPONT_ATMERO/2,
                CSOMOPONT_ATMERO, CSOMOPONT_ATMERO);

        g.setColor(Color.BLUE);
        g.drawString(""+csp.getRunID(), csp.x-2, csp.y+1);
        cspRajzDb++;
    }  
    
    private void drawToresPont(Graphics g, Pont pont){
        g.setColor(pont.isKijeloles()?kijeloesSzin:Color.YELLOW);
        g.fillOval(pont.getX()-TORESPONT_ATMERO/2, pont.getY()-TORESPONT_ATMERO/2,
                TORESPONT_ATMERO, TORESPONT_ATMERO);
    }     
    
//    private void negyzetesKijelolesKezd(MouseEvent me) {
//        folyamat = Folyamat.Kijeloles;
//        kijeloles.aktiv = true;
//        if (!me.isShiftDown()) {
//            utHalozat.reset();
//        }        
//        kijeloles.setKezd(mutato.getX(), mutato.getY());
//        kijeloles.setVeg(mutato.getX(), mutato.getY());
//    }    
    
//    private void negyzetesKijelolesVeg(MouseEvent me) {
//        if (!me.isShiftDown()) {
//            utHalozat.reset();
//        }
//        kijeloles.setVeg(mutato.getX(), mutato.getY());
//
//    } 
    
//    private void negyzetesKijelolesBefejez() {
//        folyamat = Folyamat.UresJarat;
//        kijeloles.aktiv = false;   
//    }     
//    
    private void kepernyoMozgatasStart(MouseEvent e){
        kepDX = e.getX()-kepX;
        kepDY = e.getY()-kepY;                    
        cursorValtas(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));  
        folyamat = Folyamat.KepernyoMozgatas;
//        kepernyoMozgatas = true;    
    }   
    
    private void kepernyoMozgatasVege(){
        cursorValtas(elozoCursor);  
        folyamat = Folyamat.UresJarat;
//        kepernyoMozgatas = true;    
    }  
    
    private void reset(){
        vanUtvonal = false;
        utHalozat.reset();
//        utvonalAtnezettek.clear();    
    }
    
    public void createUjCsp() {
        reset();
        folyamat = Folyamat.CsomoPontRajzolas;
        mutato.setAlapSzin(Color.MAGENTA);
        mutato.setAtmero(CSOMOPONT_ATMERO);
        frameInfo("Új csomópont beszúrása");
    }
    
    public void createUjToresPont() {
        reset();
        folyamat = Folyamat.ToresPontRajzolas;
        mutato.setAlapSzin(Color.YELLOW);
        mutato.setAtmero(TORESPONT_ATMERO);        
        frameInfo("Új töréspont beszúrása");
    }    
    
    private void pontElhuzasKezd() {
        reset();
        Pont pont = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (pont == null) {
            pont = utHalozat.toresPontKeresesAkozelben(mutato);
        }         
        if (pont != null) {
            folyamat = Folyamat.PontMozgatas;
            cursorValtas(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            pont.setKijeloles(true);
            pontTemp = pont;
        } 
    }   
    
    private void pontMozgatas(){
        pontTemp.mozgatIde(mutato.x, mutato.y);
    }
    
    private void pontMozgatasVege(){
        folyamat = Folyamat.UresJarat;
        cursorValtas(elozoCursor);
        pontTemp.setKijeloles(false);
    }    
    
    private void torlesKezd(){
        
        folyamat = Folyamat.Torles;
//        setTorlesMutato();
        cursorValtas(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public void torles(){
        if (utHalozat.csomoPontTorles(mutato, CSOMOPONT_ATMERO)) {
            return;
        }
        if (utHalozat.toresPontTorles(mutato, TORESPONT_ATMERO)){
            return;
        }
        utHalozat.utTorles(mutato);
    }    
    
    private void egyiranyusitas(){
        reset();
        folyamat = Folyamat.UtEgyiranyusitasUtKereses;
        frameInfo("Válaszd ki az utat");
        cursorValtas(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    private void utKivalasztEgyiranyusitashoz(){
        Ut ut = utHalozat.utKeresesAkozelben(mutato);
        if (ut == null) {
            frameInfo("Nincs itt út");
            return;
        }
        ut.setKijeloles(true);
        utTemp = ut;
        frameInfo("Válaszd ki a kezdő csomópontot");
        folyamat = Folyamat.UtEgyiranyusitasKezdoCspKereses;
    }
    
    private void utTempVegPontVillantas(){
        CsomoPont kezdoCsp = null;
        if (utTemp.getKezdoCsomopont().getTav(mutato)<CSOMOPONT_ATMERO) {
            kezdoCsp = utTemp.getKezdoCsomopont();
        }
        if (utTemp.getVegCsomopont().getTav(mutato)<CSOMOPONT_ATMERO) {
            kezdoCsp = utTemp.getVegCsomopont();
        }    
        if (kezdoCsp != null) {
           villanoCsp.villant(kezdoCsp);
        }         
    }
    
    private void kezdoPontKivalasztEgyiranyusitashoz(){
        CsomoPont kezdoCsp = null;
        if (utTemp.getKezdoCsomopont().getTav(mutato)<CSOMOPONT_ATMERO) {
            kezdoCsp = utTemp.getKezdoCsomopont();
        }
        if (utTemp.getVegCsomopont().getTav(mutato)<CSOMOPONT_ATMERO) {
            kezdoCsp = utTemp.getVegCsomopont();
        }        
        if (kezdoCsp == null) {
            frameInfo("Az út egyik végpontjára kattints");
            return;
        }    
        utTemp.setEgyiranyuKezdoPont(kezdoCsp);
        utTemp.setKijeloles(false);
        egyiranyusitas();
    }
    
    private void csomopontBeszuras() {
        if (utHalozat.csomoPontBeszuras(mutato) == null) {
            frameInfo("nincs út a közelben");
        }
    }   
    
    private void torespontBeszuras() {
        if (!utHalozat.toresPontBeszuras(mutato)) {
            frameInfo("nincs út a közelben");
        }
    }  
    
    private void egyesites() {
        reset();
//        ujUtRajzolas = true;
//        kezdoCspKereses = true;
        folyamat = Folyamat.KetUtEgyesites;
        cursorValtas(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        frameInfo("Két út egyesitese");
        frameInfo("Olyan csomópontot válassz amibe csak két út fut");
    }   
    
    private void egyesiteshezCspKivalasztas(){
        CsomoPont lehetsegesCsp = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (lehetsegesCsp == null) {
            frameInfo("Itt nincs csomópont");
            return;
        }    
        if (lehetsegesCsp.getUtak().size() != 2) {
            frameInfo("Olyan csomópontot válassz amibe csak két út fut");
            return;
        }
        utHalozat.ketUtEgyesitese(lehetsegesCsp, true);
//        folyamat = Folyamat.UresJarat;
//        cursorValtas(Cursor.getDefaultCursor());
    }
    
    private void utMasolasKezd() {
        reset();
        cursorValtas(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        folyamat = Folyamat.UtMasolas;
    }
    
    private void utMasolas(){
        Ut utAkozelben = utHalozat.utKeresesAkozelben(mutato);
        if (utAkozelben == null) {
            frameInfo("Nincs út a közelben!");
            return;
        }
        
        Ut ut0 = utAkozelben.masol();
        Ut ut1 = ut0.csomoPontBeszuras(new CsomoPont(mutato));
        utHalozat.addUt(ut0);
        utHalozat.addUt(ut1);
        utHalozat.addCsomoPont(ut0.getVegCsomopont());
        utAkozelben.setRejtett(true);
//        frameInfo("Lemásolva:\n"+utAkozelben);
        
    }
        
    
    public void createUjUt() {
        reset();
//        ujUtRajzolas = true;
        ujUt = new Ut(UtHalozat.getUjId());
//        kezdoCspKereses = true;
        folyamat = Folyamat.UtKezdoCspKereses;
        mutato.setAlapSzin(Color.MAGENTA);
        mutato.setAtmero(CSOMOPONT_ATMERO);         
        frameInfo("Új út létrehozása");
        frameInfo("Bal egérgomb: út kezdete csomópont");
        frameInfo("Középső egérgomb: út rajzolásának megszakítása");
    }
    
    private void utKezdoCsp(){
        CsomoPont lehetsegesCsp = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (lehetsegesCsp == null) {
            lehetsegesCsp = new CsomoPont(mutato);
            utHalozat.addCsomoPont(lehetsegesCsp);
        }
        ujUt.setKezdoCsomopont(lehetsegesCsp);
//        kezdoCspKereses = false;
        frameInfo("Bal egérgomb: új töréspontok");
        frameInfo("Jobb egérgomb: út vége csomópont");
        frameInfo("Középső egérgomb: út rajzolásának megszakítása");
        mutato.setAlapSzin(Color.YELLOW);
        mutato.setAtmero(TORESPONT_ATMERO); 
        folyamat = Folyamat.UtPontRajzolas;
    }
    
    private void utAddToresPont(){
        if (ujUt.getToresPontAkozelben(mutato, TORESPONT_ATMERO) == null) {
            ujUt.addToresPont(mutato);
            frameInfo("Töréspont hozzáadva: "+mutato);
        }
    }
    
    private void utVegCsp(MouseEvent e){
        CsomoPont lehetsegesCsp = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (lehetsegesCsp == null) {
            lehetsegesCsp = new CsomoPont(mutato);
            utHalozat.addCsomoPont(lehetsegesCsp);
        }
        ujUt.setVegCsomopont(lehetsegesCsp);
        ujUt.initHossz();
        if (ujUt.getHossz() == 0 ) {
            frameInfo("Nem lehet az út hossza: 0!");
            return;
        }
        
        utHalozat.addUt(ujUt);
        frameInfo("Kész!");
        frameInfo(ujUt.toString());
        ujUt = new Ut(UtHalozat.getUjId());
        ujUt.setKezdoCsomopont(lehetsegesCsp);        

//        folyamat = Folyamat.UtPontRajzolas;
    }    
    
    private void utRajzolasMegszakitasa() {
        ujUt.getKezdoCsomopont().removeUt(ujUt);
        frameInfo(folyamat+" vége");
        folyamat = Folyamat.UresJarat;            
    }    
        
    private void csomoPontVillantasaAkozelben() {
        CsomoPont csp = utHalozat.csomoPontKeresesAkozelben(mutato);
         if (csp != null) {
            villanoCsp.villant(csp);
         }            
    }    
    
    public void utvonalTervezesKPkereses() {
        folyamat = Folyamat.UtvonalKezdoCspKereses;
        frameInfo("Jelöld meg a kezdőcsomópontot! (bal egérgomb)");
    }
     
    private void utvonalTervKezdoCsomopont(){
        String start = utvonal.setStart(mutato);
        frameInfo(start);
    } 
    
    public void utvonalTervezesVPkereses() {
        folyamat = Folyamat.UtvonalVegCspKereses;
        frameInfo("Jelöld meg a végcsomópontot! (bal egérgomb)");                        
    }
    
    private void utvonalTervVegCsomopont(){
        String cel = utvonal.setCel(mutato);
        frameInfo(cel);
    }   
    
    private void utvonalTervez(){
        String eredmeny = utvonal.tervez();
        StringBuilder str = new StringBuilder();
        str.append("Hossz = ").append(utvonal.getHossz());
        str.append("  \tlépésszám: ").append(utvonal.getLepesSzam());
//        str.append("\tkeresesTipus: ").append(keresesTipus);
        frameInfo(eredmeny);
        frameInfo(str);
        for (Ut ut : utHalozat.getUtak()) {
            ut.setKijeloles(false);
            ut.setUtvonalAtnezte(false);
        }
        vanUtvonal = utvonal.vanUtvonal();
    }
    
    private class EgerEsemeny implements MouseListener, MouseMotionListener, MouseWheelListener{
        
        private final Pont pressedPont = new Pont();

        @Override
        public void mousePressed(MouseEvent e) {
            pressedPont.set(mutato);
            
            switch (e.getButton()) {
                case 1:
                    balEgerPress(e);
                    break;
                case 2:
                    kozepsoEgerPress(e);
                    break;
                case 3:
                    jobbEgerPress(e);
                    break;                    
            }
        }
        
        private void balEgerPress(MouseEvent e){
            
            switch (folyamat) {
                case AutoUtRajzolasKp:
                    autoKezdoPont();
//                    szinezes();
                    break;
                case CsomoPontRajzolas:
                    csomopontBeszuras();
                    break;
                case ToresPontRajzolas:
                    torespontBeszuras();
                    break; 
                case UtMasolas:
                    utMasolas();
                    break;                    
                case KetUtEgyesites:
                    egyesiteshezCspKivalasztas();
                    break;
                case UtKezdoCspKereses:
                    utKezdoCsp();
                    break; 
                case UtPontRajzolas:
                    utAddToresPont();
                    break; 
                case UtEgyiranyusitasUtKereses:
                    utKivalasztEgyiranyusitashoz();
                    break;   
                case UtEgyiranyusitasKezdoCspKereses:
                    kezdoPontKivalasztEgyiranyusitashoz();
                    break;                     
                case UtvonalKezdoCspKereses:
                    utvonalTervKezdoCsomopont();
                    break;   
                case UtvonalVegCspKereses:
                    utvonalTervVegCsomopont();
                    break;                     
                case Torles:
                    torles();
                    break;                      
                case UresJarat:
                    pontElhuzasKezd();
                    break;                    
//                default:
//                    throw new AssertionError();
            }
        }
        
        private void kozepsoEgerPress(MouseEvent e){
            switch (folyamat) {
                case UtKezdoCspKereses:
                case UtPontRajzolas:
                    utRajzolasMegszakitasa();
                    break;
                default:
                    kepernyoMozgatasStart(e);
            }
            
        }        

        private void jobbEgerPress(MouseEvent e){
            switch (folyamat) {
                case UtPontRajzolas:
                    utVegCsp(e);
                    break;                 
                case UresJarat:
                    parancsKuldes(utolsoParancs);
                    break;
//                case CsomoPontRajzolas:
//                case ToresPontRajzolas:
//                case Torles:
                default:
                    frameInfo(folyamat+" megszakítva");
                    folyamat = Folyamat.UresJarat;
                    setCursor(Cursor.getDefaultCursor());
            }
        }          
                

        @Override
        public void mouseReleased(MouseEvent e) {
            switch (folyamat) {
                case KepernyoMozgatas:
                    kepernyoMozgatasVege();
                    break;  
                case PontMozgatas:
                    pontMozgatasVege();
                    break;     
//                default:
//                    throw new AssertionError();
            }
        }


        @Override
        public void mouseClicked(MouseEvent e) {
            
        }        

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mutato.set(e);
            switch (folyamat) {
                case KepernyoMozgatas:
                    kepPozEll((int) (e.getX()-kepDX), (int) (e.getY()-kepDY));
                    break;
                case PontMozgatas:
                    pontMozgatas();
                    break;    
//                return;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            egerPos.setLocation(e.getPoint());
            if (folyamat == Folyamat.AutoUtRajzolas) {return;}
            mutato.set(e);
//            mutVon.set(mutato.x-5, mutato.y, mutato.x+5, mutato.y);
            if (folyamat == Folyamat.UtKezdoCspKereses ||
                    folyamat == Folyamat.UtvonalKezdoCspKereses ||
                    folyamat == Folyamat.UtvonalVegCspKereses ||
                    folyamat == Folyamat.UtPontRajzolas) {
                csomoPontVillantasaAkozelben();
                return;
            }
            if (folyamat == Folyamat.UtEgyiranyusitasKezdoCspKereses) {
                utTempVegPontVillantas();
            }
        }        

        
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (folyamat == Folyamat.KepernyoMozgatas) {return;}
            kepDX = e.getX()-kepX;
            kepDY = e.getY()-kepY;
            float elKepSzel = zoom*kepSzel;
            float elKepMag = zoom*kepMag;
            if (e.getWheelRotation()<0) {
                zoom *= 1.1;
            }
            else {
                zoom /= 1.1;
            }
//            csp_atmero = Math.max(2,(int)(20/zoom));
//            csp_atmero = Math.min(5,csp_atmero);
//            frame.adatok.setZoom(zoom);
            kepPozEll((int) (e.getX()-(zoom*kepSzel/elKepSzel)*kepDX), (int) (e.getY()-(zoom*kepMag/elKepMag)*kepDY));            
//            frameInfo("csp_atmero = "+(csp_atmero));
        }





    }

 
   
    public class Mutato extends Pont{
        
        private Color alapSzin;
        private int atmero = 1;

        private void setAlapSzin(Color szin) {
            this.alapSzin = szin;
        }
        
        private void set(MouseEvent me){
            set((int)(((me.getX()-kepX)/zoom)+0.5f),
            (int)(((me.getY()-kepY)/zoom)+0.5f));
        }

        private void draw(Graphics g) {
            g.setColor(alapSzin);
            g.fillOval(x-atmero/2, y-atmero/2, atmero, atmero);            
        }

        private void setAtmero(int atmero) {
            this.atmero = atmero;
        }
        
    }
    
    private class VillanoCsp{
        
        private int villantasIdo;
        private boolean villantas = false;
        private CsomoPont csp;

        private void draw(Graphics g) {
            if (villantas) {
                 villantasIdo--;
                 villantas = villantasIdo>0;
                 g.setColor(Color.red);
                 g.drawOval(csp.x-(villantasIdo/2), csp.y-(villantasIdo/2), villantasIdo, villantasIdo);
            }           
        }
        
        public void villant(CsomoPont csp) {
            if (this.csp != csp || !villantas) {
                this.csp = csp;
                villantas = true;
                villantasIdo = 22;
            }
        }        
        
    }    
    

//    private class Info{
//        String info = "tomi";
//        
//        void setInfo(String info){
//            this.info = info;
//        }
//        
//        void draw(Graphics2D g){
//            g.drawString(info, egerPos.x, egerPos.y);
//        }
//    }
}
