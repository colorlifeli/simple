package me.common.jdbcutil;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录查询条件，生成对应的 sql 语句
 * @author James
 *
 */
public class QueryRule {

	private StringBuffer sb = new StringBuffer("");
	List<Object> params = new ArrayList<Object>();
	private String alias;

	public QueryRule() {
		alias = null;
	}

	/**
	 * 为条件增加表的别名
	 * @param alias
	 */
	public QueryRule(String alias) {
		this.alias = alias;
	}

	public QueryRule andLike(String name, Object value) {

		if (alias != null)
			sb.append(String.format(" and %s.%s like ?", alias, name));
		else
			sb.append(String.format(" and %s like ?", name));

		params.add(value);
		return this;
	}

	public QueryRule andLessThan(String name, Object value) {
		if (alias != null)
			sb.append(String.format(" and %s.%s < ?", alias, name));
		else
			sb.append(String.format(" and %s < ?", name));
		params.add(value);
		return this;
	}

	public QueryRule andGreaterThan(String name, Object value) {
		if (alias != null)
			sb.append(String.format(" and %s.%s > ?", alias, name));
		else
			sb.append(String.format(" and %s > ?", name));
		params.add(value);
		return this;
	}

	public QueryRule andBetween(String name, Object start, Object end) {
		if (alias != null)
			sb.append(String.format(" and %s.%s > ? and %s.%s < ?", alias, name, alias, name));
		else
			sb.append(String.format(" and %s > ? and %s < ?", name, name));
		params.add(start);
		params.add(end);
		return this;
	}

	public QueryRule andEqual(String name, Object value) {
		if (alias != null)
			sb.append(String.format(" and %s.%s = ?", alias, name));
		else
			sb.append(String.format(" and %s = ?", name));
		params.add(value);
		return this;
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	public List<Object> getParams() {
		return params;
	}

}
