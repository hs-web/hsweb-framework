package org.hswebframework.web.oauth2.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.oauth2.entity.OAuth2ClientEntity;
import org.hswebframework.web.oauth2.service.OAuth2ClientService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2/client")
@AllArgsConstructor
@Resource(id = "oauth2-client", name = "OAuth2客户端管理")
@Tag(name = "OAuth2客户端管理")
public class WebFluxOAuth2ClientController implements ReactiveServiceCrudController<OAuth2ClientEntity, String> {

    private final OAuth2ClientService oAuth2ClientService;

    @Override
    public ReactiveCrudService<OAuth2ClientEntity, String> getService() {
        return oAuth2ClientService;
    }
}
