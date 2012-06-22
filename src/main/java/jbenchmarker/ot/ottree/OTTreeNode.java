/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker.ot.ottree;

import collect.OrderedNode;
import crdt.tree.orderedtree.PositionIdentifier;
import crdt.tree.orderedtree.Positioned;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jbenchmarker.core.Operation;
import jbenchmarker.ot.soct2.SOCT2;

/**
 *
 * @author Stephane Martin <stephane.martin@loria.fr>
 */
public class OTTreeNode<T> implements OrderedNode<T> {

    int visibleChildren = 0;
    boolean visible;
    OTTreeNode<T> father;
    T contains;
    ArrayList<OTTreeNode<T>> childrens;

    public boolean isVisible() {
        return visible;
    }

    public List<Integer> viewToModelRecurcive(List<Integer> list) {
        LinkedList<Integer> ret = new LinkedList();
        ret.add(viewToModel(list.get(0)));
        if (ret.size() > 1) {
            ret.addAll(childrens.get(ret.get(0)).viewToModelRecurcive(list.subList(1, list.size() - 1)));
        }
        return ret;
    }

    public int viewToModel(int positionInView) {
        int positionInchildrens = 0;
        int visibleCharacterCount = 0;

        while (positionInchildrens < this.childrens.size() && (visibleCharacterCount < positionInView || (!this.childrens.get(positionInchildrens).isVisible()))) {
            if (this.childrens.get(positionInchildrens).isVisible()) {
                visibleCharacterCount++;
            }
            positionInchildrens++;
        }

        return positionInchildrens;
    }
    /*
     * public void remoteApply(Operation op){
     *
     * }
     */

    public void apply(Operation op, int level) {
        OTTreeRemoteOperation<T> oop = (OTTreeRemoteOperation<T>) op;
        int pos = oop.getPath().get(level);
        if (level == oop.getPath().size() - 1) {

            if (oop.getType() == OTTreeRemoteOperation.OpType.del) {
                OTTreeNode c = this.childrens.get(pos);
                if (c.isVisible()) {
                    --visibleChildren;
                }
                c.setVisible(false);
            } else if (oop.getType() == OTTreeRemoteOperation.OpType.ins) {
                this.childrens.add(pos, new OTTreeNode<T>(this, oop.getContain()));
                ++visibleChildren;
            }
        } else {
            childrens.get(pos).apply(op, level+1);
        }

    }

    /*
     * public OTTreeNode(OTTreeNode<T> father, T contains) { this.father =
     * father; this.contains = contains; this.visible = true;
     */
    public OTTreeNode(OTTreeNode<T> father, T contains) {
        this.father = father;
        this.contains = contains;
        this.visible = true;
        this.childrens=new ArrayList();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /*
     * ----
     */
    @Override
    public int childrenNumber() {
       return this.visibleChildren;
    }

    @Override
    public OrderedNode<T> getChild(int p) {
        return childrens.get(viewToModel(p));
    }

    @Override
    public OrderedNode<T> getChild(Positioned<T> p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public T getValue() {
        return contains;
    }

    @Override
    public Positioned<T> getPositioned(int p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PositionIdentifier getNewPosition(int p, T element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(PositionIdentifier pi, T element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(PositionIdentifier pi, T element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends OrderedNode<T>> getElements() {
        LinkedList ret = new LinkedList();
        for (OTTreeNode n : childrens) {
            if (n.isVisible()) {
                ret.add(n);
            }
        }
        return ret;
    }

    @Override
    public OrderedNode<T> createNode(T elem) {
        throw new UnsupportedOperationException("createNode is not supported yet.");
    }

    @Override
    public void setReplicaNumber(int replicaNumber) {
    }

    @Override
    public String toString() {
        StringBuilder t=new StringBuilder();
                t.append("OTTreeNode{" + "visibleChildren=" + visibleChildren + ", visible=" + visible + ", father=" + father + ", contains=" + contains );
                for (OTTreeNode n:this.childrens){
                    if (n.isVisible()){
                        t.append(n);
                    }
                }
                t.append("}");
        
        return t.toString();
    }

  

  
    
}
