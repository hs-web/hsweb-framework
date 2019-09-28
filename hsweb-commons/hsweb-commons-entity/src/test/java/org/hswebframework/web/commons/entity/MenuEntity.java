package org.hswebframework.web.commons.entity;

import lombok.*;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "s_menu")
public class MenuEntity extends SimpleTreeSortSupportEntity<Integer> {
    private static final long serialVersionUID = 5548107788893085691L;

    @Column(length = 32)
    @Comment("名称")
    private String name;


    private List<MenuEntity> children;
}
