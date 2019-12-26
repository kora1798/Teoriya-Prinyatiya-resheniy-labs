package sample;
import java.io.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;

public class MainFormController {


    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField quantityOfAlternativesText;

    @FXML
    private TextField quantityOfCriteriaText;

    @FXML
    private Button generateBtn;

    @FXML
    private Button countBtn;

    @FXML
    private Button showStepsBtn;

    @FXML
    private TableView<String[]> namesOfAlternativesTable;

    @FXML
    private TableView<String[]> namesOfCriteriaTable;

    @FXML
    private TableView<CriteriaCustomData> criteriaCustomTable;

    @FXML
    private TableView<double[]> valuesTable;

    @FXML
    private TextArea resultTextArea;

    private ObservableList<String> namesOfAlternativesList, namesOfCriteriaList;

    private CriteriaCustomData[] criteriaCustomDataArray;

    private ArrayList<String> resultList;

    private double[][] valuesMatrix;

    private final String SIMPLE_FUNC = "Обычная функция";
    private final String U_SHAPE_FUNC = "U-образная функция";
    private final String V_SHAPE_FUNC = "V-образная функция";
    private final String LEVEL_FUNC = "Уровень функция";
    private final String V_SHAPE_FUNC_WITH_THRESHOLDS = "V-образная функция с порогами б-ия";
    private final String GAUSS_FUNC = "Функция Гаусса";
    private String allStepsInfo;
    StringBuilder allStepsInformation = new StringBuilder("");
    boolean isDataInitialized = false;

    @FXML
    void initialize() {
        deleteTablesStructure();
        initializeMenuBar();
        setStateOfAppDependOnData(false);
        generateBtn.setOnAction(click -> {
            if (!quantityOfAlternativesText.getText().isEmpty() && !quantityOfCriteriaText.getText().isEmpty()) {
                int quantityOfAlternatives = Integer.parseInt(this.quantityOfAlternativesText.getText()),
                        quantityOfCriteria = Integer.parseInt(this.quantityOfCriteriaText.getText());
                initializeListsOfNames(quantityOfAlternatives,quantityOfCriteria);
                initializeCriteriaCustomArrayList(quantityOfCriteria);
                initializeValuesMatrix(quantityOfAlternatives,quantityOfCriteria);
                if (isDataInitialized){
                    valuesTable.getColumns().clear();
                    valuesTable.getItems().clear();
                    buildValuesTableStructure();
                }
                buildNamesTablesStructure(namesOfAlternativesTable, namesOfAlternativesList);
                buildNamesTablesStructure(namesOfCriteriaTable, namesOfCriteriaList);
                buildCriteriaCustomTableStructure();
                buildValuesTableStructure();
                addListenersToListsOfNames();
                clearInformationInTables();
                fillNamesTable(namesOfAlternativesTable, namesOfAlternativesList);
                fillNamesTable(namesOfCriteriaTable, namesOfCriteriaList);
                fillCriteriaCustomTable();
                fillValuesTable();
                setStateOfAppDependOnData(true);
                isDataInitialized = true;
            }
        });
        showStepsBtn.setOnAction(click -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("algorithm_steps_form.fxml"));
                Parent root = loader.load();
                AlgorithmStepsController algorithmStepsController = loader.getController();
                algorithmStepsController.transferMessage(allStepsInfo);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Шаги алгоритма");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        countBtn.setOnAction(click -> {
            if (areWeightsSumEqualToOne()){
                resultTextArea.setText("");
                processAlg();
                allStepsInfo = allStepsInformation.toString();
                fillResult();
            }else
                (new Alert(Alert.AlertType.ERROR, "Сумма значений ячеек стобца \"Вес\" в таблице\n\"Настройка критериев\" должна равняться единице!")).show();
        });
    }

    private void initializeListsOfNames(int quantityOfAlternatives, int quantityOfCriteria) {
        namesOfAlternativesList = FXCollections.observableList(new ArrayList<>(Collections.nCopies(quantityOfAlternatives, "-")));
        namesOfCriteriaList = FXCollections.observableList(new ArrayList<>(Collections.nCopies(quantityOfCriteria, "-")));
    }

    private void addListenersToListsOfNames() {
        namesOfCriteriaList.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                criteriaCustomDataArray[change.getFrom()].setName(change.getAddedSubList().get(0));
                System.out.println();
                valuesTable.getColumns().get(change.getFrom() + 1).setText(change.getAddedSubList().get(0));
            }
            criteriaCustomTable.refresh();
            valuesTable.refresh();
        });
        namesOfAlternativesList.addListener((ListChangeListener<String>) change -> {
            valuesTable.refresh();
        });
    }

    private void initializeCriteriaCustomArrayList(int quantityOfCriteria) {
        criteriaCustomDataArray = new CriteriaCustomData[quantityOfCriteria];
        for (int i = 0; i < quantityOfCriteria; i++)
            criteriaCustomDataArray[i] = new CriteriaCustomData("-", SIMPLE_FUNC, 0.0, false, "none");
    }

    private void initializeValuesMatrix(int quantityOfAlternatives, int quantityOfCriteria) {
        valuesMatrix = new double[quantityOfAlternatives][quantityOfCriteria];
    }

    private void buildNamesTablesStructure(TableView<String[]> namesTable, ObservableList<String> namesList) {
        namesTable.setEditable(true);
        TableColumn<String[], String> rowNumberColumn = new TableColumn<>("Номер");
        rowNumberColumn.prefWidthProperty().bind(namesTable.widthProperty().divide(5));
        rowNumberColumn.setCellValueFactory(cellData -> {
            String[] row = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(row[0]));
        });
        namesTable.getColumns().add(rowNumberColumn);
        TableColumn<String[], String> nameColumn = new TableColumn<>("Название");
        nameColumn.prefWidthProperty().bind(namesTable.widthProperty().divide(1.25));
        nameColumn.setCellValueFactory(cellData -> {
            String[] row = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(row[1]));
        });
        nameColumn.setOnEditCommit(event -> {
            namesList.set(event.getTablePosition().getRow(), event.getNewValue());
        });
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        namesTable.getColumns().add(nameColumn);
    }

    private void buildCriteriaCustomTableStructure() {
        int divideWidthTo = 5;
        criteriaCustomTable.setEditable(true);
        TableColumn<CriteriaCustomData, String> nameOfCriteriaColumn = new TableColumn("Название");
        nameOfCriteriaColumn.prefWidthProperty().bind(criteriaCustomTable.widthProperty().divide(divideWidthTo));
        nameOfCriteriaColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        criteriaCustomTable.getColumns().add(nameOfCriteriaColumn);
        TableColumn<CriteriaCustomData, Double> weightColumn = new TableColumn<>("Вес");
        weightColumn.prefWidthProperty().bind(criteriaCustomTable.widthProperty().divide(divideWidthTo));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        weightColumn.setOnEditCommit(cellData -> criteriaCustomDataArray[cellData.getTablePosition().getRow()].setWeight(cellData.getNewValue()));
        criteriaCustomTable.getColumns().add(weightColumn);
        TableColumn<CriteriaCustomData, Boolean> negativeDirectionColumn = new TableColumn<>("Отриц. напр.");
        negativeDirectionColumn.prefWidthProperty().bind(criteriaCustomTable.widthProperty().divide(divideWidthTo));
        negativeDirectionColumn.setCellValueFactory(cellData-> {
            CriteriaCustomData cellValue = cellData.getValue();
            BooleanProperty property = cellValue.getNegativeDirection();
            property.addListener((observer,oldValue,newValue)->{
                cellValue.setNegativeDirection(newValue);});
            return property;
        });
        negativeDirectionColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        negativeDirectionColumn.setOnEditCommit(cellData->{
            System.out.println("hello");
            criteriaCustomDataArray[cellData.getTablePosition().getRow()].setNegativeDirection(cellData.getNewValue());
            System.out.println(cellData.getNewValue() + " new");
            System.out.println(cellData.getOldValue() + "old");
        });
        criteriaCustomTable.getColumns().add(negativeDirectionColumn);
        TableColumn<CriteriaCustomData, String> functionTypeColumn = new TableColumn<>("Функция");
        functionTypeColumn.prefWidthProperty().bind(criteriaCustomTable.widthProperty().divide(divideWidthTo));
        functionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("functionType"));
        functionTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new String[]{SIMPLE_FUNC, U_SHAPE_FUNC,V_SHAPE_FUNC, LEVEL_FUNC, V_SHAPE_FUNC_WITH_THRESHOLDS, GAUSS_FUNC}));
        functionTypeColumn.setOnEditCommit(cellData->{
            criteriaCustomDataArray[cellData.getTablePosition().getRow()].setFunctionType(cellData.getNewValue());
            CriteriaCustomData cellValue = cellData.getRowValue();
            switch (cellData.getNewValue()){
                case SIMPLE_FUNC:
                    criteriaCustomTable.refresh();
                    break;
                case U_SHAPE_FUNC:
                    cellValue.setParameters("q = 0");
                    criteriaCustomTable.refresh();
                    break;
                case V_SHAPE_FUNC:
                    cellValue.setParameters("s = 0");
                    criteriaCustomTable.refresh();
                    break;
                case LEVEL_FUNC:
                case V_SHAPE_FUNC_WITH_THRESHOLDS:
                    cellValue.setParameters("q = 0, s = 0");
                    criteriaCustomTable.refresh();
                    break;
                case GAUSS_FUNC:
                    cellValue.setParameters("sigma = 0");
                    criteriaCustomTable.refresh();
                    break;
            }
        });
        criteriaCustomTable.getColumns().add(functionTypeColumn);
        TableColumn<CriteriaCustomData, String> parameters = new TableColumn<>("Параметры");
        parameters.prefWidthProperty().bind(criteriaCustomTable.widthProperty().divide(divideWidthTo));
        parameters.setCellValueFactory(new PropertyValueFactory<>("parameters"));
        parameters.setCellFactory(TextFieldTableCell.forTableColumn());
        parameters.setOnEditCommit(cellData->{
            String params = cellData.getNewValue();
            criteriaCustomDataArray[cellData.getTablePosition().getRow()].setParameters(params);
        });
        criteriaCustomTable.getColumns().add(parameters);
    }

    private void buildValuesTableStructure() {
        valuesTable.setEditable(true);
        TableColumn<double[], String> nameOfAlts = new TableColumn<>("Альтернатива");
        nameOfAlts.prefWidthProperty().bind(valuesTable.widthProperty().divide(namesOfCriteriaList.size() + 1));
        nameOfAlts.setCellFactory(new Callback<>() {
            @Override
            public TableCell<double[], String> call(TableColumn<double[], String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (this.getTableRow() != null && !empty)
                            setText(namesOfAlternativesList.get(this.getTableRow().getIndex()));
                    }
                };
            }
        });
        valuesTable.getColumns().add(nameOfAlts);
        for (int i = 0; i < valuesMatrix[0].length; i++) {
            final int columnindex = i;
            //
            TableColumn<double[], String> column = new TableColumn<>(namesOfCriteriaList.get(i));
            column.setSortable(false);
            column.prefWidthProperty().bind(valuesTable.widthProperty().divide(namesOfCriteriaList.size() + 1));
            column.setCellValueFactory(cellData -> {
                double[] row = cellData.getValue();
                return new SimpleStringProperty(String.valueOf(row[columnindex]));
            });
            column.setOnEditCommit(event -> {
                int index = event.getTablePosition().getRow();
                valuesMatrix[index][event.getTablePosition().getColumn() - 1] = Double.parseDouble(event.getNewValue());
            });
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            valuesTable.getColumns().add(column);
        }
    }

    private void fillNamesTable(TableView<String[]> namesTable, ObservableList<String> namesList) {
        for (int i = 0; i < namesList.size(); i++)
            namesTable.getItems().add(new String[]{String.valueOf((i + 1)), namesList.get(i)});
    }

    private void fillCriteriaCustomTable() {
        for (int i = 0; i < namesOfCriteriaList.size(); i++)
            criteriaCustomTable.getItems().add(criteriaCustomDataArray[i]);
    }

    private void fillValuesTable() {
        for (int i = 0; i < valuesMatrix.length; i++)
            valuesTable.getItems().add(valuesMatrix[i]);
    }

    private void fillResult(){
        for (int i = 0; i < resultList.size(); i++)
            resultTextArea.appendText((i+1) + ") " + resultList.get(i) + "\n");
    }

    private boolean areWeightsSumEqualToOne(){
        double sum = 0;
        for (CriteriaCustomData data : criteriaCustomDataArray)
            sum += data.getWeight();
        return sum == 1.;
    }

    private void processAlg(){
        fourthStepOfAlg(thirdStepOfAlg(secondStepOfAlg(firstStepOfAlg())));
    }

    private ArrayList<double[][]>  firstStepOfAlg() {
        int k, i, j;
        ArrayList<double[][]> listOfMatrixes = new ArrayList<>();
        allStepsInformation.append("Шаг1. Парные сравнения альтернатив по каждому критерию.\n");
        for (k = 0; k < namesOfCriteriaList.size(); k++) {
            allStepsInformation.append("Разница между оценками альтернатив по критерию ").append(namesOfAlternativesList.get(k)).append(":\n");
            double[][] matrix = new double[namesOfAlternativesList.size()][namesOfAlternativesList.size()];
            for (i = 0; i < namesOfAlternativesList.size(); i++) {
                for (j = 0; j < namesOfAlternativesList.size(); j++) {
                    matrix[i][j] = valuesMatrix[i][k] - valuesMatrix[j][k];
                    allStepsInformation.append(matrix[i][j]).append(" ");
                }
                allStepsInformation.append("\n");
            }
            listOfMatrixes.add(matrix);
        }
        return listOfMatrixes;
    }

    private ArrayList<double[][]> secondStepOfAlg(ArrayList<double[][]> listOfMatrixes){
        int k, i, j;
        Pattern pattern;
        Matcher matcher;
        double q, s, sigma;
        allStepsInformation.append("\n\nШаг 2. Вычисление мер предпочтения по критериям.\n");
        for (k = 0; k < listOfMatrixes.size(); k++){
            double[][] matrix = listOfMatrixes.get(k);
            CriteriaCustomData criteriaCustomData = criteriaCustomDataArray[k];
            String params = criteriaCustomData.getParameters();
            boolean isNegative = criteriaCustomData.isNegativeDirection();
            allStepsInformation.append("Матрица значений функций предпочтения для критерия ").append(namesOfCriteriaList.get(k)).append(":\n");
            switch (criteriaCustomData.getFunctionType()){
                case SIMPLE_FUNC:
                    for(i = 0; i < matrix.length;i++){
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = simpleFunction(matrix[i][j]);
                            else
                                matrix[i][j] = simpleFunctionReversed(matrix[i][j]);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
                case U_SHAPE_FUNC:
                    pattern = Pattern.compile("\\d");
                    matcher = pattern.matcher(params);
                    matcher.find();
                    q = Double.parseDouble(matcher.group());
                    for(i = 0; i < matrix.length;i++){
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = functionUShape(matrix[i][j], q);
                            else
                                matrix[i][j] = functionUShapeReversed(matrix[i][j], q);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
                case V_SHAPE_FUNC:
                    pattern = Pattern.compile("\\d");
                    matcher = pattern.matcher(params);
                    matcher.find();
                    s = Double.parseDouble(matcher.group());
                    for(i = 0; i < matrix.length;i++){
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = functionVShape(matrix[i][j], s);
                            else
                                matrix[i][j] = functionVShapeReversed(matrix[i][j], s);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
                case LEVEL_FUNC:
                    pattern = Pattern.compile("\\d");
                    matcher = pattern.matcher(params);
                    matcher.find();
                        q = Double.parseDouble(matcher.group());
                        matcher.find();
                        s = Double.parseDouble(matcher.group());
                    for(i = 0; i < matrix.length;i++) {
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = levelFunction(matrix[i][j], q, s);
                            else
                                matrix[i][j] = levelFunctionReversed(matrix[i][j], q, s);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
                case V_SHAPE_FUNC_WITH_THRESHOLDS:
                    pattern = Pattern.compile("\\d");
                    matcher = pattern.matcher(params);
                    matcher.find();
                    q = Double.parseDouble(matcher.group());
                    matcher.find();
                    s = Double.parseDouble(matcher.group());
                    for(i = 0; i < matrix.length;i++){
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = functionVShapeWithThresholds(matrix[i][j], q, s);
                            else
                                matrix[i][j] = functionVShapeWithThresholdsReversed(matrix[i][j], q, s);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
                case GAUSS_FUNC:
                    pattern = Pattern.compile("\\d");
                    matcher = pattern.matcher(params);
                    matcher.find();
                    sigma = Double.parseDouble(matcher.group());
                    for(i = 0; i < matrix.length;i++){
                        for (j = 0; j < matrix[0].length; j++) {
                            if (!isNegative)
                                matrix[i][j] = functionGauss(matrix[i][j], sigma);
                            else
                                matrix[i][j] = functionGaussReversed(matrix[i][j], sigma);
                            allStepsInformation.append(matrix[i][j]).append(" ");
                        }
                        allStepsInformation.append("\n");
                    }
                    break;
            }
        }
        return listOfMatrixes;
    }

    private ArrayList<double[]> thirdStepOfAlg(ArrayList<double[][]> listOfMatrixes){
        int i, j, k;
        ArrayList<double[]> fs = new ArrayList<>();
        double[] weights = new double[criteriaCustomDataArray.length];
        allStepsInformation.append("\n\nШаг 3. Вычисление индексов предпочтения для каждой альтернативы.\n");
        for (i = 0; i < criteriaCustomDataArray.length; i++)
            weights[i] = criteriaCustomDataArray[i].getWeight();
        double[][] resMatrix = new double[namesOfAlternativesList.size()][namesOfAlternativesList.size()];
        double[] fMinus = new double[namesOfAlternativesList.size()], fPlus = new double[namesOfAlternativesList.size()];
        allStepsInformation.append("Индексы предпочтения для каждой альтернативы:\n");
        for (k = 0; k < namesOfCriteriaList.size(); k++){
            double[][] tempMatrix = listOfMatrixes.get(k);
            for (i = 0; i < namesOfAlternativesList.size(); i++){
                for (j = 0; j < namesOfAlternativesList.size(); j++){
                    resMatrix[i][j] += weights[k] * tempMatrix[i][j];
                    allStepsInformation.append(resMatrix[i][j]).append(" ");
                    fMinus[j] += weights[k] * tempMatrix[i][j];
                    fPlus[i] +=  weights[k] * tempMatrix[i][j];
                }
                allStepsInformation.append("\n");
            }
        }
        allStepsInformation.append("Ф+: ");
        for(i = 0; i < fPlus.length;i++)
            allStepsInformation.append(fPlus[i]).append(" ");
        allStepsInformation.append("\n").append("Ф-: ");
        for(i = 0; i < fMinus.length;i++)
            allStepsInformation.append(fMinus[i]).append(" ");
        allStepsInformation.append("\n");
        fs.add(fPlus);
        fs.add(fMinus);
        return fs;
    }

    private void fourthStepOfAlg(ArrayList<double[]> fs) {
        int i, j;
        double temp;
        double[] fPlus = fs.get(0);
        double[] fMinus = fs.get(1);
        double[] f = new double[fMinus.length];
        resultList = new ArrayList<>();
        allStepsInformation.append("\n\nШаг 4. Вычисление положительных, отрицательных и чистых оценок.\n");
        resultList.addAll(namesOfAlternativesList);
        String tempStr;
        allStepsInformation.append("Ф: ");
        for (i = 0; i < f.length; i++) {
            f[i] = fPlus[i] - fMinus[i];
            allStepsInformation.append(f[i]).append(" ");
        }
        for (i = 0; i < f.length; i++) {
            for (j = 0; j < f.length - 1; j++) {
                if (f[j] < f[j + 1]) {
                    temp = f[j + 1];
                    f[j + 1] = f[j];
                    f[j] = temp;
                    tempStr = resultList.get(j + 1);
                    resultList.set(j + 1, resultList.get(j));
                    resultList.set(j, tempStr);
                }
            }
        }
        allStepsInformation.append("\nФ после сортировки: ");
        for (i = 0; i < f.length; i++) {
            allStepsInformation.append(f[i]).append(" ");
        }
        allStepsInformation.append("\n").append("Ответ:\n");
        for (i = 0; i < resultList.size(); i++)
            allStepsInformation.append(i + 1).append(") ").append(resultList.get(i)).append("\n");
    }

    private int simpleFunction(double d){
        if (d > 0)
            return 1;
        return 0;
    }

    private int simpleFunctionReversed(double d){
        if(d < 0)
            return 1;
        return 0;
    }
    private int functionUShape(double d, double q){
        if (d > q)
            return 1;
        return 0;
    }

    private int functionUShapeReversed(double d, double q){
        if(d < q)
            return 1;
        return 0;
    }

    private double functionVShape(double d, double s){
        if (d<=0)
            return 0;
        if (d > 0 && d <= s)
            return d/s;
        return 1;
    }

    private double functionVShapeReversed(double d, double s){
        if (d > 0)
            return 0;
        if (s < d  && d <=0)
            return d/s;
        return 1;
    }
    private double levelFunction(double d, double q, double s){
        if (d <= q)
            return 0;
        if (d > q && d <=s)
            return 0.5;
        return 1;
    }

    private double levelFunctionReversed(double d, double q, double s){
        if ( d > q)
            return 0;
        if (d > s && d <= q)
            return 0.5;
        return 1;
    }

    private double functionVShapeWithThresholds(double d, double q, double s){
        if (d <= q)
            return 0;
        if ( d > q && d <= s)
            return (d - q)/(s - q);
        return 1;
    }

    private double functionVShapeWithThresholdsReversed(double d, double q, double s){
        if (d < q)
            return 0;
        if ( d <= q && d >= s)
            return (d - q)/(s - q);
        return 1;
    }

    private double functionGauss(double d, double sigma){
        if (d <= 0)
            return 0;
        return 1 - Math.exp(-Math.pow(d,2)/(2 * Math.pow(sigma, 2)));
    }

    private double functionGaussReversed(double d, double sigma){
        if (d > 0)
            return 0;
        return 1 - Math.exp(-Math.pow(d,2)/(2 * Math.pow(sigma, 2)));
    }

    private void initializeMenuBar(){
        MenuBar menuBar = new MenuBar();
        borderPane.setTop(menuBar);
        Menu fileMenu = new Menu("Файл");
        MenuItem newItem = new MenuItem("Создать");
        newItem.setOnAction(actionEvent ->newFile());
        MenuItem saveItem = new MenuItem("Сохранить");
        saveItem.setOnAction(actionEvent -> saveFile());
        MenuItem openItem = new MenuItem("Открыть...");
        openItem.setOnAction(actionEvent -> openFile());
        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        MenuItem exitItem = new MenuItem("Выход");
        fileMenu.getItems().addAll(newItem, saveItem, openItem, separatorMenuItem, exitItem);
        menuBar.getMenus().addAll(fileMenu);
    }

    private void newFile(){
        if (isDataInitialized) {
            clearData();
            isDataInitialized = false;
        }
        clearInformationInTables();
        setStateOfAppDependOnData(false);
    }

    private void saveFile(){
        Stage primaryStage = new Main().getPrimaryStage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл: ");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_5лаба"));
        File file = fileChooser.showSaveDialog(primaryStage);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_5лаба/" + file.getName()))) {
            CriteriaCustomDataFileUnit[]  criteriaCustomDataFileUnit = new CriteriaCustomDataFileUnit[criteriaCustomDataArray.length];
            for (int i = 0; i < criteriaCustomDataFileUnit.length; i++){
                criteriaCustomDataFileUnit[i] = new CriteriaCustomDataFileUnit(
                        criteriaCustomDataArray[i].getName(),
                        criteriaCustomDataArray[i].getFunctionType(),
                        criteriaCustomDataArray[i].getParameters(),
                        criteriaCustomDataArray[i].getWeight(),
                        criteriaCustomDataArray[i].isNegativeDirection());
            }
            FileUnit fileUnit = new FileUnit(new ArrayList<>(namesOfAlternativesList), new ArrayList<>(namesOfCriteriaList), criteriaCustomDataFileUnit, valuesMatrix, resultList);
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
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_5лаба"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_5лаба/" + file.getName()));
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
        clearInformationInTables();
        valuesTable.getColumns().clear();
        initializeDataFromFile(fileUnit);
        if (!isDataInitialized){
            addListenersToListsOfNames();
            buildNamesTablesStructure(namesOfAlternativesTable, namesOfAlternativesList);
            buildNamesTablesStructure(namesOfCriteriaTable, namesOfCriteriaList);
            buildCriteriaCustomTableStructure();
        }
        buildValuesTableStructure();
        fillNamesTable(namesOfAlternativesTable, namesOfAlternativesList);
        fillNamesTable(namesOfCriteriaTable, namesOfCriteriaList);
        fillCriteriaCustomTable();
        fillValuesTable();
        fillResult();
        refreshAllTables();
        isDataInitialized = true;
        setStateOfAppDependOnData(true);
    }

    private void initializeDataFromFile(FileUnit fileUnit){
        namesOfAlternativesList = FXCollections.observableList(fileUnit.getNamesOfAlternativesList());
        namesOfCriteriaList = FXCollections.observableList(fileUnit.getNamesOfCriteriaList());
        valuesMatrix = fileUnit.getValuesMatrix();
        CriteriaCustomDataFileUnit[] criteriaCustomDataFileUnits = fileUnit.getCriteriaCustomDataFileUnits();
        initializeCriteriaCustomArrayList(criteriaCustomDataFileUnits.length);
        for(int i = 0; i < criteriaCustomDataFileUnits.length; i++){
            criteriaCustomDataArray[i].setName(criteriaCustomDataFileUnits[i].getName());
            criteriaCustomDataArray[i].setFunctionType(criteriaCustomDataFileUnits[i].getFunctionType());
            criteriaCustomDataArray[i].setParameters(criteriaCustomDataFileUnits[i].getParameters());
            criteriaCustomDataArray[i].setNegativeDirection(criteriaCustomDataFileUnits[i].isNegativeDirection());
            criteriaCustomDataArray[i].setWeight(criteriaCustomDataFileUnits[i].getWeight());
        }
        resultList = fileUnit.getResultList();
    }

    private void clearData(){
        namesOfAlternativesList.clear();
        namesOfCriteriaList.clear();
        criteriaCustomDataArray = null;
        valuesMatrix = null;
        resultList.clear();
    }
    private void deleteTablesStructure() {
        namesOfAlternativesTable.getColumns().clear();
        namesOfCriteriaTable.getColumns().clear();
        criteriaCustomTable.getColumns().clear();
        valuesTable.getColumns().clear();
        namesOfAlternativesTable.setPlaceholder(new Label("Данных нет"));
        namesOfCriteriaTable.setPlaceholder(new Label("Данных нет"));
        criteriaCustomTable.setPlaceholder(new Label("Данных нет"));
        valuesTable.setPlaceholder(new Label("Данных нет"));
    }

    private void clearInformationInTables(){
        namesOfCriteriaTable.getItems().clear();
        namesOfAlternativesTable.getItems().clear();
        criteriaCustomTable.getItems().clear();
        valuesTable.getItems().clear();
        resultTextArea.clear();
    }

    private void refreshAllTables(){
        valuesTable.refresh();
        criteriaCustomTable.refresh();
        namesOfAlternativesTable.refresh();
        namesOfCriteriaTable.refresh();
    }

    private void setStateOfAppDependOnData(boolean hasEnteredData) {
        countBtn.setDisable(!hasEnteredData);
        showStepsBtn.setDisable(!hasEnteredData);
    }
}
