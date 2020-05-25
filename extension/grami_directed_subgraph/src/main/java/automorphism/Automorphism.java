/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package automorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import pruning.SPpruner;

import CSP.DFSSearch;
import CSP.Variable;
import dataStructures.HPListGraph;
import dataStructures.IntIterator;
import dataStructures.Query;
import dataStructures.myNode;
import utilities.MyPair;

public class Automorphism <NodeType, EdgeType>
{
	
	private HPListGraph<NodeType, EdgeType> patternGraph;
	private Variable[] result;
	public Variable[] getResult() {
		return result;
	}

	private HashMap<Integer,myNode> nodes;
	private HashMap<Integer,HashMap<Integer,myNode>> nodesByLabel;
	private int resultCounter;
	
	public Automorphism(HPListGraph<NodeType, EdgeType> graph) 
	{
		patternGraph=graph;
		result= new Variable[graph.getNodeCount()];
		nodes= new HashMap<Integer, myNode>();
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		
		Query qry = new Query((HPListGraph<Integer, Double>)graph);
		
		//create my nodes first !!
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode newNode= new myNode(i,(Integer)graph.getNodeLabel(i));
			nodes.put(newNode.getID(), newNode);
		}
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode currentNode= nodes.get(i);
			for (IntIterator currentEdges = graph.getEdgeIndices(i); currentEdges.hasNext();) 
			{
				int edge = currentEdges.next();
				int direction=graph.getDirection(edge, i);
				int otherNodeIndex= graph.getOtherNode(edge, i);
				myNode otherNode=nodes.get(otherNodeIndex);
				if(direction==1)
					currentNode.addreachableNode(otherNode, Double.parseDouble(graph.getEdgeLabel(edge)+""));
				/**
                 * @param graphthing goes 2020/4/1h 23:34omputes of automorphism
                 * from my mind, it's repeatedly added reachable nodes
                 * cuz method addreachableNode has already contained addreachedBy
                 **/
//				else if(direction ==-1)
//					otherNode.addreachableNode(currentNode, Double.parseDouble(graph.getEdgeLabel(edge)+""));
			}
		}
		//now fill by label
		for (int i = 0; i < graph.getNodeCount(); i++) 
		{
			myNode currentNode = nodes.get(i);
			
			HashMap<Integer,myNode> currentLabelNodes= nodesByLabel.get(currentNode.getLabel());
			if(currentLabelNodes==null)
			{
				currentLabelNodes= new HashMap<Integer, myNode>();
				nodesByLabel.put(currentNode.getLabel(), currentLabelNodes);
			}
			currentLabelNodes.put(currentNode.getID(), currentNode);
		}
		
		SPpruner sp = new SPpruner();
		sp.getPrunedLists(nodesByLabel, qry);
		DFSSearch df = new DFSSearch(sp,qry,-1);
		df.searchAll();
		resultCounter=df.getResultCounter();
		result=df.getResultVariables();
	}

	public boolean hasAutomorphisms()
	{
		if(resultCounter==1)
			return false;
		return true;
	}


    /**
     *@discription: 判断v1和v2是否是相同的
     *@param v1
	 *@param v2
     *@date: 2020/3/26 3:54
     *@return: boolean
     *@author: Han
     */
    private boolean isSame(Variable v1, Variable v2) {
	    boolean flag=false;
        ArrayList<MyPair<Integer, Double>> v1DistanceConstrainedWith = v1.getDistanceConstrainedWith();
        ArrayList<MyPair<Integer, Double>> v2DistanceConstrainedWith = v2.getDistanceConstrainedWith();
        if (v1DistanceConstrainedWith.size()!=v2DistanceConstrainedWith.size()) return false;
        else {
            if (v1DistanceConstrainedWith.size()==0) {
                ArrayList<MyPair<Integer, Double>> v1DistanceConstrainedBy = v1.getDistanceConstrainedBy();
                ArrayList<MyPair<Integer, Double>> v2DistanceConstrainedBy = v2.getDistanceConstrainedBy();

                if (v1DistanceConstrainedBy.size()!=v2DistanceConstrainedBy.size()) return false;
                else {
                    for (MyPair v1pair:v1DistanceConstrainedBy) {
                        for (MyPair v2pair:v2DistanceConstrainedBy) {
                            if ((double)v1pair.getB()==(double)v2pair.getB()) {
                                flag=true;
                                break;
                            }
                        }
                    }
                }
            }else {
                for (MyPair v1pair:v1DistanceConstrainedWith) {
                    for (MyPair v2pair:v2DistanceConstrainedWith) {
                        if ((double) v1pair.getB()==(double)v2pair.getB()) {
                            flag=true;
                            break;
                        }
                    }
                }
            }
        }

        return flag&&v1.getListSize()==v2.getListSize()&&v1.getLabel()==v2.getLabel();
    }


    /**
     *@discription: 得到automorphic graph
     * if the graph turns out to be automorphism, return the unique sub graph
     *@param
     *@date: 2020/3/26 3:55
     *@return: dataStructures.HPListGraph<NodeType,EdgeType>
     *@author: Han
     */
    public HPListGraph<NodeType, EdgeType> getAutomorphicGraph() {
        HPListGraph autoGraph=new HPListGraph();
        List<List<Integer>> symmetries=new ArrayList<>(); // 分类,储存对称点的id
        List<Variable> random=new ArrayList<>(); // 随机取得存在对称的点的集合
        HashMap<Integer,Integer> tempMap=new HashMap<>();
        HashMap<Integer,Integer> autoMap=new HashMap<>();

        for (int i=0;i<result.length;i++) {
            if (result[i].getListSize()==1) {
                int out=-1;
                if (!tempMap.containsKey(result[i].getID())) {
                    out=autoGraph.addNodeIndex(result[i].getLabel());
                    tempMap.put(result[i].getID(),out);
                }
                for (int j=i+1;j<result.length;j++) {
                    if (result[j].getListSize()==1) {
                        int in=autoGraph.addNodeIndex(result[j].getLabel());
                        tempMap.put(result[j].getID(),in);
                        for (MyPair pair:result[i].getDistanceConstrainedWith()) {
                            if ((Integer) pair.getA() == result[j].getID()) {
                                double label = (double) pair.getB();
                                if (out!=-1) {
                                    autoGraph.addEdgeIndex(out,in,label,1);
                                }
                            }
                        }
                    }
                }
            }else {
                boolean flag=true;
                for (List<Integer> list:symmetries) {
                    if (list.contains(result[i].getID())) {
                        flag=false;
                    }
                }
                if (flag) {
                    List<Integer> temp=new ArrayList<>();
                    temp.add(result[i].getID());
                    random.add(result[i]);
                    for (int j=i+1;j<result.length;j++) {
                        if (result[j].getListSize()!=1&&isSame(result[i],result[j])) {
                            temp.add(result[j].getID());
                        }
                    }
                    symmetries.add(temp);
                }
            }
        }

        // 将存在对称的点与一般点进行连接
        for (Variable v:random) {
            int out=autoGraph.addNodeIndex(v.getLabel());
            autoMap.put(v.getID(),out);
            ArrayList<MyPair<Integer, Double>> with = v.getDistanceConstrainedWith();
            ArrayList<MyPair<Integer, Double>> by = v.getDistanceConstrainedBy();
            for (MyPair pair:with) {
                int id=(Integer)pair.getA();
                if (tempMap.containsKey(id)) {
                    int in=tempMap.get(id);
                    double label= (double) pair.getB();
                    autoGraph.addEdgeIndex(out,in,label,1);
                }
            }
            for (MyPair pair:by) {
                int id=(Integer)pair.getA();
                if (tempMap.containsKey(id)) {
                    int in=tempMap.get(id);
                    double label= (double) pair.getB();
                    autoGraph.addEdgeIndex(out,in,label,-1);
                }
            }
        }

        // 对称点间的连接
        for (Variable v:random) {
            ArrayList<MyPair<Integer, Double>> with = v.getDistanceConstrainedWith();
            for (MyPair pair:with) {
                int id=(Integer)pair.getA();
                for (List<Integer> list:symmetries) {
                    if (list.contains(id)) {
                        for (Integer candidate:list) {
                            if (autoMap.containsKey(candidate)) {
                                int out=autoMap.get(v.getID());
                                int in=autoMap.get(candidate);
                                double label=(double) pair.getB();
                                autoGraph.addEdgeIndex(out,in,label,1);
                                list.clear();
                                break;
                            }
                        }
                    }
                }
            }
        }


        return autoGraph;
    }
}
