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
import java.util.HashMap;
import java.util.Map;

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
 * Add the OUTPUT_EXT property and set export type for each supported Google doc.
 * 
 * @author ypiel
 *
 */
public class AddGoogleDocExports extends AbstractJobMigrationTask {

    /**
     * The component to update.
     */
    private final static String COMPONENT_NAME = "tGoogleDriveGet";
    
    private final static String OUTPUT_EXT_PROPERTY_NAME = "OUTPUT_EXT";
    private final static String OUTPUT_EXT_PROPERTY_TYPE = "CHECK";
    
    private final static Map<String, String> EXPORT_TYPE_MAP = new HashMap<>();
    static {
        EXPORT_TYPE_MAP.put("EXPORT_DOCUMENT", "WORD");
        EXPORT_TYPE_MAP.put("EXPORT_DRAWING", "PNG");
        EXPORT_TYPE_MAP.put("EXPORT_PRESENTATION", "POWERPOINT");
        EXPORT_TYPE_MAP.put("EXPORT_SPREADSHEET", "EXCEL");
    }
    
    
    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 07, 26, 12, 0, 0).getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        
        if(processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
         
        IComponentFilter filter = new NameComponentFilter(COMPONENT_NAME);
        
        try {
             ModifyComponentsAction.searchAndModify(item,
                                                        processType,
                                                        filter, 
                                                        Arrays.<IComponentConversion> asList(new AddOutPutExt(), 
                                                                                                new AddExportGoogleDoc()));
         } catch (PersistenceException e) {
             ExceptionHandler.process(e);
             return ExecutionResult.FAILURE;
         }
        
        
        return ExecutionResult.SUCCESS_NO_ALERT;
    }
    
    /**
     * Add the OUTPUT_EXT property with 'false' value.
     * 
     * @author ypiel
     *
     */
    private static class AddOutPutExt implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            ElementParameterType outputExt = ComponentUtilities.getNodeProperty(node, OUTPUT_EXT_PROPERTY_NAME);
            
            if(outputExt == null) {
                ComponentUtilities.addNodeProperty(node, OUTPUT_EXT_PROPERTY_NAME, OUTPUT_EXT_PROPERTY_TYPE);
                ComponentUtilities.getNodeProperty(node, OUTPUT_EXT_PROPERTY_NAME).setValue("false");
            }
        }
        
    }
    
    /**
     * Set export type for each supported google document.
     * 
     * @author ypiel
     *
     */
    private static class AddExportGoogleDoc implements IComponentConversion {

        @Override
        public void transform(NodeType node) {
            for(Map.Entry<String, String> export : EXPORT_TYPE_MAP.entrySet()) {
                String name = export.getKey();
                String value = export.getValue();
                String type = "CLOSED_LIST";
                
                ElementParameterType outputExt = ComponentUtilities.getNodeProperty(node, name);
                
                if(outputExt == null) {
                    ComponentUtilities.addNodeProperty(node, name, type);
                    ComponentUtilities.getNodeProperty(node, name).setValue(value);
                }
            }
        }
    }

}
