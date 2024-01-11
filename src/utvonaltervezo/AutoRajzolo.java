package utvonaltervezo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import static utvonaltervezo.AutoRajzolo.AUTOCSP_ALLAPOT.LEZARVA;
import static utvonaltervezo.AutoRajzolo.AUTOCSP_ALLAPOT.MEHET;
import static utvonaltervezo.AutoRajzolo.AUTOCSP_ALLAPOT.NEM_MEHET;
import utvonaltervezo.data.CsomoPont;
import utvonaltervezo.data.Pont;
import utvonaltervezo.data.Ut;
import utvonaltervezo.data.UtHalozat;
import static utvonaltervezo.data.UtHalozat.TORESPONT_ATMERO;

/**
 *
 * @author zybon
 * Created 2017.10.09. 14:04:39
 */
public class AutoRajzolo {
    
    public enum AUTOCSP_ALLAPOT{
        NINCS_ELLENORIZVE,
        NEM_MEHET,
        MEHET,
        LEZARVA
    };        
    
    public static final int JOBBRA_INDEX = 0;
    public static final int BALRA_INDEX = 1;
    public static final int FEL_INDEX = 2;
    public static final int LE_INDEX = 3;
    private final int TORESPONT_TESZT_MELYSEG = 2*TORESPONT_ATMERO;    
    private final int CSOMOPONT_TESZT_MELYSEG = 4*TORESPONT_ATMERO;         

    private final ArrayList<AutoCsp> autoCspok = new ArrayList();        

    private final MapPanel mapPanel;
    
    final BufferedImage ffKep;
    
    private final JobbraRajz jobbraRajz = new JobbraRajz();
    private final BalraRajz balraRajz = new BalraRajz();
    private final LefeleRajz lefeleRajz = new LefeleRajz();
    private final FelfeleRajz felfeleRajz = new FelfeleRajz();
    private final Szinezes szinezes = new Szinezes();
    
    
    public AutoRajzolo(MapPanel mapPanel, BufferedImage kep) {
        this.mapPanel = mapPanel;
        this.ffKep = createKep(kep);
    }

    private BufferedImage createKep(BufferedImage kep){
        BufferedImage modKep = new BufferedImage(kep.getWidth(), kep.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int a,r,g,b;
        int Y;
        int[] kepTomb = new int[kep.getWidth()*kep.getHeight()];
        kep.getRGB(0, 0, kep.getWidth(), kep.getHeight(), kepTomb, 0, kep.getWidth());
        for (int i = 0; i < kepTomb.length; i++) {
            int s = kepTomb[i];
            r = (s>>16)&0xff;
            g = (s>>8)&0xff;
            b = (s)&0xff;
            Y = (int)(0.2126*r+0.7152*g+0.0722*b);
//            Y = (r+g+b)/3;
            kepTomb[i] = Y<50?0xff000000:0xffffffff;
        }
        modKep.setRGB(0, 0, kep.getWidth(), kep.getHeight(), kepTomb, 0, kep.getWidth());
        return modKep;
    }

    

    public void start(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                autoRajzStart();
            }
        }).start();        

    }


    private void szunet(){
        if (!UtvonalTervezo.AUTORAJZ_SZUNETTEL) {
            return;
        }
        try {
            Thread.sleep(1);
        }
        catch (InterruptedException iex){}
    }

    private void autoRajzStart(){
        AutoCsp autoCsp = getAutoCspAkozelben(mapPanel.mutato);
        if (autoCsp == null) {
            autoCsp = new AutoCsp(new CsomoPont(mapPanel.mutato));
            if (lefeleTeszt(autoCsp.csp) == CSOMOPONT_TESZT_MELYSEG) {
                autoCsp.setIranyAllapot(LE_INDEX, MEHET);
            }
            if (felfeleTeszt(autoCsp.csp) == CSOMOPONT_TESZT_MELYSEG) {
                autoCsp.setIranyAllapot(FEL_INDEX, MEHET);
            }
            if (jobbraTeszt(autoCsp.csp) == CSOMOPONT_TESZT_MELYSEG) {
                autoCsp.setIranyAllapot(JOBBRA_INDEX, MEHET);
            }
            if (balraTeszt(autoCsp.csp) == CSOMOPONT_TESZT_MELYSEG) {
                autoCsp.setIranyAllapot(BALRA_INDEX, MEHET);
            }    
            csomoPontBemeres(autoCsp);
            addToAutoCspok(autoCsp);
        }
        
        autUtRajz();
    }

    private void addToAutoCspok(AutoCsp autoCsp){
        autoCspok.add(autoCsp);
        mapPanel.utHalozat.addCsomoPont(autoCsp.csp);
//            System.out.println(autoCsp);
//       frameInfo(autoCsp);

    }  

    private void autUtRajz(){
        AutoCsp autoCsp;
        int M = 0;
        for (int i = 0; i < autoCspok.size(); i++) {
            autoCsp = autoCspok.get(i);
            if (autoCsp.mehetValamerre()) {
                jobbraRajz.rajzol(autoCsp);
                balraRajz.rajzol(autoCsp);
                lefeleRajz.rajzol(autoCsp);
                felfeleRajz.rajzol(autoCsp);
                M++;
            }
        }
        if (M == 0) {
            toresPontokKeresese();
            mapPanel.folyamat = MapPanel.Folyamat.UresJarat;
        }   
        else {
            autUtRajz();
        }
    }
    
    private void toresPontokKeresese(){
        for (AutoCsp autoCsp : autoCspok) {
            if (autoCsp.csp.getUtak().size() == 2) {
//                frameInfo(autoCsp);
                mapPanel.utHalozat.ketUtEgyesitese(autoCsp.csp, true);
            }
        }        
    }

    private AutoCsp getAutoCspAkozelben(Pont p) {
        for (AutoCsp autoCsp : autoCspok) {
            if (autoCsp.csp.kozelbenVan(p)) {
                return autoCsp;
            }
        }
        return null;
    }          

    private int iranyTeszt(Pont p, int dx, int dy){
        int m = 1;
        for (; m < CSOMOPONT_TESZT_MELYSEG; m++) {
            if (ffKep.getRGB(p.x+dx*m, p.y+dy*m) != 0xff000000) {
                break;
            }            
            mapPanel.mutato.set(p.x+dx*m, p.y+dy*m);
            szunet();
        }
        return m;
    }        

    private int lefeleTeszt(Pont p){
        return iranyTeszt(p, 0, 1);
    }

    private int felfeleTeszt(Pont p){
        return iranyTeszt(p, 0, -1);
    }  

    private int jobbraTeszt(Pont p){
        return iranyTeszt(p, 1, 0);
    }  

    private int balraTeszt(Pont p){
        return iranyTeszt(p, -1, 0);
    }      
    
    private void csomoPontBemeres(AutoCsp autoCsp) {
        Pont bf = new Pont(autoCsp.csp);
        int fel = CSOMOPONT_TESZT_MELYSEG;
        while (fel == CSOMOPONT_TESZT_MELYSEG) {
            fel = felfeleTeszt(bf);
            bf.x--;
        }
        bf.y -= fel;
        
        Pont jf = new Pont(autoCsp.csp);
        fel = CSOMOPONT_TESZT_MELYSEG;
        while (fel == CSOMOPONT_TESZT_MELYSEG) {
            fel = felfeleTeszt(jf);
            jf.x++;
        }
        jf.y -= fel;
        
        Pont bl = new Pont(autoCsp.csp);
        int le = CSOMOPONT_TESZT_MELYSEG;        
        while (le == CSOMOPONT_TESZT_MELYSEG) {
            le = lefeleTeszt(bl);
            bl.x--;
        }
        bl.y += le;        
        
        Pont jl = new Pont(autoCsp.csp);
        le = CSOMOPONT_TESZT_MELYSEG;        
        while (le == CSOMOPONT_TESZT_MELYSEG) {
            le = lefeleTeszt(jl);
            jl.x++;
        }
        jl.y += le;
//        frameInfo("bf: "+bf);
//        frameInfo("bl: "+bl);
//        frameInfo("jf: "+jf);
//        frameInfo("jl: "+jl);
        autoCsp.csp.setTeglalap(
                Math.min(bf.x, bl.x),
                Math.min(bf.y, jf.y),
                Math.max(jf.x, jl.x),
                Math.max(bl.y, jl.y)
        );
    }

          

    private class AutoCsp{
        CsomoPont csp;
        AUTOCSP_ALLAPOT[] iranyAllapot = new AUTOCSP_ALLAPOT[4];

        public AutoCsp(CsomoPont csp) {
            this.csp = csp;
            for (int i = 0; i < iranyAllapot.length; i++) {
                iranyAllapot[i] = NEM_MEHET;
            }
        }

        public void setIranyAllapot(int index, AUTOCSP_ALLAPOT allapot) {
            this.iranyAllapot[index] = allapot;

        }

        public AUTOCSP_ALLAPOT getIranyAllapot(int index) {
            return this.iranyAllapot[index];
        }  
        
        private boolean mehetValamerre() {
            for (int i = 0; i < iranyAllapot.length; i++) {
                if (iranyAllapot[i] == MEHET) {return true;}
            }            
            return false;
        }        

        @Override
        public String toString() {
            return csp+"\n"+
                    "jobbra: "+iranyAllapot[JOBBRA_INDEX]+"\n"+
                    "balra: "+iranyAllapot[BALRA_INDEX]+"\n"+
                    "fel: "+iranyAllapot[FEL_INDEX]+"\n"+
                    "le: "+iranyAllapot[LE_INDEX]+"\n";
        }






        }   

    private abstract class IranyRajz{
        final int iranyIndex;
        final int ellentettIranyIndex;
        final int keresztIndex0;
        final int keresztIndex1;

        public IranyRajz(int iranyIndex, int ellentettIranyIndex, int keresztIndex0, int keresztIndex1) {
            this.iranyIndex = iranyIndex;
            this.ellentettIranyIndex = ellentettIranyIndex;
            this.keresztIndex0 = keresztIndex0;
            this.keresztIndex1 = keresztIndex1;
        }

        abstract boolean whileFeltetel(Pont p);
        abstract Pont kezdoPont(AutoCsp autoCsp);
        abstract void pontEltolas(Pont p, int ertek);
        abstract int keresztMelysegTeszt0(Pont p);
        abstract int keresztMelysegTeszt1(Pont p);
        abstract int folytatasMelysegTeszt(Pont p);
        abstract void pontAzUtKozepere(Pont p, int min0, int min1);
    
        void rajzol(AutoCsp kezdoAutoCsp){
            if (kezdoAutoCsp.getIranyAllapot(iranyIndex) != MEHET) {
                return;
            }
            mapPanel.ujUt = new Ut(UtHalozat.getUjId());
            mapPanel.ujUt.setKezdoCsomopont(kezdoAutoCsp.csp);
            kezdoAutoCsp.setIranyAllapot(iranyIndex, LEZARVA);
//            frameInfo("autUtRajz"+iranyIndex+": ");
//            frameInfo(kezdoAutoCsp);        
            /*menni jobbra addig míg
                - egy létező csp-ba fut
                - le- és/vagy felteszt true-val tér vissza
                - vége az útnak (a getPx nem fekete)
            */
            Pont p = kezdoPont(kezdoAutoCsp);
//            frameInfo(p);
            int keresztMelyseg0;
            int keresztMelyseg1;
            AutoCsp vegAutoCsp = null;
            int minKeresztMelyseg0 = Integer.MAX_VALUE;
            int minKeresztMelyseg1 = Integer.MAX_VALUE;
            while (whileFeltetel(p)){
//                frameInfo(p);
                vegAutoCsp = getAutoCspAkozelben(p);
                if (vegAutoCsp != null) {
                    break;
                }
                keresztMelyseg0 = keresztMelysegTeszt0(p);
                minKeresztMelyseg0 = Math.min(minKeresztMelyseg0, keresztMelyseg0);
                keresztMelyseg1 = keresztMelysegTeszt1(p);
                minKeresztMelyseg1 = Math.min(minKeresztMelyseg1, keresztMelyseg1);
                if (keresztMelyseg0 == CSOMOPONT_TESZT_MELYSEG || keresztMelyseg1 == CSOMOPONT_TESZT_MELYSEG){
//                    pontAzUtKozepere(kezdoAutoCsp.csp, minKeresztMelyseg0, minKeresztMelyseg1);
                    pontAzUtKozepere(p, minKeresztMelyseg0, minKeresztMelyseg1);
                    vegAutoCsp = new AutoCsp(new CsomoPont(p));
//                    frameInfo("új csomópont itt:");
//                    frameInfo(p);
                    if (keresztMelyseg0 == CSOMOPONT_TESZT_MELYSEG) {
                        vegAutoCsp.setIranyAllapot(keresztIndex0, MEHET);
                    }
                    if (keresztMelyseg1 == CSOMOPONT_TESZT_MELYSEG) {
                        vegAutoCsp.setIranyAllapot(keresztIndex1, MEHET);
                    }
                    if (folytatasMelysegTeszt(p) == CSOMOPONT_TESZT_MELYSEG) {
                        vegAutoCsp.setIranyAllapot(iranyIndex, MEHET);
                    }
                    csomoPontBemeres(vegAutoCsp);
                    addToAutoCspok(vegAutoCsp);
                    break;
                }
                pontEltolas(p, 1);
                if (ffKep.getRGB(p.x, p.y) != 0xff000000) {
                    vegAutoCsp = new AutoCsp(new CsomoPont(p));
                    addToAutoCspok(vegAutoCsp);
                    break;
                }
            }
            if (vegAutoCsp != null) {
                vegAutoCsp.setIranyAllapot(ellentettIranyIndex, LEZARVA);
                mapPanel.ujUt.setVegCsomopont(vegAutoCsp.csp);
                mapPanel.ujUt.initHossz();
                mapPanel.utHalozat.addUt(mapPanel.ujUt);
                rajzol(vegAutoCsp);
            }
            else {
                kezdoAutoCsp.csp.removeUt(mapPanel.ujUt); 
            }
        }

        

    }
    
    private class JobbraRajz extends IranyRajz{

        public JobbraRajz() {
            super(JOBBRA_INDEX, BALRA_INDEX, LE_INDEX, FEL_INDEX);
        }

        @Override
        boolean whileFeltetel(Pont p) {
            return p.x<ffKep.getWidth();
        }

        @Override
        Pont kezdoPont(AutoCsp autoCsp) {
            Pont p = new Pont(autoCsp.csp);
            p.x = autoCsp.csp.getTeglalap().getRight()+1;
            return p;
        }
        
        

        @Override
        void pontEltolas(Pont p, int ertek) {
            p.x += ertek;
        }

        @Override
        int keresztMelysegTeszt0(Pont p) {
            return lefeleTeszt(p);
        }

        @Override
        int keresztMelysegTeszt1(Pont p) {
            return felfeleTeszt(p);
        }

        @Override
        int folytatasMelysegTeszt(Pont p) {
            return jobbraTeszt(p);
        }

        @Override
        void pontAzUtKozepere(Pont p, int min0, int min1) {
            p.y = p.y-min1+(int)((min0+min1)/2f+0.5f);
        }

        
    
    }
    
    private class BalraRajz extends IranyRajz{

        public BalraRajz() {
            super(BALRA_INDEX, JOBBRA_INDEX, LE_INDEX, FEL_INDEX);
        }

        @Override
        boolean whileFeltetel(Pont p) {
            return p.x>=0;
        }
        
        @Override
        Pont kezdoPont(AutoCsp autoCsp) {
            Pont p = new Pont(autoCsp.csp);
            p.x = autoCsp.csp.getTeglalap().getLeft()-1;
            return p;
        }        

        @Override
        void pontEltolas(Pont p, int ertek) {
            p.x -= ertek;
        }

        @Override
        int keresztMelysegTeszt0(Pont p) {
            return lefeleTeszt(p);
        }

        @Override
        int keresztMelysegTeszt1(Pont p) {
            return felfeleTeszt(p);
        }

        @Override
        int folytatasMelysegTeszt(Pont p) {
            return balraTeszt(p);
        }

        @Override
        void pontAzUtKozepere(Pont p, int min0, int min1) {
            p.y = p.y-min1+(int)((min0+min1)/2f+0.5f);
        }
    
    }
    
    private class LefeleRajz extends IranyRajz{

        public LefeleRajz() {
            super(LE_INDEX, FEL_INDEX, JOBBRA_INDEX, BALRA_INDEX);
        }

        @Override
        boolean whileFeltetel(Pont p) {
            return p.y<ffKep.getHeight();
        }
        
        @Override
        Pont kezdoPont(AutoCsp autoCsp) {
            Pont p = new Pont(autoCsp.csp);
            p.y = autoCsp.csp.getTeglalap().getBottom()+1;
            return p;
        }           

        @Override
        void pontEltolas(Pont p, int ertek) {
            p.y += ertek;
        }

        @Override
        int keresztMelysegTeszt0(Pont p) {
            return jobbraTeszt(p);
        }

        @Override
        int keresztMelysegTeszt1(Pont p) {
            return balraTeszt(p);
        }

        @Override
        int folytatasMelysegTeszt(Pont p) {
            return lefeleTeszt(p);
        }

        @Override
        void pontAzUtKozepere(Pont p, int min0, int min1) {
            p.x = p.x-min1+(int)((min0+min1)/2f+0.5f);
        }    
    }
    
    private class FelfeleRajz extends IranyRajz{

        public FelfeleRajz() {
            super(FEL_INDEX, LE_INDEX, JOBBRA_INDEX, BALRA_INDEX);
        }

        @Override
        boolean whileFeltetel(Pont p) {
            return p.y>=0;
        }
        
        @Override
        Pont kezdoPont(AutoCsp autoCsp) {
            Pont p = new Pont(autoCsp.csp);
            p.y = autoCsp.csp.getTeglalap().getTop()-1;
            return p;
        }          

        @Override
        void pontEltolas(Pont p, int ertek) {
            p.y -= ertek;
        }

        @Override
        int keresztMelysegTeszt0(Pont p) {
            return jobbraTeszt(p);
        }

        @Override
        int keresztMelysegTeszt1(Pont p) {
            return balraTeszt(p);
        }

        @Override
        int folytatasMelysegTeszt(Pont p) {
            return felfeleTeszt(p);
        }

        @Override
        void pontAzUtKozepere(Pont p, int min0, int min1) {
            p.x = p.x-min1+(int)((min0+min1)/2f+0.5f);
        }        
    }
    
    private class Szinezes{
        
        private final Pont bf = new Pont();
        private final Pont jl = new Pont();        
    
        private static final int BALRA_MEHET = 1;
        private static final int FEL_MEHET = 1<<1;
        private static final int JOBBRA_MEHET = 1<<2;
        private static final int LE_MEHET = 1<<3;
        private int mehetFlag;
        
        private void szinezes(Pont kezdoPont){
            int kattSzin = ffKep.getRGB(kezdoPont.x, kezdoPont.y);
            bf.set(kezdoPont);
            jl.set(kezdoPont);
            mehetFlag = BALRA_MEHET|FEL_MEHET|JOBBRA_MEHET|LE_MEHET;
            kitoltes(kattSzin, bf, jl);
            for (int y=bf.y;y<=jl.y;y++){
                for (int x=bf.x;x<=jl.x;x++){
                    atSzinezes(x, y, 0xffff0000);
                }
            }         
        }   

        private void kitoltes(int kattSzin, Pont bf, Pont jl){
            if ((mehetFlag & BALRA_MEHET)!= 0) {
                kitoltesBalra(kattSzin, bf, jl);
            }  
            if ((mehetFlag & FEL_MEHET)!= 0) {
                kitoltesFel(kattSzin, bf, jl);
            }          
            if ((mehetFlag & JOBBRA_MEHET)!= 0) {
                kitoltesJobbra(kattSzin, bf, jl);
            }
            if ((mehetFlag & LE_MEHET)!= 0) {
                kitoltesLe(kattSzin, bf, jl);
            }  
    //        System.out.println(Integer.toBinaryString(mehetFlag));
    //        System.out.println(bf+" - "+jl);
    //        System.out.println();
            if (mehetFlag != 0) {
                kitoltes(kattSzin, bf, jl);
            }        
        }    

        private void kitoltesBalra(int kattSzin, Pont bf, Pont jl){
    //        System.out.println("kitoltesBalra: "+bf+" - "+jl);
            for (int y = bf.y;y<=jl.y;y++) {
                if (ffKep.getRGB(bf.x-1, y) != kattSzin){
                    mehetFlag ^= BALRA_MEHET;
                    break;
                }
            }
            if ((mehetFlag & BALRA_MEHET)!= 0) {
                bf.x-=1;
    //            System.out.println("egyet balra");
            }     
        }

        private void kitoltesJobbra(int kattSzin, Pont bf, Pont jl){
    //        System.out.println("kitoltesJobbra: "+bf+" - "+jl);
            for (int y = bf.y;y<=jl.y;y++) {
                if (ffKep.getRGB(jl.x+1, y) != kattSzin){
                    mehetFlag ^= JOBBRA_MEHET;
                    break;
                }
            }
            if ((mehetFlag & JOBBRA_MEHET)!= 0) {
                jl.x+=1;
    //            System.out.println("egyet jobbra");
            }      
        }

        private void kitoltesFel(int kattSzin, Pont bf, Pont jl){
    //        System.out.println("kitoltesFel: "+bf+" - "+jl);
            for (int x = bf.x;x<=jl.x;x++) {
                if (ffKep.getRGB(x, bf.y-1) != kattSzin){
                    mehetFlag ^= FEL_MEHET;
                    break;
                }
            }
            if ((mehetFlag & FEL_MEHET)!= 0) {
                bf.y-=1;
    //            System.out.println("egyet fel");
            }     
        }  

        private void kitoltesLe(int kattSzin, Pont bf, Pont jl){
    //        System.out.println("kitoltesLe: "+bf+" - "+jl);
            for (int x = bf.x;x<=jl.x;x++) {
                if (ffKep.getRGB(x, jl.y+1) != kattSzin){
                    mehetFlag ^= LE_MEHET;
                    break;
                }
            }
            if ((mehetFlag & LE_MEHET)!= 0) {
                jl.y+=1;
    //            System.out.println("egyet le");
            }     
        }     

        private void atSzinezes(int x, int y, int szin){
            ffKep.setRGB(x, y, szin);
        }    
    }
        
    
    
}
