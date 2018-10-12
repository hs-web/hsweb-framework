package org.hswebframework.web.tests;

import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.service.CrudService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.2
 */
@RestController
@RequestMapping("/test")
public class TestController implements SimpleGenericEntityController<TestEntity, String, QueryParamEntity> {

    @Override
    public CrudService<TestEntity, String> getService() {
        return new CrudService<TestEntity, String>() {
            @Override
            public TestEntity createEntity() {
                return new TestEntity();
            }

            @Override
            public Class<TestEntity> getEntityInstanceType() {
                return TestEntity.class;
            }

            @Override
            public TestEntity deleteByPk(String s) {
                return new TestEntity();
            }

            @Override
            public String insert(TestEntity data) {
                return data.getId();
            }

            @Override
            public PagerResult<TestEntity> selectPager(Entity param) {
                return PagerResult.empty();
            }

            @Override
            public List<TestEntity> select(Entity param) {
                return Collections.emptyList();
            }

            @Override
            public int count(Entity param) {
                return 0;
            }

            @Override
            public TestEntity selectSingle(Entity param) {
                return null;
            }

            @Override
            public TestEntity selectByPk(String id) {
                return new TestEntity();
            }

            @Override
            public List<TestEntity> selectByPk(List<String> id) {
                return Collections.emptyList();
            }

            @Override
            public List<TestEntity> select() {
                return Collections.emptyList();
            }

            @Override
            public int count() {
                return 0;
            }

            @Override
            public int updateByPk(String id, TestEntity data) {
                return 0;
            }

            @Override
            public int updateByPk(List<TestEntity> data) {
                return 0;
            }

            @Override
            public String saveOrUpdate(TestEntity testEntity) {
                return testEntity.getId();
            }
        };
    }
}
