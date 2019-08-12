package test.quxiqi.sharding.sphere.config.sharding.key;

import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;
import org.springframework.stereotype.Component;
import test.quxiqi.sharding.sphere.utils.SpringComponentHolder;

import java.util.Properties;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
@Component
public class RedisKeyGenerator implements ShardingKeyGenerator {
    private Properties properties;
    private String logicTableName;

    @Override
    public Comparable<?> generateKey() {
        return SpringComponentHolder.getStringRedisTemplate()
                .opsForValue().increment(logicTableName + ":key", 1);
    }

    @Override
    public String getType() {
        return "REDIS";
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Properties properties) {
        logicTableName = properties.getProperty("logicTableName");
        if(logicTableName == null || logicTableName.isEmpty()) {
            throw new IllegalStateException("REDIS主键生成 - 没有配置逻辑表名！");
        }
        this.properties = properties;
    }
}
