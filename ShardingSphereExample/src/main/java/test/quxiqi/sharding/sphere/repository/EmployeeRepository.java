package test.quxiqi.sharding.sphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.quxiqi.sharding.sphere.entity.Employee;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
