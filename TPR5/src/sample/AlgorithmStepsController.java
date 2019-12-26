package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class AlgorithmStepsController {

    @FXML
    private TextArea resultTextArea;

    public void transferMessage(String allStepsInfo){
        resultTextArea.appendText(allStepsInfo);
    }
}
