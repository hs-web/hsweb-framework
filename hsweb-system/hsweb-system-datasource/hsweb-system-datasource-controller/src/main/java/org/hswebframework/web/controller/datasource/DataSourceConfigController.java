package org.hswebframework.web.controller.datasource;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.datasource.DataSourceConfigEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  数据源配置
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dataSourceConfig:dataSourceConfig}")
@Authorize(permission = "dataSourceConfig")
@AccessLogger("数据源配置")
public class DataSourceConfigController implements SimpleGenericEntityController<DataSourceConfigEntity, String, QueryParamEntity> {

    private DataSourceConfigService dataSourceConfigService;
  
    @Autowired
    public void setDataSourceConfigService(DataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }
  
    @Override
    public DataSourceConfigService getService() {
        return dataSourceConfigService;
    }
}
