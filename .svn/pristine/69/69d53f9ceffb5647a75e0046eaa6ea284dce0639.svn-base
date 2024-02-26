/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configuration.data;

import org.apache.log4j.Logger;

/**
 *
 * @author Tecnovision
 */
public class CommandForXML extends Command {

    private static final Logger LOG = Logger.getLogger(CommandForXML.class.getName());

    private boolean correct = false;

    private String nodeName;
    private int nodeItemNumber;
    private String childName;
    private String attributeName;
    private String attributeNameContent;
    private String valueName;

    public CommandForXML(String data, String type) {
        super(type);
        this.assignDataContent(data);
    }

    private void assignDataContent(String data) {
        try {
            if (data != null) {
                String[] elements = data.split(",");
                if (elements.length == 6) {
                    nodeName = elements[0];
                    nodeItemNumber = Integer.parseInt(elements[1]);
                    childName = elements[2];
                    attributeName = elements[3];
                    attributeNameContent = elements[4];
                    valueName = elements[5];

                    correct = true;

                    return;
                }
            }

        } catch (Exception ex) {
        }

        LOG.error("bad dataContent [" + data + "] in commandForXML");
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getNodeItemNumber() {
        return nodeItemNumber;
    }

    public String getChildName() {
        return childName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeNameContent() {
        return attributeNameContent;
    }

    public String getValueName() {
        return valueName;
    }
}
