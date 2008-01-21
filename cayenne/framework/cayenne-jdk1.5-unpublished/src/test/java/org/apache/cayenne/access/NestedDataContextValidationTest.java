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

package org.apache.cayenne.access;

import org.apache.art.Artist;
import org.apache.cayenne.unit.CayenneCase;
import org.apache.cayenne.validation.ValidationException;

public class NestedDataContextValidationTest extends CayenneCase {

    public void testValidateOnCommitToParent() {
        DataContext context = createDataContext();
        context.setValidatingObjectsOnCommit(true);

        DataContext childContext = context.createChildDataContext();
        assertTrue(
                "Child context must have inherited the validation flag from parent",
                childContext.isValidatingObjectsOnCommit());

        Artist a1 = childContext.newObject(Artist.class);
        try {
            childContext.commitChangesToParent();
            fail("No validation was performed");
        }
        catch (ValidationException e) {
            // expected
        }

        assertFalse(context.hasChanges());

        a1.setArtistName("T");
        childContext.commitChangesToParent();
    }
}
