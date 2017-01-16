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

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.runprocess.IProcessMessage;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;

/**
 * created by wchen on Jan 11, 2017 Detailled comment
 *
 */
public class ProcessConsolePartitioner {

    private int firstOffset;

    private Object overflowLock = new Object();

    private int highWaterMark = -1;

    private int lowWaterMark = -1;

    private TrimJob trimJob = new TrimJob();

    private ConsoleDocument document;

    private ConsoleDocumentAdapter consoleAdapter;

    /**
     * DOC wchen ProcessConsolePartitioner constructor comment.
     */
    public ProcessConsolePartitioner() {
        this.document = new ConsoleDocument();
        this.consoleAdapter = new ConsoleDocumentAdapter(-1);
        consoleAdapter.setDocument(document);
    }

    public void addDocumentListener(IDocumentListener listener) {
        document.addDocumentListener(listener);
    }

    /**
     * DOC wchen Comment method "doAppendToConsole".
     */
    public void doAppendToConsole(Collection<IProcessMessage> messages, List<StyleRange> styles) {
        // int startLength = document.getLength();
        StringBuffer consoleMsgText = new StringBuffer();
        for (IProcessMessage message : messages) {
            if (message.getType() == MsgType.STD_OUT) {
                String[] splitLines = message.getContent().split("\n"); //$NON-NLS-1$
                for (String lineContent : splitLines) {
                    IProcessMessage lineMsg = new ProcessMessage(getLog4jMsgType(MsgType.STD_OUT, lineContent), lineContent);
                    String content = lineMsg.getContent();
                    String[] contents = content.split("\n"); //$NON-NLS-1$
                    for (String content2 : contents) {
                        if (isPattern(content2) || isPatternFor(content2)) {
                            consoleMsgText.append(""); //$NON-NLS-1$
                            content = ""; //$NON-NLS-1$
                        } else {
                            consoleMsgText.append(content2);
                            consoleMsgText.append("\n"); //$NON-NLS-1$
                        }
                        if (consoleMsgText.length() > highWaterMark) {
                            changeDocument(consoleMsgText, styles);
                            consoleMsgText = new StringBuffer();
                        }
                    }
                }
            } else {
                // count as only one line for the error, to avoid the error to be cut from original
                consoleMsgText.append(message.getContent());
                consoleMsgText.append("\n"); //$NON-NLS-1$
                processMessage(consoleMsgText, message, styles);
            }
        }
        if (consoleMsgText.length() > 0) {
            changeDocument(consoleMsgText, styles);
        }

    }

    private void changeDocument(StringBuffer consoleMsgText, List<StyleRange> styles) {
        try {
            // synchronized (overflowLock) {
            document.replace(document.getLength(), 0, consoleMsgText.toString());
            System.out.println("*changeDocument length " + document.getLength());
            // int startLine = 0;
            // int endLine = document.getNumberOfLines() - 2;
            // IRegion startInformation = document.getLineInformation(startLine);
            // IRegion endInformation = document.getLineInformation(endLine);
            // System.out.println(document.get(startInformation.getOffset(),
            // startInformation.getLength() > 5 ? 5 : startInformation.getLength()));
            // System.out.println(document.get(endInformation.getOffset(),
            // endInformation.getLength() > 5 ? 5 : endInformation.getLength()));
            boolean bufferChanged = checkBufferSize();
            if (bufferChanged) {
                styles.clear();
            }
            // }
        } catch (BadLocationException e) {
            ExceptionHandler.process(e);
        }
    }

    public void processMessageQueue() {

    }

    private void processMessage(StringBuffer consoleText, IProcessMessage message, List<StyleRange> styles) {
        String content = message.getContent();

        int lengthBeforeAdd = document.getLength();
        int lengthAfterAdd = lengthBeforeAdd + consoleText.length();
        if (message.getType() != MsgType.STD_OUT) {
            StyleRange style = new StyleRange();
            style.start = lengthBeforeAdd;
            style.length = content.length();
            if (message.getType() == MsgType.CORE_OUT || message.getType() == MsgType.CORE_ERR) {
                style.fontStyle = SWT.ITALIC;
            }
            Color color = getColor((MsgType) message.getType());
            style.foreground = color;

            if ((style.start + style.length) > (lengthAfterAdd)) {
                style.length = lengthAfterAdd - style.start;
            }
            styles.add(style);
        }
    }

    private Color getColor(MsgType type) {
        Color color = null;
        switch (type) {
        case CORE_OUT:
            color = getDisplay().getSystemColor(SWT.COLOR_BLUE);
            break;
        case CORE_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
            break;
        case STD_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_RED);
            break;
        case LOG4J_TRACE:
        case LOG4J_DEBUG:
        case LOG4J_INFO:
            color = getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
            break;
        case LOG4J_WARN:
            color = getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
            break;
        case LOG4J_ERROR:
        case LOG4J_FATAL:
            color = getDisplay().getSystemColor(SWT.COLOR_RED);
            break;
        case STD_OUT:
        default:
            color = getDisplay().getSystemColor(SWT.COLOR_BLACK);
            break;
        }
        return color;
    }

    private MsgType getLog4jMsgType(MsgType outType, String lineContent) {
        MsgType msgLog4jType = null;
        if (outType.equals(MsgType.STD_OUT)) {
            if (lineContent.startsWith("[TRACE]")) { //$NON-NLS-1$
                return MsgType.LOG4J_TRACE;
            } else if (lineContent.startsWith("[INFO ]")) { //$NON-NLS-1$
                return MsgType.LOG4J_INFO;
            } else if (lineContent.startsWith("[DEBUG]")) { //$NON-NLS-1$
                return MsgType.LOG4J_DEBUG;
            } else if (lineContent.startsWith("[WARN ]")) { //$NON-NLS-1$
                return MsgType.LOG4J_WARN;
            } else if (lineContent.startsWith("[ERROR]")) { //$NON-NLS-1$
                return MsgType.LOG4J_ERROR;
            } else if (lineContent.startsWith("[FATAL]")) { //$NON-NLS-1$
                return MsgType.LOG4J_FATAL;
            } else {
                return outType;
            }
        }
        return msgLog4jType;
    }

    private Display getDisplay() {
        return Display.getDefault();
    }

    private boolean isPattern(String content) {
        Pattern pattern = Pattern.compile("\\$\\s*\\d+(\\.\\d*)?%"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    private boolean isPatternFor(String content) {
        Pattern pattern = Pattern.compile("\\[\\s*\\d+(\\.\\d*)?%\\]"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    public void setWaterMarks(int low, int high) {
        lowWaterMark = low;
        highWaterMark = high;
        getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                checkBufferSize();
            }
        });
    }

    private boolean checkBufferSize() {
        if (document != null && highWaterMark > 0) {
            int length = document.getLength();
            if (length > highWaterMark) {
                try {
                    int truncateOffset = length - lowWaterMark;
                    int cutoffLine = document.getLineOfOffset(truncateOffset);
                    int cutOffset = document.getLineOffset(cutoffLine);
                    // set the new length of the first partition
                    document.replace(0, cutOffset, ""); //$NON-NLS-1$
                    int startLine = 0;
                    int endLine = document.getNumberOfLines() - 2;
                    IRegion startInformation = document.getLineInformation(startLine);
                    IRegion endInformation = document.getLineInformation(endLine);
                    System.out.println(document.get(startInformation.getOffset(), startInformation.getLength() > 20 ? 20
                            : startInformation.getLength()));
                    System.out.println(document.get(endInformation.getOffset(), endInformation.getLength() > 20 ? 20
                            : endInformation.getLength()));
                } catch (BadLocationException e) {
                }
                // trimJob.setOffset(length - lowWaterMark);
                // trimJob.schedule();
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the console
     */
    public void clearBuffer() {
        document.set(""); //$NON-NLS-1$
        // synchronized (overflowLock) {
        // trimJob.setOffset(-1);
        // trimJob.schedule();
        // }
    }

    /**
     * Getter for consoleAdapter.
     * 
     * @return the consoleAdapter
     */
    public ConsoleDocumentAdapter getConsoleAdapter() {
        return this.consoleAdapter;
    }

    public void setDocument(ConsoleDocument document) {
        this.document = document;
    }

    private class QueueProcessingJob extends UIJob {

        private IProcessMessage message;

        private List<StyleRange> styles;

        QueueProcessingJob(IProcessMessage message, List<StyleRange> styles) {
            super("IOConsole Updater"); //$NON-NLS-1$
            this.message = message;
            this.styles = styles;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            // int startLength = document.getLength();
            StringBuffer consoleMsgText = new StringBuffer();
            if (message.getType() == MsgType.STD_OUT) {
                String[] splitLines = message.getContent().split("\n"); //$NON-NLS-1$
                for (String lineContent : splitLines) {
                    IProcessMessage lineMsg = new ProcessMessage(getLog4jMsgType(MsgType.STD_OUT, lineContent), lineContent);
                    String content = lineMsg.getContent();
                    String[] contents = content.split("\n"); //$NON-NLS-1$
                    for (String content2 : contents) {
                        if (isPattern(content2) || isPatternFor(content2)) {
                            consoleMsgText.append(""); //$NON-NLS-1$
                            content = ""; //$NON-NLS-1$
                        } else {
                            consoleMsgText.append(content2);
                            consoleMsgText.append("\n"); //$NON-NLS-1$
                        }
                        if (consoleMsgText.length() > highWaterMark) {
                            changeDocument(consoleMsgText, styles);
                            consoleMsgText = new StringBuffer();
                        }
                    }
                }
            } else {
                // count as only one line for the error, to avoid the error to be cut from original
                consoleMsgText.append(message.getContent());
                consoleMsgText.append("\n"); //$NON-NLS-1$
                processMessage(consoleMsgText, message, styles);
            }
            if (consoleMsgText.length() > 0) {
                changeDocument(consoleMsgText, styles);
            }

            return Status.OK_STATUS;
        }

    }

    private class TrimJob extends UIJob {

        /**
         * trims output up to the line containing the given offset, or all output if -1.
         */
        private int truncateOffset;

        /**
         * Creates a new job to trim the buffer.
         */
        TrimJob() {
            super("Trim Job"); //$NON-NLS-1$
            setSystem(true);
        }

        /**
         * Sets the trim offset.
         *
         * @param offset trims output up to the line containing the given offset
         */
        public void setOffset(int offset) {
            truncateOffset = offset;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            IJobManager jobManager = Job.getJobManager();
            try {
                jobManager.join(this, monitor);
            } catch (OperationCanceledException e1) {
                return Status.CANCEL_STATUS;
            } catch (InterruptedException e1) {
                return Status.CANCEL_STATUS;
            }
            if (document == null) {
                return Status.OK_STATUS;
            }

            int length = document.getLength();
            if (truncateOffset < length) {
                synchronized (overflowLock) {
                    try {
                        if (truncateOffset < 0) {
                            // clear
                            document.set(""); //$NON-NLS-1$
                        } else {
                            // overflow
                            int cutoffLine = document.getLineOfOffset(truncateOffset);
                            int cutOffset = document.getLineOffset(cutoffLine);
                            // set the new length of the first partition
                            document.replace(0, cutOffset, ""); //$NON-NLS-1$

                        }
                    } catch (BadLocationException e) {
                    }
                }
            }
            return Status.OK_STATUS;
        }
    }
}
