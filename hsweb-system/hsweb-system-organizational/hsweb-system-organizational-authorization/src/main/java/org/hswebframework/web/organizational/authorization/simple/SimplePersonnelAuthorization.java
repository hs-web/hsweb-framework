package org.hswebframework.web.organizational.authorization.simple;

import org.hswebframework.web.organizational.authorization.Personnel;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.Position;
import org.hswebframework.web.organizational.authorization.TreeNode;
import org.hswebframework.web.organizational.authorization.relation.Relations;

import java.util.Collections;
import java.util.Set;

/**
 * @author zhouhao
 */
public class SimplePersonnelAuthorization implements PersonnelAuthorization {
    private static final long serialVersionUID = 1_0;
    private Personnel             personnel;
    private Set<TreeNode<String>> districtIds;
    private Set<TreeNode<String>> orgIds;
    private Set<TreeNode<String>> positionIds;
    private Set<TreeNode<String>> departmentIds;
    private Relations             relations;
    private Set<Position>         positions;

    public void setPositions(Set<Position> positions) {
        this.positions = positions;
    }

    @Override
    public Set<Position> getPositions() {
        return positions;
    }

    @Override
    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    @Override
    public Set<TreeNode<String>> getDistrictIds() {
        if (districtIds == null) {
            districtIds = Collections.emptySet();
        }
        return districtIds;
    }

    public void setDistrictIds(Set<TreeNode<String>> districtIds) {
        this.districtIds = districtIds;
    }

    @Override
    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    @Override
    public Set<TreeNode<String>> getOrgIds() {
        if (orgIds == null) {
            orgIds = Collections.emptySet();
        }
        return orgIds;
    }

    public void setOrgIds(Set<TreeNode<String>> orgIds) {
        this.orgIds = orgIds;
    }

    @Override
    public Set<TreeNode<String>> getPositionIds() {
        if (positionIds == null) {
            positionIds = Collections.emptySet();
        }
        return positionIds;
    }

    public void setPositionIds(Set<TreeNode<String>> positionIds) {
        this.positionIds = positionIds;
    }

    @Override
    public Set<TreeNode<String>> getDepartmentIds() {
        if (departmentIds == null) {
            departmentIds = Collections.emptySet();
        }
        return departmentIds;
    }

    public void setDepartmentIds(Set<TreeNode<String>> departmentIds) {
        this.departmentIds = departmentIds;
    }
}
