package Dmine;

import dataStructures.Graph;
import dataStructures.myNode;
import search.Searcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Partition {
    private int num;
    private Graph singleGraph;
    private String url = "extension/grami_directed_subgraph/src/main/resources/";

    public Partition(Graph singleGraph, int num) {
        this.singleGraph = singleGraph;
        this.num = num;
    }

    private List<Graph> input() {
        List<Graph> list = new ArrayList<>();
        String path1 = url + "fragment1.lg";
        String path2 = url + "fragment2.lg";
        Graph f1 = new Graph(100, 1);
        Graph f2 = new Graph(100, 1);
//        try {
//            Searcher s1 = new Searcher(path1, Integer.MAX_VALUE, 1);
//            Searcher s2 = new Searcher(path2, Integer.MAX_VALUE, 1);
//            s1.initialize();
//            s2.initialize();
//
//            f1 = s1.getSingleGraph();
//            f2 = s2.getSingleGraph();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            f1.loadFromFile_Ehab(path1);
            f2.loadFromFile_Ehab(path2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        f1.setShortestPaths_1hop();
        f2.setShortestPaths_1hop();


        list.add(f1);
        list.add(f2);
        return list;
    }

    private List<Graph> myExample() {


        Graph part1 = new Graph(100, Integer.MAX_VALUE);
        int cust1_id = part1.addNode(100);
        int cust2_id = part1.addNode(100);
        int cust3_id = part1.addNode(100);
        int f_r_1_id = part1.addNode(200);
//        int f_r_1_id_1 = part1.addNode(200);
//        int f_r_1_id_2 = part1.addNode(200);
        int f_r_2_id = part1.addNode(200);
//        int f_r_2_id_1 = part1.addNode(200);
//        int f_r_2_id_2 = part1.addNode(200);
        int f_r_visited_1_id = part1.addNode(200);
        int f_r_visited_2_id = part1.addNode(200);
        int newYork_id = part1.addNode(400);

        myNode cust1 = new myNode(cust1_id,100);
        myNode cust2 = new myNode(cust2_id,100);
        myNode cust3 = new myNode(cust3_id,100);
        myNode f_r_1 = new myNode(f_r_1_id,200);
//        myNode f_r_1_1 = new myNode(f_r_1_id_1,200);
//        myNode f_r_1_2 = new myNode(f_r_1_id_2,200);
        myNode f_r_2 = new myNode(f_r_2_id,200);
//        myNode f_r_2_1 = new myNode(f_r_2_id_1,200);
//        myNode f_r_2_2 = new myNode(f_r_2_id_2,200);
        myNode f_r_visited_1 = new myNode(f_r_visited_1_id,200);
        myNode f_r_visited_2 = new myNode(f_r_visited_2_id,200);
        myNode newYork = new myNode(newYork_id,400);
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
        cust3.addreachableNode(f_r_visited_2,20);
        cust3.addreachableNode(f_r_2,30);
//        cust3.addreachableNode(f_r_2_1,30);
//        cust3.addreachableNode(f_r_2_2,30);
        cust3.addreachableNode(newYork,40);
        f_r_visited_1.addreachableNode(newYork,50);
        f_r_visited_2.addreachableNode(newYork,50);
        f_r_1.addreachableNode(newYork, 50);
//        f_r_1_1.addreachableNode(newYork, 50);
//        f_r_1_2.addreachableNode(newYork, 50);
        f_r_2.addreachableNode(newYork,50);
//        f_r_2_1.addreachableNode(newYork,50);
//        f_r_2_2.addreachableNode(newYork,50);



        part1.addNode(cust1);
        part1.addNode(cust2);
        part1.addNode(cust3);
        part1.addNode(f_r_1);
//        part1.addNode(f_r_1_1);
//        part1.addNode(f_r_1_2);
        part1.addNode(f_r_2);
//        part1.addNode(f_r_2_1);
//        part1.addNode(f_r_2_2);
        part1.addNode(f_r_visited_1);
        part1.addNode(f_r_visited_2);
        part1.addNode(newYork);
        part1.addEdge(cust1_id,cust2_id,10);
        part1.addEdge(cust1_id,f_r_visited_1_id,20);
        part1.addEdge(cust1_id,f_r_1_id,30);
//        part1.addEdge(cust1_id,f_r_1_id_1,30);
//        part1.addEdge(cust1_id,f_r_1_id_2,30);
        part1.addEdge(cust1_id,newYork_id,40);
        part1.addEdge(cust2_id,cust1_id,10);
        part1.addEdge(cust2_id,cust3_id,10);
        part1.addEdge(cust2_id,f_r_visited_1_id,20);
        part1.addEdge(cust2_id,f_r_visited_2_id,20);
        part1.addEdge(cust2_id,f_r_1_id,30);
//        part1.addEdge(cust2_id,f_r_1_id_1,30);
//        part1.addEdge(cust2_id,f_r_1_id_2,30);
        part1.addEdge(cust2_id,f_r_2_id,30);
//        part1.addEdge(cust2_id,f_r_2_id_1,30);
//        part1.addEdge(cust2_id,f_r_2_id_1,30);
        part1.addEdge(cust2_id,newYork_id,40);
        part1.addEdge(cust3_id,cust2_id,10);
        part1.addEdge(cust3_id,f_r_visited_2_id,20);
        part1.addEdge(cust3_id,f_r_2_id,30);
//        part1.addEdge(cust3_id,f_r_2_id_1,30);
//        part1.addEdge(cust3_id,f_r_2_id_2,30);
        part1.addEdge(cust3_id,newYork_id,40);
        part1.addEdge(f_r_visited_1_id,newYork_id,50);
        part1.addEdge(f_r_visited_2_id,newYork_id,50);
        part1.addEdge(f_r_1_id,newYork_id,50);
//        part1.addEdge(f_r_1_id_1,newYork_id,50);
//        part1.addEdge(f_r_1_id_2,newYork_id,50);
        part1.addEdge(f_r_2_id,newYork_id,50);
//        part1.addEdge(f_r_2_id_1,newYork_id,50);
//        part1.addEdge(f_r_2_id_2,newYork_id,50);

        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel = new HashMap<>();
        HashMap<Integer, myNode> custMap=new HashMap<>();
        custMap.put(cust1_id,cust1);
        custMap.put(cust2_id,cust2);
        custMap.put(cust3_id,cust3);
        freqNodesByLabel.put(100,custMap);

        HashMap<Integer, myNode> f_rMap=new HashMap<>();
        f_rMap.put(f_r_visited_1_id,f_r_visited_1);
        f_rMap.put(f_r_visited_2_id,f_r_visited_2);
        f_rMap.put(f_r_1_id,f_r_1);
//        f_rMap.put(f_r_1_id_1,f_r_1_1);
//        f_rMap.put(f_r_1_id_2,f_r_1_2);
        f_rMap.put(f_r_2_id,f_r_2);
//        f_rMap.put(f_r_2_id_1,f_r_2_1);
//        f_rMap.put(f_r_2_id_2,f_r_2_2);
        freqNodesByLabel.put(200,f_rMap);

        HashMap<Integer, myNode> cityMap=new HashMap<>();
        cityMap.put(newYork_id,newYork);
        freqNodesByLabel.put(400,cityMap);

        part1.setFreqNodesByLabel(freqNodesByLabel);// finish part1


        Graph part2 = new Graph(100, Integer.MAX_VALUE);

        int cust4_id = part2.addNode(100);
        int cust5_id = part2.addNode(100);
        int cust6_id = part2.addNode(100);
        int f_r_3_id = part2.addNode(200);
//        int f_r_3_id_1 = part2.addNode(200);
//        int f_r_3_id_2 = part2.addNode(200);
        int f_r_visited_3_id = part2.addNode(200);
        int f_r_visited_4_id = part2.addNode(200);
        int a_r_1_id = part2.addNode(300);
        int a_r_2_id = part2.addNode(300);
        int lA_id = part2.addNode(400);

        myNode cust4 = new myNode(cust4_id,100);
        myNode cust5 = new myNode(cust5_id,100);
        myNode cust6 = new myNode(cust6_id,100);
        myNode f_r_3 = new myNode(f_r_3_id,200);
//        myNode f_r_3_1 = new myNode(f_r_3_id_1,200);
//        myNode f_r_3_2 = new myNode(f_r_3_id_2,200);
        myNode f_r_visited_3 = new myNode(f_r_visited_3_id,200);
        myNode f_r_visited_4 = new myNode(f_r_visited_4_id,200);
        myNode a_r_1 = new myNode(a_r_1_id,300);
        myNode a_r_2 = new myNode(a_r_2_id,300);
        myNode lA = new myNode(lA_id,400);

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
        f_r_visited_3.addreachableNode(lA,50);
        f_r_visited_4.addreachableNode(lA,50);
        a_r_1.addreachableNode(lA,50);
        f_r_3.addreachableNode(lA,50);
//        f_r_3_1.addreachableNode(lA,50);
//        f_r_3_2.addreachableNode(lA,50);
        a_r_2.addreachableNode(lA,50);

        part2.addNode(cust4);
        part2.addNode(cust5);
        part2.addNode(cust6);
        part2.addNode(f_r_3);
//        part2.addNode(f_r_3_1);
//        part2.addNode(f_r_3_2);
        part2.addNode(f_r_visited_3);
        part2.addNode(f_r_visited_4);
        part2.addNode(a_r_1);
        part2.addNode(a_r_2);
        part2.addNode(lA);

        part2.addEdge(cust4_id,cust5_id,10);
        part2.addEdge(cust4_id,f_r_visited_3_id,20);
        part2.addEdge(cust4_id,f_r_3_id,30);
//        part2.addEdge(cust4_id,f_r_3_id_1,30);
//        part2.addEdge(cust4_id,f_r_3_id_2,30);
        part2.addEdge(cust4_id,a_r_1_id,30);
        part2.addEdge(cust4_id,lA_id,40);
        part2.addEdge(cust5_id,cust4_id,10);
        part2.addEdge(cust5_id,cust6_id,10);
        part2.addEdge(cust5_id,a_r_1_id,20);
        part2.addEdge(cust5_id,f_r_3_id,30);
//        part2.addEdge(cust5_id,f_r_3_id_1,30);
//        part2.addEdge(cust5_id,f_r_3_id_2,30);
        part2.addEdge(cust5_id,a_r_2_id,30);
        part2.addEdge(cust5_id,lA_id,40);
        part2.addEdge(cust6_id,cust5_id,10);
        part2.addEdge(cust6_id,f_r_visited_4_id,20);
        part2.addEdge(cust6_id,a_r_2_id,30);
        part2.addEdge(cust6_id,lA_id,40);
        part2.addEdge(f_r_visited_3_id,lA_id,50);
        part2.addEdge(f_r_visited_4_id,lA_id,50);
        part2.addEdge(a_r_1_id,lA_id,50);
        part2.addEdge(f_r_3_id,lA_id,50);
//        part2.addEdge(f_r_3_id_1,lA_id,50);
//        part2.addEdge(f_r_3_id_2,lA_id,50);
        part2.addEdge(a_r_2_id,lA_id,50);

        HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel2 = new HashMap<>();
        HashMap<Integer, myNode> custMap2=new HashMap<>();
        custMap2.put(cust4_id,cust4);
        custMap2.put(cust5_id,cust5);
        custMap2.put(cust6_id,cust6);
        freqNodesByLabel2.put(100,custMap2);

        HashMap<Integer, myNode> f_rMap2=new HashMap<>();
        f_rMap2.put(f_r_visited_3_id,f_r_visited_3);
        f_rMap2.put(f_r_visited_4_id,f_r_visited_4);
        f_rMap2.put(f_r_3_id,f_r_3);
//        f_rMap2.put(f_r_3_id_1,f_r_3_1);
//        f_rMap2.put(f_r_3_id_2,f_r_3_2);
        freqNodesByLabel2.put(200,f_rMap2);

        HashMap<Integer, myNode> cityMap2=new HashMap<>();
        cityMap2.put(lA_id,lA);
        freqNodesByLabel2.put(400,cityMap2);

        HashMap<Integer, myNode> a_rMap=new HashMap<>();
        a_rMap.put(a_r_1_id, a_r_1);
        a_rMap.put(a_r_2_id, a_r_2);
        freqNodesByLabel2.put(300,a_rMap);

        part2.setFreqNodesByLabel(freqNodesByLabel2);// finish part2

        List<Graph> example = new ArrayList<>(2);
        example.add(part1);
        example.add(part2);

        return example;
    }

    // temporary method
    public List<Graph> separate() {
//        return input();
        return myExample();
    }
}
