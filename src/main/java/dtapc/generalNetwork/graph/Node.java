package generalNetwork.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.google.gson.annotations.Expose;

/**
 * @bref Represent a macroscopic intersection between incoming and outgoing
 *       links
 */
public class Node {

  @Expose
  protected int unique_id;
  @Expose
  protected int x;
  @Expose
  protected int y;
  /* int[] incoming, outgoing should NEVER be used except for serialization */
  @Expose
  private int[] incoming, outgoing;
  @Expose
  private double[] incoming_priorities;
  public Vector<Link> incoming_links, outgoing_links;
  public HashMap<Integer, Double> priorities;

  public Node(int id) {
    incoming_links = new Vector<Link>(1);
    outgoing_links = new Vector<Link>(1);
    unique_id = id;
  }

  public Node(GraphUIDFactory id, int x, int y) {
    incoming_links = new Vector<Link>(1);
    outgoing_links = new Vector<Link>(1);
    unique_id = id.getId_node();
    this.x = x;
    this.y = y;
  }

  public boolean isMergingNode() {
    return outgoing_links.size() == 1;
  }

  /**
   * @brief Has to be used before exporting to Json format
   */
  public void buildToJson() {
    incoming = new int[incoming_links.size()];
    outgoing = new int[outgoing_links.size()];
    for (int i = 0; i < incoming_links.size(); i++)
      incoming[i] = incoming_links.get(i).unique_id;

    for (int i = 0; i < outgoing_links.size(); i++)
      outgoing[i] = outgoing_links.get(i).unique_id;
  }

  /**
   * @brief Update the from, to fields in the connected links
   */
  private void updateConnectedlinks() {
    for (int i = 0; i < incoming_links.size(); i++)
      incoming_links.get(i).to = this;

    for (int i = 0; i < outgoing_links.size(); i++)
      outgoing_links.get(i).from = this;
  }

  /**
   * @brief Build the vectors incoming_links and outgoing_links from the array
   *        To be used only after a json loading.
   */
  public void buildFromJson(Link[] links) {

    /*
     * You have to define priorities for NxM nodes where N > M and this is not a
     * destination
     */
    boolean priority_needed = (outgoing.length != 0)
        && (incoming.length > outgoing.length);
    if (priority_needed) {
      if (incoming_priorities == null) {
        System.out.println("In node " + unique_id
            + " the priorities are missing. Aborting!");
        System.exit(1);
      }
      if (incoming_priorities.length != incoming.length) {
        System.out.println("[Node" + unique_id
            + "]In a junction " + incoming.length + "x" + outgoing.length
            + ", you need to specify N priorities");
        System.exit(1);
      }
    }

    List<Link> incoming_l = new ArrayList<Link>(incoming.length);
    List<Link> outgoing_l = new ArrayList<Link>(outgoing.length);
    if (priority_needed) {
      priorities = new HashMap<Integer, Double>(incoming.length);
    } else {
      priorities = null;
    }

    for (int i = 0; i < incoming.length; i++) {
      incoming_l.add(links[incoming[i]]);
      if (priority_needed) {
        priorities.put(links[incoming[i]].unique_id, incoming_priorities[i]);
      }
    }

    for (int i = 0; i < outgoing.length; i++) {
      outgoing_l.add(links[outgoing[i]]);
    }

    if (incoming_links == null)
      incoming_links = new Vector<Link>(incoming_l.size());
    if (outgoing_links == null)
      outgoing_links = new Vector<Link>(outgoing_l.size());

    incoming_links.addAll(incoming_l);
    outgoing_links.addAll(outgoing_l);

    /* Updates the @a from and @a to fields in the links */
    updateConnectedlinks();

    incoming = null;
    outgoing = null;
  }

  public int getUnique_id() {
    return unique_id;
  }

  public void addIncomingLink(Link l) {
    incoming_links.add(l);
    l.to = this;
  }

  public void addOutgoingLink(Link l) {
    outgoing_links.add(l);
    l.from = this;
  }

  void print() {
    String in = "";
    for (int i = 0; i < incoming_links.size(); i++) {
      in += "," + incoming_links.get(i).unique_id;
    }
    String out = "";
    for (int i = 0; i < outgoing_links.size(); i++) {
      out += "," + outgoing_links.get(i).unique_id;
    }
    System.out.println("[Node " + unique_id + ": [" + in + "]->[" + out + "]]");
    for (int i = 0; i < outgoing_links.size(); i++) {
      outgoing_links.get(i).print();
    }
  }

  @Override
  public String toString() {
    return "Node [(J" + unique_id + ") incoming="
        + Arrays.toString(incoming_links.toArray())
        + ", outgoing=" + Arrays.toString(outgoing_links.toArray())
        + ", incoming_priorities=" + Arrays.toString(incoming_priorities)
        + ", priorities=" + priorities + "]";
  }
}