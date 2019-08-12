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
        /*
         * i want to have TableRule in this method
         * then i can do like this
         * redisTemplate.opsForValue().increment("$logicTableName:key", 1);
         * or other special rules
         * now i must create a special XxxRedisKeyGenerator for every logicTable
         */
        return redisTemplate.opsForValue().increment("test:key", 1);
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
