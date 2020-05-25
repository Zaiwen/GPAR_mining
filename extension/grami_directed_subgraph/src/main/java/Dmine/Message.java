package Dmine;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import dataStructures.HPListGraph;
import dataStructures.Query;
import edu.isi.karma.rep.alignment.Node;

import java.util.*;

public class Message {
    private String id;
    private HPListGraph rule;
    private double conf;
    //private List<Integer> ruleCenterNodesIds;
    private Rule r;
    //private HashMap<Integer, List<HashSet<Integer>>> pattern_indexes_in_graph;
    private HashMap<Integer, List<Integer>> partitionCenterNodesId;
   //private HashMap<Integer, HashMap<Integer, List<HashSet<Integer>>>> patternNodesId;


    private double Q_qGSupport = -1;
    private double RGSupport;

    // marked for filtering
    // 1 stands for initial state
    // 0 stands for saving while -1 stands for removing
    private int marked = 1;

    public Message(String id) {
        this.id = id;
    }

    public Message(HPListGraph rule, double conf, boolean isExtendable, List<Integer> ruleCenterNodesIds) {
        this.rule = rule;
        this.conf = conf;
        //this.ruleCenterNodesIds = ruleCenterNodesIds;
    }


    public void setQ_qGSupport(double q_qGSupport) {
        Q_qGSupport = q_qGSupport;
    }

    public void setRGSupport(double RGSupport) {
        this.RGSupport = RGSupport;
    }

    public void setRule(HPListGraph rule) {
        this.rule = rule;
    }

//    public void setRuleCenterNodesIds(List<Integer> ruleCenterNodesIds) {
//        this.ruleCenterNodesIds = ruleCenterNodesIds;
//    }


//    public HashMap<Integer, List<HashSet<Integer>>> getPatternIndexesInGraph() {
//        return pattern_indexes_in_graph;
//    }


    public void setConf(double conf) {
        this.conf = conf;
    }

    public void setR(Rule r) {
        this.r = r;
        this.partitionCenterNodesId = r.getPartitionIndexesMap();
//        pattern_indexes_in_graph = new HashMap<>(ruleCenterNodesIds.size());
//        Query query = new Query(this.r.getFormer_pattern());
//        HashMap<Integer, HashSet<Integer>> nonCandidates1 = new HashMap<>();//create a hashmap to save pruned variables
//        ConstraintGraph cg = new ConstraintGraph(this.r.getSingleGraph(), query, nonCandidates1);
//        for (int index : ruleCenterNodesIds) {
//            HashMap<Integer, HashSet<Integer>> nonCandidates2 = new HashMap<>();//create a hashmap to save pruned variables
//            DFSSearch search = new DFSSearch(cg, this.r.getSingleGraph().getFreqThreshold(), nonCandidates2);
//            search.searchAll(index);
//            List<HashSet<Integer>> list = new ArrayList<>();
//            for (Map<Node, Node> nodeMap : search.getIsomorphismList()) {
//                HashSet<Integer> set = new HashSet<>(nodeMap.size());
//                for (Map.Entry<Node, Node> entry : nodeMap.entrySet()) {
//                    set.add(Integer.valueOf(entry.getValue().getId()));
//                }
//                list.add(set);
//            }
//            pattern_indexes_in_graph.put(index, list);
//        }
    }

    public HashMap<Integer, List<Integer>> getPartitionCenterNodesId() {
        return partitionCenterNodesId;
    }

    public Rule getR() {
        return r;
    }

    public String getId() {
        return id;
    }

    public boolean hasQ_qSupport() {
        return !(Q_qGSupport == -1);
    }

    public HPListGraph getRule() {
        return rule;
    }

    public double getConf() {
        return conf;
    }

//    public List<Integer> getRuleCenterNodesIds() {
//        return ruleCenterNodesIds;
//    }

    public double getRGSupport() {
        return RGSupport;
    }

    public double getQ_qGSupport() {
        return Q_qGSupport;
    }

    public void setMarked(int marked) {
        this.marked = marked;
    }

    public int getMarked() {
        return marked;
    }
}
