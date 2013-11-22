package dtapc;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dtapc.optimization.GradientDescent;
import dtapc.optimization.GradientDescentMethod;
import dtapc.generalNetwork.graph.DisplayGUI;
import dtapc.generalNetwork.state.State;
import dtapc.graphics.GUI;

import dtapc.dta_solver.SOPC_Optimizer;
import dtapc.dta_solver.SO_OptimizerByFiniteDifferences;
import dtapc.dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // EditorGUI e = new EditorGUI();
    // reportExample();
    complexExample();
    /*
     * System.out.println("*****************************************");
     * System.out.println(" Gradient descent by finite differences  ");
     * System.out.println("*****************************************");
     * long startTime = System.currentTimeMillis();
     * optimizationExampleByFiniteDifferences();
     * long endTime = System.currentTimeMillis();
     * long searchTime = endTime - startTime;
     * System.out.println("Time (ms): " + searchTime);
     */
    /*
     * System.out.println("*****************************************");
     * System.out.println(" Gradient descent by the adjoint method  ");
     * System.out.println("*****************************************");
     * long startTime = System.currentTimeMillis();
     * optimizationExampleWithHomeMadeGradient();
     * long endTime = System.currentTimeMillis();
     * long searchTime = endTime - startTime;
     * System.out.println("Time (ms): " + searchTime);
     */
    // printExample();
  }

  public static void printExample() {
    String network_file = "graphs/drawing.json";
    String data_file = "graphs/drawingData.json";

    Simulator simulator = new Simulator(network_file, data_file, 0.9, true);

    State s = simulator.partialRun(true);

    DisplayGUI e = new DisplayGUI(simulator);
    e.displayState(s.get(15));
  }

  public static void optimizationExampleWithHomeMadeGradient() {
    /* Share of the compliant flow */
    double alpha = 1;
    boolean debug = false;
    // String network_file = "graphs/TwoParallelPath.json";
    // String data_file = "graphs/TwoParallelPathData.json";
    String network_file = "JUnitTests/2x1JunctionNetwork.json";
    String data_file = "JUnitTests/2x1JunctionNetworkData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 150;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void optimizationExampleByFiniteDifferences() {
    /* Share of the compliant flow */
    double alpha = 1;
    String network_file = "graphs/TwoParallelPath.json";
    String data_file = "graphs/TwoParallelPathData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, false);

    int maxIter = 50;
    SO_OptimizerByFiniteDifferences optimizer = new SO_OptimizerByFiniteDifferences(
        simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void complexExample() {
    /* Share of the compliant flow */
    double alpha = 1;
    String network_file = "graphs/ComplexNetwork.json";
    String data_file = "graphs/ComplexNetworkData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, true);

    int maxIter = 80;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescent homemade_test = new GradientDescent(maxIter);
    homemade_test.setGradient_condition(10E-9);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);
  }

  public static void reportExample() {
    System.out.println("*****************************************");
    System.out.println("   Optimization by the adjoint method    ");
    System.out.println("*****************************************");

    /* Share of the compliant flow */
    double alpha = 1;
    boolean debug = false;
    String network_file = "graphs/ReportExample.json";
    String data_file = "graphs/ReportExampleData.json";

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);

    int maxIter = 70;
    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

    GradientDescentMethod homemade_test = new GradientDescent(maxIter);
    double[] result = homemade_test.solve(optimizer);
    System.out.println("Final control");
    for (int i = 0; i < result.length; i++)
      System.out.println(result[i]);

    JFreeChart display = ((GradientDescent) homemade_test).getChart();
    GUI g = new GUI();
    ChartPanel chartPanel = new ChartPanel(display);
    g.setContentPane(chartPanel);
    g.setVisible(true);
  }
}