package org.talend.repository.preference;

// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProjectReference;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.SVNConstant;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.services.IGITProviderService;
import org.talend.core.services.ISVNProviderService;
import org.talend.repository.ProjectManager;
import org.talend.repository.ReferenceProjectProblemManager;
import org.talend.repository.RepositoryViewPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.ui.dialog.OverTimePopupDialogTask;
import org.talend.repository.ui.views.IRepositoryView;

public class ProjectRefSettingPage extends ProjectSettingPage {

    public static final String ID = "org.talend.repository.preference.ProjectRefSettingPage";

    private static final int REPOSITORY_LOCAL = 0;

    private static final int REPOSITORY_GIT = 1;

    private static final int REPOSITORY_SVN = 2;

    private ListViewer viewer;

    private Combo projectCombo;

    private Combo branchCombo;

    private Button removeButton;

    private Button addButton;

    private Project[] projects;

    private Project lastSelectedProject;

    private ISVNProviderService svnProviderService;

    private IGITProviderService gitProviderService;

    private List<ProjectReferenceBean> viewerInput = new ArrayList<ProjectReferenceBean>();

    private boolean isModified = false;

    private boolean isReadOnly = false;

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        if (PluginChecker.isSVNProviderPluginLoaded()) {
            try {
                svnProviderService = (ISVNProviderService) GlobalServiceRegister.getDefault()
                        .getService(ISVNProviderService.class);
                gitProviderService = (IGITProviderService) GlobalServiceRegister.getDefault()
                        .getService(IGITProviderService.class);
            } catch (RuntimeException e) {
                // nothing to do
            }
        }
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        int projectRepositoryType = getProjectRepositoryType(currentProject);
        if ((REPOSITORY_GIT == projectRepositoryType || REPOSITORY_SVN == projectRepositoryType)
                && this.getRepositoryContext().isOffline()) {
            isReadOnly = true;
        }
        Composite composite = new Composite(parent, SWT.None);
        composite.setLayout(new GridLayout(2, false));
        GridData data = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(data);

        SashForm form = new SashForm(composite, SWT.HORIZONTAL);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group listGroup = new Group(form, SWT.None);
        listGroup.setLayout(new GridLayout(2, false));
        listGroup.setText(Messages.getString("ReferenceProjectSetupPage.ListGroup"));//$NON-NLS-1$

        Group addGroup = new Group(form, SWT.None);
        addGroup.setLayout(new GridLayout(3, false));
        addGroup.setText(Messages.getString("ReferenceProjectSetupPage.AddGroup"));//$NON-NLS-1$

        viewer = new ListViewer(listGroup, SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setLabelProvider(new ReferenceProjectLabelProvider());
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.horizontalSpan = 2;
        viewer.getControl().setLayoutData(viewerData);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setErrorMessage(null);
                ISelection selection = viewer.getSelection();
                removeButton.setEnabled(!selection.isEmpty() && !isReadOnly);
            }
        });

        removeButton = new Button(listGroup, SWT.None);
        removeButton.setText(Messages.getString("ReferenceProjectSetupPage.ButtonDelete")); //$NON-NLS-1$
        GridData removeButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        removeButtonData.horizontalSpan = 2;
        removeButton.setLayoutData(removeButtonData);
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                removeProjectReference();
            }
        });

        Label projectLabel = new Label(addGroup, SWT.None);
        projectLabel.setLayoutData(new GridData());
        projectLabel.setText(Messages.getString("ReferenceProjectSetupPage.LabelProject"));//$NON-NLS-1$

        projectCombo = new Combo(addGroup, SWT.BORDER);
        projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectCombo.setEnabled(false);
        projectCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                Project project = getCurrentSelectedProject();
                if (lastSelectedProject == null
                        || (project != null && !project.getTechnicalLabel().equals(lastSelectedProject.getTechnicalLabel()))) {
                    lastSelectedProject = project;
                    initBranchData();
                }
                updateAddButtonStatus();
            }
        });
        Button refreshButton = new Button(addGroup, SWT.None);
        refreshButton.setLayoutData(new GridData());
        refreshButton.setEnabled(!isReadOnly);
        refreshButton.setImage(ImageProvider.getImage(EImage.REFRESH_ICON));
        refreshButton.setToolTipText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipRefresh"));//$NON-NLS-1$
        refreshButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                initProjectData();
            }
        });

        Label branchLabel = new Label(addGroup, SWT.None);
        branchLabel.setLayoutData(new GridData());
        branchLabel.setText(Messages.getString("ReferenceProjectSetupPage.LabelBranch"));//$NON-NLS-1$

        branchCombo = new Combo(addGroup, SWT.BORDER);
        branchCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        branchCombo.setEnabled(false);
        branchCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setErrorMessage(null);
                updateAddButtonStatus();
            }
        });

        addButton = new Button(addGroup, SWT.None);
        addButton.setLayoutData(new GridData());
        addButton.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addButton.setToolTipText(Messages.getString("ReferenceProjectSetupPage.ButtonTooltipAdd")); //$NON-NLS-1$
        addButton.setEnabled(false);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addProjectReference();
            }
        });

        form.setWeights(new int[] { 1, 1 });
        this.setTitle(Messages.getString("ReferenceProjectSetupPage.Title")); //$NON-NLS-1$

        applyDialogFont(composite);
        initViewerData();
        if (isReadOnly) {
            this.setTitle(Messages.getString("ReferenceProjectSetupPage.TitleReadOnly"));
        } else {
            this.setTitle(Messages.getString("ReferenceProjectSetupPage.Title"));
        }

        return null;
    }

    @Override
    public void refresh() {
    }

    private void initViewerData() {
        Project project = ProjectManager.getInstance().getCurrentProject();
        viewerInput.addAll(getReferenceProjectData(project));
        viewer.setInput(viewerInput);
        viewer.refresh();
    }

    private List<ProjectReferenceBean> getReferenceProjectData(Project project) {
        List<ProjectReferenceBean> result = new ArrayList<ProjectReferenceBean>();
        List<ProjectReference> list = project.getProjectReferenceList();
        for (ProjectReference pr : list) {
            ProjectReferenceBean prb = new ProjectReferenceBean();
            prb.setReferenceProject(pr.getReferencedProject());
            prb.setReferenceBranch(pr.getReferencedBranch());
            result.add(prb);
        }
        return result;
    }

    private void initProjectData() {
        this.setErrorMessage(null);
        projectCombo.setEnabled(false);
        projectCombo.setItems(new String[0]);
        String errorMessage = null;

        OverTimePopupDialogTask<Project[]> overTimePopupDialogTask = new OverTimePopupDialogTask<Project[]>() {

            @Override
            public Project[] run() throws Throwable {
                return ProxyRepositoryFactory.getInstance().readProject();
            }
        };
        overTimePopupDialogTask.setNeedWaitingProgressJob(false);
        try {
            projects = overTimePopupDialogTask.runTask();
        } catch (Throwable e) {
            errorMessage = e.getLocalizedMessage();
        }
        if (projects != null && projects.length > 0) {
            Project currentProject = ProjectManager.getInstance().getCurrentProject();
            int projectRepositoryType = getProjectRepositoryType(currentProject);
            List<String> itemList = new ArrayList<String>();
            for (int i = 0; i < projects.length; i++) {
                if (currentProject.getTechnicalLabel().equals(projects[i].getTechnicalLabel())) {
                    continue;
                }
                if (projectRepositoryType == getProjectRepositoryType(projects[i])) {
                    itemList.add(projects[i].getTechnicalLabel());
                }
            }
            projectCombo.setItems(itemList.toArray(new String[0]));
        }
        projectCombo.setEnabled(true);
        this.setErrorMessage(errorMessage);
    }

    /**
     * Get project repository type
     * 
     * @param project
     */
    private int getProjectRepositoryType(Project project) {
        try {
            if (!project.isLocal()) {
                if (gitProviderService != null && gitProviderService.isGITProject(project)) {
                    return REPOSITORY_GIT;
                }
            }
            if (svnProviderService != null && svnProviderService.isSVNProject(project)) {
                return REPOSITORY_SVN;
            }
        } catch (PersistenceException ex) {
            ExceptionHandler.process(ex);
        }

        return REPOSITORY_LOCAL;
    }

    private void updateAddButtonStatus() {
        boolean isEnable = false;
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        int projectRepositoryType = getProjectRepositoryType(currentProject);
        if (REPOSITORY_LOCAL == projectRepositoryType) {
            if (StringUtils.isNotEmpty(projectCombo.getText())) {
                isEnable = true;
            }
        } else {
            if (StringUtils.isNotEmpty(projectCombo.getText()) && StringUtils.isNotEmpty(branchCombo.getText())) {
                isEnable = true;
            }
        }
        Project p = getCurrentSelectedProject();
        for (ProjectReferenceBean bean : viewerInput) {
            if (bean.getReferenceProject().getTechnicalLabel().equals(p.getTechnicalLabel())) {
                isEnable = false;
                break;
            }
        }
        addButton.setEnabled(isEnable && !isReadOnly);
    }

    private void initBranchData() {
        this.setErrorMessage(null);
        branchCombo.setEnabled(false);
        branchCombo.setItems(new String[0]);
        String errorMessage = null;
        List<String> allBranch;

        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        int projectRepositoryType = getProjectRepositoryType(currentProject);
        if (REPOSITORY_LOCAL == projectRepositoryType) {
            return;
        }
        if (projectRepositoryType == REPOSITORY_SVN && this.getRepositoryContext().isOffline()) {
            this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorCanNotGetSVNBranchData")); //$NON-NLS-1$
            return;
        }
        OverTimePopupDialogTask<List<String>> overTimePopupDialogTask = new OverTimePopupDialogTask<List<String>>() {

            @Override
            public List<String> run() throws Throwable {
                IRepositoryService repositoryService = (IRepositoryService) GlobalServiceRegister.getDefault()
                        .getService(IRepositoryService.class);
                if (repositoryService != null) {
                    return repositoryService.getProjectBranch(lastSelectedProject);
                }
                return null;
            }
        };
        overTimePopupDialogTask.setNeedWaitingProgressJob(false);
        try {
            allBranch = overTimePopupDialogTask.runTask();
            if (allBranch != null) {
                branchCombo.setItems(allBranch.toArray(new String[0]));
            }
            if (projectRepositoryType == REPOSITORY_SVN) {
                if (!allBranch.contains(SVNConstant.NAME_TRUNK)) {
                    allBranch.add(SVNConstant.NAME_TRUNK);
                }
                branchCombo.setItems(allBranch.toArray(new String[0]));
                branchCombo.setText(SVNConstant.NAME_TRUNK);
            } else if (projectRepositoryType == REPOSITORY_GIT) {
                branchCombo.setItems(allBranch.toArray(new String[0]));
                branchCombo.setText(SVNConstant.NAME_MASTER);
            }
        } catch (Throwable e) {
            errorMessage = e.getLocalizedMessage();
        }
        branchCombo.setEnabled(true);
        this.setErrorMessage(errorMessage);
    }

    private RepositoryContext getRepositoryContext() {
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY);
        return repositoryContext;
    }

    protected Project getCurrentSelectedProject() {
        String label = projectCombo.getText();
        for (Project project : projects) {
            if (label.equals(project.getTechnicalLabel())) {
                return project;
            }
        }
        return null;
    }

    private void addProjectReference() {
        this.setErrorMessage(null);
        Project p = getCurrentSelectedProject();
        if (p != null) {
            int projectRepositoryType = this.getProjectRepositoryType(p);
            String branch = "";
            if (REPOSITORY_LOCAL != projectRepositoryType) {
                branch = branchCombo.getText();
                if (branch.length() == 0) {
                    this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorBranchEmpty"));//$NON-NLS-1$
                    return;
                }
            }
            for (ProjectReferenceBean bean : viewerInput) {
                if (bean.getReferenceProject().getTechnicalLabel().equals(p.getTechnicalLabel())) {
                    this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorContainedProject"));//$NON-NLS-1$
                    return;
                }
            }
            List<ProjectReferenceBean> newViewerInput = new ArrayList<ProjectReferenceBean>();
            newViewerInput.addAll(viewerInput);
            ProjectReferenceBean referenceBean = new ProjectReferenceBean();
            referenceBean.setReferenceProject(p.getEmfProject());
            referenceBean.setReferenceBranch(branch);
            newViewerInput.add(referenceBean);
            if (!checkCycleReference(ProjectManager.getInstance().getCurrentProject(), newViewerInput)) {
                this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorCycleReference"));//$NON-NLS-1$
                return;
            }
            for (ProjectReferenceBean bean : viewerInput) {
                List<ProjectReference> referencedProjectList = getAllReferenceProject(bean.getReferenceProject());
                if (referencedProjectList != null && referencedProjectList.size() > 0) {
                    for (ProjectReference pr : referencedProjectList) {
                        if (pr.getReferencedProject().getTechnicalLabel().equals(p.getTechnicalLabel())
                                && !branch.equals(pr.getReferencedBranch())) {
                            this.setErrorMessage(Messages.getString("ReferenceProjectSetupPage.ErrorReferencedByOtherProject",
                                    getProjectDecription(p.getLabel(), pr.getReferencedBranch()), pr.getProject().getLabel()));// $NON-NLS-1$
                            return;
                        }
                    }
                }
            }
            viewerInput.add(referenceBean);
            viewer.refresh();
        }
    }

    private boolean checkCycleReference(Project project, List<ProjectReferenceBean> newViewerInput) {
        List<ProjectReference> referenceList = null;
        if (project.getTechnicalLabel().equals(ProjectManager.getInstance().getCurrentProject().getTechnicalLabel())) {
            referenceList = new ArrayList<ProjectReference>();
            for (ProjectReferenceBean bean : newViewerInput) {
                ProjectReference pr = PropertiesFactory.eINSTANCE.createProjectReference();
                pr.setReferencedBranch(bean.getReferenceBranch());
                pr.setReferencedProject(bean.getReferenceProject());
                referenceList.add(pr);
            }
        } else {
            referenceList = project.getProjectReferenceList();
        }
        if (referenceList.size() == 0) {
            return false;
        }
        Map<String, List<String>> referenceMap = new HashMap<String, List<String>>();
        List<String> list = new ArrayList<String>();
        referenceMap.put(project.getTechnicalLabel(), list);
        for (ProjectReference projetReference : referenceList) {
            list.add(projetReference.getReferencedProject().getTechnicalLabel());
            List<ProjectReference> childReferenceList = new Project(projetReference.getReferencedProject())
                    .getProjectReferenceList();
            if (childReferenceList.size() > 0) {
                List<String> childList = new ArrayList<String>();
                referenceMap.put(projetReference.getReferencedProject().getTechnicalLabel(), childList);
                for (ProjectReference pr : childReferenceList) {
                    childList.add(pr.getReferencedProject().getTechnicalLabel());
                }
            }
        }
        return ReferenceProjectProblemManager.checkCycleReference(referenceMap);
    }

    private List<ProjectReference> getAllReferenceProject(org.talend.core.model.properties.Project project) {
        List<ProjectReference> result = new ArrayList<ProjectReference>();
        List<ProjectReference> referenceList = new Project(project).getProjectReferenceList();
        if (referenceList != null && referenceList.size() > 0) {
            for (ProjectReference reference : referenceList) {
                reference.setProject(project);
                result.add(reference);
                if (new Project(reference.getReferencedProject()).getProjectReferenceList().size() > 0) {
                    result.addAll(getAllReferenceProject(reference.getReferencedProject()));
                }
            }
        }

        return result;
    }

    private void removeProjectReference() {
        ISelection selection = viewer.getSelection();
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            viewerInput.removeAll(((IStructuredSelection) selection).toList());
        }
        viewer.refresh();
    }

    protected static String getProjectDecription(String projectName, String branch) {
        StringBuffer sb = new StringBuffer();
        sb.append(projectName);
        if (branch != null && branch.trim().length() > 0) {
            sb.append("/").append(branch);
        }
        return sb.toString();
    }

    @Override
    public boolean performOk() {
        saveData();
        if (isModified) {
            relogin();
        }
        return super.performOk();
    }

    protected void performApply() {
        saveData();
    }

    private void saveData() {
        List<ProjectReferenceBean> originList = getReferenceProjectData(ProjectManager.getInstance().getCurrentProject());
        if (originList.size() != viewerInput.size()) {
            isModified = true;
        }
        if (!isModified) {
            for (ProjectReferenceBean originValue : originList) {
                boolean isFind = false;
                for (ProjectReferenceBean value : viewerInput) {
                    if (originValue.getReferenceProject().getTechnicalLabel()
                            .equals(value.getReferenceProject().getTechnicalLabel())
                            && originValue.getReferenceBranch().equals(value.getReferenceBranch())) {
                        isFind = true;
                        break;
                    }
                }
                if (!isFind) {
                    isModified = true;
                    break;
                }
            }
        }
        if (isModified) {
            List<ProjectReference> projectReferenceList = new ArrayList<ProjectReference>();
            for (ProjectReferenceBean bean : viewerInput) {
                ProjectReference pr = PropertiesFactory.eINSTANCE.createProjectReference();
                pr.setReferencedBranch(bean.getReferenceBranch());
                pr.setReferencedProject(bean.getReferenceProject());
                projectReferenceList.add(pr);
            }
            String errorMessages = null;
            try {
                ProjectManager.getInstance().getCurrentProject().saveProjectReferenceList(projectReferenceList);
            } catch (Exception e) {
                errorMessages = e.getMessage();
                this.setErrorMessage(errorMessages);
            }
            if (errorMessages != null) {
                return;
            }
        }
    }

    private void relogin() {
        MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                Messages.getString("RepoReferenceProjectSetupAction.TitleReferenceChanged"), //$NON-NLS-1$
                Messages.getString("RepoReferenceProjectSetupAction.MsgReferenceChanged")); //$NON-NLS-1$

        IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                Project currentProject = ProjectManager.getInstance().getCurrentProject();
                monitor.beginTask(Messages.getString("RepoReferenceProjectSetupAction.TaskRelogin"), 10); //$NON-NLS-1$
                monitor.subTask(Messages.getString("RepoReferenceProjectSetupAction.TaskLogoff")); //$NON-NLS-1$
                ProxyRepositoryFactory.getInstance().logOffProject();
                monitor.worked(2);
                Project[] projects;
                Project switchProject = null;
                try {
                    projects = ProxyRepositoryFactory.getInstance().readProject();
                    for (Project p : projects) {
                        if (p.getTechnicalLabel().equals(currentProject.getTechnicalLabel())) {
                            switchProject = p;
                            break;
                        }
                    }
                    monitor.subTask(Messages.getString("RepoReferenceProjectSetupAction.TaskLogon", switchProject.getLabel())); //$NON-NLS-1$
                    ProxyRepositoryFactory.getInstance().logOnProject(switchProject, monitor);
                    monitor.worked(7);
                    refreshNavigatorView();
                    monitor.worked(1);
                    monitor.done();
                } catch (Exception e) {
                    throw new CoreException(new Status(Status.ERROR, RepositoryViewPlugin.PLUGIN_ID, e.getMessage(), e));
                }
            }
        };

        IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    workspace.run(workspaceRunnable, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
        try {
            progressDialog.run(true, false, iRunnableWithProgress);
        } catch (InvocationTargetException | InterruptedException e) {
            ExceptionHandler.process(e);
        }
    }

    private void refreshNavigatorView() {
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                ProjectRepositoryNode.getInstance().cleanup();
                IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
                if (repositoryView instanceof CommonNavigator) {
                    CommonViewer commonViewer = ((CommonNavigator) repositoryView).getCommonViewer();
                    Object input = commonViewer.getInput();
                    // make sure to init the repository view rightly.
                    commonViewer.setInput(input);
                }
            }
        });
    }

    private void checkDependence() {
        List<Project> allReferencedProjectList = new ArrayList<Project>();
        for (ProjectReferenceBean bean : viewerInput) {
            org.talend.core.model.properties.Project p = bean.getReferenceProject();
            Project project = ProjectManager.getInstance().getProjectFromProjectLabel(p.getLabel());
            allReferencedProjectList.add(project);
            allReferencedProjectList.addAll(ProjectManager.getInstance().getReferencedProjects(project));
        }
    }

}

class ReferenceProjectLabelProvider implements ILabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ProjectReferenceBean) {
            ProjectReferenceBean pr = (ProjectReferenceBean) element;
            StringBuffer sb = new StringBuffer();
            sb.append(ProjectRefSettingPage.getProjectDecription(pr.getReferenceProject().getTechnicalLabel(),
                    pr.getReferenceBranch()));
            return sb.toString();
        }
        return null;
    }
}

class ProjectReferenceBean {

    private String referenceBranch;

    private org.talend.core.model.properties.Project referenceProject;

    public String getReferenceBranch() {
        return referenceBranch;
    }

    public void setReferenceBranch(String referenceBranch) {
        this.referenceBranch = referenceBranch;
    }

    public org.talend.core.model.properties.Project getReferenceProject() {
        return referenceProject;
    }

    public void setReferenceProject(org.talend.core.model.properties.Project referenceProject) {
        this.referenceProject = referenceProject;
    }
}

class CheckDependenceTask implements IRunnableWithProgress {

    private List<Project> deletingReferenceProject;

    private List<DependenceProblem> problemList = new ArrayList<DependenceProblem>();

    CheckDependenceTask(List<Project> deletingReferenceProject) {
        this.deletingReferenceProject = deletingReferenceProject;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            RelationshipItemBuilder relationshipBuilder = RelationshipItemBuilder.getInstance();
            relationshipBuilder.load();
            Map<Relation, Set<Relation>> relationMap = relationshipBuilder.getCurrentProjectItemsRelations();

            if (relationMap != null && relationMap.size() > 0) {
                int workedCount = 0;
                for (Relation relation : relationMap.keySet()) {
                    if (monitor != null && monitor.isCanceled()) {
                        return;
                    }
                    updateMonitorStatus(monitor, "", relationMap.size(), workedCount++);
                    IRepositoryViewObject viewObject = ProxyRepositoryFactory.getInstance().getLastVersion(relation.getId());
                    Set<Relation> referencedRelationSet = relationMap.get(relation);
                    if (viewObject != null && referencedRelationSet != null) {
                        for (Relation r : referencedRelationSet) {
                            if (checkRelation(r)) {
                                DependenceProblem problem = new DependenceProblem(r);
                                problem.setSourceId(viewObject.getId());
                                problem.setSourceType(viewObject.getRepositoryObjectType());
                                problem.setSourceName(viewObject.getLabel());
                                problemList.add(problem);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            throw new InvocationTargetException(e);
        }
    }

    private boolean checkRelation(Relation relation) throws PersistenceException {
        // // Check current project
        // Project currentProject = ProjectManager.getInstance().getCurrentProject();
        // if (isIncludeInProject(currentProject, relation)) {
        // return true;
        // }

        if (deletingReferenceProject != null && deletingReferenceProject.size() > 0) {
            for (Project project : deletingReferenceProject) {
                if (isIncludeInProject(project, relation)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isIncludeInProject(Project project, Relation relation) throws PersistenceException {
        IRepositoryViewObject viewObject = ProxyRepositoryFactory.getInstance().getLastVersion(project, relation.getId());
        return viewObject == null ? false : true;
    }

    private void updateMonitorStatus(IProgressMonitor monitor, String message, int totalCount, int workedCount) {
        if (monitor != null) {
            if (totalCount > 0) {
                monitor.beginTask(message, totalCount);
            } else {
                monitor.subTask(message);
                monitor.worked(workedCount);
            }
        }
    }
}

class DependenceProblem {

    private Relation relation;

    private String sourceId;

    private String sourceName;

    private ERepositoryObjectType sourceType;

    private String referencedProject;

    DependenceProblem(Relation relation) {
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public ERepositoryObjectType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ERepositoryObjectType sourceType) {
        this.sourceType = sourceType;
    }

    public String getReferencedProject() {
        return referencedProject;
    }

    public void setReferencedProject(String referencedProject) {
        this.referencedProject = referencedProject;
    }
}
