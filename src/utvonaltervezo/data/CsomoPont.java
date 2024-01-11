package utvonaltervezo.data;

import java.util.ArrayList;

/**
 *
 * @author Zybon
 * Created time: 2016.09.08 14:34:47
 */
public class CsomoPont extends Pont{
    long ID;
    int runID;  
    public static int statRunId = 0;
    private final Teglalap teglalap = new Teglalap();
    
    private final ArrayList<Ut> utak = new ArrayList();
    
    
    public CsomoPont(int x, int y) {
        super(x, y);
        ID = UtHalozat.getUjId();
        runID = statRunId++;
        teglalap.set(x, y, x+1, y+1);
    }

    public CsomoPont(Pont p) {
        this(p.x, p.y);
    }
    
    public CsomoPont(int x, int y, long ID) {
        super(x, y);
        this.ID = ID;
        runID = statRunId++;
        teglalap.set(x, y, x+1, y+1);
    }    
    
    public long getID() {
        return ID;
    }
    
    public void addUt(Ut ut){
        utak.add(ut);
    }
    
    public ArrayList<Ut> getUtak() {
        return utak;
    }
    
    public ArrayList<CsomoPont> getSzomszedCspok() {
        ArrayList<CsomoPont> sz = new ArrayList<CsomoPont>();
        for (Ut ut : utak) {
            sz.add(ut.getSzomszedCsp(this));
        }
        return sz;
        
    }    

    public void setTeglalap(int left, int top, int right, int bottom) {
        this.teglalap.set(left, top, right, bottom);
//        x = (left+right)/2;
//        y = (top+bottom)/2;
    }

    public Teglalap getTeglalap() {
        return teglalap;
    }
    
    public boolean kozelbenVan(Pont p){
        return teglalap.left<=p.x && teglalap.top<=p.y &&
               teglalap.right>=p.x && teglalap.bottom>=p.y ;
    }
    

    
//    @Override
//    public String toString() {
//        StringBuilder str = new StringBuilder();
//        str.append(super.toString());
//        str.append("\n");
//        str.append(utak.size());
//        str.append(" db út csatlakozik");
//        return str.toString();
//    }

    public void torlesElokeszites() {
        for (Ut ut : utak) {
            ut.torlesElokeszites(this);
        }      
        utak.clear();
    }

    void szomszedCspUtakTorlese() {
        CsomoPont szomszed;
        for (Ut ut : utak) {
            szomszed = ut.getSzomszedCsp(this);
            if (szomszed == null) {
                throw new AssertionError(this);
            }
            szomszed.removeUt(ut);
        }
    }        
    
    public void removeUt(Ut ut){
        utak.remove(ut);
    }

    public int getRunID() {
        return runID;
    }

    @Override
    public void setKijeloles(boolean kijeloles) {
        super.setKijeloles(kijeloles);
        for (Ut ut : utak) {
            ut.setKijeloles(kijeloles);
        }
    }

    @Override
    public String toString() {
        return "CsomoPont{" + "runID=" + runID + "; x=" + x + ", y=" + y + 
//                ";\n" +teglalap +
                '}';
    }

    
    
    
    

}
