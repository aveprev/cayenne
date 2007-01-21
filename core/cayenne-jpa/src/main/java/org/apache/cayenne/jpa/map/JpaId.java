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

package org.apache.cayenne.jpa.map;

import java.sql.Types;

import javax.persistence.TemporalType;

import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.util.TreeNodeChild;

public class JpaId extends JpaAttribute {

    protected JpaColumn column;
    protected JpaGeneratedValue generatedValue;
    protected TemporalType temporal;
    protected JpaTableGenerator tableGenerator;
    protected JpaSequenceGenerator sequenceGenerator;

    /**
     * Returns default JDBC mapping for this basic attribute.
     */
    public int getDefaultJdbcType() {

        if (getTemporal() != null) {

            if (TemporalType.TIMESTAMP == getTemporal()) {
                return Types.TIMESTAMP;
            }
            else if (TemporalType.DATE == getTemporal()) {
                return Types.DATE;
            }
            else {
                return Types.TIME;
            }
        }
        else {
            return TypesMapping.getSqlTypeByJava(getPropertyDescriptor().getType());
        }
    }

    @TreeNodeChild
    public JpaColumn getColumn() {
        return column;
    }

    public void setColumn(JpaColumn column) {
        this.column = column;
    }

    @TreeNodeChild
    public JpaGeneratedValue getGeneratedValue() {
        return generatedValue;
    }

    public void setGeneratedValue(JpaGeneratedValue generatedValue) {
        this.generatedValue = generatedValue;
    }

    public TemporalType getTemporal() {
        return temporal;
    }

    public void setTemporal(TemporalType temporal) {
        this.temporal = temporal;
    }

    public JpaSequenceGenerator getSequenceGenerator() {
        return sequenceGenerator;
    }

    public void setSequenceGenerator(JpaSequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    public JpaTableGenerator getTableGenerator() {
        return tableGenerator;
    }

    public void setTableGenerator(JpaTableGenerator tableGenerator) {
        this.tableGenerator = tableGenerator;
    }
}
