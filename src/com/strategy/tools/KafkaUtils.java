package com.strategy.tools;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaUtils {
//	private static String bootstrapServers="112.64.145.218:59092";
  private static String bootstrapServers="127.0.0.1:59092";	
	private static String acks="all";
	private static int retries=0;
	private static int batchSize=16384;
	private static int lingerMs=1;
	private static int bufferMemory=33554432;
	private static String keySerializer="org.apache.kafka.common.serialization.StringSerializer";
	private static String valueSerializer="org.apache.kafka.common.serialization.StringSerializer";
	public static  Producer<String, String> producer;
/*	
	private static String groupId="test";
	private static String enableAutoCommit="true";
	private static String autoCommitIntervalMs="1000";
	private static String sessionTimeoutMs="30000";
	private static String keyDeserializer="org.apache.kafka.common.serialization.StringDeserializer";
	private static String valueDeserializer="org.apache.kafka.common.serialization.StringDeserializer";
	
	public static  KafkaConsumer<String, String> consumer;
*/	
	static{
		Properties producerProps = new Properties();
	//	Properties ConsumerProps = new Properties();
		
		producerProps.put("bootstrap.servers", bootstrapServers);
		producerProps.put("acks", acks);
		producerProps.put("retries", retries);
		producerProps.put("batch.size", batchSize);
		producerProps.put("linger.ms", lingerMs);
		producerProps.put("buffer.memory", bufferMemory);
		producerProps.put("key.serializer", keySerializer);
		producerProps.put("value.serializer", valueSerializer);
		
		producer=new KafkaProducer<>(producerProps);
	/*	
		ConsumerProps.put("bootstrap.servers", bootstrapServers);
		ConsumerProps.put("group.id", groupId); 
		ConsumerProps.put("enable.auto.commit", enableAutoCommit);
		ConsumerProps.put("auto.commit.interval.ms", autoCommitIntervalMs);
		ConsumerProps.put("session.timeout.ms", sessionTimeoutMs);
		ConsumerProps.put("key.deserializer", keyDeserializer);
		ConsumerProps.put("value.deserializer", valueDeserializer);
		
		consumer = new KafkaConsumer<>(ConsumerProps);
		consumer.subscribe(Arrays.asList(groupId));
		*/
		
	}

	public static Producer<String, String> getProducer() {
		return producer;
	}

/*	public static KafkaConsumer<String, String> getConsumer() {
		return consumer;
	}
*/
	
	public static void main(String[] arg){
		producer.send(new ProducerRecord<String, String>("test", "123"));
	}
}
