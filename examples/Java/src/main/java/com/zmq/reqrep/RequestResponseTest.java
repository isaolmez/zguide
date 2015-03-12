package com.zmq.reqrep;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestResponseTest {

	public static class ClientThread implements Runnable{

		@Override
		public void run() {
			try {
				new HelloWorldClient().start();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static class ServerThread implements Runnable{

		@Override
		public void run() {
			try {
				new HelloWorldServer().start();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		threadPool.submit(new ServerThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
	}
}
