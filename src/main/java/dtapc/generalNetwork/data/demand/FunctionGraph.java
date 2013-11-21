package generalNetwork.data.demand;

import graphics.GUI;

import java.util.TreeSet;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dta_solver.Discretization;

/**
 * @brief Represents the graph of a function.
 *        For instance it can be used to represent the flow in a form which is
 *        independent of any variables of the problem (in particular delta_t)
 * 
 * 
 * @details To use it, first add the Points (x, f(x)) of the graph and then call
 *          the build function.
 * 
 *          For now, it is a constant piecewise function: you add points in the
 *          graph, and to know the demand at time t, it's just find the greatest
 *          time less than t being defined and attributes the same value.
 * 
 */
public class FunctionGraph {
  private TreeSet<Point> values;
  private Discretization time_discretization;

  public FunctionGraph(Discretization time) {
    this.time_discretization = time;
    values = new TreeSet<Point>(new PointComparator());
  }

  public void add(double time, double flow) {
    values.add(new Point(time, flow));
  }

  /*
   * Will be needed when we will integrate the piecewise demand public void
   * build()
   * { Iterator<Point> it = values.iterator(); Point previous = null, current
   * = null;
   * 
   * if (it.hasNext()) previous = it.next();
   * 
   * while (it.hasNext()) { current = it.next(); previous.next = current;
   * current.previous = previous; previous = current; } }
   */

  /*
   * To be fast we assume that at time delta_t * step the value will be
   * constant and being the one of the previous point
   */
  /**
   * @param step
   * @return The number of cars leaving at time step, depending of the demand
   *         graph and the delta_t
   */
  private double getCars(int step) {
    double delta_t = time_discretization.getDelta_t();
    double t1 = delta_t * step;
    Point previous = values.floor(new Point(t1, 0));
    // Point next = values.ceiling(new Point (t1, 0));
    return delta_t * previous.getY();
  }

  /**
   * @brief Build the demand in term of cars for all the time step in the
   *        Environment
   * @details It depends of delta_t and Nb_steps
   */
  public double[] buildDemand() {
    int steps = time_discretization.getNb_steps();
    double[] result = new double[steps];

    for (int i = 0; i < steps; i++)
      result[i] = getCars(i);
    return result;
  }

  XYSeries XYDensity(String name) {
    int nb_steps = time_discretization.getNb_steps();
    double delta_t = time_discretization.getDelta_t();

    XYSeries result = new XYSeries(name);

    for (int i = 0; i < nb_steps + 1; i++) {
      result.add((double) i * delta_t, getCars(i));
    }
    return result;
  }

  /**
   * @brief Display the step chart of the demand. The X axis does not work, it
   *        should be looked into.
   */
  public void display() {
    GUI g = new GUI();
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(XYDensity("Demand"));

    JFreeChart chart = ChartFactory.createXYStepChart(
        "Graph of the Demand", // chart title
        "X", // x axis label
        "Y", // y axis label
        dataset, // data
        PlotOrientation.VERTICAL, true, // include legend
        true, // tooltips
        false // urls
        );

    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
    g.setContentPane(chartPanel);
    g.setVisible(true);
  }

  @Override
  public String toString() {
    return "DemandFactory [values=" + values + "]";
  }
}
