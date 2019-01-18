package lk.ijse.dep.fx.util;

import lk.ijse.dep.fx.db.DBConnection;
import lk.ijse.dep.fx.model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManageItems {

    public static ArrayList<Item> getItems() {
        ArrayList<Item> alItems = new ArrayList<>();
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Items");
            ResultSet rst = pstm.executeQuery();
            while (rst.next()) {
                String code = rst.getString(1);
                String description = rst.getString(2);
                double unitPrice = rst.getDouble(3);
                int qty = rst.getInt(4);
                Item item = new Item(code, description, unitPrice, qty);
                alItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alItems;
    }

    public static void createItem(Item item) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO Item VALUES (?,?,?,?)");
            pstm.setObject(1, item.getCode());
            pstm.setObject(2, item.getDescription());
            pstm.setObject(3, item.getUnitPrice());
            pstm.setObject(4, item.getQtyOnHand());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void updateItem(String code, Item item) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("UPDATE Item SET description=?,unitPrice=?,qtyOnHand=? WHERE code=?");
            pstm.setObject(4, code);
            pstm.setObject(1, item.getDescription());
            pstm.setObject(2, item.getUnitPrice());
            pstm.setObject(3, item.getQtyOnHand());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteItem(String code) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
            pstm.setObject(1, code);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static Item findItem(String itemCode) {
        for (Item item : getItems()) {
            if (item.getCode().equals(itemCode)) {
                return item;
            }
        }
        return null;
    }
}
