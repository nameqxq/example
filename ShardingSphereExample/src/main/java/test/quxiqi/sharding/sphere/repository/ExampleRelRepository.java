package test.quxiqi.sharding.sphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.quxiqi.sharding.sphere.entity.ExampleRel;

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
public interface ExampleRelRepository extends JpaRepository<ExampleRel, Long> {

    List<ExampleRel> findByRelId(long relId);
    List<ExampleRel> findByRelIdIn(Collection<Long> relIds);
}
