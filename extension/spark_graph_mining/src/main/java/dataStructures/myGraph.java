package dataStructures;

import edu.isi.karma.rep.alignment.Label;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.*;

public class myGraph extends DirectedWeightedMultigraph{
    private HashMap<Label, HashMap<String, myNode>> nodesByLabel=new HashMap<>();

    public myGraph(Class aClass) {
        super(aClass);
    }

    public void setNodesByLabel() {
        for (Iterator<myNode> iterator=this.vertexSet().iterator();iterator.hasNext();){
            HashMap<String, myNode> map=new HashMap<>();
            myNode outNode=iterator.next();
            for (Iterator<myNode> it=this.vertexSet().iterator();it.hasNext();){
                myNode inNode=it.next();
                if (outNode.getLabel().equals(inNode.getLabel())){
                    map.put(inNode.getId(),inNode);
                }
            }
            nodesByLabel.put(outNode.getLabel(),map);
        }
    }

    public HashMap<Label, HashMap<String, myNode>> getNodesByLabel() {
        return nodesByLabel;
    }

    /**
     *@discription: edge_label和node_label处理
     *@param edge_labelMap
	 *@param edge_label
	 *@param node_label
     *@date: 2020/3/12 18:44
     *@return: void
     *@author: Han
     */
    private void checkNull(HashMap<Label, List<Label>> edge_labelMap, Label edge_label, Label node_label){
        if (edge_labelMap.get(edge_label)==null) {
            List<Label> node_label_list=new ArrayList<>();
            node_label_list.add(node_label);
            edge_labelMap.put(edge_label,node_label_list);
        }else {
            List<Label> node_label_list=edge_labelMap.get(edge_label);
            node_label_list.add(node_label);
            edge_labelMap.put(edge_label,node_label_list);
        }
    }

    /**
     *@discription: 将edge_label,<node_label>转化为node_label,(edge_label,count)
     *@param edge_label_node_count
	 *@param edge_labelMap
     *@date: 2020/3/12 18:48
     *@return: void
     *@author: Han
     */
    private void mapTransfer(HashMap<Label, HashMap<Label,Integer>> edge_label_node_count, HashMap<Label, List<Label>> edge_labelMap) {
        for (Map.Entry<Label, List<Label>> entry : edge_labelMap.entrySet()) {
            Label edge_label=entry.getKey();
            List<Label> node_label_list=entry.getValue();

            HashMap<Label, Integer> temp=new HashMap<>();
            for (Label node_label:node_label_list){
                if (temp.containsKey(node_label)) {
                    temp.put(node_label, temp.get(node_label)+1);
                }else {
                    temp.put(node_label, 1);
                }
            }
            for (Map.Entry<Label, Integer> e : temp.entrySet()) {
                Label node_label=e.getKey();
                int count=e.getValue();
                if (edge_label_node_count.get(node_label)==null) {
                    HashMap<Label, Integer> edge_label_count=new HashMap<>();
                    edge_label_count.put(edge_label, count);
                    edge_label_node_count.put(node_label,edge_label_count);
                }else {
                    HashMap<Label, Integer> edge_label_count=edge_label_node_count.get(node_label);
                    edge_label_count.put(edge_label, count);
                    edge_label_node_count.put(node_label, edge_label_count);
                }
            }
        }
    }

    /**
     *@discription: 得出图中所有out的map
     * 储存形式 node_id, (node_label,(edge_label,degree))
     * node_id->节点id
     * node_label->此节点out出去的节点的label
     * edge_label->out出去的这条边的label
     * degree->满足边label的次数
     * 主旨为图中所有节点的集合，节点id与对应out出去根据边label分类的degree集合
     *@param
     *@date: 2020/3/12 18:50
     *@return: java.util.HashMap<java.lang.String,java.util.HashMap<edu.isi.karma.rep.alignment.Label,java.util.HashMap<edu.isi.karma.rep.alignment.Label,java.lang.Integer>>>
     *@author: Han
     */
    public HashMap<String,HashMap<Label,HashMap<Label,Integer>>> OutMap() {
        HashMap<String, HashMap<Label, HashMap<Label, Integer>>> outMap=new HashMap<>();
        for (Iterator<myNode> iterator=this.vertexSet().iterator();iterator.hasNext();){
            myNode node=iterator.next();
            HashMap<Label,List<Label>> edge_labelMap=new HashMap<>();
            for (Iterator<myEdge> edgeIterator=this.outgoingEdgesOf(node).iterator();edgeIterator.hasNext();){
                myEdge link=edgeIterator.next();
                Label edge_label=link.getLabel();
                myNode targetNode= (myNode) this.getEdgeTarget(link);
                Label node_label=targetNode.getLabel();
                checkNull(edge_labelMap,edge_label,node_label);
            }
            HashMap<Label, HashMap<Label,Integer>> edge_label_node_count=new HashMap<>();
            mapTransfer(edge_label_node_count,edge_labelMap);
            outMap.put(node.getId(),edge_label_node_count);
        }
        return outMap;
    }

    /**
     *@discription: 理解同out
     * 此处对应in
     *@param
     *@date: 2020/3/12 18:56
     *@return: java.util.HashMap<java.lang.String,java.util.HashMap<edu.isi.karma.rep.alignment.Label,java.util.HashMap<edu.isi.karma.rep.alignment.Label,java.lang.Integer>>>
     *@author: Han
     */
    public HashMap<String,HashMap<Label,HashMap<Label,Integer>>> InMap() {
        HashMap<String, HashMap<Label, HashMap<Label, Integer>>> inMap=new HashMap<>();
        for (Iterator<myNode> iterator=this.vertexSet().iterator();iterator.hasNext();){
            myNode node=iterator.next();
            HashMap<Label,List<Label>> edge_labelMap=new HashMap<>();
            for (Iterator<myEdge> edgeIterator=this.incomingEdgesOf(node).iterator();edgeIterator.hasNext();){
                myEdge link=edgeIterator.next();
                Label edge_label=link.getLabel();
                myNode sourceNode = (myNode) this.getEdgeSource(link);
                Label node_label= sourceNode.getLabel();
                checkNull(edge_labelMap,edge_label,node_label);
            }
            HashMap<Label, HashMap<Label,Integer>> edge_label_node_count=new HashMap<>();
            mapTransfer(edge_label_node_count,edge_labelMap);
            inMap.put(node.getId(),edge_label_node_count);
        }
        return inMap;
    }

    public HashMap<String, HashMap<Label, Integer>> getOutMap(){
        HashMap<String, HashMap<Label, Integer>> outMap=new HashMap<>();
        for (Iterator<myNode> iterator=this.vertexSet().iterator();iterator.hasNext();){
            myNode node=iterator.next();
            HashMap<Label, Integer> labelMap=new HashMap<>();
            for (Object link : this.outgoingEdgesOf(node)) {
                myNode targetNode = (myNode) this.getEdgeTarget(link);
                int count = labelMap.containsKey(targetNode.getLabel()) ? labelMap.get(targetNode.getLabel()) + 1 : 1;
                labelMap.put(targetNode.getLabel(), count);
            }
            outMap.put(node.getId(),labelMap);
        }
        return outMap;
    }

    public HashMap<String, HashMap<Label, Integer>> getInMap(){
        HashMap<String, HashMap<Label, Integer>> inMap=new HashMap<>();
        for (Iterator<myNode> iterator=this.vertexSet().iterator();iterator.hasNext();){
            myNode node=iterator.next();
            HashMap<Label, Integer> labelMap=new HashMap<>();
            for (Iterator<myEdge> edge=this.incomingEdgesOf(node).iterator();edge.hasNext();){
                myEdge link=edge.next();
                myNode sourceNode = (myNode) this.getEdgeSource(link);
                int count=labelMap.containsKey(sourceNode.getLabel())?labelMap.get(sourceNode.getLabel())+1:1;
                labelMap.put(sourceNode.getLabel(), count);
            }
            inMap.put(node.getId(),labelMap);
        }
        return inMap;
    }



    /**
     *@discription: 根据node_label和edge_label判断outDegree
     *@param node
	 *@param node_label
	 *@param edge_label
     *@date: 2020/3/12 20:34
     *@return: int
     *@author: Han
     */
    public int outDegreeOfByNodeLabelAndEdgeLabel(myNode node, Label node_label, Label edge_label) {
        int degree=0;
        for (Iterator<myEdge> iterator=this.outgoingEdgesOf(node).iterator();iterator.hasNext();) {
            myEdge edge=iterator.next();
            myNode targetNode= (myNode) this.getEdgeTarget(edge);
            if (edge.getLabel().equals(edge_label)&&targetNode.getLabel().equals(node_label)) degree++;
        }
        return degree;
    }

    /**
     *@discription: 根据node_label和edge_label来判断inDegree
     *@param node
	 *@param node_label
	 *@param edge_label
     *@date: 2020/3/12 20:35
     *@return: int
     *@author: Han
     */
    public int inDegreeOfByNodeLabelAndEdgeLabel(myNode node, Label node_label, Label edge_label) {
        int degree=0;
        for (Iterator<myEdge> iterator=this.incomingEdgesOf(node).iterator();iterator.hasNext();) {
            myEdge edge=iterator.next();
            myNode targetNode= (myNode) this.getEdgeSource(edge);
            if (edge.getLabel().equals(edge_label)&&targetNode.getLabel().equals(node_label)) degree++;
        }
        return degree;
    }

    public int outDegreeOfByLabel(myNode node, Label label){
        int degree=0;
        for (Iterator<myEdge> iterator=this.outgoingEdgesOf(node).iterator();iterator.hasNext();){
            myEdge edge=iterator.next();
            myNode targetNode= (myNode) this.getEdgeTarget(edge);
            if (targetNode.getLabel().equals(label)) degree++;
        }
        return degree;
    }

    public int inDegreeOfByLabel(myNode node, Label label){
        int degree=0;
        for (Iterator<myEdge> iterator=this.incomingEdgesOf(node).iterator();iterator.hasNext();){
            myEdge edge=iterator.next();
            myNode sourceNode= (myNode) this.getEdgeSource(edge);
            if (sourceNode.getLabel().equals(label)) degree++;
        }
        return degree;
    }

    public List<String> getOutNodeIds(String id){
        myNode node=null;
        for (Iterator<myNode> it=this.vertexSet().iterator();it.hasNext();) {
            myNode temp=it.next();
            if (temp.getId().equals(id)) node=temp;
        }
        List<String> outIds=new ArrayList<>();
        for (Object edge : this.outgoingEdgesOf(node)) {
            myNode targetNode = (myNode) this.getEdgeTarget(edge);
            outIds.add(targetNode.getId());
        }
        return outIds;
    }


}
