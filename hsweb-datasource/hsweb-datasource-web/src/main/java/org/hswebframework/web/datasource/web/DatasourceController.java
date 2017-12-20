package org.hswebframework.web.datasource.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfig;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datasource")
@Api(tags = "开发人员工具-数据源", value = "数据源")
@Authorize(permission = "datasource", description = "数据源管理")
public class DatasourceController {

    @Autowired
    private DynamicDataSourceConfigRepository<? extends DynamicDataSourceConfig> repository;

    @GetMapping
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("获取全部数据源信息")
    public ResponseMessage<List<? extends DynamicDataSourceConfig>> getAllConfig() {
        return ResponseMessage.ok(repository.findAll());
    }

}
