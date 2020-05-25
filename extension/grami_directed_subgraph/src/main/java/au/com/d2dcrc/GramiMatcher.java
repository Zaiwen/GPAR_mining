package au.com.d2dcrc;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import CSP.Variable;
import com.hp.hpl.jena.ontology.QualifiedRestriction;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Node;

import java.util.*;

public class GramiMatcher {

    private List<Map<Node,Node>> isomorphismList = null;
    private Set<Integer> anchorSet = null;//designated by User
    private Graph graph = null;//data graph
    private Query qry = null;//pattern graph
    private int frequency = 0;
    private Set<Integer> limitNodeSet = null;//designated by User

    /**Constructor*
     * @created 22 Feb 2019
     * */
    public GramiMatcher () {
        isomorphismList = new ArrayList<>();
        limitNodeSet = new HashSet<>();//added on 22 Feb 2019
        anchorSet = new HashSet<>();//added on 26 Feb 2019
        frequency = 0;
    }

    /**Add anchor label to the GraMi Matcher*
     * @param anchorLabel label of anchor in pattern graph
     * @created 22 Feb 2019
     * */
    public void addAnchorSet (Integer anchorLabel) {
        anchorSet.add(anchorLabel);
    }

    /**Add label of limit node to the GraMi Matcher*
     * @created 26 Feb 2019
     * @param limitNodeLabel label of limit node
     * */
    public void addLimitNode (Integer limitNodeLabel) {
        limitNodeSet.add(limitNodeLabel);
    }

    /**Set limit node label set to GraMi Matcher*
     * @created 26 Feb 2019
     * @param limitNodeSet limit node label set of pattern graph
     * */
    public void setLimitNodeSet (Set<Integer> limitNodeSet) {
        this.limitNodeSet = limitNodeSet;
    }

    /**Set Anchor label set to GraMi Matcher*
     * @created 26 Feb 2019
     *@param anchorSet anchor label set of pattern graph
     * */
    public void setAnchorSet (Set<Integer> anchorSet) {
        this.anchorSet = anchorSet;
    }

    /**Set Query graph**/
    public void setQry (Query qry) {
        this.qry = qry;
    }

    /**Set Data Graph**/
    public void setGraph (Graph graph) {
        this.graph = graph;
    }


    /**this function is used to get the frequency that a pattern appears in a big graph*
     * @param nonCandidates so far, not very sure about the meaning, it can be null
     * @param matchingOption different scenarios for sub-graph matching
     *                       0------pattern graph: tree with unique labels
     *                       1------pattern graph: a graph
     * @return the frequency that this pattern appears in the big graph
     * @reivsed 8 Jan 2019, 22 Jan 2019, 26 Jan 2019, 22 Feb 2019
     * */
    public int getFrequency(HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption){
        /**create a constraint graph to solve CSP**/
        int freq = 0 ;
        ConstraintGraph cg = new ConstraintGraph(graph, qry, nonCandidates);
        DFSSearch df = new DFSSearch(cg,graph.getFreqThreshold(),nonCandidates);
        try{
            if (matchingOption == 0) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern graph is a tree while the labels are unique...");
                df.searchExistances2();
            } else if (matchingOption == 1) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern is a graph rather than a tree...");
                if (!anchorSet.isEmpty()) {
                    df.setAnchorLabel(anchorSet);// added on 22 Feb 2019
                }
                if (!limitNodeSet.isEmpty()) {
                    df.setLimitNodeLabel(limitNodeSet);// added on 26 Feb 2019
                }
                df.searchExistances();
            }
            freq=df.getFrequencyOfPattern();
            System.out.println("the frequency of this pattern is: " + freq);
            this.isomorphismList = df.getIsomorphismList();
            System.out.println("the are totally " + isomorphismList.size() + " subgraph isomorphisms detected by GraMi!");

        }catch (Exception e) {
            e.printStackTrace();
        }

        return freq;
    }


    /**
     *@discription: 获取target_node_id的frequency
     *@param nonCandidates
	 *@param matchingOption
	 *@param target_node_id
     *@date: 2020/3/15 12:35
     *@return: int
     *@author: Han
     */
    public int getFrequency(HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption, int target_node_id){
        /**create a constraint graph to solve CSP**/
        int freq = 0 ;
        ConstraintGraph cg = new ConstraintGraph(graph, qry, nonCandidates);
        DFSSearch df = new DFSSearch(cg,graph.getFreqThreshold(),nonCandidates);
        try{
            if (matchingOption == 0) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern graph is a tree while the labels are unique...");
                df.searchExistances2();
            } else if (matchingOption == 1) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern is a graph rather than a tree...");
                df.searchExistances(target_node_id);
            }
            freq=df.getFrequencyOfPattern(target_node_id);
            System.out.println("the frequency of this pattern is: " + freq);
            this.isomorphismList = df.getIsomorphismList();
            System.out.println("the are totally " + isomorphismList.size() + " subgraph isomorphisms detected by GraMi!");

        }catch (Exception e) {
            e.printStackTrace();
        }

        return freq;
    }


    /**Get all the sub-graph isomorphisms*
     * @from 26 Jan 2019
     * */
    public List<Map<Node,Node>> getIsomorphismList () {return isomorphismList;}

    public static void myExample() {
        Graph singleGraph = new Graph(100, Integer.MAX_VALUE);


        /**representation
         * 100->customer
         * 200->French restaurant
         * 300->Asian restaurant
         * 400->city
         */
        int cust1_id = singleGraph.addNode(100);
        int cust2_id = singleGraph.addNode(100);
        int cust3_id = singleGraph.addNode(100);
        int cust4_id = singleGraph.addNode(100);
        int cust5_id = singleGraph.addNode(100);
        int cust6_id = singleGraph.addNode(100);
        int f_r_visited_1_id = singleGraph.addNode(200);
        int f_r_visited_2_id = singleGraph.addNode(200);
        int f_r_visited_3_id = singleGraph.addNode(200);
        int f_r_visited_4_id = singleGraph.addNode(200);
        int f_r_1_id = singleGraph.addNode(200);
        int f_r_2_id = singleGraph.addNode(200);
        int f_r_3_id = singleGraph.addNode(200);
        int a_r_1_id = singleGraph.addNode(300);
        int a_r_2_id = singleGraph.addNode(300);
        int newYork_id = singleGraph.addNode(400);
        int lA_id = singleGraph.addNode(400);

        myNode cust1 = new myNode(cust1_id,100);
        myNode cust2 = new myNode(cust2_id,100);
        myNode cust3 = new myNode(cust3_id,100);
        myNode cust4 = new myNode(cust4_id,100);
        myNode cust5 = new myNode(cust5_id,100);
        myNode cust6 = new myNode(cust6_id,100);
        myNode f_r_visited_1 = new myNode(f_r_visited_1_id,200);
        myNode f_r_visited_2 = new myNode(f_r_visited_2_id,200);
        myNode f_r_visited_3 = new myNode(f_r_visited_3_id,200);
        myNode f_r_visited_4 = new myNode(f_r_visited_4_id,200);
        myNode f_r_1 = new myNode(f_r_1_id,200);
        myNode f_r_2 = new myNode(f_r_2_id,200);
        myNode f_r_3 = new myNode(f_r_3_id,200);
        myNode a_r_1 = new myNode(a_r_1_id,300);
        myNode a_r_2 = new myNode(a_r_2_id,300);
        myNode newYork = new myNode(newYork_id,400);
        myNode lA = new myNode(lA_id,400);
        /**representation
         * friend->10
         * visit->20
         * like->30
         * live_in->40
         * in->50
         */
        cust1.addreachableNode(cust2,10);
        cust1.addreachableNode(f_r_visited_1,20);
        cust1.addreachableNode(f_r_1,30);
        cust1.addreachableNode(newYork,40);
        cust2.addreachableNode(cust1,10);
        cust2.addreachableNode(cust3,10);
        cust2.addreachableNode(f_r_visited_1,20);
        cust2.addreachableNode(f_r_visited_2,20);
        cust2.addreachableNode(f_r_1,30);
        cust2.addreachableNode(f_r_2,30);
        cust2.addreachableNode(newYork,40);
        cust3.addreachableNode(cust2,10);
        cust3.addreachableNode(cust4,10);
        cust3.addreachableNode(f_r_visited_2,20);
        cust3.addreachableNode(f_r_2,30);
        cust3.addreachableNode(newYork,40);
        cust4.addreachableNode(cust3,10);
        cust4.addreachableNode(cust5,10);
        cust4.addreachableNode(f_r_visited_3,20);
        cust4.addreachableNode(f_r_3,30);
        cust4.addreachableNode(a_r_1,30);
        cust4.addreachableNode(lA,40);
        cust5.addreachableNode(cust4,10);
        cust5.addreachableNode(cust6,10);
        cust5.addreachableNode(a_r_1,20);
        cust5.addreachableNode(f_r_3,30);
        cust5.addreachableNode(a_r_2,30);
        cust5.addreachableNode(lA,40);
        cust6.addreachableNode(cust5,10);
        cust6.addreachableNode(f_r_visited_4,20);
        cust6.addreachableNode(a_r_2,30);
        cust6.addreachableNode(lA,40);
        f_r_visited_1.addreachableNode(newYork,50);
        f_r_visited_2.addreachableNode(newYork,50);
        f_r_visited_3.addreachableNode(lA,50);
        f_r_visited_4.addreachableNode(lA,50);
        a_r_1.addreachableNode(lA,50);
        f_r_1.addreachableNode(newYork,50);
        f_r_2.addreachableNode(newYork,50);
        f_r_3.addreachableNode(lA,50);
        a_r_2.addreachableNode(lA,50);

        singleGraph.addNode(cust1);
        singleGraph.addNode(cust2);
        singleGraph.addNode(cust3);
        singleGraph.addNode(cust4);
        singleGraph.addNode(cust5);
        singleGraph.addNode(cust6);
        singleGraph.addNode(f_r_1);
        singleGraph.addNode(f_r_2);
        singleGraph.addNode(f_r_3);
        singleGraph.addNode(f_r_visited_1);
        singleGraph.addNode(f_r_visited_2);
        singleGraph.addNode(f_r_visited_3);
        singleGraph.addNode(f_r_visited_4);
        singleGraph.addNode(a_r_1);
        singleGraph.addNode(a_r_2);
        singleGraph.addNode(newYork);
        singleGraph.addNode(lA);

        singleGraph.addEdge(cust1_id,cust2_id,10);
        singleGraph.addEdge(cust1_id,f_r_visited_1_id,20);
        singleGraph.addEdge(cust1_id,f_r_1_id,30);
        singleGraph.addEdge(cust1_id,newYork_id,40);
        singleGraph.addEdge(cust2_id,cust1_id,10);
        singleGraph.addEdge(cust2_id,cust3_id,10);
        singleGraph.addEdge(cust2_id,f_r_visited_1_id,20);
        singleGraph.addEdge(cust2_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust2_id,f_r_1_id,30);
        singleGraph.addEdge(cust2_id,f_r_2_id,30);
        singleGraph.addEdge(cust2_id,newYork_id,40);
        singleGraph.addEdge(cust3_id,cust2_id,10);
        singleGraph.addEdge(cust3_id,cust4_id,10);
        singleGraph.addEdge(cust3_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust3_id,f_r_2_id,30);
        singleGraph.addEdge(cust3_id,newYork_id,40);
        singleGraph.addEdge(cust4_id,cust3_id,10);
        singleGraph.addEdge(cust4_id,cust5_id,10);
        singleGraph.addEdge(cust4_id,f_r_visited_3_id,20);
        singleGraph.addEdge(cust4_id,f_r_3_id,30);
        singleGraph.addEdge(cust4_id,a_r_1_id,30);
        singleGraph.addEdge(cust4_id,lA_id,40);
        singleGraph.addEdge(cust5_id,cust4_id,10);
        singleGraph.addEdge(cust5_id,cust6_id,10);
        singleGraph.addEdge(cust5_id,a_r_1_id,20);
        singleGraph.addEdge(cust5_id,f_r_3_id,30);
        singleGraph.addEdge(cust5_id,a_r_2_id,30);
        singleGraph.addEdge(cust5_id,lA_id,40);
        singleGraph.addEdge(cust6_id,cust5_id,10);
        singleGraph.addEdge(cust6_id,f_r_visited_4_id,20);
        singleGraph.addEdge(cust6_id,a_r_2_id,30);
        singleGraph.addEdge(cust6_id,lA_id,40);
        singleGraph.addEdge(f_r_visited_1_id,newYork_id,50);
        singleGraph.addEdge(f_r_visited_2_id,newYork_id,50);
        singleGraph.addEdge(f_r_visited_3_id,lA_id,50);
        singleGraph.addEdge(f_r_visited_4_id,lA_id,50);
        singleGraph.addEdge(a_r_1_id,lA_id,50);
        singleGraph.addEdge(f_r_1_id,newYork_id,50);
        singleGraph.addEdge(f_r_2_id,newYork_id,50);
        singleGraph.addEdge(f_r_3_id,lA_id,50);
        singleGraph.addEdge(a_r_2_id,lA_id,50);// singleGraph finished

        // start query one, target_node_id equals to one of the below
        HPListGraph hpListGraph = new HPListGraph("query");
        int cust=hpListGraph.addNodeIndex(100);
        int cust_=hpListGraph.addNodeIndex(100);
        int f_r_liked=hpListGraph.addNodeIndex(200);
        int f_r_visited=hpListGraph.addNodeIndex(200);
        int city=hpListGraph.addNodeIndex(400);

        hpListGraph.removeNode(f_r_visited);
        hpListGraph.addEdgeIndex(cust,cust_,10,1);
        hpListGraph.addEdgeIndex(cust,f_r_liked,30,1);
        //hpListGraph.addEdgeIndex(cust,f_r_visited, 20,1);
        hpListGraph.addEdgeIndex(cust,city,40,1);
        hpListGraph.addEdgeIndex(cust_,cust,10,1);
//        hpListGraph.addEdgeIndex(cust_,f_r_visited,20,1);
//        hpListGraph.addEdgeIndex(cust_,f_r_liked,30,1);
//        hpListGraph.addEdgeIndex(cust_,city,40,1);
//        hpListGraph.addEdgeIndex(f_r_visited,city,50,1);
//        hpListGraph.addEdgeIndex(f_r_liked,city,50,1);


        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<>();
        HashMap<Integer, myNode> custMap=new HashMap<>();
        custMap.put(cust1_id,cust1);
        custMap.put(cust2_id,cust2);
        custMap.put(cust3_id,cust3);
        custMap.put(cust4_id,cust4);
        custMap.put(cust5_id,cust5);
        custMap.put(cust6_id,cust6);
        freqNodesByLabel.put(100,custMap);

//        HashMap<Integer, myNode> cust_Map=new HashMap<>();
//        cust_Map.put(cust1_id,cust1);
//        cust_Map.put(cust2_id,cust2);
//        cust_Map.put(cust3_id,cust3);
//        cust_Map.put(cust4_id,cust4);
//        cust_Map.put(cust5_id,cust5);
//        cust_Map.put(cust6_id,cust6);
//        freqNodesByLabel.put(cust_,cust_Map);

        HashMap<Integer, myNode> f_r_visitedMap=new HashMap<>();
        f_r_visitedMap.put(f_r_visited_1_id,f_r_visited_1);
        f_r_visitedMap.put(f_r_visited_2_id,f_r_visited_2);
        f_r_visitedMap.put(f_r_visited_3_id,f_r_visited_3);
        f_r_visitedMap.put(f_r_visited_4_id,f_r_visited_4);
        f_r_visitedMap.put(f_r_1_id,f_r_1);
        f_r_visitedMap.put(f_r_2_id,f_r_2);
        f_r_visitedMap.put(f_r_3_id,f_r_3);
        freqNodesByLabel.put(200,f_r_visitedMap);

//        HashMap<Integer, myNode> f_r_likedMap=new HashMap<>();
//        f_r_likedMap.put(f_r_1_id,f_r_1);
//        f_r_likedMap.put(f_r_2_id,f_r_2);
//        f_r_likedMap.put(f_r_3_id,f_r_3);
//        f_r_likedMap.put(f_r_visited_1_id,f_r_visited_1);
//        f_r_likedMap.put(f_r_visited_2_id,f_r_visited_2);
//        f_r_likedMap.put(f_r_visited_3_id,f_r_visited_3);
//        freqNodesByLabel.put(f_r_liked,f_r_likedMap);

        HashMap<Integer, myNode> cityMap=new HashMap<>();
        cityMap.put(newYork_id,newYork);
        cityMap.put(lA_id,lA);
        freqNodesByLabel.put(400,cityMap);

        singleGraph.setFreqNodesByLabel(freqNodesByLabel);

        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        // call the target_node_id search method
        gramiMatcher.getFrequency(nonCandidates,1,cust);



    }


    /**a matching example is given. The pattern is a graph*
     * @latest revise 8 Jan 2019
     * */
    public static void example2 () {
        /**Create a big graph. The ID of this graph is 100, and the frequency threshold is???.**/
        Graph singleGraph = new Graph(100,Integer.MAX_VALUE);

        /**Add nodes into the big graph**/
        int index_u0 = singleGraph.addNode(2);
        int index_u1 = singleGraph.addNode(1);
        int index_u2 = singleGraph.addNode(3);
        int index_u3 = singleGraph.addNode(2);
        int index_u4 = singleGraph.addNode(1);
        int index_u5 = singleGraph.addNode(1);
        int index_u6 = singleGraph.addNode(2);
        int index_u7 = singleGraph.addNode(3);

        /**create new nodes. **/
        myNode u0 = new myNode(index_u0,2);
        myNode u1 = new myNode(index_u1,1);
        myNode u2 = new myNode(index_u2,3);
        myNode u3 = new myNode(index_u3,2);
        myNode u4 = new myNode(index_u4,1);
        myNode u5 = new myNode(index_u5,1);
        myNode u6 = new myNode(index_u6,2);
        myNode u7 = new myNode(index_u7,3);


        /**add reachable nodes for each node in big graph. 19 Dec 2018**/
        u1.addreachableNode(u0,10);
        u2.addreachableNode(u1,30);
        u2.addreachableNode(u0,20);

        u2.addreachableNode(u3,20);
        u2.addreachableNode(u4,30);
        u4.addreachableNode(u3,10);

        u2.addreachableNode(u5,30);
        u2.addreachableNode(u6,20);
        u5.addreachableNode(u6,10);

        u7.addreachableNode(u0,20);
        u7.addreachableNode(u1,30);

        /***add myNode to the graph. 19 Dec 2018.**/
        singleGraph.addNode(u0);
        singleGraph.addNode(u1);
        singleGraph.addNode(u2);
        singleGraph.addNode(u3);
        singleGraph.addNode(u4);
        singleGraph.addNode(u5);
        singleGraph.addNode(u6);
        singleGraph.addNode(u7);


        /**add edges into the big graph**/
        singleGraph.addEdge(index_u1,index_u0,10);
        singleGraph.addEdge(index_u2,index_u1,30);
        singleGraph.addEdge(index_u2,index_u0,20);
        singleGraph.addEdge(index_u2,index_u3,20);
        singleGraph.addEdge(index_u2,index_u4,30);
        singleGraph.addEdge(index_u4,index_u3,10);
        singleGraph.addEdge(index_u2,index_u5,30);
        singleGraph.addEdge(index_u2,index_u6,20);
        singleGraph.addEdge(index_u5,index_u6,10);
        singleGraph.addEdge(index_u7,index_u0,20);
        singleGraph.addEdge(index_u7,index_u1,30);

        /**set the frequent nodes by label of the big graph**/
        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<Integer, HashMap<Integer, myNode>>();
        HashMap<Integer,myNode> one = new HashMap<Integer, myNode>();
        one.put(1,u1);
        one.put(4,u4);
        one.put(5,u5);
        freqNodesByLabel.put(1,one);
        HashMap<Integer,myNode> two = new HashMap<Integer, myNode>();
        two.put(0,u0);
        two.put(3,u3);
        two.put(6,u6);
        freqNodesByLabel.put(2,two);
        HashMap<Integer,myNode> three = new HashMap<Integer,myNode>();
        three.put(2,u2);
        three.put(7,u7);
        freqNodesByLabel.put(3,three);
        singleGraph.setFreqNodesByLabel(freqNodesByLabel);


        /**Begin to create HPListGraph**/
        HPListGraph hpListGraph = new HPListGraph("query");
        /**add nodes into the hpListGraph**/
        int v1 = hpListGraph.addNodeIndex(1);
        int v2 = hpListGraph.addNodeIndex(3);
        int v3 = hpListGraph.addNodeIndex(2);

        /**add edges into the hplistGraph**/
        hpListGraph.addEdgeIndex(v2,v1,30,1);/**not very clear the meaning of label and direction**/
        hpListGraph.addEdgeIndex(v1,v3,10,1);
        hpListGraph.addEdgeIndex(v2,v3,20,1);

        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<Integer, HashSet<Integer>>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        gramiMatcher.addLimitNode(3);
        gramiMatcher.addAnchorSet(1);
        gramiMatcher.getFrequency(nonCandidates, 1);//the pattern graph is a graph rather than a tree

    }


    /**print all of the correspondence*
     *
     * @from 14 Feb 2019
     * */
//     public static void printMatchedNode (List<Map<Node, Node>> matchedResults) {
//
//        for (int i = 0; i < matchedResults.size(); i++) {
//            Map<Node, Node> mp = matchedResults.get(i);
//            System.out.println("The No: " + i + " graph embedding is: ");
//            Iterator it = mp.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry) it.next();
//                Node nodeInPattern = (Node) pair.getKey();
//                Node nodeInDataGraph = (Node) pair.getValue();
//                System.out.println("node: " + nodeInPattern.getId() + " in pattern graph ----- node: " + nodeInDataGraph.getId() + " in data graph");
//            }
//        }
//    }



    /**a matching example is given. The pattern is a directed graph*
     * @latest check 8 Jan 2019
     * */
    public static void example3 () {
        /**Create a big graph. The ID of this graph is 100, and the frequency threshold is???.**/
        Graph singleGraph = new Graph(100,Integer.MAX_VALUE);

        /**Add nodes into the big graph**/
        int index_u0 = singleGraph.addNode(2);
        int index_u1 = singleGraph.addNode(1);
        int index_u2 = singleGraph.addNode(3);
        int index_u3 = singleGraph.addNode(2);
        int index_u4 = singleGraph.addNode(1);
        int index_u5 = singleGraph.addNode(1);
        int index_u6 = singleGraph.addNode(2);
        int index_u7 = singleGraph.addNode(3);

        /**create new nodes. **/
        myNode u0 = new myNode(index_u0,2);
        myNode u1 = new myNode(index_u1,1);
        myNode u2 = new myNode(index_u2,3);
        myNode u3 = new myNode(index_u3,2);
        myNode u4 = new myNode(index_u4,1);
        myNode u5 = new myNode(index_u5,1);
        myNode u6 = new myNode(index_u6,2);
        myNode u7 = new myNode(index_u7,3);


        /**add reachable nodes for each node in big graph. 19 Dec 2018**/
        u1.addreachableNode(u0,10);
        u2.addreachableNode(u1,30);
        u2.addreachableNode(u0,20);

        u2.addreachableNode(u3,20);
        u2.addreachableNode(u4,30);
        u4.addreachableNode(u3,10);

        u2.addreachableNode(u5,30);
        u2.addreachableNode(u6,20);
        u5.addreachableNode(u6,10);

        u7.addreachableNode(u0,20);
        u7.addreachableNode(u1,30);

        /***add myNode to the graph. 19 Dec 2018.**/
        singleGraph.addNode(u0);
        singleGraph.addNode(u1);
        singleGraph.addNode(u2);
        singleGraph.addNode(u3);
        singleGraph.addNode(u4);
        singleGraph.addNode(u5);
        singleGraph.addNode(u6);
        singleGraph.addNode(u7);


        /**add edges into the big graph**/
        singleGraph.addEdge(index_u1,index_u0,10);
        singleGraph.addEdge(index_u2,index_u1,30);
        singleGraph.addEdge(index_u2,index_u0,20);
        singleGraph.addEdge(index_u2,index_u3,20);
        singleGraph.addEdge(index_u2,index_u4,30);
        singleGraph.addEdge(index_u4,index_u3,10);
        singleGraph.addEdge(index_u2,index_u5,30);
        singleGraph.addEdge(index_u2,index_u6,20);
        singleGraph.addEdge(index_u5,index_u6,10);
        singleGraph.addEdge(index_u7,index_u0,20);
        singleGraph.addEdge(index_u7,index_u1,30);

        /**set the frequent nodes by label of the big graph**/
        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<Integer, HashMap<Integer, myNode>>();
        HashMap<Integer,myNode> one = new HashMap<Integer, myNode>();
        one.put(1,u1);
        one.put(4,u4);
        one.put(5,u5);
        freqNodesByLabel.put(1,one);
        HashMap<Integer,myNode> two = new HashMap<Integer, myNode>();
        two.put(0,u0);
        two.put(3,u3);
        two.put(6,u6);
        freqNodesByLabel.put(2,two);
        HashMap<Integer,myNode> three = new HashMap<Integer,myNode>();
        three.put(2,u2);
        three.put(7,u7);
        freqNodesByLabel.put(3,three);
        singleGraph.setFreqNodesByLabel(freqNodesByLabel);


        /**Begin to create HPListGraph**/
        HPListGraph hpListGraph = new HPListGraph("query");
        /**add nodes into the hpListGraph**/
        int v1 = hpListGraph.addNodeIndex(1);
        int v2 = hpListGraph.addNodeIndex(3);
        int v3 = hpListGraph.addNodeIndex(2);

        /**add edges into the hplistGraph**/
        hpListGraph.addEdgeIndex(v2,v1,30,1);/**not very clear the meaning of label and direction**/
        //hpListGraph.addEdgeIndex(v1,v3,10,1);
        hpListGraph.addEdgeIndex(v2,v3,20,1);

        Query q = new Query((HPListGraph<Integer,Double>)hpListGraph);/**create a new query**/

        HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<Integer, HashSet<Integer>>();//create a hashmap to save pruned variables
        GramiMatcher gramiMatcher = new GramiMatcher();
        gramiMatcher.setGraph(singleGraph);
        gramiMatcher.setQry(q);
        gramiMatcher.getFrequency(nonCandidates, 0);//the pattern graph is a tree

    }

    /**test for adaption GraMi to a sub-graph isomorphism matcher*
     * @from 8 Feb 2019
     * */
    public static void main (String args[]){
        myExample();
    }
}
