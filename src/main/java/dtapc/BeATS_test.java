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
import edu.berkeley.path.beats.jaxb.Route;
import edu.berkeley.path.beats.jaxb.RouteLink;
import edu.berkeley.path.beats.jaxb.RouteSet;
import edu.berkeley.path.beats.jaxb.NetworkSet;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramProfile;
import edu.berkeley.path.beats.jaxb.Node;
import edu.berkeley.path.beats.jaxb.Density;
import edu.berkeley.path.beats.jaxb.Splitratio;
import edu.berkeley.path.beats.jaxb.Demand;
import edu.berkeley.path.beats.jaxb.DemandProfile;
import dtapc.generalNetwork.data.demand.DemandsFactory;
import dtapc.generalNetwork.data.JsonDemand;
import dtapc.generalNetwork.data.JsonJunctionSplitRatios;
import dtapc.generalNetwork.data.JsonSplitRatios;
import dtapc.dta_solver.SOPC_Optimizer;
import dtapc.dta_solver.Simulator;
import dtapc.dataStructures.HashMapPairCellsDouble;
import dtapc.dataStructures.PairCells;
import dtapc.generalLWRNetwork.DiscretizedGraph;
import dtapc.generalLWRNetwork.LWR_network;
import dtapc.generalNetwork.graph.Graph;
import dtapc.generalNetwork.graph.MutableGraph;
import dtapc.generalNetwork.graph.Path;
import dtapc.generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
import dtapc.optimization.GradientDescent;

public class BeATS_test {
	
	static boolean verbose = true;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configfilename = "/Users/Samitha/Documents/github/DTA-PC/graphs/Rerouting_sent_newxsd_v3.xml";
			Scenario scenario;
			try {
				
			scenario = ObjectFactory.createAndLoadScenario(configfilename);
		
		    System.out.println("Converting BeATS network to a DTAPC network...");
		    // Translate the network
		    NetworkSet network_set = scenario.getNetworkSet();
		    List<edu.berkeley.path.beats.jaxb.Network> network_list = network_set
		        .getNetwork();
		    assert network_list.size() == 1;
		    edu.berkeley.path.beats.jaxb.Network network = network_list.get(0);
		    
		    // Read the nodes
		    Iterator<edu.berkeley.path.beats.jaxb.Node> node_iterator =
		        network.getNodeList().getNode().iterator();
		    HashMap<Integer, edu.berkeley.path.beats.jaxb.Node> BeATS_nodes =
		        new HashMap<Integer, edu.berkeley.path.beats.jaxb.Node>(
		            network.getNodeList().getNode().size());
		    edu.berkeley.path.beats.jaxb.Node tmp_node;
		    System.out.println();
		    System.out.println("BeATS Nodes:");
		    while (node_iterator.hasNext()) {
		      tmp_node = node_iterator.next();
		      BeATS_nodes.put((int) tmp_node.getId(), tmp_node);
			  System.out.println(BeATS_nodes.get((int) tmp_node.getId()).getId());
		    }	
		    System.out.println();

		    // Read the links
		    Iterator<edu.berkeley.path.beats.jaxb.Link> link_iterator =
		        network.getLinkList().getLink().iterator();
		    HashMap<Integer, edu.berkeley.path.beats.jaxb.Link> BeATS_links =
		        new HashMap<Integer, edu.berkeley.path.beats.jaxb.Link>(
		            network.getLinkList().getLink().size());
		    edu.berkeley.path.beats.jaxb.Link tmp_link;
		    System.out.println("BeATS links:");
		    while (link_iterator.hasNext()) {
		      tmp_link = link_iterator.next();
		      BeATS_links.put((Integer) (int) tmp_link.getId(), tmp_link);
			  System.out.println(BeATS_links.get((int) tmp_link.getId()).getId());
		    }
		    System.out.println();
			
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

		    System.out.println("BeATS FD params:");
		    FundamentalDiagramProfile tmp_fdp;
		    while (fdp_iterator.hasNext()) {
		      tmp_fdp = fdp_iterator.next();
		      fundamentalDiagramProfiles.put((int) tmp_fdp.getLinkId(), tmp_fdp);
		      System.out.print(fundamentalDiagramProfiles.get((int) tmp_fdp.getLinkId()).getFundamentalDiagram().get(0).getCapacity() + ", ");
		      System.out.print(fundamentalDiagramProfiles.get((int) tmp_fdp.getLinkId()).getFundamentalDiagram().get(0).getCapacity() + ", ");
		      System.out.print(fundamentalDiagramProfiles.get((int) tmp_fdp.getLinkId()).getFundamentalDiagram().get(0).getCapacity());
		      System.out.println();
		    }
		    
		    /*
		     * Create the internal "mutable "graph:
		     * Add the nodes and keep the mapping to BeATS nodes
		     * Add the links and keep the mapping to BeATS links
		     * Ensure the update of the pointers to nodes and links
		     */
		    MutableGraph mutable_graph = new MutableGraph();

		    System.out.println();
		    System.out.println("Mutable graph nodes");
		    /* We first add the nodes */
		    Iterator<Node> BeATS_nodes_iterator = BeATS_nodes.values().iterator();
		    HashMap<Integer, dtapc.generalNetwork.graph.Node> BeATS_node_to_Mutable_node =
		        new HashMap<Integer, dtapc.generalNetwork.graph.Node>(BeATS_nodes.size());

		    while (BeATS_nodes_iterator.hasNext()) {
		      tmp_node = BeATS_nodes_iterator.next();
		      mutable_graph.addNode(0, 0);
		      BeATS_node_to_Mutable_node.put((int) tmp_node.getId(),
		          mutable_graph.getLastAddedNode());
		      System.out.println(mutable_graph.getLastAddedNode().getUnique_id());
		    }
		    System.out.println();

		    System.out.println("Mutable graph links");
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
		      System.out.print(mutable_graph.getLastAddedLink().getUnique_id() + " from node: ");
		      System.out.print(mutable_graph.getLastAddedLink().from.getUnique_id() + " to: ");
		      System.out.print(mutable_graph.getLastAddedLink().to.getUnique_id());
		      System.out.println();

		      BeATS_link_to_Mutable_link.put((int) tmp_link.getId(),
		          mutable_graph.getLastAddedLink());

		      Mutable_link_to_BeATS_link.put(mutable_graph.getLastAddedLink().getUnique_id(),
		          tmp_link);

		      dtapc.generalNetwork.graph.Link tmp = mutable_graph.getLastAddedLink();
		      tmp.l = tmp_link.getLength();
		    }
		    System.out.println();

		    assert mutable_graph.check() : "We should have nodes[i].id = i and links[i].id = i";

		    /*
		     * Iterate through the node and do the following:
		     * - Update the priority at the nodes. The priority is based on the number of 
		     *   lanes if the incoming links
		     * - Add origins and destinations
		     */

	    	System.out.println("Origins and destinations");
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
		    	System.out.println("Origin: " + tmp.getUnique_id());
		      }
		      if (tmp.outgoing_links.isEmpty()) {
			    mutable_graph.addSingleBufferDestination(tmp);
		    	System.out.println("Destination: " + tmp.getUnique_id());
		      }
		    }

		    System.out.println();
	    	System.out.println("Initial densities");
		    // We get the initial densities and put them into the Mutable links
		    Iterator<Density> BeATS_densities_iterator =
		        scenario.getInitialDensitySet().getDensity().iterator();

		    Density tmp_density;
		    while (BeATS_densities_iterator.hasNext()) {
		      tmp_density = BeATS_densities_iterator.next();
		      dtapc.generalNetwork.graph.Link l =
		          BeATS_link_to_Mutable_link.get((int) tmp_density.getLinkId());
		      assert l != null;
		      l.initial_density =
		          Double.parseDouble(tmp_density.getContent());
		      System.out.println(l.getUnique_id() + ": " + l.initial_density);
		    }

		    BeATS_links_iterator = BeATS_links.values().iterator();
		    
	    	System.out.println();
	    	System.out.println("Mutable graph FD params");
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
			    	//Mutable_link.dt = fundamentalDiagramProfiles.get((int) tmp.getId()).getDt();
			    	Mutable_link.jam_density = Mutable_link.F_max/Mutable_link.v + Mutable_link.F_max/Mutable_link.w;
			    	System.out.print("link: " + Mutable_link.getUnique_id());
			    	System.out.print(" F_max: " + Mutable_link.F_max);
			    	System.out.print(" v: " + Mutable_link.v);
			    	System.out.print(" w: " + Mutable_link.w);
			    	System.out.print(" dt: " + Mutable_link.dt);
			    	System.out.print(" jam_density: " + Mutable_link.jam_density);
			    	System.out.println();

		    }
			   
		    System.out.println();
		    System.out.println(mutable_graph.toString());
		    
	    	System.out.println("Mutable graph paths ");
		    // Add paths
		    RouteSet BeATS_routeSet = scenario.getRouteSet();
		    Iterator<Route> BeATS_routes_iterator = BeATS_routeSet.getRoute().iterator();
		    while (BeATS_routes_iterator.hasNext()) {
		    	Route tmpRoute = BeATS_routes_iterator.next();
		    	ArrayList<Integer> routeList = new ArrayList<Integer>();
		    	Iterator<RouteLink> routeLink_iterator = tmpRoute.getRouteLink().iterator();
		    	while (routeLink_iterator.hasNext()) {
		    		routeList.add((int) BeATS_link_to_Mutable_link.get((int) routeLink_iterator.next().getLinkId()).getUnique_id());
		    	}
		    	Path newpath = new Path((int) tmpRoute.getId(), routeList);
		    	mutable_graph.addPath(newpath);
		    	Iterator<Integer> pathList_iterator = mutable_graph.getPaths().get((int) tmpRoute.getId()-1).iterator();
		    	System.out.print(tmpRoute.getId() + ": ");
		    	while (pathList_iterator.hasNext()) {
			    	System.out.print(pathList_iterator.next().intValue() + " ");
		    	}
		    	System.out.println();
		    }

		    Graph graph = new Graph(mutable_graph);		    
		    
		    /* This needs to NOT be hard coded */
		    int delta_t =60;
		    int time_steps = 80;

		    DiscretizedGraph discretized_graph = new DiscretizedGraph(graph, delta_t,
		        time_steps);

		    // Add internal split ratios
		    System.out.println();
		    System.out.println("Discretized graph split ratios");
		    // We get the non-compliant split ratios
//		    assert scenario.getSplitRatioSet().getSplitRatioProfile().size() == ? : 
//		    	"Number of split ratio profiles should be equal to total commodities, which is
//		   		 non-compliants plus all routes";
		    double split_ratios_dt = scenario
		        .getSplitRatioSet()
		        .getSplitRatioProfile()
		        .get(0).getDt();
		    System.out.println("dt =" + split_ratios_dt);

		    LinkedList<HashMapPairCellsDouble> SR_list =
		        new LinkedList<HashMapPairCellsDouble>();

		    Iterator<Splitratio> non_compliant_SR_iterator =
		        scenario
		            .getSplitRatioSet()
		            .getSplitRatioProfile()
		            .get(0).getSplitratio()
		            .iterator();
		    
		    Splitratio tmp_SR;
	    	JsonJunctionSplitRatios[] NC_split_ratios;
		    JsonSplitRatios[] Json_SR = new JsonSplitRatios[1];
		    while (non_compliant_SR_iterator.hasNext()) {
		      tmp_SR = non_compliant_SR_iterator.next();
		      if (tmp_SR.getVehicleTypeId() == 0) {
				  System.out.println("Non-compliant split ratios:");
		    	  HashMapPairCellsDouble non_compliant_split_ratios;
		    	  List<String> history =
		    			  new ArrayList<String>(Arrays.asList(tmp_SR.getContent().split(",")));
		    	  double[] history_table = new double[history.size()];
		    	  for (int i = 0; i < history_table.length; i++) {
		    		  history_table[i] = Double.parseDouble(history.get(i));
		    	  }
		    	  NC_split_ratios = new JsonJunctionSplitRatios[history_table.length];
		    	  for (int k = 0; k < history_table.length; k++) {
		    		  if (k < SR_list.size()) {
		    			  non_compliant_split_ratios = SR_list.get(k);
		    		  }
		    		  else {
		    			  non_compliant_split_ratios = new HashMapPairCellsDouble();
		    			  SR_list.addLast(non_compliant_split_ratios);
		    		  }		    		  
		    		  NC_split_ratios[k] = new JsonJunctionSplitRatios(k, (int) BeATS_link_to_Mutable_link.get((int) tmp_SR.getLinkIn()).getUnique_id(), 
		    				  (int) BeATS_link_to_Mutable_link.get((int) tmp_SR.getLinkOut()).getUnique_id(), (int) tmp_SR.getVehicleTypeId(), history_table[k]);

		    		  non_compliant_split_ratios.put(
		    				  new PairCells((int) BeATS_link_to_Mutable_link.get( (int)tmp_SR.getLinkIn()).getUnique_id(), BeATS_link_to_Mutable_link.get( (int) tmp_SR.getLinkOut()).getUnique_id()),
		    				  history_table[k]);
					  System.out.print("beta: " + NC_split_ratios[k].beta + " ");		    		  
					  System.out.print("c: " + NC_split_ratios[k].c + " ");
					  System.out.print("in_id: " + NC_split_ratios[k].in_id + " ");
					  System.out.print("out_id: " + NC_split_ratios[k].out_id + " ");
					  System.out.print("k: " + NC_split_ratios[k].k + " ");
					  System.out.println();
		    	  }
				  System.out.println();
		    	  Json_SR[0] = new JsonSplitRatios((int) BeATS_node_to_Mutable_node.get((int) scenario
				            .getSplitRatioSet()
				            .getSplitRatioProfile().get(0).getNodeId()).getUnique_id(), NC_split_ratios);
				  System.out.println("Non-compliant split ratio node id:" + Json_SR[0].node_id);
		       }
		    }
		    System.out.println();
		    HashMapPairCellsDouble[] SR_array =
		        new HashMapPairCellsDouble[SR_list.size()];
		    SR_list.toArray(SR_array);
		    
		    IntertemporalSplitRatios intTempSR = discretized_graph.split_ratios;
		    intTempSR.addNonCompliantSplitRatios(discretized_graph, Json_SR);
		    
		    // Now we build the discretized network
		    LWR_network lwr_network = new LWR_network(discretized_graph);
		   
		    // Creating the simulator
		    double alpha = 0.01; // Fraction of compliant flow
		    Simulator simulator = new Simulator(delta_t, time_steps, alpha);
		    simulator.discretized_graph = discretized_graph;
		    simulator.lwr_network = lwr_network;
		    
		    // Demand set
		    Iterator<DemandProfile> demandProfile_iterator =
		        scenario.getDemandSet().getDemandProfile().iterator();
		    // For now we deal with only one origin
		    assert scenario.getDemandSet().getDemandProfile().size() == 1;

		    DemandProfile tmp_demandProfile;
		    JsonDemand[][] demandArray = new JsonDemand[1][time_steps];
		    double[] totalDemand = new double[time_steps];
		    for (int i = 0; i < time_steps; i++){
	        	totalDemand[i] = 0;
		    }
		    while (demandProfile_iterator.hasNext()) { // There is only one for now
		      tmp_demandProfile = demandProfile_iterator.next();

		      Iterator<Demand> demand_iterator = tmp_demandProfile.getDemand().iterator();
		      Demand tmp_demand;
		      // We have the discretization
		      double dt = tmp_demandProfile.getDt();
		      int origin_id = (int) tmp_demandProfile.getLinkIdOrg();

		      System.out.println("Demand for origin " + origin_id + " dt = " + dt);

		      while (demand_iterator.hasNext()) {
		        tmp_demand = demand_iterator.next();

		        List<String> history =
		            new ArrayList<String>(Arrays.asList(tmp_demand.getContent().split(
		                ",")));
		        double[] history_table = new double[history.size()];
		        for (int i = 0; i < history_table.length; i++){
		          history_table[i] = Double.parseDouble(history.get(i));
		          totalDemand[i] = totalDemand[i] + history_table[i];
		        }

		        demandArray[0][(int) tmp_demand.getVehicleTypeId()] =  
		        		  new JsonDemand((int) mutable_graph.getLink((int) BeATS_link_to_Mutable_link.get((int) tmp_demandProfile.getLinkIdOrg()).from.getUnique_id()).getUnique_id(), 
		        				  history_table);
		      }
		    }
			
	        JsonDemand[] json_demands = new JsonDemand[1];
		    json_demands[0] = new JsonDemand((int) mutable_graph.getLink((int) BeATS_link_to_Mutable_link.get((int) scenario.getDemandSet().getDemandProfile().
		    		get(0).getLinkIdOrg()).from.getUnique_id()).getUnique_id(), totalDemand);
		    simulator.origin_demands = new DemandsFactory(simulator.time_discretization, delta_t, json_demands, discretized_graph.node_to_origin).buildDemands();
		    System.out.println(simulator.origin_demands.toString());
		    System.out.println();
		    simulator.initializSplitRatios();

		    System.out.println();
		    System.out.println(simulator.splits.toString());

		    /* Checking the requirements on the network */
		    System.out
		        .print("Checking that the network respect needed requirements...");
		    lwr_network.checkConstraints(delta_t);
		    System.out.println("Done");

		    
	      if (verbose) {
	          System.out.print("No control cost: "
	              + simulator.objective() + "\n");
	        }

		    int maxIter = 10;
		    SOPC_Optimizer optimizer = new SOPC_Optimizer(simulator);

		    GradientDescent descentMethod = new GradientDescent(maxIter);
		    descentMethod.setGradient_condition(10E-9);
		    double[] result = descentMethod.solve(optimizer);
		    System.out.println("Final control");
		    for (int i = 0; i < result.length; i++)
		      System.out.println(result[i]);

			System.out.println("Test done!");
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

}
