import java.io.* ;
import org.apache.hadoop.conf.Configuration ;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class FlowsCount extends Configured implements Tool{

    /*
    the output value from the mapper will be an array of two values (count and size)
    as a result, the ArrayWritable interface needs to be implemented
    we can call it LongArrayWritable since we'll hold Long values
    */
    public static class LongArrayWritable extends ArrayWritable {
        public LongArrayWritable() {
            super(LongWritable.class);
        }
    }

    /*
    mapper Generics class
    the key/value pair on the input is of Object/Text types
    it will output a Text/LongArrayWritable pair
     */
    public static class FlowsCountMapper extends Mapper<Object, Text, Text, LongArrayWritable> {
        /*
        map method
        it receives a key, which holds the line number in the input file - not very useful
        and the content of the line as a Text object (think of Text as a Hadoop String object)
         */
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // LongArrayWritable is just a carrier object in which we set an array of LongWritable
            LongArrayWritable output_carrier = new LongArrayWritable();
            // this is the array of LongWritable that will hold two Long values: the count and size
            LongWritable output[] = new LongWritable[2];
            // output key, will hold a String
            Text socket = new Text();

            // split the line
            String[] split_line = value.toString().split("\t");

            /*
            don't process the line if the last column doesn't hold a date
            simple check of the length and : character
            */
            if (split_line.length == 14 && split_line[13].length() > 10 && split_line[13].contains(":")) {
                /*
                for ICMP and ESP traffic we set the destination port to 0
                otherwise we cannot aggregate ICMP and ESP
                */
                if (split_line[7].equals("ICMP") || split_line[7].equals("ESP")) {
                    split_line[5] = String.valueOf('0');
                }

                // create the key String, made of ip_src, ip_dst, port_dst and proto, tab separated
                String socket_string = split_line[2] + '\t' + split_line[3] + '\t' + split_line[5] + '\t' + split_line[7];
                socket.set(socket_string);

                // populate the array with size and count Long values
                Long count = (long) 1;
                Long size = Long.parseLong(split_line[10]);
                output[0] = new LongWritable(count);
                output[1] = new LongWritable(size);
                output_carrier.set(output);

                // write the result to the API context
                context.write(socket, output_carrier);
            }
        }
    }

    /*
    reducer Generics class
    the key/value pair on the input is the Text/LongArrayWritable we got from the mapper
    it will output a Text/Text pair
     */
    public static class FlowsCountReducer extends Reducer<Text,LongArrayWritable,Text,Text> {
        /*
        reduce method
        it receives a key, the socket String created by the mapper
        and all the values for that key, as an Iterable object
        each iteration of this object contains a Long array from the mapper
         */
        @Override
        public void reduce(Text key, Iterable<LongArrayWritable> values, Context context)
                throws IOException, InterruptedException {
            int total_count = 0;
            int total_size = 0;

            /*
            iterate through the values Interable object
            each value is a Long array, so we process it
            */
            for (LongArrayWritable val : values) {
                // increment the count for the current key (the socket)
                total_count++;

                // we first need to extract the Long array from the LongArrayWritable carrier object
                Writable array[] = val.get();

                // then we extract the Long size value as the second value from the array
                LongWritable size_object = (LongWritable)array[1];
                long size = size_object.get();

                // add the size to the total_size for the current key (the socket)
                total_size += size;
            }

            // create the output String, as a tsv made of total_count and total_size
            String output = String.valueOf(total_count) + '\t' + String.valueOf(total_size);

            // write the result to the API context
            context.write(key, new Text(output));
        }
    }

    @Override
    public int run(String[] args) throws Exception {

        // Create configuration
        Configuration conf = this.getConf();

        // Create job
        Job job = Job.getInstance(conf);
        job.setJarByClass(FlowsCount.class);

        // Setup MapReduce job
        job.setMapperClass(FlowsCountMapper.class);
        job.setReducerClass(FlowsCountReducer.class);

        // Specify key / value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongArrayWritable.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new FlowsCount(), args);
        System.exit(res);
    }
}
