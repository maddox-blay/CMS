package com.example.demo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;


public class CourseWidget {
    @FXML
    private VBox detailsBox;
    private boolean expanded = false;
    @FXML
    private StackPane removeBubble;
    @FXML
    private Label courseName;
    @FXML
    private Label instructor;
    @FXML
    private Label percentage;
    @FXML
    private ProgressBar progressBar;


    @FXML
    public AnchorPane rootPane;
    private Dash parentController;
    public String CourseId;

    public void setParentController(Dash parent) {
        this.parentController = parent;
    }


    public void setDetails(Document course, String userId) {
        this.CourseId = course.getObjectId("_id").toHexString();
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");

        // Fetch instructor details
        Document instructorDoc = users.find(Filters.eq("_id", course.getObjectId("instructor_id"))).first();

        // Set course name
        courseName.setText(course.getString("name"));

        // Set instructor name
        if (instructorDoc != null) {
            instructor.setText("Instructor: " + instructorDoc.getString("username"));
        }

        List<Document> topics = (List<Document>) course.get("topics");
        ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();

        // Populate topics with checkboxes
        for (Document topic : topics) {
            Label text = new Label(topic.getString("text"));
            text.setStyle("-fx-font-size: 18px; -fx-font-family: 'Work Sans Medium';");

            Region spacer = new Region();
            CheckBox check = new CheckBox();

            // Set checkbox state based on database
            List<ObjectId> checkedBy = (List<ObjectId>) topic.get("checked_by");
            if (checkedBy != null && checkedBy.contains(new ObjectId(userId))) {
                check.setSelected(true);
            }

            // Add listener to save checkbox state when changed
            check.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> updateCheckboxState(course.getObjectId("_id"), topic.getString("text"), userId, isNowSelected));

            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox topicBox = new HBox(text, spacer, check);
            detailsBox.getChildren().add(topicBox);
            checkBoxes.add(check);
        }

        // Bind progress bar and percentage label
        DoubleBinding progressValue = Bindings.createDoubleBinding(() -> {
                    long selectedCount = checkBoxes.stream().filter(CheckBox::isSelected).count();
                    return checkBoxes.isEmpty() ? 0 : (double) selectedCount / checkBoxes.size();
                }, checkBoxes.stream().map(CheckBox::selectedProperty).toArray(BooleanProperty[]::new)
        );

        progressBar.progressProperty().bind(progressValue);
        percentage.textProperty().bind(progressValue.multiply(100).asString("%.0f%%"));
    }


    private void updateCheckboxState(ObjectId courseId, String topicText, String userId, boolean isChecked) {
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        ObjectId userObjectId = new ObjectId(userId);

        // Filter by course ID only
        Document filter = new Document("_id", courseId);

        // Define the update operation
        Document update;
        if (isChecked) {
            update = new Document("$addToSet", new Document("topics.$[elem].checked_by", userObjectId));
        } else {
            update = new Document("$pull", new Document("topics.$[elem].checked_by", userObjectId));
        }

        // Correct usage of array filters
        Document options = new Document("arrayFilters", List.of(new Document("elem.text", topicText)));

        // Execute update with options
        coursesCollection.updateOne(filter, update, new com.mongodb.client.model.UpdateOptions().arrayFilters(List.of(new Document("elem.text", topicText))));
    }





    @FXML
    private void toggleDetails() {
        expanded = !expanded;
        detailsBox.setVisible(expanded);
        detailsBox.setManaged(expanded);
    }

    @FXML
    public void showRemoveBubble() {
        removeBubble.setVisible(true);
    }

    @FXML
    public void hideRemoveBubble() {
        removeBubble.setVisible(false);
    }

    public boolean isRemoveBubbleVisible() {
        return removeBubble.isVisible();
    }

    @FXML
    private void removeCourse() {
        if (parentController != null) {
            parentController.removeCourse(CourseId);
        }
    }
}
