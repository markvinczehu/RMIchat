package client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class View extends Application {
    private ListView<String> userList;
    private TextArea window;
    private TextField input;
    private ChatClient client;

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false);
        this.Dialog();
        this.setStage(stage);
    }

    private void Dialog() {
        int i = 1;
        while (i++ <= 3) {
            TextInputDialog dialog = this.createTextInputDialog();
            Optional<String> result = dialog.showAndWait();
            String errorMessage = "";
            if (!result.isPresent()) {
                this.closeThis();
            }
            if (result.get().length() > 0 && result.get().length() < 20) {
                this.initClient();
                if (this.isNameOk(result.get()))
                    return;
                else
                    errorMessage = "Nickname already assigned. Please select another.";
            } else {
                errorMessage = "Nickname must be between 1 and 20 characters long.";
            }
            this.connectionError(errorMessage + " Attempt: " + (i - 1) + " from " + 3);
        }
        this.closeThis();
    }

    private boolean isNameOk(String s) {
        boolean b = false;
        try {
            b = this.client.isNameOk(s);
        } catch (RemoteException e) {
            this.connectionError("There was a problem with the nickname");
        }
        return b;
    }

    private TextInputDialog createTextInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Text Input Dialog");
        dialog.setHeaderText("");
        dialog.setContentText("Chatname:");
        dialog.initModality(Modality.APPLICATION_MODAL);
        return dialog;
    }

    private void connectionError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Warning message");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initClient() {
        if (this.client == null)
            try {
                this.client = new ChatClient(this);
            } catch (RemoteException e) {
                this.connectionError("RemoteException. Server not available?");
                this.closeThis();
            } catch (MalformedURLException | NotBoundException e) {
                this.connectionError("Binding error");
                this.closeThis();
            }
    }

    private TextField getTextField() {
        TextField input = new TextField();
        input.setOnAction(e -> {
            client.sendMessage(input.getText());
            input.setText("");
        });
        return input;
    }

    private ListView<String> getListView() {
        ListView<String> temp = new ListView<String>();
        temp.setMinWidth(200.0);
        return temp;
    }

    private void setStage(Stage stage) {
        this.input = this.getTextField();
        this.userList = this.getListView();
        this.window = this.getTextArea();
        Scene scene = new Scene(this.getRoot(), 600, 450);
        stage.setOnCloseRequest(e -> {
            this.client.logout();
            this.closeThis();
        });
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Chat");
        stage.show();
    }

    private BorderPane getRoot() {
        BorderPane root = new BorderPane();
        root.setBottom(this.input);
        root.setLeft(this.window);
        root.setRight(this.userList);
        return root;
    }

    private TextArea getTextArea() {
        TextArea window = new TextArea();
        window.prefHeight(300.0);
        window.setStyle("-fx-text-fill: black; -fx-font-size: 12;");
        window.prefWidth(400.0);
        window.setEditable(false);

        return window;
    }

    private void closeThis() {
        Platform.exit();
        System.exit(0);
    }

    public void setMessage(String message) {
        Platform.runLater(() -> View.this.window.appendText("\n" + message));
    }

    public void newUserList(String[] userList2) {
        Platform.runLater(() -> {
            this.userList.setItems(null);
            ObservableList<String> list = FXCollections.observableArrayList(userList2);
            FXCollections.sort(list);
            this.userList.setItems(list);
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
