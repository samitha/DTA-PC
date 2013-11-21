package generalNetwork.state;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import dataStructures.Numerical;

/**
 * @brief Full description of the state of a cell at a given time step.
 * @details The core information is the partial densities for all commodities in
 *          the cell. There is also the demand/supply and out/in-flows that are
 *          used to compute the next time step.
 */
public class CellInfo {

  public double supply = -1;
  public double demand = -1;
  public double total_density = 0;
  // HashMap<Commodity_id -> corresponding value>
  public LinkedHashMap<Integer, Double> partial_densities;
  public LinkedHashMap<Integer, Double> in_flows;
  public LinkedHashMap<Integer, Double> out_flows;

  /**
   * @brief Creates an empty CellInfo with no values.
   * @details The supply and demand are set to -1 by default
   */
  public CellInfo() {
    partial_densities = new LinkedHashMap<Integer, Double>();
    in_flows = new LinkedHashMap<Integer, Double>();
    out_flows = new LinkedHashMap<Integer, Double>();
  }

  /**
   * @brief Creates a CellInfo based on the partial densities given
   * @details It also set the total_density
   * @param densities
   *          The HashMap <commodity_id -> density for this commodity
   */
  public CellInfo(LinkedHashMap<Integer, Double> densities) {

    partial_densities = new LinkedHashMap<Integer, Double>(densities.size());
    in_flows = new LinkedHashMap<Integer, Double>(densities.size());
    out_flows = new LinkedHashMap<Integer, Double>(densities.size());

    partial_densities.putAll(densities);

    Iterator<Double> iterator = densities.values().iterator();
    Double pair;
    while (iterator.hasNext()) {
      pair = iterator.next();
      total_density += pair;
    }
  }

  /**
   * @brief Computes the commodities-out_flows for 1x1 junctions
   * @details The in-flows of a cell is the out-flows of the previous cell.
   *          In case of sinks, there can be several cells pouring in it and
   *          their out-flows should be added
   * @param total_in_flow
   */
  public void updateInFlows(LinkedHashMap<Integer, Double> previous_out_flows,
      boolean is_sink) {
    if (previous_out_flows.size() == 0) {
      return;
    }

    /* In case of a singleJunction sink, the flows has to add each other */
    if (is_sink) {
      Iterator<Entry<Integer, Double>> iterator = previous_out_flows
          .entrySet()
          .iterator();
      Entry<Integer, Double> entry;
      Integer commodity;
      Double previous_value;
      while (iterator.hasNext()) {
        entry = iterator.next();
        commodity = entry.getKey();
        previous_value = in_flows.get(commodity);
        if (previous_value == null)
          previous_value = 0.0;

        in_flows.put(commodity, previous_value + entry.getValue());
      }
    } else {
      in_flows.putAll(previous_out_flows);
    }

  }

  /**
   * @brief Computes the out-flow for every commodities given the total out-flow
   * @brief It applies the FIFO rule to get the partial_flows
   *        f_out(c) = density(c) / total_density *total_out_flow
   * @param total_out_flow
   *          The total amount of flow (commodity independent) leaving the cell
   */
  public void updateOutFlows(double total_out_flow) {
    if (total_out_flow == 0) {
      return;
    }

    Iterator<Entry<Integer, Double>> iterator = partial_densities
        .entrySet()
        .iterator();
    Entry<Integer, Double> entry;
    while (iterator.hasNext()) {
      entry = iterator.next();
      out_flows.put(entry.getKey(), entry.getValue() / total_density
          * total_out_flow);
    }

  }

  public void print() {
    System.out.println("Demand:" + demand);
    System.out.println("Supply:" + supply);
    System.out.println("Densities:" + partial_densities.toString() + "(total: "
        + total_density + ")");
    System.out.println("f_in: " + in_flows.toString());
    System.out.println("f_out: " + out_flows.toString());
  }

  /**
   * @brief Empty all the flows. It is necessary to call this function before
   *        creating new flows (after having injected vehicles into the origins
   *        for instance) to clear the previous values
   */
  public void clearFlow() {
    in_flows.clear();
    out_flows.clear();
  }

  public void recomputeTotalDensity() {
    Iterator<Double> it = partial_densities.values().iterator();
    double new_total_density = 0;
    Double partial_density;
    while (it.hasNext()) {
      partial_density = it.next();
      assert partial_density != null && partial_density >= 0 : " Negative partial density ("
          + partial_density + ")";
      new_total_density += partial_density;
    }
    this.total_density = new_total_density;
  }

  public boolean equals(Object obj, double epsilon) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CellInfo other = (CellInfo) obj;
    if (!Numerical.equals(demand, other.demand, epsilon))
      return false;
    if (!Numerical.equals(supply, other.supply, epsilon))
      return false;
    if (!Numerical.equals(total_density, other.total_density, epsilon))
      return false;

    if (in_flows == null) {
      if (other.in_flows != null)
        return false;
    } else if (!Numerical.equals(in_flows, other.in_flows, epsilon))
      return false;
    if (out_flows == null) {
      if (other.out_flows != null)
        return false;
    } else if (!Numerical.equals(out_flows, other.out_flows, epsilon))
      return false;
    if (partial_densities == null) {
      if (other.partial_densities != null)
        return false;
    } else if (!Numerical.equals(partial_densities, other.partial_densities,
        epsilon))
      return false;

    return true;
  }

  /**
   * @Copy the whole profile
   * @return A new profile containing the same information
   */
  public CellInfo copy() {
    CellInfo result = new CellInfo();
    result.supply = this.supply;
    result.demand = this.demand;
    result.total_density = this.total_density;
    result.partial_densities =
        new LinkedHashMap<Integer, Double>(this.partial_densities);
    result.in_flows =
        new LinkedHashMap<Integer, Double>(this.in_flows);
    result.out_flows =
        new LinkedHashMap<Integer, Double>(this.out_flows);
    return result;
  }

}