package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hsweb.ezorm.meta.FieldMetaData;
import org.hsweb.ezorm.meta.TableMetaData;
import org.hsweb.ezorm.meta.converter.ClobValueConverter;
import org.hsweb.ezorm.meta.converter.DateTimeConverter;
import org.hsweb.ezorm.run.simple.trigger.ScriptTraggerSupport;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.core.Install;
import org.hsweb.web.service.form.FormParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.webbuilder.utils.common.BeanUtils;
import org.webbuilder.utils.common.StringUtils;
import org.webbuilder.utils.script.engine.DynamicScriptEngine;
import org.webbuilder.utils.script.engine.DynamicScriptEngineFactory;

import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.*;

/**
 * Created by zhouhao on 16-4-20.
 */
@Service
public class DefaultFormParser implements FormParser {

    @Autowired(required = false)
    private List<FormParser.Listener> listeners;

    public void initField(FieldMetaData fieldMetaData) {
        if (fieldMetaData.getComment() == null)
            fieldMetaData.setComment("");
        String db = Install.getDatabaseType();
        if (fieldMetaData.getDataType() == null) {
            JDBCType jdbcType = fieldMetaData.getJdbcType();
            if (jdbcType == null) throw new UnsupportedOperationException("请指定jdbcType或者dataType");
            switch (jdbcType) {
                case VARCHAR:
                    String len = fieldMetaData.getProperty("data-type-len", "256").toString();
                    if (db.equals("mysql")) {
                        fieldMetaData.setDataType("varchar(" + len + ")");
                    } else if (db.equals("oracle") || db.equals("h2")) {
                        fieldMetaData.setDataType("varchar2(" + len + ")");
                    }
                    break;
                case TINYINT:
                    if (db.equals("mysql")) {
                        fieldMetaData.setDataType("tinyint");
                    } else if (db.equals("oracle") || db.equals("h2")) {
                        fieldMetaData.setDataType("number(10)");
                    }
                case NUMERIC:
                    len = fieldMetaData.getProperty("data-type-len", "32").toString();
                    if (db.equals("mysql")) {
                        fieldMetaData.setDataType((len.contains(",") ? "double" : "int") + "(" + len + ")");
                    } else if (db.equals("oracle") || db.equals("h2")) {
                        fieldMetaData.setDataType("number(" + len + ")");
                    }
                case DATE:
                    if (db.equals("mysql")) {
                        fieldMetaData.setDataType("datetime");
                    } else if (db.equals("oracle") || db.equals("h2")) {
                        fieldMetaData.setDataType("date");
                    }
                case CLOB:
                    if (db.equals("mysql")) {
                        fieldMetaData.setDataType("text");
                    } else if (db.equals("oracle") || db.equals("h2")) {
                        fieldMetaData.setDataType("clob");
                    }
            }
        }
        if (fieldMetaData.getJdbcType() == null) {
            String dataType = fieldMetaData.getDataType();
            if (dataType != null) {
                if (dataType.contains("varchar")) {
                    fieldMetaData.setJdbcType(JDBCType.VARCHAR);
                } else if (dataType.contains("date")
                        || dataType.contains("timestamp")
                        || dataType.contains("datetime")) {
                    fieldMetaData.setJdbcType(JDBCType.DATE);
                    String format = fieldMetaData.getProperty("date-format", "yyyy-MM-dd HH:mm:ss").toString();
                    fieldMetaData.setValueConverter(new DateTimeConverter(format, fieldMetaData.getJavaType()));
                } else if (dataType.contains("clob")) {
                    fieldMetaData.setJdbcType(JDBCType.CLOB);
                    fieldMetaData.setValueConverter(new ClobValueConverter());
                } else if (dataType.contains("number") ||
                        dataType.contains("int") ||
                        dataType.contains("double") ||
                        dataType.contains("tinyint")) {
                    fieldMetaData.setJdbcType(JDBCType.NUMERIC);
                } else {
                    fieldMetaData.setJdbcType(JDBCType.VARCHAR);
                }
            }
        }
    }

    @Override
    public TableMetaData parse(Form form) {
        DynamicScriptEngine scriptEngine = DynamicScriptEngineFactory.getEngine("groovy");
        String meta = form.getMeta();
        TableMetaData metaData = new TableMetaData();
        metaData.setName(form.getName());
        metaData.setComment(form.getRemark());
        JSONObject object = JSON.parseObject(meta);
        object.forEach((id, field) -> {
            FieldMetaData fieldMeta = new FieldMetaData();
            fieldMeta.setProperty("field-id", id);
            JSONArray obj = ((JSONArray) field);
            obj.forEach((defT) -> {
                JSONObject def = ((JSONObject) defT);
                String key = def.getString("key");
                Object value = def.get("value");
                if ("main".equals(id)) {
                    if ("alias".equals(key)) {
                        if (!StringUtils.isNullOrEmpty(value))
                            metaData.setAlias(String.valueOf(value));
                    } else if ("trigger".equals(key)) {
                        List<JSONObject> jsonArray = JSON.parseArray((String) value, JSONObject.class);
                        jsonArray.forEach(jsonObject -> {
                            String name = jsonObject.getString("key");
                            String script = jsonObject.getString("value");
                            String scriptId = String.valueOf(script.hashCode());
                            if (!scriptEngine.compiled(scriptId)) {
                                try {
                                    scriptEngine.compile(scriptId, script);
                                } catch (Exception e) {
                                    throw new RuntimeException("编译脚本异常", e);
                                }
                            }
                            ScriptTraggerSupport scriptTrigger = new ScriptTraggerSupport(scriptEngine, scriptId);
                            metaData.on(name, scriptTrigger);
                        });
                        return;
                    } else
                        metaData.setProperty(key, value);
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
                    fieldMeta.setProperty(key, value);
                }
            });
            //name为空的时候 不保持此字段
            if (!"main".equals(id) && !StringUtils.isNullOrEmpty(fieldMeta.getName())) {
                initField(fieldMeta);
                if(StringUtils.isNullOrEmpty(fieldMeta.getAlias()))
                    fieldMeta.setAlias(fieldMeta.getName());
                metaData.addField(fieldMeta);
            }
        });
        if (listeners != null) {
            listeners.forEach(listener -> listener.afterParse(metaData));
        }
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
            String field_id = field.getProperty("field-id", "").toString();
            if (!"".equals(field_id)) {
                Elements elements = html.select("[field-id=\"" + field_id + "\"]");
                Element input = elements.first();
                if (null != input) {
                    List<Map> domProperty = field.getProperty("domProperty", "[]").toList();
                    domProperty.forEach((property) -> {
                        Object value = property.get("value");
                        Object key = property.get("key");
                        if (StringUtils.isNullOrEmpty(value) || StringUtils.isNullOrEmpty(key)) return;
                        input.attr(String.valueOf(property.get("key")), String.valueOf(value));
                    });
                    input.attr("name", field.getName());
                    input.attr("id", field.getName());
                    input.attr("class", field.getProperty("class").toString());
                }
            }
        });
        return html.toString();
    }
}
