package org.hswebframework.web.organizational.authorization;

import org.hswebframework.web.authorization.Authentication;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 人员权限信息,用于获取当前登录用户对应的人员相关信息
 *
 * @author zhouhao
 * @see Authentication
 * @since 3.0
 */
public interface PersonnelAuthorization extends Serializable {

    /**
     * 获取当前登录人员信息
     *
     * @return 人员权限信息
     * @see Optional
     * @see Authentication#getAttribute(String)
     */
    static Optional<PersonnelAuthorization> current() {
        return Optional.ofNullable(PersonnelAuthorizationHolder.get());
    }

    /**
     * @return 人员的基本信息
     */
    Personnel getPersonnel();

    /**
     * @return 人员所在行政区域ID, 只返回根节点, 永远不会返回{@code null}
     */
    Set<TreeNode<String>> getDistrictIds();

    /**
     * @return 人员所在机构ID, 只返回根节点, 永远不会返回{@code null}
     */
    Set<TreeNode<String>> getOrgIds();

    /**
     * @return 人员职务ID, 只返回根节点, 永远不会返回{@code null}
     */
    Set<TreeNode<String>> getPositionIds();

    /**
     * @return 人员所在部门ID, 只返回根节点, 永远不会返回{@code null}
     */
    Set<TreeNode<String>> getDepartmentIds();

    /**
     * @return 根地区ID
     */
    default Set<String> getRootDistrictId() {
        return getDistrictIds().stream().map(TreeNode::getValue).collect(Collectors.toSet());
    }

    /**
     * @return 根机构ID
     */
    default Set<String> getRootOrgId() {
        return getOrgIds().stream().map(TreeNode::getValue).collect(Collectors.toSet());
    }

    /**
     * @return 根职位ID
     */
    default Set<String> getRootPositionId() {
        return getPositionIds().stream().map(TreeNode::getValue).collect(Collectors.toSet());
    }

    /**
     * @return 根部门ID
     */
    default Set<String> getRootDepartmentId() {
        return getDepartmentIds().stream().map(TreeNode::getValue).collect(Collectors.toSet());
    }

    /**
     * @return 所有地区ID
     */
    default Set<String> getAllDistrictId() {
        return getDistrictIds().stream().map(TreeNode::getAllValue).flatMap(List::stream).collect(Collectors.toSet());
    }

    /**
     * @return 所有机构ID
     */
    default Set<String> getAllOrgId() {
        return getOrgIds().stream().map(TreeNode::getAllValue).flatMap(List::stream).collect(Collectors.toSet());
    }

    /**
     * @return 所有职位ID
     */
    default Set<String> getAllPositionId() {
        return getPositionIds().stream().map(TreeNode::getAllValue).flatMap(List::stream).collect(Collectors.toSet());
    }

    /**
     * @return 所有部门ID
     */
    default Set<String> getAllDepartmentId() {
        return getDepartmentIds().stream().map(TreeNode::getAllValue).flatMap(List::stream).collect(Collectors.toSet());
    }
}
