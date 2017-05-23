package organizational.entity;

import java.io.Serializable;

/**
 * 关联部门信息的实体,实现此接口,才能对相应的操作进行部门相关的权限控制
 *
 * @author zhouhao
 * @since 3.0
 */
public interface DepartmentAttachEntity extends Serializable {

    /*-------------------------------------------
     |               属性名常量               |
     ===========================================*/
    String departmentId = "departmentId";

    /**
     * @return 部门ID
     */
    String getDepartmentId();

    void setDepartmentId(String departmentId);
}
