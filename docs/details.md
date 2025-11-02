## Starter Code and Using Git

**_You should have installed all software (Java, Git, VS Code) before completing this project._** You can find 
the [directions for installation here](https://coursework.cs.duke.edu/201fall25/resources-201/-/blob/main/installingSoftware.md) (including workarounds for submitting without Git if needed).

We'll be using Git and the installation of GitLab at [coursework.cs.duke.edu](https://coursework.cs.duke.edu). All code for classwork will be kept here. Git is software used for version control, and GitLab is an online repository to store code in the cloud using Git.

For this project, you **start with the URL linked to course calendar**, 
[https://coursework.cs.duke.edu/201fall25/p1-markov-fall2025](https://coursework.cs.duke.edu/201fall25/p1-markov-fall2025).

**[This document details the workflow](https://coursework.cs.duke.edu/201fall25/resources-201/-/blob/main/projectWorkflow.md) for downloading the starter code for the project, updating your code on coursework using Git, and ultimately submitting to Gradescope for autograding.** We recommend that you read and follow the directions carefully this first time working on a project! While coding, 
we recommend that you periodically (perhaps when completing a method or small section) push your changes as explained in Section 5.

## Coding in Project P2: Markov Classifier

When you fork and clone the project, **make sure you open the correct project folder in VS Code** 
following the [This document details the workflow](https://coursework.cs.duke.edu/201fall25/resources-201/-/blob/main/projectWorkflow.md).

## Java Background 

Just as with P1.1 and P1.2, you'll be creating a new class `ClassifyingModel` 
that extends `BaseMarkovModel`. As described below, 
you'll use concepts from the P1.2 class `HashMarkovModel` that you previously wrote. If you didn't complete that assignment you'll likely benefit from the code in `ClassifyingModel` which includes a partially complete implementation that uses a `HashMap`. You'll need to complete
`processTraining` based on the coding details you'll find below. However, the code you're given initializes the instance variable `myMap`
correctly.


### constructors

You'll need to implement two constructors that correspond to the two constructors in `BaseMarkovModel`. These constructors are partially completed
in the code you fork/clone, but the instance variable provided is *not* initialized and there are more instance variables needed that must be initialized
in the constructor as well.

These instance variables are described next.

### instance variables

You should define and initialize instance variables that will allow you to meet correctness and
performance criteria defined below. In the [P1:Markov-GenAI](https://coursework.cs.duke.edu/201fall25/p1-markov-fall2025) assignment you were advised to use `HashMap<List<String>,List<String>>` as an instance variable. The keys in this map are each _context_, or an N-gram of N-tokens/words where `N` is the order of the model. The corresponding value is a list of the words/tokens that follow the key _context_. See that assignment for
more details. In the code you fork/clone, this instance variable `myMap` is defined, **and is** given a value in the constructor. **You'll need additional instance variables as well.**

You will need a `HashSet<String>` to store the unique words/tokens
that constitute a model's vocabulary (described below). To meet performance criteria after you know your model
is correct, you'll likely need `HashMap<List<String>, Map<String,Integer>` in which each different 
_context_ is a key, and the corresponding value is a map of each following word/token and the number of times
the following word/token occurs. Details for this are described below.

*Note that your code also has access to the `protected` instance variables
in `BaseMarkov`, including `myWordSequence`.*

## The processTraining() method

You'll need to implement the `processTraining` method that was described in implementing `HashMarkovModel`). This method
is called in `BaseMarkovModel` as the last line of both `trainDirectory` and `trainText`. In `HashMarkovModel` the
code you wrote populated the instance variable that was termed `myMap` (which exists in the code you fork/clone). You should assume that `processTraining` is
only called from `trainDirectory` so that the inherited instance variable `myWordSequence` has been
filled with words/tokens. *You must add each of these words/tokens to the instance variable `HashSet<String>` that represents the model's
vocabulary* and you must initialize instance variables appropriately. The instance variable `myMap` is, however, initialized in the code you're given.

### Relevant instructions from `HashMarkovModel`

See the [details document](https://coursework.cs.duke.edu/201fall25/p1-markov-fall2025/-/blob/main/docs/details.md) for P1:HashMarkovModel for information on adding each possible context (`List<String>` sequences) as a key
with a corresponding value of an `ArrayList<String>` of the words/tokens that follow this context. That functionality is replicated
in your implementation of `processTraining` in the class `ClassifyingModel` you're given. 

## Required public and private helper methods

You are given method headers and some code for:
  - `vocabularySize` that returns the number of unique words/tokens in the trained model (you must implement this -- it should return the size of a set you create and populate.)
  - `tokenInContextCount` for a given _context_ and _next_ word/token, returns the number of times `next` follows `context. You are given _slow code_ for this method, after you know it works (implementing the next method) you'll make it more efficient.
  - `calculateMatchProbability` - returns the maximum likelihood estimate  (MLE) that some text matches this model. This is the key method in determining the MLE for a given text.

Information on these helper methods can be found in the comments included
for them in the `ClassifyingModel.java` code you fork/clone and then modify.

### Performance Criteria

You will need to add an instance variable
that _caches_ or _memoizes_ the number of times each follow word/token occurs for each _context_ to meet performance criteria for how long
it takes `calculateMatchProbability` to execute, because the code in that
method will call `tokenInContextCount` as described next.  The code you'd 
write for this without the _cache_/instance variable is similar to what's shown below for
a helper method you're given. To calculate match probabilities you need this value, but
for improved perfomance you'll modify this approach as described below.

```
  private int tokenInContextCount(List<String> context, String token) {
      int count = 0;
      for(String s : myMap.get(context)) {
            if (s.equals(token)) {
                count += 1;
            }
      }
      return count;
  }
```

## Method calculateMatchProbability

The method `calculateMatchProbability` is called from `Classifier201` to obtain the maximal likelihood
estimate (MLE) that an unknown text matches the trained model. The 
method `calculateLogLikelihood` 
from [`AuthorShip.java`](../src/AuthorShip.java) is
similar and will likely prove useful as a model. The math behind the code you write, and
the general flow of control are described below.

The `text` parameter is the unkown text for which this method calculates
the MLE for this text compared to this trained model. The general steps are

  - Convert the text to a `List` of words/tokens by calling the `createTokenizedText`
    helper method. The code in that called method is very similar to the `updateWordSequence` code in `BaseMarkovModel` but in that class the code doesn't return a value as it does here.
  - loop over every possible context (`List<String>` with myOrder values) and the following word/token for the unknown text. For each of these:
    - calculate the number of times the `context` occurs in the trained model, store in an appropriaely named variable, e.g., `contextCount` (this value can be determined from the map instance variable of the trained model).
    - calculate the number of times `next` follows `context` in the trained model by calling helper method `tokenInContextCount`, store in an appropriately named variable, e.g., `nextCount`
    - use this equation to calculate an MLE probability for one context and one token: 
    $$
    (nextCount + smoother)/(contextCount + smoother*vocabSize)
    $$
    See the similar value in [`AuthorShip.java`](../src/AuthorShip.java) for example. Note that `nextCount` is calculated by calling `tokenInContextCount` and `contextCount` is the number of times `context` 
    occurs in the trained model, which can be determined directly from `myMap`.
    - as you loop, accumulate the sum of the log of each probability. Again see [`AuthorShip.java`](../src/AuthorShip.java) for similar code.
  - After the loop, return the _context normalized_ sum of all log-probabilities. For this you'll divide the log-sum by the number of unique contexts in the unknown text. You'll need to track that number in the loop, e.g., using a local `HashSet` to store all the (unique) contexts from the unknown texts.

## Efficiency Considerations in calculateMatchProbabilities

The method `tokenInContextCount` that is part of the code you fork 
loops over all tokens that follow parameter `context` and thus has complexity $O(N)$ where $N$ is the total number of
tokens in the list that follow `context`. For some authors the average
length of these lists across all contexts is small, but for other authors
it is large, e.g., the average length ranges from 12 to 56. When an unknown
text has contexts that "hit" these longer lists, the time across all
matches can be excessive. On ola's (very new Mac-pro) laptop, timings to match the file `old-fashioned-girl.txt` against all authors is shown below:
```
    time: 0.67 for proust
    time: 0.42 for hesse
    time: 3.42 for alcott
    time: 2.41 for verne
    time: 1.33 for dumas
    time: 0.24 for kafka
    time: 1.00 for shakespeare
    time: 4.60 for twain
    time: 9.06 for dostoevsky
    time: 2.98 for cbronte
    time: 2.59 for melville
```
With the optimization described next, the timings change as follows:
```
    time: 0.07 for proust
    time: 0.06 for hesse
    time: 0.51 for alcott
    time: 0.31 for verne
    time: 0.14 for dumas
    time: 0.04 for kafka
    time: 0.16 for shakespeare
    time: 0.41 for twain
    time: 1.44 for dostoevsky
    time: 0.36 for cbronte
    time: 0.47 for melville
```

### Caching/Memoizing

If `tokenInContextCount` is called several times with the
same `context` and `token` parameters, the code calculates the same
return value each time by looping, checking for equality, and incrementing a count. 
If the value for each pair of `context` and `token` pairs
was _stored_, it could be returned in $O(1)$ time without the loop. This
kind of optimization is called _caching_ or _memoizing_ as explained
in [this Wikipedia article](https://en.wikipedia.org/wiki/Memoization).

There are several ways to _memoize_ these results for each _context_ and _token_ pair. We suggest one method, using an instance variable `Map<List<String>, Map<String,Integer>> myCache`. Each unique `context` is a key in this map. The corresponding
value is a map of each `token` that follows the `context` key to to the integer
value of how many times the `token` follows `context. You'll likely need to think carefully about what this means and how it works.

When `tokenInContextCount` is called with `context`, `token` parameters for the
first time, the loop that counts the number of occurrences of `token` that follows
`context` executes. Before the count is returned, you must store the value in `myCache`, e.g.,
with code similar to (conceptually at least)
```
    myCache.get(context).put(token,count);
```
Note that the value of `myCache.get(context)` is a map that now has
the value of how many times `token` follows `context` stored in the map.

Modify the method `tokenInContextCount` so that before
the loop in the body of `tokenInContextCount` the map `myCache` is
checked, e.g., using `myCache.get(context).containsKey(token)`. If this is true? The
value of how many times `token` follows `context` was previously calculated and stored. This value can be obtained from `myCache` and returned --- the loop doesn't execute at all! By storing the result
the first time the method is called for each `context` and `token` pair, the
result can be retrieved rather than recalculated on the next calls with the 
same pair.

This improvement makes each model faster and faster when used to identify
more and more unknown texts since some of those texts may share the
same `context` and `token` pairs. Runs of `Classsifier201` should be much faster when you've implemented memoization.

## Correct Results

If you implement `calculateMatchProbability` as described above, you should get _best matches_ with probabilities similar to
those shown below. Please post to ED if yours are drastically different from those shown. Your timings will be different, and the timings below are from a version that does **NOT* use memoizing.

```
time: 0.14 for proust
time: 0.09 for hesse
time: 0.83 for alcott
time: 0.46 for verne
time: 0.32 for dumas
time: 0.06 for kafka
time: 0.26 for shakespeare
time: 0.67 for twain
time: 2.23 for dostoevsky
time: 0.84 for cbronte
time: 0.49 for melville
*** -54.07      shakespeare for caesar.txt
-54.07  shakespeare
-71.43  dostoevsky
-72.33  twain
-72.81  alcott
-73.22  cbronte
-74.00  melville
-74.59  verne
-90.91  kafka
-93.80  hesse
-95.21  proust
-95.71  dumas
time: 0.03 for proust
time: 0.03 for hesse
time: 0.07 for alcott
time: 0.04 for verne
time: 0.05 for dumas
time: 0.02 for kafka
time: 0.02 for shakespeare
time: 0.06 for twain
time: 0.14 for dostoevsky
time: 0.08 for cbronte
time: 0.04 for melville
*** -26.24      kafka for urteil.txt
-26.24  kafka
-27.33  hesse
-35.91  shakespeare
-37.26  alcott
-37.26  verne
-37.28  proust
-37.43  dumas
-37.61  dostoevsky
-37.93  cbronte
-38.01  twain
-38.36  melville
time: 0.23 for proust
time: 0.45 for hesse
time: 0.84 for alcott
time: 0.45 for verne
time: 0.61 for dumas
time: 0.27 for kafka
time: 0.22 for shakespeare
time: 0.71 for twain
time: 1.87 for dostoevsky
time: 0.57 for cbronte
time: 0.48 for melville
*** -66.73      hesse for gertrude.txt
-66.73  hesse
-68.05  kafka
-89.16  shakespeare
-92.66  alcott
-92.75  verne
-93.10  proust
-93.35  dumas
-93.82  dostoevsky
-94.40  cbronte
-94.68  twain
-95.48  melville
time: 0.47 for proust
time: 0.34 for hesse
time: 3.11 for alcott
time: 2.07 for verne
time: 1.18 for dumas
time: 0.22 for kafka
time: 0.97 for shakespeare
time: 3.29 for twain
time: 9.24 for dostoevsky
time: 3.23 for cbronte
time: 2.40 for melville
*** -104.23     alcott for old-fashioned-girl.txt
-104.23 alcott
-109.66 dostoevsky
-113.16 twain
-114.41 cbronte
-121.66 verne
-122.03 melville
-123.95 shakespeare
-159.29 kafka
-164.73 hesse
-167.12 proust
-167.84 dumas
time: 0.16 for proust
time: 0.11 for hesse
time: 0.86 for alcott
time: 0.64 for verne
time: 0.44 for dumas
time: 0.07 for kafka
time: 0.35 for shakespeare
time: 0.94 for twain
time: 2.63 for dostoevsky
time: 0.90 for cbronte
time: 0.65 for melville
*** -66.57      shakespeare for othello.txt
-66.57  shakespeare
-72.02  dostoevsky
-73.64  twain
-73.83  alcott
-73.92  cbronte
-75.67  melville
-76.19  verne
-93.05  kafka
-95.99  hesse
-97.36  proust
-97.82  dumas
time: 0.61 for proust
time: 0.46 for hesse
time: 4.82 for alcott
time: 3.97 for verne
time: 1.63 for dumas
time: 0.32 for kafka
time: 1.56 for shakespeare
time: 5.79 for twain
time: 16.23 for dostoevsky
time: 5.30 for cbronte
time: 4.34 for melville
*** -109.57     twain for innocents.txt
-109.57 twain
-110.34 dostoevsky
-111.84 alcott
-112.19 verne
-113.26 cbronte
-113.86 melville
-116.45 shakespeare
-143.70 kafka
-148.31 hesse
-150.49 proust
-150.77 dumas
time: 0.39 for proust
time: 0.30 for hesse
time: 2.63 for alcott
time: 2.26 for verne
time: 1.06 for dumas
time: 0.20 for kafka
time: 0.88 for shakespeare
time: 3.12 for twain
time: 8.53 for dostoevsky
time: 2.85 for cbronte
time: 2.45 for melville
*** -91.32      verne for fiveweeks-balloon.txt
-91.32  verne
-93.47  dostoevsky
-94.45  twain
-95.57  alcott
-95.96  melville
-96.39  cbronte
-100.78 shakespeare
-125.57 kafka
-129.59 hesse
-131.49 proust
-132.11 dumas
time: 2.21 for proust
time: 0.54 for hesse
time: 2.27 for alcott
time: 1.42 for verne
time: 4.75 for dumas
time: 0.35 for kafka
time: 0.76 for shakespeare
time: 2.24 for twain
time: 5.69 for dostoevsky
time: 1.96 for cbronte
time: 1.42 for melville
*** -115.96     dumas for dumas-story.txt
-115.96 dumas
-128.53 proust
-173.30 cbronte
-177.19 kafka
-179.78 shakespeare
-183.01 hesse
-183.49 dostoevsky
-184.34 verne
-185.10 alcott
-187.78 twain
-190.47 melville
time: 0.25 for proust
time: 0.18 for hesse
time: 1.68 for alcott
time: 1.26 for verne
time: 0.66 for dumas
time: 0.12 for kafka
time: 0.57 for shakespeare
time: 1.84 for twain
time: 5.48 for dostoevsky
time: 1.86 for cbronte
time: 1.49 for melville
*** -80.63      dostoevsky for gambler.txt
-80.63  dostoevsky
-85.60  cbronte
-87.11  alcott
-87.65  twain
-88.07  verne
-90.19  melville
-91.89  shakespeare
-119.82 kafka
-123.65 hesse
-124.94 proust
-125.12 dumas
time: 2.26 for proust
time: 0.43 for hesse
time: 1.86 for alcott
time: 1.16 for verne
time: 4.37 for dumas
time: 0.30 for kafka
time: 0.59 for shakespeare
time: 1.83 for twain
time: 4.35 for dostoevsky
time: 1.60 for cbronte
time: 1.18 for melville
*** -99.74      proust for prisonniere.txt
-99.74  proust
-102.62 dumas
-141.91 cbronte
-142.96 kafka
-144.34 shakespeare
-147.69 hesse
-148.42 dostoevsky
-148.79 verne
-149.39 alcott
-152.12 twain
-153.69 melville
```