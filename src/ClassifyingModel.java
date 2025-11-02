import java.util.*;
import java.util.regex.*;
import java.io.*;
import compsci201.Ignore;

public class ClassifyingModel extends BaseMarkovModel {

    // map context → list of words that followed that context
    private Map<List<String>, List<String>> myMap;
    // store all unique tokens (for vocabulary size)
    private Set<String> myVocab;
    // optional: store memoized counts for faster lookups
    private Map<List<String>, Map<String, Integer>> myMemo;

    public ClassifyingModel(int size) {
        super(size);
        myMap = new HashMap<>();
        myVocab = new HashSet<>();
        myMemo = new HashMap<>();
    }

    public ClassifyingModel() {
        this(2);
    }

    /**
     * Return number of times token follows context in trained model
     */
    private int tokenInContextCount(List<String> context, String token) {
    // disable memoization for this run
    /*
    if (myMemo.containsKey(context) && myMemo.get(context).containsKey(token)) {
        return myMemo.get(context).get(token);
    }
    */

    int count = 0;
    List<String> follows = myMap.get(context);
    if (follows != null) {
        for (String s : follows) {
            if (s.equals(token)) count++;
        }
    }

    /*
    myMemo.putIfAbsent(context, new HashMap<>());
    myMemo.get(context).put(token, count);
    */
    return count;
}


    /**
     * Tokenize with regex: words and punctuation
     */
    @Ignore
    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        String includePunc = "[A-Za-z]+|[.,!?;:]";
        Pattern pattern = Pattern.compile(includePunc);
        Matcher matcher = pattern.matcher(text.toLowerCase());

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    /**
     * Tokenize + pad with <START>/<END> tags (returns list, doesn’t modify state)
     */
    private List<String> createTokenizedText(String text) {
        List<String> padded = new ArrayList<>();
        List<String> tokens = tokenize(text);

        for (int k = 0; k < myModelSize; k++) {
            padded.add("<START>");
        }
        padded.addAll(tokens);
        for (int k = 0; k < myModelSize; k++) {
            padded.add(END);
        }
        return padded;
    }

    /**
     * Return log likelihood of text given this trained model, with Laplace smoothing.
     */
    public double calculateMatchProbability(String text, double smoother) {
        List<String> padded = createTokenizedText(text);

        double logSum = 0.0;
        int pairCount = 0;

        for (int k = 0; k < padded.size() - myModelSize; k++) {
            List<String> context = List.copyOf(padded.subList(k, k + myModelSize));
            String nextWord = padded.get(k + myModelSize);

            List<String> follows = myMap.get(context);

            double countContextNext = tokenInContextCount(context, nextWord);
            double countContext = (follows == null) ? 0 : follows.size();

            // Laplace smoothing:
            double prob = (countContextNext + smoother) /
                          (countContext + smoother * myVocab.size());

            // avoid log(0)
            if (prob <= 0) prob = 1.0 / myVocab.size();

            logSum += Math.log(prob);
            pairCount++;
        }

        // normalize log probability per token so texts of different length are comparable
        return logSum / pairCount;
    }

    /**
     * Train model: fill myMap and myVocab
     */
    @Override
    public void processTraining() {
        for (int k = 0; k < myWordSequence.size() - myModelSize; k++) {
            List<String> context = List.copyOf(myWordSequence.subList(k, k + myModelSize));
            String next = myWordSequence.get(k + myModelSize);

            myMap.putIfAbsent(context, new ArrayList<>());
            myMap.get(context).add(next);

            // track vocabulary
            myVocab.add(next);
        }
    }

    /**
     * Return number of unique words seen in training.
     */
    public int vocabularySize() {
        return myVocab.size();
    }

    public static void main(String[] args) throws IOException {
        ClassifyingModel mm = new ClassifyingModel(3);
        String dirName = "data/shakespeare";
        mm.trainDirectory(dirName);
        System.out.printf("trained model for %s, vocab size = %d, tokens = %d\n",
                dirName, mm.vocabularySize(), mm.tokenSize());
    }
}
