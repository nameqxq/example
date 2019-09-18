package test.quxiqi.sharding.sphere.config.sharding;

import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import test.quxiqi.sharding.sphere.entity.ExampleRel;
import test.quxiqi.sharding.sphere.utils.SpringComponentHolder;

import java.time.LocalDate;
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
    private static final String DEFAULT_DATE_PATTERN = "YYYYMMDD";

    private static final LocalDate SHARDING_START_DATE = LocalDate.now();
    private static final LocalDate SHARDING_END_DATE = SHARDING_START_DATE.plusMonths(12);
    private static final int SHARDING_DATE_STEP = 3;

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
                                         ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Map<String, Collection<Comparable<?>>> listValues = shardingValue.getColumnNameAndShardingValuesMap();
        Map<String, Range<Comparable<?>>> rangeValues = shardingValue.getColumnNameAndRangeValuesMap();

        Collection<String> routeByCodes = null;
        Collection<Comparable<?>> codes = listValues.get(CODE);
        if (CollectionUtils.isNotEmpty(codes)) {
            Stream<String> codeSet = convert(codes, comparable -> (String) comparable);
            routeByCodes = routeByCodes(codeSet);
        }

        Collection<String> routeByCreateTimeRange = null;
        Range<Comparable<?>> createTimeRange = rangeValues.get(CREATE_TIME);
        if (createTimeRange != null) {
            LocalDate start = SHARDING_START_DATE;
            LocalDate end = SHARDING_END_DATE;
            if (createTimeRange.hasLowerBound()) {
                start = LocalDate.from(((Date) createTimeRange.lowerEndpoint()).toInstant());
            }
            if (createTimeRange.hasUpperBound()) {
                end = LocalDate.from(((Date) createTimeRange.upperEndpoint()).toInstant());
            }
            routeByCreateTimeRange = routeByCreateTimeRange(start, end);
        }

        Collection<String> routeByRelIds = null;
        Collection<Comparable<?>> relIds = listValues.get(REL_ID);
        if (CollectionUtils.isNotEmpty(relIds)) {
            Stream<Long> relIdSet = convert(relIds, comparable -> (Long)comparable);
            routeByRelIds = routeByRelIds(relIdSet);
        }

        Stream<String> tables = availableTargetNames.stream();
        if (routeByCodes != null) {
            tables = tables.filter(routeByCodes::contains);
        }
        if (routeByCreateTimeRange != null) {
            tables = tables.filter(routeByCreateTimeRange::contains);
        }
        if (routeByRelIds != null) {
            tables = tables.filter(routeByRelIds::contains);
        }
        return tables.collect(Collectors.toSet());

    }

    private Collection<String> routeByCodes(Stream<String> codes) {
        return codes
                .map(code -> {
                    String dateStr = code.split("-")[1];
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
                    return buildRouteTable(date);
                })
                .collect(Collectors.toSet());
    }

    private String buildRouteTable(LocalDate start) {
        int year = start.getYear();
        int month = start.getMonth().getValue();
        MonthRange monthRange = MonthRange.include(month);
        return year + "_" + monthRange.start + "_" + monthRange.end;
    }

    private Collection<String> routeByCreateTimeRange(LocalDate start, LocalDate end) {
        Collection<String> routeTables = new HashSet<>(8,1);
        for (;start.compareTo(end)<=0; start = start.plusMonths(1)) {
            String routeTable = buildRouteTable(start);
            routeTables.add(routeTable);
        }
        return routeTables;
    }

    private Collection<String> routeByRelIds(Stream<Long> relIdSet) {
        List<ExampleRel> rels =
                SpringComponentHolder.getExampleRelRepository()
                        .findByRelIdIn(relIdSet.collect(Collectors.toSet()));
        return routeByCodes(rels.stream().map(ExampleRel::getCode));
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
                throw new RuntimeException("非法的月份!");
            }

            for (int start = MIN_MONTH; start <= MAX_MONTH; start += SHARDING_DATE_STEP) {
                int end = start + SHARDING_DATE_STEP - 1;
                if (start >= month && end <= month) {
                    return new MonthRange(start, end);
                }
            }
            throw new RuntimeException("非法的月份!");
        }
    }
}
