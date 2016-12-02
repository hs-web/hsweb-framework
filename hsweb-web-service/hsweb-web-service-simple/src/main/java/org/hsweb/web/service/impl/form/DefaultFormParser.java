package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.ezorm.rdb.meta.converter.ClobValueConverter;
import org.hsweb.ezorm.rdb.meta.converter.DateTimeConverter;
import org.hsweb.ezorm.rdb.meta.converter.JSONValueConverter;
import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hsweb.ezorm.rdb.simple.trigger.ScriptTraggerSupport;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.service.form.FormParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.*;

@Service
public class DefaultFormParser implements FormParser {

    @Autowired(required = false)
    private List<FormParser.Listener> listeners;

    public void initField(RDBColumnMetaData column) {
        Dialect dialect = DataSourceHolder.getActiveDatabaseType().getDialect();
        if (column.getDataType() == null) {
            if (column.getJdbcType() == null) {
                throw new UnsupportedOperationException("请指定jdbcType或者dataType");
            }
            dialect.buildDataType(column);
        }
        if (column.getJdbcType() == null) {
            String dataType = column.getDataType();
            if (dataType != null) {
                if (dataType.contains("varchar")) {
                    column.setJdbcType(JDBCType.VARCHAR);
                    String className = column.getJavaType().getSimpleName();
                    if (!typeMapper.containsKey(className)) {
                        column.setValueConverter(new JSONValueConverter(column.getJavaType(), column.getValueConverter()));
                    }
                } else if (dataType.contains("date")
                        || dataType.contains("timestamp")
                        || dataType.contains("datetime")) {
                    column.setJdbcType(JDBCType.DATE);
                    String format = column.getProperty("date-format", "yyyy-MM-dd HH:mm:ss").toString();
                    column.setValueConverter(new DateTimeConverter(format, column.getJavaType()));
                } else if (dataType.contains("clob")) {
                    column.setJdbcType(JDBCType.CLOB);
                    column.setValueConverter(new ClobValueConverter());
                    String className = column.getJavaType().getSimpleName();
                    if (!typeMapper.containsKey(className)) {
                        column.setValueConverter(new JSONValueConverter(column.getJavaType(), column.getValueConverter()));
                    }
                } else if (dataType.contains("number") ||
                        dataType.contains("int") ||
                        dataType.contains("double") ||
                        dataType.contains("tinyint")) {
                    column.setJdbcType(JDBCType.NUMERIC);
                } else {
                    column.setJdbcType(JDBCType.VARCHAR);
                    String className = column.getJavaType().getSimpleName();
                    if (!typeMapper.containsKey(className)) {
                        column.setValueConverter(new JSONValueConverter(column.getJavaType(), column.getValueConverter()));
                    }
                }
            }
        }
    }

    @Override
    public RDBTableMetaData parse(Form form) {
        DynamicScriptEngine scriptEngine = DynamicScriptEngineFactory.getEngine("groovy");
        String meta = form.getMeta();
        RDBTableMetaData metaData = new RDBTableMetaData();
        metaData.setProperty("version", form.getRelease());
        metaData.setName(form.getName());
        metaData.setComment(form.getRemark());
        JSONObject object = JSON.parseObject(meta);
        int[] sortIndex = new int[1];
        Map<String, RDBColumnMetaData> tmp = new HashMap<>();
        object.forEach((id, field) -> {
            RDBColumnMetaData columnMetaData = new RDBColumnMetaData();
            columnMetaData.setProperty("field-id", id);
            tmp.put(id, columnMetaData);
            columnMetaData.setSortIndex(sortIndex[0]++);
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
                    columnMetaData.setValidator(validatorList);
                    return;
                }
                Field ftmp = ReflectionUtils.findField(RDBColumnMetaData.class, key);
                if (ftmp != null) {
                    try {
                        if ("javaType".equals(key)) value = mapperJavaType(value.toString());
                        BeanUtils.setProperty(columnMetaData, key, value);
                    } catch (RuntimeException e) {
                        throw e;
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
                    columnMetaData.setProperty(key, value);
                }
            });
            //name为空的时候 不保存此字段
            if (!"main".equals(id)
                    && !StringUtils.isNullOrEmpty(columnMetaData.getName())) {
                initField(columnMetaData);
                if (StringUtils.isNullOrEmpty(columnMetaData.getAlias()))
                    columnMetaData.setAlias(columnMetaData.getName());
                metaData.addColumn(columnMetaData);
            }
        });
        if (listeners != null) {
            listeners.forEach(listener -> listener.afterParse(metaData));
        }
        Document document = Jsoup.parse(form.getHtml());
        Elements elements = document.select("[field-id]");
        for (int i = 0; i < elements.size(); i++) {
            RDBColumnMetaData column = tmp.get(elements.get(i).attr("field-id"));
            if (column != null) column.setSortIndex(i);
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
        put("long", Long.class);
        put("short", Short.class);
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
        RDBTableMetaData metaData = parse(form);
        Element html = Jsoup.parse(form.getHtml());
        metaData.getColumns().forEach((field) -> {
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
                    input.attr("name", field.getAlias());
                    input.attr("id", field.getAlias());
                    input.attr("class", field.getProperty("class").toString());
                }
            }
        });
        return html.toString();
    }
}
