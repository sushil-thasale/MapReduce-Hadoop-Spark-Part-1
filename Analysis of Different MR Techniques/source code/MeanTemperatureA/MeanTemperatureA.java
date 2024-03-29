/*
 * MeanTemperatureA : Computes average TMIN and TMAX for each station 
 * 					  without using any sort of combiners
 * 
 */
package cs6240;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MeanTemperatureA {
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err
					.println("Usage: hadoop jar This.jar <in> [<in>...] <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(MeanTemperatureA.class);
		job.setMapperClass(MeanTempMapperA.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(DoublePairRecord.class);
		job.setReducerClass(MeanTempReducerA.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		}
		FileOutputFormat.setOutputPath(job, new Path(
				otherArgs[otherArgs.length - 1]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}

/*
 * MeanTempMapperA : emits a DoublePairRecord for each received line of text
 * 					 if line contains tmin then, dp => (tmin,1,0,0)
 * 					 if line contains tmax then, dp => (0,0,tmax,1)
 */
class MeanTempMapperA extends Mapper<Object, Text, Text, DoublePairRecord> {

	private Text stationID = new Text();

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {

		DoublePairRecord dp = new DoublePairRecord();

		String columnValues[] = value.toString().split(",");
		stationID.set(columnValues[0]);
		double tValue = Double.parseDouble(columnValues[3]);

		if (columnValues[2].equals("TMIN")) {
			dp.incrementTMIN(tValue);			
			context.write(stationID, dp);

		} else if (columnValues[2].equals("TMAX")) {
			dp.incrementTMAX(tValue);
			context.write(stationID, dp);

		}
	}
}

/*
 * MeanTempReducerA : Combines all the DoublePairRecords of a particular station
 * 					  Computes average tmin and average tmax 
 * 					  Emits stationID and corresponding average tmin and tmax	
 */
class MeanTempReducerA extends Reducer<Text, DoublePairRecord, Text, Text> {

	private Text result = new Text();

	public void reduce(Text key, Iterable<DoublePairRecord> values,
			Context context) throws IOException, InterruptedException {

		double sumTMIN = 0;
		double sumTMAX = 0;
		long countTMIN = 0;
		long countTMAX = 0;
		double avgTMIN = 0;
		double avgTMAX = 0;

		for (DoublePairRecord val : values) {

			if (val.getCountTMIN() != 0 && val.getCountTMAX() == 0) {
				sumTMIN += val.getTMIN();
				countTMIN++;	
				
			} else if (val.getCountTMIN() == 0 && val.getCountTMAX() != 0) {
				sumTMAX += val.getTMAX();
				countTMAX++;
			}
		}

		avgTMAX = (double) sumTMAX / countTMAX;
		avgTMIN = (double) sumTMIN / countTMIN;
	
		result.set(String.valueOf(avgTMIN) + "\t" + String.valueOf(avgTMAX));
		context.write(key, result);
	}
}
