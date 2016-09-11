package org.hsweb.web.service.impl.config;

import org.hsweb.commons.StringUtils;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.config.Config;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.dao.config.ConfigMapper;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务类
 * Created by generator
 */
@Service("configService")
public class ConfigServiceImpl extends AbstractServiceImpl<Config, String> implements ConfigService,ExpressionScopeBean {

    public static final String CACHE_KEY = "config";

    //默认数据映射接口
    @Resource
    protected ConfigMapper configMapper;

    @Override
    protected ConfigMapper getMapper() {
        return this.configMapper;
    }

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public int update(Config data) {
        return configMapper.update(new UpdateParam<>(data).excludes("createDate").where("id",data.getId()));
    }

    @Override
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public int delete(String s) {
        return super.delete(s);
    }

    /**
     * 根据配置名称，获取配置内容
     *
     * @param name 配置名称
     * @return 配置内容
     * @异常信息
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.content.'+#name")
    public String getContent(String name) {
        Config config = getMapper().selectByPk(name);
        if (config == null) return null;
        return config.getContent();
    }

    /**
     * 根据配置名称，获取配置内容，并解析为Properties格式
     *
     * @param name 配置名称
     * @return 配置内容
     * @异常信息
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name")
    public Map<Object, Object> get(String name) {
        Config config = getMapper().selectByPk(name);
        if (config == null) return new HashMap<>();
        return config.toMap();
    }

    /**
     * 获取配置中指定key的值
     *
     * @param name 配置名称
     * @param key  key 异常信息
     * @return 指定的key对应的value
     * @throws Exception
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key")
    public String get(String name, String key) {
        Object val = get(name).get(key);
        if (val == null) return null;
        return String.valueOf(val);
    }

    /**
     * 获取配置中指定key的值，并指定一个默认值，如果对应的key未获取到，则返回默认值
     *
     * @param name         配置名称
     * @param key          key 异常信息
     * @param defaultValue 默认值
     * @return 对应key的值，若为null，则返回默认值
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key")
    public String get(String name, String key, String defaultValue) {
        String val;
        try {
            val = this.get(name, key);
            if (val == null) {
                logger.error("获取配置:{}.{}失败,defaultValue:{}", name, key, defaultValue);
                return defaultValue;
            }
        } catch (Exception e) {
            logger.error("获取配置:{}.{}失败,defaultValue:{}", name, key, defaultValue, e);
            return defaultValue;
        }
        return val;
    }


    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为int类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.int'")
    public int getInt(String name, String key) {
        return StringUtils.toInt(get(name, key));
    }

    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为double类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.double'")
    public double getDouble(String name, String key) {
        return StringUtils.toDouble(get(name, key));
    }

    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为long类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.long'")
    public long getLong(String name, String key) {
        return StringUtils.toLong(get(name, key));
    }

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为int类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.int'")
    public int getInt(String name, String key, int defaultValue) {
        return StringUtils.toInt(get(name, key, String.valueOf(defaultValue)));
    }

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为double类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.double'")
    public double getDouble(String name, String key, double defaultValue) {

        return StringUtils.toDouble(get(name, key, String.valueOf(defaultValue)));
    }

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为long类型
     */
    @Override
    @Cacheable(value = CACHE_KEY, key = "'info.'+#name+'.key.'+#key+'.long'")
    public long getLong(String name, String key, long defaultValue) {
        return StringUtils.toLong(get(name, key, String.valueOf(defaultValue)));
    }


    @Override
    public String insert(Config data) {
        Config old = this.selectByPk(data.getId());
        Assert.isNull(old, "配置已存在，请勿重复添加!");
        data.setCreateDate(new Date());
        return super.insert(data);
    }
}
