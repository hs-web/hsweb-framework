package org.hswebframework.web.service.form.simple.validator;

import org.hswebframework.ezorm.core.Validator;
import org.hswebframework.ezorm.core.ValidatorFactory;
import org.hswebframework.ezorm.core.meta.ColumnMetaData;
import org.hswebframework.ezorm.core.meta.TableMetaData;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.proxy.Proxy;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class DynamicBeanValidatorFactory implements ValidatorFactory {

    private String createSetPropertyCode(TableMetaData tableMetaData) {
        StringBuilder builder = new StringBuilder();
        builder.append("public void setProperty(String property,Object value){");
        for (ColumnMetaData column : tableMetaData.getColumns()) {
            String propertyName = column.getAlias();
            Class type = column.getJavaType();
            builder.append("if(property.intern()==\"")
                    .append(propertyName)
                    .append("\"||property.intern()==\"")
                    .append(column.getName()).append("\"){\n")
                    .append("this.set")
                    .append(StringUtils.toUpperCaseFirstOne(propertyName))
                    .append("((").append(type.getName()).append(")")
                    .append("org.hswebframework.web.bean.FastBeanCopier.DEFAULT_CONVERT.convert(value,")
                    .append(type.getName())
                    .append(",null))")
                    .append("\n}");

        }
        return builder.toString();
    }

    @Override
    public Validator createValidator(TableMetaData tableMetaData) {
        StringBuilder builder = new StringBuilder();

        Proxy<MapBean> proxy = Proxy.create(MapBean.class);
        proxy.addField("private java.util.Map proxy;")
                .addMethod("public java.util.Map getProxy(){return this.proxy;};")
                .addMethod("public org.hswebframework.web.service.form.simple.validator.MapBean setProxy(java.util.Map proxy){ this.proxy=proxy; return this;};");


        for (ColumnMetaData column : tableMetaData.getColumns()) {
            String propertyName = column.getAlias();
            Class type = column.getJavaType();
            String typeName = type.getName();

            proxy.addField("private " + type.getName() + " " + propertyName + ";");
            proxy.addField("public void set " + StringUtils.toUpperCaseFirstOne(propertyName) + "(" + typeName + " " + propertyName + "){\n" +
                    "this." + propertyName + "=" + propertyName + ";\n" +
                    "\n};");
            proxy.addField("public " + typeName + " get " + StringUtils.toUpperCaseFirstOne(propertyName) + "(){\n" +
                    "return this." + propertyName + ";\n" +
                    "\n};");
        }
        proxy.addMethod(createSetPropertyCode(tableMetaData));

        return null;
    }
}
