package com.yourname.clinic.ui;

import com.yourname.clinic.model.FinanceRow;
import com.yourname.clinic.service.ClinicDbService;
import com.yourname.clinic.util.CsvUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceView extends VBox {
    private final ClinicDbService service = new ClinicDbService();
    private final ObservableList<FinanceRowView> rows = FXCollections.observableArrayList();
    private final ObservableList<FinanceRowView> pageRows = FXCollections.observableArrayList();
    private final TableView<FinanceRowView> table = new TableView<>();

    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final DatePicker fromDate = new DatePicker();
    private final DatePicker toDate = new DatePicker();
    private final Label pageLabel = new Label();
    private int currentPage = 1;
    private int pageSize = 10;

    public FinanceView() {
        setPadding(new Insets(12));
        setSpacing(10);

        typeFilter.getItems().setAll("ALL", "INCOME", "EXPENSE");
        typeFilter.setValue("ALL");

        Button queryBtn = new Button("查询");
        Button refreshBtn = new Button("重置");
        Button exportBtn = new Button("导出 CSV");
        Button prevBtn = new Button("上一页");
        Button nextBtn = new Button("下一页");

        queryBtn.setOnAction(e -> applyFilter());
        refreshBtn.setOnAction(e -> {
            typeFilter.setValue("ALL");
            fromDate.setValue(null);
            toDate.setValue(null);
            loadAll();
        });
        exportBtn.setOnAction(e -> exportCsv());
        prevBtn.setOnAction(e -> { if (currentPage > 1) { currentPage--; refreshPage(); }});
        nextBtn.setOnAction(e -> { if (currentPage < totalPages()) { currentPage++; refreshPage(); }});

        initTable();
        loadAll();

        HBox filters = new HBox(8,
                new Label("类型"), typeFilter,
                new Label("从"), fromDate,
                new Label("到"), toDate,
                queryBtn, refreshBtn, exportBtn
        );

        getChildren().addAll(new Label("收支管理"), filters, table, new HBox(8, prevBtn, nextBtn, pageLabel));
    }

    private void initTable() {
        table.getColumns().add(col("ID", FinanceRowView::idProperty, 70));
        table.getColumns().add(col("流水号", FinanceRowView::txnNoProperty, 170));
        table.getColumns().add(col("类型", FinanceRowView::typeProperty, 100));
        table.getColumns().add(col("分类", FinanceRowView::categoryProperty, 130));
        table.getColumns().add(col("金额", FinanceRowView::amountProperty, 100));
        table.getColumns().add(col("日期", FinanceRowView::dateProperty, 120));
        table.getColumns().add(col("支付方式", FinanceRowView::payMethodProperty, 100));
        table.getColumns().add(col("对方", FinanceRowView::counterpartyProperty, 120));
        table.getColumns().add(col("备注", FinanceRowView::remarkProperty, 160));
        table.setItems(pageRows);
        table.setPrefHeight(520);
    }

    private TableColumn<FinanceRowView, String> col(String title,
                                                    java.util.function.Function<FinanceRowView, SimpleStringProperty> getter,
                                                    int width) {
        TableColumn<FinanceRowView, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> getter.apply(d.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private void loadAll() {
        rows.clear();
        for (FinanceRow r : service.listFinanceRows()) {
            rows.add(new FinanceRowView(
                    r.id(), r.txnNo(), r.type(), r.category(), r.amount(), r.date(),
                    r.payMethod(), r.counterparty(), r.remark()
            ));
        }
        currentPage = 1;
        refreshPage();
    }

    private void applyFilter() {
        String t = typeFilter.getValue();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        List<FinanceRowView> filtered = new ArrayList<>();
        for (FinanceRow r : service.listFinanceRows()) {
            if (!"ALL".equals(t) && !r.type().equals(t)) continue;

            LocalDate d = LocalDate.parse(r.date());
            if (from != null && d.isBefore(from)) continue;
            if (to != null && d.isAfter(to)) continue;

            filtered.add(new FinanceRowView(
                    r.id(), r.txnNo(), r.type(), r.category(), r.amount(), r.date(),
                    r.payMethod(), r.counterparty(), r.remark()
            ));
        }

        rows.setAll(filtered);
        currentPage = 1;
        refreshPage();
    }

    private void exportCsv() {
        try {
            List<List<String>> outRows = rows.stream().map(r -> List.of(
                    r.idProperty().get(),
                    r.txnNoProperty().get(),
                    r.typeProperty().get(),
                    r.categoryProperty().get(),
                    r.amountProperty().get(),
                    r.dateProperty().get(),
                    r.payMethodProperty().get(),
                    r.counterpartyProperty().get(),
                    r.remarkProperty().get()
            )).toList();

            Path out = Path.of("exports", "finance_" + LocalDate.now() + ".csv");
            CsvUtil.writeCsv(out,
                    List.of("ID", "流水号", "类型", "分类", "金额", "日期", "支付方式", "对方", "备注"),
                    outRows
            );
            new Alert(Alert.AlertType.INFORMATION, "导出成功: " + out.toAbsolutePath()).showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "导出失败: " + ex.getMessage()).showAndWait();
        }
    }

    private int totalPages() {
        return Math.max(1, (int) Math.ceil((double) rows.size() / pageSize));
    }

    private void refreshPage() {
        int totalPages = totalPages();
        if (currentPage > totalPages) currentPage = totalPages;
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, rows.size());
        pageRows.setAll(from >= to ? List.of() : rows.subList(from, to));
        pageLabel.setText("第 " + currentPage + "/" + totalPages + " 页，共 " + rows.size() + " 条");
    }

    public static class FinanceRowView {
        private final SimpleStringProperty id;
        private final SimpleStringProperty txnNo;
        private final SimpleStringProperty type;
        private final SimpleStringProperty category;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty date;
        private final SimpleStringProperty payMethod;
        private final SimpleStringProperty counterparty;
        private final SimpleStringProperty remark;

        public FinanceRowView(String id, String txnNo, String type, String category,
                              String amount, String date, String payMethod,
                              String counterparty, String remark) {
            this.id = new SimpleStringProperty(id);
            this.txnNo = new SimpleStringProperty(txnNo);
            this.type = new SimpleStringProperty(type);
            this.category = new SimpleStringProperty(category);
            this.amount = new SimpleStringProperty(amount);
            this.date = new SimpleStringProperty(date);
            this.payMethod = new SimpleStringProperty(payMethod);
            this.counterparty = new SimpleStringProperty(counterparty);
            this.remark = new SimpleStringProperty(remark);
        }

        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty txnNoProperty() { return txnNo; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty dateProperty() { return date; }
        public SimpleStringProperty payMethodProperty() { return payMethod; }
        public SimpleStringProperty counterpartyProperty() { return counterparty; }
        public SimpleStringProperty remarkProperty() { return remark; }
    }
}
