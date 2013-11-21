package jUnit;

import static org.junit.Assert.*;

import java.io.Writer;
import org.junit.Test;
import com.google.gson.Gson;

import dataStructures.Numerical;
import dta_solver.SO_Optimizer;
import dta_solver.Simulator;
import generalNetwork.state.State;
import io.InputOutput;

public class TestMinimalNetwork {

  @Test
  public void test() {

    double alpha = 0.1;
    boolean debug = true;
    String network_file = "JUnitTests/minimalNetwork.json";
    String data_file = "JUnitTests/minimalNetworkData.json";
    String dhdx_file = "JUnitTests/dHdX";
    String control_file = "JUnitTests/Control";
    String final_control_file = "JUnitTests/FinalControl";

    Gson gson = new Gson();

    Simulator simulator = new Simulator(network_file, data_file, alpha, debug);
    State state = simulator.partialRun(true);

    int maxIter = 10;
    SO_Optimizer optimizer = new SO_Optimizer(maxIter, simulator);

    optimizer.printSizes();

    /* Checking of the control */
    double[] control = optimizer.getControl();
    InputOutput.tableToFile(control, control_file);

    /* Checking of dH/dX */
    double[][] dhdx = optimizer
        .dhdx(state, optimizer.getControl())
        .toArray();

    assertTrue("dH/dX should be a not singular lower triangular matrix",
        Numerical.NonSingularLowerTriangular(dhdx));

    InputOutput.tableToFile(dhdx, "dHdX");

    Writer writer = InputOutput.Writer(dhdx_file);
    gson.toJson(dhdx, writer);
    InputOutput.close(writer);

    /*
     * System.out.println(optimizer
     * .dhdx(state, optimizer.getControl())
     * .get()
     * .toString());
     */
    double[] final_control = optimizer.solve();

    /* Study of the last computed state */
    State final_state = optimizer.forwardSimulate(final_control, true);
    InputOutput.tableToFile(final_control, final_control_file);
    optimizer.printProperties(final_state);
  }
}