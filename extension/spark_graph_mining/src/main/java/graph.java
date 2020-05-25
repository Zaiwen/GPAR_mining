import CSP.DFSSearch;
import CSP.Variable;
import dataStructures.myEdge;
import dataStructures.myNode;
import dataStructures.myGraph;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.NodeType;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;
import pruning.Pruner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class graph {
    public static void main(String[] args){
        
        // x->customer y->French_Rest z->Asian_Rest a->city
        myGraph graph=new myGraph(myEdge.class);
        myNode node_cust1=new myNode("cust1",new Label("x"), NodeType.None);
        myNode node_cust2=new myNode("cust2",new Label("x"), NodeType.None);
        myNode node_cust3=new myNode("cust3",new Label("x"), NodeType.None);
        myNode node_cust4=new myNode("cust4",new Label("x"), NodeType.None);
        myNode node_cust5=new myNode("cust5",new Label("x"), NodeType.None);
        myNode node_cust6=new myNode("cust6",new Label("x"), NodeType.None);
        
        myNode node_f_r_leb=new myNode("LeBernardin",new Label("y"), NodeType.None);
        myNode node_f_r_per=new myNode("Perse",new Label("y"), NodeType.None);
        myNode node_f_r_pat=new myNode("Patina",new Label("y"),NodeType.None);
        myNode node_f_r_no_name=new myNode("F_R_6",new Label("y"),NodeType.None);
        myNode node_a_r_no_name=new myNode("A_R_1",new Label("z"), NodeType.None);
        
        myNode node_f_r_liked_1=new myNode("f_r_1",new Label("y"), NodeType.None);
        myNode node_f_r_liked_2=new myNode("f_r_2",new Label("y"),NodeType.None);
        myNode node_f_r_liked_3=new myNode("f_r_3",new Label("y"),NodeType.None);
        myNode node_a_r_liked_1=new myNode("a_r_1",new Label("z"),NodeType.None);
        
        myNode node_city_newyork=new myNode("NewYork",new Label("a"),NodeType.None);
        myNode node_city_la=new myNode("LA",new Label("a"),NodeType.None);
        
        
        graph.addVertex(node_cust1);
        graph.addVertex(node_cust2);
        graph.addVertex(node_cust3);
        graph.addVertex(node_cust4);
        graph.addVertex(node_cust5);
        graph.addVertex(node_cust6);
        graph.addVertex(node_f_r_leb);
        graph.addVertex(node_f_r_per);
        graph.addVertex(node_f_r_pat);
        graph.addVertex(node_f_r_no_name);
        graph.addVertex(node_a_r_no_name);
        graph.addVertex(node_f_r_liked_1);
        graph.addVertex(node_f_r_liked_2);
        graph.addVertex(node_f_r_liked_3);
        graph.addVertex(node_a_r_liked_1);
        graph.addVertex(node_city_newyork);
        graph.addVertex(node_city_la);
        

        graph.addEdge(node_cust1,node_cust2,new myEdge(new Label("friend")));
        graph.addEdge(node_cust1,node_f_r_leb,new myEdge(new Label("visit")));
        graph.addEdge(node_cust1,node_f_r_liked_1,new myEdge(new Label("like")));
        graph.addEdge(node_cust1,node_city_newyork,new myEdge(new Label("live_in")));
        graph.addEdge(node_cust2,node_cust1,new myEdge(new Label("friend")));
        graph.addEdge(node_cust2,node_cust3,new myEdge(new Label("friend")));
        graph.addEdge(node_cust2,node_f_r_leb,new myEdge(new Label("visit")));
        graph.addEdge(node_cust2,node_f_r_per,new myEdge(new Label("visit")));
        graph.addEdge(node_cust2,node_city_newyork,new myEdge(new Label("live_in")));
        graph.addEdge(node_cust2,node_f_r_liked_1,new myEdge(new Label("like")));
        graph.addEdge(node_cust2,node_f_r_liked_2,new myEdge(new Label("like")));
        graph.addEdge(node_cust3,node_cust2,new myEdge(new Label("friend")));
        graph.addEdge(node_cust3,node_cust4,new myEdge(new Label("friend")));
        graph.addEdge(node_cust3,node_f_r_per,new myEdge(new Label("visit")));
        graph.addEdge(node_cust3,node_f_r_liked_2,new myEdge(new Label("like")));
        graph.addEdge(node_cust3,node_city_newyork,new myEdge(new Label("live_in")));
        graph.addEdge(node_cust4,node_cust3,new myEdge(new Label("friend")));
        graph.addEdge(node_cust4,node_cust5,new myEdge(new Label("friend")));
        graph.addEdge(node_cust4,node_f_r_pat,new myEdge(new Label("visit")));
        graph.addEdge(node_cust4,node_a_r_no_name,new myEdge(new Label("like")));
        graph.addEdge(node_cust4,node_f_r_liked_3,new myEdge(new Label("like")));
        graph.addEdge(node_cust4,node_city_la,new myEdge(new Label("live_in")));
        graph.addEdge(node_cust5,node_cust4,new myEdge(new Label("friend")));
        graph.addEdge(node_cust5,node_cust6,new myEdge(new Label("friend")));
        graph.addEdge(node_cust5,node_a_r_no_name,new myEdge(new Label("visit")));
        graph.addEdge(node_cust5,node_f_r_liked_3,new myEdge(new Label("like")));
        graph.addEdge(node_cust5,node_a_r_liked_1,new myEdge(new Label("like")));
        graph.addEdge(node_cust5,node_city_la,new myEdge(new Label("live_in")));
        graph.addEdge(node_cust6,node_cust5,new myEdge(new Label("friend")));
        graph.addEdge(node_cust6,node_f_r_no_name,new myEdge(new Label("visit")));
        graph.addEdge(node_cust6,node_a_r_liked_1,new myEdge(new Label("like")));
        graph.addEdge(node_cust6,node_city_la,new myEdge(new Label("live_in")));
        graph.addEdge(node_f_r_leb,node_city_newyork,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_per,node_city_newyork,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_pat,node_city_la,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_no_name,node_city_la,new myEdge(new Label("in")));
        graph.addEdge(node_a_r_no_name,node_city_la,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_liked_1,node_city_newyork,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_liked_2,node_city_newyork,new myEdge(new Label("in")));
        graph.addEdge(node_f_r_liked_3,node_city_la,new myEdge(new Label("in")));
        graph.addEdge(node_a_r_liked_1,node_city_la,new myEdge(new Label("in")));
        graph.setNodesByLabel();


        myGraph sub=new myGraph(myEdge.class);
//        myNode node_temp_1=new myNode("f_r_temp1",new Label("y"), NodeType.None);
//        myNode node_temp_2=new myNode("f_r_temp2",new Label("y"), NodeType.None);
        sub.addVertex(node_cust1);
        sub.addVertex(node_cust2);
        sub.addVertex(node_city_newyork);
        sub.addVertex(node_f_r_liked_1);
        sub.addVertex(node_f_r_leb);

//        sub.addVertex(node_temp_1);
//        sub.addVertex(node_temp_2);


        sub.addEdge(node_cust1,node_cust2,new myEdge(new Label("friend")));
        //sub.addEdge(node_cust1,node_f_r_leb,new myEdge(new Label("visit")));
        sub.addEdge(node_cust1,node_f_r_liked_1,new myEdge(new Label("like")));
        sub.addEdge(node_cust1,node_city_newyork,new myEdge(new Label("live_in")));
        sub.addEdge(node_cust2,node_cust1,new myEdge(new Label("friend")));
        sub.addEdge(node_cust2,node_f_r_leb,new myEdge(new Label("visit")));
        sub.addEdge(node_cust2,node_f_r_liked_1,new myEdge(new Label("like")));
        sub.addEdge(node_cust2,node_city_newyork,new myEdge(new Label("live_in")));
        sub.addEdge(node_f_r_leb,node_city_newyork,new myEdge(new Label("in")));
        sub.addEdge(node_f_r_liked_1,node_city_newyork,new myEdge(new Label("in")));

//        sub.addEdge(node_temp_1,node_city_newyork,new myEdge(new Label("in")));
//        sub.addEdge(node_temp_2,node_city_newyork,new myEdge(new Label("in")));
//        sub.addEdge(node_cust1,node_temp_1,new myEdge(new Label("like")));
//        sub.addEdge(node_cust1,node_temp_2,new myEdge(new Label("like")));
//        sub.addEdge(node_cust2,node_temp_1,new myEdge(new Label("like")));
//        sub.addEdge(node_cust2,node_temp_2,new myEdge(new Label("like")));



        sub.setNodesByLabel();

        Pruner pruner =new Pruner();
        pruner.prunProcess(graph,sub);

        DFSSearch search=new DFSSearch(graph,pruner.getVariables());

        Variable[] variables=search.getVariables();
//        for (Variable variable:variables) {
//            System.out.println(variable.getNode_id());
//            System.out.println(variable.getOut_node_id_list());
//        }
//        System.out.println(search.getLastNeighbor(2, 0));


//        DepthFirstIterator dfsIterator=new DepthFirstIterator(sub,node_cust1);
//        while (dfsIterator.hasNext()) {
//            myNode node= (myNode) dfsIterator.next();
//            System.out.println(node.getId()+""+node);
//        }


    }
}
