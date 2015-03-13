package com.zmq.reqrep.multicon;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MultiConTest {
	public static class MyThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					System.exit(1);
					System.out.println("Error occured:" + e.getMessage());
				}
			});
			return t;
		}

	}

	public static class ClientThread implements Runnable {
		private int[] ports;
		private CountDownLatch latch;

		public ClientThread(CountDownLatch latch, int... ports) {
			this.latch = latch;
			this.ports = ports;
		}

		@Override
		public void run() {
			try {
				HWClient client = new HWClient();
				// wait for server then start
				latch.await();
				client.start(ports);
				// listen
				client.listen();
				// close
				client.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static class ServerThread implements Runnable {
		private int port;
		private CountDownLatch latch;

		public ServerThread(CountDownLatch latch, int port) {
			this.latch = latch;
			this.port = port;
		}

		@Override
		public void run() {
			HWServer server = new HWServer();
			// start
			server.start(port);
			latch.countDown();
			// listen
			try {
				server.listen();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// close
			server.close();

		}
	}

	public static void main(String[] args) {
		/**
		 * When a client connects to multiple servers, it sends the requests to multiple servers in a round robin fashion
		 * 
		 */
		ExecutorService threadPool = Executors.newFixedThreadPool(5, new MyThreadFactory());
		CountDownLatch latch = new CountDownLatch(2);
		threadPool.submit(new ServerThread(latch, 5555));
		threadPool.submit(new ServerThread(latch, 5556));
		threadPool.submit(new ClientThread(latch, 5555, 5556));
	}
}
