package com.xiaode.server;

import java.util.Vector;

import org.apache.commons.lang.SystemUtils;

public class ClientManager extends Vector<MessageThread> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClientManager() {

	}

	public boolean add(MessageThread mt) {
		// printName();
		return super.add(mt);

	}

	public void remove(MessageThread mt) {
		super.remove(mt);
	}

	/* 用于查找被添加的好友 */
	public MessageThread returnThread(String username) {
		// System.out.println("S:"+username);
		MessageThread temp = null;
		System.out.println(size());
		for (int i = 0; i < size(); i++) {
			temp = (MessageThread) get(i);
			// System.out.println("Thread:"+temp.username);
			if (temp.username.equals(username)) {
				break;
			} else {
				temp = null;
			}
		}
		return temp;
	}

	// for test
	public void printName() {
		MessageThread tempMessageThread = null;
		for (int i = 0; i < size(); i++) {
			tempMessageThread = (MessageThread) get(i);
			// System.out.println(tempMessageThread.username);
		}
	}

}
