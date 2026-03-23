package com.yourname.clinic.ui;

import com.yourname.clinic.db.ConnectionManager;
import com.yourname.clinic.service.ClinicDbService;
import com.yourname.clinic.util.DbBackupUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

public class MainShellView extends BorderPane {

    private final Label statusBar = new Label("就绪");
    private final ClinicDbService db = new ClinicDbService();
    private final VisitEntryView visitView = new VisitEntryView();

    public MainShellView() {
        setPadding(new Insets(8));

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(tab("患者管理", new PatientView()));
        Tab visitTab = tab("就诊+处方", visitView);
        tabPane.getTabs().add(visitTab);
        tabPane.getTabs().add(tab("库存管理", new InventoryView()));
        tabPane.getTabs().add(tab("收支管理", new FinanceView()));
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == visitTab) {
                visitView.refreshReferenceOptions();
            }
        });

        Button backupBtn = new Button("备份数据库");
        Button restoreBtn = new Button("恢复数据库");
        Button debugBtn = new Button("数据库诊断");

        backupBtn.setOnAction(e -> backupDb());
        restoreBtn.setOnAction(e -> restoreDb());
        debugBtn.setOnAction(e -> showDbDebugInfo());

        HBox top = new HBox(10, backupBtn, restoreBtn, debugBtn);
        top.setPadding(new Insets(0, 0, 8, 0));

        HBox bottom = new HBox(statusBar);
        bottom.setPadding(new Insets(8, 0, 0, 0));

        setTop(top);
        setCenter(tabPane);
        setBottom(bottom);
    }

    private Tab tab(String title, javafx.scene.Node node) {
        Tab t = new Tab(title, node);
        t.setClosable(false);
        return t;
    }

    private void backupDb() {
        try {
            Path out = DbBackupUtil.backupNow(ConnectionManager.getDbPath(), Path.of("backup"));
            statusBar.setText("备份完成: " + out.toAbsolutePath());
            info("备份成功", out.toAbsolutePath().toString());
        } catch (Exception ex) {
            error("备份失败", ex.getMessage());
        }
    }

    private void restoreDb() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择备份数据库文件");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DB 文件", "*.db"));
        File selected = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (selected == null) return;

        try {
            DbBackupUtil.restore(selected.toPath(), ConnectionManager.getDbPath());
            statusBar.setText("恢复完成: " + selected.getAbsolutePath());
            info("恢复成功", "已恢复数据库，请重启程序以确保数据全量刷新。");
        } catch (Exception ex) {
            error("恢复失败", ex.getMessage());
        }
    }

    private void showDbDebugInfo() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DB路径: ").append(ConnectionManager.getDbPath().toAbsolutePath()).append("\\n");
            sb.append("患者总数: ").append(db.countPatients()).append("\\n");
            sb.append("最近5条患者:\\n");
            for (String[] p : db.recentPatients(5)) {
                sb.append(" - ID=").append(p[0])
                        .append(", 姓名=").append(p[1])
                        .append(", 手机=").append(p[2])
                        .append(", 出生日期=").append(p[3])
                        .append("\\n");
            }
            info("数据库诊断信息", sb.toString());
        } catch (Exception ex) {
            error("数据库诊断失败", ex.getMessage());
        }
    }

    private void info(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private void error(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
