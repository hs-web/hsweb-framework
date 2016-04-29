package org.hsweb.web.exception;

import org.hsweb.web.message.ResponseMessage;

public interface ExceptionHandler {

   <T extends Throwable> boolean support(Class<T> e);

    ResponseMessage handle(Throwable e);
}