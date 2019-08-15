package test.quxiqi.sharding.sphere.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.AttributeConverter;
import java.util.Objects;

/**
 * @author quxiqi
 * @email quxiqi@zskuaixiao.com
 * @description 抽象一个公共的接口，主要为了拓展IEnum
 *
 * 先说一下背景，为了消灭项目中的常量值，进而将所有常量值转为Enum类在项目里流通。
 * 之前使用IEnum作为所有枚举的父类，从而达到从数据库到工程的枚举类的转换。
 * 然后使用了spring的Convert来进行http请求的参数到枚举类型的转换
 *
 * 但是最近发现问题: Convert转换只能转换@RequestParam 和 @PathVariable 修饰的参数的类型，
 * 对于@RequestBody 修饰的参数里面的枚举类型实际上是不能转换的。因为这一步的转换是通过jackson转的
 *
 * 所以这里要再抽离一层，在尽量不影响原有枚举类的前提下。 我抽了一层 BaseEnum 来做这个事情
 *
 * 新增JPA枚举转换
 * @date 2019/7/25 11:47
 **/
public interface BaseEnum<T> extends AttributeConverter<BaseEnum<T>, T> {
    /**
     * @author quxiqi
     * @date 2019/7/25 14:39
     * 功能描述 通过枚举的val（数据库值）逆向查找枚举对象
     * @param val 枚举值
     * @param eClz 需要查找的枚举类
     * @param <E> 枚举类
     * @param <T> val的类型
     * @return 枚举对象，找不到返回null
     **/
    static <E extends BaseEnum<T>, T> E of(T val, Class<E> eClz) {
        E[] constants = eClz.getEnumConstants();
        for (E aEnum : constants) {
            if (Objects.equals(aEnum.getVal(), val)) {
                return aEnum;
            }
        }
        return null;
    }

    /**
     * @author quxiqi
     * @date 2019/7/25 14:39
     * 功能描述 通过枚举的val（数据库值）逆向查找枚举对象, 主要用于jackson反序列化
     * @param val 枚举值
     * @param eClz 需要查找的枚举类
     * @param <E> 枚举类
     * @return 枚举对象，找不到返回null
     **/
    static <E extends BaseEnum<String>> E of(String val, Class<E> eClz) {
        E[] constants = eClz.getEnumConstants();
        for (E aEnum : constants) {
            if (Objects.equals(String.valueOf(aEnum.getVal()), val)) {
                return aEnum;
            }
        }
        return null;
    }

    @Override
    default T convertToDatabaseColumn(BaseEnum<T> attribute) {
        return attribute.getVal();
    }
    @Override
    default BaseEnum<T> convertToEntityAttribute(T dbData) {
        @SuppressWarnings("unchecked")
        Class<? extends BaseEnum<T>> clz = (Class<? extends BaseEnum<T>>) getClass();
        return BaseEnum.of(dbData, clz);
    }

    /**
     * 功能描述 获取枚举值
     * 主要提供给jackson用来转换枚举对象
     * @author quxiqi
     * @date 2019/7/25 14:40
     * @return 枚举值
     **/
    @JsonValue
    T getVal();
}
