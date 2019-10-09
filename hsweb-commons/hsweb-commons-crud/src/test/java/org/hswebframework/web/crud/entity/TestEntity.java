package org.hswebframework.web.crud.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.crud.annotation.Reactive;

import javax.persistence.Column;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "s_test")
@Reactive
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class TestEntity extends GenericEntity<String> {

    @Column(length = 32)
    private String name;

    @Column
    private Integer age;

}
