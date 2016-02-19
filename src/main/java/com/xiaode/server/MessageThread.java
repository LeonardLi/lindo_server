package com.xiaode.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import com.xiaode.data.DatabaseConnector;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageThread extends Thread {
	/**
	 * Message Map
	 * */
	final static int C_LOGIN = 1;// login message,include
									// username,password,return frendList
	final static int C_REGISTER = 2;// register message,include
									// username,nickname,password,return no
									// message
	final static int C_QUIT = 3;// quit message,include username,
	final static int C_CHANGE_INFO = 4;// change message,
	final static int C_GET_FREINDLIST = 5;// get friend list data from server
	final static int C_GET_NEW_FRIENFINFO = 6;//
	final static int C_ADD_FRIEND = 7;// Add new friend
	final static int C_DELETE_FRIEND = 8;// delete a friend
	final static int C_REQUEST_CHAT = 9;
	final static int C_APPLY_TO_ADD = 10;

	final static int S_LOGIN_SUCCESS = 9;// server response login success
	final static int S_LOGIN_FAIL = 10;// server response login fail
	final static int S_REGISTER_SUCCESS = 11;// server response register success
	final static int S_REGISTER_FAIL = 12;// server response register
											// fail,username has already exsited
	final static int S_FRIEND_LOGIN = 17;//friend login,fresh list
	final static int S_FRIEND_LOGOUT = 18;//friend logout,fresh list

	final static int S_ADD_FRIEND_SUCCESS = 13;// server response add friend
												// success
	final static int S_ADD_FRIEND_FAIL = 14;// server response add friend fail
	final static int S_ADD_EXISTED_FRIEND = 15;// server response add existed
												// friend
	final static int S_REQUEST_CHAT = 16;//somebody wants chat with you
	final static int S_RETURN_FRIEND_INFO = 19;//server return someone's info
	final static int S_APPLY_TO_ADD = 20;//someone wants to be your friend
	final static int S_DELETE_FRIEND = 21;//delete friend success,refresh your list

	final static int D_FRIEND_ADD_POSSIBLE = 0;
	final static int D_FRIEND_ADD_IMPOSSIBLE = 1;

	public Socket socket;
	public BufferedReader bf;
	public PrintWriter pw;
	private String msg = null;
	private String message = null;
	private JSONObject json = null;
	private int flag = -1;
	private DatabaseConnector connector;
	String username = null;
	private ClientManager clientManager;

	public MessageThread(Socket soc, ClientManager clientManager) {
		this.socket = soc;
		this.clientManager = clientManager;
	}

	@Override
	public void run() {
		// 主server监听
		try {
			bf = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(), true);

			while ((msg = bf.readLine()) != null) {

				message = msg;
				// System.out.println("Message is" + message);
				if (message != null) {

					// 转成JSON,解析flag，确定消息类型
					flag = getMessagaeFlag(message);
					message = null;
				}

				if (flag != -1) {
					handleMessage(flag);
				} else {
					System.out.println("get flag error");
				}
			}

		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				// clientManager.remove(this);//应当在QUIT信号中执行此方法
				if (bf != null)
					bf.close();
				if (pw != null)
					pw.close();
				if (socket != null)
					socket.close();

			} catch (Exception e) {

			}
		}

	}

	public int getMessagaeFlag(String msg) {
		int flag = -1;
		if (msg != null) {
			try {
				json = new JSONObject(msg);
				System.out.println("RCV:" + json);
				String tempString = json.getString("Flags");
				flag = Integer.parseInt(tempString);

				// System.out.println("flag is :" + flag);
			} catch (JSONException e) {
				e.printStackTrace();

			}

		}
		return flag;
	}

	public void handleMessage(int flag) {

		// 消息分发处理
		switch (flag) {

		case C_LOGIN:
			connector = new DatabaseConnector();
			username = json.getString("username");
			String passwd_1 = json.getString("password");
			String ip = socket.getInetAddress().toString();
			ip = ip.substring(1, ip.length());// remove '/'
			if (connector.login(username, passwd_1, ip)) {
				// System.out.println("new login success");
				StringBuffer buffer = new StringBuffer(creatFlags(
						S_LOGIN_SUCCESS).toString());
				pw.println(buffer);
				pw.flush();
				// 群发上线通知
//				Vector<String> friendList = connector
//						.returnFriendName(username);
//
//				for (int i = 0; i < friendList.size(); i++) {
//					for (int j = 0; j < clientManager.size(); j++) {
//						MessageThread tempThread = clientManager
//								.returnThread((String) friendList.elementAt(i));
//						if (tempThread != null) {
//							StringBuffer buffer1 = new StringBuffer(creatFlags(
//									S_FRIEND_LOGIN).toString());
//							// System.out.println(buffer1);
//							tempThread.pw.println(buffer1);
//							tempThread.pw.flush();
//							break;
//						}
//
//					}
//				}

			} else {
				System.out.println("new login failed");
				connector.closeConnection();
				StringBuffer returnMessageArraryBuffer = new StringBuffer(
						creatFlags(S_LOGIN_FAIL).toString());
				System.out.println(returnMessageArraryBuffer);
				pw.println(returnMessageArraryBuffer);
				pw.flush();
			}

			break;

		case C_GET_FREINDLIST:
			if (username != null) {
				connector = new DatabaseConnector();
				StringBuffer returnMessageArray = new StringBuffer(connector
						.return_friends(username).toString());
				System.out.println(returnMessageArray);
				connector.closeConnection();

				pw.println(returnMessageArray);
				pw.flush();
			}

			break;

		case C_REGISTER:
			connector = new DatabaseConnector();
			String id_2 = json.getString("username");
			String passwd_2 = json.getString("password");
			String ip_2 = socket.getInetAddress().toString();
			ip_2 = ip_2.substring(1, ip_2.length());
			System.out.println(ip_2);
			String nickname = json.getString("nickname");
			String date = json.getString("date");
			int age = Integer.parseInt(json.getString("age"));
			String gender = json.getString("gender");
			String comment = json.getString("comment");
			int avatar = Integer.parseInt(json.getString("avatar"));

			if (connector.register(id_2, passwd_2, ip_2, nickname, date, age,
					gender, comment, avatar)) {
				// System.out.println("resgister Success");
				StringBuffer buffer = new StringBuffer(creatFlags(
						S_REGISTER_SUCCESS).toString());
				pw.println(buffer);
				pw.flush();
			} else {
				System.out.println("resgister failed");
				StringBuffer buffer = new StringBuffer(creatFlags(
						S_REGISTER_FAIL).toString());
				pw.println(buffer);
				pw.flush();
			}

			break;
		case C_QUIT:
			connector = new DatabaseConnector();
			// 群发下线通知
			Vector<String> friendList = connector.returnFriendName(username);
			// System.out.println(friendList);
			for (int i = 0; i < friendList.size(); i++) {
				// System.out.println("xixi");
				for (int j = 0; j < clientManager.size(); j++) {
					MessageThread tempThread = clientManager
							.returnThread((String) friendList.elementAt(i));
					if (tempThread != null) {
						StringBuffer buffer1 = new StringBuffer(creatFlags(
								S_FRIEND_LOGOUT).toString());
						// System.out.println(buffer1);
						tempThread.pw.println(buffer1);
						tempThread.pw.flush();
						break;
					} else {
						// System.out.println("haha");
					}

				}
			}
			connector.logout(json.getString("username"));
			connector.closeConnection();
			clientManager.remove(this);
			break;
		case C_GET_NEW_FRIENFINFO:
			connector = new DatabaseConnector();
			int out = connector.verify_add_friend(json.getString("friendId"),
					json.getString("username"));
			if (D_FRIEND_ADD_POSSIBLE == out) {
				JSONObject friendInfo = connector.return_info(json
						.getString("friendId"));
				friendInfo.accumulate("Flags", S_RETURN_FRIEND_INFO);
				StringBuffer buffer = new StringBuffer(friendInfo.toString());
				System.out.println("send:" + buffer);

				pw.println(buffer);
				pw.flush();
			} else if (D_FRIEND_ADD_IMPOSSIBLE == out) {
				StringBuffer buffer = new StringBuffer(creatFlags(
						S_ADD_EXISTED_FRIEND).toString());
				System.out.println(buffer);

				pw.println(buffer);
				pw.flush();

			} else {
				StringBuffer buffer = new StringBuffer(creatFlags(
						S_ADD_FRIEND_FAIL).toString());
				System.out.println(buffer);

				pw.println(buffer);
				pw.flush();
			}

			break;
		case C_APPLY_TO_ADD:
			String friendid = json.getString("friendid");
			String applyMessage = json.getString("request");
			MessageThread messageThread = clientManager.returnThread(friendid);
			JSONObject apply = new JSONObject();
			apply.accumulate("Flags", S_APPLY_TO_ADD);
			apply.accumulate("friendid", friendid);
			apply.accumulate("username", this.username);
			apply.accumulate("request", applyMessage);
			StringBuffer buffer2 = new StringBuffer(apply.toString());
			messageThread.pw.println(buffer2);
			System.out.println(buffer2);
			messageThread.pw.flush();
			break;

		case C_ADD_FRIEND:
			connector = new DatabaseConnector();
			connector.add_friend(json.getString("friendid"),
					json.getString("username"));
			connector.add_friend(json.getString("username"),
					json.getString("friendid"));

			MessageThread messageThread2 = clientManager.returnThread(json
					.getString("username"));
			JSONObject replyJsonObject = new JSONObject();
			replyJsonObject = creatFlags(S_ADD_FRIEND_SUCCESS);
			StringBuffer buffer = new StringBuffer(replyJsonObject.toString());
			System.out.println("add friend suc:" + buffer);

			System.out.println("1" + buffer);
			messageThread2.pw.println(buffer);
			messageThread2.pw.flush();
			System.out.println("2" + buffer);
			pw.println(buffer);
			pw.flush();
			break;

		case C_DELETE_FRIEND:
			connector = new DatabaseConnector();
			String id_src = json.getString("username");
			String id_des = json.getString("friendid");
			connector.rm_friend(id_des, id_src);
			connector.closeConnection();
			MessageThread mt = clientManager.returnThread(id_des);
			JSONObject delete = new JSONObject();
			delete = creatFlags(S_DELETE_FRIEND);
			StringBuffer buffer3 = new StringBuffer(delete.toString());
			System.out.println("delete:" + buffer3);
			pw.println(buffer3);
			pw.flush();
			mt.pw.println(buffer3);
			mt.pw.flush();
			break;

		case C_CHANGE_INFO:
			connector = new DatabaseConnector();

			break;
		case C_REQUEST_CHAT:
			connector = new DatabaseConnector();

			String friendname = json.getString("friendId");
			System.out.println("C:" + friendname);
			System.out.println(clientManager.size());
			if (friendname != null) {
				MessageThread tmpThread = clientManager
						.returnThread(friendname);

				if (tmpThread != null) {

					System.out.println(tmpThread.username);
					JSONObject tempJsonObject = connector
							.return_info(this.username);
					tempJsonObject.accumulate("Flags", S_REQUEST_CHAT);
					System.out.println(tempJsonObject);
					StringBuffer buffer1 = new StringBuffer(
							tempJsonObject.toString());
					tmpThread.pw.println(buffer1);
					tmpThread.pw.flush();
				} else {
					System.out.println("Thread not exist");
				}
			} else {
				// 该好友不在线

			}
			break;

		default:
			System.out.println("no flag");
			break;
		}

	}

	public JSONObject creatFlags(int flagrecv) {
		JSONObject jsonReturned = new JSONObject();
		try {

			jsonReturned.accumulate("Flags", flagrecv);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonReturned;
	}

}
