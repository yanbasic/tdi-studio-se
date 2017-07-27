// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * In tGoogleDriveGet component, add the QUERY_TYPE property and rename FILE_NAME to QUERY_CRITERIA.
 * 
 * @author ypiel
 */
public class AddGoogleDriveQueryType extends AbstractJobMigrationTask {

    /**
     * The component to update.
     */
    private final static String COMPONENT_NAME = "tGoogleDriveGet";

    /**
     * Add the new property QUERY_TYPE
     */
    private final static String QUERY_TYPE_PROPERTY_NAME = "QUERY_TYPE";

    private final static String QUERY_TYPE_PROPERTY_TYPE = "CLOSED_LIST";

    private final static String QUERY_TYPE_PROPERTY_DEFAULT = "NAME";

    /**
     * Rename the property FILE_NAME to QUERY_CRITERIA
     */
    private final static String QUERY_CRITERIA_PROPERTY_NAME = "";

    private final static String QUERY_CRITERIA_PROPERTY_TYPE = "TEXT";

    private final static String FILE_NAME_PROPERTY_NAME = "FILE_NAME";

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 07, 2, 12, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);

        if (processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        IComponentFilter filter = new NameComponentFilter(COMPONENT_NAME);

        try {

            ModifyComponentsAction.searchAndModify(item, processType, filter,
                    Arrays.<IComponentConversion> asList(new AddQueryType(), new RenameFileNameToCriteria()));

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /**
     * Add QUERY_TYPE property.
     */
    private static class AddQueryType implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            ElementParameterType queryType = ComponentUtilities.getNodeProperty(node, QUERY_TYPE_PROPERTY_NAME);

            if (queryType == null) {
                ComponentUtilities.addNodeProperty(node, QUERY_TYPE_PROPERTY_NAME, QUERY_TYPE_PROPERTY_TYPE);
                ComponentUtilities.getNodeProperty(node, QUERY_TYPE_PROPERTY_NAME).setValue(QUERY_TYPE_PROPERTY_DEFAULT);
            }
        }

    }

    /**
     * Rename FILE_NAME to QUERY_CRITERIA.
     */
    private static class RenameFileNameToCriteria implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            ElementParameterType queryCriteria = ComponentUtilities.getNodeProperty(node, QUERY_CRITERIA_PROPERTY_NAME);

            if (queryCriteria == null) {
                ElementParameterType fileName = ComponentUtilities.getNodeProperty(node, FILE_NAME_PROPERTY_NAME);

                ComponentUtilities.addNodeProperty(node, QUERY_TYPE_PROPERTY_NAME, QUERY_CRITERIA_PROPERTY_TYPE);
                ComponentUtilities.getNodeProperty(node, QUERY_TYPE_PROPERTY_NAME).setValue(fileName.getValue());

                ComponentUtilities.removeNodeProperty(node, FILE_NAME_PROPERTY_NAME);
            }

        }

    }

}
