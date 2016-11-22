package tew;

import cc.mallet.classify.Classifier;
import cc.mallet.types.Labeling;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SentClassifier {
    
    double positivo;
    double neutral;
    double negativo;
    Classifier classifier;

    public SentClassifier(File serializedFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        Classifier classifier;

        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (serializedFile));
        classifier = (Classifier) ois.readObject();
        ois.close();
        
        this.classifier = classifier;
    }
    
    public void getLabelings(String text) throws IOException {

        Labeling labeling = this.classifier.classify(text).getLabeling();        
       
        for (int rank = 0; rank < labeling.numLocations(); rank++){
            if (labeling.getLabelAtRank(rank).toString().equals("POSITIVO")) this.positivo = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("NEGATIVO")) this.negativo = labeling.getValueAtRank(rank);
            if (labeling.getLabelAtRank(rank).toString().equals("NEUTRAL")) this.neutral = labeling.getValueAtRank(rank);
        }

    }

}
