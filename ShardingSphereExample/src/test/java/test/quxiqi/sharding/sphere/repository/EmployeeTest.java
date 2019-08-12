package test.quxiqi.sharding.sphere.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import test.quxiqi.sharding.sphere.Runner;
import test.quxiqi.sharding.sphere.entity.Employee;
import test.quxiqi.sharding.sphere.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Runner.class)
public class EmployeeTest {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExampleRepository exampleRepository;
    @Test
    public void insert() {
        List<Employee> inserts = buildNewEmployee(10);
        employeeRepository.save(inserts);

        List<Example> inserts2 = ExampleTest.buildNewExamples(10);
        exampleRepository.save(inserts2);
    }

    @Test
    public void select() {
        List<Employee> all = employeeRepository.findAll();
        System.out.println(all);
    }

    private List<Employee> buildNewEmployee(int num) {
        Random random = new Random();
        int batch = random.nextInt() % 100000;
        List<Employee> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            list.add(buildNewExample(batch, i));
        }
        return list;
    }

    private Employee buildNewExample(int batch, int index) {
        Employee employee = new Employee();
        String namePre = String.format("%07d", batch);
        employee.setName(namePre + "-" + index);
        employee.setSalary(new BigDecimal(1000));
        return employee;
    }
}
