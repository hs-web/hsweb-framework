package org.hswebframework.web.workflow.flowable.service;

import org.hswebframework.web.entity.workflow.ActDefEntity;

import java.util.List;

/**
 * @Author wangwei
 * @Date 2017/9/13.
 */
public interface BpmUtilsService {

    /**
     * 根据配置获取用户
     * @param userId
     * @param actDefEntity
     * @return
     */
    List<String> selectUserIdsBy(String userId, ActDefEntity actDefEntity);
}
