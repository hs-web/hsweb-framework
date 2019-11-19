package org.hswebframework.web.authorization.basic.aop;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import reactor.core.publisher.Mono;

import javax.persistence.Column;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "test_entity")
public class TestEntity extends GenericEntity<String> {

    @Column
    private String roleId;


}
