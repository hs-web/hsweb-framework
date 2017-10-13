package org.hswebframework.web.service.template;

import org.hswebframework.web.entity.template.TemplateEntity;

/**
 * 模板渲染器工厂
 *
 * @author zhouhao
 * @since 3.0
 */
public interface TemplateRendeFacotry {
    boolean isSupport(String type);

    TemplateRender create(TemplateEntity templateEntity);
}
