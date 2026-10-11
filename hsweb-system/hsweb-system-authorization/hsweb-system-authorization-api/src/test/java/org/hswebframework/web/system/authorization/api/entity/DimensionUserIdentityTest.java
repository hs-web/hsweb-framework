package org.hswebframework.web.system.authorization.api.entity;

import org.junit.Test;

import static org.junit.Assert.*;

/** 成员身份按 UTF-8 字节长度编码三个字段，分隔字符不会合并不同自然键。 */
public class DimensionUserIdentityTest {
    @Test
    public void delimiterContainingTuplesHaveDifferentIds() {
        DimensionUserEntity first = member("a-b", "c", "user");
        DimensionUserEntity second = member("a", "b-c", "user");
        first.generateId();
        second.generateId();
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    public void unicodeIdUsesUtf8ByteLengthPrefixes() {
        DimensionUserEntity member = member("现场-运维", "片区|A:01", "用户-一");
        member.generateId();
        // 13:现场-运维11:片区|A:0110:用户-一 的 UTF-8 MD5。
        assertEquals("3133224d24611e79e511cb90d47e9c09", member.getId());
    }

    @Test
    public void sameTupleKeepsItsIdWhenRelationChanges() {
        DimensionUserEntity first = member("field-team", "district|A:01", "user-1");
        first.generateId();
        first.setRelation("manager");
        first.generateId();
        assertEquals("d2a7f6fd3daeb10bd68021d4dc75ac23", first.getId());
        DimensionUserEntity second = member("field-team", "district|A:01", "user-1");
        second.setRelation("member");
        second.generateId();
        assertEquals(first.getId(), second.getId());
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
