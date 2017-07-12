package org.hswebframework.web.service.form.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hsweb.ezorm.core.ValueConverter;
import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.ezorm.rdb.meta.converter.*;
import org.hsweb.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.form.DynamicFormColumnDao;
import org.hswebframework.web.dao.form.DynamicFormDao;
import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.entity.form.DynamicFormDeployLogEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.form.DatabaseRepository;
import org.hswebframework.web.service.form.DynamicFormDeployLogService;
import org.hswebframework.web.service.form.DynamicFormService;
import org.hswebframework.web.service.form.OptionalConvertBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormService")
public class SimpleDynamicFormService extends GenericEntityService<DynamicFormEntity, String>
        implements DynamicFormService {
    @Autowired
    private DynamicFormDao dynamicFormDao;

    @Autowired
    private DynamicFormColumnDao formColumnDao;

    @Autowired
    private DatabaseRepository databaseRepository;

    @Autowired
    private DynamicFormDeployLogService dynamicFormDeployLogService;

    @Autowired(required = false)
    private OptionalConvertBuilder optionalConvertBuilder;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormDao getDao() {
        return dynamicFormDao;
    }

    @Override
    public int updateByPk(String s, DynamicFormEntity entity) {
        return super.updateByPk(s, entity);
    }

    @Override
    public void deployAllFromLog() {
        List<DynamicFormEntity> entities = createQuery()
                .select(DynamicFormEntity.id)
                .where(DynamicFormEntity.deployed, true)
                .listNoPaging();
        if (logger.isDebugEnabled()) {
            logger.debug("do deploy all form , size:{}", entities.size());
        }
        for (DynamicFormEntity form : entities) {
            DynamicFormDeployLogEntity logEntity = dynamicFormDeployLogService.selectLastDeployed(form.getId());
            if (null != logEntity) {
                deployFromLog(logEntity);
            }
        }
    }

    @Override
    public void deployAll() {
        createQuery()
                .select(DynamicFormEntity.id)
                .listNoPaging()
                .forEach(form -> this.deploy(form.getId()));
    }

    public DynamicFormDeployLogEntity createDeployLog(DynamicFormEntity form, List<DynamicFormColumnEntity> columns) {
        DynamicFormDeployLogEntity entity = entityFactory.newInstance(DynamicFormDeployLogEntity.class);
        entity.setStatus(DataStatus.STATUS_ENABLED);
        entity.setDeployTime(System.currentTimeMillis());
        entity.setVersion(form.getVersion());
        entity.setFormId(form.getId());
        Map<String, Object> meta = new HashMap<>();
        meta.put("form", form);
        meta.put("columns", columns);
        entity.setMetaData(JSON.toJSONString(meta));
        return entity;
    }

    public void deployFromLog(DynamicFormDeployLogEntity logEntity) {
        JSONObject metadata = JSON.parseObject(logEntity.getMetaData());
        DynamicFormEntity form = metadata.getObject("form", DynamicFormEntity.class);
        List<DynamicFormColumnEntity> columns = metadata.getJSONArray("columns").toJavaList(DynamicFormColumnEntity.class);
        if (logger.isDebugEnabled()) {
            logger.debug("do deploy form {} , columns size:{}", form.getName(), columns.size());
        }
        deploy(form, columns);
    }

    @Override
    public String insert(DynamicFormEntity entity) {
        entity.setDeployed(false);
        return super.insert(entity);
    }

    @Override
    public void unDeploy(String formId) {
        DynamicFormEntity form = selectByPk(formId);
        assertNotNull(form);
        //取消发布
        dynamicFormDeployLogService.cancelDeployed(formId);
        //移除表结构定义
        RDBDatabase database = StringUtils.isEmpty(form.getDataSourceId())
                ? databaseRepository.getDefaultDatabase()
                : databaseRepository.getDatabase(form.getDataSourceId());
        database.removeTable(form.getDatabaseTableName());
    }

    public void deploy(String formId) {
        DynamicFormEntity formEntity = selectByPk(formId);
        assertNotNull(formEntity);
        List<DynamicFormColumnEntity> columns = DefaultDSLQueryService.createQuery(formColumnDao)
                .where(DynamicFormColumnEntity.formId, formId)
                .listNoPaging();
        deploy(formEntity, columns);
        try {
            dynamicFormDeployLogService.insert(createDeployLog(formEntity, columns));
        } catch (Exception e) {
            unDeploy(formId);
            throw e;
        }
    }

    protected void deploy(DynamicFormEntity form, List<DynamicFormColumnEntity> columns) {
        RDBDatabase database = StringUtils.isEmpty(form.getDataSourceId())
                ? databaseRepository.getDefaultDatabase()
                : databaseRepository.getDatabase(form.getDataSourceId());
        RDBTableMetaData metaData = buildTable(database, form, columns);
        try {
            if (!database.getMeta().getParser().tableExists(metaData.getName())) {
                database.createTable(metaData);
            } else {
                database.alterTable(metaData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected RDBTableMetaData buildTable(RDBDatabase database, DynamicFormEntity form, List<DynamicFormColumnEntity> columns) {
        RDBTableMetaData metaData = new RDBTableMetaData();
        metaData.setComment(form.getDescribe());
        metaData.setName(form.getDatabaseTableName());
        metaData.setProperties(form.getProperties());
        metaData.setAlias(form.getAlias());
        columns.stream().map(column -> {
            RDBColumnMetaData columnMeta = new RDBColumnMetaData();
            columnMeta.setName(column.getName());
            columnMeta.setAlias(column.getAlias());
            columnMeta.setComment(column.getDescribe());
            columnMeta.setLength(column.getLength() == null ? 0 : column.getLength());
            columnMeta.setPrecision(column.getPrecision() == null ? 0 : column.getPrecision());
            columnMeta.setScale(column.getScale() == null ? 0 : column.getScale());
            columnMeta.setJdbcType(JDBCType.valueOf(column.getJdbcType()));
            columnMeta.setJavaType(getJavaType(column.getJavaType()));
            columnMeta.setProperties(column.getProperties() == null ? new HashMap<>() : column.getProperties());
            if (StringUtils.isEmpty(column.getDataType())) {
                Dialect dialect = database.getMeta().getDialect();
                columnMeta.setDataType(dialect.buildDataType(columnMeta));
            } else {
                columnMeta.setDataType(column.getDataType());
            }
            columnMeta.setValueConverter(initColumnValueConvert(columnMeta.getJdbcType(), columnMeta.getJavaType()));
            if (!StringUtils.isEmpty(column.getDictId()) && optionalConvertBuilder != null) {
                columnMeta.setOptionConverter(optionalConvertBuilder.buildFromDict(column.getDictId(), column.getDictParserId()));
            }
            customColumnSetting(database, form, metaData, column, columnMeta);
            return columnMeta;
        }).forEach(metaData::addColumn);
        customTableSetting(database, form, metaData);
        return metaData;
    }

    protected void customTableSetting(RDBDatabase database
            , DynamicFormEntity formEntity
            , RDBTableMetaData table) {

    }

    protected void customColumnSetting(RDBDatabase database
            , DynamicFormEntity formEntity
            , RDBTableMetaData table
            , DynamicFormColumnEntity columnEntity
            , RDBColumnMetaData column) {

    }

    protected ValueConverter initColumnValueConvert(JDBCType jdbcType, Class javaType) {
        boolean isBasicClass = !classMapping
                .values()
                .contains(javaType) || javaType != Map.class || javaType != List.class;

        switch (jdbcType) {
            case BLOB:
                if (!isBasicClass) {
                    return new JSONValueConverter(javaType, new BlobValueConverter());
                }
                return new BlobValueConverter();
            case CLOB:
                if (!isBasicClass) {
                    return new JSONValueConverter(javaType, new ClobValueConverter());
                }
                return new ClobValueConverter();
            case NUMERIC:
            case BIGINT:
            case INTEGER:
            case SMALLINT:
            case TINYINT:
                return new NumberValueConverter(javaType);
            case DATE:
            case TIMESTAMP:
            case TIME:
                return new DateTimeConverter("yyyy-MM-dd HH:mm:ss", javaType);
            default:
                if (!isBasicClass) {
                    return new JSONValueConverter(javaType, new DefaultValueConverter());
                }
                if (javaType == String.class && (jdbcType == JDBCType.VARCHAR || jdbcType == JDBCType.NVARCHAR)) {
                    return new DefaultValueConverter() {
                        @Override
                        public Object getData(Object value) {
                            if (value instanceof Number) {
                                return value.toString();
                            }
                            return super.getData(value);
                        }
                    };
                }
                return new DefaultValueConverter();
        }

    }

    static final Map<String, Class> classMapping = new HashMap<>();

    static {
        classMapping.put("string", String.class);
        classMapping.put("String", String.class);
        classMapping.put("int", Integer.class);
        classMapping.put("Integer", Integer.class);
        classMapping.put("byte", Byte.class);
        classMapping.put("Byte", Byte.class);
        classMapping.put("short", Short.class);
        classMapping.put("Short", Short.class);
        classMapping.put("boolean", Boolean.class);
        classMapping.put("Boolean", Boolean.class);
        classMapping.put("double", Double.class);
        classMapping.put("Double", Double.class);
        classMapping.put("float", Float.class);
        classMapping.put("Float", Float.class);
        classMapping.put("long", Long.class);
        classMapping.put("Long", Long.class);
        classMapping.put("char", Character.class);
        classMapping.put("Char", Character.class);
        classMapping.put("Character", Character.class);
        classMapping.put("map", Map.class);
        classMapping.put("Map", Map.class);
        classMapping.put("list", List.class);
        classMapping.put("List", List.class);

        classMapping.put("date", Date.class);
        classMapping.put("Date", Date.class);

    }

    private Class getJavaType(String type) {
        if (StringUtils.isEmpty(type)) return String.class;
        Class clazz = classMapping.get(type);
        if (clazz == null) {
            try {
                clazz = Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return clazz;
    }

}
