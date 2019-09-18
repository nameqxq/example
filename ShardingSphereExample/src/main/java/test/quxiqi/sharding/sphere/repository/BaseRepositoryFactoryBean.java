package test.quxiqi.sharding.sphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
public class BaseRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {
	@Override
	protected RepositoryFactorySupport createRepositoryFactory(
			EntityManager entityManager) {
		return new BaseDaoFactory(entityManager);
	}

	private static class BaseDaoFactory<T, ID extends Serializable> extends JpaRepositoryFactory {

		public BaseDaoFactory(EntityManager entityManager) {
			super(entityManager);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected SimpleJpaRepository<T, ID> getTargetRepository(RepositoryInformation information, EntityManager entityManager) {
			return new BaseRepositoryImpl<T, ID>((Class<T>) information.getDomainType(), entityManager);
		}

		@Override
		protected Class getRepositoryBaseClass(RepositoryMetadata metadata) {
			return BaseRepositoryImpl.class;
		}
	}
}
