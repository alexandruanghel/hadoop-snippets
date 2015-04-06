import org.apache.hadoop.fs.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.file.tfile.*;

public class readTFile{
    public static void main (String [] args) throws Exception{
        /*
        Setup variables
         */
        Configuration conf = new Configuration();
        FileSystem hdfs = FileSystem.get(conf);
        Path filePath = new Path(args[0]);

        /*
        Finding out file length
        */
        ContentSummary cSummary = hdfs.getContentSummary(filePath);
        long length = cSummary.getLength();
        System.out.println("File length is: " + length);

        /*
        Opening the file as a FSDataInputStream
        Create a TFile.Reader object as per https://hadoop.apache.org/docs/r1.2.1/api/org/apache/hadoop/io/file/tfile/TFile.Reader.html
        And a Scanner as per https://hadoop.apache.org/docs/r1.2.1/api/org/apache/hadoop/io/file/tfile/TFile.Reader.html#createScanner%28%29
        */
        FSDataInputStream fsdis = hdfs.open(filePath);
        TFile.Reader fileReader = new TFile.Reader(fsdis, length, conf);
        TFile.Reader.Scanner fileScanner = fileReader.createScanner();

        /*
        Advance over 3 keys as per the following structure of the TFile:

        KEY 	            VALUE
        VERSION 	        log file format version
        APPLICATION_OWNER 	application owner
        APPLICATION_ACLS 	application access control
        [container-id] 	    encoded list of container log files

        Taken from http://blogs.splunk.com/2013/11/18/hadoop-2-0-rant/
        */
        fileScanner.advance();
        fileScanner.advance();
        fileScanner.advance();

        /*
        Now we can read all the entries until the end of the file
        */

        while (! fileScanner.atEnd()) {
            /*
            Use a Scanner.Entry object to read each entry.
            As per https://hadoop.apache.org/docs/r1.2.1/api/org/apache/hadoop/io/file/tfile/TFile.Reader.Scanner.Entry.html
            */
            TFile.Reader.Scanner.Entry e = fileScanner.entry();

            /*
            We'll read into a byte array.
            */
            byte[] buf = new byte[e.getValueLength()];
            e.getValue(buf);

            /*
            Now we can read the byte array into a String and do whatever we want with the string
            */
            String fileData = new String(buf, 0, e.getValueLength());
            System.out.println(fileData);
            System.out.println();

            /*
            Advance to the next key in the TFile
            */
            fileScanner.advance();
        }
        fsdis.close();
        hdfs.close();
    }
}