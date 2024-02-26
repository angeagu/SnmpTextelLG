/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author TEXTEL
 */
public class MyStaticImage {
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyStaticImage.class.getName());

    public MyStaticImage(int posX, int posY, int width, int height) {

        System.out.println(width + " " + height);
        
        JFrame frameForImages = new JFrame();
        frameForImages.setBounds(posX, posY, width, height);
        frameForImages.setUndecorated(true);


        JPanel panelForImages = new JPanel();
        panelForImages.setBackground(Color.yellow);
        frameForImages.getContentPane().add(panelForImages);
        
        JLabel labelForImage = new JLabel();
        panelForImages.add(labelForImage);
        
        
        System.out.println(labelForImage.getSize());
        
        File bck = new File("background.png");
        
        if(bck.exists()){
            
            System.out.println("");
            
            try {
                Image img = ImageIO.read(bck);
                ImageIcon icon = new ImageIcon(img);
                labelForImage.setIcon(icon);
                System.out.println(labelForImage.getLocation());
                frameForImages.setVisible(true);
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }
}
