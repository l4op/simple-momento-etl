package it.loooop;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import it.loooop.model.Cache;
import it.loooop.model.TableInfo;
import it.loooop.service.MomentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class FromDbToCache implements RequestHandler<TableInfo, Cache> {

    Logger logger = LoggerFactory.getLogger(FromDbToCache.class);
    MomentoService momentoService = new MomentoService("MOMENTO_AUTH_TOKEN");
    Connection conn = null;

    @Override
    public Cache handleRequest(TableInfo tableInfo, Context context) {

        Integer rows = 0;
        String key = "";
        try {
            // Connect to Postgres
            conn = getRemoteConnection();
            //logger.info("MOMENTO TOKEN: {}",System.getenv("MOMENTO_AUTH_TOKEN"));
            momentoService.create(tableInfo.getTable());

            Statement readStatement = conn.createStatement();
            ResultSet resultSet = readStatement.executeQuery(tableInfo.getQuery());

            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnNumber = rsmd.getColumnCount();

            while (resultSet.next()) {

                for (int i = 1; i <= columnNumber; i++) {

                    if(i == 1)
                        key = rsmd.getColumnName(i);

                    momentoService.saveItem(tableInfo.getTable(),
                            rows.toString(),
                            rsmd.getColumnName(i),
                            resultSet.getString(rsmd.getColumnName(i)));
                }

                rows++;
            }

            resultSet.close();
            readStatement.close();

        } catch (SQLException e) {
            logger.atError().setCause(e).log("Error accessing PosgreSQL: {}", e.getMessage());
          } finally {
            logger.info("Closing the connection to PostgreSQL.");
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.atError().setCause(e).log("Error closing connection with PosgreSQL: {}", e.getMessage());
                }
        }

        return new Cache(tableInfo.getTable(),rows, key );
    }

    private  Connection getRemoteConnection() {
        if (System.getenv("PG_HOSTNAME") != null) {
            try {
                Class.forName("org.postgresql.Driver");
                String dbName = System.getenv("PG_DB_NAME");
                String userName = System.getenv("PG_USERNAME");
                String password = System.getenv("PG_PASSWORD");
                String hostname = System.getenv("PG_HOSTNAME");
                String port = System.getenv("PG_PORT");
                String jdbcUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName
                        + "&password=" + password;
                logger.info("Getting remote connection with connection string from environment variables.");
                Connection con = DriverManager.getConnection(jdbcUrl);
                logger.info("Remote connection successful.");
                return con;
            } catch (SQLException | ClassNotFoundException e) {
                logger.atError().setCause(e).log("Error connecting to PostgreSQL: {}", e.getMessage());
            }
        }
        return null;
    }
}
