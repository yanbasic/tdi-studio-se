// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.ui.wizard.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.component.ui.model.genericMetadata.GenericConnection;
import org.talend.component.ui.model.genericMetadata.GenericConnectionItem;
import org.talend.component.ui.model.genericMetadata.GenericMetadataFactory;
import org.talend.component.ui.wizard.constants.IGenericConstants;
import org.talend.component.ui.wizard.i18n.Messages;
import org.talend.component.ui.wizard.internal.IGenericWizardInternalService;
import org.talend.component.ui.wizard.internal.service.GenericWizardInternalService;
import org.talend.component.ui.wizard.persistence.GenericRepository;
import org.talend.component.ui.wizard.ui.common.GenericWizardDialog;
import org.talend.component.ui.wizard.ui.common.GenericWizardPage;
import org.talend.component.ui.wizard.update.GenericUpdateManager;
import org.talend.component.ui.wizard.util.GenericWizardServiceFactory;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.ComponentProperties.Deserialized;
import org.talend.components.api.properties.presentation.Form;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * 
 * created by ycbai on 2015年9月16日 Detailled comment
 *
 */
public class GenericConnWizard extends CheckLastVersionRepositoryWizard {

    private GenericWizardPage wizPage;

    private GenericConnection connection;

    private Property connectionProperty;

    private String originalLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    private RepositoryNode repNode;

    private IGenericWizardService wizardService;

    private ComponentService compService;

    private List<ElementParameter> parameters = new ArrayList<>();

    public GenericConnWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        super(workbench, creation);
        this.existingNames = existingNames;
        repNode = node;
        wizardService = GenericWizardServiceFactory.getGenericWizardService();
        setNeedsProgressMonitor(true);
        ENodeType nodeType = node.getType();
        switch (nodeType) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }
        switch (nodeType) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            // ses the id to be used for persistence lookup
            connectionProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
            connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();

            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;
        case REPOSITORY_ELEMENT:
            RepositoryObject object = new RepositoryObject(node.getObject().getProperty());
            setRepositoryObject(object);
            connection = (GenericConnection) ((ConnectionItem) object.getProperty().getItem()).getConnection();
            connectionProperty = object.getProperty();
            connectionItem = (ConnectionItem) object.getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            isRepositoryObjectEditable();
            initLockStrategy();
            break;
        }
        if (!creation) {
            this.originalLabel = this.connectionItem.getProperty().getDisplayName();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
        }
        compService = new GenericWizardInternalService().getComponentService();
        compService.setRepository(new GenericRepository());
        IWizardContainer container = this.getContainer();
        if (container instanceof GenericWizardDialog) {
            ((GenericWizardDialog) container).setCompService(compService);
        }
        // initialize the context mode
        ConnectionContextHelper.checkContextMode(connectionItem);
        setHelpAvailable(false);
    }

    @Override
    public void addPages() {
        ERepositoryObjectType repObjType = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
        String typeName = repObjType.getType();
        setWindowTitle(typeName);
        Image wiardImage = wizardService.getWiardImage(typeName);
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(wiardImage));
        ((GenericConnectionItem) connectionItem).setTypeName(typeName);

        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        ComponentWizard componentWizard = null;
        if (creation) {
            componentWizard = internalService.getComponentWizard(typeName, connectionProperty.getId());
        } else {
            String compPropertiesStr = connection.getCompProperties();
            if (compPropertiesStr != null) {
                Deserialized fromSerialized = ComponentProperties.fromSerialized(compPropertiesStr);
                if (fromSerialized != null) {
                    componentWizard = internalService.getTopLevelComponentWizard(fromSerialized.properties, repNode.getId());
                }
            }
        }
        if (componentWizard == null) {
            return;
        }
        List<Form> forms = componentWizard.getForms();
        for (Form form : forms) {
            ComponentProperties properties = form.getProperties();
            // FIXME: Need to improve this part after.
            // When creating the wizard from component properties, component service only set the component properties
            // to the orignal form which doesn't created by it. So if you use this component properties to get forms, it
            // will return the wrong ones. Now I just fix it in studio side. Maybe it will be better to fix this problem
            // in component contract?
            Form rightForm = properties.getForm(form.getName());
            if (rightForm != form) {
                form = rightForm;
            }
            wizPage = new GenericConnWizardPage(connectionItem, isRepositoryObjectEditable(), existingNames, creation, form,
                    compService);
            if (wizPage != null) {
                wizPage.setTitle(form.getTitle());
                wizPage.setDescription(form.getSubtitle());
                if (creation) {
                    wizPage.setPageComplete(false);
                } else {
                    wizPage.setPageComplete(isRepositoryObjectEditable());
                }
            }
            addPage(wizPage);
        }
    }

    @Override
    public boolean performFinish() {
        if (wizPage.isPageComplete()) {
            IWizardPage[] pages = getPages();
            for (IWizardPage page : pages) {
                if (page instanceof GenericConnWizardPage) {
                    GenericConnWizardPage gPage = (GenericConnWizardPage) page;
                    parameters.addAll(gPage.getParameters());
                }
            }
            try {
                final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                String propertyName = getPropertyName();
                connectionProperty.setDisplayName(StringUtils.trimToNull(propertyName));
                connectionProperty.setLabel(StringUtils.trimToNull(propertyName));
                connectionProperty.setModificationDate(new Date());
                String displayName = connectionProperty.getDisplayName();
                connectionProperty.setLabel(displayName);
                this.connection.setName(displayName);
                this.connection.setLabel(displayName);
                Form form = wizPage.getForm();
                if (form.isCallAfterFormFinish()) {
                    if (creation) {
                        factory.create(connectionItem, new Path("")); //$NON-NLS-1$
                    }
                    compService.afterFormFinish(form.getName(), form.getProperties());
                }
                if (!creation) {
                    GenericUpdateManager.updateGenericConnection(connectionItem);
                }
                updateConnectionItem();
            } catch (Throwable e) {
                new ErrorDialogWidthDetailArea(getShell(), IGenericConstants.PLUGIN_ID,
                        Messages.getString("GenericConnWizard.persistenceException"), //$NON-NLS-1$
                        ExceptionUtils.getFullStackTrace(e));
                ExceptionHandler.process(e);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private String getPropertyName() throws Exception {
        for (ElementParameter parameter : parameters) {
            if (IGenericConstants.NAME_PROPERTY.equalsIgnoreCase(parameter.getName())) {
                return String.valueOf(parameter.getValue());
            }
        }
        throw new Exception("Name property is required!");
    }

    @Override
    public boolean performCancel() {
        if (!creation) {
            connectionItem.getProperty().setVersion(this.originalVersion);
            connectionItem.getProperty().setDisplayName(this.originalLabel);
            connectionItem.getProperty().setDescription(this.originalDescription);
            connectionItem.getProperty().setPurpose(this.originalPurpose);
            connectionItem.getProperty().setStatusCode(this.originalStatus);
        }
        return super.performCancel();
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     * 
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(final IWorkbench workbench, final IStructuredSelection sel) {
        super.setWorkbench(workbench);
        this.selection = sel;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

}
