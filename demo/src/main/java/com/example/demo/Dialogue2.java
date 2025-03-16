package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class Dialogue2 {
    private Dash parentController;

    public void setParentController(Dash parentController) {
        this.parentController = parentController;
    }
    @FXML
    public void ClosePopup(){
        parentController.hidePopup2();
    }
    @FXML
    public void Logout(ActionEvent event) throws IOException {
        parentController.Switch2(event);
    }
}

