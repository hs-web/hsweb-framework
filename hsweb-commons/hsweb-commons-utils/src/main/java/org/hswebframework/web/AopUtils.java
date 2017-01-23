/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.LinkedHashMap;
import java.util.Map;

public class AopUtils {

    public static final String getMethodName(JoinPoint pjp) {
        StringBuilder methodName = new StringBuilder(pjp.getSignature().getName()).append("(");
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String[] names = signature.getParameterNames();
        Class[] args = signature.getParameterTypes();
        for (int i = 0, len = args.length; i < len; i++) {
            if (i != 0) methodName.append(",");
            methodName.append(args[i].getSimpleName()).append(" ").append(names[i]);
        }
        return methodName.append(")").toString();
    }

    public static final Map<String, Object> getArgsMap(JoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Map<String, Object> args = new LinkedHashMap<>();
        String names[] = signature.getParameterNames();
        for (int i = 0, len = names.length; i < len; i++) {
            args.put(names[i], pjp.getArgs()[i]);
        }
        return args;
    }
}