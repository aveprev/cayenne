/* ====================================================================
 * 
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2004, Andrei (Andrus) Adamchik and individual authors
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
package org.objectstyle.cayenne.exp.parser;

import org.objectstyle.cayenne.exp.Expression;
import org.objectstyle.cayenne.util.ConversionUtil;

/**
 * "Between" expression.
 * 
 * @since 1.1
 * @author Andrei Adamchik
 */
public class ASTBetween extends ConditionNode {

    ASTBetween(int id) {
        super(id);
    }

    public ASTBetween(ASTPath path, Object value1, Object value2) {
        super(ExpressionParserTreeConstants.JJTBETWEEN);
        jjtAddChild(path, 0);
        jjtAddChild(new ASTScalar(value1), 1);
        jjtAddChild(new ASTScalar(value2), 2);
    }

    protected Object evaluateNode(Object o) throws Exception {
        int len = jjtGetNumChildren();
        if (len != 3) {
            return Boolean.FALSE;
        }

        Comparable c1 = ConversionUtil.toComparabe(evaluateChild(0, o));

        if (c1 == null) {
            return Boolean.FALSE;
        }

        Comparable c2 = ConversionUtil.toComparabe(evaluateChild(1, o));
        if (c2 == null) {
            return Boolean.FALSE;
        }

        Comparable c3 = ConversionUtil.toComparabe(evaluateChild(2, o));
        if (c3 == null) {
            return Boolean.FALSE;
        }

        return c1.compareTo(c2) >= 0
            && c1.compareTo(c3) <= 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Creates a copy of this expression node, without copying children.
     */
    public Expression shallowCopy() {
        return new ASTBetween(id);
    }

    protected String getExpressionOperator(int index) {
        return (index == 2) ? "and" : "between";
    }

    public int getType() {
        return Expression.BETWEEN;
    }
}
