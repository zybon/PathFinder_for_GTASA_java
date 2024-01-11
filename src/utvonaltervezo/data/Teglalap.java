package utvonaltervezo.data;

/**
 *
 * @author zybon
 * Created 2017.10.09. 15:37:26
 */
public class Teglalap {
    
    int left;
    int top;
    int right;
    int bottom;

    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int getLeft() {
        return left;
    }
    
    public int getTop() {
        return top;
    }    

    public int getRight() {
        return right;
    }    

    public int getBottom() {
        return bottom;
    }

    public int getSzel(){
        return right-left;
    }

    public int getMag(){
        return bottom-top;
    }

    @Override
    public String toString() {
        return "Teglalap{" + "left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom + '}';
    }
    
    public boolean benneVan(Pont p){
        return left<=p.x && right>=p.x && top<=p.y && bottom>=p.y;
    }
    
    

}
