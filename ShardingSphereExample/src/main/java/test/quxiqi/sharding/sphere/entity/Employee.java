package test.quxiqi.sharding.sphere.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import test.quxiqi.sharding.sphere.enums.EmployeeConst;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
@Entity
@Table(name = "employee")
@DynamicUpdate
@DynamicInsert
@Data
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "gender")
    private EmployeeConst.Gender gender;
}
