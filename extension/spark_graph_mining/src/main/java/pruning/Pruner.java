package pruning;

import CSP.Variable;
import dataStructures.myNode;
import dataStructures.myGraph;
import edu.isi.karma.rep.alignment.Label;

import java.util.*;

public class Pruner {
    private List<Variable> variables;
    private int queryNodesCount;

    public Pruner(){}

    public void prunProcess(myGraph singleGraph, myGraph queryGraph) {
        HashMap<String, HashMap<String, myNode>> pruned = new HashMap<>();
        Set<myNode> queryNodesSet = queryGraph.vertexSet();
        queryNodesCount = queryNodesSet.size();

        // refine according to node_label
        for (myNode node : queryNodesSet) {
            HashMap<String, myNode> distinctMap = new HashMap<>();
            distinctMap.putAll(singleGraph.getNodesByLabel().get(node.getLabel()));
            pruned.put(node.getId(), distinctMap);
        }

        // refine according to degree
        HashMap<String, HashMap<Label, HashMap<Label, Integer>>> outMap = queryGraph.OutMap();
        HashMap<String, HashMap<Label, HashMap<Label, Integer>>> inMap = queryGraph.InMap();
        for (myNode node : queryNodesSet) {
            HashMap<Label, HashMap<Label, Integer>> degreeOutCons = outMap.get(node.getId());
            HashMap<Label, HashMap<Label, Integer>> degreeInCons = inMap.get(node.getId());
            HashMap<String, myNode> candidates = pruned.get(node.getId());
            boolean isValid = true;

            for (Iterator<Map.Entry<String, myNode>> entryIterator = candidates.entrySet().iterator(); entryIterator.hasNext(); ) {
                Map.Entry<String, myNode> entry = entryIterator.next();
                myNode candidateNode = entry.getValue();
                isValid = true;
                if (degreeOutCons != null) {
                    for (Map.Entry<Label, HashMap<Label, Integer>> e : degreeOutCons.entrySet()) {
                        Label label = e.getKey();
                        boolean flag = true;
                        HashMap<Label, Integer> edge_label_degreeMap = e.getValue();
                        for (Map.Entry<Label, Integer> degreeMap : edge_label_degreeMap.entrySet()) {
                            Label edge_label = degreeMap.getKey();
                            int degree = degreeMap.getValue();
                            if (singleGraph.outDegreeOfByNodeLabelAndEdgeLabel(candidateNode, label, edge_label) < degree) {
                                isValid = false;
                                flag = false;
                                break;
                            }
                        }
                        if (!flag) break;
                    }
                }
                if (isValid && degreeInCons != null) {
                    for (Map.Entry<Label, HashMap<Label, Integer>> e : degreeInCons.entrySet()) {
                        Label label = e.getKey();
                        boolean flag = true;
                        HashMap<Label, Integer> edge_label_degreeMap = e.getValue();
                        for (Map.Entry<Label, Integer> degreeMap : edge_label_degreeMap.entrySet()) {
                            Label edge_label = degreeMap.getKey();
                            int degree = degreeMap.getValue();
                            if (singleGraph.inDegreeOfByNodeLabelAndEdgeLabel(candidateNode, label, edge_label) < degree) {
                                isValid = false;
                                flag = false;
                                break;
                            }
                        }
                        if (!flag) break;
                    }
                }
                if (!isValid) {
                    entryIterator.remove();
                }
            }
//        HashMap<String, HashMap<Label, Integer>> inMap=queryGraph.getInMap();
//        HashMap<String, HashMap<Label, Integer>> outMap=queryGraph.getOutMap();
//        for (myNode node : queryNodesSet) {
//            HashMap<Label, Integer> degreeInCons = inMap.get(node.getId());
//            HashMap<Label, Integer> degreeOutCons = outMap.get(node.getId());
//
//            HashMap<Label, HashMap<Label, Integer>> newDegreeOutCons=newOutMap.get(node.getId());
//
//            HashMap<String, myNode> candidates = pruned.get(node.getId());
//            boolean isValid = true;
//
//            for (Iterator<Map.Entry<String, myNode>> entryIterator = candidates.entrySet().iterator(); entryIterator.hasNext(); ) {
//                Map.Entry<String, myNode> entry = entryIterator.next();
//                myNode candidateNode = entry.getValue();
//                isValid = true;
//                if (degreeOutCons != null) {
//                    for (Map.Entry<Label, Integer> e : degreeOutCons.entrySet()) {
//                        Label label = e.getKey();
//                        int degree = e.getValue();
//                        if (singleGraph.outDegreeOfByLabel(candidateNode, label) < degree) {
//                            isValid = false;
//                            break;
//                        }
//                    }
//                }
//                if (isValid && degreeInCons != null) {
//                    for (Map.Entry<Label, Integer> e : degreeInCons.entrySet()) {
//                        Label label = e.getKey();
//                        int degree = e.getValue();
//                        if (singleGraph.inDegreeOfByLabel(candidateNode, label) < degree) {
//                            isValid = false;
//                            break;
//                        }
//                    }
//                }
//                if (!isValid){
//                    entryIterator.remove();
//                }
//            }
//            int inDegree=queryGraph.inDegreeOf(node);
//            int outDegree=queryGraph.outDegreeOf(node);
//            for (Iterator<Map.Entry<String, myNode>> entryIterator = candidates.entrySet().iterator(); entryIterator.hasNext();){
//                boolean isValid=true;
//                Map.Entry<String, myNode> entry=entryIterator.next();
//                myNode candidateNode=entry.getValue();
//                if (outDegree!=0&&singleGraph.outDegreeOf(candidateNode)<outDegree){
//                    isValid=false;
//                    entryIterator.remove();
//                }
//                if (isValid&&inDegree!=0&&singleGraph.inDegreeOf(candidateNode)<inDegree){
//                    entryIterator.remove();
//                }
//            }
            }

        // set variable
        variables=new ArrayList<>(pruned.size());
        for (Map.Entry<String, HashMap<String, myNode>> entry:pruned.entrySet()) {
            String node_id=entry.getKey();
            HashMap<String, myNode> domain=entry.getValue();
            Variable variable=new Variable(node_id,domain);
            variable.setOut_node_id_list(queryGraph.getOutNodeIds(node_id));

            variables.add(variable);
        }

    }

    public List<Variable> getVariables() {
        return variables;
    }
}
