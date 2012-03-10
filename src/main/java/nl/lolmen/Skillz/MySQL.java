package nl.lolmen.Skillz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.lolmen.Skills.SkillsSettings;

public class MySQL {
	
	private String host, username, password, database, table;
	private int port;
	
	private boolean fault;
	
	private Statement st;
	private Connection con;
	
	public MySQL(String host, int port, String username, String password, String database, String table){
		this.host = host;
		this.username = username;
		this.password = password;
		this.database = database;
		this.table = table;
		this.port = port;
		this.connect();
		this.setupDatabase();
	}

	private void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database;
			System.out.println("[Skillz] Connecting to database on " + url);
			this.con = DriverManager.getConnection(url, this.username, this.password);
			this.st = con.createStatement();
			System.out.println("[Skillz] MySQL initiated succesfully!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.setFault(true);
		} catch (SQLException e) {
			e.printStackTrace();
			this.setFault(true);
		} finally {
			if(this.fault){
				System.out.println("[Skillz] MySQL initialisation failed!");
			}
		}
	}

	private void setupDatabase() {
		if(this.isFault()){
			return;
		}
		this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.table + 
				"(player varchar(255), " +
				"skill varchar(255), " +
				"xp int, " +
				"level int)");
	}

	public boolean isFault() {
		return fault;
	}

	private void setFault(boolean fault) {
		this.fault = fault;
	}
	
	public int executeStatement(String statement){
		if(isFault()){
			System.out.println("[Skillz] Can't execute statement, something wrong with connection");
			return 0;
		}
		if(SkillsSettings.isDebug()){
			System.out.println("Statement: " + statement);
		}
		try {
			this.st = this.con.createStatement();
			int re = this.st.executeUpdate(statement);
			this.st.close();
			return re;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public ResultSet executeQuery(String statement){
		if(isFault()){
			System.out.println("[Skillz] Can't execute query, something wrong with connection");
			return null;
		}
		if(statement.toLowerCase().startsWith("update") || statement.toLowerCase().startsWith("insert") || statement.toLowerCase().startsWith("delete")){
			this.executeStatement(statement);
			return null;
		}
		if(SkillsSettings.isDebug()){
			System.out.println("[Skillz - Debug] Query: " + statement);
		}
		try {
			this.st = this.con.createStatement();
			ResultSet set = this.st.executeQuery(statement);
			//this.st.close();
			return set;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		if(isFault()){
			System.out.println("[Skillz] Can't close connection, something wrong with it");
			return;
		}
		try {
			this.con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}