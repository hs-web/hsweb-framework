package org.hswebframework.web.oauth2.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.junit.Test;

import javax.persistence.Column;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OAuth2ClientEntityTest {

    @Test
    public void shouldLeaveClientTypeUnsetUntilInsertDefaultIsApplied() {
        OAuth2ClientEntity entity = new OAuth2ClientEntity();

        assertNull(entity.getClientType());
        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, entity.toOAuth2Client().getClientType());
    }

    @Test
    public void shouldKeepClientTypeUnsetWhenLegacyJsonOmitsIt() throws Exception {
        OAuth2ClientEntity entity = new ObjectMapper()
            .readValue("{\"name\":\"renamed\"}", OAuth2ClientEntity.class);

        assertNull(entity.getClientType());
    }

    @Test
    public void shouldDeclareNullableColumnWithInsertDefault() throws Exception {
        Field field = OAuth2ClientEntity.class.getDeclaredField("clientType");
        Column column = field.getAnnotation(Column.class);
        DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);

        assertNotNull(column);
        assertTrue(column.nullable());
        assertNotNull(defaultValue);
        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, defaultValue.value());
    }

    @Test
    public void shouldMapConfiguredClientType() {
        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        entity.setClientType("api-application");

        assertEquals("api-application", entity.toOAuth2Client().getClientType());
    }

    @Test
    public void shouldNormalizeLegacyNullClientType() {
        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        entity.setClientType(null);

        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, entity.toOAuth2Client().getClientType());
    }

    @Test
    public void shouldNormalizeLegacyBlankClientType() {
        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        entity.setClientType(" ");

        assertEquals(OAuth2Client.DEFAULT_CLIENT_TYPE, entity.toOAuth2Client().getClientType());
    }
}
