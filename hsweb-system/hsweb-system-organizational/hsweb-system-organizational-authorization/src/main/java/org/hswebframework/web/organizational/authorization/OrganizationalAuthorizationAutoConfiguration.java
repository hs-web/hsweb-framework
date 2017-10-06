package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.organizational.authorization.simple.*;
import org.hswebframework.web.organizational.authorization.simple.handler.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
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

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PersonnelAuthorizationSupplier) {
            PersonnelAuthorizationHolder.addSupplier(((PersonnelAuthorizationSupplier) bean));
        }
        return bean;
    }

    @Configuration
    @ConditionalOnBean(PersonnelAuthorizationManager.class)
    public static class PersonnelAuthorizationSupplierAutoConfiguration {
        @Bean
        public DefaultPersonnelAuthorizationSupplier personnelAuthorizationManager(PersonnelAuthorizationManager personnelAuthorizationManager) {
            return new DefaultPersonnelAuthorizationSupplier(personnelAuthorizationManager);
        }
    }
}
