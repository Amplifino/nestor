package com.amplifino.nestor.jdbc.spy;

import java.util.logging.Level;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.jdbc.pools.DataSourceWrapper;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

@Component
public class DataSourceWrapperImpl implements DataSourceWrapper{

	@Override
	public DataSource wrap(DataSource dataSource) {
		return ProxyDataSourceBuilder
			        .create(dataSource)
			        .logQueryByJUL(Level.INFO, "com.amplifino.nestor.jdbc.spy")
			        .build();
	}

}
