package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.CMD;
import org.hsweb.web.socket.cmd.CmdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;

/**
 * 测试命令处理器
 * Created by 浩 on 2016-01-19 0019.
 */
@Component
public class TestProcessor implements CmdProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void exec(CMD cmd) throws Exception {
        logger.info("execute cmd :" + cmd);
        //收到命令后，向客户端推送一条消息
        cmd.getSession().sendMessage(new TextMessage("你好!"));
    }

    @Override
    @PostConstruct
    public void init() throws Exception {
        logger.info("init TestProcessor");
    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {
        logger.info("小伙伴进来了");
        session.sendMessage(new TextMessage("命令:" + getName() + " , 作用:测试"));
    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {
        logger.info("小伙伴离开了");
    }
}
