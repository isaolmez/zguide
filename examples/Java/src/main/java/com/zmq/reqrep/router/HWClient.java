package com.zmq.reqrep.router;

//
//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back
//

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.zeromq.ZMQ;

public class HWClient {
	
	private ZMQ.Context context;
	private ZMQ.Socket requesterSocket;

	public void start(int... ports) throws UnknownHostException {
		context = ZMQ.context(1);

		// Socket to talk to server
		System.out.println(Thread.currentThread().getName() + " Connecting to hello world server...");

		requesterSocket = context.socket(ZMQ.REQ);
		String hostName = InetAddress.getLocalHost().getHostName();
		for (int port : ports) {
			requesterSocket.connect("tcp://" + hostName + ":" + port);	
		}
	}

	public void listen() {
		for (int requestNbr = 0; requestNbr != 10000; requestNbr++) {
			String request = "Hello";
//			System.out.println(Thread.currentThread().getName() + " Sending Hello " + requestNbr);
			requesterSocket.send(request.getBytes(), 0);

			byte[] reply = requesterSocket.recv(0);
			System.out.println(Thread.currentThread().getName() + " Received " + new String(reply) + " " + requestNbr);
		}
	}
	
	public void close(){
		requesterSocket.close();
		context.term();		
	}
	
	public static void main(String[] args) throws UnknownHostException {
		HWClient hwClient = new HWClient();
		hwClient.start(5550);
		hwClient.listen();
		hwClient.close();
	}
}