package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.organizational.authorization.simple.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class OrganizationalAuthorizationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AreaScopeDataAccessHandler.class)
    public AreaScopeDataAccessHandler areaScopeDataAccessHandler() {
        return new AreaScopeDataAccessHandler();
    }

    @Bean
    @ConditionalOnMissingBean(DepartmentScopeDataAccessHandler.class)
    public DepartmentScopeDataAccessHandler departmentScopeDataAccessHandler() {
        return new DepartmentScopeDataAccessHandler();
    }

    @Bean
    @ConditionalOnMissingBean(OrgScopeDataAccessHandler.class)
    public OrgScopeDataAccessHandler orgScopeDataAccessHandler() {
        return new OrgScopeDataAccessHandler();
    }

    @Bean
    @ConditionalOnMissingBean(PersonScopeDataAccessHandler.class)
    public PersonScopeDataAccessHandler personScopeDataAccessHandler() {
        return new PersonScopeDataAccessHandler();
    }

    @Bean
    @ConditionalOnMissingBean(PositionScopeDataAccessHandler.class)
    public PositionScopeDataAccessHandler positionScopeDataAccessHandler() {
        return new PositionScopeDataAccessHandler();
    }
}
