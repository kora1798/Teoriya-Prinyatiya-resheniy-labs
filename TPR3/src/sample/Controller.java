package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {

    @FXML
    private Label labelOfCond;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private TextField firstStrOfFirstPlayer, firstStrOfSecondPlayer, quanOfStrFirstPlayer,
            quanOfStrSecondPlayer, P, Q, valueOfGame, textOfCond;

    @FXML
    private Button generateButton, countButton;

    @FXML
    private BorderPane borderPane;

    @FXML
    private TableView<double[]> strategyTable, resultTable;

    private double[][] strategyMatrix;

    private double[][] resultMatrix;

    private final static ObservableList<String> typesOfCondition =
            FXCollections.observableArrayList("Кол-во итераций", "Точность");

    @FXML
    void initialize() {
        initializeMenuBar();
        initializeChoiceBox();
        initializeStrategyTable();
        initializeResultTable();
        generateButton.setOnAction(actionEvent -> {
            if (!quanOfStrFirstPlayer.getText().isEmpty() && !quanOfStrSecondPlayer.getText().isEmpty()) {
                int quanOfStrFP = Integer.parseInt(quanOfStrFirstPlayer.getText()),
                        quanOfStrSP = Integer.parseInt(quanOfStrSecondPlayer.getText());
                strategyMatrix = new double[quanOfStrFP][quanOfStrSP];
                strategyTable.getColumns().clear();
                customizeStrategyTable();
                fillStrategyTable();
            } else
                (new Alert(Alert.AlertType.ERROR, "Поля должны быть заполнены!")).show();
        });
        countButton.setOnAction(actionEvent -> {
            if (!checkForValueOfGame()) {
                if (!textOfCond.getText().isEmpty() && choiceBox.getValue().equals("Кол-во итераций")) {
                    int quanOfIters = Integer.parseInt(textOfCond.getText());
                    count(quanOfIters);
                } else {
                    if (!textOfCond.getText().isEmpty() && choiceBox.getValue().equals("Точность")) {
                        double precision = Double.parseDouble(textOfCond.getText());
                        count2(precision);
                    } else {
                        (new Alert(Alert.AlertType.ERROR, "Заполните поле условия")).show();
                    }
                }
            }
        });
    }
    private void initializeMenuBar(){
        MenuBar menuBar = new MenuBar();
        borderPane.setTop(menuBar);
        Menu fileMenu = new Menu("Файл");
        MenuItem newItem = new MenuItem("Создать");
        newItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                clear();
            }
        });
        MenuItem saveItem = new MenuItem("Сохранить");
        saveItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                saveFile();
            }
        });
        MenuItem openItem = new MenuItem("Открыть...");
        openItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                openFile();
            }
        });
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem exitItem = new MenuItem("Выход");
        fileMenu.getItems().addAll(newItem, saveItem, openItem, separatorMenuItem, exitItem);
        menuBar.getMenus().addAll(fileMenu);
    }
    private void initializeChoiceBox() {
        choiceBox.setItems(typesOfCondition);
        choiceBox.setValue(typesOfCondition.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                labelOfCond.setText(typesOfCondition.get(t1.intValue()) + ":");
            }
        });
    }
    private void initializeStrategyTable() {
        strategyTable.setEditable(true);
        strategyTable.getColumns().clear();
    }
    private void customizeStrategyTable() {
        TableColumn<double[], String> numOfStrategies = new TableColumn<>();
        numOfStrategies.setCellFactory(new Callback<>() {
            @Override
            public TableCell<double[], String> call(TableColumn<double[], String> stringTableColumn) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (this.getTableRow() != null && !empty) {
                            setText(String.valueOf(this.getTableRow().getIndex() + 1));
                        }
                    }
                };
            }
        });
        strategyTable.getColumns().add(numOfStrategies);
        for (int i = 0; i < strategyMatrix[0].length; i++) {
            final int columnIndex = i;
            TableColumn<double[], String> column = new TableColumn<>(String.valueOf(i + 1));
            column.setMinWidth(60);
            column.setSortable(false);
            column.setCellValueFactory(stringCellDataFeatures -> {
                double[] row = stringCellDataFeatures.getValue();
                return new SimpleStringProperty(String.valueOf(row[columnIndex]));
            });
            column.setOnEditCommit(stringCellEditEvent -> {
                strategyMatrix[stringCellEditEvent.getTablePosition().getRow()][stringCellEditEvent.getTablePosition().getColumn() - 1] =
                        Double.parseDouble(stringCellEditEvent.getNewValue());
            });
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            strategyTable.getColumns().add(column);
        }
    }
    private void fillStrategyTable() {
        for (int i = 0; i < strategyMatrix.length; i++)
            strategyTable.getItems().add(strategyMatrix[i]);
    }
    private void initializeResultTable() {
        resultTable.getColumns().clear();
    }
    private void customizeResultTable() {
        // k i B1 ... Bn j A1 ... An Vmin Vmax Vmid => size = 8
        final int quanOfHeaders = 8;
        int tempPosition = 0;
        Map<Integer, String> map = Stream.of(new AbstractMap.SimpleEntry<>(0, "k"),
                new AbstractMap.SimpleEntry<>(1, "i"),
                new AbstractMap.SimpleEntry<>(3, "j"),
                new AbstractMap.SimpleEntry<>(5, "Vmin"),
                new AbstractMap.SimpleEntry<>(6, "Vmax"),
                new AbstractMap.SimpleEntry<>(7, "Vmid")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        for (int i = 0; i < quanOfHeaders; i++) {
            if (i == 2 || i == 4) {
                if (i == 2) {
                    for (int j = 0; j < strategyMatrix.length; j++) {
                        final int index = tempPosition;
                        TableColumn<double[], String> columnOfStrOfFP = new TableColumn<>("A" + j);
                        columnOfStrOfFP.setCellValueFactory(stringCellDataFeatures -> {
                            double[] row = stringCellDataFeatures.getValue();
                            return new SimpleStringProperty(String.valueOf(row[index]));
                        });
                        tempPosition++;
                        resultTable.getColumns().add(columnOfStrOfFP);
                    }
                }
                if (i == 4) {
                    for (int j = 0; j < strategyMatrix.length; j++) {
                        final int index = tempPosition;
                        TableColumn<double[], String> columnOfStrOfSP = new TableColumn<>("B" + j);
                        columnOfStrOfSP.setCellValueFactory(stringCellDataFeatures -> {
                            double[] row = stringCellDataFeatures.getValue();
                            return new SimpleStringProperty(String.valueOf(row[index]));
                        });
                        tempPosition++;
                        resultTable.getColumns().add(columnOfStrOfSP);
                    }
                }
            } else {
                final int index = tempPosition;
                final int tempi = i;
                TableColumn<double[], String> column = new TableColumn<>(map.get(i));
                column.setCellValueFactory(stringCellDataFeatures -> {
                    double[] row = stringCellDataFeatures.getValue();
                    if (tempi == 0 || tempi == 1 || tempi == 3)
                        return new SimpleStringProperty(String.valueOf((int) (row[index])));
                    else
                        return new SimpleStringProperty(String.valueOf(df.format(row[index])));
                });
                tempPosition++;
                resultTable.getColumns().add(column);
            }
        }
    }
    private boolean checkForValueOfGame(){
        int i , j;
        double max, valueOfGame1, valueOfGame2;
        double[] mas1 = new double[strategyMatrix[0].length], mas2 = new double[strategyMatrix.length];
        for (i = 0; i < strategyMatrix.length; i++){
            mas1[i] = Arrays.stream(strategyMatrix[i]).min().orElse(0);
        }
        for (i = 0; i < mas1.length;i++){
            System.out.println(mas1[i] + " ");
        }
        valueOfGame1 = Arrays.stream(mas1).max().orElse(0);
        System.out.println(valueOfGame1);
        for(i =0; i < strategyMatrix[0].length;i++){
            max = -999;
            for ( j = 0; j < strategyMatrix.length; j++){
                if(strategyMatrix[j][i] > max){
                    max = strategyMatrix[j][i];
                }
            }
            mas2[i] = max;
        }
        for (i = 0; i < mas2.length;i++){
            System.out.println(mas2[i] + " ");
        }
        valueOfGame2 = Arrays.stream(mas2).min().orElse(0);
        System.out.println(valueOfGame2);
        if(valueOfGame1 == valueOfGame2){
            (new Alert(Alert.AlertType.INFORMATION, "Цена игры равна: " + valueOfGame1)).show();
            valueOfGame.setText(String.valueOf(valueOfGame1));
            return true;
        }
        return false;
    }
    private void count(int quanOfIters) {
        int size = fillResultMatrixAndInfo(quanOfIters);
        customizeResultTable();
        fillResultTable(size);
    }
    private void count2(double precision){
        int size = fillResultMatrixAndInfo(precision);
        customizeResultTable();
        fillResultTable(size);
    }
    private int fillResultMatrixAndInfo(double precision){
        int i, j, tempIndex = 0, VminIndex, VmaxIndex,
                quanOfStrFP = strategyMatrix.length, quanOfStrSP = strategyMatrix[0].length,
                nextStrategyOfFP = Integer.parseInt(firstStrOfFirstPlayer.getText()),
                nextStrategyOfSP = Integer.parseInt(firstStrOfSecondPlayer.getText());
        double Vmin, Vmax;
        Map<Integer, Integer> mapP = new HashMap<>();
        Map<Integer, Integer> mapQ = new HashMap<>();
        for (i = 0; i < quanOfStrFP;i++)
            mapP.put(i,0);
        for (i = 0; i < quanOfStrSP; i++)
            mapQ.put(i,0);
        // k i B1 ... Bn j A1 ... An Vmin Vmax Vmid
        resultMatrix = new double[10000][6 + strategyMatrix.length + strategyMatrix[0].length];
        i = 0;
        do{
            tempIndex = 0;
            VminIndex = -1;
            VmaxIndex = -1;
            Vmin = 999;
            Vmax = -999;
            resultMatrix[i][tempIndex++] = i + 1;
            resultMatrix[i][tempIndex++] = nextStrategyOfFP;
            mapP.put(nextStrategyOfFP, mapP.get(nextStrategyOfFP)+ 1);
            for (j = 0; j < quanOfStrFP; j++) {
                if (i > 0)
                    resultMatrix[i][tempIndex + j] = strategyMatrix[nextStrategyOfFP][j] + resultMatrix[i-1][tempIndex+j];
                else
                    resultMatrix[i][tempIndex + j] = strategyMatrix[nextStrategyOfFP][j];
                if (resultMatrix[i][tempIndex + j]  < Vmin) {
                    Vmin = resultMatrix[i][tempIndex + j] ;
                    VminIndex = j;
                }
            }
            tempIndex+=j;
            resultMatrix[i][tempIndex++] = nextStrategyOfSP;
            mapQ.put(nextStrategyOfFP, mapP.get(nextStrategyOfFP)+ 1);
            for (j = 0; j < quanOfStrSP; j++) {
                if (i > 0)
                    resultMatrix[i][tempIndex + j] = strategyMatrix[j][nextStrategyOfSP] + resultMatrix[i-1][tempIndex+j];
                else
                    resultMatrix[i][tempIndex + j] = strategyMatrix[j][nextStrategyOfSP];
                if (resultMatrix[i][tempIndex + j] > Vmax) {
                    Vmax = resultMatrix[i][tempIndex + j];
                    VmaxIndex = j;
                }
            }
            tempIndex+=j;
            nextStrategyOfFP = VmaxIndex;
            nextStrategyOfSP = VminIndex;
            resultMatrix[i][tempIndex++] = Vmin / (i + 1);
            resultMatrix[i][tempIndex++] = Vmax / (i + 1);
            resultMatrix[i][tempIndex] = ((Vmin + Vmax) / 2) / (i + 1);
            i++;
        }while(resultMatrix[i - 1][tempIndex-1] - resultMatrix[i - 1][tempIndex-2] > precision);

        Integer sumP = (mapP.values()).stream().reduce(Integer::sum).orElse(0);
        P.setText(setTextFieldsOfInfo(quanOfStrFP, mapP, sumP));

        Integer sumQ = (mapQ.values()).stream().reduce(Integer::sum).orElse(0);
        Q.setText(setTextFieldsOfInfo(quanOfStrSP, mapQ, sumQ));

        valueOfGame.setText(String.valueOf(resultMatrix[i-1][tempIndex]));
        return i;
    }
    private int fillResultMatrixAndInfo(int quanOfIters) {
        int i, j, tempIndex = 0, VminIndex, VmaxIndex,
                quanOfStrFP = strategyMatrix.length, quanOfStrSP = strategyMatrix[0].length,
                nextStrategyOfFP = Integer.parseInt(firstStrOfFirstPlayer.getText()),
                nextStrategyOfSP = Integer.parseInt(firstStrOfSecondPlayer.getText());
        double Vmin, Vmax;
        Map<Integer, Integer> mapP = new HashMap<>();
        Map<Integer, Integer> mapQ = new HashMap<>();
        for (i = 0; i < quanOfStrFP;i++)
            mapP.put(i,0);
        for (i = 0; i < quanOfStrSP; i++)
            mapQ.put(i,0);
        // k i B1 ... Bn j A1 ... An Vmin Vmax Vmid
        resultMatrix = new double[quanOfIters][6 + strategyMatrix.length + strategyMatrix[0].length];
        for (i = 0; i < quanOfIters; i++) {
            tempIndex = 0;
            VminIndex = -1;
            VmaxIndex = -1;
            Vmin = 999;
            Vmax = -999;
            resultMatrix[i][tempIndex++] = i + 1;
            resultMatrix[i][tempIndex++] = nextStrategyOfFP;
            mapP.put(nextStrategyOfFP, mapP.get(nextStrategyOfFP)+ 1);
            for (j = 0; j < quanOfStrFP; j++) {
                if (i > 0)
                    resultMatrix[i][tempIndex + j] = strategyMatrix[nextStrategyOfFP][j] + resultMatrix[i-1][tempIndex+j];
                else
                    resultMatrix[i][tempIndex + j] = strategyMatrix[nextStrategyOfFP][j];
                if (resultMatrix[i][tempIndex + j]  < Vmin) {
                    Vmin = resultMatrix[i][tempIndex + j] ;
                    VminIndex = j;
                }
            }
            tempIndex+=j;
            resultMatrix[i][tempIndex++] = nextStrategyOfSP;
            mapQ.put(nextStrategyOfFP, mapP.get(nextStrategyOfFP)+ 1);
            for (j = 0; j < quanOfStrSP; j++) {
                if (i > 0)
                    resultMatrix[i][tempIndex + j] = strategyMatrix[j][nextStrategyOfSP] + resultMatrix[i-1][tempIndex+j];
                else
                    resultMatrix[i][tempIndex + j] = strategyMatrix[j][nextStrategyOfSP];
                if (resultMatrix[i][tempIndex + j] > Vmax) {
                    Vmax = resultMatrix[i][tempIndex + j];
                    VmaxIndex = j;
                }
            }
            tempIndex+=j;
            nextStrategyOfFP = VmaxIndex;
            nextStrategyOfSP = VminIndex;
            resultMatrix[i][tempIndex++] = Vmin / (i + 1);
            resultMatrix[i][tempIndex++] = Vmax / (i + 1);
            resultMatrix[i][tempIndex] = ((Vmin + Vmax) / 2) / (i + 1);
        }

        Integer sumP = (mapP.values()).stream().reduce(Integer::sum).orElse(0);
        P.setText(setTextFieldsOfInfo(quanOfStrFP, mapP, sumP));

        Integer sumQ = (mapQ.values()).stream().reduce(Integer::sum).orElse(0);
        Q.setText(setTextFieldsOfInfo(quanOfStrSP, mapQ, sumQ));

        valueOfGame.setText(String.valueOf(resultMatrix[quanOfIters-1][tempIndex]));
        return resultMatrix.length;
    }
    private String setTextFieldsOfInfo(int quanOfStrSP, Map<Integer, Integer> mapQ, Integer sumQ) {
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder stringBuilder = new StringBuilder();
        int i;
        for (i = 0; i < quanOfStrSP; i++)
            stringBuilder.append(df.format((double)(mapQ.get(i))/sumQ) + "; ");
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(";"));
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(" "));
        stringBuilder.append(")");
        stringBuilder.insert(0,"(");
        return stringBuilder.toString();
    }
    private void fillResultTable(int size) {
        for (int i = 0; i < size; i++)
            resultTable.getItems().add(resultMatrix[i]);
    }
    private void clear(){
        resultMatrix = null;
        strategyMatrix = null;
        resultTable.getItems().clear();
        resultTable.getColumns().clear();
        strategyTable.getColumns().clear();
        strategyTable.getItems().clear();
        textOfCond.clear();
        firstStrOfSecondPlayer.clear();
        firstStrOfSecondPlayer.clear();
        P.clear();
        Q.clear();
        valueOfGame.clear();
    }
    private void saveFile(){
        Stage primaryStage = new Main().getPrimaryStage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл: ");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_3лаба"));
        File file = fileChooser.showSaveDialog(primaryStage);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_3лаба/" + file.getName()))) {
            FileUnit fileUnit = new FileUnit(strategyMatrix, resultMatrix, P.getText(), Q.getText(), valueOfGame.getText(), choiceBox.getValue(), textOfCond.getText());
            oos.writeObject(fileUnit);
        } catch (Exception e) {
            System.out.println("excSave");
            e.printStackTrace();
        }
    }
    private void openFile(){
        Stage primaryStage = new Main().getPrimaryStage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с матрицей вероятностей");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_3лаба"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_3лаба/" + file.getName()));
                FileUnit fileUnit = (FileUnit) ois.readObject();
                openFileUnit(fileUnit);
            } catch (IOException e) {
                System.out.println("excDown");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {

            }
        }
    }
    private void openFileUnit(FileUnit fileUnit) {
        clear();
        strategyMatrix = fileUnit.getStrategyMatrix();
        resultMatrix = fileUnit.getResultMatrix();
        String Pstr = fileUnit.getP();
        String Qstr = fileUnit.getQ();
        String valueOfGameStr = fileUnit.getValueOfGame();
        String choiceBoxValueStr = fileUnit.getChoiceBoxValue();
        String textCondStr = fileUnit.getTextCond();
        customizeStrategyTable();
        fillStrategyTable();
        customizeResultTable();
        fillResultTable(resultMatrix.length);
        P.setText(Pstr);
        Q.setText(Qstr);
        valueOfGame.setText(valueOfGameStr);
        choiceBox.setValue(choiceBoxValueStr);
        textOfCond.setText(textCondStr);
    }
}
