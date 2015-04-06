mr-netflow
=================

Hadoop Map Reduce jobs for summarizing and aggregating NetFlow data.

With a benchmark twist (comparing Java and Python).


# Problem details:

We have a large (17GB, 185.000.000 lines) tsv file containing netflow output (one flow per line).

We want to summarize (count unique flows) and aggregate (calculate total size of each unique flow).

For example, from:


    a_id   c_id   ip_src           ip_dst           port_src    port_dst   flags   proto    tos   pkts   bytes   flows   stamp_inserted        stamp_updated
    1      0      10.111.0.80      10.160.216.163   54648       80         0       TCP      0     0      1634    1       2014-03-26 10:29:14   2014-03-26 10:29:14
    1      0      10.111.0.80      10.160.216.163   54654       80         0       TCP      0     0      1686    1       2014-03-26 10:31:04   2014-03-26 10:31:04
    1      0      10.112.12.123    10.160.235.51    54644       443        0       TCP      0     0      8498    1       2014-03-26 10:26:07   2014-03-26 10:26:07
    1      0      10.112.12.123    10.160.235.51    54640       443        0       TCP      0     0      7943    1       2014-03-26 10:26:06   2014-03-26 10:26:06
    1      0      10.112.12.123    10.160.235.51    54645       443        0       TCP      0     0      8498    1       2014-03-26 10:26:07   2014-03-26 10:26:08

We want to get to:

    ip_src          ip_dst           port_dst   proto   packets   bytes
    10.111.0.80     10.160.216.163   80         TCP     2         3320
    10.112.12.123   10.160.235.51    443        TCP     3         24939


This seems like an ideal job for map reduce.

Hadoop allows for two types of map reduce implementations:

* 100% Java, using the Hadoop Java APIs

* other external language (Python, Ruby, Perl), using Hadoop Streaming API that will take care of reading/writing/passing data (via STDIN and STDOUT)



# Hardware:

1 x hadoop-master (8GB Performance 1 - overkill)

4 x hadoop-slave (4GB Performance 1)


# Software:

non-HA Cloudera Hadoop Distribution (CDH) version 4.6

built using https://github.com/ansible/ansible-examples/tree/master/hadoop


# Benchmarks:

### Uploading the 17GB tsv file to HDFS

#### Took around 7 minutes:

```
time hadoop fs -put /flows.tsv /
real     6m30.256s
user     1m3.129s
sys      0m31.162s
```


### Python without Hadoop

#### Took around 1 hour:

```
time cat /flows.tsv | ./FlowsCountMapper.py | sort | ./FlowsCountReducer.py > /root/results.tsv;
real      56m21.704s
user      53m56.678s
sys       1m56.523s
```

(this actually won’t work quite well, as Linux tries to pipe and compute on chunks, so there will be some results not reduced)



### Python with default Hadoop (2 mappers per slave, 1 reducer per cluster)

#### Took about 16.5 minutes:

```
$ hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -file /jobs/FlowsCountMapper.py -mapper /jobs/FlowsCountMapper.py -file /jobs/FlowsCountReducer.py -reducer /jobs/FlowsCountReducer.py -input /flows.tsv -output /flows-results
…
14/04/09 14:16:04 INFO streaming.StreamJob:  map 0%  reduce 0%
14/04/09 14:16:35 INFO streaming.StreamJob:  map 8%  reduce 1%
14/04/09 14:21:31 INFO streaming.StreamJob:  map 100%  reduce 33%
14/04/09 14:32:20 INFO streaming.StreamJob:  map 100%  reduce 100%
14/04/09 14:32:34 INFO streaming.StreamJob: Job complete: job_201404090929_0007
```

### Java with default Hadoop (2 mappers per slave, 1 reducer per cluster)

#### Took about 8 minutes:

```
$ hadoop jar /jobs/FlowsCount.jar FlowsCount /flows.tsv /flows-result
…
14/04/09 14:37:37 INFO mapred.JobClient:  map 0% reduce 0%
14/04/09 14:38:07 INFO mapred.JobClient:  map 8% reduce 2%
14/04/09 14:42:34 INFO mapred.JobClient:  map 100% reduce 33%
14/04/09 14:45:28 INFO mapred.JobClient:  map 100% reduce 100%
14/04/09 14:45:32 INFO mapred.JobClient: Job complete: job_201404090929_0008
```

### Python with optimized Hadoop (4 mappers per slave, 8 reducers per cluster, more heap for Java slaves)

#### Took about 7.5 minutes:

```
$ hadoop jar /usr/lib/hadoop-0.20-mapreduce/contrib/streaming/hadoop-*streaming*.jar -file /jobs/FlowsCountMapper.py -mapper /jobs/FlowsCountMapper.py -file /jobs/FlowsCountReducer.py -reducer /jobs/FlowsCountReducer.py -input /flows.tsv -output /flows-results
…
14/04/09 15:08:25 INFO streaming.StreamJob:  map 0%  reduce 0%
14/04/09 15:08:57 INFO streaming.StreamJob:  map 8%  reduce 2%
14/04/09 15:13:49 INFO streaming.StreamJob:  map 100%  reduce 33%
14/04/09 15:15:38 INFO streaming.StreamJob:  map 100%  reduce 100%
14/04/09 15:15:53 INFO streaming.StreamJob: Job complete: job_201404090929_0011
```

### Java with optimized Hadoop (4 mappers per slave, 8 reducers per cluster, more heap for Java slaves)

#### Took about 5 minutes:

```
$ hadoop jar /jobs/FlowsCount.jar FlowsCount /flows.tsv /flows-result
…
14/04/09 15:29:59 INFO mapred.JobClient:  map 0% reduce 0%
14/04/09 15:30:28 INFO mapred.JobClient:  map 8% reduce 2%
14/04/09 15:34:51 INFO mapred.JobClient:  map 100% reduce 33%
14/04/09 15:35:15 INFO mapred.JobClient:  map 100% reduce 100%
14/04/09 15:35:19 INFO mapred.JobClient: Job complete: job_201404090929_0014
```

# How to compile
To compile the .java MR job, point the classpath to where Hadoop libraries were installed:
```
mkdir -p classes && javac -classpath /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/* -d classes FlowsCount.java
jar -cf FlowsCount.jar -C classes/ .
```

You don't need to have Hadoop installed to compile the job.
Just download the .tar.gz from http://apache.mirror.anlx.net/hadoop/common/ and untar it.
