package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
public class UserMenuEntity extends SimpleTreeSortSupportEntity<String> {

    private static final long serialVersionUID = 7839545362972442294L;

    private String menuId;

    //菜单名称
    private String name;

    //备注
    private String describe;

    //权限ID
    private String permissionId;

    //菜单对应的url
    private String url;

    //图标
    private String icon;

    private List<UserMenuEntity> children;

}
