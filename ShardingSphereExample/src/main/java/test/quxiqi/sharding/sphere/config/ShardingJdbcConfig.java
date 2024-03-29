// package test.quxiqi.sharding.sphere.config;
//
// import org.apache.shardingsphere.orchestration.yaml.swapper.OrchestrationConfigurationYamlSwapper;
// import org.apache.shardingsphere.shardingjdbc.orchestration.api.OrchestrationEncryptDataSourceFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Bean;
// import test.quxiqi.sharding.sphere.config.sharding.SpringBootShardingOrchestrationConfigurationProperties;
//
// import javax.sql.DataSource;
// import java.sql.SQLException;
//
// /**
//  *
//  *
//  * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
//  * @version 1.0 2019 9月.2019/9/17
//  */
// // @Configuration
// public class ShardingJdbcConfig {
//
//     @Autowired
//     private SpringBootShardingOrchestrationConfigurationProperties springBootShardingOrchestrationConfigurationProperties;
//
//     @Bean("shardingDataSource")
//     public DataSource shardingDataSource(){
//         OrchestrationConfigurationYamlSwapper swapper = new OrchestrationConfigurationYamlSwapper();
//         try {
//             return OrchestrationEncryptDataSourceFactory.createDataSource(swapper.swap(springBootShardingOrchestrationConfigurationProperties));
//         } catch (SQLException e) {
//             throw new RuntimeException(e);
//         }
//     }
// }
