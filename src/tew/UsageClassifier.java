package tew;

import cc.mallet.classify.Classifier;
import cc.mallet.types.Labeling;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class UsageClassifier {

    double viaje;
    double trabajo;
    double comida;
    double evento;
    double relajo;
    double neutral;
    Classifier classifier;
    
    public UsageClassifier(File serializedFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        Classifier classifier;

        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (serializedFile));
        classifier = (Classifier) ois.readObject();
        ois.close();
        
        this.classifier = classifier;
    }
    
    public void getLabelings(String text) throws IOException {

        Labeling labeling = this.classifier.classify(text).getLabeling();        
       
        for (int rank = 0; rank < labeling.numLocations(); rank++){
            if (labeling.getLabelAtRank(rank).toString().equals("VIAJE")) this.viaje = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("RELAJO")) this.relajo = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("COMIDA")) this.comida = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("TRABAJO")) this.trabajo = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("EVENTO")) this.evento = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("NEUTRAL")) this.neutral = labeling.getValueAtRank(rank);
        }

    }
    
}
