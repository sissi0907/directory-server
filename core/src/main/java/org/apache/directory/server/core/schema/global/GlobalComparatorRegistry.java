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
package org.apache.directory.server.core.schema.global;


import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.directory.server.core.schema.ComparatorRegistry;
import org.apache.directory.server.core.schema.SerializableComparator;
import org.apache.directory.server.core.schema.bootstrap.BootstrapComparatorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple POJO implementation of the ComparatorRegistry service interface.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class GlobalComparatorRegistry implements ComparatorRegistry
{
    /** static class logger */
    private final static Logger log = LoggerFactory.getLogger( GlobalComparatorRegistry.class );
    /** the comparators in this registry */
    private final Map comparators;
    /** maps an OID to a schema name*/
    private final Map oidToSchema;
    /** the underlying bootstrap registry to delegate on misses to */
    private BootstrapComparatorRegistry bootstrap;


    // ------------------------------------------------------------------------
    // C O N S T R U C T O R S
    // ------------------------------------------------------------------------

    /**
     * Creates a default ComparatorRegistry by initializing the map and the
     * montior.
     */
    public GlobalComparatorRegistry( BootstrapComparatorRegistry bootstrap )
    {
        this.oidToSchema = new HashMap();
        this.comparators = new HashMap();
        // override bootstrap registry used by serializable comparators
        SerializableComparator.setRegistry( this );
        this.bootstrap = bootstrap;
        if ( this.bootstrap == null )
        {
            throw new NullPointerException( "the bootstrap registry cannot be null" );
        }
    }


    // ------------------------------------------------------------------------
    // Service Methods
    // ------------------------------------------------------------------------

    
    public void register( String schema, String oid, Comparator comparator ) throws NamingException
    {
        if ( comparators.containsKey( oid ) || bootstrap.hasComparator( oid ) )
        {
            NamingException e = new NamingException( "Comparator with OID " + oid + " already registered!" );
            throw e;
        }

        oidToSchema.put( oid, schema );
        comparators.put( oid, comparator );
        if ( log.isDebugEnabled() )
        {
            log.debug( "registered comparator with OID '" + oid + "':" + comparator );
        }
    }


    public Comparator lookup( String oid ) throws NamingException
    {
        Comparator c;
        NamingException e;

        if ( comparators.containsKey( oid ) )
        {
            c = ( Comparator ) comparators.get( oid );
            if ( log.isDebugEnabled() )
            {
                log.debug( "looked up comparator with OID '" + oid + "':" + c );
            }
            return c;
        }

        if ( bootstrap.hasComparator( oid ) )
        {
            c = bootstrap.lookup( oid );
            if ( log.isDebugEnabled() )
            {
                log.debug( "looked up comparator with OID '" + oid + "':" + c );
            }
            return c;
        }

        e = new NamingException( "Comparator not found for OID: " + oid );
        throw e;
    }


    public boolean hasComparator( String oid )
    {
        return comparators.containsKey( oid ) || bootstrap.hasComparator( oid );
    }


    public String getSchemaName( String oid ) throws NamingException
    {
        if ( !Character.isDigit( oid.charAt( 0 ) ) )
        {
            throw new NamingException( "OID " + oid + " is not a numeric OID" );
        }

        if ( oidToSchema.containsKey( oid ) )
        {
            return ( String ) oidToSchema.get( oid );
        }

        if ( bootstrap.hasComparator( oid ) )
        {
            return bootstrap.getSchemaName( oid );
        }

        throw new NamingException( "OID " + oid + " not found in oid to " + "schema name map!" );
    }
}
