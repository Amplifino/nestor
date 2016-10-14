package com.amplifino.jetty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.junit.Test;

public class TcpKeepAliveTest {
	
	private ByteBuffer buffer = ByteBuffer.allocate(1024);

	@Test
	public void test() throws IOException, InterruptedException {
		SocketChannel socket = SocketChannel.open();		
		//socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		socket.connect(new InetSocketAddress("52.57.134.11", 8083));
		while (true) {
			printSocket(socket);
			Thread.sleep(50L * 1000L);
		}
	}
	
	private  void printSocket(SocketChannel socket) throws IOException {
		System.out.println(socket);
		System.out.println(socket.getLocalAddress());
		System.out.println(socket.getRemoteAddress());
		System.out.println(socket.getOption(StandardSocketOptions.SO_KEEPALIVE));
		System.out.println(socket.isConnected());
		System.out.println(socket.isOpen());
		System.out.println(socket.isRegistered());
	}
}
