package dtapc.generalNetwork.data;

import com.google.gson.annotations.Expose;

public class JsonSplitRatios {

  @Expose
  public int node_id;
  @Expose
  public JsonJunctionSplitRatios[] split_ratios;
  
  public JsonSplitRatios(int node_id, JsonJunctionSplitRatios[] split_ratios) {
	  this.node_id = node_id;
	  this.split_ratios = split_ratios;
  }
}
