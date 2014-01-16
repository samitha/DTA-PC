package dtapc.generalNetwork.data;

import com.google.gson.annotations.Expose;

public class JsonJunctionSplitRatios {

  @Expose
  public int k, in_id, out_id, c;
  @Expose
  public double beta;
  
  public JsonJunctionSplitRatios(int k, int in_id, int out_id, int c, double beta) {
	  this.k = k;
	  this.in_id = in_id;
	  this.out_id = out_id;
	  this.c = c;
	  this.beta = beta;
  }
}
