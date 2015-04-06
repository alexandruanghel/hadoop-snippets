#!/usr/bin/env python

import sys

# current holds the values for one key
# the reducer reads ordered keys, one by one
current_socket = None
current_count = 0
current_size = 0
socket = None

# we'll read input data from STDIN 
for line in sys.stdin:
    # split each line we got from mapper based on the format the mapper produces
    socket, count, size = line.split('\t', 3)

    # convert the count and size to int
    try:
        count = int(count)
        size = int(size)
    except ValueError:
        continue

    # this works because the reducer receives sorted data
    # we test if the line we're processing is for an existing key (the csv socket in this case)
    if current_socket == socket:
        current_count += count
        current_size += size

    # if not then we have new key (socket)
    # so we write the result for the previous key and initialize the new one
    else:
        # this if is required for the first iteration, when current_socket is None
        if current_socket:
            # write result to STDOUT
            # we split the csv key so we can write only tsv values
            ip_src, ip_dst, port_dst, proto = current_socket.split(';')
            print '%s\t%s\t%s\t%s\t%s\t%s' % (ip_src, ip_dst, port_dst, proto, current_count, current_size)

        # initialize the new key (socket)
        current_socket = socket
        current_count = count
        current_size = size

# this is required to print the last key (socket)
if current_socket == socket:
    ip_src, ip_dst, port_dst, proto = current_socket.split(';')
    print '%s\t%s\t%s\t%s\t%s\t%s' % (ip_src, ip_dst, port_dst, proto, current_count, current_size)
