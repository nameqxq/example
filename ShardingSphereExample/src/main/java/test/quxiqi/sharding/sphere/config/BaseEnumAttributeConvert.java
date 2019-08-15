package test.quxiqi.sharding.sphere.config;

import test.quxiqi.sharding.sphere.enums.BaseEnum;

import javax.persistence.AttributeConverter;
import java.lang.reflect.ParameterizedType;

/**
 * @author <a href="mailto:quxiqi@zskuaixiao.com"> quxiqi </a>
 * @version 1.0 2019 八月.2019/8/15
 */
public abstract class BaseEnumAttributeConvert<E extends BaseEnum<T>, T> implements AttributeConverter<BaseEnum<T>, T> {
    private Class<E> clz;

    @SuppressWarnings("unchecked")
    public BaseEnumAttributeConvert() {
        clz = (Class<E>) (((ParameterizedType)
                this.getClass().getGenericSuperclass()).getActualTypeArguments())[0];
    }

    @Override
    public T convertToDatabaseColumn(BaseEnum<T> attribute) {
        return attribute.getVal();
    }
    @Override
    public BaseEnum<T> convertToEntityAttribute(T dbData) {
        return BaseEnum.of(dbData, clz);
    }
}
