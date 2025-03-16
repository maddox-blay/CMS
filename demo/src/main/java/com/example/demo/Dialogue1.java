package com.example.demo;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class Dialogue1 {
    private Signup parentController;

    public String role = "";

    public void setInstructor() throws IOException {
        role = "Instructor";
        parentController.createUser(role);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        autoLogin();
    }
    public void setStudent() throws IOException {
        role = "Student";
        parentController.createUser(role);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        autoLogin();
    }

    public void setParentController(Signup parentController) {
        this.parentController = parentController;
    }

    public void closePopup() {
        if (parentController != null) {
            parentController.hidePopup();
        }
    }
    @FXML
    private void autoLogin() throws IOException {
        closePopup();
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");
        String uname = parentController.username.getText().trim();
        String pass = parentController.password.getText().trim();
        Document user = users.find(Filters.eq("username", uname)).first();

        if(user != null){
            String sPassword = user.getString("password");
            String userId = user.getObjectId("_id").toHexString();
            String role = user.getString("role");


            if(sPassword.equals(pass)){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dash.fxml"));
                Parent root = loader.load();
                Dash dashController = loader.getController();
                dashController.setUser(userId, role);

                Stage stage = (Stage) parentController.username.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();
            }
        }else{
            System.out.println('m');
        }
    }


}
