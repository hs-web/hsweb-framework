package org.hswebframework.web.service.oauth2;

public class TestEntityService extends AbstractOAuth2CrudService<TestEntity,String>{
    @Override
    public String getServiceId() {
        return "test";
    }

    @Override
    public String getUriPrefix() {
        return "/test";
    }
}
