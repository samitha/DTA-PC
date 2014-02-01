package dtapc.generalLWRNetwork;

import dtapc.generalNetwork.state.Profile;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import dtapc.dataStructures.HashMapIntegerDouble;

/**
 * @brief The origin is for now a buffer
 */
public class Origin {

	protected Buffer[] entries;
	protected Junction junction;
	protected LinkedList<Integer> compliant_commodities;

	protected Origin() {
		entries = null;
		junction = null;
	}

	public Origin(Junction j, String type, LinkedList<Cell> new_cells,
			LinkedList<Junction> new_junctions) {
		assert j != null;
		assert type != null;
		assert new_cells != null;
		assert new_junctions != null;
		assert j.getPrev() == null : "An origin junction should have no incoming links";

		if (type.equals("SingleBuffer")) {
			entries = new Buffer[1];
			Buffer b = new Buffer();
			entries[0] = b;
			new_cells.add(b);
			b.setNext(j);

			j.setPrev(new Cell[] { b });
			junction = j;

		} else {
			System.out.println("[Origin.java]Creation of a " + type
					+ " origin. This type does not exist");
			entries = null;
			junction = null;
			System.exit(1);
		}
		assert entries.length == 1;
	}

	/**
	 * @return The number of buffers
	 */
	public int size() {
		return entries.length;
	}

	public int getUniqueId() {
		return entries[0].getUniqueId();
	}

	/**
	 * @return The linked list containing the compliant commodities
	 */
	public LinkedList<Integer> getCompliant_commodities() {
		return compliant_commodities;
	}

	public void setCompliant_commodities(
			LinkedList<Integer> compliant_commodities) {
		this.compliant_commodities = compliant_commodities;
	}

	/**
	 * @brief Inject the demand in the current profile
	 * @param previous_profile
	 *            This is NOT modified
	 * @param p
	 *            Only the densities of the buffers are modified
	 * @param demand
	 *            Total amount of demand
	 * @param splits
	 *            The split ratios to divide up the demand
	 */
	public void injectDemand(Profile previous_profile, Profile p,
			Double demand, HashMapIntegerDouble splits, double delta_t) {

		assert demand >= 0 : "The vehicle demand at the origin should be positive";
		assert splits != null;

		LinkedHashMap<Integer, Double> previous_densities = previous_profile
				.getCell(entries[0]).partial_densities;

		/*
		 * The densities are the previous_profile densities to which we removed
		 * the out-flow and to which we add the demand
		 */
		/* Here we compute the previous densities - out-flow */
		LinkedHashMap<Integer, Double> new_densities = entries[0]
				.getUpdatedDensity(
						previous_densities,
						null,
						previous_profile.getCell(entries[0].getUniqueId()).out_flows,
						delta_t);

		/* Then we add the demand for every commodity */
		Iterator<Entry<Integer, Double>> split_iterator = splits.entrySet()
				.iterator();
		Entry<Integer, Double> split_entry;
		double new_density, split_ratio;
		Double previous_density;
		int commodity;
		while (split_iterator.hasNext()) {
			split_entry = split_iterator.next();
			split_ratio = split_entry.getValue();
			assert split_ratio >= 0 : "The split ratio (" + split_ratio
					+ ") has to be positive";
			commodity = split_entry.getKey();

			previous_density = new_densities.get(commodity);
			if (previous_density == null) {
				new_density = demand * split_ratio;
			} else {
				new_density = previous_density + demand * split_ratio;
			}
			new_densities.put(commodity, new_density);
		}

		p.getCell(entries[0]).partial_densities = new_densities;

		/* We recompute the total_density */
		p.getCell(entries[0]).recomputeTotalDensity();

	}

	@Override
	public String toString() {
		return "Origin [junction=" + junction.getUniqueId() + "]";
	}

	public Buffer[] getEntries() {
		return entries;
	}
}