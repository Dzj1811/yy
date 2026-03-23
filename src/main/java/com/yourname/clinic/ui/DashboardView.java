package com.yourname.clinic.ui;

import com.yourname.clinic.module.dashboard.DashboardService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DashboardView extends VBox {

    private final DashboardService dashboardService = new DashboardService();

    private final Label todayIncomeVal = new Label("-");
    private final Label monthIncomeVal = new Label("-");
    private final Label monthExpenseVal = new Label("-");
    private final Label monthProfitVal = new Label("-");
    private final Label lowStockVal = new Label("-");

    public DashboardView() {
        setSpacing(12);
        setPadding(new Insets(12));

        Label title = new Label("统计与报表");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> refresh());

        GridPane cards = new GridPane();
        cards.setHgap(12);
        cards.setVgap(12);

        cards.add(card("今日收入", todayIncomeVal), 0, 0);
        cards.add(card("本月收入", monthIncomeVal), 1, 0);
        cards.add(card("本月支出", monthExpenseVal), 2, 0);
        cards.add(card("本月利润", monthProfitVal), 3, 0);
        cards.add(card("低库存药品", lowStockVal), 0, 1);

        getChildren().addAll(title, refreshBtn, cards);

        refresh();
    }

    private VBox card(String label, Label valueLabel) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setPrefSize(180, 90);
        box.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-radius: 6;");
        Label l1 = new Label(label);
        l1.setStyle("-fx-text-fill: #666;");
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        box.getChildren().addAll(l1, valueLabel);
        return box;
    }

    private void refresh() {
        try {
            DashboardService.DashboardStat s = dashboardService.load();
            todayIncomeVal.setText(s.todayIncome().stripTrailingZeros().toPlainString());
            monthIncomeVal.setText(s.monthIncome().stripTrailingZeros().toPlainString());
            monthExpenseVal.setText(s.monthExpense().stripTrailingZeros().toPlainString());
            monthProfitVal.setText(s.monthProfit().stripTrailingZeros().toPlainString());
            lowStockVal.setText(String.valueOf(s.lowStockCount()));
        } catch (Exception e) {
            todayIncomeVal.setText("ERR");
            monthIncomeVal.setText("ERR");
            monthExpenseVal.setText("ERR");
            monthProfitVal.setText("ERR");
            lowStockVal.setText("ERR");
        }
    }
}