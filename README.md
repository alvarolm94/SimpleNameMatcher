# SimpleNameMatcher

![alt text](https://github.com/alvarolm94/SimpleNameMatcher/blob/master/logo.png)

## INTRODUCTION:

**Name matching**, sometimes called **fuzzy name matching** or **name comparison**, is a complex problem with a lot of approaches. 

Comparing two people's names to determine how similar they are and providing a score or a similarity percentage is not an easy thing to do for a computer. In order to do that in an acceptable way a lot of factors have to be taken into consideration, such as how we (human beings) perceive and measure the impact of each change between names.

This project started because I failed to find free **Java** libraries to do the kind of match or comparison I needed while working on a software project. 

Most of them were a little complicated to use and/or not convincing enough, meaning that the scores or similarity percentages given between names didn't match the actual grade of similarity perceived by real people (at least for me and some other people I surveyed). 

So, this is my approach to the problem, a **very easy to use** library that can do the job.

Hope it's useful!

## HOW TO USE IT:

1. Download the jar file located in the repository. It contains the last version of the library, ready to use.
2. Import it to your project.
3. Create a **SimpleNameMatcher** object, and just call the method *compareNames(String name1, String name2)* or *compareNames(String name1, String name2, double threshold)*. It will return a double between *0* and *100* representing the score or percentage of similarity. The threshold is the minimum percentage required to consider the names similar. If a threshold is specified, the matching process will end if the threshold is exceeded, returning *0* and saving computing time. If not specified, the default threshold (0) will be used for the matching. 

**Example 1:**  
```java
String name1 = "Ashleigh Thompson-Meyers";
String name2 = "Ashley Thompson Mayer";

SimpleNameMatcher simpleNameMatcher = new SimpleNameMatcher();
double similarity = simpleNameMatcher.compareNames(name1, name2);

System.out.println("Percentage of similarity: " + similarity + " %");
```

Result
```
Percentage of similarity: 71.36%
```


**Example 2:**  
```java
String name1 = "Ashleigh Thompson-Meyers";
String name2 = "Ashley Thompson Mayer";
double threshold = 80;

SimpleNameMatcher simpleNameMatcher = new SimpleNameMatcher();
double similarity = simpleNameMatcher.compareNames(name1, name2, threshold);

System.out.println("Percentage of similarity: " + similarity + " %");
```

Result
```
Percentage of similarity: 0%
```

**Example 3:**  
```java
String name1 = "Néstor laMötte Barrière";
String name2 = "Nestor la Mote Barriere";

SimpleNameMatcher simpleNameMatcher = new SimpleNameMatcher();
double similarity = simpleNameMatcher.compareNames(name1, name2);

System.out.println("Percentage of similarity: " + similarity + " %");
```

Result
```
Percentage of similarity: 90.48%
```

## WARNINGS:

Please, have in mind this two points:

- This code only works with Latin characters. If any of the names provided is written in Japanese or Arabic, for instance, a **NonPermittedSymbolException** will be thrown. 
- The **threshold** must be a double between *0* and *100*. Otherwise a **ThresholdOutOfRangeException** will be thrown. 

## HOW DOES IT WORK?

In a nutshell, the algorithm finds out each one of the differences between the two names and assigns it an impact. Each type of difference has a weight, depending on how significant it is for us. This weights have been adjusted by measuring the impact that each type of change in a name had in different people I surveyed.

It's not the same changing a letter in a name and adding a separation, we all can agree that the first one weights more, has more impact. The first change affects the information in the name while the other just affects the way in which words are separated. It is common to see the same name written with different separations for its words, as people don't consider this that much important. For example, *Andrés De La Guardia* and *Andrés deLaGuardia*. Or *Philip-Andrew Johnson* and *Philip Andrew Johnson*. Converting, for instance, *Arthur Bald* into *Arthur Bold* has a lot more impact than the examples above. 

It's also not the same changing a diacritic (like an accent) and changing a letter, it is obvious that the first one weights less than changing the whole letter. *Juan López* and *Juan Lopez* are a lot more similar than *Juan López* and *Juan Lápiz*.

So, the types of changes considered in the algorithm are these:

- **Change in Diacritic**: For example, *á* -> *a* or *ü* -> *u*.
- **Full separation**: When two words that are together get completely separated. For example, *Thomas laGarde* -> *Thomas la Garde*.
- **Half separation**: When two words that are together get separated with a milder separator, such as a hyphen, or when a mild separator is converted to a strong separator, like a blank space. For example, *Thomas laGarde* -> *Thomas la-Garde* or *Thomas la-Garde* -> *Thomas la Garde*.
-**Change in letter**: A substitution, insertion or deletion in a letter. For example, *Andrea Smith* -> *Andrew Smith* or *Ronaldo Reagan* -> *Ronald Reagan*.

In addition to this, another important thing is considered in the matching process: the impact or weight of each individual change in a name depends on the length of the name (the amount of information that the name has). The longer a name is, the less significant a change in it is. It's not the same changing a letter in *Ri Wun Ho*, a small name, and in *Pablo Emilio Durán Fernández del Álamo*; in the first case the change affects a significant portion of the information while in the second it doesn't. To see it with another perspective: if two names have more information (more letters) that match, the percentage of similarity should be bigger. 

So, the weight or impact that an individual change in a name has depends on its type and the length of the name. 

Now we now the theory, but how is this calculated in the algorithm?

Here is a summarized list of the steps the algorithm takes in each matching process:

**1.** Firstly, it cleans up both names. They are converted to lower case, trimmed, and all the redundant spaces are removed.

**2.** Then, the bigger name (the one with more letters) is selected and used to calculate the impact factor*(**\***)*. Note that only letters count as information. The separation characters, such as hyphens or blank spaces, do not count as information units. 

**3.** If the weight of the difference of length (in letters) between the names reaches the threshold, the algorithm stops to save time and avoid unnecessary operarions.

**4.** A modified version of the Minimum Edit Distance (or Levenshtein Distance) algorithm is used to find the differences between the names. The dinamic programming approach was chosen for this, so in each iteration the algorithm fills a cell in a matrix that represents all the subproblems. The difference with the classic version of the algorithm is that each cell stores a distance value calculated taking into account the weights described above. So if the characters are the same, but have different diacritics, or the character that doesn't match is a separator, the weight of that change is calculated and added.
For each row of the matrix, the minimum distance value is stored, so that if that value makes the threshold to be exceeded the algorithm stops to save time and returns a negative result. 

**5.** The final distance (the one at the bottom right corner) is substracted to 100 and rounded to avoid unnecessary decimals. The result is returned. 


*(**\***) This is the approach I chose, which consists in taking the bigger name and measure the impact of the changes necessary to transform it into the smaller one, calculating the impact factor with its length. Nevertheless, the opposite strategy could be chosen, taking the smaller name and transform it into the bigger one. I chose the first one because the impact factor would be a little smaller when choosing the bigger name, making the algorithm a little more tolerant to changes.*
