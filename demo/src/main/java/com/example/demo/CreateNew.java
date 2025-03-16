package com.example.demo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class CreateNew extends CourseWidget {
    @FXML
    private VBox detailsBox;

    @FXML
    private StackPane removeBubble;


    @FXML
    public AnchorPane rootPane;
    private Dash parentController;

    @FXML
    private Label courseNameLabel;
    @FXML
    private Button saveButton;
    @FXML
    private TextField courseNameField;
    @FXML
    private Label studentNum;


    private String instructorId;
    private ArrayList<String> originalTopics = new ArrayList<>();
    private ArrayList<Label> Topics = new ArrayList<>();


    public void setParentController(Dash parent, String instructorId) {
        this.parentController = parent;
        this.instructorId = instructorId;
    }


    @FXML
    public void initialize() {
        courseNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                saveCourseName();
            }
        });
        for (var node : detailsBox.getChildren()) {
            if (node instanceof Label label) {
                originalTopics.add(label.getText().trim());
            }
        }
    }


    @FXML
    private void editCourseName() {
        editoldlabel();
        courseNameField.setText(courseNameLabel.getText());
        courseNameLabel.setVisible(false);
        courseNameLabel.setManaged(false);

        courseNameField.setVisible(true);
        courseNameField.setManaged(true);
        courseNameField.requestFocus();
        courseNameField.selectAll();
        saveButton.setDisable(false);
    }

    @FXML
    private void saveCourseName() {
        String newName = courseNameField.getText().trim();
        if (!newName.isEmpty()) {
            courseNameLabel.setText(newName);
        }

        courseNameField.setVisible(false);
        courseNameField.setManaged(false);

        courseNameLabel.setVisible(true);
        courseNameLabel.setManaged(true);
        checkForChanges();

    }


    public void setParentController(Dash parent) {
        this.parentController = parent;
    }

    @FXML
    private void toggleDetails(MouseEvent event) {
        if (detailsBox.getChildren().stream().anyMatch(node -> node instanceof TextField)) {
            return;
        }

        Object source = event.getTarget();
        if (source instanceof TextField || (detailsBox.isVisible() && detailsBox.getBoundsInParent().contains(event.getX(), event.getY()))) {
            return;
        }

        boolean isVisible = detailsBox.isVisible();
        detailsBox.setVisible(!isVisible);
        detailsBox.setManaged(!isVisible);
    }

    @FXML
    private void addNewLabel() {
        detailsBox.getChildren().removeIf(node -> node instanceof TextField); // Remove active text fields

        Label newLabel = new Label("New Topic");
        newLabel.setStyle("-fx-font-size: 18px; -fx-font-family: 'Work Sans Medium';");
        newLabel.setOnMouseClicked(event -> makeLabelEditable(newLabel));

        int buttonIndex = detailsBox.getChildren().size() - 2;
        detailsBox.getChildren().add(buttonIndex, newLabel);
        saveButton.setDisable(false);

    }


    private void makeLabelEditable(Label label) {
        TextField textField = new TextField(label.getText());
        textField.setStyle("-fx-font-size: 18px; -fx-font-family: 'Work Sans Medium';");

        int index = detailsBox.getChildren().indexOf(label);
        detailsBox.getChildren().set(index, textField);

        textField.setOnMouseClicked(Event::consume);

        textField.setOnAction(event -> {
            saveOrRemoveLabel(textField, label, index);
            checkForChanges();
        });

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                detailsBox.requestFocus(); // Ensures it loses focus properly
                saveOrRemoveLabel(textField, label, index);
                checkForChanges();
            }
        });

        detailsBox.getScene().setOnMousePressed(event -> {
            if (!textField.getBoundsInParent().contains(event.getX(), event.getY())) {
                saveOrRemoveLabel(textField, label, index);
            }
        });

        textField.requestFocus();
        textField.selectAll();
    }

    private void saveOrRemoveLabel(TextField textField, Label label, int index) {
        String newText = textField.getText().trim();
        if (newText.isEmpty()) {
            if (index >= 0 && index < detailsBox.getChildren().size()) {
                detailsBox.getChildren().remove(textField);
            }
        } else {
            label.setText(newText);
            detailsBox.getChildren().set(index, label);
        }
    }

    @FXML
    private void saveCourseToDB() {
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> courses = db.getCollection("courses");

        String newName = courseNameLabel.getText().trim();
        if (newName.isEmpty()) return;

        List<Document> topics = new ArrayList<>();
        for (var node : detailsBox.getChildren()) {
            if (node instanceof Label label) {
                topics.add(new Document("text", label.getText()).append("checked_by", new ArrayList<>()));
            }
        }

        Document existingCourse = courses.find(new Document("_id", new ObjectId(CourseId))).first();

        if (existingCourse != null) {
            courses.updateOne(
                    new Document("_id", existingCourse.getObjectId("_id")),
                    new Document("$set", new Document("name", newName).append("topics", topics))
            );
        } else {
            Document course = new Document("name", newName)
                    .append("instructor_id", new ObjectId(instructorId))
                    .append("students_enrolled", new ArrayList<>())
                    .append("topics", topics);
            courses.insertOne(course);
        }
    }


    private void checkForChanges() {
        String currentName = courseNameLabel.getText().trim();

        boolean nameChanged = !currentName.equals(courseNameField.getText().trim());

        ArrayList<String> currentTopics = new ArrayList<>();
        for (var node : detailsBox.getChildren()) {
            if (node instanceof Label label) {
                currentTopics.add(label.getText().trim());
            }
        }

        boolean topicsChanged = !currentTopics.equals(originalTopics);
        saveButton.setDisable(!nameChanged && !topicsChanged);

        if (!saveButton.isDisabled()) {
            saveTopics();
        }
    }


    private void saveTopics() {
        originalTopics.clear();
        for (var node : detailsBox.getChildren()) {
            if (node instanceof Label label) {
                originalTopics.add(label.getText().trim());
            }
        }
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

    public void setDetailsI(Document course, String userId) {
        this.CourseId = course.getObjectId("_id").toHexString();
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> users = db.getCollection("users");


        // Set course name
        courseNameLabel.setText(course.getString("name"));
        studentNum.setText("Students Enrolled: " + (String.valueOf(course.getList("students_enrolled", ObjectId.class).size())));


        List<Document> topics = (List<Document>) course.get("topics");

        for (Document topic : topics) {
            Label text = new Label(topic.getString("text"));
            text.setStyle("-fx-font-size: 18px; -fx-font-family: 'Work Sans Medium';");

            detailsBox.getChildren().add((detailsBox.getChildren().size() - 2), text);
            Topics.add(text);

        }
    }

    private void editoldlabel() {
        for (Label label : Topics) {
            label.setOnMouseClicked(event -> makeLabelEditable(label));
        }
    }
    @FXML
    private void DeleteCourse() {
        if (parentController != null) {
            parentController.DeleteCourse(CourseId);
        }
    }
}
