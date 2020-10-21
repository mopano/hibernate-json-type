/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class JsonSqlPGObjectHandler implements SqlTypeDescriptor {

	public static final JsonSqlPGObjectHandler INSTANCE = new JsonSqlPGObjectHandler();
	private static final long serialVersionUID = -7366336604892811986L;

	@Override
	public int getSqlType() {
		return Types.OTHER;
	}

	@Override
	public boolean canBeRemapped() {
		return false;
	}

	@Override
	public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new BasicBinder<X>(javaTypeDescriptor, this) {

			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				final String printed = javaTypeDescriptor.unwrap(value, String.class, options);
				st.setObject(index, printed, Types.OTHER);
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				final String printed = javaTypeDescriptor.unwrap(value, String.class, options);
				st.setObject(name, printed, Types.OTHER);
			}
		};
	}

	@Override
	public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
		return new BasicExtractor<X>(javaTypeDescriptor, this) {
			@Override
			protected X doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap(extractFromObject(rs.getObject(name)), options);
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap(extractFromObject(statement.getObject(index)), options);
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaTypeDescriptor.wrap(extractFromObject(statement.getObject(name)), options);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private String extractFromObject(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof String) {
			return (String) o;
		}

		// Use reflection because we get classloader conflicts in Tomcat 8.5 if we use direct references
		// If driver is loaded from webapp, exception is in PG libraries when binding.
		// If driver is loaded from tomcat, exception is here when extracting.
		Class c = o.getClass();
		try {
			if (!"org.postgresql.util.PGobject".equals(c.getName())) {
				throw new RuntimeException("Don't know how to convert result from database. Incoming type: " + c.getName());
			}
			Method m = c.getMethod("getValue");
			return (String) m.invoke(o);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
