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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
        List<Example> inserts = buildNewExamples(20);
        exampleRepository.save(inserts);
        // i'm sure i only use mysql, when i exclusions other database pom dependencies and start project:
    }

    @Test
    public void save() {
        Example example = new Example();
        example.setId(2L);
        // example.setName("test");
        exampleRepository.save(example);
    }

    @Test
    public void select() {
        // List<Example> all = exampleRepository.findAll();
        // System.out.println(all);

        List<Example> byRelIds = exampleRepository.findByRelId(840242073L);
        System.out.println(byRelIds);
        List<Example> or = exampleRepository.findByRelIdOrCode(840242073L, "0840242-71");
        System.out.println(or);
        List<Example> between = exampleRepository.findByIdBetween(2L, 10L);
        System.out.println(between);
    }

    public static List<Example> buildNewExamples(int num) {
        Random random = new Random();
        int batch = random.nextInt(365);
        List<Example> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            list.add(buildNewExample(batch, i));
        }
        return list;
    }

    private static Example buildNewExample(int batch, int index) {
        Example example = new Example();
        Date date = DateUtils.addDays(new Date(), batch);
        String namePre = FastDateFormat.getInstance("YYYYMMDD").format(date);
        example.setName(namePre + "-" + index);
        example.setCode(namePre + "-" + index);
        example.setRelId((long)(batch  * 1000 + index));
        example.setCreateTime(date);
        return example;
    }
}
