// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.ui.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * created by wchen on Jan 11, 2017 Detailled comment
 *
 */
public class ConsoleDocument extends Document {

    private int startOffset;

    private int startMsgLength;

    private int endOffset;

    private int endMsgLength;

    public void setStartMsgLength(int startOffset, int startMsgLength) {
        this.startOffset = startOffset;
        this.startMsgLength = startMsgLength;
    }

    public void setEndMsgLength(int endOffset, int endMsgLength) {
        this.endOffset = endOffset;
        this.endMsgLength = endMsgLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.AbstractDocument#get(int, int)
     */
    @Override
    public String get(int pos, int length) throws BadLocationException {
        return super.get(pos, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.AbstractDocument#replace(int, int, java.lang.String)
     */
    @Override
    public void replace(int pos, int length, String text) throws BadLocationException {
        super.replace(pos, length, text);
    }

    /**
     * Getter for startOffset.
     * 
     * @return the startOffset
     */
    public int getStartOffset() {
        return this.startOffset;
    }

    /**
     * Getter for startMsgLength.
     * 
     * @return the startMsgLength
     */
    public int getStartMsgLength() {
        return this.startMsgLength;
    }

    /**
     * Getter for endOffset.
     * 
     * @return the endOffset
     */
    public int getEndOffset() {
        return this.endOffset;
    }

    /**
     * Getter for endMsgLength.
     * 
     * @return the endMsgLength
     */
    public int getEndMsgLength() {
        return this.endMsgLength;
    }

}
