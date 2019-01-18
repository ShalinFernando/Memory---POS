package lk.ijse.dep.fx.model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Order {

    private String id;
    private LocalDate date;
    private String customerId;
    private ArrayList<OrderDetail> orderDetails = new ArrayList<>();

    public Order() {
    }

    public Order(String id, LocalDate date, String customerId, ArrayList<OrderDetail> orderDetails) {
        this.id = id;
        this.date = date;
        this.customerId = customerId;
        this.orderDetails = orderDetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ArrayList<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(ArrayList<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", customerId='" + customerId + '\'' +
                ", orderDetails=" + orderDetails +
                '}';
    }
}
