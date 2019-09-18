package test.quxiqi.sharding.sphere.repository;

import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import test.quxiqi.sharding.sphere.config.sharding.ShardingEntity;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    private final EntityManager entityManager;
    // private final JpaEntityInformation<T, ?> entityInformation;

    @Autowired
    public BaseRepositoryImpl(Class<T> tClass, EntityManager em) {
        super(tClass, em);
        this.entityManager = em;
    }

    @Override
    public <S extends T> S save(S entity) {
        if (entity instanceof ShardingEntity) {
            return shardingSave(entity);
        }
        return super.save(entity);
    }

    private <S extends T> S shardingSave(S entity) {
        SessionFactoryImpl session = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
        EntityPersister entityPersister = session.getEntityPersister(((ShardingEntity) entity).tableName());
        ClassMetadata classMetadata = session.getClassMetadata(entity.getClass());

        if (!(entityPersister instanceof SingleTableEntityPersister)) {
            return super.save(entity);
        }
        SingleTableEntityPersister singleTableEntityPersister = (SingleTableEntityPersister)entityPersister;
        Iterable<AttributeDefinition> attributes = singleTableEntityPersister.getAttributes();

        return super.save(entity);
    }
}
