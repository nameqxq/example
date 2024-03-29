package test.quxiqi.sharding.sphere.repository;

import test.quxiqi.sharding.sphere.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
public interface ExampleRepository extends BaseRepository<Example, Long> {

    List<Example> findByCreateTimeBetween(Date start, Date end);
    List<Example> findByRelId(Long relId);
    List<Example> findByRelIdOrCode(Long relId, String code);
    List<Example> findByCode(String code);
    List<Example> findByCodeIn(List<String> codes);
    List<Example> findByCodeInAndRelId(List<String> codes, Long relId);
}
