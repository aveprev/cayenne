/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2002 The ObjectStyle Group
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.cayenne.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A mapping descriptor for a database stored procedure. 
 * 
 * @author Andrei Adamchik
 */
public class Procedure extends MapObject {
    protected String catalog;
    protected String schema;
    protected boolean returningValue;
    protected List callParameters = new ArrayList();

    /**
     * Default constructor for StoredProcedure.
     */
    public Procedure() {
        super();
    }

    public Procedure(String name) {
        super(name);
    }
    
    /**
     * @return parent DataMap of this entity.
     */
    public DataMap getDataMap() {
        return (DataMap) getParent();
    }

    /**
     * Sets parent DataMap of this entity.
     */
    public void setDataMap(DataMap dataMap) {
        this.setParent(dataMap);
    }

    /**
     * Adds new call parameter to the stored procedure. Also sets
     * <code>param</code>'s parent to be this procedure.
     */
    public void addCallParameter(ProcedureParameter param) {
        if (param.getName() == null) {
            throw new IllegalArgumentException("Attempt to add unnamed parameter.");
        }

        if (callParameters.contains(param)) {
            throw new IllegalArgumentException(
                "Attempt to add the same parameter more than once:" + param);
        }

        param.setProcedure(this);
        callParameters.add(param);
    }
    

    /** Removes a named call parameter. */
    public void removeCallParameter(String name) {
        for (int i = 0; i < callParameters.size(); i++) {
            ProcedureParameter nextParam = (ProcedureParameter) callParameters.get(i);
            if (name.equals(nextParam.getName())) {
                callParameters.remove(i);
                break;
            }
        }
    }

    public void clearCallParameters() {
        callParameters.clear();
    }

    /**
     * Returns a list of call parameters.
     * 
     * @return List
     */
    public List getCallParameters() {
        return callParameters;
    }
    
    /**
     * Returns a list of OUT and INOUT call parameters. If procedure has a
     * return value, it will also be included as a call parameter.
     * @return
     */
    public List getCallOutParams() {
        List outParams = new ArrayList(callParameters.size());
        Iterator it = callParameters.iterator();
        while(it.hasNext()) {
            ProcedureParameter param = (ProcedureParameter)it.next();
            if(param.isOutParam()) {
                outParams.add(param);
            }
        }
        
        return outParams;
    }

    /**
     * Returns parameter describing the return value of the StoredProcedure, or
     * null if procedure does not support return values. If procedure supports return parameters,
     * its first parameter is always assumed to be a return result.
     */
    public ProcedureParameter getResultParam() {
        // if procedure returns parameters, this must be the first parameter
        // otherwise, return null
        return (returningValue && callParameters.size() > 0)
            ? (ProcedureParameter) callParameters.get(0)
            : null;
    }
    
    /**
     * Returns the returningValue.
     * @return boolean
     */
    public boolean isReturningValue() {
        return returningValue;
    }

    /**
     * Sets the returningValue.
     * @param returningValue The returningValue to set
     */
    public void setReturningValue(boolean returningValue) {
        this.returningValue = returningValue;
    }
}
