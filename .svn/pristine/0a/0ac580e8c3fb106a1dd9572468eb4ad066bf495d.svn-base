/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import snmpAgent.data.LeafNode;
import snmpAgent.data.Node;

/**
 *
 * @author Tecnovision
 */
public class MIBParser {
 
    public final static String NOMBRE_COMPLETO = "nombreCompleto";
    public final static String MIB_ROOT_PREFIX = "1.3.6.1.4.1.";
    public final static String MIB_COMMENT = "--";
    public final static String MIB_END_BLOCK_TAG1 = ";";
    public final static String MIB_END_BLOCK_TAG2 = "}";
    
    private Node root;
    private static final Logger log = Logger.getLogger(MIBParser.class.getName());
    
    
    public Node parseMib(String filename) {

        // en elements guardamos todas las palabras de un bloque
        ArrayList<String> elements = new ArrayList<>();
        String line;

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                line = line.trim();   // línea a línea, quitando los espacios
                
                if((line.length() > 0) && (!line.startsWith(MIB_COMMENT))){
                    // en words guardamos provisionalmente todas las palabras de una línea leída.
                    String[] words = line.split(" ");
                    
                    for(int index = 0; index < words.length; index++){
                        String currentWord = words[index].trim();
                        
                        if(currentWord.length() > 0){
                            elements.add(currentWord);

                            if ((words[index].endsWith(MIB_END_BLOCK_TAG1))
                                    || (words[index].endsWith(MIB_END_BLOCK_TAG2))) {
                                // ya tenemos un bloque completo, lo procesamos
                                this.processMib(elements);
                                elements.clear();
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            log.error("Error fichero " + filename + " no encontrado");
            System.exit(-1);

        } catch (IOException ex) {
            log.error("Error al leer de fichero " + filename);
            System.exit(-1);
        }
        
        return root;
    }


    // tenemos un bloque completo del MIB, lo procesamos
    private void processMib(ArrayList<String> words){
        
        if(words.size() > 2){   // condición 1ª: nombre + identificador + valor
            
            String id = words.get(1);
            
            switch(id){
                case "DEFINITIONS":

                    //this.processDefinitionMib(words);
                    // No se procesa porque no recibimos informacion relevante (por ahora)
                    
                    /*   éste sería un ejemplo de bloque 'DEFINITIONS'
                    TEXTEL-TLI6-MIB DEFINITIONS ::= BEGIN
                    IMPORTS
                    NOTIFICATION-TYPE, OBJECT-TYPE, MODULE-IDENTITY, 
                    enterprises, Integer32
                            FROM SNMPv2-SMI
                    NOTIFICATION-GROUP, OBJECT-GROUP, MODULE-COMPLIANCE
                            FROM SNMPv2-CONF
                    RowStatus, DisplayString
                            FROM SNMPv2-TC;
                    */

                    break;

        
        
                case "OBJECT":

                    // examinando el MIB, en cualquier bloque OBJECT se mantiene este patrón...
                    // nombre_del_objeto IDENTIFICADORES(TIPO) ::= { padre posición_en_el_padre }
                    /*   éste sería un ejemplo de bloque 'OBJECT'
                    displayTextel 	OBJECT IDENTIFIER ::= { enterprises 41567 }
                    
                    fabricante OBJECT-TYPE
                    SYNTAX OCTET STRING
                    ACCESS read-write
                    STATUS current
                    DESCRIPTION
                            "Nombre del fabricante del display."
                    ::= { displayTextel 1 }
                    */

                    if(words.get(2).equals("IDENTIFIER")){
                        this.processObjectIdentifierMib(words);
                    }else{
                        // en el futuro ya buscaremos otros IDs. De momento...
                        log.error("Unexpected id:" + id + " " + words.get(2));
                    }

                    break;
                    
                case "OBJECT-TYPE":

                    /*   éste sería un ejemplo de bloque 'OBJECT-TYPE'
                    linea   OBJECT-TYPE
                    SYNTAX OCTET STRING
                    ACCESS read-write
                    STATUS current
                    DESCRIPTION
                            "Linea"
                    ::= { infoDisplay 1 }
                    */
                    
                    this.processMibLeafNodeType(words, "OBJECT-TYPE");
                    break;
                    
                case "NOTIFICATION-TYPE":
                    /*
                    	displayApagado NOTIFICATION-TYPE
                        STATUS current
                        DESCRIPTION
			"Display apagado"
                        ::= { notificaciones 6 }*/
                    this.processMibLeafNodeType(words, "NOTIFICATION-TYPE");
                    break;

                default:
                    log.error("Unexpected id:" + id);
            }
            
        }else{
            log.info("Se descarta un bloque incompleto del MIB");
        }
    }


    // displayTextel 	OBJECT IDENTIFIER ::= { enterprises 41567 }
    // infoDisplay OBJECT IDENTIFIER ::= { displayTextel 2 }
    private void processObjectIdentifierMib(ArrayList<String> words){
        
        if(words.size() == 8){
            String nameId = words.get(0);
            String parentName = words.get(5);
            String posInParent = words.get(6);
            
            // Nodo root
            if(parentName.equalsIgnoreCase("enterprises")){
                root = new Node(posInParent, nameId);
                root.setOid(MIB_ROOT_PREFIX + posInParent);
            
            // Nodo intermedio
            }else{
                Node child = new Node(posInParent, nameId);
                root.addChildByNameId(child, parentName);
            }

        }else{
            log.error("Se descarta bloque object identifier");
        }        
    }


    private void processMibLeafNodeType(ArrayList<String> words, String mibNodeType){
        
        if(words.size() >= 11){
            
            String nameId = words.get(0);
            String syntax = "", access = "", status = "", des = "", parentName = "", posInParent = "";
            
            for(int index = 2; index < words.size(); index++){
                
                switch(words.get(index)){
                    
                    case "SYNTAX":
                        while(!words.get(index + 1).equals("ACCESS")){
                            index++;
                            syntax += words.get(index) + " ";
                        }

                        syntax = syntax.trim();
                        break;
                        
                    case "ACCESS":
                        index++;
                        access = words.get(index);
                        break;
                        
                    case "STATUS":
                        index++;
                        status = words.get(index);
                        break;
                        
                    case "DESCRIPTION":
                        while(!words.get(index + 1).equals("::=")){
                            index++;
                            des += words.get(index) + " ";
                        }
                        break;
                        
                    case "::=": //::= { displayTextel 1 }
                        index += 2;
                        parentName = words.get(index++);
                        posInParent = words.get(index++);
                        break;
                        
                    default:
                        log.error("Se descarta un object type erroneo");
                    
                }
            }

            Node child = new LeafNode(syntax, access, status, des, posInParent, nameId, mibNodeType);
            root.addChildByNameId(child, parentName);
            
        }else{
            log.error("Se descarta bloque object-type");
        } 
    }

  
}
