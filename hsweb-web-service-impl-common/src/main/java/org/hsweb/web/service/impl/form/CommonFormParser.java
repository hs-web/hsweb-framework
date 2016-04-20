package org.hsweb.web.service.impl.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hsweb.web.bean.po.form.Form;
import org.springframework.util.ReflectionUtils;
import org.webbuilder.sql.FieldMetaData;
import org.webbuilder.sql.TableMetaData;
import org.webbuilder.utils.common.BeanUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-20.
 */
public class CommonFormParser implements FormParser {
    @Override
    public TableMetaData parse(Form form) {
        String meta = form.getMeta();
        TableMetaData metaData = new TableMetaData();
        metaData.setName(form.getName());
        metaData.setLocation(form.getU_id());
        metaData.setComment(form.getRemark());
        JSONObject object = JSON.parseObject(meta);
        object.forEach((id, field) -> {
            if ("main".equals(id)) return;
            FieldMetaData fieldMeta = new FieldMetaData();
            JSONArray obj = ((JSONArray) field);
            obj.forEach((defT) -> {
                JSONObject def = ((JSONObject) defT);
                String key = def.getString("key");
                Object value = def.get("value");
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
                            if (value.toString().trim().startsWith("[")
                                    || value.toString().trim().startsWith("{"))
                                value = castObj2JsonObject(value);
                        } catch (Throwable e) {
                        }
                    }
                    fieldMeta.attr(key, value);
                    metaData.addField(fieldMeta);
                }
            });
        });
        return metaData;
    }

    protected JSONObject castObj2JsonObject(Object object) {
        JSONObject obj = null;
        if (object instanceof JSONObject) {
            obj = ((JSONObject) object);
        } else if (object instanceof Map) {
            obj = new JSONObject(((Map) object));
        } else if (object instanceof String) {
            obj = JSON.parseObject(object.toString());
        }
        return obj;
    }

    public void validField(JSONObject field) {

    }

    public Class mapperJavaType(String str) {

        return null;
    }

    public static void main(String[] args) {

    }
}
