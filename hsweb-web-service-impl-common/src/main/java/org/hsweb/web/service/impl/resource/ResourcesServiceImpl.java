package org.hsweb.web.service.impl.resource;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.resource.Resources;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.dao.resource.ResourcesMapper;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.resource.ResourcesService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 资源服务类
 * Created by generator
 */
@Service("resourcesService")
public class ResourcesServiceImpl extends AbstractServiceImpl<Resources, String> implements ResourcesService {
    public static final String CACHE_KEY = "resources";
    @Resource
    protected ConfigService configService;

    //默认数据映射接口
    @Resource
    protected ResourcesMapper resourcesMapper;

    @Override
    protected ResourcesMapper getMapper() {
        return this.resourcesMapper;
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'id.'+#id")
    @Transactional(readOnly = true)
    public Resources selectByPk(String id) {
        return super.selectByPk(id);
    }

    /**
     * 根据资源md5 查询资源信息
     *
     * @param md5 md5值
     * @return 资源对象
     * @throws Exception
     */
    @Cacheable(value = CACHE_KEY, key = "'md5.'+#md5")
    @Transactional(readOnly = true)
    public Resources selectByMd5(String md5)  {
        return this.selectSingle(new QueryParam().where("md5", md5));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public String insert(Resources data) {
        data.setId(this.newid(6));//6位随机id
        return super.insert(data);
    }

    public String newid(int len) {
        String id = RandomUtil.randomChar(len);
        for (int i = 0; i < 10; i++) {
            if (this.selectByPk(id) == null) {
                return id;
            }
        }  //如果10次存在重复则位数+1
        return newid(len + 1);
    }
}
