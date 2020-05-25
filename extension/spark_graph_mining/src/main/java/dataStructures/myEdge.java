package dataStructures;

import edu.isi.karma.rep.alignment.Label;

import org.jgrapht.graph.DefaultEdge;

public class myEdge extends DefaultEdge {
    private Label label;

    public myEdge(Label label){
        this.label=label;
    }

    public Label getLabel() {
        return label;
    }
}
