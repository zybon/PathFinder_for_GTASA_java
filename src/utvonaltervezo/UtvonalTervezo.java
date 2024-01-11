package utvonaltervezo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 *
 * @author Zybon
 * Created time: 2016.09.08 2:31:48
 */
public class UtvonalTervezo extends JFrame{
    
    
    private final Futo futo = new Futo();
    private final Thread szal = new Thread(futo);
    
    private final EszkozTar eszkozTar;
    private final JPanel lablec = new JPanel();
    MapPanel map;
    
    public static final boolean AUTORAJZ_SZUNETTEL = false;
//    private final KeyEsemeny keyEsemeny = new KeyEsemeny();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new UtvonalTervezo();
    }
    
    private static final JTextArea szovegMezo = new JTextArea();
    
    public UtvonalTervezo() {
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);    
        setTitle("Útvonal tervező");
        setLayout(new BorderLayout());
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new MyDispatcher());
        map = new MapPanel(this);
        eszkozTar = new EszkozTar(map);
        
        JPanel felsoSplit = new JPanel();
        felsoSplit.setLayout(new BorderLayout());
        felsoSplit.add(eszkozTar, BorderLayout.WEST);
        felsoSplit.add(map, BorderLayout.CENTER);
        
            szovegMezo.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
            szovegMezo.setEditable(false);
            szovegMezo.setLineWrap(true);
            JScrollPane jsc = new JScrollPane(szovegMezo);   
//        lablec.setPreferredSize(new Dimension(0, 100));
//        lablec.setMinimumSize(new Dimension(0, 100));
        lablec.setLayout(new BorderLayout());
        lablec.add(jsc, BorderLayout.CENTER);
        
//        add(lablec, BorderLayout.SOUTH);
        
        JSplitPane jsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, felsoSplit, lablec);
        
        add(jsplit, BorderLayout.CENTER);
        setVisible(true);
        
        szalIndit();
        
        jsplit.setDividerLocation(0.9f);
    }
    
    private void szalIndit(){
        futo.mehet = true;
        szal.start();
               
    }   
    
    public static void frameInfo(Object o) {
        frameInfo(o.toString());
    }    

    public static void frameInfo(String szoveg) {
        szovegMezo.setText(szovegMezo.getText()+"\n"+szoveg);
        szovegMezo.setCaretPosition(szovegMezo.getDocument().getLength());
    }
    
    public static void frameInfoReset() {
        szovegMezo.setText("");
    }    

    private class Futo implements Runnable{
        private boolean mehet = false;
        @Override
        public void run() {
            long k;
            long t;
//            map.utHalozatBeolvasas();
            while (mehet){
                k = System.currentTimeMillis();
                map.requestFocusInWindow();
                repaint();
                t = 30-(System.currentTimeMillis()-k);
                if (t > 0) {
                    try {
                        Thread.sleep(t);
                    }
                    catch (InterruptedException ie) {
                       System.out.println("szálhiba:\n"+ie);
                    }
                }
            }
        }
    }
    
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
//                System.out.println(e);
                if (e.getKeyCode()==83 && e.isControlDown()) {
                    map.parancsKuldes(EszkozTar.Parancs.MENTES);
                    return true;
                }
            }
            return false;
        }
    }    
    
}
