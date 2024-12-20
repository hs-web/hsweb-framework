package org.hswebframework.web.api.crud.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.Extensible;

import java.util.Collections;
import java.util.Map;

/**
 * 可扩展的实体类
 * <p>
 * <ul>
 *     <li>
 *         实体类继承此类,或者实现{@link Extensible}接口.
 *     </li>
 *     <li>
 *         使用{@link org.hswebframework.web.crud.configuration.TableMetadataCustomizer}自定义表结构
 *     </li>
 *     <li>
 *         json序列化时,默认会将拓展字段平铺到json中.
 *     </li>
 * </ul>
 *
 * @param <PK> 主键类型
 * @see JsonAnySetter
 * @see JsonAnyGetter
 * @since 4.0.18
 */
@Getter
@Setter
public class ExtensibleEntity<PK> extends GenericEntity<PK> implements Extensible {

    private Map<String, Object> extensions;

    /**
     * 默认不序列化扩展属性,会由{@link ExtensibleEntity#extensions()},{@link JsonAnyGetter}平铺到json中.
     *
     * @return 扩展属性
     */
    @JsonIgnore
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> extensions() {
        return extensions == null ? Collections.emptyMap() : extensions;
    }

    @Override
    public Object getExtension(String property) {
        Map<String, Object> ext = this.extensions;
        return ext == null ? null : ext.get(property);
    }

    @Override
    @JsonAnySetter
    public synchronized void setExtension(String property, Object value) {
        if (extensions == null) {
            extensions = new java.util.HashMap<>();
        }
        extensions.put(property, value);
    }
}
