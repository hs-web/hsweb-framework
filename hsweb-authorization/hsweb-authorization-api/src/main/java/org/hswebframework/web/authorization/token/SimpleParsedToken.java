package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
public class SimpleParsedToken implements ParsedToken{

    private String type;

    private String token;


}
