package lk.ijse.dep.fx.util;

import lk.ijse.dep.fx.db.DBConnection;
import lk.ijse.dep.fx.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageCustomers {

    public static ArrayList<Customer> getCustomers() {

        ArrayList<Customer> alCustomers = new ArrayList<>();
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Customer");
            ResultSet rst = pstm.executeQuery();
            while (rst.next()) {
                String id = rst.getString(1);
                String name = rst.getString(2);
                String address = rst.getString(3);
                Customer customer = new Customer(id, name, address);
                alCustomers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alCustomers;

    }

    public static void createCustomer(Customer customer) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
            pstm.setObject(1, customer.getId());
            pstm.setObject(2, customer.getName());
            pstm.setObject(3, customer.getAddress());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCustomer(String customerID, Customer customer) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("UPDATE Customer SET name=?,address=? WHERE id=?");
            pstm.setObject(3, customer.getId());
            pstm.setObject(1, customer.getName());
            pstm.setObject(2, customer.getAddress());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCustomer(String customerID) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Customer WHERE id=?");
            pstm.setObject(1, customerID);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Customer findCustomer(String id) {
        for (Customer customer : getCustomers()) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }

}
