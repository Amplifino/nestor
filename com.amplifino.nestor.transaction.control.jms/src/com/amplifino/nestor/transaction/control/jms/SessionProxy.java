package com.amplifino.nestor.transaction.control.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

abstract class SessionProxy implements Session {
	
	abstract Session session() throws JMSException;

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() throws JMSException {
		session().commit();

	}

	@Override
	public QueueBrowser createBrowser(Queue arg0) throws JMSException {
		return session().createBrowser(arg0);
	}

	@Override
	public QueueBrowser createBrowser(Queue arg0, String arg1) throws JMSException {
		return session().createBrowser(arg0, arg1);
	}

	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		return session().createBytesMessage();
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0) throws JMSException {
		return session().createConsumer(arg0);
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0, String arg1) throws JMSException {
		return session().createConsumer(arg0, arg1);
	}

	@Override
	public MessageConsumer createConsumer(Destination arg0, String arg1, boolean arg2) throws JMSException {
		return session().createConsumer(arg0, arg1, arg2);
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1) throws JMSException {
		return session().createDurableSubscriber(arg0, arg1);
	}

	@Override
	public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1, String arg2, boolean arg3) throws JMSException {
		return session().createDurableSubscriber(arg0, arg1, arg2, arg3);
	}

	@Override
	public MapMessage createMapMessage() throws JMSException {
		return session().createMapMessage();
	}

	@Override
	public Message createMessage() throws JMSException {
		return session().createMessage();
	}

	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		return session().createObjectMessage();
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable arg0) throws JMSException {
		return session().createObjectMessage(arg0);
	}

	@Override
	public MessageProducer createProducer(Destination arg0) throws JMSException {
		return session().createProducer(arg0);
	}

	@Override
	public Queue createQueue(String arg0) throws JMSException {
		return session().createQueue(arg0);
	}

	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		return session().createStreamMessage();
	}

	@Override
	public TemporaryQueue createTemporaryQueue() throws JMSException {
		return session().createTemporaryQueue();
	}

	@Override
	public TemporaryTopic createTemporaryTopic() throws JMSException {
		return session().createTemporaryTopic();
	}

	@Override
	public TextMessage createTextMessage() throws JMSException {
		return session().createTextMessage();
	}

	@Override
	public TextMessage createTextMessage(String arg0) throws JMSException {
		return session().createTextMessage(arg0);
	}

	@Override
	public Topic createTopic(String arg0) throws JMSException {
		return session().createTopic(arg0);
	}

	@Override
	public int getAcknowledgeMode() throws JMSException {
		return session().getAcknowledgeMode();
	}

	@Override
	public MessageListener getMessageListener() throws JMSException {
		return session().getMessageListener();
	}

	@Override
	public boolean getTransacted() throws JMSException {
		return session().getTransacted();
	}

	@Override
	public void recover() throws JMSException {
		session().recover();

	}

	@Override
	public void rollback() throws JMSException {
		session().rollback();
	}

	@Override
	public void run() {
		try {
			session().run();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setMessageListener(MessageListener arg0) throws JMSException {
		session().setMessageListener(arg0);

	}

	@Override
	public void unsubscribe(String arg0) throws JMSException {
		session().unsubscribe(arg0);

	}

}
