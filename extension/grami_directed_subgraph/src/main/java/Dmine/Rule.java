package Dmine;

import au.com.d2dcrc.Confidence;
import bisimulation.BisimulationChecker;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import scala.Serializable;
import utilities.MyPair;

import java.util.*;

public class Rule implements Cloneable, Serializable {
    private int centerNodeId; // center node id x
    private double edge_label; // edge label in q(x,y)
    private int target_node_label; // target node id y
    private Graph singleGraph; // single graph
    private myNode centerNode;
    private HPListGraph pattern;
    private HPListGraph former_pattern;
    private List<myNode> extendableNodes; // list of nodes which are extendable
    private List<myNode> judgeExtendable;
    private HashMap<Integer, Integer> judgeInGraph; // prevent repeatedly adding nodes into pattern
    private HashMap<Integer, List<MyPair<String, Integer>>> processMap;

    private Confidence confidence;
    private int support;
    private int RSupport = -1;
    private int Q_qSupport = -1;
    private List<Integer> list = new ArrayList<>();

    private int partition;
    private HashMap<Integer, List<Integer>> partitionIndexesMap;
    private HashMap<Integer, List<HashSet<Integer>>> patternIdsInPartition;

    private int targetNodeId;

    private boolean isExtendable = false;
    private int count = 0; // number of nodes in pattern
    private int centerNode_index = 0;
    private int targetNode_index = -1;

    private List<List<Rule>> allCandidates = new ArrayList<>();

    private List<myEdge> candidateEdges;


    public Rule(int target_node_label, int edge_label, Graph singleGraph,
                Confidence confidence, int support, int partition) {
        this.target_node_label = target_node_label;
        this.edge_label = edge_label;
        this.singleGraph = singleGraph;
        this.processMap = new HashMap<>();
        this.confidence = confidence;
        this.support = support;
        this.partition = partition;
    }

    public Rule(HPListGraph pattern, Graph singleGraph, Confidence confidence,
                HashMap<Integer, List<MyPair<String, Integer>>> processMap, int support, int partition,
                int edge_label, int target_node_label) {
        this.pattern = (HPListGraph) pattern.clone();
        this.former_pattern = (HPListGraph) pattern.clone();
        this.singleGraph = singleGraph;
        this.confidence = confidence;
        this.edge_label = edge_label;
        this.target_node_label = target_node_label;
        if (processMap != null) {
            this.processMap = new HashMap<>(processMap.size());
            for (Map.Entry<Integer, List<MyPair<String, Integer>>> entry : processMap.entrySet()) {
                List<MyPair<String, Integer>> clone = new ArrayList<>(entry.getValue().size());
                clone.addAll(entry.getValue());
                this.processMap.put(entry.getKey(), clone);
            }
        } else {
            this.processMap = new HashMap<>();
        }
        this.support = support;
        this.partition = partition;
        BitSet nodes = pattern.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i)) {
                this.count++;
            }
        }

    }

    public Rule(HPListGraph pattern, Graph singleGraph, HashMap<Integer, Integer> judgeInGraph, List<myNode> extendableNodes,
                HashMap<Integer, List<MyPair<String, Integer>>> processMap, int target_node_label, int edge_label,
                int centerNodeId, int targetNodeId, Confidence confidence, int support,
                int partition) {
        this.pattern = (HPListGraph) pattern.clone();
        former_pattern = (HPListGraph) pattern.clone();
        this.singleGraph = singleGraph;
        this.judgeInGraph = judgeInGraph;
        this.extendableNodes = extendableNodes;
        this.target_node_label = target_node_label;
        this.edge_label = edge_label;
        this.count = judgeInGraph.size();
        this.processMap = new HashMap<>(processMap.size());
        for (Map.Entry<Integer, List<MyPair<String, Integer>>> entry : processMap.entrySet()) {
            List<MyPair<String, Integer>> clone = new ArrayList<>(entry.getValue().size());
            clone.addAll(entry.getValue());
            this.processMap.put(entry.getKey(), clone);
        }
        this.centerNodeId = centerNodeId;
        this.targetNodeId = targetNodeId;
        this.confidence = confidence;
        this.support = support;
    }


    public void setCenterNodeId(int centerNodeId) {
        this.centerNodeId = centerNodeId;
        this.centerNode = singleGraph.getNode(centerNodeId);
    }

    public void setTargetNodeId(int targetNodeId) {
        this.targetNodeId = targetNodeId;
    }


    public void supportCount(HPListGraph candidate) {
        Query query = new Query(candidate);
        HPListGraph rg = getTempRule(candidate);
        Query r = new Query(rg);
        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();
        confidence.setQ(query);
        confidence.setIdOfXInQ(nonCandidates, 1);
        this.list = confidence.getR_GSupportList(r, nonCandidates, 1);
        this.partitionIndexesMap = new HashMap<>();
        partitionIndexesMap.put(this.partition, this.list);
        this.RSupport = list.size();
        this.Q_qSupport = confidence.getQ_qGSupport();
    }


    private boolean checkSupportAndTrivial(HPListGraph pattern) {
        HPListGraph hpListGraph = (HPListGraph) pattern.clone();
        supportCount(hpListGraph);
        boolean isSupport = RSupport >= support;
        boolean isNotTrivial = Q_qSupport != 0;
        return isSupport && isNotTrivial;
        //return isSupport;
    }

    private void checkAndExtend(int index, int target_index, myNode node,
                                MyPair<Integer, Double> pair, Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry, List<MyPair<String, Integer>> processList,
                                HPListGraph candidate) {
        if (candidate.getEdge(index, target_index) == -1) {
            candidate.addEdgeIndex(index, target_index, (double) pair.getB(),1);
            // added on 13/04
            int RSupport = this.RSupport;
            int Q_qSupport = this.Q_qSupport;
            List<Integer> list = this.list;
            if (checkSupportAndTrivial(candidate)) {
                if (!pattern.isValidNode(target_index)) {
                    judgeExtendable.add(singleGraph.getNode((Integer) pair.getA()));
                    pattern.addNodeIndex(entry.getKey());
                    judgeInGraph.put((Integer) pair.getA(), target_index);
                    count++;
                }
                pattern.addEdgeIndex(index, target_index, (double) pair.getB(),1);
                MyPair<String, Integer> processPair = new MyPair<>
                        (String.valueOf((double) pair.getB()) + "_" + String.valueOf(entry.getKey()), target_index);
                processList.add(processPair);

                reverseAdd(pair, entry, index, target_index, node);
            } else {
                this.RSupport = RSupport;
                this.Q_qSupport = Q_qSupport;
                this.list = list;
            }
        }
    }

    private void reverseAdd(MyPair<Integer, Double> pair, Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry,
                            int index, int target_index, myNode node) {
        myNode target = singleGraph.getNode((Integer) pair.getA());
        HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = target.getReachableWithNodes();
        if (with != null && with.containsKey(node.getLabel())) {
            for (MyPair<Integer, Double> checkPair : with.get(node.getLabel())) {
                if (checkPair.getA() == node.getID()) {
                    pattern.addEdgeIndex(index, target_index, (double) checkPair.getB(), -1);
                    List<MyPair<String, Integer>> checkProcessList = processMap.containsKey(target_index) ? processMap.get(target_index) : new ArrayList<>();
                    MyPair<String, Integer> checkProcessPair = new MyPair<>
                            (String.valueOf((double) pair.getB()) + "_" + String.valueOf(entry.getKey()), index);
                    checkProcessList.add(checkProcessPair);
                    processMap.put(target_index, checkProcessList);
                }
            }
        }
    }

    public void firstExtendAndProcess() {
        if (centerNode.getReachableWithNodes() != null) {
            for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : centerNode.getReachableWithNodes().entrySet()) {
                int index = judgeInGraph.get(centerNode.getID());
                List<MyPair<String, Integer>> processList = processMap.containsKey(index) ? processMap.get(index) : new ArrayList<>();
                for (MyPair<Integer, Double> pair : entry.getValue()) {
                    if (!(entry.getKey() == target_node_label && (double) pair.getB() == edge_label)) {
                        judgeExtendable.add(singleGraph.getNode((Integer) pair.getA()));
                        int target_index = pattern.addNodeIndex(entry.getKey());
                        if (pattern.getEdge(index, target_index) == -1) {
                            pattern.addEdgeIndex(index, target_index, (double) pair.getB(),1);
                            MyPair<String, Integer> processPair = new MyPair<>
                                    (String.valueOf((double) pair.getB()) + "_" + String.valueOf(entry.getKey()), target_index);
                            processList.add(processPair);

                            reverseAdd(pair, entry, index, target_index, centerNode);
                        }
                    }
                }
                this.processMap.put(index, processList);
            }
        }
    }

    private void extendAndProcessNew(myNode node) {
        HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = node.getReachableWithNodes();
        if (with != null) {
            for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                int index = judgeInGraph.get(node.getID());
                List<MyPair<String, Integer>> processList = processMap.containsKey(index) ? processMap.get(index) : new ArrayList<>();
                for (MyPair<Integer, Double> pair : entry.getValue()) {
                    if (judgeInGraph.containsKey(pair.getA())) {
                        int target_index = judgeInGraph.get((Integer) pair.getA());
                        if (pattern.getEdge(index, target_index) == -1) {
                            pattern.addEdgeIndex(index, target_index, (double) pair.getB(),1);
                            MyPair<String, Integer> processPair = new MyPair<>
                                    (String.valueOf((double) pair.getB()) + "_" + String.valueOf(entry.getKey()), target_index);
                            processList.add(processPair);
                            reverseAdd(pair, entry, index, target_index, node);
                        }
                    }
                }
                this.processMap.put(index, processList);
            }
        }
    }

    private void extendAndProcessExtra(myNode node) {
        HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = node.getReachableWithNodes();
        if (with != null) {
            for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                int index = judgeInGraph.get(node.getID());
                List<MyPair<String, Integer>> processList = processMap.containsKey(index) ? processMap.get(index) : new ArrayList<>();
                for (MyPair<Integer, Double> pair : entry.getValue()) {
                    HPListGraph candidateGraph = (HPListGraph) pattern.clone();
                    if (!judgeInGraph.containsKey(pair.getA())) {
                        int target_index = candidateGraph.addNodeIndex(entry.getKey());
                        if ((Integer) pair.getA() == targetNodeId) {
                            targetNode_index = target_index;
                        }
                        checkAndExtend(index, target_index, node, pair, entry, processList, candidateGraph);
                    }
                }
                this.processMap.put(index, processList);
            }
        }
    }


    private void judgeSupport() {
        if (this.RSupport == -1 || this.Q_qSupport == -1) {
            HPListGraph candidate = (HPListGraph) pattern.clone();
            supportCount(candidate);
        }
    }

    private List<List<Edge>> dealWithEdges() {
        List<Edge> edges = new ArrayList<>();
        for (myNode node : extendableNodes) {
            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = node.getReachableWithNodes();
            if (with != null) {
                for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                    for (MyPair<Integer, Double> pair : entry.getValue()) {
                        if (!(entry.getKey() == target_node_label && pair.getB() == edge_label && node.getID() == centerNodeId)) {
                            Edge edge = new Edge(node, pair.getB(), singleGraph.getNode(pair.getA()));
                            edges.add(edge);
                        }
                    }
                }
            }
        }
        List<List<Edge>> first = new ArrayList<>(edges.size());
        for (Edge edge : edges) {
            List<Edge> temp = new ArrayList<>(1);
            temp.add(edge);
            first.add(temp);
        }
        return first;
    }

    private boolean latticeExpand(Rule rule, List<Edge> oneSetEdges) {
        HPListGraph pattern = rule.getPattern();
        HashMap<Integer, List<MyPair<String, Integer>>> processMap = rule.getProcessMap();
        HashMap<Integer, Integer> judgeInGraph = rule.getJudgeInGraph();
        for (Edge edge : oneSetEdges) {
            int index;
            if (!judgeInGraph.containsKey(edge.getSourceNode().getID())) {
                index = pattern.addNodeIndex(target_node_label);
                rule.targetNode_index = index;
                rule.count++;
                judgeInGraph.put(edge.getSourceNode().getID(), index);
            } else {
                index = judgeInGraph.get(edge.getSourceNode().getID());
            }
            List<MyPair<String, Integer>> processList = processMap.containsKey(index) ? processMap.get(index) : new ArrayList<>();
            int target_index;
            if (!judgeInGraph.containsKey(edge.getTargetNode().getID())) {
                target_index = pattern.addNodeIndex(edge.getTargetNode().getLabel());
                rule.count++;
                judgeInGraph.put(edge.getTargetNode().getID(), target_index);
            } else {
                target_index = judgeInGraph.get(edge.getTargetNode().getID());
            }
            if (pattern.getEdge(index, target_index) != -1) {
                return false;
            }
            pattern.addEdgeIndex(index, target_index, edge.getEdgeLabel(), 1);
            processList.add(new MyPair<>
                    (String.valueOf(edge.getEdgeLabel()) + "_" + String.valueOf(edge.getTargetNode().getLabel()), target_index));
            processMap.put(index, processList);

            // reverse
            List<MyPair<String, Integer>> processListReverse = processMap.containsKey(target_index) ? processMap.get(target_index) : new ArrayList<>();
            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = edge.getTargetNode().getReachableWithNodes();
            if (with != null) {
                for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                    if (entry.getKey() == edge.getSourceNode().getLabel()) {
                        for (MyPair<Integer, Double> pair : entry.getValue()) {
                            if (pair.getA() == edge.getSourceNode().getID()) {
                                pattern.addEdgeIndex(index, target_index, pair.getB(), -1);
                                processListReverse.add(new MyPair<>
                                        (String.valueOf(pair.getB()) + "_" + String.valueOf(edge.getSourceNode().getLabel()), index));
                                break;
                            }
                        }
                    }
                }
                if (processListReverse != null) {
                    processMap.put(target_index, processListReverse);
                }
            }
        }
        rule.supportCount(pattern);

        // needed revising
        return rule.getRSupport() >= support;
    }



    private List<List<Edge>> latticeNextExpand(List<List<Edge>> curExpand, int k) {
        List<Rule> candidates = new ArrayList<>();
        List<List<Edge>> candidateEdges = new ArrayList<>();
        List<List<Edge>> next = new ArrayList<>();
        for (List<Edge> edgeList : curExpand) {
            Rule rule = null;
            try {
                rule = this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (latticeExpand(rule, edgeList)) {
                candidates.add(rule);
                candidateEdges.add(edgeList);
            }
        }
        allCandidates.add(candidates);
        for (int i = 0; i < candidateEdges.size(); i++) {
            for (int j = i + 1; j < candidateEdges.size(); j++) {
                List<Edge> temp1 = new ArrayList<>();
                List<Edge> temp2 = new ArrayList<>();
                temp1.addAll(candidateEdges.get(i).subList(0, k - 1));
                temp2.addAll(candidateEdges.get(j).subList(0, k - 1));
                if (temp1.containsAll(temp2) && temp2.containsAll(temp1)) {
                    List<Edge> candidate = new ArrayList<>();
                    candidate.addAll(temp1);
                    candidate.add(candidateEdges.get(i).get(candidateEdges.get(i).size() - 1));
                    candidate.add(candidateEdges.get(j).get(candidateEdges.get(j).size() - 1));
                    next.add(candidate);
                }
            }
        }
        return next;
    }

    private boolean expandSingleEdge(Rule rule, myEdge edge, int source_index, int target_index) {
        HPListGraph pattern = rule.getPattern();
        HashMap<Integer, List<MyPair<String, Integer>>> processMap = rule.getProcessMap();
        List<MyPair<String, Integer>> processList = processMap.containsKey(source_index) ? processMap.get(source_index) : new ArrayList<>();

        if (pattern.getEdge(source_index, target_index) != -1) return false;
        // expand
        pattern.addEdgeIndex(source_index, target_index, edge.getLabel(), 1);
        processList.add(new MyPair<>
                (String.valueOf(edge.getLabel()) + "_" + String.valueOf(edge.getTargetLabel()), target_index));
        processMap.put(source_index, processList);
        // reverse
        if (edge.isReversable()) {
            List<MyPair<String, Integer>> processReverseList = processMap.containsKey(target_index) ? processMap.get(target_index) : new ArrayList<>();
            pattern.addEdgeIndex(source_index, target_index, edge.getLabel(), -1);
            processReverseList.add(new MyPair<>
                    (String.valueOf(edge.getLabel()) + "_" + String.valueOf(edge.getSourceLabel()), source_index));
            processMap.put(target_index, processReverseList);
        }

        return true;
    }

    private boolean latticeExpandNew(Rule rule, List<myEdge> oneSetEdges) {
        HPListGraph pattern = rule.getPattern();
        for (myEdge edge : oneSetEdges) {
            int source_index;
            if (edge.getSourceIndex() == -1) {
                if (rule.targetNode_index == -1) {
                    source_index = pattern.addNodeIndex(target_node_label);
                    rule.count++;
                    rule.targetNode_index = source_index;
                } else {
                    source_index = rule.targetNode_index;
                }
            }else {
                source_index = edge.getSourceIndex();
            }

            int target_index;
            if (!edge.isIncludedInPattern()) {
                target_index = pattern.addNodeIndex(edge.getTargetLabel());
                rule.count++;

                if (edge.isPointedToTarget()) {
                    if (rule.targetNode_index == -1) {
                        rule.targetNode_index = target_index;
                    }
                }
            } else {
              target_index = edge.getTargetIndex();
            }

            if (!expandSingleEdge(rule, edge, source_index, target_index)) return false;
        }

        rule.supportCount(pattern);

        return rule.getRSupport() >= support;
    }


    private List<List<myEdge>> latticeNextExpandNew(List<List<myEdge>> curExpand, int k) {
        List<Rule> candidates = new ArrayList<>();
        List<List<myEdge>> candidateEdges = new ArrayList<>();
        List<List<myEdge>> next = new ArrayList<>();
        for (List<myEdge> edges : curExpand) {
            Rule rule = null;
            try {
                rule = this.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (latticeExpandNew(rule, edges)) {
                candidates.add(rule);
                candidateEdges.add(edges);
            }
        }
        allCandidates.add(candidates);

        for (int i = 0; i < candidateEdges.size(); i++) {
            for (int j = i + 1; j < candidateEdges.size(); j++) {
                List<myEdge> temp1 = new ArrayList<>();
                List<myEdge> temp2 = new ArrayList<>();
                temp1.addAll(candidateEdges.get(i).subList(0, k - 1));
                temp2.addAll(candidateEdges.get(j).subList(0, k - 1));
                if (temp1.containsAll(temp2) && temp2.containsAll(temp1)) {
                    List<myEdge> candidate = new ArrayList<>();
                    candidate.addAll(temp1);
                    candidate.add(candidateEdges.get(i).get(candidateEdges.get(i).size() - 1));
                    candidate.add(candidateEdges.get(j).get(candidateEdges.get(j).size() - 1));
                    next.add(candidate);
                }
            }
        }
        return next;

    }


    private List<List<myEdge>> dealWithEdgesNew() {
        List<List<myEdge>> result = new ArrayList<>();
        for (myEdge edge : this.candidateEdges) {
            List<myEdge> element = new ArrayList<>(1);
            element.add(edge);
            result.add(element);
        }
        return result;
    }


    public void expandPattern() {
        int k = 1;
        List<List<myEdge>> next = dealWithEdgesNew();
        while (next.size() != 0) {
            next = latticeNextExpandNew(next, k++);
        }
    }


    public void updateExtendableNodesNew() {
        if (extendableNodes == null) {
            extendableNodes = new ArrayList<>();
            pattern = new HPListGraph();
            judgeInGraph = new HashMap<>(1);
            int index = pattern.addNodeIndex(centerNode.getLabel());
            former_pattern = (HPListGraph) pattern.clone();
            centerNode_index = index;
            count = 1;
            judgeInGraph.put(centerNodeId, index);
            extendableNodes.add(centerNode);
        } else {
            myNode node = singleGraph.getNode(targetNodeId);
            if (!extendableNodes.contains(node) && !judgeInGraph.containsKey(targetNodeId)) {
                extendableNodes.add(node);
            }
        }
        int k = 1;
        List<List<Edge>> next = dealWithEdges();
        while (next.size() != 0) {
            next = latticeNextExpand(next, k++);
        }
    }

    public void assemble(Rule rule) {
        this.RSupport += rule.RSupport;
        this.Q_qSupport += rule.Q_qSupport;
        for (Map.Entry<Integer, List<Integer>> entry : rule.partitionIndexesMap.entrySet()) {
            this.partitionIndexesMap.put(entry.getKey(), entry.getValue());
        }
    }


    public void updateExtendableNodes() {
        judgeExtendable = new ArrayList<>();
        if (extendableNodes == null) {
            extendableNodes = new ArrayList<>();
            pattern = new HPListGraph();
            judgeInGraph = new HashMap<>();
            int index = pattern.addNodeIndex(centerNode.getLabel());
            former_pattern = (HPListGraph) pattern.clone();
            centerNode_index = index;
            count = 1;
            judgeInGraph.put(centerNodeId, index);
            firstExtendAndProcess();
        }
        else {
            for (Map.Entry<Integer, Integer> entry : judgeInGraph.entrySet()) {
                if (entry.getKey() == targetNodeId) targetNode_index = entry.getValue();
            }
            for (myNode node : extendableNodes) {
                //extendAndProcess(node);
                extendAndProcessNew(node);
            }
            for (myNode node : extendableNodes) {
                extendAndProcessExtra(node);
            }
            // make a difference only during round 2
            myNode node = singleGraph.getNode(targetNodeId);
            if (judgeInGraph.containsKey(targetNodeId)) {
                extendAndProcessNew(node);
                extendAndProcessExtra(node);
            }else {
                int target_index = pattern.addNodeIndex(target_node_label);
                targetNode_index = target_index;
                judgeInGraph.put(targetNodeId, target_index);
                extendAndProcessNew(node);
                extendAndProcessExtra(node);
            }
        }
        judgeSupport();

        // needed revising
        this.isExtendable = true;
    }

    private boolean isExtendable() {
        for (myNode node:judgeExtendable) {
            int index = judgeInGraph.get(node.getID());
            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> with = node.getReachableWithNodes();
            if (with == null) continue;
            for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> entry : with.entrySet()) {
                for (MyPair pair : entry.getValue()) {
                    // needed revising
                    int target_index = judgeInGraph.containsKey((Integer) pair.getA()) ? judgeInGraph.get((Integer) pair.getA()) : -1;
                    if (pattern.getEdge(index, target_index) == -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setPatternIdsInPartition(HashMap<Integer, List<HashSet<Integer>>> map) {
        this.patternIdsInPartition = map;
    }

    public HashMap<Integer, List<HashSet<Integer>>> getPatternIdsInPartition() {
        return patternIdsInPartition;
    }

    private HPListGraph getTempRule(HPListGraph pattern) {
        // temp here is to reset target node index
        int temp = targetNode_index;
        HPListGraph rule = (HPListGraph) pattern.clone();
        if (targetNode_index == -1) targetNode_index = rule.addNodeIndex(target_node_label);
        rule.addEdgeIndex(centerNode_index, targetNode_index, edge_label, 1);

        targetNode_index = temp;
        return rule;
    }

    public int getTargetNode_index() {
        return this.targetNode_index;
    }

    public HPListGraph getRule() {
        return getTempRule(pattern);
    }

    public void setIsExtendable(boolean isExtendable) {
        this.isExtendable = isExtendable;
    }

    public int getCenterNodeId() {
        return centerNodeId;
    }

    public HPListGraph getPattern() {
        return pattern;
    }

    public List<myNode> getExtendableNodes() {
        return extendableNodes;
    }

    public HashMap<Integer, List<MyPair<String, Integer>>> getProcessMap() {
        return processMap;
    }

    public int getCount() {
        return count;
    }

    public HPListGraph getFormer_pattern() {
        return former_pattern;
    }

    public Graph getSingleGraph() {
        return singleGraph;
    }

    public boolean getIsExtendable() {
        return isExtendable;
    }

    public int getRSupport() {
        return RSupport;
    }

    public int getQ_qSupport() {
        return Q_qSupport;
    }

    public void setList(List<Integer> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    public List<Integer> getList() {
        return list;
    }

    public HashMap<Integer, Integer> getJudgeInGraph() {
        return judgeInGraph;
    }

//    public void addCount() {
//        this.count++;
//    }

    public HashMap<Integer, List<Integer>> getPartitionIndexesMap() {
        HashMap<Integer, List<Integer>> map = new HashMap<>(this.partitionIndexesMap.size());
        for (Map.Entry<Integer, List<Integer>> entry : this.partitionIndexesMap.entrySet()) {
            List<Integer> list = new ArrayList<>(entry.getValue().size());
            list.addAll(entry.getValue());
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public int getPartition() {
        return this.partition;
    }

    public List<List<Rule>> getCandidates() {
        List<List<Rule>> opt = new ArrayList<>();
        for (int i = allCandidates.size()/2 - 1; i < allCandidates.size(); i++) {
            opt.add(allCandidates.get(i));
        }
        return opt;
    }

    public void setCandidateEdges(List<myEdge> edges) {
        this.candidateEdges = edges;
    }

    public List<myEdge> getCandidateEdges() {
        return this.candidateEdges;
    }

    public Rule clone() throws CloneNotSupportedException {
        Rule rule = (Rule) super.clone();
        rule.pattern = (HPListGraph) pattern.clone();
        rule.judgeInGraph = judgeInGraph == null ? new HashMap<>() : (HashMap<Integer, Integer>) judgeInGraph.clone();
        rule.processMap = new HashMap<>(processMap.size());
        for (Map.Entry<Integer, List<MyPair<String, Integer>>> entry : processMap.entrySet()) {
            List<MyPair<String, Integer>> clone = new ArrayList<>(entry.getValue().size());
            clone.addAll(entry.getValue());
            rule.processMap.put(entry.getKey(), clone);
        }
        List<Integer> list = new ArrayList<>();
        list.addAll(this.list);
        rule.list = list;

        return rule;
    }

//    @Override
//    public boolean equals(Object obj) {
//        Rule rule = (Rule) obj;
//        BisimulationChecker checker = new BisimulationChecker(this, rule);
//        if (checker.isBisimulation()) {
//            if (this.count <= rule.count) {
//                rule.code = this.code;
//            } else {
//                this.code = rule.code;
//            }
//        }
//        return checker.isBisimulation();
//    }
//
//    @Override
//    public int hashCode() {
//        return Integer.valueOf(this.code);
//    }
}
