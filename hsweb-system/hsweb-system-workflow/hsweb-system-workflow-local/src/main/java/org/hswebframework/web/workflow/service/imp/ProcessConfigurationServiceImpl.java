package org.hswebframework.web.workflow.service.imp;

import io.vavr.Lazy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.expands.script.engine.ExecuteResult;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.AuthenticationPredicate;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthenticationHolder;
import org.hswebframework.web.workflow.dao.entity.ActivityConfigEntity;
import org.hswebframework.web.workflow.dao.entity.ListenerConfig;
import org.hswebframework.web.workflow.dao.entity.ProcessDefineConfigEntity;
import org.hswebframework.web.workflow.dimension.CandidateDimension;
import org.hswebframework.web.workflow.dimension.CandidateDimensionParser;
import org.hswebframework.web.workflow.dimension.DimensionContext;
import org.hswebframework.web.workflow.dimension.PermissionDimensionParser;
import org.hswebframework.web.workflow.listener.ProcessEvent;
import org.hswebframework.web.workflow.listener.ProcessEventListener;
import org.hswebframework.web.workflow.listener.TaskEvent;
import org.hswebframework.web.workflow.listener.TaskEventListener;
import org.hswebframework.web.workflow.service.ActivityConfigService;
import org.hswebframework.web.workflow.service.ProcessDefineConfigService;
import org.hswebframework.web.workflow.service.config.ProcessConfigurationService;
import org.hswebframework.web.workflow.service.config.CandidateInfo;
import org.hswebframework.web.workflow.service.config.ActivityConfiguration;
import org.hswebframework.web.workflow.service.config.ProcessConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Service
@Slf4j
public class ProcessConfigurationServiceImpl implements ProcessConfigurationService {

    @Autowired
    private ProcessDefineConfigService defineConfigService;

    @Autowired
    private ActivityConfigService activityConfigService;

    @Autowired
    private PermissionDimensionParser permissionDimensionParser;

    @Autowired
    private CandidateDimensionParser candidateDimensionParser;


    private static final EmptyActivityConfiguration emptyConfiguration = new EmptyActivityConfiguration();

    @Override
    public ActivityConfiguration getActivityConfiguration(String doingUser, String processDefineId, String activityId) {
        ActivityConfigEntity configEntity = activityConfigService.selectByProcessDefineIdAndActivityId(processDefineId, activityId);
        if (configEntity == null) {
            return emptyConfiguration;
        }

        return new ActivityConfiguration() {
            @Override
            public String getFormId() {
                return configEntity.getFormId();
            }

            @Override
            public boolean canClaim(Task task, String userId) {
                return getCandidateInfo(task)
                        .stream()
                        .map(CandidateInfo::user)
                        .anyMatch(user -> user.getUser().getId().equals(userId));
            }

            @Override
            @SuppressWarnings("all")
            public List<CandidateInfo> getCandidateInfo(Task task) {
                return Lazy.val(() -> {

                    DimensionContext context = new DimensionContext();
                    context.setCreatorId(doingUser);
                    context.setActivityId(activityId);
                    context.setProcessDefineId(processDefineId);
                    context.setTask(task);
                    CandidateDimension dimension = candidateDimensionParser
                            .parse(context, configEntity.getCandidateDimension());

                    return dimension.getCandidateUserIdList()
                            .stream()
                            .distinct()
                            .map(userId ->
                                    Lazy.val(() -> new CandidateInfo() {
                                        @Override
                                        public Authentication user() {
                                            return AuthenticationHolder.get(userId);
                                        }

                                        @Override
                                        public PersonnelAuthentication person() {
                                            return PersonnelAuthenticationHolder.getByUserId(userId);
                                        }
                                    }, CandidateInfo.class))
                            .collect(Collectors.toList());

                }, List.class);
            }

            @Override
            public TaskEventListener getTaskListener(String eventType) {
                if (CollectionUtils.isEmpty(configEntity.getListeners())) {
                    return null;
                }
                return configEntity
                        .getListeners()
                        .stream()
                        .filter(config -> eventType.equals(config.getEventType()))
                        .map(ProcessConfigurationServiceImpl.this::<TaskEvent>createTaskEventListener)
                        .collect(Collectors.collectingAndThen(Collectors.toList(),
                                list -> event -> list.forEach(listener -> listener.accept(event))));
            }
        };
    }


    @SneakyThrows
    protected <T> Consumer<T> createTaskEventListener(ListenerConfig listenerConfig) {
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(listenerConfig.getLanguage());
        if (null != engine) {
            String scriptId = DigestUtils.md5Hex(listenerConfig.getScript());
            if (!engine.compiled(scriptId)) {
                engine.compile(scriptId, listenerConfig.getScript());
            }
            return event -> {
                Map<String, Object> context = new HashMap<>();
                context.put("event", event);
                ExecuteResult result = engine.execute(scriptId, context);
                if (!result.isSuccess()) {
                    throw new BusinessException("执行监听器失败:" + result.getMessage(), result.getException());
                }
            };
        } else {
            log.warn("不支持的脚本语言:{}", listenerConfig.getLanguage());
        }
        return null;
    }

    @Override
    public ProcessConfiguration getProcessConfiguration(String processDefineId) {
        ProcessDefineConfigEntity entity = defineConfigService.selectByProcessDefineId(processDefineId);
        if (entity == null) {
            return emptyConfiguration;
        }
        return new ProcessConfiguration() {
            @Override
            public String getFormId() {
                return entity.getFormId();
            }

            @Override
            public void assertCanStartProcess(String userId, ProcessDefinition definition) {
                if (!canStartProcess(userId, definition)) {
                    throw new AccessDenyException("没有权限启动此流程");
                }
            }

            @Override
            public boolean canStartProcess(String userId, ProcessDefinition definition) {
                if (StringUtils.isEmpty(entity.getPermissionDimension()) || "*".equals(entity.getPermissionDimension())) {
                    return true;
                }
                AuthenticationPredicate predicate = permissionDimensionParser.parse(entity.getPermissionDimension());
                if (null != predicate) {
                    return predicate.test(AuthenticationHolder.get(userId));
                }
                return true;
            }

            @Override
            public ProcessEventListener getProcessListener(String eventType) {
                if (CollectionUtils.isEmpty(entity.getListeners())) {
                    return null;
                }
                return entity
                        .getListeners()
                        .stream()
                        .filter(config -> eventType.equals(config.getEventType()))
                        .map(ProcessConfigurationServiceImpl.this::<ProcessEvent>createTaskEventListener)
                        .collect(Collectors.collectingAndThen(Collectors.toList(),
                                list -> event -> list.forEach(listener -> listener.accept(event))));
            }
        };
    }

    private final static class EmptyActivityConfiguration implements ActivityConfiguration, ProcessConfiguration {

        @Override
        public String getFormId() {
            return null;
        }

        @Override
        public boolean canClaim(Task task, String userId) {
            return false;
        }

        @Override
        public List<CandidateInfo> getCandidateInfo(Task task) {
            return new java.util.ArrayList<>();
        }

        @Override
        public void assertCanStartProcess(String userId, ProcessDefinition definition) {
        }

        @Override
        public boolean canStartProcess(String userId, ProcessDefinition definition) {
            return true;
        }

        @Override
        public ProcessEventListener getProcessListener(String eventType) {
            return null;
        }

        @Override
        public TaskEventListener getTaskListener(String eventType) {
            return null;
        }
    }
}
