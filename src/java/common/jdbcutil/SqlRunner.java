package common.jdbcutil;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

public class SqlRunner {

	// ParameterMetaData
	private volatile boolean pmdKnownBroken = false;

	// 可以先给定一个 connection
	protected Connection conn = null;

	public static class holder {
		private static final SqlRunner instance = new SqlRunner();
	}

	private SqlRunner() {
	};

	public static final SqlRunner me() {
		return holder.instance;
	}

	/**
	 * 对某个 sql 语句批量执行不同值 使用预设 connection
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int[] batch(String sql, Object[][] params) throws SQLException {
		return batch(this.conn, sql, params);
	}

	/**
	 * 对某个 sql 语句批量执行不同值
	 * 
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int[] batch(Connection conn, String sql, Object[][] params) throws SQLException {
		if (conn == null) {
			throw new SQLException("Null connection");
		}

		if (sql == null) {
			throw new SQLException("Null SQL statement");
		}

		if (params == null) {
			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		}

		PreparedStatement stmt = null;
		int[] rows = null;
		try {
			stmt = this.prepareStatement(conn, sql);

			for (int i = 0; i < params.length; i++) {
				this.fillStatement(stmt, params[i]);
				stmt.addBatch();
			}
			rows = stmt.executeBatch();

		} catch (SQLException e) {
			this.rethrow(e, sql, (Object[]) params);
		} finally {
			close(stmt);
		}

		return rows;
	}

	/**
	 * 查询，sql语句不需要参数 使用预设connection
	 * 
	 * @param sql
	 * @param rsh
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		return this.<T> query(this.conn, sql, rsh, (Object[]) null);
	}

	/**
	 * 查询，sql语句不需要参数
	 * 
	 * @param conn
	 * @param sql
	 * @param rsh
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		return this.<T> query(conn, sql, rsh, (Object[]) null);
	}

	/**
	 * 查询，sql语句带参数 使用预设connection
	 * 
	 * @param sql
	 * @param rsh
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		return this.<T> query(this.conn, sql, rsh, params);
	}

	/**
	 * 查询，sql语句带参数
	 * 
	 * @param conn
	 * @param sql
	 * @param rsh
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		if (conn == null) {
			throw new SQLException("Null connection");
		}

		if (sql == null) {
			throw new SQLException("Null SQL statement");
		}

		if (rsh == null) {
			throw new SQLException("Null ResultSetHandler");
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		T result = null;

		try {
			stmt = this.prepareStatement(conn, sql);
			this.fillStatement(stmt, params);
			rs = stmt.executeQuery();
			result = rsh.handle(rs);

		} catch (SQLException e) {
			this.rethrow(e, sql, params);

		} finally {
			try {
				close(rs);
			} finally {
				close(stmt);
			}
		}

		return result;
	}

	public int update(String sql) throws SQLException {
		return this.update(this.conn, sql, (Object[]) null);
	}

	/**
	 * 执行jdbc的 executeUpdate，不带参数
	 * 
	 * @param conn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public int update(Connection conn, String sql) throws SQLException {
		return this.update(conn, sql, (Object[]) null);
	}

	/**
	 * 执行jdbc的 executeUpdate，带参数,使用预设connection
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int update(String sql, Object... params) throws SQLException {
		return this.update(this.conn, sql, params);
	}

	/**
	 * 执行jdbc的 executeUpdate，带参数
	 * 
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	private int update(Connection conn, String sql, Object... params) throws SQLException {
		if (conn == null) {
			throw new SQLException("Null connection");
		}

		if (sql == null) {
			throw new SQLException("Null SQL statement");
		}

		PreparedStatement stmt = null;
		int rows = 0;

		try {
			stmt = this.prepareStatement(conn, sql);
			this.fillStatement(stmt, params);
			rows = stmt.executeUpdate();

		} catch (SQLException e) {
			this.rethrow(e, sql, params);

		} finally {
			close(stmt);
		}

		return rows;
	}

	public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
		return insert(this.conn, sql, rsh, (Object[]) null);
	}

	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		return insert(conn, sql, rsh, (Object[]) null);
	}

	public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		return insert(conn, sql, rsh, params);
	}

	private <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		if (conn == null) {
			throw new SQLException("Null connection");
		}

		if (sql == null) {
			throw new SQLException("Null SQL statement");
		}

		if (rsh == null) {
			throw new SQLException("Null ResultSetHandler");
		}

		PreparedStatement stmt = null;
		T generatedKeys = null;

		try {
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			this.fillStatement(stmt, params);
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();
			generatedKeys = rsh.handle(resultSet);
		} catch (SQLException e) {
			this.rethrow(e, sql, params);
		} finally {
			close(stmt);
		}

		return generatedKeys;
	}

	public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) throws SQLException {
		return insertBatch(this.conn, sql, rsh, params);
	}

	private <T> T insertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, Object[][] params)
			throws SQLException {
		if (conn == null) {
			throw new SQLException("Null connection");
		}

		if (sql == null) {
			throw new SQLException("Null SQL statement");
		}

		if (params == null) {
			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		}

		PreparedStatement stmt = null;
		T generatedKeys = null;
		try {
			stmt = this.prepareStatement(conn, sql, Statement.RETURN_GENERATED_KEYS);

			for (int i = 0; i < params.length; i++) {
				this.fillStatement(stmt, params[i]);
				stmt.addBatch();
			}
			stmt.executeBatch();
			ResultSet rs = stmt.getGeneratedKeys();
			generatedKeys = rsh.handle(rs);

		} catch (SQLException e) {
			this.rethrow(e, sql, (Object[]) params);
		} finally {
			close(stmt);
		}

		return generatedKeys;
	}

	public boolean isPmdKnownBroken() {
		return pmdKnownBroken;
	}

	protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {

		return conn.prepareStatement(sql);
	}

	protected PreparedStatement prepareStatement(Connection conn, String sql, int returnedKeys) throws SQLException {

		return conn.prepareStatement(sql, returnedKeys);
	}

	public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {

		// check the parameter count, if we can
		ParameterMetaData pmd = null;
		if (!pmdKnownBroken) {
			pmd = stmt.getParameterMetaData();
			int stmtCount = pmd.getParameterCount();
			int paramsCount = params == null ? 0 : params.length;

			if (stmtCount != paramsCount) {
				throw new SQLException(
						"Wrong number of parameters: expected " + stmtCount + ", was given " + paramsCount);
			}
		}

		// nothing to do here
		if (params == null) {
			return;
		}

		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				stmt.setObject(i + 1, params[i]);
			} else {
				// VARCHAR works with many drivers regardless
				// of the actual column type. Oddly, NULL and
				// OTHER don't work with Oracle's drivers.
				int sqlType = Types.VARCHAR;
				if (!pmdKnownBroken) {
					try {
						/*
						 * It's not possible for pmdKnownBroken to change from
						 * true to false, (once true, always true) so pmd cannot
						 * be null here.
						 */
						sqlType = pmd.getParameterType(i + 1);
					} catch (SQLException e) {
						pmdKnownBroken = true;
					}
				}
				stmt.setNull(i + 1, sqlType);
			}
		}
	}

	public void fillStatementWithBean(PreparedStatement stmt, Object bean, PropertyDescriptor[] properties)
			throws SQLException {
		Object[] params = new Object[properties.length];
		for (int i = 0; i < properties.length; i++) {
			PropertyDescriptor property = properties[i];
			Object value = null;
			Method method = property.getReadMethod();
			if (method == null) {
				throw new RuntimeException(
						"No read method for bean property " + bean.getClass() + " " + property.getName());
			}
			try {
				value = method.invoke(bean, new Object[0]);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Couldn't invoke method: " + method, e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Couldn't invoke method with 0 arguments: " + method, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Couldn't invoke method: " + method, e);
			}
			params[i] = value;
		}
		fillStatement(stmt, params);
	}

	public void fillStatementWithBean(PreparedStatement stmt, Object bean, String... propertyNames)
			throws SQLException {
		PropertyDescriptor[] descriptors;
		try {
			descriptors = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
		} catch (IntrospectionException e) {
			throw new RuntimeException("Couldn't introspect bean " + bean.getClass().toString(), e);
		}
		PropertyDescriptor[] sorted = new PropertyDescriptor[propertyNames.length];
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (propertyName == null) {
				throw new NullPointerException("propertyName can't be null: " + i);
			}
			boolean found = false;
			for (int j = 0; j < descriptors.length; j++) {
				PropertyDescriptor descriptor = descriptors[j];
				if (propertyName.equals(descriptor.getName())) {
					sorted[i] = descriptor;
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("Couldn't find bean property: " + bean.getClass() + " " + propertyName);
			}
		}
		fillStatementWithBean(stmt, bean, sorted);
	}

	protected void rethrow(SQLException cause, String sql, Object... params) throws SQLException {

		String causeMessage = cause.getMessage();
		if (causeMessage == null) {
			causeMessage = "";
		}
		StringBuffer msg = new StringBuffer(causeMessage);

		msg.append(" Query: ");
		msg.append(sql);
		msg.append(" Parameters: ");

		if (params == null) {
			msg.append("[]");
		} else {
			msg.append(Arrays.deepToString(params));
		}

		SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
		e.setNextException(cause);

		throw e;
	}

	protected void close(Statement stmt) throws SQLException {
		if (stmt != null) {
			stmt.close();
		}

	}

	protected void close(ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}

	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}
}
