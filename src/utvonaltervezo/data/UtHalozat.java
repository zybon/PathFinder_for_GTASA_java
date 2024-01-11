package utvonaltervezo.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import static utvonaltervezo.UtvonalTervezo.frameInfo;

/**
 *
 * @author zybon
 * Created 2017.09.29. 19:16:34
 */
public class UtHalozat {
    
    public static final int CSOMOPONT_ATMERO = 5;
    public static final int TORESPONT_ATMERO = 3;     
    
    private final String cspokDataFile;// = ;
    private final String utakDataFile;// = ; 
    private static final int ID_BYTE_DB = 6;   
    private static final int XY_BYTE_DB = 3;
    
    private final ArrayList<CsomoPont> csomopontok = new ArrayList();
    private final ArrayList<Ut> utak = new ArrayList();
    
//    private static long allID = System.currentTimeMillis();
    private static final AtomicLong allId = new AtomicLong(System.currentTimeMillis());
            
    
    public static long getUjId(){
        return allId.incrementAndGet();
//        return allID++;
    }

    public UtHalozat(String cspokDataFile, String utakDataFile) {
        this.cspokDataFile = cspokDataFile;
        this.utakDataFile = utakDataFile;
    }
    
    public void reset(){
        for (CsomoPont csp : csomopontok) {
            csp.setKijeloles(false);
        }
        for (Ut ut : utak) {
//            ut.setUtvonalAtnezte(false);
        }
    }
    
    public final void addCsomoPont(CsomoPont csp){
        csomopontok.add(csp);
    }    

    public ArrayList<CsomoPont> getCsomopontok() {
        return csomopontok;
    }
    
    public final void addUt(Ut ut){
        utak.add(ut);
    }      

    public ArrayList<Ut> getUtak() {
        return utak;
    }
    
    public final CsomoPont csomoPontBeszuras(Pont hely){
        CsomoPont csp = new CsomoPont(hely);
        Ut _ujUt;
        for (Ut ut : utak) {
            _ujUt = ut.csomoPontBeszuras(csp);
            if (_ujUt !=  null) {
                utak.add(_ujUt);
                csomopontok.add(csp);
                return csp;
            }
        }        
        return null;
    }    
    
    public final void csomoPontBeszuras(CsomoPont csp, Ut ut){
        Ut _ujUt = ut.csomoPontBeszuras(csp);
        if (_ujUt !=  null) {
            if (ut.isEgyiranyu()) {
                _ujUt.setEgyiranyu(true);
                _ujUt.setEgyiranyuKezdoPont(csp);
            }
            utak.add(_ujUt);
            csomopontok.add(csp);
        }
    }    
        
    
    public final boolean toresPontBeszuras(Pont hely){
        Pont tp;
        for (Ut ut : utak) {
            tp = ut.toresPontBeszuras(hely);
            if (tp != null) {
                return true;
            }
        }
        return false;
    }   
    
    public final CsomoPont csomoPontKeresesAkozelben(Pont hely) {
        for (CsomoPont csp : csomopontok) {
            if (csp.getTav(hely)<CSOMOPONT_ATMERO) {
                return csp;
            }    
        }
        return null;
    }     
    
    public final Pont toresPontKeresesAkozelben(Pont hely) {
        for (Ut ut : utak) {
            if (ut.rejtett) {continue;}
            for (Pont tp : ut.getToresPontok()) {
                if (tp.getTav(hely)<TORESPONT_ATMERO) {
                    return tp;
                }    
            }   
        }        
        return null;
    }  
    
    public Ut utKeresesAkozelben(Pont hely){
        for (Ut ut : utak) {
            if (ut.rejtett) {continue;}
            if (ut.rajtaVanAzUton(hely)) {
                return ut;    
            }   
        }        
        return null;    
    }
    
    public boolean csomoPontTorles(Pont hely, int sugar){
        CsomoPont csp = csomoPontKeresesAkozelben(hely);
        if (csp == null) {return false;}
        csomoPontTorles(csp);
        return true;
    }

    public void csomoPontTorles(CsomoPont csp){
        csp.setKijeloles(true);
        csp.torlesElokeszites();
        csomopontok.remove(csp); 
        Ut ut;
        for (int i = 0; i < utak.size(); i++) {
            ut = utak.get(i);
            if (ut.isTorlendo()) {
                utak.remove(i);
                i--;
            }    
        }  
    }
    
    
    public boolean toresPontTorles(Pont hely, int sugar){
        for (Ut ut : utak) {
            for (Pont tp : ut.getToresPontok()) {
                if (tp.getTav(hely)<sugar) {
                    ut.removeToresPont(tp);
                    return true;
                }    
            }   
        }
        return false;
    }   
    

    
    public void utTorles(Pont hely) {
        Ut ut;
        for (int i = 0; i < utak.size(); i++) {
            ut = utak.get(i);
            if (ut.rajtaVanAzUton(hely)) {
                ut.kezdoCsomopont.removeUt(ut);
                ut.vegCsomopont.removeUt(ut);
                utak.remove(i);
                return;
            }    
        }         
    }    
    
    
    
    
    
    
    
    public final void mentes(){
        File csfile = new File(cspokDataFile);
        File csfile2 = new File(csfile.getParentFile(), "bizt_mentes/"+csfile.getName().replaceAll(".zyb", System.currentTimeMillis()+".zyb"));
        File utfile = new File(utakDataFile);
        File utfile2 = new File(utfile.getParentFile(), "bizt_mentes/"+utfile.getName().replaceAll(".zyb", System.currentTimeMillis()+".zyb"));        
        try {
            Files.copy(csfile.toPath(), csfile2.toPath());
            Files.copy(utfile.toPath(), utfile2.toPath());
        } catch (IOException ex) {
            System.out.println(ex);
        }
        csomopontokMentese();
        utakMentese();
        frameInfo("Úthálozat mentve");
    }
    
    private void csomopontokMentese(){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(cspokDataFile));
            for (CsomoPont csp : csomopontok) {
                fos.write(getCspByteData(csp));
            }
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException hiba a mentés közben\n"+ex);
        } catch (IOException ex) {
            System.out.println("IOException hiba a mentés közben\n"+ex);
        }
        finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }         
    }   
    
    public final byte[] getCspByteData(CsomoPont csp){
        byte[] bT = new byte[ID_BYTE_DB+XY_BYTE_DB];
        int i = addIdToBytes(csp.ID, bT, 0);
        addXYToBytes(csp.x, csp.y, bT, i);
        return bT;
    }
    
    private int addIdToBytes(long id, byte[] byteok, int index){
//        System.out.println(id);
        for (int i = ID_BYTE_DB-1; i > 0; i--) {
            byteok[index+i] = (byte)id;
//            System.out.println(byteok[index+i]);
            id >>>= 8;
        }
        byteok[index] = (byte)id;
        return index+ID_BYTE_DB;
    }  
    
    public final int addXYToBytes(int x, int y, byte[] byteok, int index){
//        System.out.println(id);
        int shift = XY_BYTE_DB*8/2;
        int XY = x<<shift|y;
        for (int i = XY_BYTE_DB-1; i > 0; i--) {
            byteok[index+i] = (byte)XY;
//            System.out.println(byteok[index+i]);
            XY >>>= 8;
        }
        byteok[index] = (byte)XY;
        return index+XY_BYTE_DB;
    }  
    
    
    
    private void utakMentese(){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(utakDataFile));
            for (Ut ut : utak) {
                fos.write(getUtByteData(ut));
            }
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException hiba a mentés közben\n"+ex);
        } catch (IOException ex) {
            System.out.println("IOException hiba a mentés közben\n"+ex);
        }
        finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }         
    }   
     
    public final byte[] getUtByteData(Ut ut){
        int db = ut.getToresPontDb();
        byte[] bytok = new byte[ID_BYTE_DB + ID_BYTE_DB*2 + 1 + 1 + db*XY_BYTE_DB];
        int i = 0;
        i = addIdToBytes(ut.ID, bytok, i);
        i = addIdToBytes(ut.kezdoCsomopont.getID(), bytok, i);
        i = addIdToBytes(ut.vegCsomopont.getID(), bytok, i);
        bytok[i++] = (byte)(ut.isEgyiranyu()?1:0);
        bytok[i++] = (byte)db;
        if (db>0) {
            for (Pont tp : ut.torespontok) {
                i = addXYToBytes(tp.x, tp.y, bytok, i);
            }
        }
        return bytok;
    }
    
    public final void reload() {
        csomopontok.clear();
        utak.clear();
        beolvasas();
    }
    
    public final void beolvasas(){
        CsomoPont.statRunId = 0;
        csomopontokBeolvasasa();
        utakBeolvasasa();
//        boolean van = egyesit();
//        while (van) {
//            van = egyesit();
//        };
        frameInfo("Úthálozat betöltve");
    }   
    
    private void csomopontokBeolvasasa(){
        FileInputStream fis = null;
        File f = new File(cspokDataFile);
        byte[] cspByte = new byte[ID_BYTE_DB+XY_BYTE_DB];
        try {
            fis = new FileInputStream(f);
            CsomoPont csp;
            while ((fis.read(cspByte))!= -1){
                csp = createCspFromBytes(cspByte);
//                if (csp != null) {
                    csomopontok.add(csp);
//                }
            }
            fis.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException hiba a mentés közben\n"+ex);
        } catch (IOException ex) {
            System.out.println("IOException hiba a mentés közben\n"+ex);
        }
        finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }     
    } 
    
    
    private CsomoPont createCspFromBytes(byte[] cspBytes){
        long id = readIdFromBytes(cspBytes, 0);
        Pont p = readPontFromBytes(cspBytes, ID_BYTE_DB);
        CsomoPont csp = new CsomoPont(p.x, p.y, id);
        return csp;
    }
    
    private long readIdFromBytes(byte[] byteok, int index){
        long id = 0;
        for (int i = 0; i < ID_BYTE_DB; i++) {
            id = id<<8 | (byteok[index+i]&0xff);
        }
        return id;
    }      
    
    private static final Pont tempPont = new Pont();
    public final Pont readPontFromBytes(byte[] byteok, int index){
        int intErtek = 0;
        int shift = XY_BYTE_DB*8/2;
        int mask = (1<<shift)-1 ;
        for (int i = 0; i < XY_BYTE_DB; i++) {
            intErtek = intErtek<<8 | (byteok[index+i]&0xff);
        }
        tempPont.x = intErtek>>>shift;
        tempPont.y = intErtek & mask;
        return tempPont;
    }      
    
    private void utakBeolvasasa(){
        FileInputStream fis = null;
        File f = new File(utakDataFile);
        byte[] idkBytes = new byte[ID_BYTE_DB*3+1]; //ut, kp, vp
        byte[] tpkBytes;
        Ut ut;
        int tDb;
        try {
            fis = new FileInputStream(f);
            while ((fis.read(idkBytes))!= -1){
                ut = createUtFromBytes(idkBytes);
                
                tDb = fis.read()&0xff;
                if (tDb>0) {
                    tpkBytes = new byte[tDb*XY_BYTE_DB];
                    fis.read(tpkBytes);
//                    if (ut != null) {
                        addTpokToUt(ut, tDb, tpkBytes);
//                    }
                }
                ut.initHossz();
//                if (ut != null) {
                    utak.add(ut);
//                }
            }
            fis.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException hiba a mentés közben\n"+ex);
        } catch (IOException ex) {
            System.out.println("IOException hiba a mentés közben\n"+ex);
        }
        finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }         
    }
    
    private Ut createUtFromBytes(byte[] utIdBytes){
        int i=0;
        long id = readIdFromBytes(utIdBytes, i);
//        if (id < 1005681242878L) {
//            id = 1000000000000L + id;
//        }
//        if (id > 1507573000000L) {return null;}
        i+=ID_BYTE_DB;
        Ut ut = new Ut(id); 
        ut.setKezdoCsomopont(getCspById(readIdFromBytes(utIdBytes, i)));
        i+=ID_BYTE_DB;        
        ut.setVegCsomopont(getCspById(readIdFromBytes(utIdBytes, i)));
        i+=ID_BYTE_DB;        
        ut.setEgyiranyu(utIdBytes[i]==1);
        return ut;
    }    
    
    private void addTpokToUt(Ut ut, int tDb, byte[] tpkBytes){
        Pont p;
        int i = 0;
        for (int j = 0; j < tDb; j++) {
            p = readPontFromBytes(tpkBytes, i);
            i += XY_BYTE_DB;
            ut.addToresPont(p);
        }
    }
    
    private CsomoPont getCspById(long id){
        for (CsomoPont csp : csomopontok) {
            if (csp.getID()== id) {
                return csp;
            }
        }
//        return new CsomoPont(0,0);
        throw new AssertionError();
    }          

    public void ketUtEgyesitese(CsomoPont kozosCsp, boolean helyereToresPont){
        Ut ut1 = kozosCsp.getUtak().get(0);
        CsomoPont kp = ut1.getSzomszedCsp(kozosCsp);
        Ut ut2 = kozosCsp.getUtak().get(1);
        CsomoPont vp = ut2.getSzomszedCsp(kozosCsp);
        ut1.torlesElokeszites(kozosCsp);
        ut2.torlesElokeszites(kozosCsp);
        utak.remove(ut1);
        utak.remove(ut2);
        csomopontok.remove(kozosCsp);
        Ut ujUt = new Ut(getUjId());
        ujUt.setKezdoCsomopont(kp);
        ujUt.setVegCsomopont(vp);
        ut1.setEgyiranyuKezdoPont(kp);
        ut2.setEgyiranyuKezdoPont(kozosCsp);
        for (Pont tp: ut1.torespontok) {
            ujUt.addToresPont(tp);
        }
        if (helyereToresPont) {
            ujUt.addToresPont(kozosCsp);
        }
        for (Pont tp: ut2.torespontok) {
            ujUt.addToresPont(tp);
        }
        ujUt.initHossz();
        utak.add(ujUt);
    }
          

}
