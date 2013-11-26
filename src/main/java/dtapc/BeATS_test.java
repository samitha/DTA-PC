package dtapc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import edu.berkeley.path.beats.jaxb.Link;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.jaxb.FundamentalDiagram;
import edu.berkeley.path.beats.jaxb.RouteSet;
import edu.berkeley.path.beats.jaxb.NetworkSet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile;
import edu.berkeley.path.beats.jaxb.Node;
import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.jaxb.Splitratio;
import edu.berkeley.path.beats.jaxb.Demand;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import dtapc.generalLWRNetwork.Origin;
import dtapc.generalNetwork.data.demand.Demands;
import dtapc.dta_solver.Discretization;
import dtapc.dta_solver.Simulator;
import dtapc.dataStructures.HashMapPairCellsDouble;
import dtapc.dataStructures.PairCells;
import dtapc.generalLWRNetwork.DiscretizedGraph;
import dtapc.generalLWRNetwork.LWR_network;
import dtapc.generalNetwork.graph.Graph;
import dtapc.generalNetwork.graph.MutableGraph;
import dtapc.generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;

public class BeATS_test {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configfilename = "/Users/Samitha/Documents/github/DTA-PC/graphs/Rerouting_sent_newxsd_v2.xml";
			Scenario scenario;
			try {
				
			scenario = ObjectFactory.createAndLoadScenario(configfilename);
		
		    System.out.println("Converting BeATS network to a DTAPC network...");
		    // Translate the network
		    NetworkSet network_set = scenario.getNetworkSet();
		    List<edu.berkeley.path.beats.jaxb.Network> list = network_set
		        .getNetwork();
		    assert list.size() == 1;
		    edu.berkeley.path.beats.jaxb.Network network = list.get(0);

		    // Read the nodes
		    Iterator<edu.berkeley.path.beats.jaxb.Node> node_iterator =
		        network.getNodeList().getNode().iterator();
		    HashMap<Integer, edu.berkeley.path.beats.jaxb.Node> BeATS_nodes =
		        new HashMap<Integer, edu.berkeley.path.beats.jaxb.Node>(
		            network.getNodeList().getNode().size());
		    edu.berkeley.path.beats.jaxb.Node tmp_node;
		    while (node_iterator.hasNext()) {
		      tmp_node = node_iterator.next();
		      BeATS_nodes.put((Integer) (int) tmp_node.getId(), tmp_node);
		    }	
		    
		    // Read the links
		    Iterator<edu.berkeley.path.beats.jaxb.Link> link_iterator =
		        network.getLinkList().getLink().iterator();
		    HashMap<Integer, edu.berkeley.path.beats.jaxb.Link> BeATS_links =
		        new HashMap<Integer, edu.berkeley.path.beats.jaxb.Link>(
		            network.getLinkList().getLink().size());
		    edu.berkeley.path.beats.jaxb.Link tmp_link;
		    while (link_iterator.hasNext()) {
		      tmp_link = link_iterator.next();
		      BeATS_links.put((Integer) (int) tmp_link.getId(), tmp_link);
		    }
			
		    // Read the Fundamental Diagram profiles
		    Iterator<FundamentalDiagramProfile> fdp_iterator =
		        scenario
		            .getFundamentalDiagramSet()
		            .getFundamentalDiagramProfile()
		            .iterator();
		    
		    // PATH link id -> FDP
		    HashMap<Integer, FundamentalDiagramProfile> fundamentalDiagramProfiles =
		        new HashMap<Integer, FundamentalDiagramProfile>(
		            scenario
		                .getFundamentalDiagramSet().getFundamentalDiagramProfile().size());

		    FundamentalDiagramProfile tmp_fdp;
		    while (fdp_iterator.hasNext()) {
		      tmp_fdp = fdp_iterator.next();
		      fundamentalDiagramProfiles.put((int) tmp_fdp.getLinkId(), tmp_fdp);
		    }
		    
		    /*
		     * Create the internal "mutable "graph:
		     * Add the nodes and keep the mapping to BeATS nodes
		     * Add the links and keep the mapping to BeATS links
		     * Ensure the update of the pointers to nodes and links
		     */
		    MutableGraph mutable_graph = new MutableGraph();

		    /* We first add the nodes */
		    Iterator<Node> BeATS_nodes_iterator = BeATS_nodes.values().iterator();
		    HashMap<Integer, dtapc.generalNetwork.graph.Node> BeATS_node_to_Mutable_node =
		        new HashMap<Integer, dtapc.generalNetwork.graph.Node>(BeATS_nodes.size());

		    while (BeATS_nodes_iterator.hasNext()) {
		      tmp_node = BeATS_nodes_iterator.next();
		      mutable_graph.addNode(0, 0);
		      BeATS_node_to_Mutable_node.put((int) tmp_node.getId(),
		          mutable_graph.getLastAddedNode());
		    }

		    /* We then add the links */
		    Iterator<Link> BeATS_links_iterator = BeATS_links.values().iterator();
		    HashMap<Integer, dtapc.generalNetwork.graph.Link> BeATS_link_to_Mutable_link =
		        new HashMap<Integer, dtapc.generalNetwork.graph.Link>(BeATS_links.size());

		    HashMap<Integer, Link> Mutable_link_to_BeATS_link =
		        new HashMap<Integer, Link>(BeATS_links.size());

		    while (BeATS_links_iterator.hasNext()) {
		      tmp_link = BeATS_links_iterator.next();
		      dtapc.generalNetwork.graph.Node from, to;
		      from = BeATS_node_to_Mutable_node.get((int) tmp_link.getBegin().getNodeId());
		      assert from != null;
		      to = BeATS_node_to_Mutable_node.get((int) tmp_link.getEnd().getNodeId());
		      assert to != null;
		      mutable_graph.addLink(from, to);

		      BeATS_link_to_Mutable_link.put((int) tmp_link.getId(),
		          mutable_graph.getLastAddedLink());

		      Mutable_link_to_BeATS_link.put(mutable_graph.getLastAddedLink().getUnique_id(),
		          tmp_link);

		      dtapc.generalNetwork.graph.Link tmp = mutable_graph.getLastAddedLink();
		      tmp.l = tmp_link.getLength();
		    }

		    assert mutable_graph.check() : "We should have nodes[i].id = i and links[i].id = i";

		    /*
		     * Iterate through the node and do the following:
		     * - Update the priority at the nodes. The priority is based on the number of 
		     *   lanes if the incoming links
		     * - Add origins and destinations
		     */

		    // Add origins and destinations
		    for (int i = 0; i < mutable_graph.sizeNode(); i++) {
		      dtapc.generalNetwork.graph.Node tmp = mutable_graph.getNode(i);
		      if (tmp.incoming_links.size() > 1) {
		        int nb = tmp.incoming_links.size();
		        double total_nb_lanes = 0;
		        for (int j = 0; j < nb; j++) {
		          int id = tmp.incoming_links.get(j).getUnique_id();
		          Link l = Mutable_link_to_BeATS_link.get(id);
		          assert l != null;
		          total_nb_lanes += l.getLanes();
		        }
		        tmp.priorities = new HashMap<Integer, Double>(nb);
		        Vector<dtapc.generalNetwork.graph.Link> incoming = tmp.incoming_links;
		        for (int link = 0; link < incoming.size(); link++) {
		          int id = tmp.incoming_links.get(link).getUnique_id();
		          Link l = Mutable_link_to_BeATS_link.get(id);
		          tmp.priorities.put(id, l.getLanes() / total_nb_lanes);
		        }
		      }
		      if (tmp.incoming_links.isEmpty()) {
			    mutable_graph.addSingleBufferSource(tmp);
		      }
		      if (tmp.outgoing_links.isEmpty()) {
			    mutable_graph.addSingleBufferDestination(tmp);
		      }
		    }

		    // We get the initial densities and put them into the Mutable links
		    Iterator<Density> densities =
		        scenario.getInitialDensitySet().getDensity().iterator();

		    Density tmp_density;
		    while (densities.hasNext()) {
		      tmp_density = densities.next();
		      dtapc.generalNetwork.graph.Link l =
		          BeATS_link_to_Mutable_link.get((int) tmp_density.getLinkId());
		      assert l != null;
		      l.initial_density =
		          Double.parseDouble(tmp_density.getContent());
		    }

		    BeATS_links_iterator = BeATS_links.values().iterator();
		    
		    /* We update the fundamental triangular diagram in the links */
		    /* The rest of the code currently only handles a single FD values across time */
		    while (BeATS_links_iterator.hasNext()) {
		        Link tmp = BeATS_links_iterator.next();
		        dtapc.generalNetwork.graph.Link Mutable_link = BeATS_link_to_Mutable_link.get((int) tmp.getId());
		
//			    for (int i = 0; i < fundamentalDiagrams.get(tmp).
//		        		getFundamentalDiagram().size(); i++) {
			    	FundamentalDiagram tmp_fd = fundamentalDiagramProfiles.get((int) tmp.getId()).
			        		getFundamentalDiagram().get(0);
//			    	Mutable_link.jam_density= tmp_fd.getJamDensity();
			    	Mutable_link.F_max = tmp_fd.getCapacity();
			    	Mutable_link.v = tmp_fd.getFreeFlowSpeed();
			    	Mutable_link.w = tmp_fd.getCongestionSpeed();
			    	Mutable_link.dt = fundamentalDiagramProfiles.get((int) tmp.getId()).getDt();
			    	Mutable_link.jam_density = Mutable_link.F_max/Mutable_link.v + Mutable_link.F_max/Mutable_link.w;
			    	System.out.println(Mutable_link.jam_density);
			    	//			    }
		    }
			   
		    
		    System.out.println(mutable_graph.toString());

		    Graph graph = new Graph(mutable_graph);

		    /* This needs to not be hard coded */
		    double delta_t = 300;
		    int time_steps = 10;

		    DiscretizedGraph discretized_graph = new DiscretizedGraph(graph, delta_t,
		        time_steps);

		    // Now we build the discretized network
		    LWR_network lwr_network = new LWR_network(discretized_graph);

		    // We get the non-compliant split ratios
//		    assert scenario.getSplitRatioSet().getSplitRatioProfile().size() == ? : 
//		    	"Number of split ratio profiles should be equal to total commodities, which is
//		   		 non-compliants plus all routes";
		    double split_ratios_dt = scenario
		        .getSplitRatioSet()
		        .getSplitRatioProfile()
		        .get(0).getDt();
		    System.out.println("Split ratios (dt =" + split_ratios_dt + "):");

		    LinkedList<HashMapPairCellsDouble> SR_list =
		        new LinkedList<HashMapPairCellsDouble>();

		    Iterator<Splitratio> non_compliant_SR_iterator =
		        scenario
		            .getSplitRatioSet()
		            .getSplitRatioProfile()
		            .get(0).getSplitratio()
		            .iterator();

		    Splitratio tmp_SR;
		    while (non_compliant_SR_iterator.hasNext()) {
		      tmp_SR = non_compliant_SR_iterator.next();
		      HashMapPairCellsDouble non_compliant_split_ratios;

		      List<String> history =
		          new ArrayList<String>(Arrays.asList(tmp_SR.getContent().split(",")));
		      double[] history_table = new double[history.size()];
		      for (int i = 0; i < history_table.length; i++)
		        history_table[i] = Double.parseDouble(history.get(i));

		      for (int k = 0; k < history_table.length; k++) {
		        if (k < SR_list.size())
		          non_compliant_split_ratios = SR_list.get(k);
		        else {
		          non_compliant_split_ratios = new HashMapPairCellsDouble();
		          SR_list.addLast(non_compliant_split_ratios);
		        }

		        non_compliant_split_ratios.put(
		            new PairCells((int) tmp_SR.getLinkIn(), (int) tmp_SR.getLinkOut()),
		            history_table[k]);
		      }
		    }
		    HashMapPairCellsDouble[] SR_array =
		        new HashMapPairCellsDouble[SR_list.size()];
		    SR_list.toArray(SR_array);

		    System.out
		        .println("Non-compliant split ratios:" + Arrays.toString(SR_array));
		    
		    
		    // Demand set
		    Iterator<DemandProfile> demandProfile_iterator =
		        scenario.getDemandSet().getDemandProfile().iterator();
		    // For now we deal with only one origin
		    assert scenario.getDemandSet().getDemandProfile().size() == 1;

		    LinkedList<Double> demands = new LinkedList<Double>();

		    DemandProfile tmp_demandProfile;
		    while (demandProfile_iterator.hasNext()) { // There is only one for now
		      tmp_demandProfile = demandProfile_iterator.next();

		      Iterator<Demand> demand_iterator = tmp_demandProfile.getDemand().iterator();
		      Demand tmp_demand;
		      // We have the discretization
		      double dt = tmp_demandProfile.getDt();
		      int origin_id = (int) tmp_demandProfile.getLinkIdOrg();

		      while (demand_iterator.hasNext()) {
		        tmp_demand = demand_iterator.next();

		        List<String> history =
		            new ArrayList<String>(Arrays.asList(tmp_demand.getContent().split(
		                ",")));
		        double[] history_table = new double[history.size()];
		        for (int i = 0; i < history_table.length; i++)
		          history_table[i] = Double.parseDouble(history.get(i));

		        System.out.println("Demand for origin " + origin_id + " (dt = " + dt
		            + Arrays.toString(history_table));
		      }
		    }

		    System.out.println("Demand profiles:" + demands.toString());
			

		    // Add split ratio's and demands to the LWR network
		    
		    // Then we create all the nodes
		    // we create the hash map PATH_node -> JB_node
		    // We create all the links with the right values of the incoming links,
		    // We create the Hash map PATH_link -> JB_Link
		    // We update the correct in and out link in the jb-nodes.
		    // We create the graph.
		    // We add the non-compliant split ratios
		    // We also need to create the path !!!

		    // We also need to discretize the demand and the non-compliant split ratios
		    
					
			System.out.println("Test done!");
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

}
