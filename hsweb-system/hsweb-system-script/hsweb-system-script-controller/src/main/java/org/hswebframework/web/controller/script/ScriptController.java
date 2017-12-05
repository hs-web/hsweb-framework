package org.hswebframework.web.controller.script;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.script.ScriptEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.script.ScriptExecutorService;
import org.hswebframework.web.service.script.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态脚本
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.script:script}")
@Authorize(permission = "script", description = "动态脚本管理")
@Api(value = "动态脚本",tags = "动态脚本管理")
public class ScriptController implements SimpleGenericEntityController<ScriptEntity, String, QueryParamEntity> {

    private ScriptService scriptService;

    private ScriptExecutorService scriptExecutorService;

    @Autowired
    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @Autowired
    public void setScriptExecutorService(ScriptExecutorService scriptExecutorService) {
        this.scriptExecutorService = scriptExecutorService;
    }

    @Override
    public ScriptService getService() {
        return scriptService;
    }


    @GetMapping("/{id}/execute")
    @ApiOperation("执行脚本")
    @Authorize(action = "execute", description = "执行脚本")
    public ResponseMessage<Object> executeForGet(@PathVariable String id, @RequestParam(required = false) Map<String, Object> parameters) throws Exception {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        Object result = scriptExecutorService.execute(id, parameters);
        return ResponseMessage.ok(result);
    }


    @RequestMapping(value = "/{id}/execute", method = {RequestMethod.POST, RequestMethod.PUT})
    @Authorize(action = "execute", description = "执行脚本")
    @ApiOperation("执行脚本")
    public ResponseMessage<Object> executeFotPostAndPut(@PathVariable String id,
                                                        @RequestBody(required = false) Map<String, Object> parameters) throws Exception {
        return ResponseMessage.ok(executeForGet(id, parameters));
    }
}
