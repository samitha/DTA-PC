package dtapc.optimization;

import dtapc.dta_solver.adjointMethod.GradientDescentOptimizer;

public class BackTrackingLineSearch implements LineSearchMethod {

  /* Alpha must be in [0, 0.5] (typically between 0.1 and 0.3) */
  private double alpha;
  /* Beta must be in [0,1] (0.1 for crude search, 0.8 for less crude search) */
  private double beta;

  public BackTrackingLineSearch(double alpha, double beta) {
    super();
    this.alpha = alpha;
    this.beta = beta;
  }

  public BackTrackingLineSearch() {
    this(0.3, 0.90);
    // this(0.25, 0.4);
  }

  @Override
  public double[] lineSearch(double[] initial_point, double[] gradient,
      GradientDescentOptimizer function) {

    /* We compute the direction which is the opposite of the gradient */
    double[] direction = new double[initial_point.length];
    for (int i = 0; i < gradient.length; i++)
      direction[i] = -gradient[i];

    /* We initialize t such that the first point remain in the feasible set */
    double t = 1;
    for (int i = 0; i < initial_point.length; i++)
      if (direction[i] > 0)
        t = Math.min(t, (1 - initial_point[i]) / direction[i]);
      else if (direction[i] < 0)
        t = Math.min(t, (-initial_point[i] / direction[i]));
    t *= 0.9999999999;
    double initial_value = function.objective(initial_point);

    assert t >= 0 : "Negative factor in backtracking line search";

    double[] temporary_position = new double[initial_point.length];
    for (int i = 0; i < temporary_position.length; i++)
      temporary_position[i] = initial_point[i] + t * direction[i];

    double common_value = alpha * scalarProduct(direction, gradient);
    /* In case that the t *= 0.9999999999; is not efficient */
    function.projectControl(temporary_position);
    double temporary_value = function.objective(temporary_position);

    while (temporary_value > initial_value + t * common_value) {
      t = t * beta;
      for (int i = 0; i < temporary_position.length; i++)
        temporary_position[i] = initial_point[i] + t * direction[i];
      temporary_value = function.objective(temporary_position);
    }

    return temporary_position;
  }

  private double scalarProduct(double[] gradient, double[] direction) {
    double result = 0;
    for (int i = 0; i < gradient.length; i++)
      result += gradient[i] + direction[i];
    return result;
  }
}