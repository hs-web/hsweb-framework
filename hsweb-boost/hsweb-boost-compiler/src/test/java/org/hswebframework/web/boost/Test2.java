package org.hswebframework.web.boost;

import org.hswebframework.web.boost.bean.Converter;
import org.hswebframework.web.boost.bean.Copier;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
public class Test2 implements Copier{
    public void copy(Object s, Object t, java.util.Set ignore, org.hswebframework.web.boost.bean.Converter converter){
        org.hswebframework.web.boost.Source source=(org.hswebframework.web.boost.Source)s;
        org.hswebframework.web.boost.Target target=(org.hswebframework.web.boost.Target)t;
        if(!ignore.contains("name")&&source.getName()!=null){
            java.lang.String name=source.getName();
            target.setName(name);
        }
        if(!ignore.contains("nestObject")&&source.getNestObject()!=null){
            org.hswebframework.web.boost.NestObject nestObject=source.getNestObject();
            target.setNestObject(nestObject);
        }

        if(!ignore.contains("age")){
            int age=source.getAge();
            target.setAge(age);
        }
    }
}
