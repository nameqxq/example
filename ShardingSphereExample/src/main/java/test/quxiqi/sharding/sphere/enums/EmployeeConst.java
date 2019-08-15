package test.quxiqi.sharding.sphere.enums;


import test.quxiqi.sharding.sphere.config.BaseEnumAttributeConvert;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/15
 */
public class EmployeeConst {

    public enum Gender implements BaseEnum<Integer>{
        MAN(1),
        WOMAN(0),
        ;
        public Integer val;

        Gender(Integer val) {
            this.val = val;
        }

        @Override
        public Integer getVal() {
            return val;
        }

        public static class Convert extends BaseEnumAttributeConvert<Gender, Integer> {
        }
    }
}
