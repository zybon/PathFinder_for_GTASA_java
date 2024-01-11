package utvonaltervezo.data;

/**
 *
 * @author zybon
 * Created 2017.09.20. 14:29:25
 */
public class Pont {
    
    
    public int x;
    public int y;
    
    private static final Pont kozepPont = new Pont(0,0);
    
    private boolean kijeloles = false;
    
    public Pont(Pont p) {
        this(p.x, p.y);
    }    

    public Pont() {
        this(0, 0);
    }
    
    public Pont(int x, int y) {
        this.x = x;
        this.y = y;
    }  

    public void setKijeloles(boolean kijeloles) {
        this.kijeloles = kijeloles;
    }

    public boolean isKijeloles() {
        return kijeloles;
    }
    
    public final void set(int x, int y) {
        this.x = x;
        this.y = y;
        onSet();
    }  
    
    protected void onSet(){}
    
    public void set(Pont p) {
        set(p.x, p.y);
    }   
    
    public void setX(int x){
        this.x = x;
        set(x, y);
    }
    
    public void setY(int y){
        this.y = y;
        set(x, y);
    }      

    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }    
    
    public float getTav(Pont masik){
        double px = masik.x - this.x;
        double py = masik.y - this.y;
        return (float)Math.sqrt(px * px + py * py);        
    }
    
    public double getTav(int x, int y){
        x -= this.x;
        y -= this.y;
        return Math.sqrt(x * x + y * y);
    }
    
    public Pont getKozepPont(Pont masik){
        kozepPont.set((masik.x + this.x)/2, (masik.y + this.y)/2);
        return kozepPont;
    }    

    public void forgat(int x0, int y0, double sinA, double cosA){
        int aktX = x-x0;
        int aktY = y-y0;
        x = (int)(Math.round(aktX*cosA - aktY*sinA) + x0);
        y = (int)(Math.round(aktX*sinA + aktY*cosA) + y0); 
    }
    
    public void mozgatDtavval(int dx, int dy) {
        mozgatIde(x+dx, y+dy);
    }
    
    public void mozgatIde(int x, int y) {
        set(x, y);
    }    

    @Override
    public String toString() {
        return "Pont{" + "x=" + x + ", y=" + y + '}';
    }
    
   
   

}
