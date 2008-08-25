// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.scd.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.i18n.Messages;

/**
 * DOC hcw class global comment. Detailled comment
 */
public abstract class AbstractScdDialog extends TrayDialog {

    protected static final int SECTION_HEIGHT = 100;

    protected static final int SECTION_WIDTH = 400;

    protected ScdManager scdManager;

    protected Text filterText;

    protected FieldSection unusedFields;

    protected FieldSection type0Fields;

    protected FieldSection type1Fields;

    protected FieldSection sourceKeys;

    protected Type2Section type2Fields;

    protected SurrogateSection surrogateKeys;

    protected Type3Section type3Fields;

    /**
     * DOC hcw AbstractScdDialog constructor comment.
     * 
     * @param shell
     */
    public AbstractScdDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.TITLE);
    }

    // /**
    // * DOC hcw AbstractScdDialog constructor comment.
    // *
    // * @param parentShell
    // */
    // public AbstractScdDialog(IShellProvider parentShell) {
    // super(parentShell);
    // }

    /**
     * Create contents of the dialog
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().applyTo(container);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "org.talend.designer.scd.scdDialog");
        getShell().addListener(SWT.Close, new Listener() {

            public void handleEvent(Event event) {
                showWarningDialog();
            }

        });
        createScdContents(container);
        return container;
    }

    @Override
    protected void cancelPressed() {
        setReturnCode(CANCEL);
        showWarningDialog();
    }

    /**
     * Prompt the user for saving before closing the dialog.
     */
    protected void showWarningDialog() {
        boolean isNotSaveSetting = MessageDialog.openQuestion(getShell(), Messages.getString("UIManager.MessageBox.title"),
                Messages.getString("UIManager.MessageBox.Content"));
        if (!isNotSaveSetting) {
            setReturnCode(OK);
        }
        close();
    }

    abstract Control createScdContents(Composite container);

    /**
     * DOC hcw Comment method "createFilter".
     * 
     * @param container
     * @return
     */
    protected ViewerFilter createFilter(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
        GridDataFactory.swtDefaults().hint(SECTION_WIDTH, SWT.DEFAULT).applyTo(composite);

        filterText = new Text(composite, SWT.BORDER);
        GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).hint(SECTION_WIDTH - 200, SWT.DEFAULT).applyTo(filterText);
        Button filterButton = new Button(composite, SWT.PUSH);
        filterButton.setText("filter");
        filterButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                applyFilter();
            }

        });

        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String pattern = filterText.getText();
                SearchPattern matcher = new SearchPattern();
                matcher.setPattern(pattern);
                return matcher.matches(element.toString());
            }
        };

        return filter;
    }

    /**
     * DOC hcw Comment method "applyFilter".
     */
    protected void applyFilter() {
        unusedFields.getTableViewer().refresh();
    }

    /**
     * Create contents of the button bar
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("SCD component editor");
    }

    public void addContextHelp(Control control, final String contextId) {
        final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
        helpSystem.setHelp(control, contextId);
        control.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseEnter(MouseEvent e) {
                if (getTray() != null) {
                    helpSystem.displayHelp(contextId);
                }
            }
        });
    }

}
