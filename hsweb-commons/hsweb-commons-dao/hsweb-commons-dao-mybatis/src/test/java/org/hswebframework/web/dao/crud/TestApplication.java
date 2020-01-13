package org.hswebframework.web.dao.crud;

import org.apache.ibatis.session.SqlSessionFactory;
import org.hswebframework.web.dao.mybatis.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@SpringBootApplication
//@EntityScan("org.hswebframework.web.dao")

public class TestApplication {

    @Bean
    public SqlSessionFactory sqlSessionFactory2(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setVfs(SpringBootVFS.class);
        factory.setDataSource(dataSource);
        String typeHandlers = "org.hswebframework.web.dao.mybatis.handler";
        factory.setTypeHandlersPackage(typeHandlers);
        factory.setMapperLocations(new Resource[]{new ClassPathResource("org/hswebframework/web/dao/test/TestDao.xml")});

        return factory.getObject();
    }

}
