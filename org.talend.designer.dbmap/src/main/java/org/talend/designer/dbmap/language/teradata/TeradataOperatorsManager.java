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
package org.talend.designer.dbmap.language.teradata;

import org.talend.designer.dbmap.language.IDbOperator;
import org.talend.designer.dbmap.language.IDbOperatorManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class TeradataOperatorsManager implements IDbOperatorManager {

    /**
     * 
     * DOC amaumont OracleOperatorsManager class global comment. Detailled comment <br/>
     * 
     * $Id$
     * 
     */
    public enum DB_OPERATOR implements IDbOperator {
        EQUAL("EQ"), //$NON-NLS-1$
        DIFFERENT("NE"), //$NON-NLS-1$
        STRICTLY_INFERIOR("LT"), //$NON-NLS-1$
        INFERIOR_OR_EQUAL("LE"), //$NON-NLS-1$
        SUPERIOR("GT"), //$NON-NLS-1$
        SUPERIOR_OR_EQUAL("GE"), //$NON-NLS-1$

        IS_NULL("IS NULL", null, true), //$NON-NLS-1$
        IS_NOT_NULL("IS NOT NULL", null, true), //$NON-NLS-1$
        BETWEEN("BETWEEN", " AND ", false), //$NON-NLS-1$ //$NON-NLS-2$
        NOT_BETWEEN("NOT BETWEEN", " AND ", false), //$NON-NLS-1$ //$NON-NLS-2$
        IN("IN", "( , )", false), //$NON-NLS-1$ //$NON-NLS-2$
        NOT_IN("NOT IN", "( , )", false), //$NON-NLS-1$ //$NON-NLS-2$
        LIKE("LIKE", "'%'", false), //$NON-NLS-1$ //$NON-NLS-2$
        NOT_LIKE("NOT LIKE", "'%'", false), //$NON-NLS-1$ //$NON-NLS-2$
        EXISTS("EXISTS", "(SELECT * FROM MyTable)", false), ; //$NON-NLS-1$ //$NON-NLS-2$

        private String operator;

        private String associatedExpression;

        private boolean monoOperand;

        /**
         * DOC amaumont DbOperator constructor comment.
         * 
         * @param operator
         * @param expressionPattern
         * @param monoOperand
         */
        private DB_OPERATOR(String operator, String expressionPattern, boolean monoOperand) {
            this.operator = operator;
            this.associatedExpression = expressionPattern;
            this.monoOperand = monoOperand;
        }

        /**
         * DOC amaumont DbOperator constructor comment.
         * 
         * @param operator
         * @param associatedExpression
         * @param monoOperand
         */
        private DB_OPERATOR(String operator) {
            this.operator = operator;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.designer.dbmap.language.IDbOperator#getValue()
         */
        public String getOperator() {
            return operator;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.designer.dbmap.language.IDbOperator#getAssociatedExpression()
         */
        public String getAssociatedExpression() {
            return associatedExpression;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.talend.designer.dbmap.language.IDbOperator#isMonoOperand()
         */
        public boolean isMonoOperand() {
            return monoOperand;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.language.IDbOperatorManager#getOperators()
     */
    public IDbOperator[] getOperators() {
        return DB_OPERATOR.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.language.IDbOperatorManager#getOperatorFromValue(java.lang.String)
     */
    public IDbOperator getOperatorFromValue(String operatorValue) {
        IDbOperator[] operators = getOperators();
        for (int i = 0; i < operators.length; i++) {
            if (operators[i].getOperator().equals(operatorValue)) {
                return operators[i];
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.dbmap.language.IDbOperatorManager#getDefaultEqualOperator()
     */
    public IDbOperator getDefaultEqualOperator() {
        return DB_OPERATOR.EQUAL;
    }

}
