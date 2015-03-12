package com.zmq.pubsub;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PubSubTest {

	public static class ClientThread implements Runnable {
		public ClientThread() {
		}

		@Override
		public void run() {
			try {
				new WeatherUpdateClient().start(new String[] {});
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	public static class ServerThread implements Runnable {
		public ServerThread() {
		}

		@Override
		public void run() {
			new WeatherUpdateServer().start();
		}
	}

	public static void main(String[] args) {
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		threadPool.submit(new ServerThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
		threadPool.submit(new ClientThread());
	}
}
