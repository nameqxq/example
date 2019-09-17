package test.quxiqi.sharding.sphere.config.sharding;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.orchestration.yaml.swapper.OrchestrationConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationEncryptDataSourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 *
 *
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/17
 */
@Configuration
@RequiredArgsConstructor
public class ShardingJdbcConfig {

    private final SpringBootShardingOrchestrationConfigurationProperties springBootShardingOrchestrationConfigurationProperties;

    @Bean("shardingDataSource")
    public DataSource shardingDataSource() throws SQLException {
        OrchestrationConfigurationYamlSwapper swapper = new OrchestrationConfigurationYamlSwapper();
        return OrchestrationEncryptDataSourceFactory.createDataSource(swapper.swap(springBootShardingOrchestrationConfigurationProperties));
    }
}
