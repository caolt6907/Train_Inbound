package com.strategy.call;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;









import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;
import org.freeswitch.esl.client.dialer;

import com.strategy.tools.KafkaUtils;
import com.strategy.tools.Util;
import com.strategy.vo.CallResultModel;

public class CallTableTask_Robot implements Runnable {
	 private static Logger logger = Logger.getLogger(CallTableTask_Robot.class);
	private  Queue<String> queue;
	private Map flagMap;
	private String threadName;
	private String sProject;
	private String StrategyId;
	private String ProjectNumber;
	private String AsrName;
	Producer<String, String> producer;
	public CallTableTask_Robot( Queue<String> queue,String ProjectNumber,Map flagMap,String threadName,String StrategyId,String AsrName,Producer<String, String> producer){
		this.queue=queue;
		this.flagMap=flagMap;
		this.threadName=threadName;
		this.sProject=threadName.split("_")[0];
		this.StrategyId=StrategyId;
		this.producer=producer;
		this.ProjectNumber = ProjectNumber;
		this.AsrName=AsrName;
	}

	@Override
	public void run() {
		try{
				
		while(flagMap.get(this.threadName).equals("0")){
		//	 System.out.println("threadName="+this.threadName); //测试使用
			while(queue.peek() != null&&flagMap.get(this.threadName).equals("0")){
				String queueMobile=queue.poll();
				if(queueMobile!=null){
				 String[] mobiles = queueMobile.split("&");
				 String mobile = mobiles[0];
				 String callid = UUID.randomUUID().toString();
				System.out.println(mobile+this.threadName);
				logger.info(mobile+this.threadName);
				String info="";
				if(mobiles.length>1){
					 info = dialer.originate(this.ProjectNumber, mobile, "",this.sProject+"|"+this.StrategyId+"|"+callid+"|"+mobile+"|"+this.AsrName+"|"+mobiles[1]+"|"+this.threadName);
					 System.out.println("###########"+mobiles[1]+"**********");	
				}else{
					 info = dialer.originate(this.ProjectNumber, mobile, "",this.sProject+"|"+this.StrategyId+"|"+callid+"|"+mobile+"|"+this.AsrName);
				}
				
					
					System.out.println("###########"+info+"**********");//USER_BUSY,TIMEOUT					
					logger.info("###########"+info+"**********");
					Util.SendKafka(callid,this.sProject,mobile,info,1,"","", producer,"");//向kafka发送拨通状态
					System.out.println("ProjectNumber="+this.ProjectNumber);
			
				}	
				
				
				Thread.sleep(1000);
			}
			
			Thread.sleep(1000);
			
		}
		
		 
		flagMap.remove(this.threadName);
	}catch(Exception e){
		flagMap.remove(this.threadName);
		Writer w = new StringWriter();
      	 e.printStackTrace(new PrintWriter(w));
      	  String s=w.toString();
		System.out.println(s+":"+this.threadName);	
		logger.warn(s+":"+this.threadName);
	}
	}

}
