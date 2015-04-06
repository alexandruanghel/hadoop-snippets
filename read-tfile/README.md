read-tfile
=================

Example how to read a TFile.

TFile is a file format / container that is used by YARN to store logs.

http://hadoop.apache.org/docs/r2.4.0/api/org/apache/hadoop/io/file/tfile/TFile.html

# Usage
It accepts the HDFS file path as the Command-Line Argument:

```
hadoop jar readTFile.jar readTFile /app-logs/alexandru/logs/application_1427810642232_0001/DATANODE-1_45454
```

# How to compile
To compile it, point the classpath to where Hadoop libraries are installed or download the JARs from a mirror:

```
wget http://apache.mirror.anlx.net/hadoop/common/hadoop-2.6.0/hadoop-2.6.0.tar.gz
tar zxvpf hadoop-2.6.0.tar.gz
javac -classpath hadoop-2.6.0/share/hadoop/common/hadoop-common-2.6.0.jar:hadoop-2.6.0/share/hadoop/hdfs/hadoop-hdfs-2.6.0.jar src/readTFile.java
jar -cvf readTFile.jar -C src/ .
```
