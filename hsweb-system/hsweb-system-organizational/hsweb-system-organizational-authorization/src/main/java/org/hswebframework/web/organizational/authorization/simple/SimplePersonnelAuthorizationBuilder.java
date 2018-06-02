package org.hswebframework.web.organizational.authorization.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.bean.BeanFactory;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.organizational.authorization.*;
import org.hswebframework.web.organizational.authorization.relation.Relation;
import org.hswebframework.web.organizational.authorization.relation.Relations;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelation;
import org.hswebframework.web.organizational.authorization.relation.SimpleRelations;

import java.util.Map;

public class SimplePersonnelAuthorizationBuilder {
   private static FastBeanCopier.DefaultConverter converter = new FastBeanCopier.DefaultConverter();

    static {
        converter.setBeanFactory(new BeanFactory() {
            @Override
            @SuppressWarnings("all")
            public <T> T newInstance(Class<T> targetClass) {
                if (targetClass == Position.class) {
                    return (T) new SimplePosition();
                }
                if (targetClass == Personnel.class) {
                    return (T) new SimplePersonnel();
                }
                if (targetClass == Department.class) {
                    return (T) new SimpleDepartment();
                }
                if (targetClass == Organization.class) {
                    return (T) new SimpleOrganization();
                }
                if (targetClass == District.class) {
                    return (T) new SimpleDistrict();
                }
                if (targetClass == Relation.class) {
                    return (T) new SimpleRelation();
                }
                if (targetClass == Relations.class) {
                    return (T) new SimpleRelations();
                }
                return FastBeanCopier.getBeanFactory().newInstance(targetClass);
            }
        });
    }

    public static SimplePersonnelAuthentication fromJson(String json) {
        return fromMap(JSON.parseObject(json));
    }

    public static SimplePersonnelAuthentication fromMap(Map<String,Object> map) {
        return FastBeanCopier.copy(map, new SimplePersonnelAuthentication(), converter);
    }

}
