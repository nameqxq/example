package test.quxiqi.sharding.sphere.config.sharding.key;

import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
@Component
public class RedisKeyGenerator implements ShardingKeyGenerator, ApplicationContextAware {
    private static StringRedisTemplate redisTemplate;
    private Properties properties;

    @Override
    public Comparable<?> generateKey() {
        String logicTableName = properties.getProperty("logicTableName");
        if(logicTableName == null || logicTableName.isEmpty()) {
            throw new IllegalStateException("REDIS主键生成 - 没有配置逻辑表名！");
        }
        return redisTemplate.opsForValue().increment(logicTableName + ":key", 1);
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
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
    }
}
