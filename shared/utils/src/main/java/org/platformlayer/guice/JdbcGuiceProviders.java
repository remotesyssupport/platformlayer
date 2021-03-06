package org.platformlayer.guice;

import java.sql.Connection;

import com.google.inject.Provider;

public class JdbcGuiceProviders implements Provider<Connection> {

	@Override
	public Connection get() {
		return JdbcTransactionInterceptor.getConnection();
	}

}
