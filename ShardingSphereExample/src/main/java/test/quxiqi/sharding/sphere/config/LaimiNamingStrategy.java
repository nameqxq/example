package test.quxiqi.sharding.sphere.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * 表命名策略变更
 * <p>表
 * PriceSetGoods => t_price_set_goods
 * Created by wangpeng on 2016-10-14.
 */
public class LaimiNamingStrategy extends PhysicalNamingStrategyStandardImpl {

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return new Identifier(addUnderscores(name.getText()), name.isQuoted());
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		return new Identifier(this.addUnderscores(name.getText()), name.isQuoted());
	}

	private String addUnderscores(String name) {
		final StringBuilder buf = new StringBuilder(name.replace('.', '_'));
		for (int i = 1; i < buf.length() - 1; i++) {
			if (
					Character.isLowerCase(buf.charAt(i - 1)) &&
							Character.isUpperCase(buf.charAt(i)) &&
							Character.isLowerCase(buf.charAt(i + 1))
					) {
				buf.insert(i++, '_');
			}
		}
		return buf.toString().toLowerCase(Locale.ROOT);
	}

}
