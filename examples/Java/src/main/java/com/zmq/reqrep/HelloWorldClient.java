package com.zmq.reqrep;

//
//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back
//

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.zeromq.ZMQ;

public class HelloWorldClient {
	
	public static void main(String[] args) throws UnknownHostException {
		new HelloWorldClient().start();
	}

	public void start() throws UnknownHostException {
		ZMQ.Context context = ZMQ.context(1);

		// Socket to talk to server
		System.out.println(Thread.currentThread().getName() + " Connecting to hello world server...");

		ZMQ.Socket requester = context.socket(ZMQ.REQ);
		String hostName = InetAddress.getLocalHost().getHostName();
		requester.connect("tcp://" + hostName + ":5555");

		for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
			String request = "Hello";
			System.out.println(Thread.currentThread().getName() + " Sending Hello " + requestNbr);
			requester.send(request.getBytes(), 0);

			byte[] reply = requester.recv(0);
			System.out.println(Thread.currentThread().getName() + " Received " + new String(reply) + " " + requestNbr);
		}
		
		requester.close();
		context.term();
	}
}