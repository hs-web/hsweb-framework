package org.hsweb.web.socket.cmd;


import org.hsweb.web.socket.WebSocketSessionListener;

/**
 * 基于命令的wesocket执行器
 * Created by 浩 on 2015-09-08 0008.
 */
public interface CmdProcessor extends WebSocketSessionListener {
    /**
     * 获取命令名称
     *
     * @return 命令名称
     */
    String getName();

    /**
     * 执行命令
     *
     * @param cmd 要执行的命令
     * @return 执行结果
     * @throws Exception 异常
     */
    void exec(CMD cmd) throws Exception;


    /**
     * 初始化方法，用于自动注册命令等操作
     *
     * @throws Exception
     */
    void init() throws Exception;
}
