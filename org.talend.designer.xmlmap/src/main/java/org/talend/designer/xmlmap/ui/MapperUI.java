package org.talend.designer.xmlmap.ui;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ExternalNodeChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.xmlmap.XmlMapComponent;
import org.talend.designer.xmlmap.editor.XmlMapEditor;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.ui.resource.ColorProviderMapper;
import org.talend.designer.xmlmap.ui.resource.FontProviderMapper;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.ui.tabs.TabFolderEditors;

public class MapperUI {

    private XmlMapComponent mapperComponent;

    private XmlMapData copyOfMapData;

    private int mapperResponse;

    private Shell mapperShell;

    private SashForm datasViewSashForm;

    private SashForm mainSashForm;

    private TabFolderEditors tabFolderEditors;

    private MapperManager mapperManager;

    private XmlMapEditor editor;

    private boolean closeWithoutPrompt;

    public MapperUI(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
        this.mapperComponent = mapperManager.getMapperComponent();
        this.copyOfMapData = mapperManager.getCopyOfMapData();
        mapperManager.setMapperUI(this);

    }

    public Shell createWindow(final Display display) {

        Shell activeShell = display.getActiveShell();
        int style = SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL | SWT.RESIZE;
        if (activeShell == null) {
            mapperShell = new Shell(mapperShell, style);
        } else {
            mapperShell = new Shell(activeShell, style);
        }

        mapperShell.addShellListener(new ShellListener() {

            public void shellActivated(ShellEvent e) {
            }

            public void shellClosed(ShellEvent e) {
                if (editor != null && editor.isDirty() && !closeWithoutPrompt) {
                    boolean closeWindow = MessageDialog.openConfirm(mapperShell, "Close without save",
                            "Are you sure to close window without save modifications ?");
                    if (!closeWindow) {
                        e.doit = false;
                    } else {
                        // mapperManager.getUiManager().prepareClosing(SWT.CANCEL);
                    }

                }

            }

            public void shellDeactivated(ShellEvent e) {
            }

            public void shellDeiconified(ShellEvent e) {
            }

            public void shellIconified(ShellEvent e) {
            }

        });

        mapperShell.setMaximized(true);

        mapperShell.setImage(CoreImageProvider.getComponentIcon(mapperComponent.getComponent(), ICON_SIZE.ICON_32));

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        String productName = brandingService.getFullProductName();
        mapperShell.setText(productName
                + " - " + mapperComponent.getComponent().getName() + " - " + mapperComponent.getUniqueName()); //$NON-NLS-1$

        GridLayout parentLayout = new GridLayout(1, true);
        mapperShell.setLayout(parentLayout);
        // Composite composite = new Composite(mapperShell, SWT.NONE);
        // composite.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));

        mainSashForm = new SashForm(mapperShell, SWT.SMOOTH | SWT.VERTICAL);
        GridData mainSashFormGridData = new GridData(GridData.FILL_BOTH);
        mainSashForm.setLayoutData(mainSashFormGridData);

        datasViewSashForm = new SashForm(mainSashForm, SWT.SMOOTH | SWT.HORIZONTAL | SWT.BORDER);
        editor = new XmlMapEditor(mapperManager);
        editor.createPartControl(datasViewSashForm);

        if (copyOfMapData.getVarTables().isEmpty()) {
            VarTable varTable1 = XmlmapFactory.eINSTANCE.createVarTable();
            varTable1.setName("Var"); //$NON-NLS-N$
            varTable1.setMinimized(true);
            copyOfMapData.getVarTables().add(varTable1);
        }
        editor.setContent(copyOfMapData);

        tabFolderEditors = new TabFolderEditors(mainSashForm, mapperManager, SWT.BORDER);
        selectFirstInOutTree();
        mainSashForm.setWeights(new int[] { 70, 30 });

        FooterComposite footerComposite = new FooterComposite(mapperShell, this);
        footerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mapperShell.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                ColorProviderMapper.releaseColors();
                FontProviderMapper.releaseFonts();
            }

        });

        mapperShell.open();
        return mapperShell;

    }

    private void selectFirstInOutTree() {
        if (!copyOfMapData.getInputTrees().isEmpty()) {
            InputXmlTree inputXmlTree = copyOfMapData.getInputTrees().get(0);
            mapperManager.selectInputXmlTree(inputXmlTree);
        }
        if (!copyOfMapData.getOutputTrees().isEmpty()) {
            OutputXmlTree outputXmlTree = copyOfMapData.getOutputTrees().get(0);
            mapperManager.selectOutputXmlTree(outputXmlTree);
        }
    }

    public void closeMapperDialog(int response) {
        mapperResponse = response;
        if (response == SWT.OK || response == SWT.APPLICATION_MODAL) {
            mapperComponent.setExternalEmfData(copyOfMapData);
            if (response == SWT.APPLICATION_MODAL) {
                IExternalNode externalNode = mapperComponent;
                IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

                if (externalNode != null && (part instanceof AbstractMultiPageTalendEditor)) {
                    INode node = externalNode.getOriginalNode();
                    if (node != null && node instanceof Node) {
                        Command cmd = new ExternalNodeChangeCommand((Node) node, externalNode);
                        CommandStack cmdStack = (CommandStack) part.getAdapter(CommandStack.class);
                        cmdStack.execute(cmd);
                    }
                }
            }
        }
        if (response == SWT.OK) {
            closeWithoutPrompt = true;
        }
        if (response == SWT.OK || response == SWT.CANCEL) {
            mapperShell.close();
        }

    }

    public int getMapperDialogResponse() {
        return mapperResponse;
    }

    public XmlMapComponent getMapperComponent() {
        return mapperComponent;
    }

    public TabFolderEditors getTabFolderEditors() {
        return tabFolderEditors;
    }

}
