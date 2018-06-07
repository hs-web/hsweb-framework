package org.hswebframework.web.service.organizational;

import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.service.TreeService;

import java.util.List;

/**
 *  表单发布日志 服务类
 *
 * @author hsweb-generator-online
 */
public interface DistrictService extends TreeService<DistrictEntity, String>,CrudService<DistrictEntity, String> {
    void disable(String id);

    void enable(String id);

    DistrictEntity selectByCode(String code);
}
