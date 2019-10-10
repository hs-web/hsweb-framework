package org.hswebframework.web.crud.configuration;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "entityType")
@AllArgsConstructor
public class EntityInfo {
    private Class entityType;

    private Class realType;

    private Class idType;

    private boolean reactive;
}