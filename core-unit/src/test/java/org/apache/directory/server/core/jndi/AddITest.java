/*
 *  Licensed to the Apache Software Foundation (ASF) under one
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
 *
 */
package org.apache.directory.server.core.jndi;


import org.apache.directory.server.core.unit.AbstractAdminTestCase;
import org.apache.directory.shared.ldap.exception.LdapSchemaViolationException;
import org.apache.directory.shared.ldap.message.AttributeImpl;
import org.apache.directory.shared.ldap.message.AttributesImpl;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;


/**
 * Contributed by Luke Taylor to fix DIRSERVER-169.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class AddITest extends AbstractAdminTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    
//    /**
//     * Test that attribute name case is preserved after adding an entry
//     * in the case the user added them.  This is to test DIRSERVER-832.
//     */
//    public void testAddCasePreservedOnAttributeNames() throws Exception
//    {
//        Attributes attrs = new AttributesImpl( true );
//        Attribute oc = new AttributeImpl( "ObjectClass", "top" );
//        oc.add( "PERSON" );
//        oc.add( "organizationalPerson" );
//        oc.add( "inetORGperson" );
//        Attribute cn = new AttributeImpl( "Cn", "Kevin Spacey" );
//        Attribute dc = new AttributeImpl( "sN", "Spacey" );
//        attrs.put( oc );
//        attrs.put( cn );
//        attrs.put( dc);
//        sysRoot.createSubcontext( "uID=kevin", attrs );
//        Attributes returned = sysRoot.getAttributes( "UID=kevin" );
//        
//        NamingEnumeration attrList = returned.getAll();
//        while( attrList.hasMore() )
//        {
//            Attribute attr = ( Attribute ) attrList.next();
//            
//            if ( attr.getID().equalsIgnoreCase( "uid" ) )
//            {
//                assertEquals( "uID", attr.getID() );
//            }
//            
//            if ( attr.getID().equalsIgnoreCase( "objectClass" ) )
//            {
//                assertEquals( "ObjectClass", attr.getID() );
//            }
//            
//            if ( attr.getID().equalsIgnoreCase( "sn" ) )
//            {
//                assertEquals( "sN", attr.getID() );
//            }
//            
//            if ( attr.getID().equalsIgnoreCase( "cn" ) )
//            {
//                assertEquals( "Cn", attr.getID() );
//            }
//        }
//    }
    
    
    /**
     * Test that we can't add an entry with an attribute type not within
     * any of the MUST or MAY of any of its objectClasses
     */
    public void testAddAttributesNotInObjectClasses() throws Exception
    {
        Attributes attrs = new AttributesImpl( true );
        Attribute oc = new AttributeImpl( "ObjectClass", "top" );
        Attribute cn = new AttributeImpl( "cn", "kevin Spacey" );
        Attribute dc = new AttributeImpl( "dc", "ke" );
        attrs.put( oc );
        attrs.put( cn );
        attrs.put( dc);

        String base = "uid=kevin";

        //create subcontext
        try
        {
            sysRoot.createSubcontext( base, attrs );
            fail( "Should not reach this state" );
        }
        catch ( LdapSchemaViolationException e )
        {
            assertTrue( true );
        }
    }
}
