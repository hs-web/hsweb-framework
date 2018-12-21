package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.organizational.authorization.relation.RelationTargetSupplierAutoRegister;
import org.hswebframework.web.organizational.authorization.simple.*;
import org.hswebframework.web.organizational.authorization.simple.handler.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
public class OrganizationalAuthorizationAutoConfiguration implements BeanPostProcessor {

    @Bean
    @ConditionalOnMissingBean(DistrictScopeDataAccessHandler.class)
    public DistrictScopeDataAccessHandler areaScopeDataAccessHandler() {
        return new DistrictScopeDataAccessHandler();
    }


    @Bean
    @ConditionalOnMissingBean(CustomScopeHandler.class)
    public CustomScopeHandler customScopeHandler() {
        return new CustomScopeHandler();
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

    @Bean
    @ConditionalOnMissingBean(ScopeDataAccessConfigConvert.class)
    public ScopeDataAccessConfigConvert scopeDataAccessConfigConvert() {
        return new ScopeDataAccessConfigConvert();
    }

    @Bean
    @ConditionalOnMissingBean(CustomScopeDataAccessConfigConvert.class)
    public CustomScopeDataAccessConfigConvert customScopeDataAccessConfigConvert() {
        return new CustomScopeDataAccessConfigConvert();
    }

    @Bean
    @ConditionalOnMissingBean(ScopeByUserDataAccessConfigConvert.class)
    public ScopeByUserDataAccessConfigConvert scopeByUserDataAccessConfigConvert() {
        return new ScopeByUserDataAccessConfigConvert();
    }

    @Bean
    @ConditionalOnMissingBean(ScopeByUserHandler.class)
    public ScopeByUserHandler scopeByUserHandler() {
        return new ScopeByUserHandler();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PersonnelAuthenticationSupplier) {
            PersonnelAuthenticationHolder.addSupplier(((PersonnelAuthenticationSupplier) bean));
        }
        return bean;
    }

    @Bean
    public RelationTargetSupplierAutoRegister relationTargetSupplierAutoRegister() {
        return new RelationTargetSupplierAutoRegister();
    }

    @Configuration
    @ConditionalOnBean(PersonnelAuthenticationManager.class)
    public static class PersonnelAuthorizationSupplierAutoConfiguration {

        @Bean
        public DefaultPersonnelAuthenticationSupplier personnelAuthorizationManager(PersonnelAuthenticationManager personnelAuthenticationManager) {
            return new DefaultPersonnelAuthenticationSupplier(personnelAuthenticationManager);
        }

        @Bean
        public PersonnelAuthenticationSettingTypeSupplier personnelAuthorizationSettingTypeSupplier() {
            return new PersonnelAuthenticationSettingTypeSupplier();
        }
    }
}
