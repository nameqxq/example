package test.quxiqi.sharding.sphere.config.sharding;

import org.apache.shardingsphere.orchestration.yaml.config.YamlOrchestrationConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/17
 */
@ConfigurationProperties(prefix = "spring.shardingsphere.orchestration")
public class SpringBootShardingOrchestrationConfigurationProperties extends YamlOrchestrationConfiguration {
}
