package org.hswebframework.web.dashboard;

import org.hswebframework.web.service.CrudService;

import java.util.List;

public interface DashBoardService extends CrudService<DashBoardConfigEntity, String> {

    List<DashBoardConfigEntity> selectAllDefaults();
}
