/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utvonaltervezo;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import static utvonaltervezo.EszkozTar.Parancs.*;

/**
 *
 * @author Dékán Tamás
 */
public class EszkozTar extends JPanel{
    private final MapPanel mapPanel;
    private final JLabel teto = new JLabel();
    
    private final EszkozAction action = new EszkozAction();
    private final int SZEL = 65;
    private final int MAG = 300;
    
    public enum Parancs {
        UJ_CSOMOPONT("Új csomópont beszúrása","csomopont"),
        UJ_TORESPONT("Új töréspont beszúrása","torespont"),
        UJ_UT("Új út","ut"),
        UT_MASOLAS("Út felosztása","ut_felosztas"),
        KET_UT_EGYESITES("Két út egyesítése","ut_egyesites"),
        EGYIRANYU_UT("Egyirányúsítás","ut_egyiranyu"),
        TORLES("Törlés","torles"),
        UTVONALTERVKP("Útvonalterv kezdőpontja","utvonalterv_kp"),
        UTVONALTERVVP("Útvonalterv végpontja","utvonalterv_vp"),
        UTVONALTERVTERVEZ("Útvonaltervezés","utvonalterv"),
        UJRATOLT("Újratölt","ujratolt"),
        MENTES("Mentés","mentes"),
        AUTOUTRAJZ("AutoÚtRajz","mozgatas"),
        
        SEMMI("","")
        
        ;

        final String parancsString;
        final String kepPath;
        private Parancs(String parancs, String kepNev) {
            this.parancsString = parancs;
            this.kepPath = "/kepek/"+kepNev+".png";
        }
        
        
                
        
        

    };    
    
    
    
    public EszkozTar(MapPanel mapPanel){
        this.mapPanel = mapPanel;
        //setFloatable(true);
        //setRollover(true);        
        setPreferredSize(new Dimension(SZEL,MAG));
        setBackground(Color.WHITE);
        setLayout(null);
        teto.setBounds(2, 2, SZEL-4, 16);
        teto.setBackground(Color.BLUE);
        teto.setOpaque(true);
        add(teto);
        int x = 5;
        int y = 22;
        Parancs[] parancsok = new Parancs[]{
            UJ_UT, UT_MASOLAS,
            KET_UT_EGYESITES, SEMMI,
            EGYIRANYU_UT,SEMMI,
            UJ_CSOMOPONT, UJ_TORESPONT,
            TORLES,SEMMI,
            UTVONALTERVKP, UTVONALTERVVP,
            UTVONALTERVTERVEZ, SEMMI,
            UJRATOLT,MENTES,
            AUTOUTRAJZ
        };
        for (Parancs parancs : parancsok) {
            if (parancs != SEMMI) {
                add(new EszkozGomb(parancs, x, y));
            }
            x += 30;
            if (x > 35) {
                x = 5;
                y += 30;
            }
        }
//        CreateEszkozGomb eszkozGomb;
//        add(new CreateEszkozGomb(vonLancComm,"Vonallánc",5,22)); 
//        add(new CreateEszkozGomb(torlesComm,"Törlés",35,22));         
//        add(new CreateEszkozGomb(szinValComm,"Színválasztó",5,52));                 
//        add(new CreateEszkozGomb(aranyValtComm,"Arányváltás",35,52));  
        
    }
    
    
    private class EszkozGomb extends JButton{
        BufferedImage kep;
        String szoveg;
        Parancs parancs;
        
        EszkozGomb(Parancs parancs, int x, int y) {
            this.parancs = parancs;
            setToolTipText(createSzovegTip(parancs.parancsString)); 
            setBounds(x, y, 25, 25);
            setActionCommand(szoveg);
            addActionListener(action);
            try {
                kep = getKep(parancs.kepPath);
            } catch (IOException ex) {
                kep = null;
            }
            if (kep == null){
//                setText(szoveg.substring(0, 1));
//                setFont(font);
                this.szoveg = szoveg.substring(0, 1);
            }              
            
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (kep != null){
            g.drawImage(kep, 2, 2, 20, 20, null);
            }
            else {
                g.drawString(szoveg, 7, 18);
            }
          
        }
        
        
       private String createSzovegTip(String szoveg) {
            String tip = "<html><div style='background-color:#ffffff;margin-left:-3;margin-right:-3;padding:2 5;'>";
            tip += szoveg;
            tip += "</div></html>";
            return tip;
       }
       
        private BufferedImage getKep(String kepPath) throws IOException {
            return ImageIO.read(getClass().getResource(kepPath));
        }
           
    }
    

    class EszkozAction implements ActionListener{
        
        EszkozAction(){
        
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.parancsKuldes(((EszkozGomb)e.getSource()).parancs);
        }
    }
}
