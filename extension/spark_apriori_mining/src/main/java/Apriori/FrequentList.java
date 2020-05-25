package Apriori;

import scala.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *@discription:
 * 自定义类 frequentList
 * 重写equals和hashCode方法来完成HashMap的key读取
 *@param
 *@date: 2020/2/28 20:39
 *@return:
 *@author: Han
 */
public class FrequentList implements Serializable {
    private List<String> frequentItem=new ArrayList<>();

    public FrequentList(List<String> frequentItem) {
        this.frequentItem.addAll(frequentItem);
        this.frequentItem=this.frequentItem.stream().sorted().collect(Collectors.toList());
    }

    public List<String> getFrequentItem() {
        return frequentItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        if (o==null||getClass()!=o.getClass()) return false;

        FrequentList frequentList=(FrequentList) o;
        if (frequentItem!=null?
                (!(frequentItem.containsAll(frequentList.frequentItem)&&frequentList.frequentItem.containsAll(frequentItem)))
                :frequentList.frequentItem!=null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return frequentItem!=null? frequentItem.hashCode():0;
    }
}
