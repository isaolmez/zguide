package com.zmq.reqrep.router;
//
//  Hello World server in Java
//  Binds REP socket to tcp://*:5555
//  Expects "Hello" from client, replies with "World"
//

import org.zeromq.ZMQ;

public class HWServer {
	private ZMQ.Context context;
	private ZMQ.Socket responderSocket;
	
	public void start(int port){
		context = ZMQ.context(1);

        //  Socket to talk to clients
        responderSocket = context.socket(ZMQ.REP);
        responderSocket.bind("tcp://*:"+port);
	}
	
	public void listen() throws InterruptedException{
		while (!Thread.currentThread().isInterrupted()) {
            // Wait for next request from the client
            byte[] request = responderSocket.recv(0);
            System.out.println(Thread.currentThread().getName() + " Received Hello");

            // Do some 'work'
            Thread.sleep(1000);

            // Send reply back to client
            String reply = "World";
            responderSocket.send(reply.getBytes(), 0);
        }
	}
	
	public void close(){
        responderSocket.close();
        context.term();
	}
	
	public static void main(String[] args) throws Exception {
		HWServer hwServer = new HWServer();
		hwServer.start(5555);
		hwServer.listen();
		hwServer.close();
    }
}
