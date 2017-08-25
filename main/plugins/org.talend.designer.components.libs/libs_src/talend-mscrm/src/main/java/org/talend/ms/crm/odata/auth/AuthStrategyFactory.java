package org.talend.ms.crm.odata.auth;

import javax.naming.AuthenticationException;

import org.talend.ms.crm.odata.ClientConfiguration;

/**
 * Factory for OData authentication strategies. 
 * 
 * No need to use singleton pattern since no implementation/extends, static is enough.
 */
public final class AuthStrategyFactory {
    
    private AuthStrategyFactory(){}
    
    public final static IAuthStrategy createAuthStrategy(ClientConfiguration conf) {
        IAuthStrategy authStrategy = null;
        switch(conf.getAuthStrategy()){
            case OAUTH:
                    authStrategy = new OAuthStrategyImpl(conf);
                break;
            case NTLM:
                    authStrategy = new NTLMStrategyImpl(conf);
                   break;
        }
        
        return authStrategy;
    }

}
