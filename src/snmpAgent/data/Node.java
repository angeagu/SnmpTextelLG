/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package snmpAgent.data;

import java.util.ArrayList;
import java.util.Iterator;
import org.snmp4j.smi.Variable;

/**
 *
 * @author Tecnovision
 */
public class Node {

    protected Node parent;
    protected ArrayList<Node> children;
    
    protected String oid;
    protected String nameId;                    // Lo necesitamos saber para construir el arbol (lectura del MIB)
    protected String positionInParentNode;      // Lo necesitamos saber para cuando busquemos por OIDs
    
    
    // al constructor del nuevo nodo se le pasa la posición (orden) en el nodo padre y el nombre
    public Node(String pos, String nId) {
        parent = null;
        positionInParentNode = pos;
        nameId = nId;
        children = new ArrayList<>();
    }


    public void addChildAt(Node child, int index){
        
        while(index > children.size()){
            children.add(new Node("-1", "empty"));
        }
        
        children.set((index - 1), child);
    }
    
    
    public boolean addChildByNameId(Node child, String parentName){
        boolean success = false;
        
        // Condicion de parada
        if(nameId.equalsIgnoreCase(parentName)){
            child.setParent(this);
            child.setOid(this.getOid() + "." + child.getPositionInParentNode());
            this.addChildAt(child, child.getPositionInParentNode());
            success = true;
                    
        } else {

            for (Iterator<Node> it = children.iterator(); it.hasNext() && !success;) {
                Node currentNode = it.next();
                success = currentNode.addChildByNameId(child, parentName);
            }
        }

        return success;
    }

  
    public LeafNode getLeafNodeByOid(String oid){
        LeafNode node = null;

        for (Iterator<Node> it = children.iterator(); (it.hasNext()) && (node == null);) {
            Node currentNode = it.next();
            node = currentNode.getLeafNodeByOid(oid);
        }

        return node;
    }
    
    
    public Node getNodeByOid(String oid){
        Node node = null;
        
        if(oid.equals(this.getOid())){
            node = this;
            
        }else{
            for (Iterator<Node> it = children.iterator(); (it.hasNext()) && (node == null);) {
                Node currentNode = it.next();
                node = currentNode.getNodeByOid(oid);
            }
        }

        return node;
    }
    
    
    public Node getNext(int pos){
        Node nextNode = null;
        
        // Si tengo hijos, el siguiente es
        //      si pos == -1, el primer hijo
        //      si pos != -1, es el hijo en pos+1
        //          Si pos+1 no exister, es mi hermano
        int index = (pos == -1) ? 0 : pos + 1;
        if(children.size() > index){
            nextNode = children.get(index);
        
        // Sino, es mi hermano
        }else{
            if(parent != null){
                nextNode = parent.getNext(Integer.parseInt(positionInParentNode) - 1);
            }
        }
        
        return nextNode;
    }


    public String getNameId() {
        return nameId;
    }

    public String getOid() {
        return oid;
    }

    public void setParent(Node p){
        parent = p;
    }
    
    public void setOid(String currentOid){
        oid = currentOid;
    }

    public int getPositionInParentNode() {
        return Integer.parseInt(positionInParentNode);
    }

    // Un nodo intermedio nunca tiene valor
    public Variable getSnmpValue(){
        return null;
    }


    public void print(){
        
        System.out.println(nameId + " " + oid);
        
        for (Node currentNode : children) {
            currentNode.print();
        }
    }
}
