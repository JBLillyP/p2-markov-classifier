import java.util.*;
import java.util.regex.*;
import java.io.*;

public class ClassifyingModel extends BaseMarkovModel {

    // context -> list of following words
    private Map<List<String>, List<String>> myMap;
    // set of all unique tokens
    private Set<String> myVocab;
    // cache for memoization: context -> (token -> count)
    private Map<List<String>, Map<String, Integer>> myCache;

    public ClassifyingModel(int size) {
        super(size);
        myMap = new HashMap<>();
        myVocab = new HashSet<>();
        myCache = new HashMap<>();
    }

    public ClassifyingModel() {
        this(2);
    }

    /** Tokenize using required regex */
    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Pattern p = Pattern.compile("[A-Za-z]+|[.,!?;:]");
        Matcher m = p.matcher(text.toLowerCase());
        while (m.find()) tokens.add(m.group());
        return tokens;
    }

    /** Count how many times token follows context, with memoization */
    private int tokenInContextCount(List<String> context, String token) {
        Map<String,Integer> inner = myCache.get(context);
        if (inner != null && inner.containsKey(token)) {
            return inner.get(token);
        }

        int count = 0;
        List<String> follows = myMap.get(context);
        if (follows != null) {
            for (String s : follows) {
                if (s.equals(token)) count++;
            }
        }

        // store result in cache
        myCache.computeIfAbsent(new ArrayList<>(context), k -> new HashMap<>())
               .put(token, count);
        return count;
    }

    /** Train model: build myMap and vocab set */
    @Override
    public void processTraining() {
        myVocab.clear();
        for (int i = 0; i < myWordSequence.size() - myModelSize; i++) {
            List<String> context = myWordSequence.subList(i, i + myModelSize);
            String next = myWordSequence.get(i + myModelSize);

            // record follower
            myMap.computeIfAbsent(new ArrayList<>(context), k -> new ArrayList<>()).add(next);
            myVocab.add(next);
        }
    }

    /** Return size of vocabulary */
    public int vocabularySize() {
        return myVocab.size();
    }

    /**
     * Return normalized log-likelihood of text given this trained model.
     * Uses Laplace smoothing (smoother) and normalization by #unique contexts.
     */
    public double calculateMatchProbability(String text, double smoother) {
        List<String> padded = createTokenizedText(text);
        if (padded.size() <= myModelSize) return Double.NEGATIVE_INFINITY;

        double logSum = 0.0;
        Set<String> uniqueContexts = new HashSet<>();

        for (int k = 0; k < padded.size() - myModelSize; k++) {
            List<String> context = padded.subList(k, k + myModelSize);
            String next = padded.get(k + myModelSize);

            List<String> key = new ArrayList<>(context);
            uniqueContexts.add(String.join("|", key));

            List<String> follows = myMap.get(key);
            double contextCount = (follows == null) ? 0.0 : follows.size();
            double nextCount = tokenInContextCount(key, next);

            double prob = (nextCount + smoother) /
                          (contextCount + smoother * myVocab.size());
            if (prob <= 0) prob = 1.0 / myVocab.size();

            logSum += Math.log(prob);
        }

        if (uniqueContexts.isEmpty()) return Double.NEGATIVE_INFINITY;
        // normalize by number of unique contexts in unknown text
        return logSum / uniqueContexts.size();
    }

    /** Local test */
    public static void main(String[] args) throws IOException {
        ClassifyingModel cm = new ClassifyingModel(1);
        String dir = "data/shakespeare";
        cm.trainDirectory(dir);
        System.out.printf("trained %s: vocab=%d, tokens=%d%n",
                dir, cm.vocabularySize(), cm.tokenSize());
    }
}
