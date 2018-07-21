package org.hswebframework.web.service.form.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.ezorm.core.ObjectWrapperFactory;
import org.hswebframework.ezorm.core.Trigger;
import org.hswebframework.ezorm.core.ValidatorFactory;
import org.hswebframework.ezorm.core.ValueConverter;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.meta.Correlation;
import org.hswebframework.ezorm.rdb.meta.RDBColumnMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.meta.converter.*;
import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.ezorm.rdb.simple.trigger.ScriptTraggerSupport;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.form.DynamicFormColumnDao;
import org.hswebframework.web.dao.form.DynamicFormDao;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.entity.form.*;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLDeleteService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.form.*;
import org.hswebframework.web.service.form.events.FormDeployEvent;
import org.hswebframework.web.service.form.initialize.ColumnInitializeContext;
import org.hswebframework.web.service.form.initialize.DynamicFormInitializeCustomer;
import org.hswebframework.web.service.form.initialize.TableInitializeContext;
import org.hswebframework.web.service.form.simple.cluster.ClusterDatabase;
import org.hswebframework.web.service.form.simple.convert.SmartValueConverter;
import org.hswebframework.web.service.form.simple.dict.EnumDictValueConverter;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dynamicFormService")
@CacheConfig(cacheNames = "dyn-form")
public class SimpleDynamicFormService extends GenericEntityService<DynamicFormEntity, String>
        implements DynamicFormService, FormDeployService {

    @Value("${hsweb.dynamic-form.tags:none}")
    private String[] tags;

    @Value("${hsweb.dynamic-form.tag:none}")
    private String tag;

    @Value("${hsweb.dynamic-form.load-only-tags:null}")
    private String[] loadOnlyTags;

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

    @Autowired(required = false)
    private List<DynamicFormInitializeCustomer> initializeCustomers;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired(required = false)
    private ObjectWrapperFactory objectWrapperFactory;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DynamicFormDao getDao() {
        return dynamicFormDao;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "dyn-form-deploy", allEntries = true),
            @CacheEvict(value = "dyn-form", allEntries = true),
    })
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deployAllFromLog() {

        List<String> tags = new ArrayList<>(Arrays.asList(this.tags));
        if (loadOnlyTags != null) {
            tags.addAll(Arrays.asList(loadOnlyTags));
        }
        List<DynamicFormEntity> entities = createQuery()
                .select(DynamicFormEntity.id)
                .where(DynamicFormEntity.deployed, true)
                .and()
                .in(DynamicFormEntity.tags, tags)
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
    @Caching(evict = {
            @CacheEvict(value = "dyn-form-deploy", allEntries = true),
            @CacheEvict(value = "dyn-form", allEntries = true),
    })
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
        DynamicFormColumnBindEntity bindEntity = new DynamicFormColumnBindEntity();
        bindEntity.setForm(form);
        bindEntity.setColumns(columns);
        entity.setMetaData(JSON.toJSONString(bindEntity));
        return entity;
    }


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deployFromLog(DynamicFormDeployLogEntity logEntity) {
        DynamicFormColumnBindEntity entity = JSON.parseObject(logEntity.getMetaData(), DynamicFormColumnBindEntity.class);
        DynamicFormEntity form = entity.getForm();
        List<DynamicFormColumnEntity> columns = entity.getColumns();
        if (logger.isDebugEnabled()) {
            logger.debug("do deploy form {} , columns size:{}", form.getName(), columns.size());
        }

        deploy(form, columns, !(loadOnlyTags != null && Arrays.asList(loadOnlyTags).contains(entity.getForm().getTags())));
    }


    @Override
    @CacheEvict(key = "'form_id:'+#entity.id")
    public String insert(DynamicFormEntity entity) {
        entity.setDeployed(false);
        entity.setVersion(1L);
        entity.setCreateTime(System.currentTimeMillis());
        entity.setTags(tag);
        return super.insert(entity);
    }

    @Override
    @Cacheable(key = "'form_id:'+#id")
    public DynamicFormEntity selectByPk(String id) {
        return super.selectByPk(id);
    }

    @Override
    @CacheEvict(key = "'form_id:'+#id")
    public int updateByPk(String id, DynamicFormEntity entity) {
        entity.setVersion(null);
        entity.setDeployed(null);
        entity.setUpdateTime(System.currentTimeMillis());
        getDao().incrementVersion(id);
        return super.updateByPk(id, entity);
    }

    protected void initDatabase(RDBDatabase database) {

    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "dyn-form-deploy", allEntries = true),
            @CacheEvict(value = "dyn-form", allEntries = true),
    })
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
        createUpdate().set(DynamicFormEntity.deployed, false).where(DynamicFormEntity.id, formId).exec();
        eventPublisher.publishEvent(new FormDeployEvent(formId));
    }

    private String saveOrUpdate0(DynamicFormColumnEntity columnEntity) {
        if (StringUtils.isEmpty(columnEntity.getId())
                || DefaultDSLQueryService.createQuery(formColumnDao)
                .where(DynamicFormColumnEntity.id, columnEntity.getId())
                .total() == 0) {
            if (StringUtils.isEmpty(columnEntity.getId())) {
                columnEntity.setId(getIDGenerator().generate());
            }
            tryValidate(columnEntity, CreateGroup.class);
            formColumnDao.insert(columnEntity);
        } else {
            tryValidate(columnEntity, UpdateGroup.class);
            DefaultDSLUpdateService
                    .createUpdate(formColumnDao, columnEntity)
                    .where(DynamicFormColumnEntity.id, columnEntity.getId())
                    .exec();
        }
        return columnEntity.getId();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'form-columns:'+#columnEntity.formId"),
                    @CacheEvict(key = "'form_id:'+#columnEntity.formId"),

            }
    )
    public String saveOrUpdateColumn(DynamicFormColumnEntity columnEntity) {
        String id = saveOrUpdate0(columnEntity);
        getDao().incrementVersion(columnEntity.getFormId());
        return id;
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<String> saveOrUpdateColumn(List<DynamicFormColumnEntity> columnEntities) {
        Set<String> formId = new HashSet<>();

        List<String> columnIds = columnEntities.stream()
                .peek(columnEntity -> formId.add(columnEntity.getFormId()))
                .map(this::saveOrUpdateColumn)
                .collect(Collectors.toList());

        formId.forEach(getDao()::incrementVersion);
        return columnIds;

    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'form-columns:'+#result"),
                    @CacheEvict(key = "'form_id:'+#result"),

            }
    )
    public String saveOrUpdate(DynamicFormColumnBindEntity bindEntity) {
        DynamicFormEntity formEntity = bindEntity.getForm();

        List<DynamicFormColumnEntity> columnEntities = bindEntity.getColumns();
        //保存表单
        saveOrUpdate(formEntity);

        //保存表单列
        columnEntities.stream()
                .peek(column -> column.setFormId(formEntity.getId()))
                .forEach(this::saveOrUpdate0);

        return formEntity.getId();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'form-columns:'+#formId"),
                    @CacheEvict(key = "'form_id:'+#formId"),

            }
    )
    public DynamicFormColumnEntity deleteColumn(String formId) {
        DynamicFormColumnEntity oldColumn = DefaultDSLQueryService
                .createQuery(formColumnDao)
                .where(DynamicFormColumnEntity.id, formId)
                .single();
        assertNotNull(oldColumn);
        DefaultDSLDeleteService.createDelete(formColumnDao)
                .where(DynamicFormDeployLogEntity.id, formId)
                .exec();
        return oldColumn;
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'form-columns:'+#id"),
                    @CacheEvict(key = "'form_id:'+#id")
            })
    public DynamicFormEntity deleteByPk(String id) {
        Objects.requireNonNull(id, "id can not be null");

        DefaultDSLDeleteService.createDelete(formColumnDao)
                .where(DynamicFormColumnEntity.formId, id)
                .exec();
        return super.deleteByPk(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<DynamicFormColumnEntity> deleteColumn(List<String> ids) {
        Objects.requireNonNull(ids);
        if (ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<DynamicFormColumnEntity> oldColumns = DefaultDSLQueryService
                .createQuery(formColumnDao)
                .where()
                .in(DynamicFormColumnEntity.id, ids)
                .listNoPaging();

        DefaultDSLDeleteService.createDelete(formColumnDao)
                .where().in(DynamicFormDeployLogEntity.id, ids)
                .exec();
        return oldColumns;
    }

    @Override
    public List<DynamicFormColumnEntity> selectColumnsByFormId(String formId) {
        Objects.requireNonNull(formId);
        return DefaultDSLQueryService.createQuery(formColumnDao)
                .where(DynamicFormColumnEntity.formId, formId)
                .orderByAsc(DynamicFormColumnEntity.sortIndex)
                .listNoPaging();
    }

    @Override
    @Cacheable(value = "dyn-form-deploy", key = "'form-deploy:'+#formId+':'+#version")
    public DynamicFormColumnBindEntity selectDeployed(String formId, int version) {
        DynamicFormDeployLogEntity entity = dynamicFormDeployLogService.selectDeployed(formId, version);
        if (entity == null) {
            return null;
        }
        return JSON.parseObject(entity.getMetaData(), DynamicFormColumnBindEntity.class);
    }

    @Override
    @Cacheable(value = "dyn-form-deploy", key = "'form-deploy-version:'+#formId")
    public long selectDeployedVersion(String formId) {
        DynamicFormColumnBindEntity entity = selectLatestDeployed(formId);
        if (null != entity) {
            return entity.getForm().getVersion();
        }
        return 0L;
    }

    @Override
    @Cacheable(value = "dyn-form-deploy", key = "'form-deploy:'+#formId+':latest'")
    public DynamicFormColumnBindEntity selectLatestDeployed(String formId) {
        DynamicFormDeployLogEntity entity = dynamicFormDeployLogService.selectLastDeployed(formId);
        if (entity == null) {
            return null;
        }
        return JSON.parseObject(entity.getMetaData(), DynamicFormColumnBindEntity.class);
    }

    @Override
    public DynamicFormColumnBindEntity selectEditing(String formId) {
        Objects.requireNonNull(formId);
        return new DynamicFormColumnBindEntity(selectByPk(formId), selectColumnsByFormId(formId));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "dyn-form-deploy", key = "'form-deploy-version:'+#formId"),
            @CacheEvict(value = "dyn-form-deploy", key = "'form-deploy:'+#formId+':latest'"),
            @CacheEvict(value = "dyn-form", allEntries = true)
    })
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deploy(String formId) {
        DynamicFormEntity formEntity = selectByPk(formId);
        assertNotNull(formEntity);
        if (Boolean.TRUE.equals(formEntity.getDeployed())) {
            dynamicFormDeployLogService.cancelDeployed(formId);
        }
        List<DynamicFormColumnEntity> columns = selectColumnsByFormId(formId);
        deploy(formEntity, columns, true);
        createUpdate().set(DynamicFormEntity.deployed, true).where(DynamicFormEntity.id, formId).exec();
        try {
            dynamicFormDeployLogService.insert(createDeployLog(formEntity, columns));
            eventPublisher.publishEvent(new FormDeployEvent(formId));
        } catch (Exception e) {
            unDeploy(formId);
            throw e;
        }
    }

    public void deploy(DynamicFormEntity form, List<DynamicFormColumnEntity> columns, boolean updateMeta) {
        RDBDatabase database = StringUtils.isEmpty(form.getDataSourceId())
                ? databaseRepository.getDefaultDatabase()
                : databaseRepository.getDatabase(form.getDataSourceId());
        initDatabase(database);
        RDBTableMetaData metaData = buildTable(database, form, columns);
        try {
            if (!database.getMeta().getParser().tableExists(metaData.getName())) {
                database.createTable(metaData);
            } else {
                if (!updateMeta) {
                    database.reloadTable(metaData);
                } else {
                    database.alterTable(metaData);
                }
            }
        } catch (SQLException e) {
            throw new DynamicFormException("部署失败:" + e.getMessage(), e);
        }
    }

    protected Set<Correlation> buildCorrelations(String correlations) {
        if (StringUtils.isEmpty(correlations)) {
            return new LinkedHashSet<>();
        }
        JSONArray correlationsConfig = JSON.parseArray(correlations);
        Set<Correlation> correlations1 = new LinkedHashSet<>();
        for (int i = 0; i < correlationsConfig.size(); i++) {
            JSONObject single = correlationsConfig.getJSONObject(i);

            String target = single.getString("target");
            String alias = single.getString("alias");
            String condition = single.getString("condition");
            Objects.requireNonNull(target);
            Objects.requireNonNull(condition);
            Correlation correlation = new Correlation(target, alias, condition);
            correlation.setJoin(Correlation.JOIN.valueOf(String.valueOf(single.getOrDefault("join", "LEFT")).toUpperCase()));
            JSONObject properties = single.getJSONObject("properties");

            if (properties != null) {
                properties.forEach(correlation::setProperty);
            }
            correlations1.add(correlation);
        }

        return correlations1;

    }

    protected Map<String, Trigger> buildTrigger(String config) {
        if (StringUtils.isEmpty(config)) {
            return new HashMap<>();
        }
        JSONArray triggerConfig = JSON.parseArray(config);
        Map<String, Trigger> triggers = new HashMap<>();
        for (int i = 0; i < triggerConfig.size(); i++) {
            JSONObject single = triggerConfig.getJSONObject(i);
            String trigger = single.getString("trigger");
            String language = single.getString("language");
            String script = single.getString("script");
            String scriptId = DigestUtils.md5Hex(script);
            try {
                DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(language);
                if (engine == null) {
                    throw new UnsupportedOperationException("not support script language : " + language);
                }
                if (!engine.compiled(scriptId)) {
                    engine.compile(scriptId, script);
                }
                Trigger singleTrigger = new ScriptTraggerSupport(engine, scriptId);
                triggers.put(trigger, singleTrigger);
            } catch (Exception e) {
                throw new BusinessException("compile script error :" + e.getMessage(), e);
            }
        }
        return triggers;
    }

    protected RDBTableMetaData buildTable(RDBDatabase database, DynamicFormEntity form, List<DynamicFormColumnEntity> columns) {
        RDBTableMetaData metaData = new RDBTableMetaData();
        metaData.setComment(form.getDescribe());
        metaData.setName(form.getDatabaseTableName());
        if (null != form.getProperties()) {
            metaData.setProperties(form.getProperties());
        }
        metaData.setProperty("version", form.getVersion());
        metaData.setProperty("formId", form.getId());

        metaData.setAlias(form.getAlias());
        metaData.setCorrelations(buildCorrelations(form.getCorrelations()));
        buildTrigger(form.getTriggers()).forEach(metaData::on);

        columns.forEach(column -> {
            RDBColumnMetaData columnMeta = new RDBColumnMetaData();
            columnMeta.setName(column.getColumnName());
            columnMeta.setAlias(column.getAlias());
            columnMeta.setComment(column.getDescribe());
            columnMeta.setLength(column.getLength() == null ? 0 : column.getLength());
            columnMeta.setPrecision(column.getPrecision() == null ? 0 : column.getPrecision());
            columnMeta.setScale(column.getScale() == null ? 0 : column.getScale());
            columnMeta.setJdbcType(JDBCType.valueOf(column.getJdbcType()));
            columnMeta.setJavaType(getJavaType(column.getJavaType()));
            columnMeta.setProperties(column.getProperties() == null ? new HashMap<>() : column.getProperties());
            if (!CollectionUtils.isEmpty(column.getValidator())) {
                columnMeta.setValidator(new HashSet<>(column.getValidator()));
            }
            if (StringUtils.isEmpty(column.getDataType())) {
                Dialect dialect = database.getMeta().getDialect();
                columnMeta.setDataType(dialect.buildDataType(columnMeta));
            } else {
                columnMeta.setDataType(column.getDataType());
            }
            columnMeta.setValueConverter(initColumnValueConvert(columnMeta.getJdbcType(), columnMeta.getJavaType()));

            if (optionalConvertBuilder != null && null != column.getDictConfig()) {
                try {
                    DictConfig config = JSON.parseObject(column.getDictConfig(), DictConfig.class);
                    config.setColumn(columnMeta);
                    columnMeta.setOptionConverter(optionalConvertBuilder.build(config));
                    ValueConverter converter = optionalConvertBuilder.buildValueConverter(config);
                    if (null != converter) {
                        columnMeta.setValueConverter(converter);
                    }
                } catch (Exception e) {
                    logger.warn("创建字典转换器失败", e);
                }
            }
            customColumnSetting(database, form, metaData, column, columnMeta);
            metaData.addColumn(columnMeta);
        });
        if (objectWrapperFactory != null) {
            metaData.setObjectWrapper(objectWrapperFactory.createObjectWrapper(metaData));
        }
        metaData.setValidator(validatorFactory.createValidator(metaData));

        customTableSetting(database, form, metaData);
        //没有主键并且没有id字段
        if (metaData.getColumns().stream().noneMatch(RDBColumnMetaData::isPrimaryKey) && metaData.findColumn("id") == null) {
            RDBColumnMetaData primaryKey = createPrimaryKeyColumn();
            Dialect dialect = database.getMeta().getDialect();
            primaryKey.setDataType(dialect.buildDataType(primaryKey));
            metaData.addColumn(primaryKey);
        }

        return metaData;
    }

    protected RDBColumnMetaData createPrimaryKeyColumn() {
        RDBColumnMetaData id = new RDBColumnMetaData();
        id.setName("id");
        id.setJdbcType(JDBCType.VARCHAR);
        id.setJavaType(String.class);
        id.setLength(32);
        id.setDefaultValue(IDGenerator.MD5::generate);
        id.setComment("主键");
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setProperty("read-only", true);
        return id;
    }

    protected void customTableSetting(RDBDatabase database
            , DynamicFormEntity formEntity
            , RDBTableMetaData table) {
        TableInitializeContext context = new TableInitializeContext() {
            @Override
            public RDBDatabase getDatabase() {
                return database;
            }

            @Override
            public DynamicFormEntity getFormEntity() {
                return formEntity;
            }

            @Override
            public RDBTableMetaData getTable() {
                return table;
            }
        };
        if (!CollectionUtils.isEmpty(initializeCustomers)) {
            initializeCustomers.forEach(customer -> customer.customTableSetting(context));
        }
    }

    protected void customColumnSetting(RDBDatabase database
            , DynamicFormEntity formEntity
            , RDBTableMetaData table
            , DynamicFormColumnEntity columnEntity
            , RDBColumnMetaData column) {
        ColumnInitializeContext context = new ColumnInitializeContext() {
            @Override
            public DynamicFormColumnEntity getColumnEntity() {
                return columnEntity;
            }

            @Override
            public RDBColumnMetaData getColumn() {
                return column;
            }

            @Override
            public RDBDatabase getDatabase() {
                return database;
            }

            @Override
            public DynamicFormEntity getFormEntity() {
                return formEntity;
            }

            @Override
            public RDBTableMetaData getTable() {
                return table;
            }
        };
        if (!CollectionUtils.isEmpty(initializeCustomers)) {
            initializeCustomers.forEach(customer -> customer.customTableColumnSetting(context));
        }
    }

    protected ValueConverter initColumnValueConvert(JDBCType jdbcType, Class javaType) {
        boolean isBasicClass = !classMapping
                .values()
                .contains(javaType) || javaType != Map.class || javaType != List.class;

        if (javaType.isEnum() && EnumDict.class.isAssignableFrom(javaType)) {
            return new EnumDictValueConverter<EnumDict>(() -> (List) Arrays.asList(javaType.getEnumConstants()));
        }
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

    private static final Map<String, Class> classMapping = new HashMap<>();

    static {
        classMapping.put("string", String.class);
        classMapping.put("String", String.class);
        classMapping.put("int", Integer.class);
        classMapping.put("Integer", Integer.class);
        classMapping.put("byte", Byte.class);
        classMapping.put("Byte", Byte.class);

        classMapping.put("byte[]", Byte[].class);
        classMapping.put("Byte[]", Byte[].class);

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
        classMapping.put("char[]", Character[].class);
        classMapping.put("Char[]", Character[].class);

        classMapping.put("Character", Character.class);

        classMapping.put("BigDecimal", BigDecimal.class);
        classMapping.put("BigInteger", BigInteger.class);

        classMapping.put("map", Map.class);
        classMapping.put("Map", Map.class);
        classMapping.put("list", List.class);
        classMapping.put("List", List.class);

        classMapping.put("date", Date.class);
        classMapping.put("Date", Date.class);

    }

    private Class getJavaType(String type) {
        if (StringUtils.isEmpty(type)) {
            return String.class;
        }
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
