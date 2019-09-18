package test.quxiqi.sharding.sphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.quxiqi.sharding.sphere.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
public interface ExampleRepository extends JpaRepository<Example, Long> {

    List<Example> findByCreateTimeBefore(Date createTime);
    List<Example> findByRelId(Long relId);
    List<Example> findByRelIdOrCode(Long relId, String code);
    List<Example> findByIdBetween(long startId, long endId);
}
