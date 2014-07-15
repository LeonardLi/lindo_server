package com.xiaode.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LindoServer {

	private ServerSocket serviceSocket;
	private int port = 9999;
	private ClientManager clientManager = new ClientManager();

	void startService() {
		// 消息处理服务器

		System.out.println("Service Start");
		try {
			serviceSocket = new ServerSocket(port);
			while (true) {

				Socket socket = serviceSocket.accept();
				System.out.println("new");
				MessageThread newThread = new MessageThread(socket,
						clientManager);
				newThread.start();
				clientManager.add(newThread);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		LindoServer myServer = new LindoServer();
		myServer.startService();

	}
}
