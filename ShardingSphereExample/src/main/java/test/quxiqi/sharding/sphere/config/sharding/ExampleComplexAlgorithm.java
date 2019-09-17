package test.quxiqi.sharding.sphere.config.sharding;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/16
 */
public class ExampleComplexAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Long> shardingValue) {
        Collection<String> routeTables = new LinkedList<>();
        Map<String, Collection<Long>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();
        Collection<Long> idValues = columnNameAndShardingValuesMap.get("id");
        if (idValues != null) {

        }
        Collection<Long> name = columnNameAndShardingValuesMap.get("name");
        return availableTargetNames;
    }
}
