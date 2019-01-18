package lk.ijse.dep.fx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null){
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/placeorder", "root", "Sfdo@123");
        }
        return connection;
    }

}
