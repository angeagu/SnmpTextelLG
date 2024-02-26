/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Tecnovision
 */
public class EditXMLConfigFile {

    public static void main(String argv[]) {

        try {
            String nombre;
            String valor;
            boolean attributeHasntBeenFound = true;

            String filepath = "config.xml";
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            System.out.println("--------------------- buscando los hijos de cada nodo");

            // listar los hijos de document
            NodeList hijos = doc.getChildNodes();
            for (int i = 0; i < hijos.getLength(); i++) {
                Node hijo = hijos.item(i);
                System.out.println("hijo [" + hijo.getNodeName() + "]");

                // listar los nietos de document
                NodeList nietos = hijo.getChildNodes();
                for (int j = 0; j < nietos.getLength(); j++) {
                    Node nieto = nietos.item(j);
                    if (!"#text".equals(nombre = nieto.getNodeName())) {
                        System.out.println("nieto [" + nombre + "]");

                        // listar los bisnietos de document
                        NodeList bisnietos = nieto.getChildNodes();
                        for (int k = 0; k < bisnietos.getLength(); k++) {
                            Node bisnieto = bisnietos.item(k);
                            if (!"#text".equals(nombre = bisnieto.getNodeName())) {
                                System.out.println("bisnieto [" + nombre + "]");

                                // listar los tataranietos de document
                                NodeList tataranietos = bisnieto.getChildNodes();
                                for (int l = 0; l < tataranietos.getLength(); l++) {
                                    Node tataranieto = tataranietos.item(l);
                                    if (!"#text".equals(nombre = tataranieto.getNodeName())) {
                                        System.out.println("tataranieto [" + nombre + "]");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("--------------------- otra forma de hacerlo, directamente por el nombre");

// acceder directamente a un nodo y listar sus hijos (y sus atributos)
// si es la IP... la cambiamos            
            Node notifications = doc.getElementsByTagName("NOTIFICATIONS").item(0);
            System.out.println("padre [" + notifications.getNodeName() + "]");
            NodeList otrosHijos = notifications.getChildNodes();
            for (int i = 0; i < otrosHijos.getLength(); i++) {
                Node otroHijo = otrosHijos.item(i);
                if (!"#text".equals(nombre = otroHijo.getNodeName())) {
                    System.out.println("otro Hijo [" + nombre + "]");

                    NamedNodeMap attr = otroHijo.getAttributes();
                    for (int j = 0; j < attr.getLength(); j++) {
                        Node attrHijo = attr.item(j);
                        System.out.println("attributo [" + (nombre = attrHijo.getNodeName()) + "] valor [" + (valor = attrHijo.getNodeValue()) + "]");

                        if (attributeHasntBeenFound) {
                            if ("ADMINISTRATOR_IP_ADDRESS".equals(valor)) {
                                attributeHasntBeenFound = false;
                            }

                        } else {
                            if ("VALUE".equals(nombre)) {
                                attrHijo.setTextContent("192.168.1.114");
                            }
                            attributeHasntBeenFound = true;
                            System.out.println("attributo [" + (nombre = attrHijo.getNodeName()) + "] valor [" + (valor = attrHijo.getNodeValue()) + "]");
                        }
                    }
                }
            }

            System.out.println("---------------------");

            /*
             // Get the root element
             //Node config = doc.getFirstChild();    //// sirve para algo?
             // append a new node to staff
             Element age = doc.createElement("age");
             age.appendChild(doc.createTextNode("28"));
             staff.appendChild(age);
             // get the salary element, and update the value
             if ("salary".equals(node.getNodeName())) {
             node.setTextContent("2000000");
             }
             //remove firstname
             if ("firstname".equals(node.getNodeName())) {
             staff.removeChild(node);
             }
             */
            //}
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

            System.out.println("Done");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        }
        /*
         } catch (ParserConfigurationException pce) {
         pce.printStackTrace();
         } catch (TransformerException tfe) {
         tfe.printStackTrace();
         } catch (IOException ioe) {
         ioe.printStackTrace();
         } catch (SAXException sae) {
         sae.printStackTrace();
         }
         */
    }

}
