import java.util.*;
import java.util.regex.*;
import java.io.*;

public class ClassifyingModel extends BaseMarkovModel {

    // context (N-gram) -> list of tokens that followed in training
    private Map<List<String>, List<String>> myMap;

    // unique tokens seen during training (vocabulary)
    private Set<String> myVocab;

    // memo: for a given context, cache counts of each follow token
    private Map<List<String>, Map<String, Integer>> myCache;

    public ClassifyingModel(int size) {
        super(size);
        myMap   = new HashMap<>();
        myVocab = new HashSet<>();
        myCache = new HashMap<>();
    }

    public ClassifyingModel() {
        this(2);
    }

    /** Regex tokenization: alphabetic words or punctuation marks. */
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
     * Tokenize and pad with <START> and <END> (myModelSize copies on each side).
     * Returns a NEW list; does not modify instance state.
     */
    private List<String> createTokenizedText(String text) {
        List<String> tokens = tokenize(text);
        List<String> padded = new ArrayList<>(tokens.size() + 2 * myModelSize);
        for (int i = 0; i < myModelSize; i++) padded.add("<START>");
        padded.addAll(tokens);
        for (int i = 0; i < myModelSize; i++) padded.add(END);
        return padded;
    }

    /**
     * Count how many times 'token' follows 'context' in the trained model.
     * Uses memoization to avoid rescanning the follows list repeatedly.
     */
    private int tokenInContextCount(List<String> context, String token) {
        // contexts used as keys must be stable lists
        List<String> key = List.copyOf(context);

        Map<String, Integer> inner = myCache.get(key);
        if (inner != null) {
            Integer cached = inner.get(token);
            if (cached != null) return cached;
        }

        List<String> follows = myMap.get(key);
        int count = 0;
        if (follows != null) {
            for (String s : follows) {
                if (s.equals(token)) count++;
            }
        }

        if (inner == null) {
            inner = new HashMap<>();
            myCache.put(key, inner);
        }
        inner.put(token, count);
        return count;
    }

    /**
     * Return the CONTEXT-NORMALIZED log-likelihood of 'text' under this model,
     * with Laplace smoothing: (nextCount + smoother) / (contextCount + smoother * |V|).
     * Normalize by the number of UNIQUE contexts seen in the unknown text.
     */
    public double calculateMatchProbability(String text, double smoother) {
        List<String> padded = createTokenizedText(text);
        if (padded.size() <= myModelSize) return Double.NEGATIVE_INFINITY;

        int V = Math.max(1, myVocab.size());
        double logSum = 0.0;

        // track unique contexts from the unknown text
        Set<List<String>> uniqueContexts = new HashSet<>();

        for (int i = 0; i < padded.size() - myModelSize; i++) {
            // make stable key for maps/sets
            List<String> context = List.copyOf(padded.subList(i, i + myModelSize));
            String next = padded.get(i + myModelSize);

            uniqueContexts.add(context);

            List<String> follows = myMap.get(context);
            int contextCount = (follows == null) ? 0 : follows.size();
            int nextCount = tokenInContextCount(context, next);

            double prob = (nextCount + smoother) / (contextCount + smoother * V);
            // prob cannot be zero with smoothing; guard anyway
            if (prob <= 0) prob = 1.0 / V;

            logSum += Math.log(prob);
        }

        int denom = Math.max(1, uniqueContexts.size());
        return logSum / denom;
    }

    /**
     * Build myMap and myVocab from myWordSequence (already prepared by BaseMarkovModel).
     * Also clears the memo cache so counts reflect this training set.
     */
    @Override
    public void processTraining() {
        // fresh build for this training run
        myMap.clear();
        myVocab.clear();
        myCache.clear();

        // add tokens to vocab and fill context->follows
        for (int i = 0; i < myWordSequence.size(); i++) {
            myVocab.add(myWordSequence.get(i));
        }

        for (int i = 0; i < myWordSequence.size() - myModelSize; i++) {
            List<String> context = List.copyOf(myWordSequence.subList(i, i + myModelSize));
            String next = myWordSequence.get(i + myModelSize);

            myMap.putIfAbsent(context, new ArrayList<>());
            myMap.get(context).add(next);
        }
    }

    /** Number of unique tokens in the trained model. */
    public int vocabularySize() {
        return myVocab.size();
    }

    // (Optional) local test
    public static void main(String[] args) throws IOException {
        ClassifyingModel mm = new ClassifyingModel(1);
        mm.trainDirectory("data/shakespeare");
        System.out.printf("order %d, vocab=%d, tokens=%d%n", mm.getOrder(), mm.vocabularySize(), mm.tokenSize());
    }
}
