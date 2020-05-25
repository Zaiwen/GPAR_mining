package CSP;

import dataStructures.myGraph;
import dataStructures.myNode;

import java.util.List;

public class DFSSearch {
    private Variable[] variables;
    private myGraph singleGraph;
    private boolean isVisited[];


    public DFSSearch(myGraph singleGraph, List<Variable> variables){
        this.singleGraph=singleGraph;
        isVisited=new boolean[variables.size()];
        this.variables=variables.toArray(new Variable[variables.size()]);
    }

    public Variable[] getVariables() {
        return variables;
    }

    private int getFirstNeighbor(int index) {
        Variable variable=variables[index];
        List<String> outList=variable.getOut_node_id_list();
        for (String out_id:outList) {
            for (int i=0;i<variables.length;i++) {
                if (out_id.equals(variables[i].getNode_id())) return i;
            }
        }
        return -1;
    }

    private int getNextNeighbor(int i, int w){
        Variable variable=variables[i];
        List<String> outList=variable.getOut_node_id_list();
        String out_cur_id=variables[w].getNode_id();
        int index=outList.indexOf(out_cur_id);
        if (index+1<outList.size()) {
            String out_id=outList.get(index+1);
            for (int j=0;j<variables.length;j++) {
                if (out_id.equals(variables[j].getNode_id())) return j;
            }
        }
        return -1;
    }

    private int getLastNeighbor(int i, int w){
        Variable variable=variables[i];
        List<String> outList=variable.getOut_node_id_list();
        String out_cur_id=variables[w].getNode_id();
        int index=outList.indexOf(out_cur_id);
        if (index-1>-1) {
            String out_id=outList.get(index-1);
            for (int j=0;j<variables.length;j++) {
                if (out_id.equals(variables[j].getNode_id())) return j;
            }
        }
        return -1;
    }

    private boolean check(Variable parent, Variable child) {
        myNode node_p=parent.getCandidateNode();
        myNode node_c=child.getCandidateNode();
        return singleGraph.containsEdge(node_p, node_c);
    }


    private void depthFirstSearch(int i) {
        isVisited[i]=true;
        int w=getFirstNeighbor(i);
        Variable variable_parent=variables[i];
        Variable variable_child=variables[w];
        while (w!=-1) {
            if (isVisited[w]) {
                if (!check(variable_parent, variable_child)) {
                    isVisited[w]=false;
                    isVisited[i]=false;
                    w=getLastNeighbor(i,w);
                    isVisited[w]=false;
                    //variable_parent.setNextCandidateNode();
                }
            }
            while (!check(variable_parent,variable_child)) {
                variable_child.setNextCandidateNode();
                myNode tem=variable_child.getCandidateNode();
            }
            if (!isVisited[w]){
                depthFirstSearch(w);
            }
            w=getNextNeighbor(i,w);
        }
    }








}
