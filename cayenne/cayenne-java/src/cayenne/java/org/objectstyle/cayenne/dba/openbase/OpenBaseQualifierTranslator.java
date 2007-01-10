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
package org.objectstyle.cayenne.dba.openbase;

import org.objectstyle.cayenne.access.trans.QualifierTranslator;
import org.objectstyle.cayenne.access.trans.QueryAssembler;
import org.objectstyle.cayenne.exp.Expression;
import org.objectstyle.cayenne.map.DbAttribute;

/** 
 * Translates query qualifier to SQL. Used as a helper class by query translators.
 * 
 * @author <a href="mailto:mkienenb@alaska.net">Mike Kienenberger</a>
 * @author Andrei Adamchik
 * 
 * @since 1.1
 */
public class OpenBaseQualifierTranslator extends QualifierTranslator {

    public OpenBaseQualifierTranslator() {
        this(null);
    }

    public OpenBaseQualifierTranslator(QueryAssembler queryAssembler) {
        super(queryAssembler);
    }

    public void startNode(Expression node, Expression parentNode) {

        if (node.getOperandCount() == 2) {
            // binary nodes are the only ones that currently require this
            detectObjectMatch(node);

            if (parenthesisNeeded(node, parentNode)) {
                qualBuf.append('(');
            }

            // super implementation has special handling 
            // of LIKE_IGNORE_CASE and NOT_LIKE_IGNORE_CASE
            // OpenBase is case-insensitive by default
            // ...
        }
        else {
            super.startNode(node, parentNode);
        }
    }

    public void endNode(Expression node, Expression parentNode) {
        if (node.getOperandCount() == 2) {
            // check if we need to use objectMatchTranslator to finish building the expression
            if (matchingObject) {
                appendObjectMatch();
            }

            if (parenthesisNeeded(node, parentNode))
                qualBuf.append(')');

            // super implementation has special handling 
            // of LIKE_IGNORE_CASE and NOT_LIKE_IGNORE_CASE
            // OpenBase is case-insensitive by default
            // ...
        }
        else {
            super.endNode(node, parentNode);
        }
    }

    protected void appendLiteralDirect(
        StringBuffer buf,
        Object val,
        DbAttribute attr,
        Expression parentExpression) {

        // Special handling of string matching is needed:
        // Case-sensitive LIKE must be converted to [x][Y][z] format
        if (val instanceof String
            && (parentExpression.getType() == Expression.LIKE
                || parentExpression.getType() == Expression.NOT_LIKE)) {

            val = caseSensitiveLikePattern((String) val);
        }

        super.appendLiteralDirect(buf, val, attr, parentExpression);
    }

    private String caseSensitiveLikePattern(String pattern) {
        int len = pattern.length();
        StringBuffer buffer = new StringBuffer(len * 3);

        for (int i = 0; i < len; i++) {
            char c = pattern.charAt(i);
            if (c == '%' || c == '?') {
                buffer.append(c);
            }
            else {
                buffer.append("[").append(c).append("]");
            }
        }

        return buffer.toString();
    }

    public void finishedChild(Expression node, int childIndex, boolean hasMoreChildren) {
        if (!hasMoreChildren) {
            return;
        }

        // super implementation has special handling 
        // of LIKE_IGNORE_CASE and NOT_LIKE_IGNORE_CASE
        // OpenBase is case-insensitive by default
        // ...

        switch (node.getType()) {

            case Expression.LIKE_IGNORE_CASE :
                finishedChildNodeAppendExpression(node, " LIKE ");
                break;
            case Expression.NOT_LIKE_IGNORE_CASE :
                finishedChildNodeAppendExpression(node, " NOT LIKE ");
                break;
            default :
                super.finishedChild(node, childIndex, hasMoreChildren);
        }
    }

    private void finishedChildNodeAppendExpression(Expression node, String operation) {
        StringBuffer buf = (matchingObject) ? new StringBuffer() : qualBuf;
        buf.append(operation);
        if (matchingObject) {
            objectMatchTranslator.setOperation(buf.toString());
            objectMatchTranslator.setExpression(node);
        }
    }
}