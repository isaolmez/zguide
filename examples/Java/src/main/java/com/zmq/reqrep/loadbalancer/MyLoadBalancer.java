package com.zmq.reqrep.loadbalancer;

import java.util.LinkedList;
import java.util.Queue;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.zmq.ZHelper;

public class MyLoadBalancer {

	public static class Worker implements Runnable {

		@Override
		public void run() {
			ZMQ.Context context = ZMQ.context(1);
			ZMQ.Socket socket = context.socket(ZMQ.REQ);
			ZHelper.setId (socket);
			socket.connect("ipc://backend");
			socket.send("READY");
			System.out.println("Starting to listen!");
			while (!Thread.currentThread().isInterrupted()) {
				String addressOfClient = socket.recvStr();
				socket.recv();
				String requestData = socket.recvStr();

				socket.sendMore(addressOfClient);
				socket.sendMore("");
				socket.send(processRequest(requestData));
			}

			socket.close();
			context.term();
		}

		private String processRequest(String requestData) {
			return "request handled by " + Thread.currentThread().getName();
		}

	}

	public static class Client implements Runnable {

		@Override
		public void run() {
			int counter = 0;
			ZMQ.Context context = ZMQ.context(1);
			Socket socket = context.socket(ZMQ.REQ);
			ZHelper.setId (socket);
			socket.connect("ipc://frontend");
			System.out.println("Starting to listen!");
			while (true) {
				socket.send("Request");
				String reply = socket.recvStr();
				counter++;
//				System.out.println("Reply:" + reply);
				if (counter % 1000 == 0) {
					System.out.println("Request count: " + counter);
				}
			}

		}

	}

	public static void main(String[] args) {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket frontend = context.socket(ZMQ.ROUTER);
		ZMQ.Socket backend = context.socket(ZMQ.ROUTER);

		frontend.bind("ipc://frontend");
		backend.bind("ipc://backend");

		Poller poller = new Poller(2);
		Queue<String> workerQueue = new LinkedList<>();

		for (int i = 0; i < 2; i++) {
			new Thread(new Worker()).start();
		}

		for (int i = 0; i < 2; i++) {
			new Thread(new Client()).start();
		}
		
		while (!Thread.currentThread().isInterrupted()) {
			poller.register(backend, Poller.POLLIN);
			if(workerQueue.size() != 0){
				poller.register(frontend, Poller.POLLIN);	
			}
			
			if(poller.poll() < 0){
				break;
			}
			
			if (poller.pollin(0)) {
//				System.out.println("Polling backend");
				String workerAddress = backend.recvStr();
				workerQueue.add(workerAddress);
				backend.recvStr();// empty delimiter frame
				String replyFirstFrame = backend.recvStr();

				if (!replyFirstFrame.equals("READY")) {
					backend.recvStr(); // empty frame
					String replyData = backend.recvStr();
					frontend.sendMore(replyFirstFrame);
					frontend.sendMore("");
					frontend.send(replyData);
				}
			}
			if (workerQueue.size() != 0) {
				if (poller.pollin(1)) {
//					System.out.println("Polling frontend");
					String clientAddress = frontend.recvStr();
					frontend.recvStr();
					String requestData = frontend.recvStr();

					// Get the oldest worker
					String workerAddress = workerQueue.poll();
					backend.sendMore(workerAddress);
					backend.sendMore("");
					backend.sendMore(clientAddress);
					backend.sendMore("");
					backend.send(requestData);
				}
			}
		}

		backend.close();
		frontend.close();
		context.term();

	}
}
