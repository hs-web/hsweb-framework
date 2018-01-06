package org.hswebframework.web.authorization.ldap;

import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

public class LdapAuthorizationTests {

    LdapTemplate ldapTemplate;

   // @Test
    public void testGetUser(){
        ldapTemplate=new LdapTemplate();

        ldapTemplate.authenticate(LdapQueryBuilder.query().base("dc=261consulting, dc=com"),"admin");
    }
}
