package test.quxiqi.sharding.sphere.config.sharding;

/**
 * 分表实体接口
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/18
 */
public interface ShardingEntity<T> {
    String primaryShardingKey();
}
