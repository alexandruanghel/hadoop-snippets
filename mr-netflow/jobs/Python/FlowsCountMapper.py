#!/usr/bin/env python

import sys

# we'll read input data from STDIN 
# Hadoop will read the data from HDFS and pipe it here
for line in sys.stdin:
    # split the line
    split_line = line.split("\t")

    # don't process the line if the last column doesn't hold a date
    # simple check of the length and : character

    if len(split_line) == 14 and len(split_line[13]) > 10 and split_line[13].find(":") != -1:
        # for ICMP and ESP traffic we set the destination port to 0
        # otherwise we cannot aggregate ICMP and ESP
        if split_line[7] == "ICMP" or split_line[7] == "ESP":
            split_line[5] = '0'

        # write the results to STDOUT
        # the chosen format was a csv key made of ip_src, ip_dst, port_dst and proto
        # and the values are 1 and packet size in bytes
        print '%s\t%s\t%s' % (';'.join([split_line[2], split_line[3], split_line[5], split_line[7]]),
                              1, split_line[10])
