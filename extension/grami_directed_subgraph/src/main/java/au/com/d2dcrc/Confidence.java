package au.com.d2dcrc;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import automorphism.Automorphism;
import bisimulation.BisimulationChecker;
import dataStructures.Graph;
import dataStructures.HPListGraph;
import dataStructures.Query;
import dataStructures.myNode;
import edu.isi.karma.rep.alignment.Node;
import scala.Serializable;
import utilities.MyPair;

import java.util.*;

public class Confidence implements Serializable {
    private Graph G = null;
    private Query Q = null;
    private int target_node_id;
    private int target_node_label;
    private double target_edge_label;

    private List<Integer> idOfXInQ;
    private List<Integer> idOfXInq;
    private HashMap<Integer, Integer> _qGMap;

    public Confidence(int target_node_id, int target_node_label, double target_edge_label) {
        idOfXInQ = new ArrayList<>();
        idOfXInq = new ArrayList<>();
        _qGMap = new HashMap<>();
        this.target_node_id = target_node_id;
        this.target_node_label = target_node_label;
        this.target_edge_label = target_edge_label;
    }

    public void setG(Graph G) {
        this.G = G;
    }

    public void setQ(Query Q) {
        this.Q = Q;
    }

    private void setIdOfXInq(List<Integer> list) {
        idOfXInq = list;
    }

    public List<Integer> getIdOfXInq() {
        return idOfXInq;
    }

    /**
     *@discription: 设置满足Q(x,G)的整数型list -> 满足Q(x,G)的G中x的id的集合
     *@param nonCandidates
	 *@param matchingOption
     *@date: 2020/3/17 1:55
     *@return: int
     *@author: Han
     */
    public int setIdOfXInQ(HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption) {
        idOfXInQ = getIDOfXInQuery(Q, nonCandidates, matchingOption);
//        System.out.println("support(Q,G) = " + idOfXInQ.size());
        return idOfXInQ.size();
    }



    /**
     *@discription: 复用代码 -> 获取满足target_node的query的大图中节点id的集合
     *@param query
	 *@param nonCandidates
	 *@param matchingOption
     *@date: 2020/3/17 1:57
     *@return: java.util.List<java.lang.Integer>
     *@author: Han
     */
    private List<Integer> getIDOfXInQuery(Query query,HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption){
        List<Integer> list = new ArrayList<>();
        ConstraintGraph cg = new ConstraintGraph(G, query, nonCandidates);
        DFSSearch df = new DFSSearch(cg, G.getFreqThreshold(), nonCandidates);
        try{
            if (matchingOption == 0) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern graph is a tree while the labels are unique...");
                df.searchExistances2();
            } else if (matchingOption == 1) {
                df.searchExistances(target_node_id);
            }
            list = df.getListOfX(target_node_id);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     *@discription: support(q,G)
     *@param query
	 *@param nonCandidates
	 *@param matchingOption
     *@date: 2020/3/17 1:59
     *@return: int
     *@author: Han
     */
    public int getq_GSupport(Query query, HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption) {
        List<Integer> q_G_list = getIDOfXInQuery(query, nonCandidates, matchingOption);
        setIdOfXInq(q_G_list);
        int support = q_G_list.size();
        System.out.println("support(q,G) = " + support);
        return support;
    }

    public HashMap<Integer, Integer> getSourceToTarget(Query query, HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption) {
        ConstraintGraph cg = new ConstraintGraph(G, query, nonCandidates);
        DFSSearch df = new DFSSearch(cg, G.getFreqThreshold(), nonCandidates);
        HashMap<Integer, Integer> result = new HashMap<>();
        try{
            if (matchingOption == 0) {
                System.out.println("MINIMUM IMAGE BASED METRICS! The pattern graph is a tree while the labels are unique...");
                df.searchExistances2();
            } else if (matchingOption == 1) {
                df.searchExistances(target_node_id);
            }
            for (Map<Node, Node> map : df.getIsomorphismList()) {
                int key = 0;
                int value = 0;
                int time = 0;
                for (Map.Entry<Node, Node> entry : map.entrySet()) {
                    key = Integer.valueOf(entry.getValue().getId());
                    break;
                }
                for (Map.Entry<Node, Node> entry : map.entrySet()) {
                    ++time;
                    if (time == 2) {
                        value = Integer.valueOf(entry.getValue().getId());
                    }
                }
                result.put(key, value);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *@discription: support(R,G)
     *@param query
	 *@param nonCandidates
	 *@param matchingOption
     *@date: 2020/3/17 1:59
     *@return: int
     *@author: Han
     */
    public int getR_GSupport(Query query, HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption) {
        int support=getIDOfXInQuery(query, nonCandidates, matchingOption).size();
//        System.out.println("support(R,G) = " + support);
        return support;
    }

    public List<Integer> getR_GSupportList(Query query, HashMap<Integer, HashSet<Integer>> nonCandidates, int matchingOption) {
        List<Integer> l = getIDOfXInQuery(query, nonCandidates, matchingOption);
//        System.out.println("support(R,G) = " + l.size());
        return l;
    }

    /**
     *@discription: support(_q,G)
     *@param
     *@date: 2020/3/17 1:59
     *@return: int
     *@author: Han
     */
    public int get_q_GSupport() {
        HashMap<Integer, Integer> result = new HashMap<>();
        HashMap<Integer, myNode> nodesByTargetLabel = G.getFreqNodesByLabel().get(target_node_label);
        for (Map.Entry<Integer, myNode> entry : nodesByTargetLabel.entrySet()) {
            int id = entry.getKey();
            myNode node = entry.getValue();
            int count = 0;
            HashMap<Integer, ArrayList<MyPair<Integer, Double>>> reachableWithNodes = node.getReachableWithNodes();
            for (Map.Entry<Integer, ArrayList<MyPair<Integer, Double>>> inEntry : reachableWithNodes.entrySet()) {
                ArrayList<MyPair<Integer, Double>> list = inEntry.getValue();
                for (MyPair<Integer, Double> pair : list) {
                    if (pair.getB() == target_edge_label) {
                        result.put(id, ++count);
                    }
                }
            }
        }
        result.entrySet().removeIf(entry -> entry.getValue() == 0);
        for (int id : idOfXInq) {
            result.entrySet().removeIf(entry -> entry.getKey() == id);
        }
        _qGMap = result;
        int support = result.size();
//        System.out.println("support(_q,G) = " + support);
        return support;
    }

    /**
     *@discription: support(Q_q,G)
     *@param
     *@date: 2020/3/17 1:59
     *@return: int
     *@author: Han
     */
    public int getQ_qGSupport() {
        HashMap<Integer, Integer> result = (HashMap<Integer, Integer>) _qGMap.clone();
        result.entrySet().removeIf(entry -> !idOfXInQ.contains(entry.getKey()));
        int support = result.size();
//        System.out.println("support(Q_q,G) = " + support);
        return support;
    }


//    public static void bisimulation() {
//        BisimulationChecker bisimulationChecker = new BisimulationChecker("p.txt","q.txt");
//        System.out.println(bisimulationChecker.isBisimulation());
//    }



    public static void myExample() {
        Graph singleGraph = new Graph(100, Integer.MAX_VALUE);

        /**representation
         * 100->customer
         * 200->French restaurant
         * 300->Asian restaurant
         * 400->city
         */
//        int cust4_id = singleGraph.addNode(100);
//        int cust5_id = singleGraph.addNode(100);
//        int cust6_id = singleGraph.addNode(100);
//        int f_r_3_id = singleGraph.addNode(200);
//        int f_r_3_id_1 = singleGraph.addNode(200);
//        int f_r_3_id_2 = singleGraph.addNode(200);
//        int f_r_visited_3_id = singleGraph.addNode(200);
//        int f_r_visited_4_id = singleGraph.addNode(200);
//        int a_r_1_id = singleGraph.addNode(300);
//        int a_r_2_id = singleGraph.addNode(300);
//        int lA_id = singleGraph.addNode(400);
//
//        myNode cust4 = new myNode(cust4_id,100);
//        myNode cust5 = new myNode(cust5_id,100);
//        myNode cust6 = new myNode(cust6_id,100);
//        myNode f_r_3 = new myNode(f_r_3_id,200);
//        myNode f_r_3_1 = new myNode(f_r_3_id_1,200);
//        myNode f_r_3_2 = new myNode(f_r_3_id_2,200);
//        myNode f_r_visited_3 = new myNode(f_r_visited_3_id,200);
//        myNode f_r_visited_4 = new myNode(f_r_visited_4_id,200);
//        myNode a_r_1 = new myNode(a_r_1_id,300);
//        myNode a_r_2 = new myNode(a_r_2_id,300);
//        myNode lA = new myNode(lA_id,400);
//
//        cust4.addreachableNode(cust5,10);
//        cust4.addreachableNode(f_r_visited_3,20);
//        cust4.addreachableNode(f_r_3,30);
//        cust4.addreachableNode(f_r_3_1,30);
//        cust4.addreachableNode(f_r_3_2,30);
//        cust4.addreachableNode(a_r_1,30);
//        cust4.addreachableNode(lA,40);
//        cust5.addreachableNode(cust4,10);
//        cust5.addreachableNode(cust6,10);
//        cust5.addreachableNode(a_r_1,20);
//        cust5.addreachableNode(f_r_3,30);
//        cust5.addreachableNode(f_r_3_1,30);
//        cust5.addreachableNode(f_r_3_2,30);
//        cust5.addreachableNode(a_r_2,30);
//        cust5.addreachableNode(lA,40);
//        cust6.addreachableNode(cust5,10);
//        cust6.addreachableNode(f_r_visited_4,20);
//        cust6.addreachableNode(a_r_2,30);
//        cust6.addreachableNode(lA,40);
//        f_r_visited_3.addreachableNode(lA,50);
//        f_r_visited_4.addreachableNode(lA,50);
//        a_r_1.addreachableNode(lA,50);
//        f_r_3.addreachableNode(lA,50);
//        f_r_3_1.addreachableNode(lA,50);
//        f_r_3_2.addreachableNode(lA,50);
//        a_r_2.addreachableNode(lA,50);
//
//        singleGraph.addNode(cust4);
//        singleGraph.addNode(cust5);
//        singleGraph.addNode(cust6);
//        singleGraph.addNode(f_r_3);
//        singleGraph.addNode(f_r_3_1);
//        singleGraph.addNode(f_r_3_2);
//        singleGraph.addNode(f_r_visited_3);
//        singleGraph.addNode(f_r_visited_4);
//        singleGraph.addNode(a_r_1);
//        singleGraph.addNode(a_r_2);
//        singleGraph.addNode(lA);
//
//        singleGraph.addEdge(cust4_id,cust5_id,10);
//        singleGraph.addEdge(cust4_id,f_r_visited_3_id,20);
//        singleGraph.addEdge(cust4_id,f_r_3_id,30);
//        singleGraph.addEdge(cust4_id,f_r_3_id_1,30);
//        singleGraph.addEdge(cust4_id,f_r_3_id_2,30);
//        singleGraph.addEdge(cust4_id,a_r_1_id,30);
//        singleGraph.addEdge(cust4_id,lA_id,40);
//        singleGraph.addEdge(cust5_id,cust4_id,10);
//        singleGraph.addEdge(cust5_id,cust6_id,10);
//        singleGraph.addEdge(cust5_id,a_r_1_id,20);
//        singleGraph.addEdge(cust5_id,f_r_3_id,30);
//        singleGraph.addEdge(cust5_id,f_r_3_id_1,30);
//        singleGraph.addEdge(cust5_id,f_r_3_id_2,30);
//        singleGraph.addEdge(cust5_id,a_r_2_id,30);
//        singleGraph.addEdge(cust5_id,lA_id,40);
//        singleGraph.addEdge(cust6_id,cust5_id,10);
//        singleGraph.addEdge(cust6_id,f_r_visited_4_id,20);
//        singleGraph.addEdge(cust6_id,a_r_2_id,30);
//        singleGraph.addEdge(cust6_id,lA_id,40);
//        singleGraph.addEdge(f_r_visited_3_id,lA_id,50);
//        singleGraph.addEdge(f_r_visited_4_id,lA_id,50);
//        singleGraph.addEdge(a_r_1_id,lA_id,50);
//        singleGraph.addEdge(f_r_3_id,lA_id,50);
//        singleGraph.addEdge(f_r_3_id_1,lA_id,50);
//        singleGraph.addEdge(f_r_3_id_2,lA_id,50);
//        singleGraph.addEdge(a_r_2_id,lA_id,50);
//
//        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel2 = new HashMap<>();
//        HashMap<Integer, myNode> custMap2=new HashMap<>();
//        custMap2.put(cust4_id,cust4);
//        custMap2.put(cust5_id,cust5);
//        custMap2.put(cust6_id,cust6);
//        freqNodesByLabel2.put(100,custMap2);
//
//        HashMap<Integer, myNode> f_rMap2=new HashMap<>();
//        f_rMap2.put(f_r_visited_3_id,f_r_visited_3);
//        f_rMap2.put(f_r_visited_4_id,f_r_visited_4);
//        f_rMap2.put(f_r_3_id,f_r_3);
//        f_rMap2.put(f_r_3_id_1,f_r_3_1);
//        f_rMap2.put(f_r_3_id_2,f_r_3_2);
//        freqNodesByLabel2.put(200,f_rMap2);
//
//        HashMap<Integer, myNode> cityMap2=new HashMap<>();
//        cityMap2.put(lA_id,lA);
//        freqNodesByLabel2.put(400,cityMap2);
//
//        HashMap<Integer, myNode> a_rMap=new HashMap<>();
//        a_rMap.put(a_r_1_id, a_r_1);
//        a_rMap.put(a_r_2_id, a_r_2);
//        freqNodesByLabel2.put(300,a_rMap);
//
//        singleGraph.setFreqNodesByLabel(freqNodesByLabel2);// finish part2

        int cust1_id = singleGraph.addNode(100);
        int cust2_id = singleGraph.addNode(100);
        int cust3_id = singleGraph.addNode(100);
        int cust4_id = singleGraph.addNode(100);
        int cust5_id = singleGraph.addNode(100);
        int cust6_id = singleGraph.addNode(100);
        int f_r_1_id = singleGraph.addNode(200);
        int f_r_1_id_1 = singleGraph.addNode(200);
        int f_r_1_id_2 = singleGraph.addNode(200);
        int f_r_2_id = singleGraph.addNode(200);
        int f_r_2_id_1 = singleGraph.addNode(200);
        int f_r_2_id_2 = singleGraph.addNode(200);
        int f_r_3_id = singleGraph.addNode(200);
        int f_r_3_id_1 = singleGraph.addNode(200);
        int f_r_3_id_2 = singleGraph.addNode(200);
        int f_r_visited_1_id = singleGraph.addNode(200);
        int f_r_visited_2_id = singleGraph.addNode(200);
        int f_r_visited_3_id = singleGraph.addNode(200);
        int f_r_visited_4_id = singleGraph.addNode(200);
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
        myNode f_r_1 = new myNode(f_r_1_id,200);
        myNode f_r_1_1 = new myNode(f_r_1_id_1,200);
        myNode f_r_1_2 = new myNode(f_r_1_id_2,200);
        myNode f_r_2 = new myNode(f_r_2_id,200);
        myNode f_r_2_1 = new myNode(f_r_2_id_1,200);
        myNode f_r_2_2 = new myNode(f_r_2_id_2,200);
        myNode f_r_3 = new myNode(f_r_3_id,200);
        myNode f_r_3_1 = new myNode(f_r_3_id_1,200);
        myNode f_r_3_2 = new myNode(f_r_3_id_2,200);
        myNode f_r_visited_1 = new myNode(f_r_visited_1_id,200);
        myNode f_r_visited_2 = new myNode(f_r_visited_2_id,200);
        myNode f_r_visited_3 = new myNode(f_r_visited_3_id,200);
        myNode f_r_visited_4 = new myNode(f_r_visited_4_id,200);
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
        cust1.addreachableNode(f_r_1_1,30);
        cust1.addreachableNode(f_r_1_2,30);
        cust1.addreachableNode(newYork,40);
        cust2.addreachableNode(cust1,10);
        cust2.addreachableNode(cust3,10);
        cust2.addreachableNode(f_r_visited_1,20);
        cust2.addreachableNode(f_r_visited_2,20);
        cust2.addreachableNode(f_r_1,30);
        cust2.addreachableNode(f_r_1_1,30);
        cust2.addreachableNode(f_r_1_2,30);
        cust2.addreachableNode(f_r_2,30);
        cust2.addreachableNode(f_r_2_1,30);
        cust2.addreachableNode(f_r_2_2,30);
        cust2.addreachableNode(newYork,40);
        cust3.addreachableNode(cust2,10);
        cust3.addreachableNode(cust4,10);
        cust3.addreachableNode(f_r_visited_2,20);
        cust3.addreachableNode(f_r_2,30);
        cust3.addreachableNode(f_r_2_1,30);
        cust3.addreachableNode(f_r_2_2,30);
        cust3.addreachableNode(newYork,40);
        cust4.addreachableNode(cust3,10);
        cust4.addreachableNode(cust5,10);
        cust4.addreachableNode(f_r_visited_3,20);
        cust4.addreachableNode(f_r_3,30);
        cust4.addreachableNode(f_r_3_1,30);
        cust4.addreachableNode(f_r_3_2,30);
        cust4.addreachableNode(a_r_1,30);
        cust4.addreachableNode(lA,40);
        cust5.addreachableNode(cust4,10);
        cust5.addreachableNode(cust6,10);
        cust5.addreachableNode(a_r_1,20);
        cust5.addreachableNode(f_r_3,30);
        cust5.addreachableNode(f_r_3_1,30);
        cust5.addreachableNode(f_r_3_2,30);
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
        f_r_1_1.addreachableNode(newYork,50);
        f_r_1_2.addreachableNode(newYork,50);
        f_r_2.addreachableNode(newYork,50);
        f_r_2_1.addreachableNode(newYork,50);
        f_r_2_2.addreachableNode(newYork,50);
        f_r_3.addreachableNode(lA,50);
        f_r_3_1.addreachableNode(lA,50);
        f_r_3_2.addreachableNode(lA,50);
        a_r_2.addreachableNode(lA,50);

        singleGraph.addNode(cust1);
        singleGraph.addNode(cust2);
        singleGraph.addNode(cust3);
        singleGraph.addNode(cust4);
        singleGraph.addNode(cust5);
        singleGraph.addNode(cust6);
        singleGraph.addNode(f_r_1);
        singleGraph.addNode(f_r_1_1);
        singleGraph.addNode(f_r_1_2);
        singleGraph.addNode(f_r_2);
        singleGraph.addNode(f_r_2_1);
        singleGraph.addNode(f_r_2_2);
        singleGraph.addNode(f_r_3);
        singleGraph.addNode(f_r_3_1);
        singleGraph.addNode(f_r_3_2);
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
        singleGraph.addEdge(cust1_id,f_r_1_id_1,30);
        singleGraph.addEdge(cust1_id,f_r_1_id_2,30);
        singleGraph.addEdge(cust1_id,newYork_id,40);
        singleGraph.addEdge(cust2_id,cust1_id,10);
        singleGraph.addEdge(cust2_id,cust3_id,10);
        singleGraph.addEdge(cust2_id,f_r_visited_1_id,20);
        singleGraph.addEdge(cust2_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust2_id,f_r_1_id,30);
        singleGraph.addEdge(cust2_id,f_r_1_id_1,30);
        singleGraph.addEdge(cust2_id,f_r_1_id_2,30);
        singleGraph.addEdge(cust2_id,f_r_2_id,30);
        singleGraph.addEdge(cust2_id,f_r_2_id_1,30);
        singleGraph.addEdge(cust2_id,f_r_2_id_1,30);
        singleGraph.addEdge(cust2_id,newYork_id,40);
        singleGraph.addEdge(cust3_id,cust2_id,10);
        singleGraph.addEdge(cust3_id,cust4_id,10);
        singleGraph.addEdge(cust3_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust3_id,f_r_2_id,30);
        singleGraph.addEdge(cust3_id,f_r_2_id_1,30);
        singleGraph.addEdge(cust3_id,f_r_2_id_2,30);
        singleGraph.addEdge(cust3_id,newYork_id,40);
        singleGraph.addEdge(cust4_id,cust3_id,10);
        singleGraph.addEdge(cust4_id,cust5_id,10);
        singleGraph.addEdge(cust4_id,f_r_visited_3_id,20);
        singleGraph.addEdge(cust4_id,f_r_3_id,30);
        singleGraph.addEdge(cust4_id,f_r_3_id_1,30);
        singleGraph.addEdge(cust4_id,f_r_3_id_2,30);
        singleGraph.addEdge(cust4_id,a_r_1_id,30);
        singleGraph.addEdge(cust4_id,lA_id,40);
        singleGraph.addEdge(cust5_id,cust4_id,10);
        singleGraph.addEdge(cust5_id,cust6_id,10);
        singleGraph.addEdge(cust5_id,a_r_1_id,20);
        singleGraph.addEdge(cust5_id,f_r_3_id,30);
        singleGraph.addEdge(cust5_id,f_r_3_id_1,30);
        singleGraph.addEdge(cust5_id,f_r_3_id_2,30);
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
        singleGraph.addEdge(f_r_1_id_1,newYork_id,50);
        singleGraph.addEdge(f_r_1_id_2,newYork_id,50);
        singleGraph.addEdge(f_r_2_id,newYork_id,50);
        singleGraph.addEdge(f_r_2_id_1,newYork_id,50);
        singleGraph.addEdge(f_r_2_id_2,newYork_id,50);
        singleGraph.addEdge(f_r_3_id,lA_id,50);
        singleGraph.addEdge(f_r_3_id_1,lA_id,50);
        singleGraph.addEdge(f_r_3_id_2,lA_id,50);
        singleGraph.addEdge(a_r_2_id,lA_id,50);// singleGraph finished

        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<>();
        HashMap<Integer, myNode> custMap=new HashMap<>();
        custMap.put(cust1_id,cust1);
        custMap.put(cust2_id,cust2);
        custMap.put(cust3_id,cust3);
        custMap.put(cust4_id,cust4);
        custMap.put(cust5_id,cust5);
        custMap.put(cust6_id,cust6);
        freqNodesByLabel.put(100,custMap);

        HashMap<Integer, myNode> f_rMap=new HashMap<>();
        f_rMap.put(f_r_visited_1_id,f_r_visited_1);
        f_rMap.put(f_r_visited_2_id,f_r_visited_2);
        f_rMap.put(f_r_visited_3_id,f_r_visited_3);
        f_rMap.put(f_r_visited_4_id,f_r_visited_4);
        f_rMap.put(f_r_1_id,f_r_1);
        f_rMap.put(f_r_1_id_1,f_r_1_1);
        f_rMap.put(f_r_1_id_2,f_r_1_2);
        f_rMap.put(f_r_2_id,f_r_2);
        f_rMap.put(f_r_2_id_1,f_r_2_1);
        f_rMap.put(f_r_2_id_2,f_r_2_2);
        f_rMap.put(f_r_3_id,f_r_3);
        f_rMap.put(f_r_3_id_1,f_r_3_1);
        f_rMap.put(f_r_3_id_2,f_r_3_2);
        freqNodesByLabel.put(200,f_rMap);

        HashMap<Integer, myNode> cityMap=new HashMap<>();
        cityMap.put(newYork_id,newYork);
        cityMap.put(lA_id,lA);
        freqNodesByLabel.put(400,cityMap);

        HashMap<Integer, myNode> a_rMap=new HashMap<>();
        a_rMap.put(a_r_1_id, a_r_1);
        a_rMap.put(a_r_2_id, a_r_2);
        freqNodesByLabel.put(300,a_rMap);

        singleGraph.setFreqNodesByLabel(freqNodesByLabel);// finish singleGraph


        HashMap<Integer, HashSet<Integer>> nonCandidates_1 = new HashMap<>();//create a hashmap to save pruned variables
        HashMap<Integer, HashSet<Integer>> nonCandidates_2 = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> nonCandidates_3 = new HashMap<>();

        // start from support(q,G)
        HPListGraph queryGraph = new HPListGraph("query");
        int cust=queryGraph.addNodeIndex(100);
        int f_r_visited=queryGraph.addNodeIndex(200);
        queryGraph.addEdgeIndex(cust,f_r_visited,20,1);

        Query q = new Query((HPListGraph<Integer,Double>)queryGraph);

        Confidence confidence = new Confidence(cust, 100, 20);
        confidence.setG(singleGraph);
        int qGSupport = confidence.getq_GSupport(q, nonCandidates_3, 1);

        // support(_q,G)
        int _qGSupport = confidence.get_q_GSupport();

        /**
         * 修改理由:
         * HPListGraph 此数据结构在removeNode的时候
         * node的个数没有改变
         * 所以新建，不再重用
         */

        // then set Q list
        HPListGraph QueryGraph = new HPListGraph("Query");
        int Q_cust=QueryGraph.addNodeIndex(100); // make sure Q_cust equals to cust, always first add node of aimed cust
        int Q_cust_=QueryGraph.addNodeIndex(100);
        //int Q_a_r_liked=QueryGraph.addNodeIndex(300);
        int Q_city=QueryGraph.addNodeIndex(400);
        //int Q_f_r_visited=QueryGraph.addNodeIndex(200);
//        int Q_f_r_visited_1=QueryGraph.addNodeIndex(200);
        int Q_f_r_liked=QueryGraph.addNodeIndex(200);
        int Q_f_r_liked_1=QueryGraph.addNodeIndex(200);
        int Q_f_r_liked_2=QueryGraph.addNodeIndex(200);
        //int Q_f_r_liked=QueryGraph.addNodeIndex(200);

        //int Q_cust_1=QueryGraph.addNodeIndex(100);
        QueryGraph.addEdgeIndex(Q_cust,Q_cust_,10,1);
        //QueryGraph.addEdgeIndex(Q_cust,Q_a_r_liked,30,1);
        QueryGraph.addEdgeIndex(Q_cust,Q_f_r_liked,30,1);
        QueryGraph.addEdgeIndex(Q_cust,Q_f_r_liked_1,30,1);
        QueryGraph.addEdgeIndex(Q_cust,Q_f_r_liked_2,30,1);
        QueryGraph.addEdgeIndex(Q_cust,Q_city,40,1);
        QueryGraph.addEdgeIndex(Q_cust_,Q_cust,10,1);
        //QueryGraph.addEdgeIndex(Q_cust_,Q_a_r_liked,30,1);
        //QueryGraph.addEdgeIndex(Q_cust_,Q_city,40,1);
//        QueryGraph.addEdgeIndex(Q_cust_,Q_f_r_visited_1,20,1);
        //QueryGraph.addEdgeIndex(Q_cust_,Q_f_r_liked,30,1);
        //QueryGraph.addEdgeIndex(Q_a_r_liked,Q_city,50,1);
        //QueryGraph.addEdgeIndex(Q_f_r_visited,Q_city,50,1);
        //QueryGraph.addEdgeIndex(Q_cust_,Q_f_r_liked,30,1);
//        QueryGraph.addEdgeIndex(cust_,f_r_visited,20,1);
//        QueryGraph.addEdgeIndex(cust_,f_r_liked,30,1);
//        QueryGraph.addEdgeIndex(cust_,city,40,1);
//        QueryGraph.addEdgeIndex(f_r_visited,city,50,1);
//        QueryGraph.addEdgeIndex(f_r_liked,city,50,1);

        Query Q = new Query((HPListGraph<Integer,Double>)QueryGraph);/**create Q**/
        confidence.setQ(Q);
        confidence.setIdOfXInQ(nonCandidates_1, 1);

        // then support(R,G)
        int Q_f_r_visited=QueryGraph.addNodeIndex(200);
        QueryGraph.addEdgeIndex(Q_cust,Q_f_r_visited,20,1);
        Query R = new Query((HPListGraph<Integer,Double>)QueryGraph);// create Rule
        int RGSupport = confidence.getR_GSupport(R, nonCandidates_2, 1);


        // support(Q_q,G)
        int Q_qGSupport = confidence.getQ_qGSupport();

        double conf = ((double) ((RGSupport)*(_qGSupport))) / ((double) ((Q_qGSupport)*(qGSupport)));
        System.out.println("confidence(R,G) = " +conf);

    }

    public static void auto() {
        HPListGraph graph = new HPListGraph("test");
        int cust=graph.addNodeIndex(100);
        int city=graph.addNodeIndex(300);
        int cust_=graph.addNodeIndex(100);
//        int cust__=graph.addNodeIndex(100);
//        int cust_test=graph.addNodeIndex(100);
//        int cust_test_=graph.addNodeIndex(100);
        int frl=graph.addNodeIndex(200);
        int frl1=graph.addNodeIndex(200);
        int frl2=graph.addNodeIndex(200);
//        int frl_=graph.addNodeIndex(200);
//        int frl_1=graph.addNodeIndex(200);
//        int frl_2=graph.addNodeIndex(200);
//        int test=graph.addNodeIndex(500);
//        int test_=graph.addNodeIndex(500);
//        int test=graph.addNodeIndex(200);

        graph.addEdgeIndex(cust,cust_,10,1);
//        graph.addEdgeIndex(cust_test,frl,30,1);
//        graph.addEdgeIndex(cust_test,frl1,30,1);
//        graph.addEdgeIndex(cust_test,frl2,30,1);
        graph.addEdgeIndex(cust,city,40,1);
        graph.addEdgeIndex(cust_,cust,10,1);
//        graph.addEdgeIndex(cust,cust__,10,1);
//        graph.addEdgeIndex(cust__,cust,10,1);
//        graph.addEdgeIndex(cust,frl,20,1);
//        graph.addEdgeIndex(cust,frl_,20,1);
//        graph.addEdgeIndex(cust,cust_test,20,1);
//        graph.addEdgeIndex(cust_test,cust,20,1);
//        graph.addEdgeIndex(cust,cust_test_,20,1);
//        graph.addEdgeIndex(cust_test_,cust,20,1);
//        graph.addEdgeIndex(cust_test_,frl_,30,1);
//        graph.addEdgeIndex(cust_test_,frl_1,30,1);
//        graph.addEdgeIndex(cust_test_,frl_2,30,1);
//        graph.addEdgeIndex(frl,test,80,1);
//        graph.addEdgeIndex(frl_,test_,80,1);
//        graph.addEdgeIndex(cust,test,30,1);
        graph.addEdgeIndex(cust,frl,30,1);
        graph.addEdgeIndex(cust,frl1,30,1);
        graph.addEdgeIndex(cust,frl2,30,1);

        Automorphism automorphism=new Automorphism(graph);
        System.out.println("whether has automorphism");
        System.out.println(automorphism.hasAutomorphisms());


        System.out.println(automorphism.getAutomorphicGraph());

    }


    public static void main(String[] args) {
        myExample();
        //auto();
        //bisimulation();
    }


}
