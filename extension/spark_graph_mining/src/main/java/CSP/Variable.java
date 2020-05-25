package CSP;

import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Variable {

    private String node_id;
    private myNode candidateNode;
    private HashMap<String, myNode> domain;
    private List<String> out_node_id_list;

    public Variable(String node_id, HashMap<String, myNode> domain) {
        this.node_id=node_id;
        this.domain=domain;
        for (Map.Entry<String, myNode> map:domain.entrySet()) {
            if (map!=null) {
                candidateNode=map.getValue();
                break;
            }
        }
    }

    public String getNode_id() {
        return node_id;
    }

    public myNode getCandidateNode() {
        return candidateNode;
    }

    public boolean setNextCandidateNode() {
        boolean flag=true;
        for (Iterator<Map.Entry<String, myNode>> iterator =domain.entrySet().iterator();iterator.hasNext();){
            Map.Entry map=iterator.next();
            if (candidateNode.getId().equals(map.getKey())) {
                flag=iterator.hasNext();
                if (flag) {
                    map=iterator.next();
                    candidateNode= (myNode) map.getValue();
                }
            }
        }
        return flag;
    }

    public void setOut_node_id_list(List<String> out_node_id_list) {
        this.out_node_id_list = out_node_id_list;
    }

    public List<String> getOut_node_id_list() {
        return out_node_id_list;
    }

}
