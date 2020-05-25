package Dmine;

import dataStructures.myNode;

public class Edge {
    private myNode source_node;
    private double label;
    private myNode target_node;

    public Edge(myNode source_node, double label, myNode target_node) {
        this.source_node = source_node;
        this.label = label;
        this.target_node = target_node;
    }

    public myNode getSourceNode() {
        return source_node;
    }

    public double getEdgeLabel() {
        return label;
    }

    public myNode getTargetNode() {
        return target_node;
    }
}
