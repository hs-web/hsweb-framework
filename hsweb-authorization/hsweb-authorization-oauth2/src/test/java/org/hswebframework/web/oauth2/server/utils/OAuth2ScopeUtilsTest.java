package org.hswebframework.web.oauth2.server.utils;

import org.hswebframework.web.oauth2.server.ScopePredicate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OAuth2ScopeUtilsTest {


    @Test
    public void testEmpty() {
        ScopePredicate predicate = OAuth2ScopeUtils.createScopePredicate(null);
        assertFalse(predicate.test("basic"));
    }

    @Test
    public void testScope() {
        ScopePredicate predicate = OAuth2ScopeUtils.createScopePredicate("basic user:info device:query");

        assertTrue(predicate.test("basic"));
        {

            assertTrue(predicate.test("user", "info"));
            assertFalse(predicate.test("user", "info2"));
        }

        {
            assertTrue(predicate.test("device", "query"));
            assertFalse(predicate.test("device", "query2"));
        }

    }
}