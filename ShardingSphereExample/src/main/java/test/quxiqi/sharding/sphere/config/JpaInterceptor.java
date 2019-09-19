package test.quxiqi.sharding.sphere.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/19
 */
@Slf4j
public class JpaInterceptor implements StatementInspector {
    @Override
    public String inspect(String sql) {
        // log.info("拦截器SQL --> {}", sql);
        return sql;
    }
}
