package test.quxiqi.sharding.sphere.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@Component
public class TableUtils implements ApplicationContextAware {
    private static final ConcurrentHashMap<String, Integer> EXIST_TABLE_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private static JdbcTemplate jdbcTemplate;
    private static final String CREATE_IF_NOT_EXIST = "CREATE TABLE IF NOT EXISTS %s LIKE %s";
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    }

    public static void createIfNotExist(String table, String sourceTable) {
        EXIST_TABLE_CACHE.computeIfAbsent(table, s -> {
            jdbcTemplate.execute(String.format(CREATE_IF_NOT_EXIST, table, sourceTable));
            return 1;
        });
    }
}
