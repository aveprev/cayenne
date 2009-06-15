/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.exp.parser;

import java.math.BigDecimal;
import java.util.List;

import org.apache.cayenne.exp.Expression;

/**
 * "Equal To" expression.
 * 
 * @since 1.1
 */
public class ASTEqual extends ConditionNode {
    /**
     * Constructor used by expression parser. Do not invoke directly.
     */
    ASTEqual(int id) {
        super(id);
    }

    public ASTEqual() {
        super(ExpressionParserTreeConstants.JJTEQUAL);
    }

    /**
     * Creates "Equal To" expression.
     */
    public ASTEqual(ASTPath path, Object value) {
        super(ExpressionParserTreeConstants.JJTEQUAL);
        jjtAddChild(path, 0);
        jjtAddChild(new ASTScalar(value), 1);
        connectChildren();
    }

    @Override
    protected Object evaluateNode(Object o) throws Exception {
        int len = jjtGetNumChildren();
        if (len != 2) {
            return Boolean.FALSE;
        }

        Object o1 = evaluateChild(0, o);
        Object o2 = evaluateChild(1, o);

        return evaluateImpl(o1, o2);
    }
    
    /**
     * Compares two objects, if one of them is array, 'in' operation is performed
     */
    static boolean evaluateImpl(Object o1, Object o2) {
        // TODO: maybe we need a comparison "strategy" here, instead of
        // a switch of all possible cases? ... there were other requests for
        // more relaxed type-unsafe comparison (e.g. numbers to strings)
        
        if (o1 == null && o2 == null) {
            return true;
        }
        else if (o1 != null) {
            /**
             * Per CAY-419 we perform 'in' comparison if one object is a list,
             * and other is not
             */
            if (o1 instanceof List && !(o2 instanceof List)) {
                for (Object element : ((List<?>) o1)) {
                    if (element != null && evaluateAtomic(element, o2)) {
                        return true;
                    }
                }
                return false;
            }
            if (o2 instanceof List && !(o1 instanceof List)) {
                for (Object element : ((List<?>) o2)) {
                    if (element != null && evaluateAtomic(element, o1)) {
                        return true;
                    }
                }
                return false;
            }
            
            return evaluateAtomic(o1, o2);
        }
        return false;
    }
    
    /**
     * Compares two objects. They must not be null
     */
    static boolean evaluateAtomic(Object o1, Object o2) {
        // BigDecimals must be compared using compareTo (
        // see CAY-280 and BigDecimal.equals JavaDoc)
        if (o1 instanceof BigDecimal) {
            if (o2 instanceof BigDecimal) {
                return ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
            }

            return false;
        }

        return o1.equals(o2);
    }

    /**
     * Creates a copy of this expression node, without copying children.
     */
    @Override
    public Expression shallowCopy() {
        return new ASTEqual(id);
    }

    @Override
    protected String getExpressionOperator(int index) {
        return "=";
    }

    @Override
    public int getType() {
        return Expression.EQUAL_TO;
    }
}
