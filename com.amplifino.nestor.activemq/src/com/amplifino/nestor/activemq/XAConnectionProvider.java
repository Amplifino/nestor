package com.amplifino.nestor.activemq;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.XAConnection;

import org.apache.activemq.ActiveMQXAConnection;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class XAConnectionProvider {
	
	private BundleContext bundleContext;
	private ActiveMQXAConnectionFactory factory;
	private ActiveMQXAConnection connection;
	private ServiceRegistration<XAConnection> registration;
	
	@Activate
	public void activate(ConnectionProviderConfiguration configuration, BundleContext context) throws JMSException {
		this.bundleContext = context;
		factory = new ActiveMQXAConnectionFactory(configuration.brokerUrl());
		connection = (ActiveMQXAConnection) factory.createXAConnection();
		connection.start();
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("application", configuration.application());
		registration = bundleContext.registerService(XAConnection.class, connection, properties);		
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
