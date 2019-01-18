/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.ijse.dep.fx.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep.fx.main.AppInitializer;
import lk.ijse.dep.fx.model.Customer;
import lk.ijse.dep.fx.util.ManageCustomers;
import lk.ijse.dep.fx.view.util.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author ranjith-suranga
 */
public class ManageCustomerFormController implements Initializable {

    @FXML
    private JFXButton btnSave;
    @FXML
    private JFXButton btnDelete;
    @FXML
    private AnchorPane root;
    @FXML
    private JFXTextField txtCustomerId;
    @FXML
    private JFXTextField txtCustomerName;
    @FXML
    private JFXTextField txtCustomerAddress;

    @FXML
    private TableView<CustomerTM> tblCustomers;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tblCustomers.getColumns().get(0).setStyle("-fx-alignment:center");
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        btnSave.setDisable(true);
        btnDelete.setDisable(true);

        ArrayList<Customer> customersDB = ManageCustomers.getCustomers();
        ObservableList<Customer> customers = FXCollections.observableArrayList(customersDB);
        ObservableList<CustomerTM> tblItems = FXCollections.observableArrayList();
        for (Customer customer : customers) {
            tblItems.add(new CustomerTM(customer.getId(), customer.getName(), customer.getAddress()));
        }
        tblCustomers.setItems(tblItems);

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM selectedCustomer) {

                if (selectedCustomer == null) {
                    // Clear Selection
                    return;
                }

                txtCustomerId.setText(selectedCustomer.getId());
                txtCustomerName.setText(selectedCustomer.getName());
                txtCustomerAddress.setText(selectedCustomer.getAddress());

                txtCustomerId.setEditable(false);

                btnSave.setDisable(false);
                btnDelete.setDisable(false);

            }
        });
    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        AppInitializer.navigateToHome(root, (Stage) this.root.getScene().getWindow());
    }

    @FXML
    private void btnSave_OnAction(ActionEvent event) {

        if (txtCustomerId.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Customer ID is empty",ButtonType.OK).showAndWait();
            txtCustomerId.requestFocus();
            return;
        }else if(txtCustomerName.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Customer Name is empty",ButtonType.OK).showAndWait();
            txtCustomerName.requestFocus();
            return;
        }else if(txtCustomerAddress.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Customer Address is empty",ButtonType.OK).showAndWait();
            txtCustomerAddress.requestFocus();
            return;
        }

        if (tblCustomers.getSelectionModel().isEmpty()) {
            // New

            ObservableList<CustomerTM> items = tblCustomers.getItems();
            for (CustomerTM customerTM : items) {
                if (customerTM.getId().equals(txtCustomerId.getText())){
                    new Alert(Alert.AlertType.ERROR,"Duplicate Customer IDs are not allowed").showAndWait();
                    txtCustomerId.requestFocus();
                    return;
                }
            }

            CustomerTM customerTM = new CustomerTM(txtCustomerId.getText(), txtCustomerName.getText(), txtCustomerAddress.getText());
            tblCustomers.getItems().add(customerTM);
            Customer customer = new Customer(txtCustomerId.getText(), txtCustomerName.getText(), txtCustomerAddress.getText());
            ManageCustomers.createCustomer(customer);

            new Alert(Alert.AlertType.INFORMATION, "Customer has been saved successfully",ButtonType.OK).showAndWait();
            tblCustomers.scrollTo(customerTM);

        } else {
            // Update

            CustomerTM selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            selectedCustomer.setName(txtCustomerName.getText());
            selectedCustomer.setAddress(txtCustomerAddress.getText());
            tblCustomers.refresh();

            String selectedCustomerID = tblCustomers.getSelectionModel().getSelectedItem().getId();

            ManageCustomers.updateCustomer(selectedCustomerID,new Customer(txtCustomerId.getText(),
                    txtCustomerName.getText(),
                    txtCustomerAddress.getText()));

            new Alert(Alert.AlertType.INFORMATION,"Customer has been updated successfully", ButtonType.OK).showAndWait();
        }

        reset();

    }

    @FXML
    private void btnDelete_OnAction(ActionEvent event) {
        Alert confirmMsg = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this customer?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = confirmMsg.showAndWait();

        if (buttonType.get() == ButtonType.YES) {
            String selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem().getId();

            tblCustomers.getItems().remove(tblCustomers.getSelectionModel().getSelectedItem());
            ManageCustomers.deleteCustomer(selectedCustomer);
            reset();
        }

    }

    @FXML
    private void btnAddNew_OnAction(ActionEvent actionEvent) {
        reset();
    }

    private void reset() {
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        txtCustomerId.requestFocus();
        txtCustomerId.setEditable(true);
        btnSave.setDisable(false);
        btnDelete.setDisable(true);
        tblCustomers.getSelectionModel().clearSelection();
    }

}
