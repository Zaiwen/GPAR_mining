package Dmine;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import au.com.d2dcrc.Confidence;
import bisimulation.BisimulationChecker;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Node;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import scala.Serializable;
import utilities.MyPair;

import java.util.*;

public class dminePartition implements Serializable {
    private int partition;
    private Graph graph;
    private int qGSupport;
    private int _qGSupport;
    private Confidence conf;
    private HashMap<Integer, Integer> sourceToTarget;
    private int source_label;
    private int edge_label;
    private int target_label;
    private int support;
    private List<List<Rule>> partitionRules;

    private List<Rule> candidates;


    public dminePartition(int partition, Graph graph, Confidence conf,
                          int qGSupport, int _qGSupport, HashMap<Integer, Integer> sourceToTarget,
                          int edge_label, int target_label, int support,
                          int source_label) {
        this.partition = partition;
        this.graph = graph;
        this.conf = conf;
        this.qGSupport = qGSupport;
        this._qGSupport = _qGSupport;
        this.sourceToTarget = sourceToTarget;
        this.edge_label = edge_label;
        this.target_label = target_label;
        this.support = support;
        this.source_label = source_label;

    }

    public void setCandidates(List<Rule> candidates) {
        this.candidates = candidates;
    }


    private List<Rule> localAutomorphismChecking() {
        List<Rule> candidates = new ArrayList<>();
        for (List<Rule> rules : partitionRules) {
            candidates.addAll(rules);
        }
        long start = System.currentTimeMillis();
        automorphismCheck(candidates);
        System.out.println("It costs while bi-simulation locally："+(System.currentTimeMillis() - start) +"ms");
        return candidates;
    }

//    private List<Rule> getDiff(List<Rule> list1, List<Rule> list2) {
//        List<Rule> diff = new ArrayList<>();
//        Map<Rule, Integer> map = new HashMap<Rule, Integer>(list1.size() + list2.size());
//        List<Rule> max = list1;
//        List<Rule> min = list2;
//        if (list2.size() > list1.size()) {
//            max = list2;
//            min = list1;
//        }
//        for (Rule rule : max) {
//            map.put(rule, 1);
//        }
//        for (Rule rule : min) {
//            Integer count = map.get(rule);
//            if (count != null) {
//
//            }
//        }
//    }

//    private List<Rule> newLocalAutoChecking() {
//        long start = System.currentTimeMillis();
//        Map<Rule, Integer> map = new HashMap<Rule, Integer>();
//
//    }

    // automorphism check method
    // option 0 stands for the local while 1 stands for partition
    private List<Rule> automorphismCheck(List<Rule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                BisimulationChecker checker = new BisimulationChecker(rules.get(i), rules.get(j));
                if (checker.isBisimulation()) {
                    // remove the bigger one
                    if (rules.get(i).getCount() <= rules.get(j).getCount()) {
                        rules.get(i).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.remove(rules.get(j));
                        j--;
                    }else {
                        rules.get(j).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.remove(rules.get(i));
                        i--;
                        break;
                    }
                }
            }
        }

        return rules;
    }

    // <Integer, List<Integer>> first para stands for index in pattern while list stands for matching index in graph
    private void joinEdgesByNode(HashMap<Integer, List<Integer>> map) {

    }
//
//    public List<Rule> first() {
//
//
//    }


    public List<Rule> firstMining() {
        partitionRules = new ArrayList<>(conf.getIdOfXInq().size());
        long start = System.currentTimeMillis();
        for (int id : conf.getIdOfXInq()) {
            List<Rule> extendRules = new ArrayList<>();
            Rule rule = new Rule(target_label, edge_label, graph, conf, support, this.partition);
            rule.setCenterNodeId(id);
            rule.setTargetNodeId(sourceToTarget.get(id));
            rule.updateExtendableNodesNew();
            List<List<Rule>> candidates = rule.getCandidates();
            for (List<Rule> rules : candidates) {
                extendRules.addAll(rules);
            }
            partitionRules.add(extendRules);
        }
        System.out.println("It costs while extending to mine frequent patterns locally：" + (System.currentTimeMillis() - start) + "ms");

//        JavaRDD<Integer> centerIds = sc.parallelize(conf.getIdOfXInq());
//        JavaRDD<List<Rule>> rules = centerIds
//                .flatMap((FlatMapFunction<Integer, List<Rule>>) id -> {
//                    List<List<Rule>> result = new ArrayList<>();
//                    List<Rule> extendRules = new ArrayList<>();
//                    Rule rule = new Rule(target_label, edge_label, graph, conf, support, this.partition);
//                    rule.setCenterNodeId(id);
//                    rule.setTargetNodeId(sourceToTarget.get(id));
//                    rule.updateExtendableNodesNew();
//                    List<List<Rule>> candidates = rule.getCandidates();
//                    for (List<Rule> r : candidates) {
//                        extendRules.addAll(r);
//                    }
//                    result.add(extendRules);
//                    return result.iterator();
//                });
//
//        partitionRules.addAll(rules.collect());

        return localAutomorphismChecking();
    }

    private void patternIdsInGraph() {
        for (Rule rule : candidates) {
            Query query = new Query(rule.getFormer_pattern());
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            ConstraintGraph cg = new ConstraintGraph(graph, query, nonCandidates);
            HashMap<Integer, List<HashSet<Integer>>> map = new HashMap<>(rule.getList().size());
            for (int index : rule.getList()) {
                DFSSearch search = new DFSSearch(cg, graph.getFreqThreshold(), nonCandidates);
                search.searchAll(index);
                List<HashSet<Integer>> list = new ArrayList<>();
                for (Map<Node, Node> nodeMap : search.getIsomorphismList()) {
                    HashSet<Integer> set = new HashSet<>(nodeMap.size());
                    for (Map.Entry<Node, Node> entry : nodeMap.entrySet()) {
                        set.add(Integer.valueOf(entry.getValue().getId()));
                    }
                    list.add(set);
                }
                map.put(index, list);
            }
            rule.setPatternIdsInPartition(map);
        }
    }


    // in order to have the ultimate candidate edges for lattice structure
    private List<myEdge> assembleEdges(List<List<myEdge>> edges) {
        List<myEdge> result = new ArrayList<>();
        result.addAll(edges.get(0));
        for (int i = 1; i < edges.size(); i++) {
            for (myEdge edge : edges.get(i)) {
                if (!result.contains(edge)) {
                    result.add(edge);
                }
            }
        }
        return result;
    }

    // deal with single isomorphism
    private List<myEdge> edges(List<myNode> extendableNodes, HashMap<Integer, Integer> judgeInMap, boolean isFirst, int targetId) {
        List<myEdge> result = new ArrayList<>();
        for (myNode node : extendableNodes) {
            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = node.getReachableWithNodes();
            if (with != null) {
                for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                    for (MyPair<Integer, Double> pair : entry.getValue()) {
                        // pair.A stands for target id in graph, pair.B stands for edge label while entry.key accounts for target node label
                        if (!(entry.getKey() == target_label && pair.getB() == edge_label && isFirst)) {
                            myEdge edge = judgeInMap.containsKey(node.getID()) ?
                                    new myEdge(judgeInMap.get(node.getID()), pair.getB(), entry.getKey(), node.getLabel()) :
                                    new myEdge(-1, pair.getB(), entry.getKey(), node.getLabel());
                            // target node exists in pattern
                            if (judgeInMap.containsKey(pair.getA())) {
                                edge.setIncludedInPattern(true);
                                edge.setTargetIndex(judgeInMap.get(pair.getA()));
                            } else {
                                edge.setIncludedInPattern(false);
                                if (targetId != -1 && pair.getA() == targetId) edge.setPointedToTarget(true);
                            }


                            // reverse
                            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> by = graph.getNode(pair.getA()).getReachableWithNodes();
                            if (by != null && by.containsKey(node.getLabel())) {
                                for (MyPair<Integer, Double> p : by.get(node.getLabel())) {
                                    if (p.getB().equals(pair.getB()) && p.getA() == node.getID()) {
                                        edge.setReversable(true);
                                    }
                                }
                            }

                            result.add(edge);
                        }
                    }
                }
            }
        }

        return result;
    }

    // prepare candidate edges for each candidate rule
    private void dealWithEdges() {
        for (Rule rule : candidates) {
            HPListGraph pattern = rule.getPattern();
            Query query = new Query(pattern);
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            ConstraintGraph cg = new ConstraintGraph(graph, query, nonCandidates);
            // gathering last round's pattern indexes
            HPListGraph former = rule.getFormer_pattern();
            BitSet nodes = former.getNodes();
            List<Integer> formerIndex = new ArrayList<>(nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i)) {
                    formerIndex.add(i);
                }
            }
            List<List<myEdge>> tempList = new ArrayList<>();
            for (int center_index : rule.getList()) {
                DFSSearch search = new DFSSearch(cg, graph.getFreqThreshold(), nonCandidates);
                search.searchAll(center_index);
                for (Map<Node, Node> map : search.getIsomorphismList()) {
                    HashMap<Integer, Integer> judgeInMap = new HashMap<>(map.size());
                    // temp stands for map<index in pattern, id in graph>
                    HashMap<Integer, Integer> temp = new HashMap<>(map.size());
                    // curIndex stands for current round's pattern indexes
                    List<Integer> curIndex = new ArrayList<>(map.size());
                    for (Map.Entry<Node, Node> entry : map.entrySet()) {
                        judgeInMap.put(Integer.valueOf(entry.getValue().getId()), Integer.valueOf(entry.getKey().getId()));
                        temp.put(Integer.valueOf(entry.getKey().getId()), Integer.valueOf(entry.getValue().getId()));
                        curIndex.add(Integer.valueOf(entry.getKey().getId()));
                    }
                    // curIndex now stands for extendable indexes
                    curIndex.removeAll(formerIndex);
                    // update extendable nodes in graph
                    List<myNode> extendableNodes = new ArrayList<>(curIndex.size());
                    for (int index : curIndex) {
                        extendableNodes.add(graph.getNode(temp.get(index)));
                    }
                    // if curPattern does not contain target node, then add it to extendable nodes
                    int targetId = -1;
                    if (rule.getTargetNode_index() == -1) {
                        targetId = this.sourceToTarget.get(center_index);
                        extendableNodes.add(graph.getNode(targetId));
                    }

                    List<myEdge> single = edges(extendableNodes, judgeInMap, false, targetId);
                    tempList.add(single);
                }
            }

            rule.setCandidateEdges(assembleEdges(tempList));
        }
    }

    public List<Rule> firstMiningNew() {
        partitionRules = new ArrayList<>();
        List<List<myEdge>> temp = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int id : conf.getIdOfXInq()) {
            List<myNode> extendableNodes = new ArrayList<>(1);
            extendableNodes.add(graph.getNode(id));
            HashMap<Integer, Integer> judgeInMap = new HashMap<>(1);
            judgeInMap.put(id, 0);
            List<myEdge> single = edges(extendableNodes, judgeInMap, true,-1);
            temp.add(single);
        }
        HPListGraph pattern = new HPListGraph();
        pattern.addNodeIndex(source_label);
        Rule rule = new Rule(pattern, graph, conf, null, support, this.partition, edge_label, target_label);
        rule.setCandidateEdges(assembleEdges(temp));

        rule.expandPattern();

        List<Rule> extendRules = new ArrayList<>();
        List<List<Rule>> candidates = rule.getCandidates();
        for (List<Rule> rules : candidates) {
            extendRules.addAll(rules);
        }

        partitionRules.add(extendRules);

        System.out.println("It costs while extending to mine frequent patterns locally：" + (System.currentTimeMillis() - start) + "ms");

        return localAutomorphismChecking();
    }

    public List<Rule> nextMiningNew() {
        partitionRules = new ArrayList<>();
        dealWithEdges();
        long start = System.currentTimeMillis();
        for (Rule rule : candidates) {
            Rule candidate = new Rule(rule.getPattern(), graph, conf,
                    rule.getProcessMap(), support, this.partition, edge_label, target_label);
            candidate.setCandidateEdges(rule.getCandidateEdges());

            candidate.expandPattern();

            List<Rule> extendRules = new ArrayList<>();
            List<List<Rule>> candidates = candidate.getCandidates();
            for (List<Rule> rules : candidates) {
                extendRules.addAll(rules);
            }

            partitionRules.add(extendRules);
        }

        System.out.println("It costs while extending to mine frequent patterns locally：" + (System.currentTimeMillis() - start) + "ms");

        return localAutomorphismChecking();
    }


    public List<Rule> nextMining() {
        partitionRules = new ArrayList<>();
        patternIdsInGraph();
        long start = System.currentTimeMillis();
        for (Rule rule : candidates) {
            HPListGraph pattern = rule.getPattern();
            Query query = new Query(pattern);
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            ConstraintGraph cg = new ConstraintGraph(graph, query, nonCandidates);
            HashMap<Integer, List<HashSet<Integer>>> patternIndexesInGraph = rule.getPatternIdsInPartition();
            for (int center_index : rule.getList()) {
                DFSSearch search = new DFSSearch(cg, graph.getFreqThreshold(), nonCandidates);
                search.searchAll(center_index);
                for (Map<Node, Node> map : search.getIsomorphismList()) {
                    List<Rule> extendRules = new ArrayList<>();
                    HashMap<Integer, Integer> judgeInMap = new HashMap<>(map.size());
                    HashSet<Integer> temp = new HashSet<>(map.size());
                    for (Map.Entry<Node, Node> entry : map.entrySet()) {
                        judgeInMap.put(Integer.valueOf(entry.getValue().getId()), Integer.valueOf(entry.getKey().getId()));
                        temp.add(Integer.valueOf(entry.getValue().getId()));
                    }
                    for (HashSet<Integer> set : patternIndexesInGraph.get(center_index)) {
                        if (temp.containsAll(set)) {
                            temp.removeAll(set);
                        }
                    }
                    List<myNode> extendableNodes = new ArrayList<>(temp.size());
                    for (Integer id : temp) {
                        extendableNodes.add(graph.getNode(id));
                    }
                    Rule candidate = new Rule(pattern, graph, judgeInMap,
                            extendableNodes, rule.getProcessMap(), target_label,
                            edge_label, center_index, sourceToTarget.get(center_index),
                            conf, support, this.partition);
                    candidate.updateExtendableNodesNew();
                    List<List<Rule>> candidates = candidate.getCandidates();
                    for (List<Rule> rules : candidates) {
                        extendRules.addAll(rules);
                    }
                    partitionRules.add(extendRules);
                }
            }
        }

        System.out.println("扩展耗时：" + (System.currentTimeMillis() - start) + "毫秒");
        return localAutomorphismChecking();

    }



    public int getqGSupport() {
        return qGSupport;
    }

    public int get_qGSupport() {
        return _qGSupport;
    }

    public int getPartition() {
        return partition;
    }
}
