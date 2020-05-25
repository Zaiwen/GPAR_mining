package Apriori;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Apriori implements Serializable {
    private String url;
    private double support;
    private double confidence;
    private boolean isSelEnd;
    private boolean isAssEnd;
    private int times;
    private static List<List<String>> data=new ArrayList<>();
    private static List<List<String>> frequentSet=new ArrayList<>();
    private static HashMap<FrequentList,Integer> frequentMap=new HashMap<>();
    private static List<AssociationRule> rules=new ArrayList<>();
    private static double support_num;



    // wordCount思路,为reduceByKey作准备
    static class Count implements PairFunction<FrequentList,FrequentList, Integer> {
        @Override
        public Tuple2<FrequentList, Integer> call(FrequentList frequentList) throws Exception {
            return new Tuple2<>(frequentList,1);
        }
    }


    // 检测是否满足最小支持度
    static class isSupport implements Function<Tuple2<FrequentList,Integer>,Boolean>{
        @Override
        public Boolean call(Tuple2<FrequentList, Integer> frequentListIntegerTuple2) throws Exception {
            boolean flag=false;
            if (frequentListIntegerTuple2._2()>=support_num){
                flag=true;
                frequentSet.add(frequentListIntegerTuple2._1.getFrequentItem());
                frequentMap.put(frequentListIntegerTuple2._1,frequentListIntegerTuple2._2);
            }
            return flag;
        }
    }


    public Apriori(double support, double confidence, String url){
        this.support=support;
        this.confidence=confidence;
        this.url="extension/spark_apriori_mining/src/main/resources/"+url;
    }

    /**
     *@discription: 基于spark并行
     *@param
     *@date: 2020/3/1 2:47
     *@return: void
     *@author: Han
     */
    public void SparkRun(){
        SparkConf conf=new SparkConf()
                .setAppName("test")
                .setMaster("local");
        JavaSparkContext sc=new JavaSparkContext(conf);
        // 获取data
        JavaRDD<String> lines=sc.textFile(url);

        // 以list形式得到transactions,后面多次用到,此处进行cache
        JavaRDD<List<String>> transactions=lines.map((Function<String, List<String>>) s -> Arrays.asList(s.split(","))).cache();
        support_num=transactions.count()*support;

        // 获取1-项候选集
        JavaRDD<FrequentList> firstCandidateSet=lines
                .flatMap((FlatMapFunction<String, FrequentList>) s -> {
            List<FrequentList> list=new ArrayList<>();
            for (String singleItem:s.split(",")){
                FrequentList fl=new FrequentList(Collections.singletonList(singleItem));
                list.add(fl);
            }
            return list.iterator();
        });
        System.out.println("进行第1次候选集筛选");
        // 获取1-项频繁集
        JavaRDD<FrequentList> firstFreqSet=firstCandidateSet
                .mapToPair(new Count()).reduceByKey((a, b)->a+b)
                .filter(new isSupport())
                .map(x->x._1);
        List<FrequentList> freqSetList=firstFreqSet.collect();
        output(1,freqSetList);
        // 迭代
        times=2;
        while (0 != freqSetList.size()) {
            List<List<String>> frequentItemSet = obToList(freqSetList);
            // 获取k-项候选集
            List<List<String>> nextCandidateSet = nextCandidateSet(frequentItemSet, times);
            System.out.println("进行第"+times+"次候选集筛选");
            System.out.println(nextCandidateSet);
            // 进行广播
            Broadcast<List<List<String>>> canBroadcast = sc.broadcast(nextCandidateSet);
            JavaRDD<FrequentList> candidateSet = transactions
                    .flatMap(new FlatMapFunction<List<String>, FrequentList>() {
                        List<List<String>> value = canBroadcast.value();
                        @Override
                        public Iterator<FrequentList> call(List<String> list) throws Exception {
                            List<FrequentList> lists = new ArrayList<>();
                            for (List<String> candidateItem : value) {
                                if (list.containsAll(candidateItem)) {
                                    FrequentList frequentList = new FrequentList(candidateItem);
                                    lists.add(frequentList);
                                }
                            }
                            return lists.iterator();
                        }
                    });
            JavaRDD<FrequentList> nextFreqSet = candidateSet
                    .mapToPair(new Count())
                    .reduceByKey((a, b) -> a + b)
                    .filter(new isSupport())
                    .map(x -> x._1);
            freqSetList = nextFreqSet.collect();
            output(times,freqSetList);

            // 生成关联规则
            List<List<String>> freqItemSet=obToList(freqSetList);
            JavaRDD<List<String>> ruleTransactions=sc.parallelize(freqItemSet);
            JavaRDD<AssociationRule> rulesRdd=ruleTransactions
                    .flatMap((FlatMapFunction<List<String>, AssociationRule>) list -> {
                        List<List<String>> allSubSets=allSubSets(list);
                        List<AssociationRule> rules=new ArrayList<>();
                        for (List<String> latter:allSubSets){
                            List<String> former=list.stream().filter(e->!latter.contains(e)).collect(Collectors.toList());
                            double conf1 =(double)frequentMap.get(new FrequentList(list))/(double)frequentMap.get(new FrequentList(former));
                            if (conf1>=confidence){
                                AssociationRule rule=new AssociationRule(former,latter, conf1);
                                rules.add(rule);
                            }
                        }
                        return rules.iterator();
                    });
            rules.addAll(rulesRdd.collect());

            times++;
            if (0 == freqSetList.size()){
                System.out.println("无满足第"+--times+"次,结束");
                System.out.println("所有频繁集为:");
                System.out.println(frequentSet);
            }
        }

        // 输出规则
        outputRules();


    }


    private List<List<String>> obToList(List<FrequentList> list) {
        List<List<String>> frequentItemSet = new ArrayList<>();
        for (FrequentList f : list) {
            frequentItemSet.add(f.getFrequentItem());
        }
        return frequentItemSet;
    }

    private void output(int times,List<FrequentList> list){
        System.out.println("进行第"+times+"次"+"频率集筛选");
        System.out.println(obToList(list));
    }


    /**
     *@discription: 获取一个集合所有子集,除开自身
     *@param set
     *@date: 2020/3/1 18:45
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    public List<List<String>> allSubSets(List<String> set){
        int n=set.size();
        List<List<String>> allSubSets=getSubSet(set,n,n-1);
        allSubSets.remove(0);
        allSubSets.remove(set);
        return allSubSets;
    }

    /**
     *@discription: 递归求子集
     *@param set
	 *@param n
	 *@param cur
     *@date: 2020/3/1 18:46
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    private List<List<String>> getSubSet(List<String> set, int n, int cur){
        List<List<String>> newSet=new ArrayList<>();
        if (cur==0){
            List<String> firstSet=new ArrayList<>();
            List<String> nullSet=new ArrayList<>();
            firstSet.add(set.get(0));
            newSet.add(nullSet);
            newSet.add(firstSet);
            return newSet;
        }

        List<List<String>> oldSet=getSubSet(set,n,cur-1);

        for (List<String> s: oldSet){
            newSet.add(s);
            List<String> clone=new ArrayList<>();
            clone.addAll(s);
            clone.add(set.get(cur));
            newSet.add(clone);
        }
        return newSet;

    }
    /**
     *@discription: 普通运行
     *@param
     *@date: 2020/3/1 2:47
     *@return: void
     *@author: Han
     */
    public void normalRun(){
        // 获取data
        data=getData();
        // 获取1-项候选集
        System.out.println("进行第1次候选集筛选");
        List<List<String>> firstCandidateSet=firstCandidateSet();
        // 获取1-项频繁集
        System.out.println("进行第1次频率集筛选");
        List<List<String>> frequentSet=frequentSet(firstCandidateSet);
        System.out.println(frequentSet);
        addFrequentSet(frequentSet);
        // 迭代
        times=2;
        while (!isSelEnd){
            System.out.println("进行第"+times+"次候选集筛选");
//            List<List<String>> nextCandidateSet=nextCandidateSet(frequentSet);
            List<List<String>> nextCandidateSet=nextCandidateSet(frequentSet,times);
            System.out.println(nextCandidateSet);
            System.out.println("进行第"+times+"次频率集筛选");
            List<List<String>> nextFrequentSet=frequentSet(nextCandidateSet);
            addFrequentSet(nextFrequentSet);
            System.out.println(nextFrequentSet);

            if (isSelEnd){
                System.out.println("无满足第"+times+"次,结束");
                System.out.println("所有频繁集为:");
                System.out.println(this.frequentSet);
            }

            frequentSet=nextFrequentSet;
            times++;
        }

        // 挖掘关联规则
        associationRulesMining();

    }

    /**
     *@discription: 获取数据
     *@param
     *@date: 2020/2/24 16:22
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    private List<List<String>> getData(){
        List<List<String>> data=new ArrayList<>();
        try{
            File file=new File(url);
            if (file.isFile()&&file.exists()){
                InputStreamReader inputStreamReader=new InputStreamReader(new FileInputStream(file),"UTF-8");
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line=null;
                while ((line=bufferedReader.readLine())!=null){
                    String[] item=line.split(",");
                    List<String> itemList=new ArrayList<>();
                    itemList.addAll(Arrays.asList(item));
                    data.add(itemList);
                }
                inputStreamReader.close();
                System.out.println("成功读取data文件");
            }else {
                System.out.println("无法找到data文件");
            }
        }catch (Exception e){
            System.out.println("读取data文件出现错误");
            e.printStackTrace();
        }
        return data;
    }

    /**
     *@discription: 获取1-项候选集
     * 思路将双list合成一个去重后重回双list
     *@param
     *@date: 2020/2/24 16:57
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    private List<List<String>> firstCandidateSet(){
        List<String> unclean=new ArrayList<>();
        for (List<String> l:data) {
            unclean.addAll(l);
        }
        List<String> clean=unclean.stream().distinct().collect(Collectors.toList());
        List<List<String>> firstCandidateSet=new ArrayList<>();
        for (String s: clean){
            List<String> temp = new ArrayList<>();
            temp.add(s);
            firstCandidateSet.add(temp);
        }
        return firstCandidateSet;
    }

    /**
     *@discription: k-项候选集筛选出k-项频繁集
     *@param candidateSet
     *@date: 2020/2/24 17:06
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    private List<List<String>> frequentSet(List<List<String>> candidateSet){
        List<List<String>> frequentSet=new ArrayList<>();
        isSelEnd=true;
        for (List<String> candidateItem:candidateSet){
            int freq=frequenceCount(candidateItem);
            if (freq>=support*(data.size())){
                frequentSet.add(candidateItem);
                isSelEnd=false;
                frequentMap.put(new FrequentList(candidateItem),freq);
            }
        }
        return frequentSet;
    }

    /**
     *@discription: 计算候选集中每个项的支持度计数
     *@param candidateItem
     *@date: 2020/2/24 16:22
     *@return: int
     *@author: Han
     */
    private int frequenceCount(List<String> candidateItem){
        int freq=0;
        for (List<String> list:data) {
            boolean flag=true;
            if (!list.containsAll(candidateItem)){
                flag=false;
            }
            if (flag){
                freq++;
            }
        }
        return freq;
    }
    
    /**
     *@discription: 根据k-1项频繁集求出k项候选集
     * 思路求并集后根据长度先进行一次筛选
     * 筛选后的每一项子集在原频率集中再次筛选
     *
     *
     * 优化思路->利用lattice structure
     * 添加参数k 为迭代次数
     * 取频繁集项中前k-2项相同项进行取并集
     *@param frequentSet, k
     *@date: 2020/2/24 18:13
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    public List<List<String>> nextCandidateSet(List<List<String>> frequentSet, int k){
        List<List<String>> nextCandidateSet=new ArrayList<>();
        List<List<String>> tempCandidateSet=lattice(frequentSet, k);
        for (List<String> temp:tempCandidateSet){
            if (haveSub(temp,frequentSet)){
                List<String> element=new ArrayList<>();
                element.addAll(temp);
                nextCandidateSet.add(element);
            }
        }
        return nextCandidateSet;
    }

    /**
     *@discription: 判断temp的所有k-1项子集是否在frequentSet里
     *@param temp
	 *@param frequentSet
     *@date: 2020/2/24 19:01
     *@return: boolean
     *@author: Han
     */
    private boolean haveSub(List<String> temp, List<List<String>> frequentSet) {
        boolean tag=true;
        List<String> item=new ArrayList<>();
        for (String single:temp) {
            item.addAll(temp);
            item.removeIf(single::equals);
            boolean tempTag1=false;
            for (List<String> frequentItem:frequentSet){
                boolean tempTag2=false;
                if (frequentItem.containsAll(item)){
                    tempTag2=true;
                }
                tempTag1=tempTag1||tempTag2;
            }
            tag=tag&&tempTag1;
            if (!tag) {
                break;
            }
            item.clear();
        }
        return tag;
    }

    /**
     *@discription: 将当前k-项频繁集加入最终所有项频繁集中
     *@param frequentSet 
     *@date: 2020/2/24 17:25
     *@return: void
     *@author: Han
     */
    private void addFrequentSet(List<List<String>> frequentSet){
        this.frequentSet.addAll(frequentSet);
    }

    /**
     *@discription: lattice结构
     *
     * 提取复用
     *@param list
	 *@param k
     *@date: 2020/2/28 23:12
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    public List<List<String>> lattice(List<List<String>> list,int k){
        List<List<String>> nextList=new ArrayList<>();
        for (int i=0;i<list.size();i++){
            for (int j=i+1;j<list.size();j++){
                List<String> temp1=new ArrayList<>();
                List<String> temp2=new ArrayList<>();
                temp1.addAll(list.get(i).subList(0,k-2));
                temp2.addAll(list.get(j).subList(0,k-2));
                if (temp1.containsAll(temp2)&&temp2.containsAll(temp1)) {
                    List<String> temp=new ArrayList<>();
                    temp.addAll(list.get(i));
                    temp.addAll(list.get(j));
                    nextList.add(temp.stream().distinct().collect(Collectors.toList()));
                }
            }
        }
        return nextList;
    }

    /**
     *@discription: 关联规则挖掘
     *
     * 思路->针对所有频繁集，分为频繁集项为2项和2项以上
     * 2项进行一次计算
     * 2项以上通过lattice迭代
     *@param
     *@date: 2020/2/28 21:04
     *@return: void
     *@author: Han
     */
    private void associationRulesMining(){
        for (List<String> frequentItem:frequentSet){
            if (frequentItem.size()>1){
                List<List<String>> curLatter=new ArrayList<>();
                for (String element:frequentItem){
                    List<String> oneLatter=new ArrayList<>();
                    oneLatter.add(element);
                    curLatter.add(oneLatter);
                    calculateConf(frequentItem,oneLatter);
                }
                if (frequentItem.size()>2){
                    int times=2;
                    while (!isAssEnd){
                        curLatter=nextLatter(frequentItem,curLatter,times);
                        times++;
                    }
                }
            }
        }
        if (isAssEnd) {
            outputRules();
        }
    }

    private void outputRules(){
        System.out.println("挖掘关联规则,其中最小SUPPORT为:"+support+",最小CONFIDENCE为:"+confidence);
        for (AssociationRule rule:rules){
            System.out.println(rule.getFormerPart()+"====>"+rule.getLatterPart()+"  confidence="+rule.getConfidence());
        }
    }

    /**
     *@discription: 通过lattice结构推出下一个关联后者项
     *@param item
	 *@param curLatter
	 *@param k
     *@date: 2020/2/28 23:17
     *@return: java.util.List<java.util.List<java.lang.String>>
     *@author: Han
     */
    private List<List<String>> nextLatter(List<String> item,List<List<String>> curLatter,int k) {
        List<List<String>> tempLatter=lattice(curLatter,k);
        List<List<String>> nextLatter=new ArrayList<>();
        for (List<String> latter:tempLatter){
            if (calculateConf(item,latter)){
                nextLatter.add(latter);
            }
        }
        return nextLatter;
    }

    /**
     *@discription: 计算confidence
     *@param item
	 *@param latter
     *@date: 2020/2/28 23:19
     *@return: boolean
     *@author: Han
     */
    private boolean calculateConf(List<String> item,List<String> latter){
        List<String> former=item.stream().filter(e->!latter.contains(e)).collect(Collectors.toList());
        double conf=(double)frequentMap.get(new FrequentList(item))/(double)frequentMap.get(new FrequentList(former));
        isAssEnd=true;
        if (conf>=confidence){
            AssociationRule rule=new AssociationRule(former,latter,conf);
            rules.add(rule);
            isAssEnd=false;
            return true;
        }
        return false;
    }

}
