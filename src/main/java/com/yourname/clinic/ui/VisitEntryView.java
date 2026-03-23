package com.yourname.clinic.ui;

import com.yourname.clinic.model.OptionItem;
import com.yourname.clinic.service.ClinicDbService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VisitEntryView extends VBox {
    private final ClinicDbService db = new ClinicDbService();

    private final ComboBox<OptionItem> patientBox = new ComboBox<>();
    private final DatePicker visitDate = new DatePicker();
    private final TextField complaintField = new TextField();
    private final TextArea diagnosisArea = new TextArea();
    private final TableView<RowItem> prescriptionTable = new TableView<>();
    private final ObservableList<RowItem> prescriptionRows = FXCollections.observableArrayList();
    private final Label totalAmountLabel = new Label("总金额：0.00");

    private final TextField qPatientField = new TextField();
    private final TextField qDateField = new TextField();
    private final TextField qDiagnosisField = new TextField();
    private final TableView<VisitRow> visitTable = new TableView<>();
    private final ObservableList<VisitRow> visitRows = FXCollections.observableArrayList();
    private final ObservableList<VisitRow> visitPageRows = FXCollections.observableArrayList();
    private final Label visitPageLabel = new Label();
    private int visitCurrentPage = 1;
    private int visitPageSize = 10;

    public VisitEntryView() {
        setPadding(new Insets(12));
        setSpacing(12);

        initInputSection();
        initQuerySection();
        reloadVisitQuery();
    }

    public void refreshReferenceOptions() {
        patientBox.getItems().setAll(db.listPatientOptions());
    }

    private void initInputSection() {
        patientBox.getItems().setAll(db.listPatientOptions());
        patientBox.setPromptText("请选择患者");
        visitDate.setValue(LocalDate.now());
        complaintField.setPromptText("主诉");
        diagnosisArea.setPromptText("诊断（多行）");
        diagnosisArea.setPrefRowCount(3);

        initPrescriptionTable();

        Button addRow = new Button("添加处方行");
        Button delRow = new Button("删除选中行");
        Button save = new Button("保存就诊");
        Button refreshOptionsBtn = new Button("刷新患者/药品");

        addRow.setOnAction(e -> addPrescriptionDialog());
        delRow.setOnAction(e -> {
            RowItem selected = prescriptionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                prescriptionRows.remove(selected);
                refreshTotal();
            }
        });
        save.setOnAction(e -> saveVisit());
        refreshOptionsBtn.setOnAction(e -> patientBox.getItems().setAll(db.listPatientOptions()));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("患者"), patientBox, new Label("就诊日期"), visitDate);
        form.addRow(1, new Label("主诉"), complaintField);
        form.addRow(2, new Label("诊断"), diagnosisArea);

        HBox actions = new HBox(8, addRow, delRow, save, refreshOptionsBtn);

        Label title = new Label("就诊记录 + 处方录入（SQLite 持久化）");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        getChildren().addAll(title, form, prescriptionTable, totalAmountLabel, actions, new Separator());
    }

    private void initQuerySection() {
        qPatientField.setPromptText("按患者名模糊查询");
        qDateField.setPromptText("按日期模糊查询，例如 2026-03");
        qDiagnosisField.setPromptText("按诊断模糊查询");

        Button queryBtn = new Button("查询");
        Button resetBtn = new Button("重置");
        Button detailBtn = new Button("查看详情");
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");

        queryBtn.setOnAction(e -> applyVisitFilter());
        resetBtn.setOnAction(e -> {
            qPatientField.clear();
            qDateField.clear();
            qDiagnosisField.clear();
            reloadVisitQuery();
        });
        detailBtn.setOnAction(e -> showVisitDetail());
        prevBtn.setOnAction(e -> { if (visitCurrentPage > 1) { visitCurrentPage--; refreshVisitPage(); }});
        nextBtn.setOnAction(e -> { if (visitCurrentPage < visitTotalPages()) { visitCurrentPage++; refreshVisitPage(); }});

        initVisitTable();

        HBox queryBar = new HBox(8,
                new Label("患者"), qPatientField,
                new Label("日期"), qDateField,
                new Label("诊断"), qDiagnosisField,
                queryBtn, resetBtn, detailBtn
        );

        Label title = new Label("就诊查询（按人/日期/诊断模糊查询）");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        getChildren().addAll(title, queryBar, visitTable, new HBox(8, prevBtn, nextBtn, visitPageLabel));
    }

    private void initPrescriptionTable() {
        prescriptionTable.getColumns().add(colRx("药品ID", RowItem::medicineIdProperty, 100));
        prescriptionTable.getColumns().add(colRx("药品", RowItem::medicineLabelProperty, 200));
        prescriptionTable.getColumns().add(colRx("数量", RowItem::qtyProperty, 100));
        prescriptionTable.getColumns().add(colRx("单位", RowItem::unitProperty, 90));
        prescriptionTable.getColumns().add(colRx("单价", RowItem::priceProperty, 100));
        prescriptionTable.getColumns().add(colRx("金额", RowItem::amountProperty, 120));
        prescriptionTable.getColumns().add(colRx("用法", RowItem::usageProperty, 180));
        prescriptionTable.setItems(prescriptionRows);
        prescriptionTable.setPrefHeight(250);
    }

    private void initVisitTable() {
        visitTable.getColumns().add(colVisit("患者", VisitRow::patientProperty, 180));
        visitTable.getColumns().add(colVisit("日期", VisitRow::dateProperty, 130));
        visitTable.getColumns().add(colVisit("主诉", VisitRow::complaintProperty, 180));
        visitTable.getColumns().add(colVisit("诊断", VisitRow::diagnosisProperty, 220));
        visitTable.getColumns().add(colVisit("处方条数", VisitRow::rxCountProperty, 90));
        visitTable.getColumns().add(colVisit("总金额", VisitRow::totalAmountProperty, 100));
        visitTable.setItems(visitPageRows);
        visitTable.setPrefHeight(230);
    }

    private TableColumn<RowItem, String> colRx(String title,
                                               java.util.function.Function<RowItem, SimpleStringProperty> getter,
                                               int width) {
        TableColumn<RowItem, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> getter.apply(d.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private TableColumn<VisitRow, String> colVisit(String title,
                                                   java.util.function.Function<VisitRow, SimpleStringProperty> getter,
                                                   int width) {
        TableColumn<VisitRow, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> getter.apply(d.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private void addPrescriptionDialog() {
        Dialog<RowItem> dialog = new Dialog<>();
        dialog.setTitle("添加处方行");

        ComboBox<OptionItem> medicineBox = new ComboBox<>();
        medicineBox.getItems().setAll(db.listMedicineOptions());
        medicineBox.setPrefWidth(300);

        TextField qty = new TextField("1");
        TextField unit = new TextField("盒");
        TextField price = new TextField("0");
        TextArea usage = new TextArea();
        usage.setPromptText("用法（多行）");
        usage.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);
        gp.addRow(0, new Label("药品"), medicineBox);
        gp.addRow(1, new Label("数量"), qty);
        gp.addRow(2, new Label("单位"), unit);
        gp.addRow(3, new Label("单价"), price);
        gp.addRow(4, new Label("用法"), usage);

        dialog.getDialogPane().setContent(gp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            OptionItem med = medicineBox.getValue();
            if (med == null) return null;
            try {
                BigDecimal q = new BigDecimal(qty.getText().trim());
                BigDecimal p = new BigDecimal(price.getText().trim());
                if (q.signum() <= 0) {
                    new Alert(Alert.AlertType.WARNING, "数量必须大于0").showAndWait();
                    return null;
                }
                BigDecimal amount = q.multiply(p);
                double dbStock = db.getMedicineStock(med.id());
                double currentUsed = usedInCurrentPrescription(med.id());
                double remain = dbStock - currentUsed;
                if (remain < q.doubleValue()) {
                    new Alert(Alert.AlertType.WARNING,
                            "库存不足，药品: " + med.label() + "\\n当前库存: " + dbStock + "\\n已占用: " + currentUsed + "\\n可用: " + remain)
                            .showAndWait();
                    return null;
                }
                return new RowItem(
                        med.id().toString(), med.label(),
                        q.stripTrailingZeros().toPlainString(), unit.getText().trim(),
                        p.stripTrailingZeros().toPlainString(), amount.stripTrailingZeros().toPlainString(), usage.getText().trim()
                );
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "数量/单价格式错误").showAndWait();
                return null;
            }
        });

        dialog.showAndWait().ifPresent(item -> {
            prescriptionRows.add(item);
            refreshTotal();
        });
    }

    private void refreshTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (RowItem r : prescriptionRows) {
            try { total = total.add(new BigDecimal(r.amountProperty().get())); } catch (Exception ignored) {}
        }
        totalAmountLabel.setText("总金额：" + total.stripTrailingZeros().toPlainString());
    }

    private void saveVisit() {
        if (patientBox.getValue() == null) { new Alert(Alert.AlertType.WARNING, "请选择患者").showAndWait(); return; }
        if (visitDate.getValue() == null) { new Alert(Alert.AlertType.WARNING, "请选择就诊日期").showAndWait(); return; }
        if (prescriptionRows.isEmpty()) { new Alert(Alert.AlertType.WARNING, "请至少添加一条处方").showAndWait(); return; }

        String patient = patientBox.getValue().label();
        String date = visitDate.getValue().toString();
        String complaint = complaintField.getText().trim();
        String diagnosis = diagnosisArea.getText().trim();
        int rxCount = prescriptionRows.size();
        double total = Double.parseDouble(totalAmountLabel.getText().replace("总金额：", ""));

        Map<Long, Double> usageMap = buildUsageMap();
        for (Map.Entry<Long, Double> e : usageMap.entrySet()) {
            double stock = db.getMedicineStock(e.getKey());
            if (stock < e.getValue()) {
                new Alert(Alert.AlertType.WARNING,
                        "保存失败，库存不足。药品ID=" + e.getKey() + "，当前库存=" + stock + "，需要=" + e.getValue())
                        .showAndWait();
                return;
            }
        }

        String detail = buildPrescriptionDetail();
        db.consumeMedicineStock(usageMap);
        db.insertVisit(patient, date, complaint, diagnosis, rxCount, total, detail);
        reloadVisitQuery();

        patientBox.setValue(null);
        visitDate.setValue(LocalDate.now());
        complaintField.clear();
        diagnosisArea.clear();
        prescriptionRows.clear();
        totalAmountLabel.setText("总金额：0.00");

        new Alert(Alert.AlertType.INFORMATION, "保存成功").showAndWait();
    }

    private void reloadVisitQuery() {
        visitRows.clear();
        for (String[] r : db.queryVisits("", "", "")) {
            visitRows.add(new VisitRow(r[0], r[1], r[2], r[3], r[4], trimDouble(r[5]), r[6], r[7]));
        }
        visitCurrentPage = 1;
        refreshVisitPage();
    }

    private void applyVisitFilter() {
        String patientKey = normalize(qPatientField.getText());
        String dateKey = normalize(qDateField.getText());
        String diagnosisKey = normalize(qDiagnosisField.getText());

        visitRows.clear();
        for (String[] r : db.queryVisits(patientKey, dateKey, diagnosisKey)) {
            visitRows.add(new VisitRow(r[0], r[1], r[2], r[3], r[4], trimDouble(r[5]), r[6], r[7]));
        }
        visitCurrentPage = 1;
        refreshVisitPage();
    }

    private int visitTotalPages() {
        return Math.max(1, (int) Math.ceil((double) visitRows.size() / visitPageSize));
    }

    private void refreshVisitPage() {
        int totalPages = visitTotalPages();
        if (visitCurrentPage > totalPages) visitCurrentPage = totalPages;
        int from = (visitCurrentPage - 1) * visitPageSize;
        int to = Math.min(from + visitPageSize, visitRows.size());
        visitPageRows.setAll(from >= to ? java.util.List.of() : visitRows.subList(from, to));
        visitPageLabel.setText("第 " + visitCurrentPage + "/" + totalPages + " 页，共 " + visitRows.size() + " 条");
    }

    private double usedInCurrentPrescription(long medicineId) {
        double used = 0;
        for (RowItem r : prescriptionRows) {
            try {
                if (Long.parseLong(r.medicineIdProperty().get()) == medicineId) {
                    used += Double.parseDouble(r.qtyProperty().get());
                }
            } catch (Exception ignored) {}
        }
        return used;
    }

    private Map<Long, Double> buildUsageMap() {
        Map<Long, Double> map = new HashMap<>();
        for (RowItem r : prescriptionRows) {
            long mid = Long.parseLong(r.medicineIdProperty().get());
            double qty = Double.parseDouble(r.qtyProperty().get());
            map.put(mid, map.getOrDefault(mid, 0.0) + qty);
        }
        return map;
    }

    private String buildPrescriptionDetail() {
        StringBuilder sb = new StringBuilder();
        sb.append("诊断:\n").append(diagnosisArea.getText()).append("\n\n");
        sb.append("处方明细:\n");
        for (RowItem r : prescriptionRows) {
            sb.append("- 药品: ").append(r.medicineLabelProperty().get())
                    .append(", 数量: ").append(r.qtyProperty().get())
                    .append(", 单位: ").append(r.unitProperty().get())
                    .append(", 单价: ").append(r.priceProperty().get())
                    .append(", 金额: ").append(r.amountProperty().get())
                    .append("\n  用法:\n").append(r.usageProperty().get())
                    .append("\n");
        }
        return sb.toString();
    }

    private void showVisitDetail() {
        VisitRow selected = visitTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "请先选择一条就诊记录").showAndWait();
            return;
        }
        String detailText = selected.detailProperty().get() == null ? "" : selected.detailProperty().get().replace("\\n", "\n");
        String content = "患者: " + selected.patientProperty().get() + "\n"
                + "日期: " + selected.dateProperty().get() + "\n"
                + "主诉: " + selected.complaintProperty().get() + "\n"
                + "诊断:\n" + selected.diagnosisProperty().get().replace("\\n", "\n") + "\n\n"
                + "处方条数: " + selected.rxCountProperty().get() + "\n"
                + "总金额: " + selected.totalAmountProperty().get() + "\n\n"
                + "详细信息:\n" + detailText;

        TextArea ta = new TextArea(content);
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefRowCount(20);
        ta.setPrefColumnCount(80);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("就诊详情");
        dialog.getDialogPane().setContent(ta);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private String trimDouble(String s) {
        try {
            return BigDecimal.valueOf(Double.parseDouble(s)).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return s;
        }
    }

    public static class RowItem {
        private final SimpleStringProperty medicineId, medicineLabel, qty, unit, price, amount, usage;
        public RowItem(String medicineId, String medicineLabel, String qty, String unit, String price, String amount, String usage) {
            this.medicineId = new SimpleStringProperty(medicineId);
            this.medicineLabel = new SimpleStringProperty(medicineLabel);
            this.qty = new SimpleStringProperty(qty);
            this.unit = new SimpleStringProperty(unit);
            this.price = new SimpleStringProperty(price);
            this.amount = new SimpleStringProperty(amount);
            this.usage = new SimpleStringProperty(usage);
        }
        public SimpleStringProperty medicineIdProperty() { return medicineId; }
        public SimpleStringProperty medicineLabelProperty() { return medicineLabel; }
        public SimpleStringProperty qtyProperty() { return qty; }
        public SimpleStringProperty unitProperty() { return unit; }
        public SimpleStringProperty priceProperty() { return price; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty usageProperty() { return usage; }
    }

    public static class VisitRow {
        private final SimpleStringProperty id, patient, date, complaint, diagnosis, rxCount, totalAmount, detail;
        public VisitRow(String id, String patient, String date, String complaint, String diagnosis,
                        String rxCount, String totalAmount, String detail) {
            this.id = new SimpleStringProperty(id);
            this.patient = new SimpleStringProperty(patient);
            this.date = new SimpleStringProperty(date);
            this.complaint = new SimpleStringProperty(complaint);
            this.diagnosis = new SimpleStringProperty(diagnosis);
            this.rxCount = new SimpleStringProperty(rxCount);
            this.totalAmount = new SimpleStringProperty(totalAmount);
            this.detail = new SimpleStringProperty(detail);
        }
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty patientProperty() { return patient; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty complaintProperty() { return complaint; }
        public SimpleStringProperty diagnosisProperty() { return diagnosis; }
        public SimpleStringProperty rxCountProperty() { return rxCount; }
        public SimpleStringProperty totalAmountProperty() { return totalAmount; }
        public SimpleStringProperty detailProperty() { return detail; }
    }
}
