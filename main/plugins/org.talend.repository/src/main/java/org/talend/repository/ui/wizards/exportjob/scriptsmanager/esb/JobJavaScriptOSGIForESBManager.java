// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.utils.io.FileCopyUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.repository.build.BuildExportManager;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.maven.launch.MavenPomCommandLauncher;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JarBuilder;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager;
import org.talend.repository.utils.EmfModelUtils;
import org.talend.repository.utils.TemplateProcessor;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.FileResource;
import aQute.bnd.osgi.Jar;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class JobJavaScriptOSGIForESBManager extends JobJavaScriptsManager {

    protected static final char MANIFEST_ITEM_SEPARATOR = ',';

    @SuppressWarnings("serial")
    private static final Collection<String> EXCLUDED_MODULES = new ArrayList<String>() {
        {
            try (InputStream is = RepositoryPlugin.getDefault().getBundle()
                    .getEntry("/resources/osgi-exclude.properties").openStream()) { //$NON-NLS-1$
                final Properties p = new Properties();
                p.load(is);
                for (Enumeration<?> e = p.propertyNames(); e.hasMoreElements();) {
                    add((String) e.nextElement());
                }
            } catch (IOException e) {
                RepositoryPlugin.getDefault().getLog()
                        .log(new Status(Status.ERROR, RepositoryPlugin.PLUGIN_ID, "Unable to load OSGi excludes", e));
            }
        }
    };

    public JobJavaScriptOSGIForESBManager(Map<ExportChoice, Object> exportChoiceMap, String contextName, String launcher,
            int statisticPort, int tracePort) {
        super(exportChoiceMap, contextName, launcher, statisticPort, tracePort);
    }

    protected static final char PACKAGE_SEPARATOR = '.';

    private static final String JAVA = "java"; //$NON-NLS-1$

    private MultiKeyMap requireBundleModules = new MultiKeyMap();

//    private String itemType = null;

    private final File classesLocation = new File(getTmpFolder() + File.separator + "classes"); //$NON-NLS-1$;

    private static final String DLL_FILE = ".dll"; //$NON-NLS-1$

    private static final String SO_FILE = ".so"; //$NON-NLS-1$

    private static final String OSGI_OS_CODE = ';' + "osname=" + System.getProperty("osgi.os") + ';' + "processor=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + System.getProperty("osgi.arch") + ','; //$NON-NLS-1$

    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] processes, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> list = new ArrayList<ExportFileResource>();

        ExportFileResource osgiResource = new ExportFileResource(null, ""); //$NON-NLS-1$;
        ExportFileResource jobScriptResource = new ExportFileResource(null, ""); //$NON-NLS-1$

        list.add(osgiResource);
        list.add(jobScriptResource);

        // set export config mode now only to be sure that the libraries will be
        // setup for an export mode, and not
        // editor mode.
        ProcessorUtilities.setExportConfig(JAVA, "", ""); //$NON-NLS-1$
        // set export type as osgi
        ProcessorUtilities.setExportAsOSGI(true);

        try {
            ProcessItem processItem = null;
            for (ExportFileResource process : processes) {
                processItem = (ProcessItem) process.getItem();
                if (processItem.eIsProxy() || processItem.getProcess().eIsProxy()) {
                    try {
                        Property property = ProxyRepositoryFactory.getInstance().getUptodateProperty(processItem.getProperty());
                        processItem = (ProcessItem) property.getItem();
                    } catch (PersistenceException e) {
                        throw new ProcessorException(e);
                    }
                }

                String jobVersion = processItem.getProperty().getVersion();
                if (!isMultiNodes() && getSelectedJobVersion() != null) {
                    jobVersion = getSelectedJobVersion();
                }

                ProcessorUtilities.setExportConfig(process.getDirectoryName(), true);

                String processId = processItem.getProperty().getId();

                if (null == contextName) {
                    contextName = processItem.getProcess().getDefaultContext();
                }
                IProcess iProcess = generateJobFiles(processItem, contextName, jobVersion,
                        statisticPort != IProcessor.NO_STATISTICS, tracePort != IProcessor.NO_TRACES,
                        isOptionChoosed(ExportChoice.applyToChildren), progressMonitor);
                analysisModules(processId, jobVersion);

                analysisMavenModule(processItem);

                // generate jar file for job
                getJobScriptsUncompressed(jobScriptResource, processItem);

                // dynamic DB XML mapping
                addXmlMapping(process, isOptionChoosed(ExportChoice.needSourceCode));

                generateConfig(osgiResource, processItem, iProcess);

                addResources(osgiResource, processItem);

                /*
                 * export current item's dependencies. this used for TDM components specially and need more discussion
                 * about then
                 */
                BuildExportManager.getInstance().exportOSGIDependencies(osgiResource, processItem);
            }

            ExportFileResource libResource = getCompiledLibExportFileResource(processes);
            list.add(libResource);

            // generate the META-INFO folder
            ExportFileResource metaInfoFolder = genMetaInfoFolder(libResource, processItem);
            list.add(0, metaInfoFolder);

            ExportFileResource providedLibResources = getProvidedLibExportFileResource(processes);
            if (providedLibResources != null) {
                list.add(providedLibResources);
            }
        } catch (ProcessorException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessorException(e);
        }

        return list;
    }

    @Override
    protected void analysisModules(String processId, String processVersion) {
        Set<ModuleNeeded> neededModules = LastGenerationInfo.getInstance().getModulesNeededWithSubjobPerJob(processId,
                processVersion);
        for (ModuleNeeded module : neededModules) {
            if (isCompiledLib(module)) {
                addModuleNeededsInMap(getCompiledModules(), processId, processVersion, module);
            } else if (isRequireBundleLib(module)) {
                addModuleNeededsInMap(getRequireBundleModules(), processId, processVersion, module);
            } else {
                addModuleNeededsInMap(getExcludedModules(), processId, processVersion, module);
            }
        }
    }

    @Override
    protected ExportFileResource getCompiledLibExportFileResource(ExportFileResource[] processes) {
        ExportFileResource libResource = new ExportFileResource(null, LIBRARY_FOLDER_NAME);
        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(true, processes, getCompiledModuleNames());
        if (talendLibraries != null) {
            libResource.addResources(talendLibraries);
        }
        addRoutinesResources(processes, libResource);
        return libResource;
    }

    protected String getIncludeRoutinesPath() {
        return USER_ROUTINES_PATH;
    }

    protected Collection<String> getRoutinesPaths() {
        return Collections.singletonList(getIncludeRoutinesPath());
    }

    @Override
    protected void addRoutinesResources(ExportFileResource[] processes, ExportFileResource libResource) {
        File jarFile = new File(getTmpFolder() + File.separatorChar + JavaUtils.ROUTINES_JAR);

        // make a jar file of system routine classes
        File classRootFileLocation = getCodeClassRootFileLocation(ERepositoryObjectType.ROUTINES);
        if (classRootFileLocation == null) {
            return;
        }
        if (ERepositoryObjectType.valueOf("BEANS") != null) {
            FileCopyUtils.copyFolder(getCodeClassRootFileLocation(ERepositoryObjectType.valueOf("BEANS")), classRootFileLocation);
        }
        try {
            JarBuilder jarbuilder = new JarBuilder(classRootFileLocation, jarFile);
            jarbuilder.setIncludeDir(getRoutinesPaths());
            Collection<File> includeRoutines = new ArrayList<File>(getRoutineDependince(processes, true, USER_ROUTINES_PATH));
            includeRoutines.addAll(getRoutineDependince(processes, false, getIncludeRoutinesPath()));
            jarbuilder.setIncludeRoutines(includeRoutines);
            jarbuilder.buildJar();

            libResource.addResources(Collections.singletonList(jarFile.toURI().toURL()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected ExportFileResource getProvidedLibExportFileResource(ExportFileResource[] processes) {
        return null; // default, no provided lib for osgi bundle
    }

    /**
     *
     * This should be same as @see isIncludedLib. But, there are some special jar to exclude temp.
     */
    @Override
    protected boolean isCompiledLib(ModuleNeeded module) {
        /*
         * If null, will add the lib always.
         * If empty, nothing will be added.
         * Else, add the bundle id in "Require-Bundle", but don't add the lib.
         */
        return module != null && isIncludedLib(module);
    }

    /**
     * If null, will add the lib always. @see isIncludedLib
     * If empty, nothing will be added. @see isExcludedLib
     * Else, add the bundle id in "Require-Bundle", but don't add the lib. @see isIncludedInRequireBundle
     */
    protected boolean isIncludedLib(ModuleNeeded module) {
        return module != null && module.getBundleName() == null && !isExcluded(module.getModuleName());
    }

    private static boolean isExcluded(String moduleName) {
        for (String exclude : EXCLUDED_MODULES) {
            if (moduleName.startsWith(exclude)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isProvidedLib(ModuleNeeded module) {
        return isRequireBundleLib(module) || isExcludedLib(module);
    }

    /**
     * nothing will be added.
     */
    @Override
    protected boolean isExcludedLib(ModuleNeeded module) {
        if (module != null) {
            if (module.getBundleName() != null && "".equals(module.getBundleName().trim())) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    /**
     * check for the lib for "Require-Bundle"
     */
    protected boolean isRequireBundleLib(ModuleNeeded module) {
        if (module != null) {
            if (module.getBundleName() != null && !"".equals(module.getBundleName().trim())) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    /**
     * Get all route resource needed.
     *
     * @param osgiResource
     * @param processItem
     * @throws MalformedURLException
     */
    protected void addResources(ExportFileResource osgiResource, ProcessItem processItem) throws Exception {
    }

    /**
     * DOC ycbai Comment method "getJobScriptsUncompressed".
     *
     * @param resource
     * @param process
     * @throws IOException
     */
    private void getJobScriptsUncompressed(ExportFileResource resource, ProcessItem process) throws IOException {
        final URI classRootURI = classesLocation.toURI();
        List<String> jobFolderNames = getRelatedJobFolderNames(process);
        try {
            for (String jobFolderName : jobFolderNames) {
                String[] jf = jobFolderName.split(":"); //$NON-NLS-1$
                String projectName = jf[0];
                String folderName = jf[1];
                String classRootLocation = getJobClassRootLocation(process.getProperty()) + projectName + File.separator;
                String classRoot = FilesUtils.getFileRealPath(classRootLocation + folderName);
                String targetPath = FilesUtils.getFileRealPath(classesLocation + File.separator + projectName + File.separator
                        + folderName);
                File sourceFile = new File(classRoot);
                File targetFile = new File(targetPath);
                FilesUtils.copyFolder(sourceFile, targetFile, true, null, null, true, false);

                List<URL> fileURLs = FilesUtils.getFileURLs(targetFile);
                for (URL url : fileURLs) {
                    resource.addResource(classRootURI.relativize(new File(url.toURI()).getParentFile().toURI()).toString(), url);
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private static boolean isTalendESBJob(ProcessItem processItem) {
        return null != EmfModelUtils.getComponentByName(processItem, "tESBProviderRequest", "tESBConsumer", "tRouteInput");
    }

    private static boolean isTalendESBJobFactory(ProcessItem processItem) {
        return null != EmfModelUtils.getComponentByName(processItem, "tESBProviderRequest", "tRouteInput");
    }

    private static boolean isTalendStepTemplate(ProcessItem processItem) {
        return null != EmfModelUtils.getComponentByName(processItem, "tActionInput", "tActionOutput", "tActionReject");
    }

    private static NodeType getRESTRequestComponent(ProcessItem processItem) {
        return EmfModelUtils.getComponentByName(processItem, "tRESTRequest");
    }

    protected static String getPackageName(ProcessItem processItem) {
        return JavaResourcesHelper.getProjectFolderName(processItem)
                + PACKAGE_SEPARATOR
                + JavaResourcesHelper.getJobFolderName(processItem.getProperty().getLabel(), processItem.getProperty()
                        .getVersion());
    }

    private Map<String, Object> collectJobInfo(ProcessItem processItem, IProcess process) {
        // velocity template context
        Map<String, Object> jobInfo = new HashMap<String, Object>();

        String name = processItem.getProperty().getLabel();
        String className = getPackageName(processItem) + PACKAGE_SEPARATOR + name;

        boolean isTalendStepTemplate = isTalendStepTemplate(processItem);
        // job name and class name
        String jobName = name;
        if (!EmfModelUtils.getComponentsByName(processItem, "tRouteInput").isEmpty()) { //$NON-NLS-1$
            jobName = className;
        }
        jobInfo.put("name", jobName);
        jobInfo.put("version", getBundleVersion()); //$NON-NLS-1$
        jobInfo.put("className", className); //$NON-NLS-1$

        // additional Talend job interfaces (ESB related)
        boolean isESBJob = isTalendESBJob(processItem);
        jobInfo.put("isESBJob", isESBJob); //$NON-NLS-1$
        jobInfo.put("isESBJobFactory", isESBJob && isTalendESBJobFactory(processItem)); //$NON-NLS-1$
        jobInfo.put("isTalendStepTemplate", isTalendStepTemplate); //$NON-NLS-1$

        // job components use SAM / use SAML
        boolean useSAM = false;
        for (NodeType node : EmfModelUtils.getComponentsByName(processItem, "tRESTClient")) { //$NON-NLS-1$
            if (!useSAM && EmfModelUtils.computeCheckElementValue("SERVICE_ACTIVITY_MONITOR", node)) { //$NON-NLS-1$
                useSAM = true;
                break;
            }
        }
        jobInfo.put("useSAM", useSAM); //$NON-NLS-1$

        // job OSGi DataSources
        jobInfo.put("dataSources", DataSourceConfig.getAliases(process)); //$NON-NLS-1$

        jobInfo.put("hasDestroyMethod", ERepositoryObjectType.PROCESS_MR != ERepositoryObjectType.getItemType(processItem));

        return jobInfo;
    }

    private static Map<String, Object> collectRestEndpointInfo(ProcessItem processItem) {
        Map<String, Object> endpointInfo = new HashMap<String, Object>();

        NodeType restRequestComponent = getRESTRequestComponent(processItem);

        if (null == restRequestComponent) {
            return endpointInfo;
        }

        // REST endpoint address
        final String runtimeServicesContext = "/services"; //$NON-NLS-1$
        final String runtimeServicesContextFull = runtimeServicesContext + '/';
        String endpointUri = EmfModelUtils.computeTextElementValue("REST_ENDPOINT", restRequestComponent); //$NON-NLS-1$
        if (!endpointUri.isEmpty() && !endpointUri.contains("://") && !endpointUri.startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
            endpointUri = '/' + endpointUri;
        }

        endpointInfo.put("originalAddress", endpointUri); //$NON-NLS-1$  Needed by Swagger

        String endpointDescription = "";
        if (EmfModelUtils.computeCheckElementValue("INCLUDE_DOC_INTO_SWAGGER_SPEC", restRequestComponent)) {
            endpointDescription = EmfModelUtils.computeTextElementValue("COMMENT", restRequestComponent);
            if (endpointDescription == null) {
                endpointDescription = "";
            }
            if (endpointDescription.contains("\r\n")) {
                endpointDescription = endpointDescription.replace("\r\n", "&#10;");
            } else {
                endpointDescription = endpointDescription.replace("\n", "&#10;");
            }
        }

        endpointInfo.put("description", endpointDescription); //$NON-NLS-1$  Needed by Swagger


        // TESB-5916: Rest service can't be deployed in the Runtime on the port said in the studio
        // if (endpointUri.contains("://")) {
        // endpointUri = new URL(endpointUri).getPath();
        // }
        if (runtimeServicesContextFull.equals(endpointUri) || runtimeServicesContext.equals(endpointUri)) {
            // pass as is
        } else if (endpointUri.startsWith(runtimeServicesContextFull)) {
            // remove forwarding "/services/" context as required by runtime (but leave forwarding slash)
            endpointUri = endpointUri.substring(runtimeServicesContextFull.length() - 1);
        }
        endpointInfo.put("address", endpointUri); //$NON-NLS-1$

        // log messages
        endpointInfo.put("logMessages", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("LOG_MESSAGES", restRequestComponent)); //$NON-NLS-1$

        // wrap JSON request
        endpointInfo.put("wrapJsonRequest", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("WRAP_JSON_REQUEST", restRequestComponent)); //$NON-NLS-1$

        // unwrap JSON response (drop root element)
        endpointInfo.put("unwrapJsonResponse", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("UNWRAP_JSON_RESPONSE", restRequestComponent)); //$NON-NLS-1$

        // Convert JSON to string (big double values)
        endpointInfo.put("convertTypesToStrings", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("CONVERT_JSON_VALUES_TO_STRING", restRequestComponent)); //$NON-NLS-1$

        // use Authentication & authentication type
        endpointInfo.put("useAuthentication", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("NEED_AUTH", restRequestComponent)); //$NON-NLS-1$
        endpointInfo.put("authenticationType", //$NON-NLS-1$
                EmfModelUtils.computeTextElementValue("AUTH_TYPE", restRequestComponent)); //$NON-NLS-1$

        // use Service Activity Monitoring
        endpointInfo.put("useSAM", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("SERVICE_ACTIVITY_MONITOR", restRequestComponent)); //$NON-NLS-1$

        // use Service Locator
        endpointInfo.put("useSL", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("SERVICE_LOCATOR", restRequestComponent)); //$NON-NLS-1$

        // use Authorization
        if (isStudioEEVersion()) {
            if (EmfModelUtils.computeCheckElementValue("NEED_AUTH", restRequestComponent)) {
                endpointInfo.put("useAuthorization", //$NON-NLS-1$
                        EmfModelUtils.computeCheckElementValue("NEED_AUTHORIZATION", restRequestComponent)); //$NON-NLS-1$
            }
        } else {
            endpointInfo.put("useAuthorization", false); //$NON-NLS-1$
        }


        // expose Swagger specification
        endpointInfo.put("exposeSwaggerSpecification", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("EXPOSE_SWAGGER_SPEC", restRequestComponent)); //$NON-NLS-1$


        // Service Locator custom properties
        Map<String, String> slCustomProperties = new HashMap<String, String>();
        ElementParameterType customPropsType = EmfModelUtils.findElementParameterByName(
                "SERVICE_LOCATOR_CUSTOM_PROPERTIES", restRequestComponent); //$NON-NLS-1$
        if (null != customPropsType) {
            List<?> elementValues = customPropsType.getElementValue();
            final int size = elementValues.size();
            for (int i = 0; i < size; i += 2) {
                if (size <= i + 1) {
                    break;
                }
                ElementValueType name = (ElementValueType) elementValues.get(i);
                ElementValueType value = (ElementValueType) elementValues.get(i + 1);
                if (null != name && null != value) {
                    if (null != name.getValue() && null != value.getValue()) {
                        slCustomProperties.put(unquote(name.getValue()), unquote(value.getValue()));
                    }
                }
            }
        }

        Map<String, String> samCustomProperties = new HashMap<String, String>();
        customPropsType = EmfModelUtils.findElementParameterByName("SERVICE_ACTIVITY_MONITOR_CUSTOM_PROPERTIES", //$NON-NLS-1$
                restRequestComponent);
        if (null != customPropsType) {
            List<?> elementValues = customPropsType.getElementValue();
            final int size = elementValues.size();
            for (int i = 0; i < size; i += 2) {
                if (size <= i + 1) {
                    break;
                }
                ElementValueType name = (ElementValueType) elementValues.get(i);
                ElementValueType value = (ElementValueType) elementValues.get(i + 1);
                if (null != name && null != value) {
                    if (null != name.getValue() && null != value.getValue()) {
                        samCustomProperties.put(unquote(name.getValue()), unquote(value.getValue()));
                    }
                }
            }
        }

        endpointInfo.put("slCustomProps", slCustomProperties); //$NON-NLS-1$
        endpointInfo.put("samCustomProps", samCustomProperties); //$NON-NLS-1$

        // service name & namespace
        endpointInfo.put("serviceName", //$NON-NLS-1$
                EmfModelUtils.computeTextElementValue("SERVICE_NAME", restRequestComponent)); //$NON-NLS-1$
        endpointInfo.put("serviceNamespace", //$NON-NLS-1$
                EmfModelUtils.computeTextElementValue("SERVICE_NAMESPACE", restRequestComponent)); //$NON-NLS-1$

        // correlation id
        endpointInfo.put("useBusinesCorrelation", //$NON-NLS-1$
                EmfModelUtils.computeCheckElementValue("USE_BUSINESS_CORRELATION", restRequestComponent)); //$NON-NLS-1$

        return endpointInfo;
    }

    /**
     *
     * Ensure that the value is not surrounded by quotes.
     *
     * @param value
     * @return
     */
    private static final String unquote(String value) {
        String result = value;
        if (null != value && 1 < value.length()) {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                result = value.substring(1, value.length() - 1);
            }
        }
        return result;
    }

    protected void generateConfig(ExportFileResource osgiResource, ProcessItem processItem, IProcess process) throws IOException {
        final File targetFile = new File(getTmpFolder() + PATH_SEPARATOR + "job.xml"); //$NON-NLS-1$
        // restJob
        if (null != getRESTRequestComponent(processItem)) {
            generateRestJobBlueprintConfig(processItem, process, targetFile);
        } else {
            createJobBundleBlueprintConfig(processItem, process, targetFile);
        }
        osgiResource.addResource(FileConstants.BLUEPRINT_FOLDER_NAME, targetFile.toURI().toURL());
    }

    private static final String TEMPLATE_BLUEPRINT_JOB_REST = "/resources/job-rest-template.xml"; //$NON-NLS-1$

    private void generateRestJobBlueprintConfig(ProcessItem processItem, IProcess process, File targetFile) throws IOException {
        // velocity template context
        Map<String, Object> contextParams = new HashMap<String, Object>();
        contextParams.put("endpoint", collectRestEndpointInfo(processItem)); //$NON-NLS-1$
        contextParams.put("job", collectJobInfo(processItem, process)); //$NON-NLS-1$

        TemplateProcessor.processTemplate("REST_JOB_BLUEPRINT_CONFIG", //$NON-NLS-1$
                contextParams, targetFile, JobJavaScriptOSGIForESBManager.class.getResourceAsStream(TEMPLATE_BLUEPRINT_JOB_REST));
    }

    private static final String TEMPLATE_BLUEPRINT_JOB = "/resources/job-template.xml"; //$NON-NLS-1$

    private void createJobBundleBlueprintConfig(ProcessItem processItem, IProcess process, File targetFile) throws IOException {
        TemplateProcessor.processTemplate("JOB_BLUEPRINT_CONFIG", //$NON-NLS-1$
                collectJobInfo(processItem, process), targetFile, JobJavaScriptOSGIForESBManager.class.getClassLoader()
                        .getResourceAsStream(TEMPLATE_BLUEPRINT_JOB));
    }

    private ExportFileResource genMetaInfoFolder(ExportFileResource libResource, ProcessItem processItem) throws IOException {

        ExportFileResource metaInfoResource = new ExportFileResource(null, FileConstants.META_INF_FOLDER_NAME);

        // generate the MANIFEST.MF file in the temp folder
        File manifestFile = new File(getTmpFolder() + PATH_SEPARATOR + FileConstants.MANIFEST_MF_FILE_NAME);

        FileOutputStream fos = null;
        try {
            Manifest manifest = getManifest(libResource, processItem);
            fos = new FileOutputStream(manifestFile);
            manifest.write(fos);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        metaInfoResource.addResources(Collections.singletonList(manifestFile.toURI().toURL()));

        return metaInfoResource;
    }

    private Manifest getManifest(ExportFileResource libResource, ProcessItem processItem) throws IOException {

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService service = (IRunProcessService) GlobalServiceRegister.getDefault()
                    .getService(IRunProcessService.class);
            ITalendProcessJavaProject talendProcessJavaProject = service.getTalendJobJavaProject(processItem.getProperty());
            if (talendProcessJavaProject != null) {
                MavenPomCommandLauncher mavenLauncher = new MavenPomCommandLauncher(talendProcessJavaProject.getProjectPom(),
                        TalendMavenConstants.GOAL_COMPILE);
                mavenLauncher.setSkipCIBuilder(true);
                mavenLauncher.setSkipTests(true);
                try {
                    mavenLauncher.execute(new NullProgressMonitor());
                    FileCopyUtils.copyFolder(talendProcessJavaProject.getOutputFolder().getLocation().toFile(), classesLocation);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        Analyzer analyzer = createAnalyzer(libResource, processItem);
        // Calculate the manifest
        Manifest manifest = null;
        try {
            manifest = analyzer.calcManifest();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            analyzer.close();
        }
        return manifest;
    }

    protected Analyzer createAnalyzer(ExportFileResource libResource, ProcessItem processItem) throws IOException {
        Analyzer analyzer = new Analyzer();
        Jar bin = new Jar(classesLocation);
        analyzer.setJar(bin);

        final String bundleName = processItem.getProperty().getLabel();
        String symbolicName = bundleName;
        // http://jira.talendforge.org/browse/TESB-5382 LiXiaopeng
        Project project = ProjectManager.getInstance().getCurrentProject();
        if (project != null) {
            String proName = project.getLabel();
            if (proName != null) {
                symbolicName = proName.toLowerCase() + '.' + symbolicName;
            }
        }
        analyzer.setProperty(Analyzer.BUNDLE_NAME, bundleName);
        analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, symbolicName);
        analyzer.setProperty(Analyzer.BUNDLE_VERSION, getBundleVersion());
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        analyzer.setProperty(Analyzer.BUNDLE_VENDOR, brandingService.getFullProductName() + " (" //$NON-NLS-1$
                + brandingService.getAcronym() + '_' + RepositoryPlugin.getDefault().getBundle().getVersion().toString() + ")"); //$NON-NLS-1$

        addOsgiDependencies(analyzer, libResource, processItem);

        final StringBuilder bundleClasspath = new StringBuilder("."); //$NON-NLS-1$
        final StringBuilder bundleNativeCode = new StringBuilder();
        Set<String> relativePathList = libResource.getRelativePathList();
        for (String path : relativePathList) {
            Set<URL> resources = libResource.getResourcesByRelativePath(path);
            for (URL url : resources) {
                // TESB-21804:Fail to deploy cMessagingEndpoint with quartz component in runtime for ClassCastException
                if (url.getPath().matches("(.*)camel-(.*)-alldep-(.*)$")) {
                    continue;
                }
                File dependencyFile = new File(FilesUtils.getFileRealPath(url.getPath()));
                String relativePath = libResource.getDirectoryName() + PATH_SEPARATOR + dependencyFile.getName();
                bundleClasspath.append(MANIFEST_ITEM_SEPARATOR).append(relativePath);
                bin.putResource(relativePath, new FileResource(dependencyFile));
                // analyzer.addClasspath(new File(url.getPath()));
                // Add dynamic library declaration in manifest
                if (relativePath.toLowerCase().endsWith(DLL_FILE) || relativePath.toLowerCase().endsWith(SO_FILE)) {
                    bundleNativeCode.append(libResource.getDirectoryName() + PATH_SEPARATOR + dependencyFile.getName()).append(
                            OSGI_OS_CODE);
                }
            }
        }
        analyzer.setProperty(Analyzer.BUNDLE_CLASSPATH, bundleClasspath.toString());

        // TESB-15680: Add Bundle-NativeCode in manifest
        if (bundleNativeCode.length() > 0) {
            bundleNativeCode.setLength(bundleNativeCode.length() - 1);
            analyzer.setProperty(Analyzer.BUNDLE_NATIVECODE, bundleNativeCode.toString());
        }

        return analyzer;
    }

    protected void addOsgiDependencies(Analyzer analyzer, ExportFileResource libResource, ProcessItem processItem)
            throws IOException {
        analyzer.setProperty(Analyzer.EXPORT_SERVICE, "routines.system.api.TalendJob;name=" + processItem.getProperty().getLabel() + ";type=" + "job");
        analyzer.setProperty(Analyzer.EXPORT_PACKAGE, getPackageName(processItem));

        final Collection<String> importPackages = new HashSet<String>();
        String requireBundle = null;

        NodeType restRequestComponent = getRESTRequestComponent(processItem);
        if (null != restRequestComponent) {
            importPackages.add("org.apache.cxf.metrics");
            if (EmfModelUtils.computeCheckElementValue("EXPOSE_SWAGGER_SPEC", restRequestComponent)) {
                importPackages.add("org.apache.cxf.jaxrs.swagger");
            }


            if (EmfModelUtils.computeCheckElementValue("NEED_AUTH", restRequestComponent)) { //$NON-NLS-1$
                String authType = EmfModelUtils.computeTextElementValue("AUTH_TYPE", restRequestComponent); //$NON-NLS-1$
                if ("BASIC".equals(authType)) { //$NON-NLS-1$
                    importPackages.add("org.apache.cxf.jaxrs.security"); //$NON-NLS-1$
                } else if ("SAML".equals(authType)) { //$NON-NLS-1$
                    importPackages.add("org.apache.cxf.interceptor.security"); //$NON-NLS-1$
                    importPackages.add("org.apache.cxf.rs.security.saml"); //$NON-NLS-1$
                    if (EmfModelUtils.computeCheckElementValue("NEED_AUTHORIZATION", restRequestComponent)) { //$NON-NLS-1$
                        importPackages.add("org.talend.esb.authorization.xacml.rt.pep"); //$NON-NLS-1$
                    }
                }
            }
        }
        for (NodeType node : EmfModelUtils.getComponentsByName(processItem, "tESBConsumer")) { //$NON-NLS-1$
            // https://jira.talendforge.org/browse/TESB-9574
            if (requireBundle == null && EmfModelUtils.computeCheckElementValue("USE_SR", node)) { //$NON-NLS-1$
                requireBundle = "tesb-xacml-rt"; //$NON-NLS-1$
            }
        }
        if (ERepositoryObjectType.PROCESS_MR == ERepositoryObjectType.getItemType(processItem)) {
            importPackages.add("org.talend.cloud"); //$NON-NLS-1$
        }


        if (requireBundle != null && !requireBundle.isEmpty()) {
            requireBundle += (", org.ops4j.pax.logging.pax-logging-api");
        } else {
            requireBundle = "org.ops4j.pax.logging.pax-logging-api";
        }

        if (requireBundle != null) {
            analyzer.setProperty(Analyzer.REQUIRE_BUNDLE, requireBundle);
        }

        StringBuilder importPackage = new StringBuilder("routines.system.api,"); //$NON-NLS-1$
        for (String ip : importPackages) {
            importPackage.append(ip).append(',');
        }
        importPackage.append("*;resolution:=optional"); //$NON-NLS-1$
        analyzer.setProperty(Analyzer.IMPORT_PACKAGE, importPackage.toString());
    }

    protected static boolean isStudioEEVersion() {
        return PluginChecker.isTIS();
    }

    @Override
    protected List<URL> getExternalLibraries(boolean needLibraries, ExportFileResource[] process, Set<String> neededLibraries) {
        if (!needLibraries) {
            return Collections.emptyList();
        }
        // Lists all the needed jar files
        final Set<String> listModulesReallyNeeded = new HashSet<String>(neededLibraries);

        // jar from routines
        for (IRepositoryViewObject object : collectRoutines(process, getIncludeRoutinesPath())) {
            Item item = object.getProperty().getItem();
            if (item instanceof RoutineItem) {
                @SuppressWarnings("unchecked")
                List<IMPORTType> imports = ((RoutineItem) item).getImports();
                for (IMPORTType type : imports) {
                    if (type.isREQUIRED()) {
                        listModulesReallyNeeded.add(type.getMODULE());
                    }
                }
            }
        }

        return getNeededModuleURLs(listModulesReallyNeeded);
    }

    protected List<URL> getNeededModuleURLs(Set<String> neededModules) {
        List<URL> list = new ArrayList<URL>();
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            IFolder libFolder = processService.getJavaProjectLibFolder();
            File file = libFolder.getLocation().toFile();
            File[] files = file.listFiles(FilesUtils.getAcceptModuleFilesFilter());
            for (File tempFile : files) {
                try {
                    if (neededModules.contains(tempFile.getName())) {
                        list.add(tempFile.toURI().toURL());
                    }
                } catch (MalformedURLException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        return list;
    }

    @Override
    public void setTopFolder(List<ExportFileResource> resourcesToExport) {
        return;
    }

    @Override
    public String getOutputSuffix() {
        return FileConstants.JAR_FILE_SUFFIX;
    }

    /**
     * Getter for requireBundleModules.
     *
     * @return the requireBundleModules
     */
    protected MultiKeyMap getRequireBundleModules() {
        return this.requireBundleModules;
    }

    protected Set<ModuleNeeded> getRequireBundleModuleNeededs() {
        return getModuleNeededs(getRequireBundleModules());
    }

    @Override
    public Set<ModuleNeeded> getExcludedModuleNeededs() {
        Set<ModuleNeeded> excludedModuleNeededs = super.getExcludedModuleNeededs();

        excludedModuleNeededs.addAll(getRequireBundleModuleNeededs());
        return excludedModuleNeededs;
    }

    @Override
    protected Set<String> getExcludedModuleNames() {
        Set<String> providedModulesSet = super.getExcludedModuleNames();

        for (ModuleNeeded module : getRequireBundleModuleNeededs()) {
            providedModulesSet.add(module.getModuleName());
        }
        return providedModulesSet;
    }
    
    @Override
    protected IProcess generateJobFiles(ProcessItem process, String contextName, String version, boolean statistics,
            boolean trace, boolean applyContextToChildren, IProgressMonitor monitor) throws ProcessorException {
        IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
        IProcess currentProcess = service.getProcessFromProcessItem(process);
        IProcessor processor = ProcessorUtilities.getProcessor(currentProcess, process.getProperty());
        return processor.getProcess();
    }
}
