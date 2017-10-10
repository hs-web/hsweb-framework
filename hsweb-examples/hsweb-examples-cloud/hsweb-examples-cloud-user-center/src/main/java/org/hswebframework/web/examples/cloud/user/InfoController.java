package org.hswebframework.web.examples.cloud.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@RestController
public class InfoController {

    @GetMapping("/info")
    public String info() {
        return "success";
    }
}
