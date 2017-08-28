package org.hswebframework.web.service.schedule.simple;

import org.hswebframework.web.dao.schedule.ScheduleJobDao;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("scheduleJobService")
public class SimpleScheduleJobService extends GenericEntityService<ScheduleJobEntity, String>
        implements ScheduleJobService {
    @Autowired
    private ScheduleJobDao scheduleJobDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public ScheduleJobDao getDao() {
        return scheduleJobDao;
    }

}
