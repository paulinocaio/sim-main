package io.sim.Project;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class GraphPlotter {

    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private double[] measures1;
    private double[] measures2;

    public GraphPlotter(String title, String xAxisLabel, String yAxisLabel, double[] measures1, double[] measures2) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.measures1 = measures1;
        this.measures2 = measures2;
    }

    public void plotGraph() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(createChartPanel());
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private JPanel createChartPanel() {
        XYSeries series1 = new XYSeries("Medidas 1");
        XYSeries series2 = new XYSeries("Medidas 2");

        for (int i = 0; i < Math.min(measures1.length, measures2.length); i++) {
            series1.add(i, measures1[i]);
            series2.add(i, measures2[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return new ChartPanel(chart);
    }
}
