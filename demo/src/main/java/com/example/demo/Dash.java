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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dash {
    @FXML
    private VBox containerBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane course_container;
    @FXML
    private StackPane popupOverlay;
    @FXML
    private StackPane popupOverlay2;
    @FXML
    private VBox popupContainer;
    @FXML
    private VBox popupContainer2;
    @FXML
    private Label user_name;
    @FXML
    private VBox instructorControls;
    @FXML
    private VBox studentControls;

    private String userId;
    private String userRole; // "Student" or "Instructor"

    private final int numColumns = 2; // Number of columns
    private final List<VBox> columns = new ArrayList<>();

    public void setUser(String id, String role) {
        this.userId = id;
        this.userRole = role;
        addCourseWidgets();
    }

    @FXML
    public void initialize() {
        scrollPane.setCache(false);
        scrollPane.getContent().setCache(false);
        course_container.setCache(false);
        containerBox.setCache(false);
        course_container.setStyle("-fx-snap-to-pixel: true;");
        scrollPane.setStyle("-fx-snap-to-pixel: true;");
        containerBox.setStyle("-fx-snap-to-pixel: true;");
        scrollPane.getContent().snapshot(null, null);
        scrollPane.getContent().setScaleX(1.0);
        scrollPane.getContent().setScaleY(1.0);
        containerBox.setScaleX(1.0);
        containerBox.setScaleY(1.0);
        containerBox.snapshot(null, null);
        course_container.setScaleX(1.0);
        course_container.setScaleY(1.0);
        course_container.snapshot(null,null);


        for (VBox column : columns){
            column.setCache(false);
            column.setStyle("-fx-snap-to-pixel: true;");
            column.setScaleY(1.0);
            column.setScaleX(1.0);
            column.snapshot(null,null);
        }

        for (int i = 0; i < numColumns; i++) {
            VBox column = new VBox();
            column.setSpacing(24); // Space between widgets in the same column
            columns.add(column);
            course_container.getChildren().add(column); // Add VBox to FlowPane
        }
        try {
            // Load popup content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNew.fxml"));
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("dialogue2.fxml"));
            Node popupContent = loader.load();
            Node popupContent2 = loader2.load();
            com.example.demo.AddNew popupController = loader.getController();
            Dialogue2 popupController2 = loader2.getController();
            popupController.setParentController(this);
            popupController2.setParentController(this);
            popupContainer.getChildren().add(popupContent);
            popupContainer2.getChildren().add(popupContent2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCourseWidgets() {
        for (VBox column : columns) {
            column.getChildren().clear();
        }

        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        MongoCollection<Document> users = db.getCollection("users");
        Document user = users.find(Filters.eq("_id",new ObjectId(userId))).first();
        assert user != null;

        List<Document> courses;
        if ("Student".equals(userRole)) {
            user_name.setText(user.getString("username"));
            studentControls.setVisible(true);
            studentControls.setManaged(true);
            courses = coursesCollection.find(Filters.in("students_enrolled", new ObjectId(userId))).into(new java.util.ArrayList<>());
        } else {
            instructorControls.setVisible(true);
            instructorControls.setManaged(true);
            user_name.setText("Instructor " + user.getString("username"));
            courses = coursesCollection.find(Filters.eq("instructor_id", new ObjectId(userId))).into(new java.util.ArrayList<>());
        }
        int columnIndex = 0;

        for (Document course : courses) {
            try {
                FXMLLoader loader;
                if ("Student".equals(userRole)) {
                    loader = new FXMLLoader(getClass().getResource("CourseWidget.fxml"));
                    AnchorPane course_widget = loader.load();
                    CourseWidget controller = loader.getController();
                    course_widget.setUserData(controller);
                    controller.setParentController(this);
                    controller.setDetails(course, userId);
                    columns.get(columnIndex).getChildren().add(course_widget);
                } else {
                    loader = new FXMLLoader(getClass().getResource("CreateNew.fxml"));
                    AnchorPane course_widget = loader.load();
                    CreateNew controller = loader.getController();
                    course_widget.setUserData(controller);
                    controller.setParentController(this, userId);
                    controller.setDetailsI(course,userId);
                    columns.get(columnIndex).getChildren().add(course_widget);
                }
                columnIndex = (columnIndex + 1) % numColumns;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Switch2(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("login.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void showPopup() {
        popupOverlay.setVisible(true);
        popupContainer.setVisible(true);
    }

    public void hidePopup() {
        popupOverlay.setVisible(false);
        popupContainer.setVisible(false);
    }
    @FXML
    public void showPopup2() {
        popupOverlay2.setVisible(true);
        popupContainer2.setVisible(true);
    }

    public void hidePopup2() {
        popupOverlay2.setVisible(false);
        popupContainer2.setVisible(false);
    }

    @FXML
    public void handleRemoveCourse() {
        for (VBox column : columns){
            for (Node node : column.getChildren()) {
                if (node instanceof AnchorPane) {
                    if ("Student".equals(userRole)) {
                        CourseWidget controller = (CourseWidget) node.getUserData();
                        if (controller != null) {
                            if (controller.isRemoveBubbleVisible()) {
                                controller.hideRemoveBubble();
                            } else {
                                controller.showRemoveBubble();
                            }
                        }
                    }else {
                        CreateNew controller = (CreateNew) node.getUserData();
                        if (controller != null) {
                            if (controller.isRemoveBubbleVisible()) {
                                controller.hideRemoveBubble();
                            } else {
                                controller.showRemoveBubble();
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeCourse(String CourseId) {
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        coursesCollection.updateOne(
                Filters.eq("_id", new ObjectId(CourseId)),  // Find the course
                new Document("$pull", new Document("students_enrolled", new ObjectId(userId)))
        );
        addCourseWidgets();
    }
    public void DeleteCourse(String CourseId){
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> coursesCollection = db.getCollection("courses");
        coursesCollection.deleteOne(Filters.eq("_id", new ObjectId(CourseId)));
        addCourseWidgets();

    }
    public void createCourse(){
        MongoDatabase db = MongoDBConnection.connect();
        MongoCollection<Document> courses = db.getCollection("courses");
        List<Document> topics = new ArrayList<>();
        Document course = new Document("name", "Course")
                .append("instructor_id", new ObjectId(userId))
                .append("students_enrolled", new ArrayList<>())
                .append("topics", topics);
        topics.add(new Document("text", "new Topic").append("checked_by", new ArrayList<>()));
        courses.insertOne(course);
        addCourseWidgets();
    }
    public String getUserId() {
        return userId;
    }

}
