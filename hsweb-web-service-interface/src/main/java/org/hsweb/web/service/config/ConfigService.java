package org.hsweb.web.service.config;

import org.hsweb.web.bean.po.config.Config;
import org.hsweb.web.service.GenericService;

import java.util.Properties;

/**
 * 系统配置服务类
 * Created by generator
 */
public interface ConfigService extends GenericService<Config, String> {
    /**
     * 根据配置名称，获取配置内容
     *
     * @param name 配置名称
     * @return 配置内容
     * @throws Exception 异常信息
     */
    String getContent(String name) throws Exception;

    /**
     * 根据配置名称，获取配置内容，并解析为Properties格式
     *
     * @param name 配置名称
     * @return 配置内容
     * @throws Exception 异常信息
     */
    Properties get(String name) throws Exception;

    /**
     * 获取配置中指定key的值
     *
     * @param name 配置名称
     * @param key  key 异常信息
     * @return 指定的key对应的value
     * @throws Exception
     */
    String get(String name, String key) throws Exception;

    /**
     * 获取配置中指定key的值，并指定一个默认值，如果对应的key未获取到，则返回默认值
     *
     * @param name         配置名称
     * @param key          key 异常信息
     * @param defaultValue 默认值
     * @return 对应key的值，若为null，则返回默认值
     */
    String get(String name, String key, String defaultValue);


    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为int类型
     */
    int getInt(String name, String key) throws Exception;

    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为double类型
     */
    double getDouble(String name, String key) throws Exception;

    /**
     * 参照 {@link ConfigService#get(String, String)}，将值转为long类型
     */
    long getLong(String name, String key) throws Exception;

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为int类型
     */
    int getInt(String name, String key, int defaultValue);

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为double类型
     */
    double getDouble(String name, String key, double defaultValue);

    /**
     * 参照 {@link ConfigService#get(String, String, String)}，将值转为long类型
     */
    long getLong(String name, String key, long defaultValue);

}
