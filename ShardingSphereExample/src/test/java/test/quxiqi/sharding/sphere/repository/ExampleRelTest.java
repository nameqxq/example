package test.quxiqi.sharding.sphere.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import test.quxiqi.sharding.sphere.Runner;
import test.quxiqi.sharding.sphere.entity.Example;
import test.quxiqi.sharding.sphere.entity.ExampleRel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Runner.class)
public class ExampleRelTest {
    @Autowired
    private ExampleRelRepository exampleRelRepository;
    @Autowired
    private ExampleRepository exampleRepository;

    @Test
    public void init() {
        List<Example> examples = exampleRepository.findAll();
        List<ExampleRel> rels =
                examples.stream()
                        .map(example -> new ExampleRel(null, example.getRelId(), example.getCode()))
                        .collect(Collectors.toList());
        exampleRelRepository.save(rels);

    }


}
