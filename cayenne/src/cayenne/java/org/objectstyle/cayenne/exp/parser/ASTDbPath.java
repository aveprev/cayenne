/* Generated By:JJTree: Do not edit this line. ASTDbPath.java */

package org.objectstyle.cayenne.exp.parser;

import org.objectstyle.cayenne.exp.Expression;

class ASTDbPath extends SimpleNode {
    public ASTDbPath(int id) {
        super(id);
    }

    public ASTDbPath(ExpressionParser p, int id) {
        super(p, id);
    }

    protected void toStringBuffer(StringBuffer buf) {
        buf.append("db:").append(value);
    }

    public int getType() {
        return Expression.DB_PATH;
    }

    public int getOperandCount() {
        return 1;
    }
    
    public Object getOperand(int index) {
        if(index == 0) {
            return value;
        }
        
        throw new ArrayIndexOutOfBoundsException(index);
    }
}
