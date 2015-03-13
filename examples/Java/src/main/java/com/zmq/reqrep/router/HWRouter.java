package com.zmq.reqrep.router;

//
//  Hello World client in Java
//  Connects REQ socket to tcp://localhost:5555
//  Sends "Hello" to server, expects "World" back
//

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.zeromq.ZMQ;

public class HWRouter {

	private ZMQ.Context context;
	private ZMQ.Context context2;
	private ZMQ.Socket routerSocket;
	private ZMQ.Socket dealerSocket;

	public void start(int frontendPort, int... backendPorts) throws UnknownHostException {
		context = ZMQ.context(1);
		context2 = ZMQ.context(1);
		// Socket to talk to server
		System.out.println(Thread.currentThread().getName() + " Connecting to hello world server...");

		routerSocket = context.socket(ZMQ.ROUTER);
		dealerSocket = context2.socket(ZMQ.DEALER);
		String hostName = InetAddress.getLocalHost().getHostName();
		routerSocket.bind("tcp://*:" + frontendPort);	
		for (int backendPort : backendPorts) {
			dealerSocket.connect("tcp://" + hostName + ":" + backendPort);	
		}
	}

	public void listen() {
		ZMQ.proxy(routerSocket, dealerSocket, null);
	}

	public void close() {
		routerSocket.close();
		dealerSocket.close();
		context.term();
		context2.term();
	}

	public static void main(String[] args) throws UnknownHostException {
		HWRouter hwRouter = new HWRouter();
		hwRouter.start(5550, 5555);
		hwRouter.listen();
		hwRouter.close();
	}
}