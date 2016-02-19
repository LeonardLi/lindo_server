package com.xiaode.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

//import net.sf.json.JSONArray;
//import net.sf.json.JSONException;
//import net.sf.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class DatabaseConnector {
	Connection conn = null;

	public DatabaseConnector() {
		initConnector();

	}

	private boolean initConnector() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://127.0.0.1:3306/chatroom";
		String user = "xiaode";
		String password = "666666";

		try {

			Class.forName(driver);
			// connect to database throw ClassNotFoundException
			conn = DriverManager.getConnection(url, user, password);
			// build connection

			if (!conn.isClosed())

				System.out.println("Succeeded connecting to the Database!");

		} catch (ClassNotFoundException e) {
			System.out.println("Sorry,can`t find the Driver!");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}

	public boolean register(String id, String passwd, String ip,
			String nickname, String date, int age, String gender,
			String comment, int avatar) {
		boolean out = false;
		try {
			Statement statement = conn.createStatement();
			String sql = "select id from Account where id ='" + id + "'";
			ResultSet rs = statement.executeQuery(sql);
			if (!rs.next()) {
				statement.executeUpdate("insert into Account values('" + id
						+ "','" + passwd + "')");
				statement.executeUpdate("insert into info values('" + id
						+ "','" + nickname + "','" + ip + "',0,'" + date
						+ "','" + gender + "'," + age + ",'" + comment + "',"
						+ avatar + ")");
				// System.out.println("register1");
				out = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return out;
	}

	public boolean login(String id, String passwd, String ip) {
		boolean out = false;
		try {
			Statement statement = conn.createStatement();
			String sql = "select id,password from Account where id ='" + id
					+ "'";
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				String ID = rs.getString("id");
				String pass = rs.getString("password");
				if (ID.equals(id))
					if (pass.equals(passwd)) {
						sql = "update Info  set isAliveFlag =1,ip='" + ip
								+ "' where id ='" + id + "'";
						statement.executeUpdate(sql);
						out = true;
					}
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return out;
	}

	public int verify_add_friend(String id_des, String id_src) {
		/**
		 * @return 1:朋友已存在 0:可以添加好友 -1:该好友不在线或不存在
		 */
		int out = -1;
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
			rs = statement
					.executeQuery("select friendid from friendlist where friendlistid ='"
							+ id_src + "' and friendid='" + id_des + "'");
			if (rs.next()) {
				System.out.println("friend exists!");
				return 1;
			}
			String sql = "select isAliveFlag from info where id ='" + id_des
					+ "'";
			rs = statement.executeQuery(sql);
			if (rs.next()) {

				if (rs.getInt("isAliveFlag") == 1) {
					out = 0;

				}
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return out;
	}

	public void add_friend(String id_des, String id_src) {
		try {
			Statement statement = conn.createStatement();
			statement.executeUpdate("insert into friendlist values('" + id_src
					+ "','" + id_des + "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void rm_friend(String id_des, String id_src) {
		try {
			Statement statement = conn.createStatement();
			statement
					.executeUpdate("delete from friendlist where friendlistid='"
							+ id_des + "' and friendid='" + id_src + "'");
			statement
					.executeUpdate("delete from friendlist where friendlistid='"
							+ id_src + "' and friendid='" + id_des + "'");
		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	public JSONArray return_friends(String id) {
		JSONArray jarray = new JSONArray();
		try {
			JSONObject json = new JSONObject();
			Statement statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("select id,nickname,ip,isAliveFlag,birthday,gender,age,comment,avatar from Info where id='"
							+ id + "' ");
			while (rs.next()) {
				json.accumulate("friendid", rs.getString("id"));
				json.accumulate("nickname", rs.getString("nickname"));
				json.accumulate("ip", rs.getString("ip"));
				json.accumulate("isAliveFlag", rs.getInt("isAliveFlag"));
				json.accumulate("birthday", rs.getString("birthday"));
				json.accumulate("gender", rs.getString("gender"));
				json.accumulate("age", rs.getInt("age"));
				json.accumulate("comment", rs.getString("comment"));
				json.accumulate("avatar", rs.getInt("avatar"));
				jarray.put(json);
				json = new JSONObject();
				//jarray.add(json);

			}
			rs = statement
					.executeQuery("select friendid,nickname,ip,isAliveFlag,birthday,gender,age,comment,avatar from Info,Friendlist where friendlistid='"
							+ id + "' and friendid=info.id ");
			while (rs.next()) {
				// System.out.println(rs.getString("friendid"));
				json.accumulate("friendid", rs.getString("friendid"));
				// System.out.println(rs.getString("nickname"));
				json.accumulate("nickname", rs.getString("nickname"));
				// System.out.println(rs.getString("ip"));
				json.accumulate("ip", rs.getString("ip"));
				json.accumulate("isAliveFlag", rs.getInt("isAliveFlag"));
				json.accumulate("birthday", rs.getString("birthday"));
				json.accumulate("gender", rs.getString("gender"));
				json.accumulate("age", rs.getInt("age"));
				json.accumulate("comment", rs.getString("comment"));
				json.accumulate("avatar", rs.getInt("avatar"));
				jarray.put(json);
				json = new JSONObject();
//				jarray.add(json);
//				json.clear();
			}
		} catch (SQLException e) {
			e.printStackTrace();

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jarray;
	}

	public void logout(String id) {
		try {
			Statement statement = conn.createStatement();
			statement
					.executeUpdate("update Info set isAliveFlag =0 where id ='"
							+ id + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void alter_info(String id, String passwd, String nickname,
			String date, int age, String gender, String comment, int avatar) {
		Statement statement = null;
		try {
			statement = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			statement.executeUpdate("update Info set nickname ='" + nickname
					+ "',birthday='" + date + "',age=" + age + ",gender='"
					+ gender + "',comment='" + comment + "',avatar=" + avatar
					+ " where id ='" + id + "'");
			statement.executeUpdate("update account set password='" + passwd
					+ "' where id ='" + id + "'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JSONObject return_info(String id) {
		JSONObject json = new JSONObject();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			rs = statement
					.executeQuery("select id,nickname,ip,isAliveFlag,birthday,gender,age,comment,avatar from Info where id='"
							+ id + "' ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (rs.next()) {
				json.accumulate("username", rs.getString("id"));
				json.accumulate("nickname", rs.getString("nickname"));
				json.accumulate("ip", rs.getString("ip"));
				json.accumulate("isAliveFlag", rs.getInt("isAliveFlag"));
				json.accumulate("birthday", rs.getString("birthday"));
				json.accumulate("gender", rs.getString("gender"));
				json.accumulate("age", rs.getInt("age"));
				json.accumulate("comment", rs.getString("comment"));
				json.accumulate("avatar", rs.getInt("avatar"));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	public void closeConnection() {
		try {
			this.conn.close();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public JSONArray returnAliveUser() {
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("select id from info where isAliveFlag = 1");
			while (rs.next()) {
				json.accumulate("username", rs.getString("id"));
				jsonArray.put(json);
				json = new JSONObject();
				//jsonArray.add(json);
				//json.clear();
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return jsonArray;
	}

	public Vector<String> returnFriendName(String id) {
		Vector<String> temp = new Vector<String>();
		Statement statement;
		try {
			statement = conn.createStatement();
			ResultSet rs = statement
					.executeQuery("select friendid from info,friendlist where info.id=friendlistid and isAliveFlag = 1 and friendlistid ='"
							+ id + "'");
			while (rs.next()) {
				temp.add(rs.getString("friendid"));
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return temp;

	}
}