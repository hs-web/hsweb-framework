package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hibernate.validator.constraints.Length;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.service.impl.form.trigger.ScriptTrigger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.webbuilder.sql.FieldMetaData;
import org.webbuilder.sql.TableMetaData;
import org.webbuilder.sql.trigger.ScriptTriggerSupport;
import org.webbuilder.utils.common.BeanUtils;
import org.webbuilder.utils.common.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by zhouhao on 16-4-20.
 */
@Service
public class DefaultFormParser implements FormParser {

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Override
    public TableMetaData parse(Form form) {
        String meta = form.getMeta();
        TableMetaData metaData = new TableMetaData();
        metaData.setName(form.getName());
        metaData.setLocation(form.getId());
        metaData.setComment(form.getRemark());
        JSONObject object = JSON.parseObject(meta);
        object.forEach((id, field) -> {
            FieldMetaData fieldMeta = new FieldMetaData();
            fieldMeta.attr("field-id", id);
            JSONArray obj = ((JSONArray) field);
            obj.forEach((defT) -> {
                JSONObject def = ((JSONObject) defT);
                String key = def.getString("key");
                Object value = def.get("value");
                if ("main".equals(id)) {
                    metaData.attr(key, value);
                    return;
                }
                if ("validator-list".equals(key)) {
                    Set<String> validatorList = new LinkedHashSet<>();
                    if (value instanceof String) {
                        List<JSONObject> jsonArray = JSON.parseArray((String) value, JSONObject.class);
                        jsonArray.forEach(json -> {
                            String validator = json.getString("validator");
                            if (validatorSupport(validator))
                                validatorList.add(validator);
                        });
                    }
                    fieldMeta.setValidator(validatorList);
                    return;
                }
                if ("trigger".equals(key)) {
                    List<JSONObject> jsonArray = JSON.parseArray((String) value, JSONObject.class);
                    jsonArray.forEach(jsonObject -> {
                        String name = jsonObject.getString("key");
                        String script = jsonObject.getString("value");
                        ScriptTrigger scriptTrigger = new ScriptTrigger();
                        scriptTrigger.setId(String.valueOf(script.hashCode()));
                        if (expressionScopeBeanMap != null)
                            scriptTrigger.setDefaultVar(expressionScopeBeanMap);
                        scriptTrigger.setName(name);
                        scriptTrigger.setContent(script);
                        scriptTrigger.setLanguage("groovy");
                    });
                    return;
                }
                Field ftmp = ReflectionUtils.findField(FieldMetaData.class, key);
                if (ftmp != null) {
                    try {
                        if ("javaType".equals(key)) value = mapperJavaType(value.toString());
                        BeanUtils.attr(fieldMeta, key, value);
                    } catch (Exception e) {
                    }
                } else {
                    if (value instanceof String) {
                        try {
                            String stringValue = ((String) value).trim();
                            if (stringValue.startsWith("[")
                                    || stringValue.startsWith("{"))
                                value = JSON.parse(stringValue);
                        } catch (Throwable e) {
                        }
                    }
                    fieldMeta.attr(key, value);
                }
            });
            //name为空的时候 不保持此字段
            if (!"main".equals(id) && !StringUtils.isNullOrEmpty(fieldMeta.getName())) {
                metaData.addField(fieldMeta);
            }
        });
        return metaData;
    }

    protected boolean validatorSupport(String validator) {
        return !StringUtils.isNullOrEmpty(validator);
    }

    protected static Map<String, Class> typeMapper = new HashMap() {{
        put("", String.class);
        put("null", String.class);
        put("string", String.class);
        put("String", String.class);
        put("str", String.class);
        put("int", Integer.class);
        put("double", Double.class);
        put("boolean", Boolean.class);
        put("byte", Byte.class);
        put("char", Character.class);
        put("float", Double.class);
        put("date", Date.class);
        put("Date", Date.class);
    }};

    public Class mapperJavaType(String str) {
        Class clazz = typeMapper.get(str);
        if (clazz == null)
            try {
                clazz = Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        return clazz;
    }

    @Override
    public String parseHtml(Form form) {
        TableMetaData metaData = parse(form);
        Element html = Jsoup.parse(form.getHtml());
        metaData.getFields().forEach((field) -> {
            String field_id = field.attrWrapper("field-id", "").toString();
            if (!"".equals(field_id)) {
                Elements elements = html.select("[field-id=\"" + field_id + "\"]");
                Element input = elements.first();
                if (null != input) {
                    List<Map> domProperty = field.attrWrapper("domProperty", "[]").toList();
                    domProperty.forEach((property) -> {
                        Object value = property.get("value");
                        Object key = property.get("key");
                        if (StringUtils.isNullOrEmpty(value) || StringUtils.isNullOrEmpty(key)) return;
                        input.attr(String.valueOf(property.get("key")), String.valueOf(value));
                    });
                    input.attr("name", field.getName());
                    input.attr("id", field.getName());
                    input.attr("class", field.attrWrapper("class").toString());
                }
            }
        });
        return html.toString();
    }
}
