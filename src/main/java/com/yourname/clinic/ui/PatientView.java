package com.yourname.clinic.ui;

import com.yourname.clinic.service.ClinicDbService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientView extends VBox {

    private final ClinicDbService db = new ClinicDbService();
    private final ObservableList<PatientRow> rows = FXCollections.observableArrayList();
    private final ObservableList<PatientRow> pageRows = FXCollections.observableArrayList();
    private final TableView<PatientRow> table = new TableView<>();
    private final TextField keywordField = new TextField();
    private final Label pageLabel = new Label();
    private int currentPage = 1;
    private int pageSize = 10;
    private List<PatientRow> workingSet = new ArrayList<>();

    public PatientView() {
        setSpacing(10);
        setPadding(new Insets(12));

        keywordField.setPromptText("姓名/手机号搜索");

        Button searchBtn = new Button("搜索");
        Button addBtn = new Button("新增");
        Button editBtn = new Button("编辑");
        Button delBtn = new Button("删除");
        Button resetBtn = new Button("重置");
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");

        searchBtn.setOnAction(e -> applyFilter());
        addBtn.setOnAction(e -> openForm(null));
        editBtn.setOnAction(e -> openForm(table.getSelectionModel().getSelectedItem()));
        delBtn.setOnAction(e -> deleteSelected());
        resetBtn.setOnAction(e -> {
            keywordField.clear();
            loadData();
        });
        prevBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; refreshPage(); }});
        nextBtn.setOnAction(e -> {
            int totalPages = totalPages();
            if (currentPage < totalPages) { currentPage++; refreshPage(); }
        });

        HBox toolbar = new HBox(8, keywordField, searchBtn, addBtn, editBtn, delBtn, resetBtn);
        HBox pager = new HBox(8, prevBtn, nextBtn, pageLabel);
        initTable();
        loadData();

        getChildren().addAll(new Label("患者管理（SQLite 持久化）"), toolbar, table, pager);
    }

    private void initTable() {
        table.getColumns().add(col("ID", PatientRow::idProperty, 80));
        table.getColumns().add(col("姓名", PatientRow::nameProperty, 140));
        table.getColumns().add(col("性别", PatientRow::genderProperty, 90));
        table.getColumns().add(col("出生日期", PatientRow::birthDateProperty, 120));
        table.getColumns().add(col("手机号", PatientRow::phoneProperty, 160));
        table.getColumns().add(col("备注", PatientRow::remarkProperty, 200));
        table.setItems(pageRows);
        table.setPrefHeight(520);
    }

    private TableColumn<PatientRow, String> col(String title,
                                                java.util.function.Function<PatientRow, SimpleStringProperty> getter,
                                                int width) {
        TableColumn<PatientRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> getter.apply(d.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private void loadData() {
        rows.clear();
        for (String[] r : db.listPatients()) {
            rows.add(new PatientRow(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        workingSet = new ArrayList<>(rows);
        currentPage = 1;
        refreshPage();
    }

    private void applyFilter() {
        String k = keywordField.getText() == null ? "" : keywordField.getText().trim();
        if (k.isEmpty()) {
            loadData();
            return;
        }
        List<PatientRow> filtered = new ArrayList<>();
        for (PatientRow r : rows) {
            if (r.nameProperty().get().contains(k) || r.phoneProperty().get().contains(k)) filtered.add(r);
        }
        workingSet = filtered;
        currentPage = 1;
        refreshPage();
    }

    private void openForm(PatientRow current) {
        Dialog<PatientForm> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "新增患者" : "编辑患者");

        TextField name = new TextField(current == null ? "" : current.nameProperty().get());
        ComboBox<String> gender = new ComboBox<>(FXCollections.observableArrayList("男", "女", "其他"));
        gender.setValue(current == null ? "男" : current.genderProperty().get());
        DatePicker birth = new DatePicker();
        if (current != null && !current.birthDateProperty().get().isBlank()) birth.setValue(LocalDate.parse(current.birthDateProperty().get()));
        TextField phone = new TextField(current == null ? "" : current.phoneProperty().get());
        TextField remark = new TextField(current == null ? "" : current.remarkProperty().get());

        VBox form = new VBox(8, row("姓名", name), row("性别", gender), row("出生日期", birth), row("手机号", phone), row("备注", remark));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new PatientForm(name.getText().trim(), gender.getValue(),
                birth.getValue() == null ? "" : birth.getValue().toString(), phone.getText().trim(), remark.getText().trim()) : null);

        Optional<PatientForm> ret = dialog.showAndWait();
        if (ret.isEmpty()) return;
        PatientForm f = ret.get();
        if (f.name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "姓名不能为空").showAndWait();
            return;
        }

        if (current == null) db.insertPatient(f.name, f.gender, f.birthDate, f.phone, f.remark);
        else db.updatePatient(Long.parseLong(current.idProperty().get()), f.name, f.gender, f.birthDate, f.phone, f.remark);

        loadData();
    }

    private void deleteSelected() {
        PatientRow current = table.getSelectionModel().getSelectedItem();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择患者").showAndWait();
            return;
        }
        if (new Alert(Alert.AlertType.CONFIRMATION, "确认删除该患者？", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            db.deletePatient(Long.parseLong(current.idProperty().get()));
            loadData();
        }
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil((double) workingSet.size() / pageSize));
    }

    private void refreshPage() {
        int totalPages = totalPages();
        if (currentPage > totalPages) currentPage = totalPages;
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, workingSet.size());
        pageRows.setAll(from >= to ? List.of() : workingSet.subList(from, to));
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页，共 " + workingSet.size() + " 条");
    }

    private HBox row(String label, javafx.scene.Node input) {
        Label l = new Label(label + "：");
        l.setPrefWidth(80);
        return new HBox(8, l, input);
    }

    private record PatientForm(String name, String gender, String birthDate, String phone, String remark) {}

    public static class PatientRow {
        private final SimpleStringProperty id, name, gender, birthDate, phone, remark;
        public PatientRow(String id, String name, String gender, String birthDate, String phone, String remark) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.gender = new SimpleStringProperty(gender);
            this.birthDate = new SimpleStringProperty(birthDate);
            this.phone = new SimpleStringProperty(phone);
            this.remark = new SimpleStringProperty(remark);
        }
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty genderProperty() { return gender; }
        public SimpleStringProperty birthDateProperty() { return birthDate; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty remarkProperty() { return remark; }
    }
}
