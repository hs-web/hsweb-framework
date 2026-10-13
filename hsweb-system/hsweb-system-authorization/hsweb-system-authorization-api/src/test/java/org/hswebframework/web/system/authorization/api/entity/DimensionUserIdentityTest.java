package org.hswebframework.web.system.authorization.api.entity;

import org.junit.Test;

import static org.junit.Assert.*;

/** 保持公开成员 ID 的原有生成与保留契约。 */
public class DimensionUserIdentityTest {
    @Test
    public void ordinaryTupleUsesLegacyMd5() {
        DimensionUserEntity member = member("role", "operator", "user-1");
        member.generateId();
        // role-operator-user-1 的旧 MD5。
        assertEquals("c2906b52da24edf0524f7b42229236e6", member.getId());
        member.setRelation("manager");
        member.generateId();
        assertEquals("c2906b52da24edf0524f7b42229236e6", member.getId());
    }

    @Test
    public void callerProvidedIdRemainsUnchanged() {
        DimensionUserEntity member = member("role", "worker", "user");
        member.setId("legacy-public-member-id");
        member.generateId();
        assertEquals("legacy-public-member-id", member.getId());
    }

    private static DimensionUserEntity member(String type, String dimension, String user) {
        DimensionUserEntity member = new DimensionUserEntity();
        member.setDimensionTypeId(type);
        member.setDimensionId(dimension);
        member.setUserId(user);
        return member;
    }
}
