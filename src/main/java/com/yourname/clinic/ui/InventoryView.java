package com.yourname.clinic.ui;

import com.yourname.clinic.service.ClinicDbService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryView extends VBox {

    private final ClinicDbService db = new ClinicDbService();
    private final ObservableList<MedicineRow> rows = FXCollections.observableArrayList();
    private final ObservableList<MedicineRow> pageRows = FXCollections.observableArrayList();
    private final TableView<MedicineRow> table = new TableView<>();
    private final Label pageLabel = new Label();
    private int currentPage = 1;
    private int pageSize = 10;

    public InventoryView() {
        setPadding(new Insets(12));
        setSpacing(10);

        Button addBtn = new Button("新增药品");
        Button editBtn = new Button("编辑药品");
        Button inBtn = new Button("入库");
        Button outBtn = new Button("出库");
        Button refreshBtn = new Button("刷新");
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");

        addBtn.setOnAction(e -> openForm(null));
        editBtn.setOnAction(e -> openForm(table.getSelectionModel().getSelectedItem()));
        inBtn.setOnAction(e -> adjustStock(true));
        outBtn.setOnAction(e -> adjustStock(false));
        refreshBtn.setOnAction(e -> loadData());
        prevBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; refreshPage(); }});
        nextBtn.setOnAction(e -> { if (currentPage < totalPages()) { currentPage++; refreshPage(); }});

        initTable();
        loadData();

        getChildren().addAll(
                new Label("库存管理（SQLite 持久化）"),
                new HBox(8, addBtn, editBtn, inBtn, outBtn, refreshBtn),
                table,
                new HBox(8, prevBtn, nextBtn, pageLabel)
        );
    }

    private void initTable() {
        table.getColumns().add(col("ID", MedicineRow::idProperty, 80));
        table.getColumns().add(col("药品", MedicineRow::nameProperty, 160));
        table.getColumns().add(col("规格", MedicineRow::specProperty, 120));
        table.getColumns().add(col("单位", MedicineRow::unitProperty, 80));
        table.getColumns().add(col("库存", MedicineRow::stockProperty, 100));
        table.getColumns().add(col("有效期", MedicineRow::expireDateProperty, 120));
        table.getColumns().add(col("备注", MedicineRow::remarkProperty, 220));
        table.setItems(pageRows);
        table.setPrefHeight(520);
    }

    private TableColumn<MedicineRow, String> col(String title,
                                                 java.util.function.Function<MedicineRow, SimpleStringProperty> getter,
                                                 int width) {
        TableColumn<MedicineRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> getter.apply(d.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private void loadData() {
        rows.clear();
        for (String[] r : db.listMedicines()) rows.add(new MedicineRow(r[0], r[1], r[2], r[3], r[4], r[5], r[6]));
        currentPage = 1;
        refreshPage();
    }

    private void openForm(MedicineRow current) {
        Dialog<MedicineForm> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "新增药品" : "编辑药品");

        TextField name = new TextField(current == null ? "" : current.nameProperty().get());
        TextField spec = new TextField(current == null ? "" : current.specProperty().get());
        TextField unit = new TextField(current == null ? "盒" : current.unitProperty().get());
        DatePicker expire = new DatePicker();
        if (current != null && !current.expireDateProperty().get().isBlank()) expire.setValue(LocalDate.parse(current.expireDateProperty().get()));
        TextField remark = new TextField(current == null ? "" : current.remarkProperty().get());

        VBox form = new VBox(8, row("药品", name), row("规格", spec), row("单位", unit), row("有效期", expire), row("备注", remark));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? new MedicineForm(name.getText().trim(), spec.getText().trim(),
                unit.getText().trim(), expire.getValue() == null ? "" : expire.getValue().toString(), remark.getText().trim()) : null);

        Optional<MedicineForm> ret = dialog.showAndWait();
        if (ret.isEmpty()) return;
        MedicineForm f = ret.get();
        if (f.name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "药品名不能为空").showAndWait();
            return;
        }

        if (current == null) db.insertMedicine(f.name, f.spec, f.unit, f.expireDate, f.remark);
        else db.updateMedicine(Long.parseLong(current.idProperty().get()), f.name, f.spec, f.unit, f.expireDate, f.remark);

        loadData();
    }

    private void adjustStock(boolean inbound) {
        MedicineRow current = table.getSelectionModel().getSelectedItem();
        if (current == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择药品").showAndWait();
            return;
        }

        TextInputDialog input = new TextInputDialog("1");
        input.setHeaderText(inbound ? "入库数量" : "出库数量");
        Optional<String> ret = input.showAndWait();
        if (ret.isEmpty()) return;

        try {
            BigDecimal qty = new BigDecimal(ret.get().trim());
            if (!inbound) qty = qty.negate();

            BigDecimal stock = new BigDecimal(current.stockProperty().get());
            BigDecimal newStock = stock.add(qty);
            if (newStock.signum() < 0) {
                new Alert(Alert.AlertType.WARNING, "库存不足").showAndWait();
                return;
            }

            db.adjustMedicineStock(Long.parseLong(current.idProperty().get()), qty.doubleValue());
            loadData();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "数量格式错误").showAndWait();
        }
    }

    private HBox row(String label, javafx.scene.Node input) {
        Label l = new Label(label + "：");
        l.setPrefWidth(80);
        return new HBox(8, l, input);
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil((double) rows.size() / pageSize));
    }

    private void refreshPage() {
        int totalPages = totalPages();
        if (currentPage > totalPages) currentPage = totalPages;
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, rows.size());
        pageRows.setAll(from >= to ? List.of() : new ArrayList<>(rows.subList(from, to)));
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页，共 " + rows.size() + " 条");
    }

    private record MedicineForm(String name, String spec, String unit, String expireDate, String remark) {}

    public static class MedicineRow {
        private final SimpleStringProperty id, name, spec, unit, stock, expireDate, remark;
        public MedicineRow(String id, String name, String spec, String unit, String stock, String expireDate, String remark) {
            this.id = new SimpleStringProperty(id); this.name = new SimpleStringProperty(name); this.spec = new SimpleStringProperty(spec);
            this.unit = new SimpleStringProperty(unit); this.stock = new SimpleStringProperty(stock); this.expireDate = new SimpleStringProperty(expireDate);
            this.remark = new SimpleStringProperty(remark);
        }
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty specProperty() { return spec; }
        public SimpleStringProperty unitProperty() { return unit; }
        public SimpleStringProperty stockProperty() { return stock; }
        public SimpleStringProperty expireDateProperty() { return expireDate; }
        public SimpleStringProperty remarkProperty() { return remark; }
    }
}
