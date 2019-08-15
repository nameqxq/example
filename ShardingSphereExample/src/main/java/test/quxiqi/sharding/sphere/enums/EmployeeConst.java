package test.quxiqi.sharding.sphere.enums;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/15
 */
public class EmployeeConst {

    public enum Gender implements BaseEnum<Integer>{
        MAN(0),
        WOMAN(1),
        ;
        public Integer val;

        Gender(Integer val) {
            this.val = val;
        }

        @Override
        public Integer getVal() {
            return val;
        }
    }
}
