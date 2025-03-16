package com.example.demo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;


import java.io.IOException;
import java.util.Objects;

public class Login {
    @FXML
    private Label userError;
    @FXML
    private Label passwordError;
    private Stage stage;
    private Scene scene;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;


    @FXML
    public void initialize() throws IOException {
        error();
    }
    public void Switch1(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("signup.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void Authenticate(ActionEvent event) throws IOException {
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");
        final String Username = username.getText().trim();
        final String Password = password.getText().trim();
        Document user = users.find(Filters.eq("username", Username)).first();
        FXMLLoader loader2 = new  FXMLLoader(getClass().getResource("signup.fxml"));
        loader2.load();
        Signup controller = loader2.getController();

        if(user != null){
            String sPassword = user.getString("password");
            String userId = user.getObjectId("_id").toHexString();
            String role = user.getString("role");


            if(sPassword.equals(Password)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dash.fxml"));
                Parent root = loader.load();
                Dash dashController = loader.getController();
                dashController.setUser(userId, role);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            }else{
                controller.showError(passwordError,"wrong password");
            }
        }else{
            controller.showError(passwordError, "wrong password");
            controller.showError(userError, "user doesn't exist");
        }
        if (password.getText().trim().isEmpty()){
            controller.showError(passwordError,"Field is required");
        }
        if (username.getText().trim().isEmpty()){
            controller.showError(userError,"username Field is required");
        }
    }
    public void error() throws IOException {
        FXMLLoader loader = new  FXMLLoader(getClass().getResource("signup.fxml"));
        loader.load();
        Signup controller = loader.getController();
        username.textProperty().addListener(((observableValue, s, newVal) -> {
            if(newVal.isEmpty()){
                controller.showError(userError, "username field is required");
            }else {
                controller.hideError(userError);
            }
        }));
        password.textProperty().addListener(((observableValue, s, newVal) -> {
            if(newVal.isEmpty()){
                controller.showError(passwordError, "password field is required");
            }else {
                controller.hideError(passwordError);
            }
        }));

    }
}
