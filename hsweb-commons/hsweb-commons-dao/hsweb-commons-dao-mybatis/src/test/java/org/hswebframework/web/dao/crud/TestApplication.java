package org.hswebframework.web.dao.crud;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.dao.mybatis.EnumDictHandlerRegister;
import org.hswebframework.web.dao.mybatis.MybatisEntityFactory;
import org.hswebframework.web.dao.mybatis.MybatisProperties;
import org.hswebframework.web.dao.mybatis.MybatisUtils;
import org.hswebframework.web.dao.mybatis.builder.EasyOrmSqlBuilder;
import org.hswebframework.web.dao.mybatis.dynamic.DynamicDataSourceSqlSessionFactoryBuilder;
import org.hswebframework.web.dao.mybatis.dynamic.DynamicSpringManagedTransaction;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Arrays;

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

        SqlSessionFactory sqlSessionFactory = factory.getObject();
        return sqlSessionFactory;
    }

}
