// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.runprocess;

import org.talend.commons.exception.ExceptionHandler;

/**
 * Message about a process. <br/>
 * 
 * $Id$
 * 
 * 
 */
public class ProcessMessage implements IProcessMessage {

    /** Type of the message. */
    public enum MsgType implements IMsgType {
        STD_OUT,
        STD_ERR,
        CORE_OUT,
        CORE_ERR
    }

    /** Type of the message. */
    public IMsgType type;

    /** Content of the message. */
    public String content;

    /**
     * Constrcucts a new ProcessMessage.
     */
    public ProcessMessage(IMsgType type, String content) {
        super();

        if (type == null) {
            ExceptionHandler.process(new IllegalArgumentException("Type is null"));
        }
        if (content == null) {
            ExceptionHandler.process(new IllegalArgumentException("Content is null"));
        }

        this.type = type;
        this.content = content;
    }

    /**
     * Getter for content.
     * 
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Getter for type.
     * 
     * @return the type
     */
    public IMsgType getType() {
        return this.type;
    }

}
