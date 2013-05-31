import java.util.Arrays;
import java.util.Iterator;

import org.wsj.IpOptAdjointOptimizer;

import dataStructures.Numerical;
import dta_solver.SO_Optimizer;
import dta_solver.Simulator;

public class DTASolver {

  /**
   * @param args
   */
  public static void main(String[] args) {

    boolean debug = true;
    String network_file = "graphs/parallelPath.json";
    String data_file = "graphs/parallelPathData.json";
    // String network_file = "graphs/PathWithPriorities.json";
    // String data_file = "graphs/PathWithPrioritiesData.json";

    Simulator simulator = new Simulator(network_file, data_file, debug);
    simulator.run();

    // TestSimulation.registerParallelPath();
    // Test2x1JunctionSolver.createProfile();
    // Test2x1JunctionSolver.register2x1Junction();

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(new IpOptAdjointOptimizer(),
        maxIter, simulator);
    System.out.println(Arrays.toString(optimizer.getControl()));
    System.out.println(
        optimizer.djdx(simulator, optimizer.getControl()).toString());

    System.out.println(Arrays.toString(optimizer.djdu(simulator,
        optimizer.getControl()).toArray()));
    
    double[] tmp = optimizer.djdu(simulator,
        optimizer.getControl()).toArray();
    for (int i = 0; i < tmp.length; i++)
      assert Numerical.validNumber(tmp[i]);
    
    System.out.println(optimizer
        .dhdx(simulator, optimizer.getControl())
        .toString());
    
    /* Checking of J */
    assert Numerical.validNumber(optimizer.objective(simulator, optimizer.getControl()));
    
    /* Checking of dH/dX */
    double[][] tmp2 = optimizer
        .dhdx(simulator, optimizer.getControl()).get().toArray();
    for (int i = 0; i < tmp2.length; i++)
      for (int j = 0; j < tmp2[0].length; j++)
      assert Numerical.validNumber(tmp2[i][j]);
    
    /* Checking of dH/dU */
    tmp2 = optimizer
        .dhdu(simulator, optimizer.getControl()).get().toArray();
    for (int i = 0; i < tmp2.length; i++)
      for (int j = 0; j < tmp2[0].length; j++)
      assert Numerical.validNumber(tmp2[i][j]);
    
    /* Checking of dJ/dU */
    tmp = optimizer
        .djdu(simulator, optimizer.getControl()).toArray();
    for (int i = 0; i < tmp.length; i++)
      assert Numerical.validNumber(tmp[i]);
    
    /* Checking of dJ/dX */
    tmp = optimizer
        .djdx(simulator, optimizer.getControl()).toArray();
    for (int i = 0; i < tmp.length; i++)
      assert Numerical.validNumber(tmp[i]);
    
    System.out.println("Checking done.");
   // if (a == Double.POSITIVE_INFINITY || a == Double.NEGATIVE_INFINITY)
    // System.out.println("Infinity detected");
     optimizer.solve(optimizer.getControl());
  }
}