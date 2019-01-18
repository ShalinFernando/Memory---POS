package lk.ijse.dep.fx.util;

import lk.ijse.dep.fx.db.DBConnection;
import lk.ijse.dep.fx.model.Item;
import lk.ijse.dep.fx.model.Order;
import lk.ijse.dep.fx.model.OrderDetail;
import lk.ijse.dep.fx.view.util.OrderDetailTM;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ManageOrders {

    public static ArrayList<Order> getOrders(){
        ArrayList<Order> alOrders = new ArrayList<>();
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `Orderdetails`");
            ResultSet rst = pstm.executeQuery();
            while(rst.next()){
                String orderId = rst.getString(1);
                Date orderDate = rst.getDate(2);
                String customerId = rst.getString(3);

                PreparedStatement pstm2 = connection.prepareStatement("SELECT * FROM Orders WHERE orderId=?");
                pstm2.setObject(1,orderId);
                ResultSet rst2 = pstm2.executeQuery();

                ArrayList<OrderDetail> alOrderDetailList = new ArrayList<>();

                while(rst2.next()){
                    String itemCode = rst2.getString(2);
                    int qty = rst2.getInt(3);
                    double unitPrice = rst2.getDouble(4);
                    String description = ManageItems.findItem(itemCode).getDescription();
                    OrderDetail orderDetail = new OrderDetail(itemCode, description, qty, unitPrice);
                    alOrderDetailList.add(orderDetail);
                }

                Order order = new Order(orderId, orderDate.toLocalDate(), customerId, alOrderDetailList);
                alOrders.add(order);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alOrders;
    }

    public static String generateOrderId(){
        return getOrders().size() + 1 + "";
    }

    public static void createOrder(Order order){

        Connection connection = null;
        try {

            connection = DBConnection.getConnection();

            // connection.setTransactionIsolation();

            // (1) Starting the transaction
            connection.setAutoCommit(false);

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO `Orderdetails` VALUES (?,?,?)");
            pstm.setObject(1,order.getId());
            pstm.setObject(2,order.getDate());
            pstm.setObject(3,order.getCustomerId());
            int affectedRows = pstm.executeUpdate();

            if (affectedRows == 0){
                return;
            }

            PreparedStatement pstm2 = connection.prepareStatement("INSERT INTO Orders VALUES (?,?,?,?,?)");
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                pstm2.setObject(2,order.getId());
                pstm2.setObject(1, orderDetail.getCode());
                pstm2.setObject(5,orderDetail.getQty());
                pstm2.setObject(4, orderDetail.getUnitPrice());
                pstm2.setObject(3,orderDetail.getDescription());
                affectedRows = pstm2.executeUpdate();

//                if (true) {
//                    throw new RuntimeException();
//                }

                if (affectedRows == 0){
                    connection.rollback();
                    return;
                }

                int qtyOnHand = ManageItems.findItem(orderDetail.getCode()).getQtyOnHand();
                qtyOnHand -= orderDetail.getQty();

//                PreparedStatement pstm3 = connection.prepareStatement("UPDATE Items SET qtyOnHand=? WHERE code=?");
//                pstm3.setObject(1,qtyOnHand);
//                pstm3.setObject(2,orderDetail.getCode());
//
//                affectedRows = pstm3.executeUpdate();

                if (affectedRows ==0){
                    connection.rollback();
                    return;
                }
            }

            connection.commit();

        } catch (Exception e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally{
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

//        ordersDB.add(order);
//        for (OrderDetail orderDetail : order.getOrderDetails()) {
//            Item item = ManageItems.findItem(orderDetail.getCode());
//            item.setQtyOnHand(item.getQtyOnHand() - orderDetail.getQty());
//        }
    }

    public static Order findOrder(String orderId){
        for (Order order : getOrders()) {
            if (order.getId().equals(orderId)){
                return order;
            }
        }
        return null;
    }
}
