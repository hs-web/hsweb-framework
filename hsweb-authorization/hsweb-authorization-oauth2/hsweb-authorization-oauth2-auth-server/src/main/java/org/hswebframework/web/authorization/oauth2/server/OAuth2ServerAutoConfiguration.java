package org.hswebframework.web.authorization.oauth2.server;

import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Configuration
public class OAuth2ServerAutoConfiguration{

    @Bean
    public OAuth2ServerErrorControllerAdvice oAuth2ServerErrorControllerAdvice(){
        return new OAuth2ServerErrorControllerAdvice();
    }
    /**
     * @author zhouhao
     */
    @RestControllerAdvice
    public static class OAuth2ServerErrorControllerAdvice {

        @ExceptionHandler(GrantTokenException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseMessage<String> error(GrantTokenException e) {
            return ResponseMessage.<String>error(e.getErrorType().code(),e.getMessage())
                    .result(e.getErrorType().message());
        }
    }
}

