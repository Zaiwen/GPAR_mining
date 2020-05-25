package Dmine;

import scala.Serializable;

import java.util.Arrays;

public class myEdge implements Serializable {
    private int sourceIndex;
    private double label;
    private int targetLabel;
    private boolean isIncludedInPattern;
    private int targetIndex;
    private boolean isPointedToTarget;
    private boolean reversable;
    private int sourceLabel;

    public myEdge(int sourceIndex, double label, int targetLabel, int sourceLabel) {
        this.sourceIndex = sourceIndex;
        this.label = label;
        this.targetLabel = targetLabel;
        this.isPointedToTarget = false;
        this.reversable = false;
        this.sourceLabel = sourceLabel;
    }

    public void setIncludedInPattern(boolean includedInPattern) {
        isIncludedInPattern = includedInPattern;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public void setPointedToTarget(boolean flag) {
        this.isPointedToTarget = flag;
    }

    public void setReversable(boolean reversable) {
        this.reversable = reversable;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public int getTargetLabel() {
        return targetLabel;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public boolean isIncludedInPattern() {
        return isIncludedInPattern;
    }

    public boolean isPointedToTarget() {
        return isPointedToTarget;
    }

    public boolean isReversable() {
        return reversable;
    }

    public int getSourceLabel() {
        return sourceLabel;
    }

    public double getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        myEdge edge = (myEdge) obj;
        return this.sourceIndex == edge.sourceIndex &&
                this.label == edge.label && this.targetLabel == edge.targetLabel
                && this.isIncludedInPattern == edge.isIncludedInPattern
                && this.isPointedToTarget == edge.isPointedToTarget;
    }
}
