package org.hswebframework.web.datasource.web;

import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfig;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datasource")
@Resource(id = "datasource", name = "数据源管理")
public class DatasourceController {

    @Autowired
    private DynamicDataSourceConfigRepository<? extends DynamicDataSourceConfig> repository;

    @GetMapping
    @QueryAction
    public List<? extends DynamicDataSourceConfig> getAllConfig() {
        return repository.findAll();
    }

}
