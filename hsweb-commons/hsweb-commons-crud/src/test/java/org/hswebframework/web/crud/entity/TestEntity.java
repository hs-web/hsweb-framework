package org.hswebframework.web.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.ExtendableEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;

import javax.persistence.Column;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "s_test")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EnableEntityEvent
public class TestEntity extends ExtendableEntity<String> {

    @Column(length = 32)
    private String name;

    @Column
    private Integer age;

    @Column
    private String testName;


}
