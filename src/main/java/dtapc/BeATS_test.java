import edu.berkeley.path.beats.simulator.DemandSet;
import edu.berkeley.path.beats.simulator.Link;
import edu.berkeley.path.beats.simulator.ObjectFactory;
import edu.berkeley.path.beats.simulator.Scenario;
import edu.berkeley.path.beats.jaxb.Demand;
import edu.berkeley.path.beats.jaxb.FundamentalDiagramSet;
import edu.berkeley.path.beats.jaxb.RouteSet;

public class BeATS_test {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String configfilename = "/Users/torresl/LRTORRES/dtapc/graphs/Rerouting_sent_newxsd_v2.xml";
			Scenario scenario;
			try {
				scenario = ObjectFactory.createAndLoadScenario(configfilename);
		
			
			FundamentalDiagramSet fdset = scenario.getFundamentalDiagramSet();
			DemandSet demset = (DemandSet) scenario.getDemandSet();
			
			RouteSet route_set = scenario.getRouteSet();
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

}
