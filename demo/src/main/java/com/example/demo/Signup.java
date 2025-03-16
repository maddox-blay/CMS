package com.example.demo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.bson.Document;

import java.io.IOException;

public class Signup {
    @FXML
    public TextField username;
    @FXML
    public PasswordField password;
    @FXML
    private Label passError;
    @FXML
    private Label confirmError;
    @FXML
    private PasswordField confirmPassword;
    private String Password;
    @FXML
    private StackPane popupOverlay;
    @FXML
    private VBox popupContainer;
    @FXML
    private Label existError;
    private Node popupContent;
    private com.example.demo.Dialogue1 popupController;



    @FXML
    public void initialize() {
        errors();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dialogue1.fxml"));
            popupContent = loader.load();
            popupController = loader.getController();
            popupController.setParentController(this);
            popupContainer.getChildren().add(popupContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createUser(String role){
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");
        final String name = username.getText().trim();
        if (password.getText().trim().equals(confirmPassword.getText().trim())){
            Password = password.getText();
        }
        Document user = new Document("username", name)
                .append("password", Password)
                .append("role", role);
        users.insertOne(user);

    }

    @FXML
    public void showPopup() {
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");
        Document user = users.find(Filters.eq("username",username.getText().trim())).first();
        boolean passConfirmed = password.getText().trim().equals(confirmPassword.getText().trim());
        boolean passText = !password.getText().isEmpty() && !confirmPassword.getText().isEmpty() ;
        boolean existingUser = user != null;

        if (!username.getText().isEmpty() && passText && passConfirmed && !existingUser) {
            popupOverlay.setVisible(true);
            popupContainer.setVisible(true);
        }
        if(username.getText().trim().isEmpty()){
            showError(existError, "Field is required");
        }
        if(existingUser){
            showError(existError,"user already exists");
        }
        if(!passText){
            showError(passError, "Field is required");
            showError(confirmError, "Field is required");
        }
        if(!passConfirmed){
            showError(confirmError, "passwords must match");
        }

    }
    public void errors(){
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");

        username.textProperty().addListener(((observableValue, s, nValue) -> {
            if (nValue.trim().isEmpty()){
                showError(existError,"Field is required");
                return;
            }else {
                hideError(existError);
            }
            Document user = users.find(Filters.eq("username",nValue.trim())).first();
            if(user!= null){
                showError(existError, "user already exists");
            }else {
                hideError(existError);
            }

        }));
        password.textProperty().addListener(((observableValue, s, nValue) -> {
            if(nValue.trim().isEmpty()){
                showError(passError, "Field is required");
            }else {
                hideError(passError);
            }

        }));
        confirmPassword.textProperty().addListener(((observableValue, s, nValue) -> {
            if(nValue.trim().isEmpty()){
                showError(confirmError, "Field is required");
            } else if (!nValue.trim().equals(password.getText().trim())) {
                showError(confirmError,"passwords must match");
            } else {
                hideError(confirmError);
            }


        }));

    }
    public void showError(Label label, String text){
        label.setText(text);
        label.setVisible(true);
        label.setManaged(true);
    }
    public void hideError(Label label){
        label.setVisible(false);
        label.setManaged(false);
    }
    public void hidePopup() {
        popupOverlay.setVisible(false);
        popupContainer.setVisible(false);
    }
}
