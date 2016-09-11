package org.hsweb.web.service.script;

import org.hsweb.web.bean.po.script.DynamicScript;
import org.hsweb.web.service.GenericService;

import java.util.Map;

/**
 * 动态脚本服务类
 * Created by generator
 */
public interface DynamicScriptService extends GenericService<DynamicScript, String> {
    /**
     * 编译一个脚本
     *
     * @param id 要编译脚本的ID
     * @throws Exception 编译异常
     */
    void compile(String id) throws Exception;

    /**
     * 编译所有脚本
     *
     * @throws Exception 编译异常
     */
    void compileAll() throws Exception;

    String getScriptMd5(String scriptId) throws Exception;

    DynamicScript selectByNameAndType(String name, String type) throws Exception;
}
