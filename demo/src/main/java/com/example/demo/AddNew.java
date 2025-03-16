package com.example.demo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNew {
    @FXML private TextField searchField;
    @FXML private VBox courseContainer;
    @FXML private Button addButton;

    private Dash parentController;

    MongoDatabase db = MongoDBConnection.connect();
    MongoCollection<Document> courseCollection = db.getCollection("courses");
    List<Document> courses = courseCollection.find().into(new ArrayList<>());
    List<HBox> allcourses = new ArrayList<>();
    private Map<HBox, String> selectedCourses = new HashMap<>();

    @FXML
    public void initialize() {
        load_courses();
        // Store all course HBoxes in a list

        // Listen for text changes in the search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCourses(newValue));

        addButton.setOnAction(event -> addSelectedCourse());


    }

    public void load_courses(){
        selectedCourses.clear();

        for (Document course : courses){
            Label label = new Label(course.getString("name"));
            label.setStyle("-fx-font-size: 18px; -fx-font-family: 'Work Sans Medium';");
            CheckBox check = new CheckBox();
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox box = new HBox(label, spacer, check);
            box.setMaxWidth(360);
            courseContainer.getChildren().add(box);
            allcourses.add(box);
            check.setOnAction(e -> {
                if(check.isSelected()){
                    selectedCourses.put(box, course.getObjectId("_id").toHexString());
                } else {
                    selectedCourses.remove(box);
                }
            });
        }
    }

    private void filterCourses(String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();

        for (HBox course : allcourses) {
            Label courseLabel = (Label) course.getChildren().get(0); // Get label inside HBox
            if (courseLabel.getText().toLowerCase().contains(lowerCaseKeyword)) {
                course.setVisible(true);
                course.setManaged(true);
            } else {
                course.setVisible(false);
                course.setManaged(false);
            }
        }
    }

    private void addSelectedCourse(){
        if (parentController == null || selectedCourses.isEmpty()) return;

        MongoCollection<Document> coursescollection = db.getCollection("courses");

        for (String courseId : selectedCourses.values()) {
            coursescollection.updateOne(
                    Filters.eq("_id", new ObjectId(courseId)),
                    Updates.addToSet("students_enrolled", new ObjectId(parentController.getUserId())) // Add student only if not already in the list
            );
        }

        parentController.addCourseWidgets(); // Refresh UI
        parentController.hidePopup(); // Close popup
    }


    public void setParentController(Dash parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void closePopup() {
        if (parentController != null) {
            parentController.hidePopup();
        }
    }
}
