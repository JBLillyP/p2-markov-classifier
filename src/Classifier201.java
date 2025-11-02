import java.util.*;
import java.io.*;
import java.util.stream.*;
import java.nio.file.*;

import compsci201.Ignore;

/**
 * Use ClassifyingModel instances to be trained on several
 * authors, then to attempt to identify "unknown" files
 * by calculating maximal likelihood estimation for
 * each unknown file.
 * 
 * @author Owen Astrachan
 * @version 1.0, for Compsci 201, Fall 2025, September 25
 */


public class Classifier201 {

    private Map<String, ClassifyingModel> myModels;  // dirname->model for dir
    private int mySize;                              // order of each model (same)
    private final double SMOOTHING = 0.1;

    public Classifier201(){
        this(3);
    }
    public Classifier201(int size){
        myModels = new HashMap<>();
        mySize = size;
    }

    /** 
     * @return the last /name in a /foo/bar/baz/name file/folder    
     */
    private String getDirName(String fullPath){
        int index = fullPath.lastIndexOf(File.separator);
        return fullPath.substring(index+1);
    }

    /**
     * Folder specified by dirName is a collection of folders, each representing
     * an author's works. Train a model for each folder/author, make models
     * accessible via other methods (modifies internal state).
     * @param dirName is the name of the folder containing author folders
     * @throws IOException if folders cannot be open/read
     */

    @Ignore
    public void trainAllAuthors(String dirName) throws IOException {
        Path root = Paths.get(dirName)
;        try (Stream<Path> paths = Files.walk(root)){
            List<Path> dirs = paths.filter(Files::isDirectory)
                                   .filter(path -> ! path.equals(root))
                                   .collect(Collectors.toList());

            System.out.printf("%s has %d subdirs\n",dirName,dirs.size());
            for(Path each : dirs) {
                String trainDir = each.toAbsolutePath().toString();
                String dName = getDirName(trainDir);
                System.out.printf("training %14s\t",dName);
                               
                ClassifyingModel model = new ClassifyingModel(mySize);
                model.trainDirectory(trainDir);
                myModels.put(dName,model);
                System.out.printf("order %d with %d unique tokens, %d tokens\n",
                                  model.getOrder(),
                                  model.vocabularySize(),
                                  model.tokenSize());
            }
        }      
    }

    public void findBestMatch(Path path) throws IOException{

        String text = Files.readString(path);
        String fileName = path.getFileName().toString();
        Map<String,Double> record = new HashMap<>();

        for(String modelName: myModels.keySet()) {
            double start = System.nanoTime();
            double val = myModels.get(modelName)
                                 .calculateMatchProbability(text, SMOOTHING);
            double end = System.nanoTime();
            double time = (end-start)/1e9;
            System.out.printf("time: %1.2f for %s\n",time,modelName);
            record.put(modelName,val);
        }

        // sort map entries by log likelihood, with largest first, smallest last
        ArrayList<Map.Entry<String,Double>> all = new ArrayList<>(record.entrySet());
        Collections.sort(all, Map.Entry.comparingByValue(Comparator.reverseOrder()));
        double max = all.get(0).getValue();
        String best = all.get(0).getKey();

        System.out.printf("*** %1.2f\t%s for %s\n",max,best,fileName);

        for(int k=0; k < all.size(); k++){
            System.out.printf("%1.2f\t%s\n",all.get(k).getValue(),all.get(k).getKey());
        }
    }

    /**
     * For each file in the folder with dirName, test against all models
     * and print information about which model is best (and what scores are)
     * by the call to findBestMatch
     * @param dirName is name of folder containing files to be identified
     * @throws IOException if reading files/folder fails
     */

    public void identifyAll(String dirName) throws IOException{
         try (Stream<Path> paths = Files.walk(Paths.get(dirName))){
            List<Path> files = paths.filter(Files::isRegularFile)
                                    .collect(Collectors.toList());

            for(Path each: files){
                findBestMatch(each);
            }
         }
    }

    /**
     * Train authors in first parameter/folder, identify works in second parameter/folder
     * @param trainingFolder contains folders, each for an author (train a model on each)
     * @param identifyFolder is a folder of unknown works, compare against all models
     * @throws IOException if reading folders/files fails
     */

    public void traindAndIdentify(String trainingFolder, String identifyFolder) throws IOException {
        trainAllAuthors(trainingFolder);
        identifyAll(identifyFolder);
    }

    public static void main(String[] args) throws IOException {
        Classifier201 classifier = new Classifier201(1); 
        String training = "data";
        String identify = "identify";
        classifier.traindAndIdentify(training,identify);
       
    }
}
