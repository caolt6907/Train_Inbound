package com.strategy.call;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import com.strategy.tools.KafkaUtils;

public class CallConsumerTask_Kafka implements Runnable{
	 private static Logger logger = Logger.getLogger(CallConsumerTask_Kafka.class);
	
	 public CallConsumerTask_Kafka(){
		 
	 }
	@Override
	public void run() {
	/*	 KafkaConsumer<String, String> consumer=KafkaUtils.getConsumer();
		 while (true) {
			 

	            ConsumerRecords<String, String> records = consumer.poll(100);  

	            for (ConsumerRecord<String, String> record : records){  

	                System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());  
	            	logger.warn(record.offset()+"="+ record.value());
	            }
	        }
		*/
	}

}
