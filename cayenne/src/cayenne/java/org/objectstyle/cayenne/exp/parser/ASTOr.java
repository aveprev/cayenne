/* Generated By:JJTree: Do not edit this line. ASTOr.java */

package org.objectstyle.cayenne.exp.parser;

import org.objectstyle.cayenne.exp.Expression;

class ASTOr extends SimpleNode {
    public ASTOr(int id) {
        super(id);
    }

    public ASTOr(ExpressionParser p, int id) {
        super(p, id);
    }

    protected String getExpressionOperator(int index) {
        return "or";
    }

    public int getType() {
        return Expression.OR;
    }

    public void jjtClose() {
        super.jjtClose();
        flattenTree();
    }
}
