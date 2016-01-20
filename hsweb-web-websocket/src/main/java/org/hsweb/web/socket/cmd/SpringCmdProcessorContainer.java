package org.hsweb.web.socket.cmd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * spring管理的处理器容器
 * Created by 浩 on 2016-01-19 0019.
 */
@Component
public class SpringCmdProcessorContainer implements CmdProcessorContainer {

    /**
     * 命令缓存
     */
    private Map<String, CmdProcessor> cache = new HashMap<>();

    /**
     * spring容器
     */
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public CmdProcessor getCmdProcessor(String name) {
        return cache.get(name);
    }

    @Override
    public CmdProcessor registerCmdProcessor(CmdProcessor processor) {
        return cache.put(processor.getName(), processor);
    }

    @Override
    public CmdProcessor cancelCmdProcessor(String name) {
        return cache.remove(name);
    }

    @Override
    public List<CmdProcessor> getAll() {
        return new ArrayList<>(cache.values());
    }

    /**
     * 通过获取spring管理的bean，向容器注册处理器
     */
    @Override
    @PostConstruct
    public void init() {
        Map<String, CmdProcessor> processorMap = applicationContext.getBeansOfType(CmdProcessor.class);
        if (processorMap != null) {
            for (CmdProcessor cmdProcessor : processorMap.values()) {
                registerCmdProcessor(cmdProcessor);
            }
        }
    }
}
