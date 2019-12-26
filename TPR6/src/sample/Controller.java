package sample;

import java.io.*;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class Controller {

    @FXML
    private BorderPane borderPane;

    @FXML
    private ChoiceBox<?> graphTypeChoice;

    @FXML
    private AnchorPane graphPane;

    @FXML
    private TableView<StockData> stockDataTable;

    ArrayList<StockData> stockDataList;

    @FXML
    void initialize() {
        initializeMenuBar();
    }

    private void initializeStockDataArray(BufferedReader bufferedReader) throws IOException {
        stockDataList = new ArrayList<>();
        String st, time, date;
        double open, high, low, close;
        int vol;
        while((st = bufferedReader.readLine()) != null){
            String[] temp = st.split(",");
            date = temp[0];
            time = temp[1];
            open = Double.parseDouble(temp[2]);
            high = Double.parseDouble(temp[3]);
            low = Double.parseDouble(temp[4]);
            close = Double.parseDouble(temp[5]);
            vol = Integer.parseInt(temp[6]);
            stockDataList.add(new StockData(date, time, open, high, low, close, vol));
        }
    }

    private void initializeStockDataTable(){
        int divisionOfWidth = 7;
        stockDataTable.setEditable(true);
        TableColumn<StockData, String> date = new TableColumn<>("DATE");
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        date.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setDate(cellData.getNewValue()));
        date.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn<StockData, String> time = new TableColumn<>("TIME");
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        time.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setTime(cellData.getNewValue()));
        time.setCellFactory(TextFieldTableCell.forTableColumn());
        TableColumn<StockData, Double> open = new TableColumn<>("OPEN");
        open.setCellValueFactory(new PropertyValueFactory<>("open"));
        open.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setOpen(cellData.getNewValue()));
        open.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        TableColumn<StockData, Double> high = new TableColumn<>("HIGH");
        high.setCellValueFactory(new PropertyValueFactory<>("high"));
        high.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setHigh(cellData.getNewValue()));
        high.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        TableColumn<StockData, Double> low = new TableColumn<>("LOW");
        low.setCellValueFactory(new PropertyValueFactory<>("low"));
        low.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setLow(cellData.getNewValue()));
        low.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        TableColumn<StockData, Double> close = new TableColumn<>("CLOSE");
        close.setCellValueFactory(new PropertyValueFactory<>("close"));
        close.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setClose(cellData.getNewValue()));
        close.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        TableColumn<StockData, Integer> vol = new TableColumn<>("VOL");
        vol.setCellValueFactory(new PropertyValueFactory<>("vol"));
        vol.setOnEditCommit(cellData-> stockDataList.get(cellData.getTablePosition().getRow()).setVol(cellData.getNewValue()));
        vol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        date.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        time.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        open.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        high.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        low.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        close.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        vol.prefWidthProperty().bind(stockDataTable.widthProperty().divide(divisionOfWidth));
        stockDataTable.getColumns().addAll(date, time, open, high, low, close, vol);
    }

    private void fillStockDataTable(){
        for (StockData stockDataItem : stockDataList){
            stockDataTable.getItems().add(stockDataItem);
        }
    }

    private void buildGraph() throws IOException{

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
    }

    private void saveFile(){
        Stage primaryStage = new Main().getPrimaryStage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл: ");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_6лаба"));
        File file = fileChooser.showSaveDialog(primaryStage);
        try (BufferedWriter oos = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_6лаба/" + file.getName())))) {
            for(StockData stockData : stockDataList){
                oos.write(
                        stockData.getDate()+ "," +
                                stockData.getTime() + "," +
                                stockData.getOpen() + "," +
                                stockData.getHigh() + "," +
                                stockData.getLow() + "," +
                                stockData.getClose() + "," +
                                stockData.getVol() + "\n"
                );
            }
        } catch (Exception e) {
            System.out.println("excSave");
            e.printStackTrace();
        }
    }

    private void openFile(){
        Stage primaryStage = new Main().getPrimaryStage();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с данными об акции");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_6лаба"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(System.getProperty("user.home") + "/Desktop/Универ/4курс1семестр/ТПР/ТПР_6лаба/" + file.getName())));
                initializeStockDataArray(bufferedReader);
                initializeStockDataTable();
                fillStockDataTable();
                buildGraph();
            } catch (IOException e) {
                System.out.println("excDown");
                e.printStackTrace();
            }
        }
    }
}
