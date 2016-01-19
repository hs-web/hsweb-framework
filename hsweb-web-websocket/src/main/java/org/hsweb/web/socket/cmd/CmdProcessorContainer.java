package org.hsweb.web.socket.cmd;

import java.util.List;

/**
 * cmd处理器容器，用于注册和获取命令处理器
 * Created by 浩 on 2016-01-19 0019.
 */
public interface CmdProcessorContainer {

    /**
     * 根据命令名获取一个命令处理器
     *
     * @param name 命令名称
     * @return 处理器实例，处理器不存在则返回null
     */
    CmdProcessor getCmdProcessor(String name);

    /**
     * 向容器中注册一个命令处理器
     *
     * @param processor 处理器实例
     * @return 注册后的处理器实例
     */
    CmdProcessor registerCmdProcessor(CmdProcessor processor);

    /**
     * 从容器中注销一个命令处理器
     *
     * @param name 要注销命令名称
     * @return 被注销的处理器实例
     */
    CmdProcessor cancelCmdProcessor(String name);

    /**
     * 获取所有命令处理器
     *
     * @return
     */
    List<CmdProcessor> getAll();

    /**
     * 容器初始化
     */
    void init();

}
