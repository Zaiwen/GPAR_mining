package Apriori;

import scala.Serializable;

import java.util.ArrayList;
import java.util.List;

public class AssociationRule implements Serializable {
    private List<String> formerPart=new ArrayList<>();
    private List<String> latterPart=new ArrayList<>();
    private double confidence;

    public AssociationRule(List<String> former,List<String> latter, double confidence){
        formerPart=former;
        latterPart=latter;
        this.confidence=confidence;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getFormerPart() {
        return formerPart;
    }

    public List<String> getLatterPart() {
        return latterPart;
    }

}
