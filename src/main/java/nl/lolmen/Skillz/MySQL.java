package nl.lolmen.Skillz;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.lolmen.Skills.SkillsSettings;

public class MySQL {

    private String host, username, password, database, table;
    private int port;
    private boolean fault;
    
    private JDCConnection con;

    public MySQL(String host, int port, String username, String password, String database, String table) {
        this.table = table;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.setupDatabase();
    }

    private void setupDatabase() {
        if (this.isFault()) {
            return;
        }
        this.executeStatement("CREATE TABLE IF NOT EXISTS " + this.table
                + "(player varchar(255), "
                + "skill varchar(255), "
                + "xp int, "
                + "level int)");
    }

    public boolean isFault() {
        return fault;
    }

    private void setFault(boolean fault) {
        this.fault = fault;
    }

    public int executeStatement(String statement) {
        if (isFault()) {
            System.out.println("[Skillz] Can't execute statement, something wrong with connection");
            return 0;
        }
        if (SkillsSettings.isDebug()) {
            System.out.println("[Skillz - Debug] Statement: " + statement);
        }
        try {
            Connection con = this.getConnection();
            Statement st = con.createStatement();
            int re = st.executeUpdate(statement);
            st.close();
            con.close();
            return re;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResultSet executeQuery(String statement) {
        if (isFault()) {
            System.out.println("[Skillz] Can't execute query, something wrong with connection");
            return null;
        }
        if (statement.toLowerCase().startsWith("update") || statement.toLowerCase().startsWith("insert") || statement.toLowerCase().startsWith("delete")) {
            this.executeStatement(statement);
            return null;
        }
        if (SkillsSettings.isDebug()) {
            System.out.println("[Skillz - Debug] Query: " + statement);
        }
        try {
            Connection con = this.getConnection();
            Statement st = con.createStatement();
            con.close();
            return st.executeQuery(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (isFault()) {
            System.out.println("[Skillz] Can't close connection, something wrong with it");
            return;
        }
        this.con.close();
    }

    void clean(String table) {
        ResultSet set = this.executeQuery("SELECT COUNT(*), player, skill FROM " + table + " GROUP BY player,skill HAVING COUNT(*) >1;");
        //finds duplicate entries
        if(set == null){
            System.out.println("Something is wrong with the database, query returned null");
            return;
        }
        try {
            while(set.next()){
                this.executeStatement("DELETE FROM " + table + " WHERE player='" + set.getString("player") + "' AND skill='" + set.getString("skill") + "' LIMIT " + (set.getInt(1) - 1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public JDCConnection getConnection(){
        if (this.con != null) {
            if (this.con.lease()) {
                if (con.isValid()) {
                    return con;
                }
            }
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            if(SkillsSettings.isDebug()){
                System.out.println("[Skillz] Connection created to MySQL database");
            }
            try {
                return (this.con = new JDCConnection(DriverManager.getConnection(url, username, password)));
            } catch (SQLException ex) {
                Logger.getLogger(MySQL.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.setFault(true);
        } finally {
            if (this.fault) {
                System.out.println("MySQL connection failed!");
            }
        }
        return null;
    }
    
    private class JDCConnection implements Connection {

        private final Connection conn;
        private boolean inuse;
        private long timestamp;
        private int networkTimeout;
        private String schema;

        JDCConnection(Connection conn) {
            this.conn = conn;
            inuse = false;
            timestamp = 0;
            networkTimeout = 30;
            schema = "default";
        }

        @Override
        public void clearWarnings() throws SQLException {
            conn.clearWarnings();
        }

        @Override
        public void close() {
            inuse = false;
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
            } catch (final SQLException ex) {
                terminate();
            }
        }

        @Override
        public void commit() throws SQLException {
            conn.commit();
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return conn.createArrayOf(typeName, elements);
        }

        @Override
        public Blob createBlob() throws SQLException {
            return conn.createBlob();
        }

        @Override
        public Clob createClob() throws SQLException {
            return conn.createClob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return conn.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return conn.createSQLXML();
        }

        @Override
        public Statement createStatement() throws SQLException {
            return conn.createStatement();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return conn.createStruct(typeName, attributes);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return conn.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException {
            return conn.getCatalog();
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return conn.getClientInfo();
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return conn.getClientInfo(name);
        }

        @Override
        public int getHoldability() throws SQLException {
            return conn.getHoldability();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return conn.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return conn.getTransactionIsolation();
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return conn.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return conn.getWarnings();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return conn.isClosed();
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return conn.isReadOnly();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return conn.isValid(timeout);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return conn.isWrapperFor(iface);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return conn.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            return conn.prepareCall(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return conn.prepareStatement(sql);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return conn.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return conn.prepareStatement(sql, columnIndexes);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return conn.prepareStatement(sql, columnNames);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            conn.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback() throws SQLException {
            conn.rollback();
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            conn.rollback(savepoint);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            conn.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            conn.setCatalog(catalog);
        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            conn.setClientInfo(properties);
        }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            conn.setClientInfo(name, value);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            conn.setHoldability(holdability);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            conn.setReadOnly(readOnly);
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return conn.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            return conn.setSavepoint(name);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            conn.setTransactionIsolation(level);
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            conn.setTypeMap(map);
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return conn.unwrap(iface);
        }

        @SuppressWarnings("unused")
        public int getNetworkTimeout() throws SQLException {
            return networkTimeout;
        }

        @SuppressWarnings("unused")
        public void setNetworkTimeout(Executor exec, int timeout) throws SQLException {
            networkTimeout = timeout;
        }

        @SuppressWarnings("unused")
        public void abort(Executor exec) throws SQLException {
            // Not implemented really...
        }

        @SuppressWarnings("unused")
        public String getSchema() throws SQLException {
            return schema;
        }

        @SuppressWarnings("unused")
        public void setSchema(String str) throws SQLException {
            schema = str;
        }

        long getLastUse() {
            return timestamp;
        }

        boolean inUse() {
            return inuse;
        }

        boolean isValid() {
            try {
                return conn.isValid(1);
            } catch (final SQLException ex) {
                return false;
            }
        }

        synchronized boolean lease() {
            if (inuse) {
                return false;
            }
            inuse = true;
            timestamp = System.currentTimeMillis();
            return true;
        }

        void terminate() {
            try {
                conn.close();
            } catch (final SQLException ex) {
            }
        }
    }
}