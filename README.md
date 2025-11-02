# Project 2: Classifying Markov Models, Fall 2025

This is the high-level project document for Project P2-Markov-Classifying in CompSci 201 at Duke University, Fall 2025.

See [the details document](docs/details.md) for information on using Git, starting the project, 
and more details about the project including information about the classes and concepts 
that are outlined briefly below. 

*You'll absolutely need to read the information* in the [details document](docs/details.md) to understand how the classes 
in this project work independently and together. 

This project earns engagement points and
also earns points in the numerator of your
total project percentage, but it is **not required**, so the denominator of the project
percentage does **not include** these points. 

You must complete this project **before** October 31 for credit.

## Introduction

In Projects 1.1 and 1.2 you were asked to implement two different Markov Model classes
to _generate_ text probabilistically after training on different authors. Those models,
`SimpleMarkov` and `HashMarkov` are *generative AI Markov Models*. 

In this project you'll implement a new subclass of `BaseMarkov` that is
conceptually a variant of `HashMarkov`. 

This new `ClassifyingModel` is used
to *classify* unknown texts after training on many different authors. Your class, `ClassifyingModel`, will extend the `BaseMarkovModel` class (as classes in P1.1 and P1.2 did). You'll train multiple models on the
works of say `N` authors, each model representing a Markov Model for one of the authors, say
M<sub>1</sub>, M<sub>2</sub>, ..., M<sub>N</sub>. Then, for a set of unknown files, 
say U<sub>1</sub>, U<sub>2</sub>,..., U<sub>k</sub>, you'll find the maximum likelyhood estimate (MLE)
for each of the `k` files that it's authored by each of the `N` authors (represented by a trained model). The largest of these MLE values will "predict" or "classify" the author of the unknown text.

**You will need to copy/paste the output of running `Classifier201` before and after memoizing as described
below.**

### Similar code in AuthorShip.java

We discussed the code in [AuthorShip.java](src/AuthorShip.java) in class. That code
trains a single order-1 Markov Model on the works of one author. Then it
tries to identify which one of `k` unknown files U<sub>1</sub>, U<sub>2</sub>,..., U<sub>k</sub> is most
likely authored by the single author used to train the model.

In this assignment you'll create `N` Markov Models and then
identify which of each of the `k` unknown files is most likely written by which of the `N` authors.


## General Work for this project

Your goal is to create a class `ClassifyingModel` and use it to train ten
or more different models, and then use these models to classify
texts whose authorship is "unknown" in the sense that these texts weren't part
of the training process.

Summary:
    - Fork/Clone the project
    - Compete the implementation of `ClassifyingModel`
    - Run the class `Classifier201` with different orders to predict the best
    (maximal MLE) for unknown texts to answer the analysis questions.

Details about `ClassifyingModel` can be found 
in the [details document](docs/details.md) and more information is included
below for determining correctness.

### Reading/Commenting at Start (optional, Engagement points)

This assignment is clearly an extension of Project P1, but the classifying aspect
is part of a new emphasize on AI/ML in Compsci 201 during the Fall 2025.

This new version is built for 201 in the Fall 2025 course offering and differs in *several details* from previous versions. This
means that using an LLM or previous solutions may not be relevant.

## Running Driver Code

The primary driver code for this assignment is located in `Classifier201.java`. You should be able to run 
the `public static void main` method of that program: `Classifer201.java` 
immediately after cloning the starter code, and should see something like the output shown below. The timings are not relevant, the vocabulary sizes 
are incorrect, and the probabilities are not based on actual calculations

```
data has 11 subdirs
training          dumas order 1 with 2 unique tokens, 530842 tokens
training     dostoevsky order 1 with 2 unique tokens, 955589 tokens
training          hesse order 1 with 2 unique tokens, 186468 tokens
training    shakespeare order 1 with 2 unique tokens, 223930 tokens
training       melville order 1 with 2 unique tokens, 493511 tokens
training          twain order 1 with 2 unique tokens, 607487 tokens
training          kafka order 1 with 2 unique tokens, 137797 tokens
training         proust order 1 with 2 unique tokens, 368899 tokens
training         alcott order 1 with 2 unique tokens, 539696 tokens
training          verne order 1 with 2 unique tokens, 402366 tokens
training        cbronte order 1 with 2 unique tokens, 556642 tokens
time: 0.00 for proust
time: 0.00 for hesse
time: 0.00 for alcott
(more not shown)
...
```

As you can see from the output the `data` folder has 11 sub-folders, one for each of 11 authors. The code in `Classifier201` then tries to match the ten "unknown" files in the folder named `identify` using the maximum likelihood estimate code you'll complete in `ClassifyingModel`.

## Programming and Testing

Please see [the details document](docs/details.md) for complete information on the code you must write. As described above, you'll run the program `Classifier201` which creates many `ClassifyingModel` objects, trains
them on 11 different author/folders, then tries to match "unknown" works against these models using
the maximum likelihood estimate code you write. You'll complete the following methods
in `ClassifyingModel` (each is described in detail in [the details document](docs/details.md)).

  - `processTraining` partially complete, you must update vocabulary, stored in a `HashSet` instance variable.
  - `vocabularySize` uses the `HashSet` instance variable and returns its size.
  - `calculateMatchProbability` which uses the instance variables, a local variable, and the logic
  described in [the details document](docs/details.md).

After implementing these, you will likely be able to run `Classifier201` and see if it matches the expected output. Then you'll need to modify `tokenInContextCount` using memoization to make your program more efficient.

See the expected output in [the details document](docs/details.md). Copy that output to a document you'll turn in as part of the analysis questions, and to which you can compare the more efficient, memoized version of your program.



## Analysis Questions

Answer the following questions in your analysis. You'll submit your analysis as a separate PDF as a 
separate assignment to Gradescope. Answering these questions will require you to run the driver code to 
generate timing data and to reason about the algorithms and data structures you have implemented. 
Here is the [template file for submitting analysis to Gradescope](https://courses.cs.duke.edu/compsci201/fall25/assign/p2-classify-analysis.docx).

### Working Together for Analysis

You're *stronlgy encouraged* to work with others in 201 in completing the analysis section for this project. 
In future projects you'll work on an entire project in pairs, and submit once for the pair. For this project, however, 
each person should submit independently. If you actively work with one or more people in 201, *please make sure* you list each other 
in the analysis document you turn in. 

**For your analysis repsonses submit a PDF 
with the answer to each of the four questions below on a separate page.** (for copy/paste, some of the results may take more than a page. Start the answer to each question on a separate page.)

### Question 1

Copy/paste the results from running `Classifier201` on your model **before** you implement memoizing. Before that
output, explain why you think your program is correct. This should include comparisions to the expected
results as described in the [details document](docs/details.md).

### Question 2

Copy/paste the results of running `Classifier201` **after** memoizing. In text you write before
the output explain why you think your program is correct. Explain (in your own words) why memoizing
makes your program faster. Describe how you'd disable memoizing by indicating how you'd change
code in `tokenInContextCount` in a simple way to avoid activing the memoization.

### Question 3

The "unknown" documents were actually each created by one of the authors used when
training models, e.g., the author has works in the `data` folder. Design and explain
a method/experiment of how you might verify that MLE is working even when it fails to match an unknown work
correctly, e.g., the unknown work is created by an author **not** represented in
the `data` folder. You do **not** need to actually run the code to verify your
approach, but you can get extra points be doing that and explaining your methodology as
well as your results. Note that the training data includes works written in three
different languages: English, French, and German.

### Question 4

Modify the code that prints results in `Classifier201` to also print a measure of "how much
more likely" the best model is compared to/than the second best model. Since the values
printed are log-likelihoods, you'll compute this measure by $$e^{L1 - L2}$$ where $L1$ is the log likelihood of the best model (MLE) and $L2$ is the
next best. Note that you'll use `Math.exp(L1-L2)` in Java to raise $e$ to a power. Print these
results for each of the unknown texts, copy/paste them into your analysis document. Copy/paste the
code that prints them, and explain the likelihood
ratios for two of the unknown texts in your analysis.



## Submitting and Grading

There may be an autograder to check the efficiency of your memoized code. However, for P2, you're expected
to verify the correctness of your program as part of the the analysis questions you answer. You'll upload
your answers as a PDF to gradescope.



### Grading

| Section.  | points |
|-----------|--------|
|Analysis   |     12 |
|Code       |      8 |  
