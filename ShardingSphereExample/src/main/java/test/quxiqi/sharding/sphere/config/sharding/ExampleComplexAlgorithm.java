package test.quxiqi.sharding.sphere.config.sharding;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import test.quxiqi.sharding.sphere.entity.ExampleRel;
import test.quxiqi.sharding.sphere.utils.SpringComponentHolder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/16
 */
public class ExampleComplexAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {

    private static final String CODE = "code";
    private static final String REL_ID = "rel_id";

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Comparable<?>> shardingValue) {
        Collection<String> routeTables = new HashSet<>();
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();

        Collection<Comparable<?>> codes = columnNameAndShardingValuesMap.get(CODE);
        Collection<Comparable<?>> relIds = columnNameAndShardingValuesMap.get(REL_ID);
        if (CollectionUtils.isNotEmpty(codes)) {
            Set<String> codeSet = codes.stream().map(code -> (String) code).collect(Collectors.toSet());
            for (String code : codeSet) {
                Integer integer = Integer.valueOf(code.split("-")[1]);
                routeTables.add(shardingValue.getLogicTableName() + "_" + integer % 2);
            }
            return routeTables;
        } else if (CollectionUtils.isNotEmpty(relIds)) {
            Set<Long> ids = relIds.stream().map(relId -> (Long)relId).collect(Collectors.toSet());
            for (ExampleRel rel : SpringComponentHolder.getExampleRelRepository().findByRelIdIn(ids)) {
                Integer integer = Integer.valueOf(rel.getCode().split("-")[1]);
                routeTables.add(shardingValue.getLogicTableName() + "_" + integer % 2);
            }
        } else {
            routeTables = availableTargetNames;
        }
        return routeTables;
    }
}
