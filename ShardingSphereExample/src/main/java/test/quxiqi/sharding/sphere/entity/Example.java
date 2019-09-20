package test.quxiqi.sharding.sphere.entity;

import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import test.quxiqi.sharding.sphere.config.sharding.ShardingEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/9
 */
@Entity
@Table(name = "example")
@DynamicUpdate
@DynamicInsert
@Getter
public class Example implements Serializable, ShardingEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "rel_id")
    private Long relId;

    @Column(name = "name")
    private String name;

    @Column(name = "create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setId(Long id) {
        set("id", id, aLong -> this.id= aLong, this::getId);
    }

    public void setCode(String code) {
        set("code", code, str -> this.code = str, this::getCode);
    }

    public void setRelId(Long relId) {
        set("relId", relId, aLong -> this.relId= aLong, this::getRelId);
    }

    public void setName(String name) {
        set("name", name, str -> this.name = str, this::getName);
    }

    public void setCreateTime(Date createTime) {
        set("createTime", createTime, date -> this.createTime = date, this::getCreateTime);
    }

    @Override
    public String primaryShardingKey() {
        return "code";
    }

    @Transient
    private Set<String> hasUpdate = new HashSet<>();

    @Override
    public boolean hasUpdate(String propName) {
        return hasUpdate.contains(propName);
    }

    @Override
    public void clear() {
        hasUpdate.clear();
    }

    private <T> void set(String propName, T t, Consumer<T> consumer, Supplier<T> supplier) {
        if (Objects.equals(supplier.get(), t)) {
            return;
        }
        consumer.accept(t);
        hasUpdate.add(propName);
    }
}
