/* ====================================================================
 * 
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2005, Andrei (Andrus) Adamchik and individual authors
 * of the software. All rights reserved.
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
 * 3. The end-user documentation included with the redistribution, if any,
 *    must include the following acknowlegement:
 *    "This product includes software developed by independent contributors
 *    and hosted on ObjectStyle Group web site (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, email
 *    "andrus at objectstyle dot org".
 * 
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    or "Cayenne", nor may "ObjectStyle" or "Cayenne" appear in their
 *    names without prior written permission.
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
 * individuals and hosted on ObjectStyle Group web site.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 */
package org.objectstyle.cayenne.tools;

import org.apache.tools.ant.Task;
import org.objectstyle.cayenne.CayenneRuntimeException;
import org.objectstyle.cayenne.PersistentObject;
import org.objectstyle.cayenne.gen.AntClassGenerator;
import org.objectstyle.cayenne.gen.ClassGenerator;
import org.objectstyle.cayenne.map.ObjEntity;

/**
 * @since 1.2
 * @author Andrus Adamchik
 */
//TODO: integrate with Antlib and new cgen code by Mike K. (hopefully new cgen features
//will make this class redundant)
class AntClientClassGenerator extends AntClassGenerator {

    static final String CLIENT_SUBCLASS_TEMPLATE = "dotemplates/client-subclass.vm";
    static final String CLIENT_SUPERCLASS_TEMPLATE = "dotemplates/client-superclass.vm";

    AntClientClassGenerator(Task task) {
        setParentTask(task);
    }

    protected String defaultSubclassTemplate() {
        return CLIENT_SUBCLASS_TEMPLATE;
    }

    protected String defaultSuperclassTemplate() {
        return CLIENT_SUPERCLASS_TEMPLATE;
    }

    /**
     * Overrides superclass settings to provide client-side values to the ClassGenerator.
     */
    protected void initClassGenerator(
            ClassGenerator generator,
            ObjEntity entity,
            boolean superclass) {

        // figure out generator properties
        String fullClassName = entity.getClientClassName();
        if (fullClassName == null) {
            fullClassName = entity.getClassName();
        }

        int i = fullClassName.lastIndexOf(".");

        String pkg = null;
        String spkg = null;
        String cname = null;

        // dot in first or last position is invalid
        if (i == 0 || i + 1 == fullClassName.length()) {
            throw new CayenneRuntimeException("Invalid class mapping: " + fullClassName);
        }
        else if (i < 0) {
            pkg = (superclass) ? superPkg : null;
            spkg = (superclass) ? null : superPkg;
            cname = fullClassName;
        }
        else {
            cname = fullClassName.substring(i + 1);
            pkg = (superclass && superPkg != null) ? superPkg : fullClassName
                    .substring(0, i);

            spkg = (!superclass && superPkg != null && !pkg.equals(superPkg))
                    ? superPkg
                    : null;
        }

        // init generator
        generator.setPackageName(pkg);
        generator.setClassName(cname);
        if (entity.getClientSuperClassName() != null) {
            generator.setSuperClassName(entity.getClientSuperClassName());
        }
        else {
            generator.setSuperClassName(PersistentObject.class.getName());
        }

        generator.setSuperPackageName(spkg);
    }
}