package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.CMD;
import org.hsweb.web.socket.cmd.CmdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
    }

    @Override
    @PostConstruct
    public void init() throws Exception {
        logger.info("init TestProcessor");
    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {
    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {

    }
}
