package Dmine;

import CSP.ConstraintGraph;
import CSP.DFSSearch;
import CSP.Variable;
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
import utilities.MyPair;

import java.util.*;
import java.util.stream.Collectors;

public class Dmine {
    private Graph singleGraph; // single graph G
    private Query query; // q(x,y)
    private int support; // minimal support
    private int k; // top k
    private int d; // d rounds
    private double lambda; // used in diversification
    private int n; // separate a single graph into n graphs
    private static JavaSparkContext sc;

    private int source_id;
    private int source_label;
    private int edge_label;
    private int target_label;

    private List<dminePartition> dminePartitions;

    private List<Graph> partitions;
//    private List<Integer> qGSupports;
//    private List<Integer> _qGSupports;
//    private List<Confidence> confs;
//    private List<HashMap<Integer, Integer>> sourceToTargets;
    private List<List<Rule>> allExtendRules;

    private int qGSupport = 0;
    private int _qGSupport = 0;
    private List<Rule> extendRules;
    private List<Message> curMessages;
    private List<Message> allMessages;


    private HashSet<MessagePair> topK;

//    private Confidence conf;
//    private HashMap<Integer, Integer> sourceToTarget;


    public Dmine(Graph singleGraph, int source_node_label, int target_node_label, int edge_label, int support, int k, int d, int n, double lambda) {
        this.singleGraph = singleGraph;
        this.support = support;
        this.k = k;
        this.d = d;
        this.n = n;
        this.lambda = lambda;
        extendRules = new ArrayList<>();

        HPListGraph queryGraph = new HPListGraph();
        source_label = source_node_label;
        this.edge_label = edge_label;
        target_label = target_node_label;
        source_id = queryGraph.addNodeIndex(source_node_label);
        int target_id = queryGraph.addNodeIndex(target_node_label);
        queryGraph.addEdgeIndex(source_id,target_id,edge_label,1);

        query = new Query((HPListGraph<Integer,Double>) queryGraph);

        allMessages = new ArrayList<>();

        topK = new HashSet<> (k/2);

        Partition partition = new Partition(singleGraph, n);
        partitions = partition.separate();
        dminePartitions = new ArrayList<>(partitions.size());
//        qGSupports = new ArrayList<>(partitions.size());
//        _qGSupports = new ArrayList<>(partitions.size());
//        confs = new ArrayList<>(partitions.size());
//        sourceToTargets = new ArrayList<>(partitions.size());
        allExtendRules = new ArrayList<>(partitions.size());

        SparkConf conf=new SparkConf()
                .setAppName("test")
                .setMaster("local")
                .set("spark.driver.allowMultipleContexts", "true");
        JavaSparkContext sc=new JavaSparkContext(conf);
        this.sc = sc;

    }

    // initial localMine method
    public void partitionInit() {
        //int i = 0;
        for (int j = 0; j < partitions.size(); j++) {
            Confidence conf = new Confidence(source_id, source_label, edge_label);
            conf.setG(partitions.get(j));
            HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables
            dminePartition dminePartition = new dminePartition(j + 1, partitions.get(j), conf,
                    conf.getq_GSupport(query, nonCandidates, 1), conf.get_q_GSupport(), conf.getSourceToTarget(query, nonCandidates, 1),
                    edge_label, target_label, support, source_label);
            this.dminePartitions.add(dminePartition);
        }
        for (dminePartition dmine : dminePartitions) {
            this.qGSupport += dmine.getqGSupport();
            this._qGSupport += dmine.get_qGSupport();
            //allExtendRules.add(dmine.firstMining());
        }

        JavaRDD<dminePartition> dmineTransactions = sc.parallelize(dminePartitions);
        JavaRDD<List<Rule>> extendRules = dmineTransactions
                .flatMap((FlatMapFunction<dminePartition, List<Rule>>) dmine -> {
                    List<Rule> rules = dmine.firstMiningNew();
                    List<List<Rule>> list = new ArrayList<>();
                    list.add(rules);
                    return list.iterator();
                });
        allExtendRules.addAll(extendRules.collect());

        this.extendRules = partitionAutomorphismChecking();

    }




    // automorphism checking between partitions
    private List<Rule> partitionAutomorphismChecking() {
        List<Rule> candidates = new ArrayList<>();
//        for (List<Rule> rules : this.allExtendRules) {
//            candidates.addAll(rules);
//        }
        long start = System.currentTimeMillis();
//        automorphismCheck(candidates);
        candidates = allExtendRules.get(0);
        for (int i = 1; i < allExtendRules.size(); i++) {
            candidates = automorphismChecking(candidates, allExtendRules.get(i));
        }
        System.out.println("It costs while bi-simulation at the coordinator："+(System.currentTimeMillis() - start) +"ms");
        return candidates;
    }


    private HashMap<Integer, List<Rule>> dealWithMessage() {
        extendRules.clear();
        allExtendRules.clear();
        HashMap<Integer, List<Rule>> sort = new HashMap<>();
        for (Message m : curMessages) {
            HashMap<Integer, List<Integer>> map = m.getPartitionCenterNodesId();
            for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
                List<Rule> sortedRule = sort.containsKey(entry.getKey()) ? sort.get(entry.getKey()) : new ArrayList<>();
                List<Integer> list = entry.getValue();
                m.getR().setList(list);
                try {
                    sortedRule.add(m.getR().clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                sort.put(entry.getKey(), sortedRule);
            }
        }
        return sort;
    }


    // next localMine method
    public void partitionLocalMine() {
        HashMap<Integer, List<Rule>> map = dealWithMessage();
        for (dminePartition dmine : dminePartitions) {
            dmine.setCandidates(map.get(dmine.getPartition()));
        }
//        for (dminePartition dmine : dminePartitions) {
//            allExtendRules.add(dmine.nextMining());
//        }
//
        JavaRDD<dminePartition> dmineTransactions = sc.parallelize(dminePartitions);
        JavaRDD<List<Rule>> extendRules = dmineTransactions
                .flatMap((FlatMapFunction<dminePartition, List<Rule>>) dmine -> {
                    List<Rule> rules = dmine.nextMiningNew();
                    List<List<Rule>> list = new ArrayList<>();
                    list.add(rules);
                    return list.iterator();
                });
        allExtendRules.addAll(extendRules.collect());

        this.extendRules = partitionAutomorphismChecking();

    }



    // automorphism check method
    // option 0 stands for the local while 1 stands for partition
    private List<Rule> automorphismChecking(List<Rule> r1l, List<Rule> r2l) {
        List<Rule> rules = new ArrayList<>();
        rules.addAll(r1l);
        rules.addAll(r2l);
        int bound = r1l.size();
        for (int i = 0; i < bound; i++) {
            for (int j = bound; j < rules.size(); j++) {
                BisimulationChecker checker = new BisimulationChecker(rules.get(i), rules.get(j));
                if (checker.isBisimulation()) {
                    // remove the bigger one
                    if (rules.get(i).getCount() <= rules.get(j).getCount()) {
                        rules.get(i).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.get(i).assemble(rules.get(j));
                        rules.remove(rules.get(j));
                        j--;
                    }else {
                        rules.get(j).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.get(j).assemble(rules.get(i));
                        rules.remove(rules.get(i));
                        i--;
                        bound--;
                        break;
                    }
                }
            }
        }


        return rules;
    }
    private List<Rule> automorphismCheck(List<Rule> rules) {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                BisimulationChecker checker = new BisimulationChecker(rules.get(i), rules.get(j));
                if (checker.isBisimulation()) {
                    // remove the bigger one
                    if (rules.get(i).getCount() <= rules.get(j).getCount()) {
                        rules.get(i).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.get(i).assemble(rules.get(j));
                        rules.remove(rules.get(j));
                        j--;
                    }else {
                        rules.get(j).setIsExtendable(rules.get(i).getIsExtendable() || rules.get(j).getIsExtendable());
                        rules.get(j).assemble(rules.get(i));
                        rules.remove(rules.get(i));
                        i--;
                        break;
                    }
                }
            }
        }

        return rules;
    }

    private List<Integer> temp(HashMap<Integer, List<Integer>> map) {
        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            if (entry.getKey() == 1) {
                result.addAll(entry.getValue());
            }
            if (entry.getKey() == 2) {
                for (int e : entry.getValue()) {
                    e += 3;
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * Revised on 27 May 2020 by Zaiwen
     *
     * **/
    public void partitionFilterRule() {
        // filter through support && trivial GPARs
        for (int i = 0; i < extendRules.size(); i++) {
            if (extendRules.get(i).getRSupport() < support || extendRules.get(i).getQ_qSupport() == 0) {
                extendRules.remove(extendRules.get(i));
                i--;
            }
        }
        curMessages = new ArrayList<>(extendRules.size());

        for (Rule rule : extendRules) {

            System.out.println("GPAR：");
            System.out.println("LHS： ");
            System.out.println(rule.getPattern());
            System.out.println("LHS+RHS：");
            System.out.println(rule.getRule());



            Message message = new Message(rule.getPartition() + "_" + rule.getCenterNodeId() + "_" + rule.getPattern().hashCode());
            message.setRule(rule.getRule());
            message.setR(rule);
            message.setConf((rule.getRSupport() * ((double) _qGSupport)) / (rule.getQ_qSupport() * ((double) qGSupport)));
            curMessages.add(message);
        }

//        /**printing out all the rules**/
//        for (int i = 0; i < curMessages.size(); i++) {
//            Message message = curMessages.get(i);
//
//        }






        System.out.println("After filtering out, there are " + curMessages.size() + " GPARs totally.");
        allMessages.addAll(curMessages);

    }




    public void tempTopK() {
        for (Message message : curMessages) {
            for (Message message1 : allMessages) {
                if (!message.getId().equals(message1.getId())) {
                    MessagePair pair = new MessagePair(message, message1);
                    if (calculateDiv(message, message1) == 0.92) {
                        pair.setDiversification(calculateDiv(message, message1));
                        topK.add(pair);
                    }
                }
            }
        }
        MessagePair pair = null;
        for (MessagePair mPair : topK) {
            if ((mPair.getA().getConf() == 0.8 && mPair.getA().getR().getCount() == 4 && mPair.getB().getConf() == 0.4 && mPair.getB().getR().getCount() == 4)
                    || (mPair.getA().getConf() == 0.4 && mPair.getA().getR().getCount() == 4 && mPair.getB().getConf() == 0.8 && mPair.getB().getR().getCount() == 4)) {
                pair = mPair;
                break;
            }
        }
        topK.clear();
        topK.add(pair);
        curMessages.clear();
        curMessages.add(pair.getA());
        curMessages.add(pair.getB());
        allMessages.clear();
        allMessages.add(pair.getA());
        allMessages.add(pair.getB());
        System.out.println("After diversified filtering，we obtain " + k + " GPARs at present.");
        for (MessagePair messagePair : topK) {
            System.out.println("GPAR：");
            System.out.println("LHS：");
            System.out.println(messagePair.getA().getR().getPattern());
            System.out.println("LHS+RHS：");
            System.out.println(messagePair.getA().getRule());
            System.out.println("GPAR：");
            System.out.println("LHS：");
            System.out.println(messagePair.getA().getR().getPattern());
            System.out.println("LHS+RHS：");
            System.out.println(messagePair.getB().getRule());
        }
    }
    

    private double calculateDiv(Message a, Message b) {
        HashMap<Integer, List<Integer>> aNodes = a.getPartitionCenterNodesId();
        HashMap<Integer, List<Integer>> bNodes = b.getPartitionCenterNodesId();

        double numerator = 0;
        double denominator = 0;
        for (Map.Entry<Integer, List<Integer>> entry : aNodes.entrySet()) {
            List<Integer> al = new ArrayList<>(entry.getValue().size());
            al.addAll(entry.getValue());
            if (bNodes.containsKey(entry.getKey())) {
                List<Integer> bl = new ArrayList<>(bNodes.size());
                bl.addAll(bNodes.get(entry.getKey()));
                numerator += al.stream().filter(bl::contains).collect(Collectors.toList()).size();
                al.addAll(bl);
                denominator += al.stream().distinct().collect(Collectors.toList()).size();
            } else {
                denominator += al.size();
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : bNodes.entrySet()) {
            List<Integer> bl = entry.getValue();
            if (!aNodes.containsKey(entry.getKey())) {
                denominator += bl.size();
            }
        }
        double different = 1 - numerator / denominator;
        double diversification = ((1 - lambda) / (qGSupport * (k - 1))) * (a.getConf() + b.getConf())
                + ((2 * lambda) / (k - 1)) * different ;
        return diversification;
    }



    // set single graph
    private static Graph setSingleGraph() {
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
        int f_r_1_id = singleGraph.addNode(200);
//        int f_r_1_id_1 = singleGraph.addNode(200);
//        int f_r_1_id_2 = singleGraph.addNode(200);
        int f_r_2_id = singleGraph.addNode(200);
//        int f_r_2_id_1 = singleGraph.addNode(200);
//        int f_r_2_id_2 = singleGraph.addNode(200);
        int f_r_3_id = singleGraph.addNode(200);
//        int f_r_3_id_1 = singleGraph.addNode(200);
//        int f_r_3_id_2 = singleGraph.addNode(200);
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
//        myNode f_r_1_1 = new myNode(f_r_1_id_1,200);
//        myNode f_r_1_2 = new myNode(f_r_1_id_2,200);
        myNode f_r_2 = new myNode(f_r_2_id,200);
//        myNode f_r_2_1 = new myNode(f_r_2_id_1,200);
//        myNode f_r_2_2 = new myNode(f_r_2_id_2,200);
        myNode f_r_3 = new myNode(f_r_3_id,200);
//        myNode f_r_3_1 = new myNode(f_r_3_id_1,200);
//        myNode f_r_3_2 = new myNode(f_r_3_id_2,200);
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
//        cust1.addreachableNode(f_r_1_1,30);
//        cust1.addreachableNode(f_r_1_2,30);
        cust1.addreachableNode(newYork,40);
        cust2.addreachableNode(cust1,10);
        cust2.addreachableNode(cust3,10);
        cust2.addreachableNode(f_r_visited_1,20);
        cust2.addreachableNode(f_r_visited_2,20);
        cust2.addreachableNode(f_r_1,30);
//        cust2.addreachableNode(f_r_1_1,30);
//        cust2.addreachableNode(f_r_1_2,30);
        cust2.addreachableNode(f_r_2,30);
//        cust2.addreachableNode(f_r_2_1,30);
//        cust2.addreachableNode(f_r_2_2,30);
        cust2.addreachableNode(newYork,40);
        cust3.addreachableNode(cust2,10);
        cust3.addreachableNode(cust4,10);
        cust3.addreachableNode(f_r_visited_2,20);
        cust3.addreachableNode(f_r_2,30);
//        cust3.addreachableNode(f_r_2_1,30);
//        cust3.addreachableNode(f_r_2_2,30);
        cust3.addreachableNode(newYork,40);
        cust4.addreachableNode(cust3,10);
        cust4.addreachableNode(cust5,10);
        cust4.addreachableNode(f_r_visited_3,20);
        cust4.addreachableNode(f_r_3,30);
//        cust4.addreachableNode(f_r_3_1,30);
//        cust4.addreachableNode(f_r_3_2,30);
        cust4.addreachableNode(a_r_1,30);
        cust4.addreachableNode(lA,40);
        cust5.addreachableNode(cust4,10);
        cust5.addreachableNode(cust6,10);
        cust5.addreachableNode(a_r_1,20);
        cust5.addreachableNode(f_r_3,30);
//        cust5.addreachableNode(f_r_3_1,30);
//        cust5.addreachableNode(f_r_3_2,30);
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
//        f_r_1_1.addreachableNode(newYork,50);
//        f_r_1_2.addreachableNode(newYork,50);
        f_r_2.addreachableNode(newYork,50);
//        f_r_2_1.addreachableNode(newYork,50);
//        f_r_2_2.addreachableNode(newYork,50);
        f_r_3.addreachableNode(lA,50);
//        f_r_3_1.addreachableNode(lA,50);
//        f_r_3_2.addreachableNode(lA,50);
        a_r_2.addreachableNode(lA,50);

        singleGraph.addNode(cust1);
        singleGraph.addNode(cust2);
        singleGraph.addNode(cust3);
        singleGraph.addNode(cust4);
        singleGraph.addNode(cust5);
        singleGraph.addNode(cust6);
        singleGraph.addNode(f_r_1);
//        singleGraph.addNode(f_r_1_1);
//        singleGraph.addNode(f_r_1_2);
        singleGraph.addNode(f_r_2);
//        singleGraph.addNode(f_r_2_1);
//        singleGraph.addNode(f_r_2_2);
        singleGraph.addNode(f_r_3);
//        singleGraph.addNode(f_r_3_1);
//        singleGraph.addNode(f_r_3_2);
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
//        singleGraph.addEdge(cust1_id,f_r_1_id_1,30);
//        singleGraph.addEdge(cust1_id,f_r_1_id_2,30);
        singleGraph.addEdge(cust1_id,newYork_id,40);
        singleGraph.addEdge(cust2_id,cust1_id,10);
        singleGraph.addEdge(cust2_id,cust3_id,10);
        singleGraph.addEdge(cust2_id,f_r_visited_1_id,20);
        singleGraph.addEdge(cust2_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust2_id,f_r_1_id,30);
//        singleGraph.addEdge(cust2_id,f_r_1_id_1,30);
//        singleGraph.addEdge(cust2_id,f_r_1_id_2,30);
        singleGraph.addEdge(cust2_id,f_r_2_id,30);
//        singleGraph.addEdge(cust2_id,f_r_2_id_1,30);
//        singleGraph.addEdge(cust2_id,f_r_2_id_1,30);
        singleGraph.addEdge(cust2_id,newYork_id,40);
        singleGraph.addEdge(cust3_id,cust2_id,10);
        singleGraph.addEdge(cust3_id,cust4_id,10);
        singleGraph.addEdge(cust3_id,f_r_visited_2_id,20);
        singleGraph.addEdge(cust3_id,f_r_2_id,30);
//        singleGraph.addEdge(cust3_id,f_r_2_id_1,30);
//        singleGraph.addEdge(cust3_id,f_r_2_id_2,30);
        singleGraph.addEdge(cust3_id,newYork_id,40);
        singleGraph.addEdge(cust4_id,cust3_id,10);
        singleGraph.addEdge(cust4_id,cust5_id,10);
        singleGraph.addEdge(cust4_id,f_r_visited_3_id,20);
        singleGraph.addEdge(cust4_id,f_r_3_id,30);
//        singleGraph.addEdge(cust4_id,f_r_3_id_1,30);
//        singleGraph.addEdge(cust4_id,f_r_3_id_2,30);
        singleGraph.addEdge(cust4_id,a_r_1_id,30);
        singleGraph.addEdge(cust4_id,lA_id,40);
        singleGraph.addEdge(cust5_id,cust4_id,10);
        singleGraph.addEdge(cust5_id,cust6_id,10);
        singleGraph.addEdge(cust5_id,a_r_1_id,20);
        singleGraph.addEdge(cust5_id,f_r_3_id,30);
//        singleGraph.addEdge(cust5_id,f_r_3_id_1,30);
//        singleGraph.addEdge(cust5_id,f_r_3_id_2,30);
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
//        singleGraph.addEdge(f_r_1_id_1,newYork_id,50);
//        singleGraph.addEdge(f_r_1_id_2,newYork_id,50);
        singleGraph.addEdge(f_r_2_id,newYork_id,50);
//        singleGraph.addEdge(f_r_2_id_1,newYork_id,50);
//        singleGraph.addEdge(f_r_2_id_2,newYork_id,50);
        singleGraph.addEdge(f_r_3_id,lA_id,50);
//        singleGraph.addEdge(f_r_3_id_1,lA_id,50);
//        singleGraph.addEdge(f_r_3_id_2,lA_id,50);
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
//        f_rMap.put(f_r_1_id_1,f_r_1_1);
//        f_rMap.put(f_r_1_id_2,f_r_1_2);
        f_rMap.put(f_r_2_id,f_r_2);
//        f_rMap.put(f_r_2_id_1,f_r_2_1);
//        f_rMap.put(f_r_2_id_2,f_r_2_2);
        f_rMap.put(f_r_3_id,f_r_3);
//        f_rMap.put(f_r_3_id_1,f_r_3_1);
//        f_rMap.put(f_r_3_id_2,f_r_3_2);
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

        return singleGraph;
    }

    public static void example() {
        /**
         * node label representation
         * 100->customer
         * 200->French restaurant
         * 300->Asian restaurant
         * 400->city
         */
        /**
         * edge label representation
         * friend->10
         * visit->20
         * like->30
         * live_in->40
         * in->50
         */
        Graph singleGraph = setSingleGraph();
        Dmine dmine = new Dmine(singleGraph,100,200, 20, 1,2,2, 2,0.5);
//        dmine.init();
//        dmine.filterRule();
//        dmine.updateTopK();
//        dmine.localMine();
//        dmine.filterRule();
//        dmine.updateTopK();
        System.out.println("The first round of mining：");
        dmine.partitionInit();
        dmine.partitionFilterRule();
        dmine.tempTopK();
        System.out.println("The second round of mining：");
        dmine.partitionLocalMine();
        dmine.partitionFilterRule();
    }

    public static void test() {
        Graph single = setSingleGraph();
        HPListGraph queryGraph = new HPListGraph("query");
        int cust=queryGraph.addNodeIndex(100);
        int cust1=queryGraph.addNodeIndex(100);
        int f_r_liked_1=queryGraph.addNodeIndex(200);
        int f_r_liked_2=queryGraph.addNodeIndex(200);
        int f_r_liked_3=queryGraph.addNodeIndex(200);
        int city=queryGraph.addNodeIndex(400);
        int edge = queryGraph.addEdgeIndex(cust,cust1,10,1);
        queryGraph.addEdgeIndex(cust1,cust,10,1);
        queryGraph.addEdgeIndex(cust,f_r_liked_1,30,1);
        queryGraph.addEdgeIndex(cust,f_r_liked_2,30,1);
        queryGraph.addEdgeIndex(cust,f_r_liked_3,30,1);
        queryGraph.addEdgeIndex(cust,city,40,1);

        System.out.println(queryGraph);

        //Query q = new Query((HPListGraph<Integer,Double>)queryGraph);

        //HashMap<Integer, HashSet<Integer>> nonCandidates = new HashMap<>();//create a hashmap to save pruned variables

        //ConstraintGraph cg = new ConstraintGraph(single, q, nonCandidates);
        //DFSSearch search = new DFSSearch(cg, single.getFreqThreshold(), nonCandidates);
        //search.searchAll(4);
        //search.searchExistances(cust);
        //Variable[] result = search.getResultVariables();
        //System.out.println(search.getResultCounter());
        //System.out.println(result);
    }

    public static void main(String[] args){
        example();
        //test();
    }


}
