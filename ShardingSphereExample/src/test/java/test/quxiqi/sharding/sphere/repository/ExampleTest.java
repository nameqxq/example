package test.quxiqi.sharding.sphere.repository;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import test.quxiqi.sharding.sphere.Runner;
import test.quxiqi.sharding.sphere.entity.Example;

import java.util.*;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Runner.class)
public class ExampleTest {
    @Autowired
    private ExampleRepository exampleRepository;

    @Test
    public void insert() {
        List<Example> inserts = buildNewExamples(1000);
        exampleRepository.save(inserts);
        // i'm sure i only use mysql, when i exclusions other database pom dependencies and start project:
    }

    @Test
    public void save() {
        // fixme update需要带上code参数
        List<Example> byCode = exampleRepository.findByCode("20190317-16475");
        for (Example example : byCode) {
            example.setRelId(75164L);
            exampleRepository.save(example);
        }

        Example one = exampleRepository.findOne(381113856803471361L);
        one.setRelId(69120L);
        exampleRepository.save(one);
    }

    @Test
    public void select() {
        // List<Example> all = exampleRepository.findAll();
        // System.out.println(all);

        List<Example> byRelIds = exampleRepository.findByRelId(88162L);
        System.out.println(byRelIds);

        List<Example> or = exampleRepository.findByRelIdOrCode(88162L, "20190317-16475");
        System.out.println(or);

        Date end = new Date();
        List<Example> between = exampleRepository.findByCreateTimeBetween(DateUtils.addMonths(end, -2), end);
        System.out.println(between);

        List<Example> byCode = exampleRepository.findByCode("20190317-16475");
        System.out.println(byCode);
    }

    public static List<Example> buildNewExamples(int num) {
        List<Example> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            list.add(buildNewExample(i));
        }
        return list;
    }

    private static Example buildNewExample(int index) {
        Random random = new Random();
        int batch = random.nextInt(365);
        Date date = DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.YEAR), batch);
        String namePre = FastDateFormat.getInstance("yyyyMMdd").format(date);

        Example example = new Example();
        example.setName(namePre + "-" + index + batch);
        example.setCode(namePre + "-" + index + batch);
        example.setRelId((long)(batch  * 1000 + index));
        example.setCreateTime(date);
        return example;
    }
}
