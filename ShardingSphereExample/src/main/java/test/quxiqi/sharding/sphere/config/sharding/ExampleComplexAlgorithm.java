package test.quxiqi.sharding.sphere.config.sharding;

import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import test.quxiqi.sharding.sphere.entity.ExampleRel;
import test.quxiqi.sharding.sphere.utils.SpringComponentHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/16
 */
public class ExampleComplexAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {
    private static final String CODE = "code";
    private static final String REL_ID = "rel_id";
    private static final String CREATE_TIME = "create_time";
    private static final String DEFAULT_DATE_PATTERN = "yyyyMMdd HH:mm:ss";

    private static final LocalDateTime SHARDING_START_DATE = LocalDateTime.now().withYear(2019).withMonth(1);
    private static final LocalDateTime SHARDING_END_DATE = SHARDING_START_DATE.plusMonths(12);
    private static final int SHARDING_DATE_STEP = 3;

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Map<String, Collection<Comparable<?>>> listValues = shardingValue.getColumnNameAndShardingValuesMap();
        Map<String, Range<Comparable<?>>> rangeValues = shardingValue.getColumnNameAndRangeValuesMap();
        String logicTableName = shardingValue.getLogicTableName();

        Collection<String> routeByCodes = null;
        Collection<Comparable<?>> codes = listValues.get(CODE);
        if (CollectionUtils.isNotEmpty(codes)) {
            Stream<String> codeSet = convert(codes, comparable -> (String) comparable);
            routeByCodes = routeByCodes(logicTableName, codeSet);
        }

        Collection<String> routeByCreateTimeRange = null;
        Range<Comparable<?>> createTimeRange = rangeValues.get(CREATE_TIME);
        if (createTimeRange != null) {
            LocalDateTime start = SHARDING_START_DATE;
            LocalDateTime end = SHARDING_END_DATE;
            if (createTimeRange.hasLowerBound()) {
                start = LocalDateTime.ofInstant(((Date) createTimeRange.lowerEndpoint()).toInstant(),  ZoneId.systemDefault());
            }
            if (createTimeRange.hasUpperBound()) {
                end = LocalDateTime.ofInstant(((Date) createTimeRange.upperEndpoint()).toInstant(), ZoneId.systemDefault());
            }
            routeByCreateTimeRange = routeByCreateTimeRange(logicTableName, start, end);
        }

        Collection<String> routeByRelIds = null;
        Collection<Comparable<?>> relIds = listValues.get(REL_ID);
        if (routeByCodes == null && CollectionUtils.isNotEmpty(relIds)) {
            Stream<Long> relIdSet = convert(relIds, comparable -> (Long)comparable);
            routeByRelIds = routeByRelIds(logicTableName, relIdSet);
        }

        Stream<String> tables = availableTargetNames.stream();
        if (CollectionUtils.isNotEmpty(routeByCodes)) {
            tables = tables.filter(routeByCodes::contains);
        }
        if (CollectionUtils.isNotEmpty(routeByCreateTimeRange)) {
            tables = tables.filter(routeByCreateTimeRange::contains);
        }
        if (CollectionUtils.isNotEmpty(routeByRelIds)) {
            tables = tables.filter(routeByRelIds::contains);
        }
        return tables.collect(Collectors.toSet());
    }

    private Collection<String> routeByCodes(String logicTableName, Stream<String> codes) {
        return codes
                .map(code -> {
                    String dateStr = code.split("-")[0] + " 00:00:00";
                    LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
                    return buildRouteTable(logicTableName, date);
                })
                .collect(Collectors.toSet());
    }

    private String buildRouteTable(String logicTableName, LocalDateTime start) {
        int year = start.getYear();
        int month = start.getMonth().getValue();
        MonthRange monthRange = MonthRange.include(month);
        return logicTableName + "_" +year + "_" + monthRange.start + "_" + monthRange.end;
    }

    private Collection<String> routeByCreateTimeRange(String logicTableName, LocalDateTime start, LocalDateTime end) {
        Collection<String> routeTables = new HashSet<>(8,1);
        for (;start.compareTo(end)<=0; start = start.plusMonths(1)) {
            String routeTable = buildRouteTable(logicTableName, start);
            routeTables.add(routeTable);
        }
        return routeTables;
    }

    private Collection<String> routeByRelIds(String logicTableName, Stream<Long> relIdSet) {
        List<ExampleRel> rels =
                SpringComponentHolder.getExampleRelRepository()
                        .findByRelIdIn(relIdSet.collect(Collectors.toSet()));
        return routeByCodes(logicTableName, rels.stream().map(ExampleRel::getCode));
    }

    private <R> Stream<R> convert(Collection<Comparable<?>> sources, Function<Comparable<?>, R> func) {
        return sources.stream().map(func);
    }


    public static class MonthRange {
        static final int MIN_MONTH = 1;
        static final int MAX_MONTH = 12;
        final int start;
        final int end;

        MonthRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        static MonthRange include(int month) {
            if (month < MIN_MONTH || month > MAX_MONTH) {
                throw new RuntimeException("非法的月份! month -> " + month);
            }

            for (int start = MIN_MONTH; start < MAX_MONTH; start += SHARDING_DATE_STEP) {
                int end = start + SHARDING_DATE_STEP - 1;
                if (start <= month && end >= month) {
                    return new MonthRange(start, end);
                }
            }
            throw new RuntimeException("非法的月份! month -> " + month);
        }
    }
}
