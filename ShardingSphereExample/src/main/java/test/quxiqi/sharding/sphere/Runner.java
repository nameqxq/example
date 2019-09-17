package test.quxiqi.sharding.sphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import test.quxiqi.sharding.sphere.config.sharding.SpringBootShardingOrchestrationConfigurationProperties;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */

@EnableConfigurationProperties(SpringBootShardingOrchestrationConfigurationProperties.class)
@EnableJpaRepositories
@EnableTransactionManagement
@SpringBootApplication
public class Runner {
    public static void main(String[] args) {
        SpringApplication.run(Runner.class);
    }
}
