/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userLayer;

import java.util.ArrayList;
import javax.swing.JTextArea;
import org.apache.commons.io.input.TailerListenerAdapter;

/**
 *
 * @author Tecnovision
 */

/* Simple implementation of the unix "tail -f" functionality */
public class TailerListener extends TailerListenerAdapter {

    private final JTextArea textArea;
    private final ArrayList<String> currentText;

    public TailerListener(JTextArea area) {
        textArea = area;
        currentText = new ArrayList<>();
    }

    @Override
    public void handle(String line) {

        /* recorremos la línea para quitarle la fecha y el campo entre paréntesis*/
        /* separamos la línea por espacios */
        String[] words = line.split("\\s+");

        /* comprobamos si el primer campo es el de la fecha
         /* para ello separamos la línea por guiones y comprobamos que hay tres campos */
        String[] dataFields = words[0].split("-");
        if (dataFields.length == 3) {
            words[0] = null;
        }

        /* quitamos el tercer campo si es INFO o DEBUG */
        if ((words[2].equalsIgnoreCase("INFO")) || (words[2].equalsIgnoreCase("DEBUG"))) {
            words[2] = null;
        }

        /* comprobamos si el cuarto campo es el que va entre paréntesis */
        if ((words[3].startsWith("(")) && (words[3].endsWith(")"))) {
            words[3] = null;
        }

        /* construimos la nueva línea quitando los nulos */
        String newLine = "";
        for (String word : words) {
            if (word != null) {
                newLine += word + " ";
            }
        }
        currentText.add(newLine);

        if (currentText.size() > 10) {
            currentText.remove(0);
        }

        textArea.setText("");

        for (String currentLine : currentText) {
            textArea.append(currentLine + "\n");
        }
    }
}
