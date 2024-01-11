package utvonaltervezo.data;

import java.util.ArrayList;
import static utvonaltervezo.UtvonalTervezo.frameInfo;

/**
 *
 * @author zybon
 * Created 2017.09.19. 10:08:16
 */
public class Utvonal {
    public static final String NINCS_UT = "Nincs út a közelben";
    
    private final VegCsomoPont startCsp = new VegCsomoPont();
    private final VegCsomoPont celCsp = new VegCsomoPont();
            
    private final ArrayList<Pont> utvonal = new ArrayList();
    private final ArrayList<UtvonalCsp> nyitottak = new ArrayList();
    private int atnezes = 0;
    
    private boolean vanUtvonal = false;
    private static final String TAG = "Utvonal";
    
    public static final String OK = "Ok";
    public static final String NINCS_MEGOLDAS = "Nem található útvonal a két pont között";
    
    private float hossz;
    private final UtHalozat utHalozat;
    
    public Utvonal(UtHalozat utHalozat) {
        this.utHalozat = utHalozat;
    }
    
    public boolean vanUtvonal() {
        return vanUtvonal;
    }    
    
    public String setStart(Pont mutato) {
        CsomoPont lehetsegesCsp = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (lehetsegesCsp != null) {
            startCsp.setToValos(lehetsegesCsp);
        }
        else {
            Ut utAkozelben = utHalozat.utKeresesAkozelben(mutato);
            if (utAkozelben == null) {
                return NINCS_UT;
            }
            startCsp.setToVirtualis(mutato);
        }           
        return "Start: "+startCsp.pont;
    }
    
    public boolean isStartBeallitva() {
        return startCsp.beallitva;
    }
    
    public Pont getStartPont(){
        return startCsp.pont;
    }

    public String setCel(Pont mutato) {
        CsomoPont lehetsegesCsp = utHalozat.csomoPontKeresesAkozelben(mutato);
        if (lehetsegesCsp != null) {
            celCsp.setToValos(lehetsegesCsp);
        }
        else {
            Ut utAkozelben = utHalozat.utKeresesAkozelben(mutato);
            if (utAkozelben == null) {
                return NINCS_UT;
            }
            celCsp.setToVirtualis(mutato);
        }
        
        return "Cél: "+celCsp.pont;
    }

    public boolean isCelBeallitva() {
        return celCsp.beallitva;
    }
    
    public Pont getCelPont(){
        return celCsp.pont;
    }    
    

    public String tervez() {
        if (!startCsp.beallitva) {
            return "Nincs kezdőpont";
        }
        if (!celCsp.beallitva) {
            return "Nincs végpont";
        }
        vanUtvonal = false;
        utvonal.clear();
        nyitottak.clear();  
        atnezes = 0;
        hossz = 0;     
        startCsp.init();
        celCsp.init();
        String eredmeny = szamolasKezd();
        celCsp.clear();
        startCsp.clear();
        return eredmeny;
    }
    
    private String szamolasKezd(){
        UtvonalCsp utvCsp = szamol(new UtvonalCsp(startCsp.csomopont));
        while (utvCsp.csomoPont != celCsp.csomopont){
            utvCsp = szamol(utvCsp);
            if (utvCsp == null) {break;}
        }
        
        if (utvCsp == null) {
            return NINCS_MEGOLDAS;
        }
        else {
            UtvonalCsp csp = utvCsp;
            hossz += csp.szulobeVezetoUt.getHossz();
            addToUtvonal(csp.szulobeVezetoUt, csp.csomoPont);
//            int a = 1;
//            frameInfo(a+". db: "+csp.csomoPont.getUtak().size());
            while (csp.szuloCsp.csomoPont != startCsp.csomopont) {
//                a++;
                csp = csp.szuloCsp;
                hossz += csp.szulobeVezetoUt.getHossz();
                addToUtvonal(csp.szulobeVezetoUt, csp.csomoPont);
//                frameInfo(a+". db: "+csp.csomoPont.getUtak().size());
            }  
            utvonal.add(startCsp.pont);
            vanUtvonal = true;
            return OK;
        }    
    }
    
    private UtvonalCsp szamol(UtvonalCsp szuloUtvonalCsp){
        CsomoPont szuloCsp = szuloUtvonalCsp.csomoPont;
        ArrayList<Ut> kapcsolodoUtak = szuloCsp.getUtak();
        for (Ut ut : kapcsolodoUtak) {
            if (ut.isRejtett()) {continue;}
            if (ut.isUtvonalAtnezte()) {continue;}
            if (ut.isEgyiranyu() && szuloCsp == ut.vegCsomopont) {continue;}
            CsomoPont csp = ut.getSzomszedCsp(szuloCsp);
            addToNyitottak(
                new UtvonalCsp(
                        szuloUtvonalCsp,
                        csp,
                        ut
                )
            );
            ut.setUtvonalAtnezte(true);
            ut.setUtvonalIndex(atnezes++);
//            try{
//                Thread.sleep(5);
//            }
//            catch (InterruptedException iec){}
        }
        if (nyitottak.isEmpty()) {
//            frameInfo("Nincs út a két pont között");
            return null;
        }
        else {
            return nyitottak.remove(0);
        }
        
    }
   
    private void addToNyitottak(UtvonalCsp utvonalCsp){
        UtvonalCsp nyitott;
        boolean beszurva = false;
        for (int i = 0; i < nyitottak.size(); i++) {
            nyitott = nyitottak.get(i);
            if (!beszurva) {
                if (utvonalCsp.hasonlitas(nyitott)<0) {
                    if (nyitott.csomoPont == utvonalCsp.csomoPont) {
                        nyitott.set(utvonalCsp);
                        return;
    //                    frameInfo("van benne nyitott");
                    }
                    else {
//                        Log.i(TAG, "add: "+i+", size: "+nyitottak.size());
                        nyitottak.add(i, utvonalCsp);
                        beszurva = true;
                        continue;
                    }
                }
            }
            if (nyitott.csomoPont == utvonalCsp.csomoPont) {
                if (beszurva) {
                    nyitottak.remove(i);
                    return;
                }
                else {
                    return;
                }
            }
        }
        if (!beszurva) {
            nyitottak.add(utvonalCsp);        
        }


    }
       
    
    public float getHossz(){
        return hossz;
    }
    
    public int getLepesSzam(){
        return atnezes;
    }

    private void addToUtvonal(Ut ut, CsomoPont utvVegCsp){
        utvonal.add(new Pont(utvVegCsp));
        boolean kezdoCspAzUtvVeg = ut.kezdoCsomopont==utvVegCsp;
        if (kezdoCspAzUtvVeg) {
            for (int i=0, n=ut.torespontok.size();i<n;i++){
                utvonal.add(new Pont(ut.torespontok.get(i)));
            }
        }
        else {
            for (int i=ut.torespontok.size()-1;i>=0;i--){
                utvonal.add(new Pont(ut.torespontok.get(i)));
            }
        }
    }

    public ArrayList<Pont> getUtvonal() {
        return utvonal;
    }


    
    
    private class UtvonalCsp{
        CsomoPont csomoPont;
        float tavAStartCsptol = 0;
        float modositoErtek = 0;
        UtvonalCsp szuloCsp;
        Ut szulobeVezetoUt;
        
        public UtvonalCsp(CsomoPont csp) {
            this.csomoPont = csp;
        }        

        public UtvonalCsp(UtvonalCsp szuloCsp, CsomoPont csp, Ut szulobeVezetoUt) {
            this.csomoPont = csp;
            this.szuloCsp = szuloCsp;
            this.szulobeVezetoUt = szulobeVezetoUt;
            this.tavAStartCsptol = (szuloCsp.tavAStartCsptol + szulobeVezetoUt.getHossz());
            this.modositoErtek = celCsp.csomopont.getTav(csomoPont);
        }
        
        void set(UtvonalCsp ucsp){
            this.csomoPont = ucsp.csomoPont;
            this.szuloCsp = ucsp.szuloCsp;
            this.szulobeVezetoUt = ucsp.szulobeVezetoUt;
            this.tavAStartCsptol = ucsp.tavAStartCsptol;
        }
        
        int hasonlitas(UtvonalCsp masik){
            return tavAStartCsptol+modositoErtek < masik.tavAStartCsptol+masik.modositoErtek?
                    -1 : 1;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("Csomopont: ");
            str.append(csomoPont.getRunID());             
            str.append("; ");
            str.append("Távolság a startCsp-tól: ");
            str.append(tavAStartCsptol);   
            str.append("; ");
            str.append("Szülő csomópont: ");
            str.append(szuloCsp==null?"null":szuloCsp.csomoPont.getRunID());              
            return str.toString();
        }
        
        
        
    }
    
    private class VegCsomoPont{
        boolean beallitva = false;
        Pont pont;
        CsomoPont csomopont;
        boolean virtualis;
        Ut virtEredetije;
        
        void setToValos(CsomoPont csp){
            virtualis = false;
            pont = new Pont(csp);
            csomopont = csp;
            beallitva = true;
        }
        
        void setToVirtualis(Pont pont){
            virtualis = true;
            this.pont = new Pont(pont);
            this.csomopont = new CsomoPont(pont);
            beallitva = true;
        }
        
        void init(){
            if (!virtualis) {return;}
            virtEredetije = utHalozat.utKeresesAkozelben(pont);
            Ut ut0 = virtEredetije.masol();
            Ut ut1 = ut0.csomoPontBeszuras(csomopont);
            utHalozat.addUt(ut0);
            utHalozat.addUt(ut1);
            utHalozat.addCsomoPont(ut0.getVegCsomopont());
            virtEredetije.setRejtett(true);             
        }
        
        private void clear() {
            if (!virtualis) {return;}
            utHalozat.csomoPontTorles(csomopont);
            virtEredetije.setRejtett(false);
        }
        
        @Override
        public String toString() {
            return csomopont.toString()+"\n"+ 
                   "virtualis: "+ virtualis +"\n"+
                   csomopont.getUtak() ;
        }        
        
    }
    
}
