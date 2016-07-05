package org.hsweb.web.controller.config;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.config.Config;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器，继承自GenericController,使用rest+json。
 * 此功能将传配置文件放到数据库和缓存中，可动态修改配置。
 * Created by generator 2015-8-17 11:16:45
 */
@RestController
@RequestMapping(value = "/config")
@AccessLogger("配置管理")
@Authorize
public class ConfigController extends GenericController<Config, String> {
    private static final String CACHE_KEY = "config";

    //默认服务类
    @Autowired
    private ConfigService configService;

    @Override
    public ConfigService getService() {
        return this.configService;
    }

    /**
     * 批量获取缓存，如传入["core.system.version","upload.path"] 将获取core中的system.version属性和upload中的path属性
     * <br/>并返回结果如: {"core":{"system.version":"1.0"},"upload":{"path":"/files"}}
     *
     * @param ids 请求获取的配置列表
     * @return 配置内容
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @Cacheable(value = CACHE_KEY, key = "'info_list'+#ids.hashCode()")
    @AccessLogger("批量获取配置")
    public Object batch(@RequestParam(value = "ids", defaultValue = "[]") String ids, boolean map) {
        List<String> requestData = JSON.parseArray(ids, String.class);
        //获取缓存里的配置
        Map<String, Object> config = new LinkedHashMap<>();
        //临时缓存，用于当进行如: cfg.name,cfg.data,cfg.other。等获取时，cfg只获取一次，提升效率
        Map<String, Map<String, String>> temp = new LinkedHashMap<>();
        for (String request : requestData) {
            Config conf = null;
            try {
                conf = configService.selectByPk(request);
            } catch (Exception e) {
            }
            //如果包含[.]，则代表是获取当个配置属性。如: core.system.version,将获取core配置中的system.version属性
            if (conf == null && request.contains(".")) {
                String[] res = request.split("[.]", 2);
                String name = res[0]; //如: core
                String key = res[1]; //如: system.version
                Map cache;
                //获取临时缓存中的配置
                if ((cache = temp.get(name)) == null) {
                    try {
                        conf = configService.selectByPk(name);
                        cache = conf.toMap();
                    } catch (Exception e) {
                    }
                    if (cache == null) {
                        config.put(request, new LinkedHashMap<>());
                        continue;
                    }
                    temp.put(name, cache);
                }
                Map<String, Object> tmp = (Map) config.get(name);
                if (tmp != null) {
                    tmp.put(key, cache.get(key));
                } else {
                    tmp = new LinkedHashMap<>();
                    tmp.put(key, cache.get(key));
                    config.put(name, tmp);
                }
            } else {
                //获取完整配置
                if (conf != null) {
                    config.put(request, map ? conf.toMap() : conf.toList());
                }
            }
        }
        temp.clear();
        return config;
    }

    /**
     * 获取一个配置的完整内容
     *
     * @param name 配置名称
     * @return 配置内容
     */
    @RequestMapping(value = "/{name:.+}.map", method = RequestMethod.GET)
    @AccessLogger("根据配置名获取配置（map格式）")
    public Object configInfo(@PathVariable("name") String name)  {
        return configService.get(name);
    }

    @RequestMapping(value = "/{name:.+}.array", method = RequestMethod.GET)
    @AccessLogger("根据配置名获取配置(list格式)")
    public Object listInfo(@PathVariable("name") String name)  {
        String content = configService.getContent(name);
        if (content == null) content = "[]";
        return content;
    }

    /**
     * 获取一个配置中的某个属性
     *
     * @param name 配置名称
     * @param key  配置属性，支持.，如 system.version
     * @return 配置内容
     */
    @RequestMapping(value = {"/{name:.+}/{key:.+}"}, method = RequestMethod.GET)
    @AccessLogger("根据配置名和键获取配置")
    public Object configInfo(@PathVariable("name") String name, @PathVariable("key") String key)  {
        return configService.get(name, key, "");
    }

    @Override
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    public ResponseMessage info(@PathVariable("id") String id)  {
        return super.info(id);
    }

    @Override
    @Authorize(module = "config", action = "C")
    public ResponseMessage add(@RequestBody Config object)  {
        return super.add(object);
    }

    @Override
    @Authorize(module = "config", action = "D")
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
    public ResponseMessage delete(@PathVariable("id") String id)  {
        return super.delete(id);
    }

    @Override
    @Authorize(module = "config", action = "U")
    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PUT)
    public ResponseMessage update(@PathVariable("id") String id, @RequestBody Config object)  {
        return super.update(id, object);
    }
}
