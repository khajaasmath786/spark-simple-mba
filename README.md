# Simple Spark app for Market Basket Analysis
From [Wikipedia](https://en.wikipedia.org/wiki/Affinity_analysis):

> Market basket analysis might tell a retailer that customers often purchase shampoo and conditioner together, so putting both items on promotion at the same time would not create a significant increase in revenue, while a promotion involving just one of the items would likely drive sales of the other.

## Simple usage

### Create a transactions.csv file
Your transactions (old orders of customers) should be specified in CSV format, in which each line represent a single transaction (order).

Example:
```
milk,bread
butter
beer,diapers
milk,bread,butter
bread
```

Or, more realistic, with product ids:
```
116769,456809
220
98098,24708
116769,456809,220
456809
```

### Generate frequent itemsets and association rules
Clone this repo and then:
```
git clone https://github.com/lorenzo-ange/spark-fpgrowth.git
cd spark-fpgrowth
./spark-2.1.0-bin-hadoop2.7/bin/spark-submit --class "FPGrowth" --master local[*] fpgrowth-assembly-1.0.jar \
  --minSupport <MIN_SUPPORT> \
  --minConfidence <MIN_CONFIDENCE> \
  <TRANSACTIONS_CSV_PATH> > <OUTPUT_FILE>
```
I recommend seeing the next sections of this README and tune these values to have accurate results.

### Output
The output file will contain something like this:
```
FPGrowth and Association Rules generation with {
  input:	transactions.sample.csv,
  minSupport:	0.3,
  minConfidence:	0.7,
  numPartitions:	1
}
Number of transactions: 5
Number of frequent itemsets: 4
[butter], 2
[milk], 2
[milk,bread], 2
[bread], 3
Association Rules:
[milk] => [bread], 1.0
```

The number next to each frequent itemset is the number of times it has been found in the dataset.

The number next to each association rule is the confidence of the rule.

## Minimum Support
Support is a value between 0 and 1 indicating how frequently an itemset appears in the DB.

Minimum Support is the treshold value used to discriminate between 'frequent' and 'infrequent' itemsets.

The higher is the Minimum Support, the lower is the number of frequent itemsets found.

### Details
Support for itemset X = number of transactions containing itemset X / number of transactions in DB

Suppose we have this transactions in DB
```
milk,bread
butter
beer,diapers
milk,bread,butter
bread
```
Itemset [milk,bread] will have support 2/5 = 0.4 because appears 2 times in a DB with 5 transactions

Source: [Wikipedia](https://en.wikipedia.org/wiki/Association_rule_learning#Support)

## Minimum Confidence
Confidence is a value between 0 and 1 indicating how often an association rule has been found to be true.

Minimum Confidence is the treshold value used to discriminate between 'strong' and 'weak' rules.

The higher is the Minimum Confidence, the lower is the number of association rules found.

### Details
The confidence value of a rule, X => Y , with respect to a set of transactions T, is the proportion of the transactions that contains X which also contains Y.

Confidence is defined as:
conf(X=>Y) = supp(X U Y) / supp(X)

Suppose we have this transactions in DB
```
milk,bread
butter
beer,diapers
milk,bread,butter
bread
```

Rule [butter,bread] => [milk] will have confidence 0.2/0.2 = 1 because support([butter,bread,milk]) = 0.2 and support([butter,bread]) = 0.2.
This means that 100% of the times a customer buys butter and bread, milk is bought as well.

Source: [Wikipedia](https://en.wikipedia.org/wiki/Association_rule_learning#Confidence)
