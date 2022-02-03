# Wikipedia-based Explicit Semantic Analysis

## ESA

Explicit Semantic Analysis (ESA) is a technology used to represent texts as vectors into a document corpus space. The document corpus is traditionally Wikipedia. These vectors can then be used to compare documents for similarity.

ESA was written by Gabrilovich and ? in 2006 and far surpassed the previous approaches to document similarity (LSA). To our knowledge, no further methods of document similarity exist which are better than ESA, though some improvements can be found by augmenting ESA with other technologies (such as word2vec).

This implementation of ESA was written by Sheldon Juncker (sheldonjuncker@gmail.com) and the DreamCloud Team (dreamcloud.app/about).

We created our ESA tool to follow the original ESA implementation as defined by Gabrilovich in his 2007 paper. (add in paper reference).
Using the 2005 Wikipedia XML dump used in this paper, we were able to get Spearman/Pearson scores of 0.75/0.72, and with 2006 we got 0.75/0.73 which are all within 0.01 of the historical ones--and our 2005 numbers are slightly higher than those in the original paper.


## License

This software is provided under the terms of the MIT license, which pretty much lets you do anything you want with the software provided you leave our copyright notice and don't hold us responsible for any liability.

## Usage

`esa-wiki` can be used as a library or as a stand-alone tool. There are a variety of different Maven packages that we host here on GitHub which can be used or combined in various ways:

- `esa-wiki`: A command line tool and library for processing Wikipedia XML dumps and generating a final preprocessed corpus taking into account: templates, variables, term counts, link counts, English stopwords and dictionary words, rare words, and more.
- `esa-core`: The core ESA implementation including text analysis, document preprocessing, and vectorization.
- `esa-score`: The ESA scoring library which contains a set of scoring algorithms (various versions of TF-IDF and BM25) and file system writing and reading tools for working with written score indexes.
- `esa-server`: A simple HTTP server for vectorizing documents and comparing text similarity.
- `esa-tuner`: A tool for verifying the accuracy of an ESA implementation using the WordSim-353 dataset with Spearman and the LP-50 document dataset with Pearson. It also includes a tool for fine-tuning an ESA implementation.

Each of these libraries includes a few CLI tools for creating the various configuration classes from CLI options. This is useful if you want to create a CLI tool for interfacing with the library.

## Prerequisites
To run the `esa-wiki` tool or incorporate it as a library, you'll need to do a few things first.

### 1) Install Java

If you haven't set up Java on your computer, you'll need that.

**On Mac**

`brew install java`


Also, you'll need the Java Development Kit:
https://www.oracle.com/java/technologies/downloads/#jdk17-mac]
Use the `Arm 64 DMG Installer` or `x64 DMG Installer`, depending on your system.

**On Windows**

Download and install the latest version of Java from:
https://www.oracle.com/java/technologies/downloads/#jdk17-windows

### 2) Install Maven
**On Mac**

`brew install maven`

**On Windows**

Download a Maven binary zip archive from:
https://maven.apache.org/download.cgi

Extract it to a location of your choice. Drill down into the directory structure until you find a "bin" folder. Add this so the system path via an environment variable.

To verify your insallation run `mvn -v` from a new terminal window and ensure the command is found.

If there are any issues, check out out the Maven installation website:
https://maven.apache.org/install.html
and this SO page which helped us to resolve some issues:
https://stackoverflow.com/questions/26609922/maven-home-mvn-home-or-m2-home

### 3) Install packages
To use `esa-wiki`, you will need to download and install the `esa-core` and `esa-score` libraries and install them with Maven.
- Download the relevant repositories
- Enter the directories of each repository
- Run `mvn install` to install the package to make it available to `esa-wiki`.

### 4) Install package using Maven

In the home folder of the `esa-wiki` repo, run:
`mvn package`

This compiles the Java, links to all relevant dependencies, and generates a stand-alone executable JAR file in the "target" folder. 

### 5) Make folders for Wikipedia data

In the home folder of the repo:
`mkdir index` (for the Lucene index)
`mkdir enwiki` (to store your English Wikipedia download. You could use a different folder name and then different commands if you like).

### 6) Download a Wikipedia dump

A list of all available database dumps is available here: <https://dumps.wikimedia.org/backup-index-bydb.html>.

Click on a Wikipedia version, it's good to start with a Simple Wikipedia version if you're using English:

![image](https://user-images.githubusercontent.com/14936307/145384562-2431a7d5-bd36-454c-8779-241414e1f5a9.png)

On the next page, choose a download which contains all current articles without history, such as this:

![image](https://user-images.githubusercontent.com/14936307/145387013-26238b20-8be5-4803-9775-281231ac1c45.png)

### 7) Put the dump in the folder

Take your downloaded Wikipedia database dump (a zipped file in .bz2 format, e.g. `simplewiki-20211201-pages-articles-multistream.xml.bz2`), and put it in the folder you just made, e.g. `enwiki`.

### 8) Build the index

This can take some time, depending on your system:
On Mac:

- Make the `esa.sh` file executable: `chmod +x esa.sh`
- Run the script: `./esa.sh --preprocess enwiki/simplewiki-20211201-pages-articles-multistream.xml.bz2 index/2021 --title-exclusion [regex1 rege2 ...]` (Make sure you reference the dump file you just put in the folder)

On Windows:
`./esa.sh --preprocess enwiki/simplewiki-20211201-pages-articles-multistream.xml.bz2 index/2021 --title-exclusion [regex1 rege2 ...]`


### Indexing



### Analyzing



## References
