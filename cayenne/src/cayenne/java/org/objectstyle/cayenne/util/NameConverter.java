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
package org.objectstyle.cayenne.util;

import java.util.StringTokenizer;

/** 
 * Utility class to convert from different naming styles to Java convention.
 * For example names like "ABCD_EFG" can be converted to "abcdEfg".
 * 
 * @author Andrei Adamchik
 */
public class NameConverter {

    /** 
     * Converts names like "ABCD_EFG_123" to Java-style names like "abcdEfg123".
     * If <code>capitalize</code> is true, returned name is capitalized
     * (for instance if this is a class name). 
     */
    public static String undescoredToJava(String name, boolean capitalize) {
        StringTokenizer st = new StringTokenizer(name, "_");
        StringBuffer buf = new StringBuffer();

        boolean first = true;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            
            int len = token.length();
            if (len == 0) {
                continue;
            }
            
            // sniff mixed case vs. single case styles
            boolean hasLowerCase = false;
            boolean hasUpperCase = false;
            for(int i = 0; i < len && !(hasUpperCase && hasLowerCase); i++) {
                if(Character.isUpperCase(token.charAt(i))) {
                    hasUpperCase = true;
                }
                else if(Character.isLowerCase(token.charAt(i))) {
                    hasLowerCase = true;
                }
            }
            
            // if mixed case, preserve it, if all upper, convert to lower
            if(hasUpperCase && !hasLowerCase) {
                token = token.toLowerCase();
            }
            
            if (first) {
                // apply explicit capitalization rules, if this is the first token
                first = false;
                if (capitalize) {
                    buf.append(Character.toUpperCase(token.charAt(0)));
                }
                else {
                    buf.append(Character.toLowerCase(token.charAt(0)));
                }
            }
            else {
                buf.append(Character.toUpperCase(token.charAt(0)));
            }
            

            if (len > 1) {
                buf.append(token.substring(1, len));
            }
        }
        return buf.toString();
    }
}