package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;

/** Головне вікно JavaFX-клієнта: чотири вкладки (обладнання, клієнти, заявки, договори). */
public class MainApp extends Application {
    private final HttpRentalClient api = new HttpRentalClient();

    private final ObservableList<Equipment> equipment = FXCollections.observableArrayList();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();
    private final ObservableList<RentalOrder> orders = FXCollections.observableArrayList();
    private final ObservableList<RentalContract> contracts = FXCollections.observableArrayList();

    private final TableView<Equipment> equipmentTable = new TableView<>(equipment);
    private final TableView<Client> clientTable = new TableView<>(clients);
    private final TableView<RentalOrder> orderTable = new TableView<>(orders);
    private final TableView<RentalContract> contractTable = new TableView<>(contracts);

    private final Label status = new Label("Готово");
    private final ProgressBar progress = new ProgressBar(0);
    private final Label importLabel = new Label();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                tab("Обладнання", equipmentTab()),
                tab("Клієнти", clientTab()),
                tab("Заявки", orderTab()),
                tab("Договори", contractTab()));
        BorderPane root = new BorderPane(tabs);
        root.setBottom(new HBox(10, status));
        ((HBox) root.getBottom()).setPadding(new Insets(5, 10, 5, 10));
        stage.setTitle("Інформаційна система прокату обладнання");
        stage.setScene(new Scene(root, 1100, 650));
        stage.show();
        refreshAll();
    }

    private Tab tab(String title, javafx.scene.Node content) {
        Tab t = new Tab(title, content);
        t.setClosable(false);
        return t;
    }

    // ---------- Загальні допоміжні методи ----------
    private void safe(Runnable action) {
        try {
            action.run();
            status.setText("Операцію виконано");
        } catch (ApiException e) {
            status.setText("Помилка: " + e.getMessage());
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        } catch (RuntimeException e) {
            status.setText("Некоректні дані: " + e.getMessage());
            new Alert(Alert.AlertType.WARNING, "Перевірте введені дані: " + e.getMessage()).showAndWait();
        }
    }

    private void refreshAll() {
        safe(() -> {
            equipment.setAll(api.getEquipment());
            clients.setAll(api.getClients());
            orders.setAll(api.getOrders());
            contracts.setAll(api.getContracts());
        });
    }

    private static <T> TableColumn<T, ?> col(String title, String property) {
        TableColumn<T, Object> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(120);
        return c;
    }

    private static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private static HBox row(javafx.scene.Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.setPadding(new Insets(5));
        return box;
    }

    private static <T> T selected(TableView<T> table) {
        T item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException("Оберіть рядок у таблиці");
        }
        return item;
    }

    private static LocalDate date(DatePicker p, String name) {
        if (p.getValue() == null) {
            throw new IllegalArgumentException("Вкажіть дату: " + name);
        }
        return p.getValue();
    }

    private static double number(TextField f, String name) {
        try {
            return Double.parseDouble(f.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути числом");
        }
    }

    // ---------- Вкладка «Обладнання» ----------
    @SuppressWarnings("unchecked")
    private VBox equipmentTab() {
        equipmentTable.getColumns().addAll(col("ID", "id"), col("Код", "code"), col("Назва", "name"),
                col("Категорія", "category"), col("Ціна за день", "pricePerDay"));
        TextField code = field("Код");
        TextField name = field("Назва");
        TextField category = field("Категорія");
        TextField price = field("Ціна за день");

        equipmentTable.getSelectionModel().selectedItemProperty().addListener((o, a, e) -> {
            if (e != null) {
                code.setText(e.getCode());
                name.setText(e.getName());
                category.setText(e.getCategory());
                price.setText(String.valueOf(e.getPricePerDay()));
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(ev -> safe(() -> {
            api.addEquipment(buildEquipment(0, code, name, category, price));
            equipment.setAll(api.getEquipment());
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(ev -> safe(() -> {
            api.updateEquipment(buildEquipment(selected(equipmentTable).getId(), code, name, category, price));
            equipment.setAll(api.getEquipment());
        }));
        Button del = new Button("Видалити");
        del.setOnAction(ev -> safe(() -> {
            api.deleteEquipment(selected(equipmentTable).getId());
            equipment.setAll(api.getEquipment());
        }));

        Button importBtn = new Button("Імпортувати CSV");
        Button cancelBtn = new Button("Скасувати");
        cancelBtn.setDisable(true);
        importBtn.setOnAction(ev -> startImport(importBtn, cancelBtn));
        progress.setPrefWidth(200);

        VBox box = new VBox(8, equipmentTable, row(code, name, category, price),
                row(add, edit, del), row(importBtn, cancelBtn, progress, importLabel));
        box.setPadding(new Insets(10));
        VBox.setVgrow(equipmentTable, Priority.ALWAYS);
        return box;
    }

    private Equipment buildEquipment(long id, TextField code, TextField name, TextField category, TextField price) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setCode(code.getText().trim());
        e.setName(name.getText().trim());
        e.setCategory(category.getText().trim());
        e.setPricePerDay(number(price, "Ціна за день"));
        return e;
    }

    private void startImport(Button importBtn, Button cancelBtn) {
        ImportEquipmentTask task = new ImportEquipmentTask(Path.of("sample-data", "equipment.csv"), api);
        progress.progressProperty().bind(task.progressProperty());
        importLabel.textProperty().bind(task.messageProperty());
        importBtn.setDisable(true);
        cancelBtn.setDisable(false);
        cancelBtn.setOnAction(ev -> task.cancel());
        task.setOnSucceeded(ev -> {
            finishImport(importBtn, cancelBtn, "Готово. Імпортовано записів: " + task.getValue());
            safe(() -> equipment.setAll(api.getEquipment()));
        });
        task.setOnFailed(ev -> {
            Throwable t = task.getException();
            String msg;
            if (t instanceof java.nio.file.NoSuchFileException) {
                msg = "Файл sample-data/equipment.csv не знайдено. Запускайте клієнт із папки client.";
            } else if (t instanceof IllegalArgumentException || t instanceof ApiException) {
                msg = t.getMessage();
            } else {
                msg = "Імпорт перервано: " + t.getMessage();
            }
            finishImport(importBtn, cancelBtn, "Помилка імпорту. Частина записів могла бути збережена.");
            status.setText(msg);
            new Alert(Alert.AlertType.ERROR, msg).showAndWait();
            safe(() -> equipment.setAll(api.getEquipment()));
        });
        task.setOnCancelled(ev -> {
            finishImport(importBtn, cancelBtn, "Імпорт скасовано; частина записів могла бути збережена.");
            safe(() -> equipment.setAll(api.getEquipment()));
        });
        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private void finishImport(Button importBtn, Button cancelBtn, String message) {
        progress.progressProperty().unbind();
        importLabel.textProperty().unbind();
        importLabel.setText(message);
        importBtn.setDisable(false);
        cancelBtn.setDisable(true);
    }

    // ---------- Вкладка «Клієнти» ----------
    @SuppressWarnings("unchecked")
    private VBox clientTab() {
        clientTable.getColumns().addAll(col("ID", "id"), col("ПІБ", "fullName"), col("Телефон", "phone"),
                col("Email", "email"), col("Адреса", "address"));
        TextField fullName = field("ПІБ");
        TextField phone = field("Телефон");
        TextField email = field("Email");
        TextField address = field("Адреса");

        clientTable.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                fullName.setText(c.getFullName());
                phone.setText(c.getPhone());
                email.setText(c.getEmail());
                address.setText(c.getAddress());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(ev -> safe(() -> {
            api.addClient(buildClient(0, fullName, phone, email, address));
            clients.setAll(api.getClients());
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(ev -> safe(() -> {
            api.updateClient(buildClient(selected(clientTable).getId(), fullName, phone, email, address));
            clients.setAll(api.getClients());
        }));
        Button del = new Button("Видалити");
        del.setOnAction(ev -> safe(() -> {
            api.deleteClient(selected(clientTable).getId());
            clients.setAll(api.getClients());
        }));

        VBox box = new VBox(8, clientTable, row(fullName, phone, email, address), row(add, edit, del));
        box.setPadding(new Insets(10));
        VBox.setVgrow(clientTable, Priority.ALWAYS);
        return box;
    }

    private Client buildClient(long id, TextField fullName, TextField phone, TextField email, TextField address) {
        Client c = new Client();
        c.setId(id);
        c.setFullName(fullName.getText().trim());
        c.setPhone(phone.getText().trim());
        c.setEmail(email.getText().trim());
        c.setAddress(address.getText().trim());
        return c;
    }

    // ---------- Вкладка «Заявки» ----------
    @SuppressWarnings("unchecked")
    private VBox orderTab() {
        orderTable.getColumns().addAll(col("ID", "id"), col("Клієнт", "clientId"), col("Обладнання", "equipmentId"),
                col("Початок", "startDate"), col("Кінець", "endDate"), col("Стан", "state"),
                col("F1", "f1"), col("F2", "f2"), col("F3", "f3"), col("Індекс I", "priorityIndex"));

        ComboBox<Client> clientBox = new ComboBox<>(clients);
        clientBox.setPromptText("Клієнт");
        clientBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Client c) { return c == null ? "" : c.getId() + " - " + c.getFullName(); }
            @Override public Client fromString(String s) { return null; }
        });
        ComboBox<Equipment> equipmentBox = new ComboBox<>(equipment);
        equipmentBox.setPromptText("Обладнання");
        equipmentBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Equipment e) { return e == null ? "" : e.getCode() + " - " + e.getName(); }
            @Override public Equipment fromString(String s) { return null; }
        });
        DatePicker start = new DatePicker();
        start.setPromptText("Початок");
        DatePicker end = new DatePicker();
        end.setPromptText("Кінець");
        TextField f2 = field("F2 (0-100)");
        TextField f3 = field("F3 (0-100)");

        orderTable.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r != null) {
                start.setValue(r.getStartDate());
                end.setValue(r.getEndDate());
                f2.setText(String.valueOf(r.getF2()));
                f3.setText(String.valueOf(r.getF3()));
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(ev -> safe(() -> {
            api.addOrder(buildOrder(0, clientBox, equipmentBox, start, end, f2, f3));
            orders.setAll(api.getOrders());
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(ev -> safe(() -> {
            api.updateOrder(buildOrder(selected(orderTable).getId(), clientBox, equipmentBox, start, end, f2, f3));
            orders.setAll(api.getOrders());
        }));
        Button del = new Button("Видалити");
        del.setOnAction(ev -> safe(() -> {
            api.deleteOrder(selected(orderTable).getId());
            orders.setAll(api.getOrders());
        }));

        ComboBox<OrderState> stateBox = new ComboBox<>(FXCollections.observableArrayList(OrderState.values()));
        stateBox.setPromptText("Новий стан");
        Button transition = new Button("Змінити стан");
        transition.setOnAction(ev -> safe(() -> {
            if (stateBox.getValue() == null) {
                throw new IllegalArgumentException("Оберіть новий стан");
            }
            api.changeOrderState(selected(orderTable).getId(), stateBox.getValue());
            orders.setAll(api.getOrders());
        }));
        Button refresh = new Button("Оновити");
        refresh.setOnAction(ev -> refreshAll());

        VBox box = new VBox(8, orderTable, row(clientBox, equipmentBox, start, end, f2, f3),
                row(add, edit, del), row(stateBox, transition, refresh));
        box.setPadding(new Insets(10));
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        return box;
    }

    private RentalOrder buildOrder(long id, ComboBox<Client> clientBox, ComboBox<Equipment> equipmentBox,
                                   DatePicker start, DatePicker end, TextField f2, TextField f3) {
        if (clientBox.getValue() == null || equipmentBox.getValue() == null) {
            throw new IllegalArgumentException("Оберіть клієнта й обладнання");
        }
        RentalOrder o = new RentalOrder();
        o.setId(id);
        o.setClientId(clientBox.getValue().getId());
        o.setEquipmentId(equipmentBox.getValue().getId());
        o.setStartDate(date(start, "початок"));
        o.setEndDate(date(end, "кінець"));
        o.setF2(number(f2, "F2"));
        o.setF3(number(f3, "F3"));
        return o;
    }

    // ---------- Вкладка «Договори» ----------
    @SuppressWarnings("unchecked")
    private VBox contractTab() {
        contractTable.getColumns().addAll(col("ID", "id"), col("Заявка", "orderId"), col("Номер", "contractNumber"),
                col("Дата підписання", "signDate"), col("Сума", "totalPrice"));
        TextField orderId = field("ID заявки");
        TextField number = field("Номер договору");
        DatePicker sign = new DatePicker();
        sign.setPromptText("Дата підписання");

        contractTable.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                orderId.setText(String.valueOf(c.getOrderId()));
                number.setText(c.getContractNumber());
                sign.setValue(c.getSignDate());
            }
        });

        Button add = new Button("Додати");
        add.setOnAction(ev -> safe(() -> {
            api.addContract((long) number(orderId, "ID заявки"), number.getText().trim(), date(sign, "підписання"));
            contracts.setAll(api.getContracts());
        }));
        Button edit = new Button("Змінити");
        edit.setOnAction(ev -> safe(() -> {
            api.updateContract(selected(contractTable).getId(), (long) number(orderId, "ID заявки"),
                    number.getText().trim(), date(sign, "підписання"));
            contracts.setAll(api.getContracts());
        }));
        Button del = new Button("Видалити");
        del.setOnAction(ev -> safe(() -> {
            api.deleteContract(selected(contractTable).getId());
            contracts.setAll(api.getContracts());
        }));

        VBox box = new VBox(8, contractTable, row(orderId, number, sign), row(add, edit, del));
        box.setPadding(new Insets(10));
        VBox.setVgrow(contractTable, Priority.ALWAYS);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
