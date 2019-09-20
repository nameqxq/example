package test.quxiqi.sharding.sphere.config.sharding;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分表实体接口
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/18
 */
public interface ShardingEntity {
    ConcurrentHashMap<ShardingEntity, Set<Set>> HAS_UPDATE = new ConcurrentHashMap<>();
    String primaryShardingKey();
    boolean hasUpdate(String propName);
    default boolean hasUpdate(ShardingEntity shardingEntity, String propName) {
        return false;
    }
    void clear();
}
