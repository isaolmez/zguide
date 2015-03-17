package com.zmq.reqrep.router;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class RouterTest {
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
	
	public static class RouterThread implements Runnable {
		private CountDownLatch latch;
		private int frontendPort;
		private int[] backendPorts;

		public RouterThread(CountDownLatch latch, int frontendPorts, int... backendPorts) {
			this.latch = latch;
			this.frontendPort = frontendPorts;
			this.backendPorts = backendPorts;
		}

		@Override
		public void run() {
			HWCustomRouter router = new HWCustomRouter();
			// start
			try {
				router.start(frontendPort, backendPorts);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			latch.countDown();
			// listen
			router.listen();
			// close
			router.close();

		}
	}

	public static void main(String[] args) {
		// Multi client one server
		// server responds the clients in a round robin fashion or fair queueing
//		ExecutorService threadPool = Executors.newFixedThreadPool(5, new MyThreadFactory());
//		CountDownLatch latch = new CountDownLatch(2);
//		threadPool.submit(new RouterThread(latch, 5550, 5555));
//		threadPool.submit(new ServerThread(latch, 5555));
//		threadPool.submit(new ClientThread(latch, 5550));
//		threadPool.submit(new ClientThread(latch, 5550));
//		threadPool.submit(new ClientThread(latch, 5550));
		
		// One client multi servers
		// Client request are mapped to servers in a round robin fashion
		ExecutorService threadPool = Executors.newFixedThreadPool(5, new MyThreadFactory());
		CountDownLatch latch = new CountDownLatch(4);
		threadPool.submit(new RouterThread(latch, 5550, 5555, 5556, 5557));
		threadPool.submit(new ServerThread(latch, 5555));
		threadPool.submit(new ServerThread(latch, 5556));
		threadPool.submit(new ServerThread(latch, 5557));
		threadPool.submit(new ClientThread(latch, 5550));
		
		// Multi clients multi servers
//		ExecutorService threadPool = Executors.newFixedThreadPool(10, new MyThreadFactory());
//		CountDownLatch latch = new CountDownLatch(4);
//		threadPool.submit(new RouterThread(latch, 5550, 5555, 5556, 5557));
//		threadPool.submit(new ServerThread(latch, 5555));
//		threadPool.submit(new ServerThread(latch, 5556));
//		threadPool.submit(new ServerThread(latch, 5557));
//		threadPool.submit(new ClientThread(latch, 5550));
//		threadPool.submit(new ClientThread(latch, 5550));
//		threadPool.submit(new ClientThread(latch, 5550));
	}
}
