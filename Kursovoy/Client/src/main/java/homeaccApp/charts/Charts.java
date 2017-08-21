package homeaccApp.charts;

import homeaccApp.api.DAO.CategoryDAO;
import homeaccApp.api.DAO.CostsIncomesDAO;
import homeaccApp.categories.Categories;
import homeaccApp.mainwindow.costsincomes.Costincomes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vellial on 22.09.16.
 */
public class Charts {

    public static void showChart(boolean costinc, String title) {
        try {
            ObservableList<Categories> categories = CategoryDAO.selectCategories(costinc);
            ObservableList<Costincomes> costincomes = CostsIncomesDAO.selectCostsIncomesData(costinc);

            ObservableList<PieChart.Data> categoriesPieData =
                    FXCollections.observableArrayList();

            int catId;
            for (Categories cat : categories) {
                double catSum = 0;
                catId = cat.getCategoryId();
                for (Costincomes costincel : costincomes) {
                    int costCat = costincel.getCostincCategory().getCategoryId();
                    if (catId == costCat) {
                        catSum += costincel.getCostincAmount();
                    }
                }
                categoriesPieData.add(new PieChart.Data(cat.getCategoryName(), catSum));
            }

            // Create window with diagram
            Stage stage = new Stage();
            Scene scene = new Scene(new Group());
            stage.setTitle(title);
            stage.setWidth(500);
            stage.setHeight(500);
            final PieChart chart = new PieChart(categoriesPieData);
            chart.setTitle(title);

            ((Group) scene.getRoot()).getChildren().add(chart);
            stage.setScene(scene);
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
