package org.hsweb.web.core;

import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.core.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@ComponentScan("org.hsweb.web.core")
public class CoreAutoConfiguration {

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap = new HashMap<>();

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String initializeScript = "classpath*:scripts/startup/*.";

    @PostConstruct
    public void init() {
        initScript();
    }

    private void initScript() {
        Map<String, Object> vars = new HashMap<>(expressionScopeBeanMap);
        vars.put("LoginUser", (Supplier) () -> WebUtil.getLoginUser());
        vars.put("StringUtils", StringUtils.class);
        vars.put("User", User.class);

        initScript("js", vars);
        initScript("groovy", vars);
        initScript("java", vars);
        initScript("spel", vars);
        initScript("ognl", vars);
        initScript("ruby", vars);
        initScript("python", vars);
        //执行脚本
    }

    private void initScript(String language, Map<String, Object> vars) {
        try {
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(language);
            if (engine == null) return;
            vars.put("logger", LoggerFactory.getLogger("org.hsweb.script.".concat(language)));
            vars.put("scriptEngine", engine);
            engine.addGlobalVariable(vars);
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(initializeScript.concat(language));
            for (Resource resource : resources) {
                String script = StreamUtils.copyToString(resource.getInputStream(), Charset.forName("utf-8"));
                engine.compile("__tmp", script);
                try {
                    engine.execute("__tmp");
                } finally {
                    engine.remove("__tmp");
                }
            }
        } catch (NullPointerException e) {
            //
        } catch (IOException e) {
            logger.error("读取脚本文件失败", e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
