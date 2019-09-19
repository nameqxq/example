package test.quxiqi.sharding.sphere.repository;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import test.quxiqi.sharding.sphere.config.sharding.ShardingEntity;
import test.quxiqi.sharding.sphere.utils.TableInfo;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@Slf4j
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    private final EntityManager em;
    private final JpaEntityInformation<T, ?> entityInformation;
    private Class<T> domainClass;
    @Autowired
    public BaseRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
        this.domainClass = domainClass;
        entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, em);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public <S extends T> S save(S entity) {
        if (entityInformation.isNew(entity)) {
            em.persist(entity);
            return entity;
        } else {
            if (entity instanceof ShardingEntity) {
                return shardingSave(entity);
            }
            return em.merge(entity);
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends T> S shardingSave(S entity) {

        TableInfo tableInfo = getAndCacheTableInfo(entity);
        if (tableInfo == null) {
            return em.merge(entity);
        }

        String updateSql = buildUpdateSql(tableInfo);

        Map<String, Object> valueMap = buildValueMap(entity, tableInfo);
        if (valueMap == null) {
            return em.merge(entity);
        }

        Query query = buildQuery(entity, updateSql, valueMap);
        query.executeUpdate();

        ID id = (ID) valueMap.get(tableInfo.getPrimaryKey().getName());
        return (S) findOne(id);
    }

    private <S extends T> TableInfo getAndCacheTableInfo(S entity) {
        SessionFactoryImpl session = em.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
        EntityPersister entityPersister = session.getEntityPersister(entity.getClass().getName());
        if (!(entityPersister instanceof SingleTableEntityPersister)) {
            return null;
        }
        SingleTableEntityPersister persister = (SingleTableEntityPersister)entityPersister;

        TableInfo tableInfo = new TableInfo();
        tableInfo.setName(persister.getTableName());
        List<TableInfo.ColumnInfo> columnInfos = new LinkedList<>();
        ShardingEntity shardingEntity = (ShardingEntity) entity;
        for (AttributeDefinition attribute : persister.getAttributes()) {
            String name = attribute.getName();
            Field field;
            try {
                field = entity.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                log.error("分片表表结构解析错误 --> table: {}", persister.getTableName(), e);
                return null;
            }

            field.setAccessible(true);
            String columnName = persister.getPropertyColumnNames(name)[0];

            if (name.equals(shardingEntity.primaryShardingKey())) {
                tableInfo.setPrimaryShardingKey(new TableInfo.ColumnInfo(columnName, columnName + "_shardingPrimaryKey", field));
            }
            columnInfos.add(new TableInfo.ColumnInfo(columnName, columnName, field));
        }

        Field field;
        try {
            field = entity.getClass().getDeclaredField(persister.getIdentifierPropertyName());
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            log.error("分片表表结构解析错误 --> table: {}", persister.getTableName(), e);
            return null;
        }
        tableInfo.setPrimaryKey(
                new TableInfo.ColumnInfo(persister.getIdentifierPropertyName(), persister.getIdentifierPropertyName() + "_primaryKey", field));
        columnInfos.add(new TableInfo.ColumnInfo(persister.getIdentifierPropertyName(), persister.getIdentifierPropertyName(), field));
        tableInfo.setColumnInfos(columnInfos);
        return tableInfo;
    }

    private String buildUpdateSql(TableInfo tableInfo) {
        StringBuilder updateSql=
                new StringBuilder("update ")
                        .append(tableInfo.getName())
                        .append(" set ");
        //拼装set语句
        for (TableInfo.ColumnInfo columnInfo : tableInfo.getColumnInfos()) {
            updateSql.append(columnInfo.getName()).append("=:").append(columnInfo.getValueName()).append(",");
        }
        // 拼装where语句
        updateSql
                //去掉最后一个","
                .deleteCharAt(updateSql.length() - 1)
                .append(" where ")
                .append(tableInfo.getPrimaryKey().getName())
                .append(" = :")
                .append(tableInfo.getPrimaryKey().getValueName())
                .append(" and ")
                .append(tableInfo.getPrimaryShardingKey().getName())
                .append(" = :")
                .append(tableInfo.getPrimaryShardingKey().getValueName());
        return updateSql.toString();
    }

    private <S extends T> Map<String, Object> buildValueMap(S entity, TableInfo tableInfo) {
        Map<String, Object> valueMap;
        try {
            valueMap = new HashMap<>(tableInfo.getColumnInfos().size() + 2, 1);
            for (TableInfo.ColumnInfo columnInfo : tableInfo.getColumnInfos()) {
                valueMap.put(columnInfo.getValueName(), columnInfo.getField().get(entity));
            }
            valueMap.put(tableInfo.getPrimaryKey().getValueName(), tableInfo.getPrimaryKey().getField().get(entity));
            valueMap.put(tableInfo.getPrimaryShardingKey().getValueName(), tableInfo.getPrimaryShardingKey().getField().get(entity));
        } catch (IllegalAccessException e) {
            log.error("获取实体信息失败 --> tableInfo: {}", JSONObject.valueToString(tableInfo), e);
            return null;
        }
        return valueMap;
    }

    private <S extends T> Query buildQuery(S entity, String updateSql, Map<String, Object> valueMap) {
        Query query = em.createNativeQuery(updateSql, entity.getClass());
        setParams(query, valueMap);
        return query;
    }

    private void setParams(Query query, Map<String, Object> map) {
        if (map != null) {
            for (String key : map.keySet()) {
                Object value = map.get(key);
                if (value instanceof Date) {
                    query.setParameter(key, (Date) map.get(key), TemporalType.TIMESTAMP);
                } else {
                    query.setParameter(key, map.get(key));
                }
            }
        }
    }
}
