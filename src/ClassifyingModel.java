import java.util.*;
import java.util.regex.*;
import java.io.*;

public class ClassifyingModel extends BaseMarkovModel {

    // map "context string" → list of following tokens
    private Map<String, List<String>> myMap;
    // vocabulary of all unique tokens
    private Set<String> myVocab;
    // memoized counts for token-in-context
    private Map<String, Map<String, Integer>> myMemo;

    public ClassifyingModel(int size) {
        super(size);
        myMap = new HashMap<>();
        myVocab = new HashSet<>();
        myMemo = new HashMap<>();
    }

    public ClassifyingModel() {
        this(2);
    }

    /** Converts list of words into a stable string key */
    private String makeKey(List<String> context) {
        return String.join("|", context);
    }

    /** Return number of times token follows context in trained model */
    private int tokenInContextCount(List<String> context, String token) {
        String key = makeKey(context);

        if (myMemo.containsKey(key) && myMemo.get(key).containsKey(token)) {
            return myMemo.get(key).get(token);
        }

        int count = 0;
        List<String> follows = myMap.get(key);
        if (follows != null) {
            for (String s : follows) {
                if (s.equals(token)) count++;
            }
        }

        myMemo.putIfAbsent(key, new HashMap<>());
        myMemo.get(key).put(token, count);
        return count;
    }

    /** Tokenize with regex: words and punctuation */
    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        // includes all alphabetic sequences and punctuation marks
        String includePunc = "\\p{L}+|[.,!?;:]";
        Pattern pattern = Pattern.compile(includePunc);
        Matcher matcher = pattern.matcher(text.toLowerCase());

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    /** Tokenize + pad with <START>/<END> tags (returns list, doesn’t modify state) */
    private List<String> createTokenizedText(String text) {
        List<String> padded = new ArrayList<>();
        List<String> tokens = tokenize(text);

        for (int k = 0; k < myModelSize; k++) padded.add("<START>");
        padded.addAll(tokens);
        for (int k = 0; k < myModelSize; k++) padded.add(END);

        return padded;
    }

    /** Return log likelihood of text given this trained model, with Laplace smoothing. */
    public double calculateMatchProbability(String text, double smoother) {
        List<String> padded = createTokenizedText(text);
        if (padded.size() <= myModelSize) {
            return Double.NEGATIVE_INFINITY;
        }

        double logSum = 0.0;

        for (int k = 0; k < padded.size() - myModelSize; k++) {
            List<String> context = padded.subList(k, k + myModelSize);
            String nextWord = padded.get(k + myModelSize);

            String key = makeKey(context);
            List<String> follows = myMap.get(key);

            double countContextNext = tokenInContextCount(context, nextWord);
            double countContext = (follows == null) ? 0.0 : follows.size();

            double prob = (countContextNext + smoother) /
                          (countContext + smoother * myVocab.size());

            if (prob <= 0) prob = 1.0 / myVocab.size();
            logSum += Math.log(prob);
        }

        return logSum; // no normalization—keep full log probability
    }

    /** Train model: fill myMap and myVocab */
    @Override
    public void processTraining() {
        // ensure START and END are always counted in vocab
        myVocab.add("<START>");
        myVocab.add("<END>");

        for (int k = 0; k < myWordSequence.size() - myModelSize; k++) {
            List<String> context = myWordSequence.subList(k, k + myModelSize);
            String next = myWordSequence.get(k + myModelSize);

            String key = makeKey(context);
            myMap.putIfAbsent(key, new ArrayList<>());
            myMap.get(key).add(next);
            myVocab.add(next);
        }
    }

    /** Return number of unique words seen in training. */
    public int vocabularySize() {
        return myVocab.size();
    }

    public static void main(String[] args) throws IOException {
        ClassifyingModel mm = new ClassifyingModel(3);
        String dirName = "data/shakespeare";
        mm.trainDirectory(dirName);
        System.out.printf(
            "trained model for %s, vocab size = %d, tokens = %d\n",
            dirName, mm.vocabularySize(), mm.tokenSize()
        );
    }
}
