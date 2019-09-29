package org.hswebframework.web.workflow.service.imp;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.ezorm.core.RuntimeDefaultValue;
import org.hswebframework.ezorm.rdb.codec.DateTimeCodec;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.hswebframework.web.service.form.initialize.ColumnInitializeContext;
import org.hswebframework.web.service.form.initialize.DynamicFormInitializeCustomizer;
import org.hswebframework.web.service.form.initialize.TableInitializeContext;
import org.hswebframework.web.workflow.dao.entity.ActivityConfigEntity;
import org.hswebframework.web.workflow.dao.entity.ProcessDefineConfigEntity;
import org.hswebframework.web.workflow.service.ActivityConfigService;
import org.hswebframework.web.workflow.service.ProcessDefineConfigService;
import org.hswebframework.web.workflow.service.WorkFlowFormService;
import org.hswebframework.web.workflow.service.request.SaveFormRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.JDBCType;
import java.util.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkFlowFormServiceImpl extends AbstractFlowableService implements WorkFlowFormService, DynamicFormInitializeCustomizer {

    @Autowired
    private DynamicFormOperationService dynamicFormOperationService;

    @Autowired
    private ActivityConfigService activityConfigService;

    @Autowired
    private ProcessDefineConfigService processDefineConfigService;

    @Override
    public void saveProcessForm(ProcessInstance instance, SaveFormRequest request) {
        request.tryValidate();

        ProcessDefineConfigEntity entity = processDefineConfigService.selectByProcessDefineId(instance.getProcessDefinitionId());

        if (entity == null || StringUtils.isEmpty(entity.getFormId())) {
            return;
        }
        Map<String, Object> formData = request.getFormData();

        acceptStartProcessFormData(instance, formData);

        dynamicFormOperationService.saveOrUpdate(entity.getFormId(), formData);

    }

    @Override
    @Transactional(readOnly = true)
    public   PagerResult<Record> selectProcessForm(String processDefineId, QueryParamEntity queryParam) {
        ProcessDefineConfigEntity entity = processDefineConfigService.selectByProcessDefineId(processDefineId);

        if (entity == null || StringUtils.isEmpty(entity.getFormId())) {
            return PagerResult.empty();
        }

        return dynamicFormOperationService.selectPager(entity.getFormId(), queryParam);
    }

    @Override
    @Transactional(readOnly = true)
    public   PagerResult<Record> selectTaskForm(String processDefineId, String activityId, QueryParamEntity queryParam) {
        Objects.requireNonNull(processDefineId, "processDefineId can not be null");
        Objects.requireNonNull(activityId, "activityId can not be null");

        ActivityConfigEntity entity = activityConfigService.selectByProcessDefineIdAndActivityId(processDefineId, activityId);

        if (entity == null || StringUtils.isEmpty(entity.getFormId())) {
            return PagerResult.empty();
        }
        return dynamicFormOperationService.selectPager(entity.getFormId(), queryParam);
    }

    @Override
    public void saveTaskForm(ProcessInstance instance, Task task, SaveFormRequest request) {
        request.tryValidate();

        ActivityConfigEntity entity = activityConfigService.selectByProcessDefineIdAndActivityId(task.getProcessDefinitionId(), task.getTaskDefinitionKey());

        if (entity == null || StringUtils.isEmpty(entity.getFormId())) {
            return;
        }

        Map<String, Object> formData = request.getFormData();

        acceptStartProcessFormData(instance, formData);
        acceptTaskFormData(task, formData);

        dynamicFormOperationService.saveOrUpdate(entity.getFormId(), formData);

    }

    protected void acceptTaskFormData(Task task,
                                      Map<String, Object> formData) {


        formData.put("processTaskId", task.getId());
        formData.put("processTaskDefineKey", task.getTaskDefinitionKey());
        formData.put("processTaskName", task.getName());

    }

    protected void acceptStartProcessFormData(ProcessInstance instance,

                                              Map<String, Object> formData) {
        String processDefinitionName;
        try {
            processDefinitionName = ((ExecutionEntity) instance).getProcessDefinition().getName();
        } catch (NullPointerException e) {
            processDefinitionName = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(instance.getProcessDefinitionId())
                    .singleResult().getName();
        }
        formData.put("id", instance.getBusinessKey());
        formData.put("processDefineId", instance.getProcessDefinitionId());
        formData.put("processDefineKey", instance.getProcessDefinitionKey());
        formData.put("processDefineName", processDefinitionName);
        formData.put("processInstanceId", instance.getProcessInstanceId());

    }

    @Override
    public void customTableSetting(TableInitializeContext context) {
        RDBTableMetadata table = context.getTable();
        Dialect dialect = table.getDialect();

        if (!table.findFeature("enableWorkFlow").isPresent()) {
            return;
        }
        //----------taskId--------------
        {
            RDBColumnMetadata processTaskId = table.newColumn();
            processTaskId.setJdbcType(JDBCType.VARCHAR,String.class);
            processTaskId.setLength(32);
            processTaskId.setName("proc_task_id");
            processTaskId.setAlias("processTaskId");
            processTaskId.setComment("流程任务ID");
            table.addColumn(processTaskId);
        }

        //----------taskDefineKey--------------
        {
            RDBColumnMetadata taskDefineKey = table.newColumn();
            taskDefineKey.setJdbcType(JDBCType.VARCHAR,String.class);
            taskDefineKey.setLength(128);
            taskDefineKey.setName("proc_task_key");
            taskDefineKey.setAlias("processTaskDefineKey");
            taskDefineKey.setComment("流程任务定义KEY");
            table.addColumn(taskDefineKey);
        }

        //----------taskName--------------
        {
            RDBColumnMetadata processTaskName = table.newColumn();
            processTaskName.setJdbcType(JDBCType.VARCHAR,String.class);
            processTaskName.setLength(128);
            processTaskName.setName("proc_task_name");
            processTaskName.setAlias("processTaskName");
            processTaskName.setComment("流程任务定义名称");
            table.addColumn(processTaskName);
        }

        //----------processDefineId--------------
        {
            RDBColumnMetadata processDefineId = table.newColumn();
            processDefineId.setJdbcType(JDBCType.VARCHAR,String.class);
            processDefineId.setLength(32);
            processDefineId.setName("proc_def_id");
            processDefineId.setAlias("processDefineId");
            processDefineId.setProperty("read-only", true);
            processDefineId.setComment("流程定义ID");
            table.addColumn(processDefineId);
        }
        //----------processDefineKey--------------
        {
            RDBColumnMetadata processDefineKey = table.newColumn();
            processDefineKey.setJdbcType(JDBCType.VARCHAR,String.class);
            processDefineKey.setLength(32);
            processDefineKey.setName("proc_def_key");
            processDefineKey.setAlias("processDefineKey");
            processDefineKey.setProperty("read-only", true);
            processDefineKey.setComment("流程定义KEY");
            table.addColumn(processDefineKey);
        } //----------processDefineName--------------
        {
            RDBColumnMetadata processDefineName =table.newColumn();
            processDefineName.setJdbcType(JDBCType.VARCHAR,String.class);
            processDefineName.setLength(128);
            processDefineName.setName("proc_def_name");
            processDefineName.setAlias("processDefineName");
            processDefineName.setProperty("read-only", true);
            processDefineName.setComment("流程定义Name");
            table.addColumn(processDefineName);
        }
        //----------processDefineVersion--------------
        {
            RDBColumnMetadata processInstanceId =table.newColumn();
            processInstanceId.setJdbcType(JDBCType.VARCHAR,String.class);
            processInstanceId.setLength(32);
            processInstanceId.setName("proc_ins_id");
            processInstanceId.setAlias("processInstanceId");
            processInstanceId.setProperty("read-only", true);
            processInstanceId.setComment("流程实例ID");
            table.addColumn(processInstanceId);
        }//----------creatorUserId--------------
        {
            RDBColumnMetadata creatorUserId = table.newColumn();
            creatorUserId.setJdbcType(JDBCType.VARCHAR,String.class);
            creatorUserId.setLength(32);
            creatorUserId.setName("creator_id");
            creatorUserId.setAlias("creatorId");
            creatorUserId.setProperty("read-only", true);
            creatorUserId.setComment("创建人ID");
            creatorUserId.setDefaultValue((RuntimeDefaultValue)() -> Authentication.current().map(autz -> autz.getUser().getId()).orElse(null));
            table.addColumn(creatorUserId);
        }
        {//-----------creatorName---------
            RDBColumnMetadata creatorName =table.newColumn();
            creatorName.setJdbcType(JDBCType.VARCHAR,String.class);
            creatorName.setLength(32);
            creatorName.setName("creator_name");
            creatorName.setAlias("creatorName");
            creatorName.setProperty("read-only", true);
            creatorName.setComment("创建人姓名");
            creatorName.setDefaultValue((RuntimeDefaultValue)() -> Authentication.current().map(autz -> autz.getUser().getName()).orElse(null));
            table.addColumn(creatorName);
        }
        {//-----------creatorName---------
            RDBColumnMetadata createTime =table.newColumn();
            createTime.setJavaType(Date.class);
            createTime.setJdbcType(JDBCType.TIMESTAMP,Date.class);
            createTime.setName("create_time");
            createTime.setAlias("createTime");
            createTime.setProperty("read-only", true);
            createTime.setComment("创建时间");
            createTime.setNotNull(true);
            createTime.setValueCodec(new DateTimeCodec("yyyy-MM-dd HH:mm:ss", Date.class));
            createTime.setDefaultValue((RuntimeDefaultValue)Date::new);
            table.addColumn(createTime);
        }
        {//-----------lastUpdateTime---------
            RDBColumnMetadata lastUpdateTime =table.newColumn();
            lastUpdateTime.setJavaType(Date.class);
            lastUpdateTime.setJdbcType(JDBCType.TIMESTAMP,Date.class);
            lastUpdateTime.setName("last_update_time");
            lastUpdateTime.setAlias("lastUpdateTime");
            lastUpdateTime.setComment("最后一次修改时间");
            lastUpdateTime.setNotNull(true);
            lastUpdateTime.setValueCodec(new DateTimeCodec("yyyy-MM-dd HH:mm:ss", Date.class));
            lastUpdateTime.setDefaultValue((RuntimeDefaultValue)Date::new);
            table.addColumn(lastUpdateTime);
        }
    }

    @Override
    public void customTableColumnSetting(ColumnInitializeContext context) {

    }
}
