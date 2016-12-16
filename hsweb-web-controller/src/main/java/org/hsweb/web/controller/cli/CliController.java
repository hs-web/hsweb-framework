package org.hsweb.web.controller.cli;

import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.springframework.web.bind.annotation.*;

import static org.hsweb.web.core.message.ResponseMessage.*;

/**
 * @author zhouhao
 */
@RestController
@RequestMapping("/cli")
@AccessLogger("命令行工具")
@Authorize(module = "cli")
public class CliController {

    @RequestMapping(value = "/{language}", method = RequestMethod.POST)
    @AccessLogger("执行脚本")
    @Authorize(action = "exec")
    public ResponseMessage exec(@PathVariable String language,
                                @RequestBody String script) {
        return ok();
    }

    @RequestMapping(value = "/shell", method = RequestMethod.POST)
    @AccessLogger("执行Shell脚本")
    @Authorize(action = "exec")
    public ResponseMessage execShell(@RequestBody String shell, @RequestParam(required = false) String messageHolder) {

        return ok();
    }
}
