package org.hswebframework.web.system.authorization.api.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SaveUserRequest {

    private String id;

    @NotBlank
    private String name;

    private String username;

    private String password;


}
