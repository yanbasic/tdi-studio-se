// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.business.diagram.custom;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.NoteEditPart;
import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.NoteAttachmentEditPart;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.impl.EdgeImpl;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.business.BusinessAlignment;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.designer.business.diagram.custom.actions.CreateDiagramAction;
import org.talend.designer.business.diagram.custom.actions.DeleteAssignmentAction;
import org.talend.designer.business.diagram.custom.actions.DiagramResourceManager;
import org.talend.designer.business.diagram.custom.commands.ChangeBusinessItemAlignmentCommand;
import org.talend.designer.business.diagram.custom.edit.parts.BaseBusinessItemRelationShipEditPart;
import org.talend.designer.business.diagram.custom.edit.parts.BusinessItemShapeEditPart;
import org.talend.designer.business.diagram.custom.properties.RepositoryFactoryProxyLabelProvider;
import org.talend.designer.business.model.business.BusinessItem;
import org.talend.designer.business.model.business.diagram.edit.parts.BusinessProcessEditPart;
import org.talend.designer.business.model.business.diagram.part.BusinessDiagramEditor;
import org.talend.designer.business.model.business.provider.BusinessItemProviderAdapterFactory;
import org.talend.repository.editor.RepositoryEditorInput;

/**
 * DOC qian class global comment. An implementation of the IRunProcessService. <br/>
 * 
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期�? 29 九月 2006) nrousseau $
 * 
 */

public class DiagramModelService implements IDiagramModelService {

    public IAction getCreateDiagramAction(boolean isToolbar) {
        return new CreateDiagramAction(isToolbar);
    }

    public void refreshBusinessModel(IEditorReference editors) {
        IEditorPart editor = editors.getEditor(true);
        if (editor instanceof BusinessDiagramEditor) {
            BusinessDiagramEditor businessDiagramEditor = (BusinessDiagramEditor) editor;
            businessDiagramEditor.refresh();
        }
    }

    public boolean isBusinessDiagramEditor(IEditorPart part) {
        if (part instanceof BusinessDiagramEditor) {
            return true;
        }
        return false;

    }

    public RepositoryEditorInput getBusinessDiagramEditorInput(IEditorPart editor) {
        if (editor instanceof BusinessDiagramEditor) {
            BusinessDiagramEditor businessEditor = (BusinessDiagramEditor) editor;
            return businessEditor.getDiagramEditorInput();
        }
        return null;

    }

    public DiagramEditPart getBusinessEditorProcess() {
        BusinessDiagramEditor editor = (BusinessDiagramEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        return editor.getDiagramEditPart();
    }

    public BusinessType getBusinessModelType(Object part) {
        if (part instanceof BusinessProcessEditPart) {
            return BusinessType.PROCESS;
        } else if (part instanceof BusinessItemShapeEditPart) {
            return BusinessType.SHAP;
        } else if (part instanceof BaseBusinessItemRelationShipEditPart || part instanceof NoteAttachmentEditPart) {
            return BusinessType.CONNECTION;
        } else if (part instanceof NoteEditPart) {
            return BusinessType.NOTE;
        }

        return null;
    }

    public AdapterFactory getBusinessItemProviderAdapterFactory() {
        return new BusinessItemProviderAdapterFactory();
    }

    public AdapterFactoryLabelProvider getRepositoryFactoryProxyLabelProvider(AdapterFactory factory) {
        return new RepositoryFactoryProxyLabelProvider(factory);
    }

    public EObject getEObject(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            if (obj instanceof BusinessItemShapeEditPart) {
                return ((Node) ((BusinessItemShapeEditPart) obj).getModel()).getElement();
            } else if (obj instanceof BaseBusinessItemRelationShipEditPart) {
                Object model = ((BaseBusinessItemRelationShipEditPart) obj).getModel();
                return ((EdgeImpl) model).getElement();
            }

        }
        return null;
    }

    public ISelection getBusinessEditorSelection(IEditorPart editor) {
        if (editor instanceof BusinessDiagramEditor) {
            return ((BusinessDiagramEditor) editor).getSelection();
        }
        return null;
    }

    public void setBusinessItemAlignment(BusinessAlignment alignment, BusinessAlignment alignmentGroup, Object part) {

        if (part instanceof BusinessItemShapeEditPart) {
            BusinessItem item = (BusinessItem) ((Node) ((BusinessItemShapeEditPart) part).getModel()).getElement();
            ChangeBusinessItemAlignmentCommand command = new ChangeBusinessItemAlignmentCommand(item, alignment, alignmentGroup);
            try {
                command.execute(null, null);
            } catch (ExecutionException e) {
            }
            ((BusinessItemShapeEditPart) part).refreshVisuals();

        }
    }

    public String getBusinessItemAlignment(Object part, BusinessAlignment alignmentGroup) {
        if (part instanceof BusinessItemShapeEditPart) {
            EObject object = ((Node) ((BusinessItemShapeEditPart) part).getModel()).getElement();
            if (object instanceof BusinessItem) {
                BusinessItem item = (BusinessItem) object;
                if (BusinessAlignment.HORIZONTAL.equals(alignmentGroup)) {
                    return item.getHAlignment();
                } else if (BusinessAlignment.VERTICAL.equals(alignmentGroup)) {
                    return item.getVAlignment();
                }
            }
        }
        return null;
    }

    public void setBusinessItemsAlignment(BusinessAlignment alignment, BusinessAlignment alignmentGroup, Object part) {
        if (part instanceof BusinessProcessEditPart) {
            BusinessProcessEditPart processPart = (BusinessProcessEditPart) part;
            for (Object object : processPart.getChildren()) {
                if (object instanceof BusinessItemShapeEditPart) {
                    setBusinessItemAlignment(alignment, alignmentGroup, object);
                }
            }
        }
    }

    public void handleNewEditorAction(IWorkbenchPart editor) {
        if (editor instanceof BusinessDiagramEditor) {
            BusinessDiagramEditor diagrmEditor = (BusinessDiagramEditor) editor;
            if (diagrmEditor.isAlreadyOpened()) {
                IEditorReference[] ref = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditors(
                        diagrmEditor.getEditorInput(), diagrmEditor.getEditorID(), IWorkbenchPage.MATCH_INPUT);
                IEditorPart editorPart = ref[0].getEditor(false);
                editorPart.doSave(new NullProgressMonitor());
                ((BusinessDiagramEditor) editorPart).setKeepPropertyLocked(true);
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editorPart, false);
            }
        }
    }

    public void openBusinessDiagramEditor(IWorkbenchPage page, IEditorInput input) throws PartInitException {
        page.openEditor(input, BusinessDiagramEditor.ID, false);
    }

    public IFile getDiagramFile(IWorkbenchPage page) {
        DiagramResourceManager diagramResourceManager = new DiagramResourceManager(page, new NullProgressMonitor());
        return diagramResourceManager.createDiagramFile();
    }

    public void addDeleteAssignmentAction(IMenuManager mgr, ISelection selection) {
        if (selection == null || selection.isEmpty()) {
            return;
        }

        DeleteAssignmentAction action = new DeleteAssignmentAction(selection);
        mgr.add(action);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.business.diagram.custom.IDiagramModelService#updateProcessFromResource(org.talend.core.model
     * .properties.BusinessProcessItem)
     */
    public void updateProcessFromResource(BusinessProcessItem processItem) {
        DiagramResourceManager diagramResourceManager = new DiagramResourceManager(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage(), new NullProgressMonitor());
        IFile file = diagramResourceManager.createDiagramFile();
        diagramResourceManager.updateFromResource(processItem, file);
    }
}
