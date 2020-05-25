package Apriori;


public class Main {
    public static void main(String[] args){
        Apriori apriori=new Apriori(0.02,0.7,"data");
        apriori.SparkRun();
        //apriori.normalRun();

    }
}
