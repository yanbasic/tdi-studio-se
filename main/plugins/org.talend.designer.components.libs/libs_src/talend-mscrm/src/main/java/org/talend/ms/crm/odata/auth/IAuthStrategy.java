// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.ms.crm.odata.auth;

import javax.naming.AuthenticationException;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.olingo.client.api.communication.request.ODataRequest;
import org.talend.ms.crm.odata.httpclientfactory.IHttpclientFactoryObservable;

public interface IAuthStrategy {

    /**
     * Init Authentication strategy.
     * 
     */
    public void init() throws AuthenticationException;
    
    /**
     * Retrieve the HttpClientFactory.
     * 
     * Should be AbstractHttpClientFactoryObservable since DynamicsCRMClient need
     * to know if a new client is necessary.
     */
    public IHttpclientFactoryObservable getHttpClientFactory() throws AuthenticationException;
    
    /**
     * Refresh Authentication if needed.
     */
    public void refreshAuth() throws AuthenticationException;
    
    public void configureRequest(ODataRequest request);
    public void configureRequest(HttpRequestBase request);
    
}
