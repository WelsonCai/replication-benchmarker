/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jbenchmarker.ot.ttf;

import jbenchmarker.ot.soct2.SOCT2Message;
import jbenchmarker.core.SequenceMessage;
import jbenchmarker.core.SequenceOperation;
import jbenchmarker.core.SequenceOperation.OpType;

/**
 * This object is only here for compatibility of sequence message
 * its a couple with SOCT2Message and original operation which has generated this message 
 * @author Stephane Martin
 */
public class TTFSequenceMessage extends SequenceMessage {

    SOCT2Message soct2Message;

    /**
     * Soct2Message with original operation 
     * @param soct2Message Message generated by soct2
     * @param o original Operation which have generate the message
     */
    public TTFSequenceMessage(SOCT2Message soct2Message, SequenceOperation o) {
        super(o);
        this.soct2Message = soct2Message;
    }

    /**
     * 
     * @return the message of operation
     */
    public SOCT2Message getSoct2Message() {
        return soct2Message;
    }

    /**
     * 
     * @return clone this operation
     */
    @Override
    public SequenceMessage copy() {
        return new TTFSequenceMessage(soct2Message.clone(), this.getOriginalOp());
    }

  
}
