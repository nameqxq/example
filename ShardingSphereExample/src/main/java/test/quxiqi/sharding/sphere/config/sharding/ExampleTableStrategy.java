package test.quxiqi.sharding.sphere.config.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

/**
 * 一个简单的路由策略，主要试验一下 java代码描述路由规则
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
public class ExampleTableStrategy implements PreciseShardingAlgorithm<Long> {

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        return preciseShardingValue.getLogicTableName() + "_" + preciseShardingValue.getValue()%2;
    }

}
