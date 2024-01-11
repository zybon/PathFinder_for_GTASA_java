package utvonaltervezo.data;

import java.util.ArrayList;

/**
 *
 * @author Zybon
 * Created time: 2016.09.08 14:34:51
 */
public class Ut {
      
    final long ID;

    CsomoPont kezdoCsomopont;
    CsomoPont vegCsomopont;
    
    private boolean egyiranyu = false;
    
    boolean rejtett = false;
    
    final ArrayList<Pont> torespontok = new ArrayList<>();
    
    private boolean utvonalAtnezte = false;
    private int utvonalAtnezesIndex;
    private boolean kijeloles = false;
    private boolean torlendo = false;
    
    float hossz;

    public Ut(long ID) {
        this.ID = ID;
    }    

    public long getID() {
        return ID;
    }
    
    public void setKijeloles(boolean kijeloles) {
        this.kijeloles = kijeloles;
    }

    public boolean isKijeloles() {
        return kijeloles;
    }    

    public void setKezdoCsomopont(CsomoPont csomoPont) {
        this.kezdoCsomopont = csomoPont;
        this.kezdoCsomopont.addUt(this);
    }

    public void setVegCsomopont(CsomoPont csomoPont) {
        this.vegCsomopont = csomoPont;
        this.vegCsomopont.addUt(this);
    }
    
    public CsomoPont getKezdoCsomopont() {
        return kezdoCsomopont;
    }

    public CsomoPont getVegCsomopont() {
        return vegCsomopont;
    }
    
    public void initHossz(){
        Pont cspK = kezdoCsomopont; 
        Pont cspV;      
        hossz = 0;
        for (int i=0, n=torespontok.size();i<n;i++){
            cspV = torespontok.get(i);
            hossz += cspK.getTav(cspV);
            cspK = torespontok.get(i);
        }
        cspV = vegCsomopont; 
            hossz += cspK.getTav(cspV);
    }
    

    public float getHossz() {
        return hossz;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("ID: ");
        str.append(ID);
        str.append("\n");
        str.append("Kezdő csomópont: ");
        str.append(kezdoCsomopont.toString());
        str.append("\n");        
        str.append("Töréspont db: ");
        str.append(torespontok.size());
        str.append("\n");
        str.append("Hossz: ");
        str.append(hossz);
        str.append("\n");
        str.append("Vég csomópont: ");
        str.append(vegCsomopont.toString());
        str.append("\n");        
        return str.toString();
    }
    
    public void addToresPont(Pont pont) {
        torespontok.add(new Pont(pont));
        hossz = -1;
    } 
    
    public void addToresPont(int index, Pont pont) {
        torespontok.add(index,new Pont(pont));
        hossz = -1;
    }     
    
    public void removeToresPont(Pont toresPont){
        torespontok.remove(toresPont);
        hossz = -1;
    }
    
    public Pont toresPontBeszuras(Pont p){
        Pont ujTp;
        Pont cspK = kezdoCsomopont; 
        Pont cspV;        
        for (int i=0, n=torespontok.size();i<n;i++){
            cspV = torespontok.get(i);
            if (Vonal.pontAVonalonVan(p, cspK, cspV)) {
                ujTp = new Pont(p);
                addToresPont(i, ujTp);
                return ujTp;
            }
            cspK = torespontok.get(i);
        }
        cspV = vegCsomopont; 
        if (Vonal.pontAVonalonVan(p, cspK, cspV)) {
            ujTp = new Pont(p);
            addToresPont(ujTp);
            return ujTp;
        }
        return null;
    }
    
    public Ut csomoPontBeszuras(CsomoPont ujCsp){
        Pont cspK = kezdoCsomopont; 
        Pont cspV;     
        for (int i=0, n=torespontok.size();i<n;i++){
            cspV = torespontok.get(i);
            if (Vonal.pontAVonalonVan(ujCsp, cspK, cspV)) {
                return utFelosztas(i, ujCsp);
            }
            cspK = torespontok.get(i);
        }
        cspV = vegCsomopont; 
        if (Vonal.pontAVonalonVan(ujCsp, cspK, cspV)) {
            return utFelosztas(torespontok.size(), ujCsp);
        }        
        return null;        
    }
    
    private Ut utFelosztas(int index, CsomoPont ujCsp){
        CsomoPont vP = vegCsomopont;
        vP.removeUt(this);
        setVegCsomopont(ujCsp);
        Ut ujUt = new Ut(UtHalozat.getUjId());
        ujUt.setKezdoCsomopont(ujCsp);
        ujUt.setVegCsomopont(vP);
        for (int i = index; i < torespontok.size(); i++) {
            ujUt.addToresPont(torespontok.remove(i));
            i--;
        }
        initHossz();
        ujUt.initHossz();
        if (isEgyiranyu()) {
            ujUt.setEgyiranyu(true);
        }        
        return ujUt;
    }
  
    public Pont getToresPontAkozelben(Pont hely, float maxTav) {
        for (Pont tp : torespontok) 
        {
            if (tp.getTav(hely)<maxTav) {
                return tp;
            }
        }   
        return null;
    }      
        
    public ArrayList<Pont> getToresPontok() {
        return torespontok;
    }
    
    public int getToresPontDb() {
        return torespontok.size();
    }    

    public CsomoPont getSzomszedCsp(CsomoPont csp) {
        if (csp == kezdoCsomopont) {
            return vegCsomopont;
        }
        else {
            return kezdoCsomopont;
        }
    }    
    
    void resetUtvonal() {
        setUtvonalAtnezte(false);
    }

    public void setUtvonalAtnezte(boolean utvonalAtnezte) {
        this.utvonalAtnezte = utvonalAtnezte;
    }

    public void setUtvonalIndex(int utvonalAtnezesIndex) {
        this.utvonalAtnezesIndex = utvonalAtnezesIndex;
    }

    public boolean isUtvonalAtnezte() {
        return utvonalAtnezte;
    }

    public void torlesElokeszites(CsomoPont csp){
        if (kezdoCsomopont != vegCsomopont) {
            if (csp == kezdoCsomopont) {
                vegCsomopont.removeUt(this);    
            }
            else {
                kezdoCsomopont.removeUt(this);
            }
        }
        
        torlendo = true;
//        torespontok.clear();
    }

    public boolean isTorlendo() {
        return torlendo;
    }

    public boolean rajtaVanAzUton(Pont p){
        if (rejtett) {return false;}
        Pont cspK = kezdoCsomopont; 
        Pont cspV;        
        for (int i=0, n=torespontok.size();i<n;i++){
            cspV = torespontok.get(i);
            if (Vonal.pontAVonalonVan(p, cspK, cspV)) {
                return true;
            }
            cspK = torespontok.get(i);
        }
        cspV = vegCsomopont; 
        return Vonal.pontAVonalonVan(p, cspK, cspV); 
    }    

    public void setEgyiranyu(boolean egyiranyu) {
        this.egyiranyu = egyiranyu;
    }

    public boolean isEgyiranyu() {
        return egyiranyu;
    }

    public void setEgyiranyuKezdoPont(CsomoPont kezdoCsp) {
        if (kezdoCsp == vegCsomopont) {
            vegCsomopont = kezdoCsomopont;
            kezdoCsomopont = kezdoCsp;
            toresPontBejarasMegforditas();
        }
        egyiranyu = true;
    }
    
    private void toresPontBejarasMegforditas(){
        int h = torespontok.size();
        for (int i = 0, j = h-1; i<h>>1; i++, j--) {
            torespontok.set(i, torespontok.set(j, torespontok.get(i)));
        }
    }
    
    public Ut masol(){
        Ut ut0 = new Ut(UtHalozat.getUjId());
        ut0.setKezdoCsomopont(kezdoCsomopont);
        for (Pont torespont : torespontok) {
            ut0.addToresPont(torespont);
        }
        ut0.setVegCsomopont(vegCsomopont);
        ut0.setEgyiranyu(egyiranyu);
        ut0.hossz = hossz;
        return ut0;
    }

    public void setRejtett(boolean rejtett) {
        this.rejtett = rejtett;
    }

    public boolean isRejtett() {
        return rejtett;
    }


}
