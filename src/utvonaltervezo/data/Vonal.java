package utvonaltervezo.data;

/**
 *
 * @author zybon
 * Created 2017.09.20. 21:22:54
 */
public class Vonal {
    private final Pont kezdoPont = new Pont();
    private final Pont vegPont = new Pont();
    
    private float meredekseg;
    private float C;
    
    public static float VASTAGSAG = 2;
    
    public enum Tipus{
        X_TENGELLYEL_PARHUZAMOS,
        Y_TENGELLYEL_PARHUZAMOS,
        ALTALANOS
    }
    
    private Tipus tipus = Tipus.ALTALANOS;
    
    public Vonal(){
    }
    

    public Vonal(Pont kezdoPont, Pont vegPont) {
        set(kezdoPont, vegPont);
    }

    public Pont getKezdoPont() {
        return kezdoPont;
    }

    public Pont getVegPont() {
        return vegPont;
    }
    
    public final void set(int x0, int y0, int x1, int y1 ){
        this.kezdoPont.set(x0, y0);
        this.vegPont.set(x1, y1);       
        init();
//        System.out.println(kezdoPont+"\n"+vegPont);
//        System.out.println("meredekseg = "+meredekseg);
//        System.out.println("tipus "+tipus);
//        System.out.println();        
    }    
    
    public final void set(Pont kezdoPont, Pont vegPont){
        this.kezdoPont.set(kezdoPont);
        this.vegPont.set(vegPont);       
        init();
//        System.out.println(kezdoPont+"\n"+vegPont);
//        System.out.println("meredekseg = "+meredekseg);
//        System.out.println("tipus "+tipus);
//        System.out.println();        
    }
    
    private void init(){

        
        if (Math.abs(kezdoPont.getX() - vegPont.getX())<=VASTAGSAG) {
            meredekseg = Float.POSITIVE_INFINITY;
            C = (kezdoPont.getX()+vegPont.getX())/2;
            tipus = Tipus.Y_TENGELLYEL_PARHUZAMOS;
            return;
        }
        if (Math.abs(kezdoPont.getY() - vegPont.getY())<=VASTAGSAG) {
            meredekseg = 0;
            C = (kezdoPont.getY()+vegPont.getY())/2;
            tipus = Tipus.X_TENGELLYEL_PARHUZAMOS;
            return;
        }
        float dy = vegPont.getY()-kezdoPont.getY();
        float dx = vegPont.getX()-kezdoPont.getX();
//        System.out.println(dy+"/"+dx);
        meredekseg = dy/dx;
        C = (float)kezdoPont.getY()-meredekseg*kezdoPont.getX();
        tipus = Tipus.ALTALANOS;
    }
    
    void mozgatDtavval(int mdx, int mdy) {
        kezdoPont.mozgatDtavval(mdx, mdy);
        vegPont.mozgatDtavval(mdx, mdy);
        init();
    }    

    public Tipus getTipus() {
        return tipus;
    }

    public float getMeredekseg() {
        return meredekseg;
    }

    public float getC() {
        return C;
    }
    
    public float Xszamol(float y){
        return (y-C)/meredekseg;
    }    

    public float Yszamol(float x){
        return x*meredekseg+C;
    }
    
    public static boolean pontAVonalonVan(Pont p, Pont v0, Pont v1){
        V1.set(v0, v1);
        return V1.rajtaVan(p);
    }    
    
    public boolean rajtaVan(Pont p){
        if (tipus == Tipus.Y_TENGELLYEL_PARHUZAMOS) {
            if (Math.abs(p.x - C)<VASTAGSAG) {
                return vegPontokKozottVan(p);
            }
        }
        else {
            if (meredekseg>1) {
                if (Math.abs(p.x - Xszamol(p.y))<VASTAGSAG) {
                    return vegPontokKozottVan(p);
                }            
            }
            else {
                if (Math.abs(p.y - Yszamol(p.x))<VASTAGSAG) {
                    return vegPontokKozottVan(p);
                }
            }
        }
        return false;
    }
    
    private boolean vegPontokKozottVan(Pont p) {
        int x0 = Math.min(kezdoPont.x, vegPont.x);
        int y0 = Math.min(kezdoPont.y, vegPont.y);
        int x1 = Math.max(kezdoPont.x, vegPont.x);
        int y1 = Math.max(kezdoPont.y, vegPont.y);        
        switch (tipus) {
            case Y_TENGELLYEL_PARHUZAMOS:
                return (y0<=p.y && p.y<=y1);
            case X_TENGELLYEL_PARHUZAMOS:
                return (x0<=p.x && p.x<=x1);                
            default:
                return (x0<=p.x && p.x<=x1) && (y0<=p.y && p.y<=y1);
        }
        
    }
    
    public Pont metszesPont(Vonal masik){
        Pont p = metszesPontSzamol(masik);
        if (p == null) {return null;}
        if (vegPontokKozottVan(p) && masik.vegPontokKozottVan(p)) {
            return p;
        }
        else {
            return null;
        }
    }    
    
    private Pont metszesPontSzamol(Vonal masik){
        switch (tipus) {
            case X_TENGELLYEL_PARHUZAMOS:
                return metszesXtengParh(masik);
            case Y_TENGELLYEL_PARHUZAMOS:
                return metszesYtengParh(masik);    
            default: //ALTALANOS
                return metszesAltalanos(masik);
                
        }
    }
    
    private static Vonal V1 = new Vonal();
    private static Vonal V2 = new Vonal();
    private static Pont statMetszesPont = new Pont();
    public static Pont metszesPont(Pont v1k, Pont v1v, Pont v2k, Pont v2v){
        V1.set(v1k, v1v);
        V2.set(v2k, v2v);
        
        return V1.metszesPont(V2);
    }
    
    private Pont metszesXtengParh(Vonal masik){
        switch (masik.tipus) {
            case X_TENGELLYEL_PARHUZAMOS:
                return null;
            case Y_TENGELLYEL_PARHUZAMOS:
                statMetszesPont.set((int)masik.C, (int)C);
                break;
            default: //ALTALANOS
                statMetszesPont.set((int)masik.Xszamol(C), (int)C);
        }    
        return statMetszesPont;
    }
    
    private Pont metszesYtengParh(Vonal masik){
        switch (masik.tipus) {
            case X_TENGELLYEL_PARHUZAMOS:
                statMetszesPont.set((int)C, (int)masik.C);
                break;
            case Y_TENGELLYEL_PARHUZAMOS:
                return null;    
            default: //ALTALANOS
                statMetszesPont.set((int)C, (int)masik.Yszamol(C));
        } 
        return statMetszesPont;
    }    
    
    private Pont metszesAltalanos(Vonal masik){
        switch (masik.tipus) {
            case X_TENGELLYEL_PARHUZAMOS:
                statMetszesPont.set((int)Xszamol(masik.C), (int)masik.C);
                break;
            case Y_TENGELLYEL_PARHUZAMOS:
                statMetszesPont.set((int)masik.C, (int)Yszamol(masik.C));
                break;    
            default: //ALTALANOS
                if (masik.meredekseg == meredekseg) {
//                    System.out.println("masik.meredekseg == meredekseg");
                    return null;
                }
                else {
                    float X = (masik.C-C)/(meredekseg-masik.meredekseg);
//                    System.out.println("X "+X);
                    statMetszesPont.set((int)X, (int)Yszamol(X));
                }
        }    
        return statMetszesPont;
    }   
    
    
}
