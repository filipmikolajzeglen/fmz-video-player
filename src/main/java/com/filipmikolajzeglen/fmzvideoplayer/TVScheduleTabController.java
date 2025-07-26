package com.filipmikolajzeglen.fmzvideoplayer;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TVScheduleTabController
{
    @FXML
    private VBox tvScheduleContent;
    @FXML
    private CheckBox useCustomScheduleCheckBox;
    @FXML
    private ComboBox<String> seriesComboBox;
    @FXML
    private ListView<String> scheduleListView;
    @FXML
    private GridPane scheduleGridPane;
    @FXML
    private HBox scheduleButtonsHBox;

    @Setter
    private QuickStartTabController quickStartTabController;

    @FXML
    public void initialize()
    {
        // Dodaj referencję do kontrolera w properties VBox
        tvScheduleContent.getProperties().put("controller", this);

        // Ustaw powiązania disable
        scheduleGridPane.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
        scheduleListView.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
        scheduleButtonsHBox.disableProperty().bind(useCustomScheduleCheckBox.selectedProperty().not());
    }

   public void updateSeriesComboBox() {
        if (quickStartTabController == null) return;
        List<SeriesInfo> seriesList = quickStartTabController.getSeriesFolders(quickStartTabController.getVideoMainSourcePath());
        seriesComboBox.getItems().clear();
        for (SeriesInfo series : seriesList) {
            seriesComboBox.getItems().add(series.getName());
        }
    }

    @FXML
    private void onAddSeriesToSchedule()
    {
        String selectedSeries = seriesComboBox.getValue();
        if (selectedSeries != null && !selectedSeries.isEmpty())
        {
            scheduleListView.getItems().add(selectedSeries);
        }
    }

    @FXML
    private void onMoveSeriesUp()
    {
        int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0)
        {
            ObservableList<String> items = scheduleListView.getItems();
            String item = items.remove(selectedIndex);
            items.add(selectedIndex - 1, item);
            scheduleListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    private void onMoveSeriesDown()
    {
        int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
        ObservableList<String> items = scheduleListView.getItems();
        if (selectedIndex != -1 && selectedIndex < items.size() - 1)
        {
            String item = items.remove(selectedIndex);
            items.add(selectedIndex + 1, item);
            scheduleListView.getSelectionModel().select(selectedIndex + 1);
        }
    }

    @FXML
    private void onRemoveSeriesFromSchedule()
    {
        int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1)
        {
            scheduleListView.getItems().remove(selectedIndex);
        }
    }
}
