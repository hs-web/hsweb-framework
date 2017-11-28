package org.hswebframework.web.commons.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuEntity extends SimpleTreeSortSupportEntity<Integer> {
    private static final long serialVersionUID = 5548107788893085691L;

    private String name;

    private List<MenuEntity> children;
}
