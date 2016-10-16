/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.mybatis;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MybatisProperties extends org.mybatis.spring.boot.autoconfigure.MybatisProperties {
    private static final String   defaultMapperLocation  = "classpath*:org/hsweb/web/dao/impl/mybatis/mapper/**/*.xml";
    private              String   type                   = "oracle";
    private              boolean  dynamicDatasource      = false;
    private              String[] mapperLocationExcludes = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getMapperLocationExcludes() {
        return mapperLocationExcludes;
    }

    public void setMapperLocationExcludes(String[] mapperLocationExcludes) {
        this.mapperLocationExcludes = mapperLocationExcludes;
    }

    public boolean isDynamicDatasource() {
        return dynamicDatasource;
    }

    public void setDynamicDatasource(boolean dynamicDatasource) {
        this.dynamicDatasource = dynamicDatasource;
    }

    @Override
    public String[] getMapperLocations() {
        return super.getMapperLocations();
    }

    public Resource[] resolveMapperLocations() {
        Map<String, Resource> resources = new HashMap<>();
        Set<String> locations;

        if (this.getMapperLocations() == null)
            locations = new HashSet<>();
        else
            locations = Arrays.stream(getMapperLocations()).collect(Collectors.toSet());
        locations.add(defaultMapperLocation);
        for (String mapperLocation : locations) {
            Resource[] mappers;
            try {
                mappers = new PathMatchingResourcePatternResolver().getResources(mapperLocation);
                for (Resource mapper : mappers) {
                    resources.put(mapper.getURL().toString(), mapper);
                }
            } catch (IOException e) {
            }
        }
        if (mapperLocationExcludes != null && mapperLocationExcludes.length > 0)

        {
            for (String mapperLocationExclude : mapperLocationExcludes) {
                try {
                    Resource[] excludesMappers = new PathMatchingResourcePatternResolver().getResources(mapperLocationExclude);
                    for (Resource excludesMapper : excludesMappers) {
                        resources.remove(excludesMapper.getURL().toString());
                    }
                } catch (IOException e) {
                }
            }
        }

        Resource[] mapperLocations = new Resource[resources.size()];
        mapperLocations = resources.values().

                toArray(mapperLocations);
        return mapperLocations;
    }

}
