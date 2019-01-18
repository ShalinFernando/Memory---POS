package lk.ijse.dep.fx.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.dep.fx.db.DBConnection;
import lk.ijse.dep.fx.model.Customer;
import lk.ijse.dep.fx.model.Item;
import lk.ijse.dep.fx.model.Order;
import lk.ijse.dep.fx.model.OrderDetail;
import lk.ijse.dep.fx.util.ManageCustomers;
import lk.ijse.dep.fx.util.ManageItems;
import lk.ijse.dep.fx.util.ManageOrders;
import lk.ijse.dep.fx.view.util.OrderDetailTM;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaceOrderFormController {
    public Button btnPrint;
    @FXML
    private JFXButton btnPlaceOrder;
    @FXML
    private JFXTextField txtCustomerId;
    @FXML
    private JFXTextField txtItemCode;
    @FXML
    private JFXTextField txtCustomerName;
    @FXML
    private JFXTextField txtDescription;
    @FXML
    private JFXTextField txtQtyOnHand;
    @FXML
    private JFXTextField txtUnitPrice;
    @FXML
    private JFXTextField txtQty;
    @FXML
    private TableView<OrderDetailTM> tblOrderDetails;
    @FXML
    private JFXButton btnRemove;
    @FXML
    private Label lblTotal;
    @FXML
    private JFXTextField txtOrderID;
    @FXML
    private JFXDatePicker txtOrderDate;

    private ObservableList<Item> tempItemsDB = FXCollections.observableArrayList();

    public void initialize() {

        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        ArrayList<Item> itemsDB = ManageItems.getItems();
        for (Item item : itemsDB) {
            tempItemsDB.add(new Item(item.getCode(), item.getDescription(), item.getUnitPrice(), item.getQtyOnHand()));
        }

        txtOrderID.setEditable(false);

        txtOrderID.setText(ManageOrders.generateOrderId());
        txtOrderDate.setValue(LocalDate.now());

        btnRemove.setDisable(true);
        btnPlaceOrder.setDisable(true);
        calculateTotal();

        Platform.runLater(() -> {
            txtCustomerId.requestFocus();
        });

        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderDetailTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderDetailTM> observable, OrderDetailTM oldValue, OrderDetailTM selectedOrderDetail) {

                if (selectedOrderDetail == null){
                    // Clear Selection
                    return;
                }

                txtItemCode.setText(selectedOrderDetail.getCode());
                txtDescription.setText(selectedOrderDetail.getDescription());
                txtUnitPrice.setText(selectedOrderDetail.getUnitPrice() + "");
                txtQty.setText(selectedOrderDetail.getQty() + "");
                txtQtyOnHand.setText(getItemFromTempDB(txtItemCode.getText()).getQtyOnHand() + "");

                txtItemCode.setEditable(false);
                btnRemove.setDisable(false);

            }
        });

        tblOrderDetails.getItems().addListener(new ListChangeListener<OrderDetailTM>() {
            @Override
            public void onChanged(Change<? extends OrderDetailTM> c) {
                calculateTotal();

                btnPlaceOrder.setDisable(tblOrderDetails.getItems().size() == 0);
            }
        });

    }

    @FXML
    private void navigateToMain(MouseEvent event) throws IOException {
        Label lblMainNav = (Label) event.getSource();
        Stage primaryStage = (Stage) lblMainNav.getScene().getWindow();

        Parent root = FXMLLoader.load(this.getClass().getResource("/lk/ijse/dep/fx/view/MainForm.fxml"));
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        primaryStage.centerOnScreen();
    }


    @FXML
    private void btnSaveOnAction(ActionEvent event) {

        if (validateItemCode() == null) {
            return;
        }

        String qty = txtQty.getText();
        if (!isInt(qty)) {
            showInvalidateMsgBox("Qty should be a number");
            return;
        } else if (Integer.parseInt(qty) == 0) {
            showInvalidateMsgBox("Qty can't be zero");
            return;
        } else if (Integer.parseInt(qty) > Integer.parseInt(txtQtyOnHand.getText())) {
            showInvalidateMsgBox("Invalid Qty");
            return;
        }

        if (tblOrderDetails.getSelectionModel().isEmpty()) {
            // New

            OrderDetailTM orderDetailTM = null;

            if ((orderDetailTM = isItemExist(txtItemCode.getText())) == null) {

                OrderDetailTM newOrderDetailTM = new OrderDetailTM(txtItemCode.getText(),
                        txtDescription.getText(),
                        Integer.parseInt(qty),
                        Double.parseDouble(txtUnitPrice.getText()),
                        Integer.parseInt(qty) * Double.parseDouble(txtUnitPrice.getText()));

                tblOrderDetails.getItems().add(newOrderDetailTM);

            } else {
                orderDetailTM.setQty(orderDetailTM.getQty() + Integer.parseInt(qty));
            }



        } else {
            // Update
            OrderDetailTM selectedItem = tblOrderDetails.getSelectionModel().getSelectedItem();
            synchronizeQty(selectedItem.getCode());
            selectedItem.setQty(Integer.parseInt(qty));
        }

        setTempQty(txtItemCode.getText(), Integer.parseInt(qty));
        tblOrderDetails.refresh();
        reset();

//        calculateTotal();
    }

    @FXML
    private void btnRemoveOnAction(ActionEvent actionEvent) {

        OrderDetailTM selectedItem = tblOrderDetails.getSelectionModel().getSelectedItem();
        tblOrderDetails.getItems().remove(selectedItem);

        synchronizeQty(selectedItem.getCode());
        reset();

//        calculateTotal();

    }

    @FXML
    private void btnPlaceOrderOnAction(ActionEvent actionEvent) {

        if (txtCustomerId.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Can't place a order without a customer Id", ButtonType.OK).showAndWait();
            txtCustomerId.requestFocus();
            return;
        }

        ObservableList<OrderDetailTM> items = tblOrderDetails.getItems();
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderDetailTM item : items) {
            orderDetails.add(new OrderDetail(item.getCode(),item.getDescription(),item.getQty(),item.getUnitPrice()));
        }
        ManageOrders.createOrder(new Order(txtOrderID.getText(), txtOrderDate.getValue(),txtCustomerId.getText(),orderDetails));

        new Alert(Alert.AlertType.CONFIRMATION,"Order has been placed successfully", ButtonType.OK).showAndWait();
        hardReset();

    }

    private void hardReset() {
        reset();
        tblOrderDetails.getItems().removeAll(tblOrderDetails.getItems());
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtOrderID.setText(ManageOrders.generateOrderId());
        txtCustomerId.requestFocus();
    }

    public void calculateTotal() {
        ObservableList<OrderDetailTM> items = tblOrderDetails.getItems();

        double total = 0.0;

        for (OrderDetailTM item : items) {
            total += item.getTotal();
        }

        lblTotal.setText("Total : " + total + "");
    }

    @FXML
    private void txtCustomerID_OnAction(ActionEvent actionEvent) {

        String customerID = txtCustomerId.getText();

        Customer customer = ManageCustomers.findCustomer(customerID);

        if (customer == null) {
            new Alert(Alert.AlertType.ERROR, "Invalid Customer ID", ButtonType.OK).showAndWait();
            txtCustomerName.clear();
            txtCustomerId.requestFocus();
            txtCustomerId.selectAll();
        } else {
            txtCustomerName.setText(customer.getName());
            txtItemCode.requestFocus();
        }

    }

    @FXML
    private void txtItemCode_OnAction(ActionEvent actionEvent) {

        Item item = validateItemCode();

        if (item != null) {

            txtDescription.setText(item.getDescription());
            txtQtyOnHand.setText(getItemFromTempDB(item.getCode()).getQtyOnHand() + "");
            txtUnitPrice.setText(item.getUnitPrice() + "");
            txtQty.requestFocus();
        }

    }

    @FXML
    private void txtQty_OnAction(ActionEvent actionEvent) {
        btnSaveOnAction(actionEvent);
    }

    private Item validateItemCode() {
        String itemCode = txtItemCode.getText();

        Item item = ManageItems.findItem(itemCode);

        if (item == null) {
            new Alert(Alert.AlertType.ERROR, "Invalid Item Code", ButtonType.OK).showAndWait();
            txtDescription.clear();
            txtQtyOnHand.clear();
            txtUnitPrice.clear();
            txtQty.clear();
            txtItemCode.requestFocus();
            txtItemCode.selectAll();
        }
        return item;
    }

    public boolean isInt(String number) {
        char[] chars = number.toCharArray();
        for (char aChar : chars) {
            if (!Character.isDigit(aChar)) {
                return false;
            }
        }
        return true;
    }

    public Item getItemFromTempDB(String itemCode) {
        for (Item item : tempItemsDB) {
            if (item.getCode().equals(itemCode)) {
                return item;
            }
        }
        return null;
    }

    private void showInvalidateMsgBox(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
        txtQty.requestFocus();
        txtQty.selectAll();
    }

    private OrderDetailTM isItemExist(String itemCode) {
        ObservableList<OrderDetailTM> items = tblOrderDetails.getItems();
        for (OrderDetailTM item : items) {
            if (item.getCode().equals(itemCode)) {
                return item;
            }
        }
        return null;
    }

    public void reset() {
        tblOrderDetails.refresh();
        txtItemCode.clear();
        txtDescription.clear();
        txtQty.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        txtItemCode.setEditable(true);
        btnRemove.setDisable(true);
        tblOrderDetails.getSelectionModel().clearSelection();
        txtItemCode.requestFocus();
    }

    private void setTempQty(String itemCode, int qty) {
        for (Item item : tempItemsDB) {
            if (item.getCode().equals(itemCode)) {
                item.setQtyOnHand(item.getQtyOnHand() - qty);
                break;
            }
        }
    }

    private void synchronizeQty(String itemCode){
        int qtyOnHand = ManageItems.findItem(itemCode).getQtyOnHand();
        for (Item item : tempItemsDB) {
            if (item.getCode().equals(itemCode)){
                item.setQtyOnHand(qtyOnHand);
                return;
            }
        }
    }

    public void btnPrint_OnAction(ActionEvent actionEvent) throws JRException, SQLException {

        File file = new File("Jasper/FXPOS.jasper");
        JasperReport compiledReport = (JasperReport) JRLoader.loadObject(file);

//        DefaultTableModel dtm = new DefaultTableModel(new Object[]{"id","name","addrs"},0);


        Map<String,Object> param=new HashMap<>();
        param.put("orderid",txtOrderID.getText());
        param.put("orderdate",txtOrderDate.getValue());
        param.put("customerid",txtCustomerId.getText());
        param.put("customername",txtCustomerName.getText());
        JasperPrint filledReport = JasperFillManager.fillReport(compiledReport, param, DBConnection.getConnection());

        JasperViewer.viewReport(filledReport,false);

    }
}
