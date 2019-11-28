package test.quxiqi.sharding.sphere.utils;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 9月.2019/9/19
 */
@Data
public class TableInfo {

    private String name;
    private ColumnInfo primaryKey;
    private ColumnInfo primaryShardingKey;
    private List<ColumnInfo> columnInfos;

    @Data
    @Builder
    public static class ColumnInfo {
        private String name;
        private String propName;
        private String valueName;
        private Field field;
    }
}
