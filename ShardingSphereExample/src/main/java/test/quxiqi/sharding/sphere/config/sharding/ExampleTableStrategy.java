package test.quxiqi.sharding.sphere.config.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import test.quxiqi.sharding.sphere.utils.TableUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExampleTableStrategy implements PreciseShardingAlgorithm<Timestamp>, RangeShardingAlgorithm<Timestamp> {

    private static DateTimeFormatter toFormatter   = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Timestamp> preciseShardingValue) {
        LocalDateTime startTime = preciseShardingValue.getValue().toLocalDateTime();
        String physicalTableName = preciseShardingValue.getLogicTableName() + "_" + startTime.format(toFormatter);
        TableUtils.createIfNotExist(physicalTableName, preciseShardingValue.getLogicTableName());
        return physicalTableName;
    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Timestamp> rangeShardingValue) {
        List<String> list = new ArrayList<>();
        Range<Timestamp> ranges = rangeShardingValue.getValueRange();
        LocalDateTime startTime = ranges.lowerEndpoint().toLocalDateTime();
        LocalDateTime endTime = ranges.upperEndpoint().toLocalDateTime();
        while (startTime.isBefore(endTime)) {
            list.add(rangeShardingValue.getLogicTableName() + "_" + startTime.format(toFormatter));
            startTime = startTime.plusMonths(1);
        }
        return list;
    }
}
