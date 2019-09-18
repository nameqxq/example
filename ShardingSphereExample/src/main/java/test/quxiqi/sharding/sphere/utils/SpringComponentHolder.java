package test.quxiqi.sharding.sphere.utils;

import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import test.quxiqi.sharding.sphere.entity.Example;
import test.quxiqi.sharding.sphere.repository.ExampleRelRepository;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.function.Consumer;

/**
 * 常用spring组件持有者，工具类中的组件请使用此类统一获取
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/12
 */
@Component
public class SpringComponentHolder implements ApplicationContextAware {
    @Getter
    private static ApplicationContext alc;
    @Getter
    private static StringRedisTemplate stringRedisTemplate;
    @Getter
    private static ExampleRelRepository exampleRelRepository;
    @Getter
    private static EntityManager entityManager;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        alc = applicationContext;
        load();
    }

    private void load() {
        for (Component component : Component.values()) {
            component.load(alc);
        }
    }

    private enum Component {
        STRING_REDIS_TEMPLATE(StringRedisTemplate.class, it -> SpringComponentHolder.stringRedisTemplate = (StringRedisTemplate)it),
        EXAMPLE_REL_REPOSITORY(ExampleRelRepository.class, it -> SpringComponentHolder.exampleRelRepository = (ExampleRelRepository)it),
        ENTITY_MANAGER(EntityManager.class, it -> SpringComponentHolder.entityManager = (EntityManager)it),
        ;
        private final Class<?> clz;
        private final Consumer<Object> consumer;

        Component(Class<?> clz, Consumer<Object> consumer) {
            this.clz = clz;
            this.consumer = consumer;
        }
        public void load(ApplicationContext alc) {
            Object bean = alc.getBean(clz);
            if (bean == null) {
                throw new IllegalStateException("找不到对应的Spring组件 --> " + clz.getSimpleName());
            }
            consumer.accept(bean);
        }
    }
}
