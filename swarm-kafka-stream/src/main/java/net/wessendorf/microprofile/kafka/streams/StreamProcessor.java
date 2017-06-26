package net.wessendorf.microprofile.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;

public class StreamProcessor {


    private final KStreamBuilder builder = new KStreamBuilder();




    public void startProcessing(final Properties props) {

        // read from the topic that contains all messages, for all jobs
        final KStream<String, String> source = builder.stream("push_messages_metrics");


        // some simple processing, and grouping by key, applying a predicate and send to new topic:

        final KTable<String, Long> successCountsPerJob = source.filter((key, value) -> value.equals("Success"))
                .groupByKey()
                .count("successMessagesPerJob");
        successCountsPerJob.to(Serdes.String(), Serdes.Long(), "successMessagesPerJob");

        final KTable<String, Long> failCountsPerJob = source.filter((key, value) -> value.equals("Rejected"))
                .groupByKey()
                .count("failedMessagesPerJob");
        failCountsPerJob.to(Serdes.String(), Serdes.Long(), "failedMessagesPerJob");

        source.groupByKey()
                .count("totalMessagesPerJob")
                .to(Serdes.String(), Serdes.Long(), "totalMessagesPerJob");



//        // we also could branch, since we all want to
//        final KStream<String, Long>[] branches = source.branch(
//                (key, value) -> value.equals("Success"),
//                (key, value) -> value.equals("Rejected"),
//                (key, value) -> true
//        );
//        // sample: just done the above, only on first
//        branches[0].groupByKey().count("successMessagesPerJob").to(Serdes.String(), Serdes.Long(), "successMessagesPerJob");


        KafkaStreams streams = new KafkaStreams(builder, props);
        streams.start();

    }


}
