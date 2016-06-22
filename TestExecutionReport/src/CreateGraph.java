/**
 * Created by klewis on 6/20/2016.
 */

import com.sun.org.apache.xpath.internal.operations.Or;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreateGraph extends Application{

    public static ArrayList<HashMap<String, Double>> hashmaplist = new ArrayList<>();
    public String[] filters = {"Device", "Phase", "Priority"};

    public void start(Stage stage){
        stage.setTitle("Filter");

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        FlowPane flow = new FlowPane();


        flow.getChildren().addAll(bar(), pie());


        Scene scene = new Scene(flow);


        stage.setScene(scene);
        stage.show();
    }


    public FlowPane pie(){
        FlowPane flow = new FlowPane();
        flow.setOrientation(Orientation.VERTICAL);
        Scene scene = new Scene(flow);

        ArrayList<HashMap<String, Double>> temphml = new ArrayList<>(hashmaplist);

        for(int i = 0; i < temphml.size(); i++){


            HashMap<String, Double> temp = temphml.get(i);

            Iterator it = temp.entrySet().iterator();
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                data.add(new PieChart.Data((String)pair.getKey(), (Double)pair.getValue()));

            }
            PieChart chart = new PieChart(data);
            flow.getChildren().add(chart);


        }


        return flow;
    }

    public FlowPane bar(){

        FlowPane flow = new FlowPane();
        flow.setOrientation(Orientation.VERTICAL);
        ArrayList<HashMap<String, Double>> temphml = new ArrayList<>(hashmaplist);

        for(int i = 0; i < temphml.size(); i++){
            HashMap<String, Double> temp = temphml.get(i);

            Iterator it = temp.entrySet().iterator();
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis(0,100,10);
            BarChart<String, Number> bc = new BarChart<String, Number>(xAxis, yAxis);
            bc.setTitle(filters[i]);
            xAxis.setLabel("Filter Data");
            yAxis.setLabel("Percent");
            XYChart.Series series = new XYChart.Series();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();

                series.getData().add(new XYChart.Data(pair.getKey(), pair.getValue()));

            }
            bc.getData().add(series);
            flow.getChildren().add(bc);

        }

        Scene scene = new Scene(flow);
        return flow;
    }


    public static void setHashmaplist(ArrayList<HashMap<String, Double>> hml){
        hashmaplist = hml;
    }

}
