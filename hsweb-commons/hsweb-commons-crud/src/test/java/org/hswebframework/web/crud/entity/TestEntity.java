package org.hswebframework.web.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.crud.generator.Generators;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "s_test")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EnableEntityEvent
public class TestEntity extends GenericEntity<String> {

    @Column(length = 32)
    private String name;

    @Column
    private Integer age;

    @Column
    private String testName;


}
