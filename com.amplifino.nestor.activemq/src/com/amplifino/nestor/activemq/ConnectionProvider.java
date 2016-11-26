package com.amplifino.nestor.activemq;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class ConnectionProvider {
	
	private BundleContext bundleContext;
	private ActiveMQConnectionFactory factory;
	private ActiveMQConnection connection;
	private ServiceRegistration<Connection> registration;
	
	@Activate
	public void activate(ConnectionProviderConfiguration configuration, BundleContext context) throws JMSException {
		this.bundleContext = context;
		factory = new ActiveMQConnectionFactory(configuration.brokerUrl());
		connection = (ActiveMQConnection) factory.createConnection();
		connection.start();
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("application", configuration.application());
		registration = bundleContext.registerService(Connection.class, connection, properties);		
	}
	
	@Deactivate
	public void deactivate() throws JMSException {
		registration.unregister();
		try {
			connection.stop();
		} finally {
			connection.close();
		}
	}

}
