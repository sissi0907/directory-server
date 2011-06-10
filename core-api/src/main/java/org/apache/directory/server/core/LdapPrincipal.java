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
package org.apache.directory.server.core;


import java.net.SocketAddress;
import java.security.Principal;

import org.apache.directory.server.i18n.I18n;
import org.apache.directory.shared.ldap.model.constants.AuthenticationLevel;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;


/**
 * An alternative X500 user implementation that has access to the distinguished
 * name of the principal as well as the String representation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdapPrincipal implements Principal, Cloneable
{
    private static final long serialVersionUID = 3906650782395676720L;

    /** the normalized distinguished name of the principal */
    private Dn dn = Dn.EMPTY_DN;

    /** the authentication level for this principal */
    private AuthenticationLevel authenticationLevel;
    
    /** The userPassword
     * @todo security risk remove this immediately
     */
    private byte[] userPassword;
    
    /** The SchemaManager */
    private SchemaManager schemaManager;
    
    private SocketAddress clientAddress;
    private SocketAddress serverAddress;


    /**
     * Creates a new LDAP/X500 principal without any group associations.  Keep
     * this package friendly so only code in the package can create a
     * trusted principal.
     *
     * @param dn the normalized distinguished name of the principal
     * @param authenticationLevel the authentication level for this principal
     */
    public LdapPrincipal( SchemaManager schemaManager, Dn dn, AuthenticationLevel authenticationLevel )
    {
        this.schemaManager = schemaManager;
        this.dn = dn;
        
        if ( ! dn.isSchemaAware() )
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_436 ) );
        }
        
        this.authenticationLevel = authenticationLevel;
        this.userPassword = null;
    }
    

    /**
     * Creates a new LDAP/X500 principal without any group associations.  Keep
     * this package friendly so only code in the package can create a
     * trusted principal.
     *
     * @param dn the normalized distinguished name of the principal
     * @param authenticationLevel the authentication level for this principal
     * @param userPassword The user password
     */
    public LdapPrincipal(  SchemaManager schemaManager, Dn dn, AuthenticationLevel authenticationLevel, byte[] userPassword )
    {
        this.dn = dn;
        this.authenticationLevel = authenticationLevel;
        this.userPassword = new byte[ userPassword.length ];
        System.arraycopy( userPassword, 0, this.userPassword, 0, userPassword.length );
        this.schemaManager = schemaManager;
}


    /**
     * Creates a principal for the no name anonymous user whose Dn is the empty
     * String.
     */
    public LdapPrincipal()
    {
        authenticationLevel = AuthenticationLevel.NONE;
        userPassword = null;
    }


    /**
     * Creates a principal for the no name anonymous user whose Dn is the empty
     * String.
     */
    public LdapPrincipal( SchemaManager schemaManager )
    {
        authenticationLevel = AuthenticationLevel.NONE;
        userPassword = null;
        this.schemaManager = schemaManager;
    }


    /**
     * Gets a cloned copy of the normalized distinguished name of this
     * principal as a {@link org.apache.directory.shared.ldap.model.name.Dn}.
     *
     * @return the cloned distinguished name of the principal as a {@link org.apache.directory.shared.ldap.model.name.Dn}
     */
    public Dn getDn()
    {
        return dn;
    }


    /**
     * Returns the normalized distinguished name of the principal as a String.
     */
    public String getName()
    {
        return dn.getNormName();
    }


    /**
     * Gets the authentication level associated with this LDAP principle.
     *
     * @return the authentication level
     */
    public AuthenticationLevel getAuthenticationLevel()
    {
        return authenticationLevel;
    }


    public byte[] getUserPassword()
    {
        return userPassword;
    }


    public void setUserPassword( byte[] userPassword )
    {
        this.userPassword = new byte[ userPassword.length ];
        System.arraycopy( userPassword, 0, this.userPassword, 0, userPassword.length );
    }
    
    
    /**
     * Clone the object. This is done so that we don't store the 
     * password in a LdapPrincipal more than necessary.
     */
    public Object clone() throws CloneNotSupportedException
    {
        LdapPrincipal clone = (LdapPrincipal)super.clone();
        
        if ( userPassword != null )
        {
            clone.setUserPassword( userPassword );
        }
        
        return clone;
    }
    
    
    /**
     * @return the schemaManager
     */
    public SchemaManager getSchemaManager()
    {
        return schemaManager;
    }


    /**
     * @param schemaManager the schemaManager to set
     */
    public void setSchemaManager( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
        
        try
        {
            dn.apply( schemaManager );
        }
        catch ( LdapInvalidDnException lide )
        {
            // TODO: manage this exception
        }
    }


    /**
     * @return the clientAddress
     */
    public SocketAddress getClientAddress()
    {
        return clientAddress;
    }


    /**
     * @param clientAddress the clientAddress to set
     */
    public void setClientAddress( SocketAddress clientAddress )
    {
        this.clientAddress = clientAddress;
    }


    /**
     * @return the serverAddress
     */
    public SocketAddress getServerAddress()
    {
        return serverAddress;
    }


    /**
     * @param serverAddress the serverAddress to set
     */
    public void setServerAddress( SocketAddress serverAddress )
    {
        this.serverAddress = serverAddress;
    }


    /**
     * Returns string representation of the normalized distinguished name
     * of this principal.
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        if ( dn.isSchemaAware() )
        {
            sb.append( "(n)" );
        }

        sb.append( "['" );
        sb.append( dn.getName() );
        sb.append( "'" );
        
        if ( clientAddress != null )
        {
            sb.append( ", client@" );
            sb.append( clientAddress );
        }
        
        if ( serverAddress != null )
        {
            sb.append( ", server@" );
            sb.append( serverAddress );
        }
        
        sb.append( "]" );
        
        return sb.toString();
    }
}
