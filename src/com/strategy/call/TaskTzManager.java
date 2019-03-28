package com.strategy.call;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;




import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.kafka.clients.producer.Producer;
import org.apache.log4j.Logger;
import org.freeswitch.esl.client.dialer;
import org.freeswitch.esl.client.dialerClient;
import org.freeswitch.esl.client.dialerMessage;
import org.freeswitch.esl.client.outbound.SocketClient;

import redis.clients.jedis.Jedis;

import com.sinovoice.jTTS.jTTS_ML;
import com.strategy.tools.JsonUtils;
import com.strategy.tools.KafkaUtils;
import com.strategy.tools.RedisUtils;
import com.strategy.tools.TtsUtils;
import com.strategy.tools.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.strategy.vo.Ai;
import com.ttsconvert.TtsConvert;




public class TaskTzManager  {
	 private static Logger logger = Logger.getLogger(TaskTzManager.class);
//	
	 private static  Producer<String, String> producer= KafkaUtils.getProducer();//kafka生产对象
	 private static  jTTS_ML jTTS = TtsUtils.getjTTS();//获取tts对象
	
	
//创建外拨响应消息，实现两个函数：
//onConnected呼叫建立后的处理
//createMessage创建class自己
	 static class clientMessage implements dialerMessage
	    {
		
			@Override
			public void onConnected(dialerClient client) {
				String Speech="";
				String Type="";
				
				String LyCode="";
				String lastLy="";
				String projectId="";
				String AsrName="";
				String BotName="";
				String callid="";
				String mobile="";
				String sb="";
			
				String [] sbs=null;
			
				
				String tts="";
				String Content="";
				String ThreadName="";
				
				
				String grammer="break";
				String params="break=8,silent=800";
				String filePath="/usr/local/freeswitch/sounds/en/us/callie/crm/" ;
				String url="http://10.168.1.4:3086/api/v1/bots/bostname/converse/callid";
				Jedis rs=RedisUtils.getJedis();
				String CurrentAIId="";
				int count=0;
				int amount=0;
				int deduc=0;
				String Evaluation="";
				String NextAIid="";
				String AiContent="";
				String jobnumber="";
				
				 BlockingQueue<String> queue = new LinkedBlockingQueue<String>();//存放不能判别要播放的语音
				try {
					client.exec.answer(); 
				
					
					Map eventHeaders=client.getEventHeaders();
					System.out.println("callerId="+eventHeaders.get("variable_effective_caller_id_number")); //variable_effective_caller_id_number
					System.out.println("callerNumber="+eventHeaders.get("Caller-Destination-Number"));
					jobnumber=eventHeaders.get("variable_effective_caller_id_number")+"";
					mobile=eventHeaders.get("Caller-Destination-Number")+"";
					logger.warn("eventHeaders="+eventHeaders);
					
					 List<Map<String, Object>> listproject = JsonUtils.parseJSON2List((String)rs.get("weviking_training_projectsetting_ivr"+mobile));
					 for(Map<String, Object> map:listproject){			
						 if(Util.compare_date(map.get("StartTime").toString(), map.get("EndTime").toString()) ) {	//该项目没有在拨打同时时间到了
							 projectId=map.get("ProjectId")+"";
							 AsrName=map.get("AsrName")+"";
							 BotName=map.get("BotName")+"";
						 }else{
							 projectId=map.get("ProjectId")+"";
							 sbs=projectId.split("-");
							 sb="crm/"+sbs[sbs.length-1]+"/";
							 BotName="";
							 AsrName="";
						 }
					 }
				
					if(!BotName.equals("")){
			//		projectId=userDate.split("\\|")[0];			
					 callid=UUID.randomUUID().toString();//userDate.split("\\|")[2];
		//			 mobile="5025";//userDate.split("\\|")[3];
		//			 AsrName=userDate.split("\\|")[4];
		//			 BotName=userDate.split("\\|")[5];
					 sbs=projectId.split("-");
					 sb="crm/"+sbs[sbs.length-1]+"/";
					 filePath=filePath+sbs[sbs.length-1]+"/";
		/*			 if(userDate.split("\\|").length>6){
						 tts=userDate.split("\\|")[6];
						 ThreadName=callid.split("-")[0];
					 }
		*/			
					System.out.println("$$$$$$$"+callid+"&&&&&&&");
				
				
					 
					JSONObject body = new JSONObject();
					body.put("type", "text");
					body.put("text","hello");
					String temp=url.replace("bostname",BotName);
					String urltemp=temp.replace("callid",callid);
					System.out.println("urltemp="+urltemp);
					System.out.println("body="+body.toString());
					String  result=Util.post(urltemp,body.toString());
					JSONObject jsonObject = JSONObject.fromObject(result);
					JSONArray responses = jsonObject.getJSONArray("responses");
					List<Map<String, Object>> list= JsonUtils.parseJSON2List(responses.toString());
					for(Map<String, Object> map:list){
						System.out.println(map.get("type"));
						System.out.println(map.get("text"));
						if ((map.get("text")+"").indexOf("lyCode|")!=-1){
							LyCode=(map.get("text")+"").split("\\|")[1];
							Type=(map.get("text")+"").split("\\|")[2];
							Content=(map.get("text")+"").split("\\|")[3];
							System.out.println("Type="+Type);
							System.out.println("Content="+Content);
							System.out.println("LyCode="+LyCode);
							break;
						}
					}						 
					File fileSrc = new File(filePath+LyCode+".wav");
					if(fileSrc.exists()){ //查找当前的录音文件存在
						Speech=client.exec.playAndDetectSpeech(sb+LyCode+".wav", AsrName,grammer,params);
						lastLy=LyCode;
					}else{
						System.out.println(Content);
						System.out.println(tts);
						System.out.println(filePath+ThreadName+".wav");
						Content=Util.replaceTTs(Content, tts);//替换content变量文本
						System.out.printf("Content= " + Content );
						logger.info("Content= " + Content );										
						int nRet=jTTS.jTTS_PlayToFile(Content, filePath+ThreadName+".wav", 6, null, 0, "", (long)0);
						System.out.printf("jTTS_PlayToFile nRet is " + nRet );
						logger.info("jTTS_PlayToFile nRet is " + nRet);
						Speech=client.exec.playAndDetectSpeech(filePath+ThreadName+".wav", AsrName,grammer,params);
						lastLy=ThreadName;
						}
							 
					Util.SendKafka(callid,projectId,jobnumber, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",lastLy, producer,"0","0","","");//向kafka发送拨打过程
						
					 while(true){
							Map map1=	JsonUtils.parseJSON2Map(Speech);
							System.out.println("speech="+map1.get("speech")+"");
							String speech = (map1.get("speech")+"").equals("")|| (map1.get("speech")+"").equals("null") ? "abcdefg":map1.get("speech")+"";
							JSONObject body1 = new JSONObject();
							body1.put("type", "text");
							body1.put("text",speech);
							String temp1=url.replace("bostname",BotName);
							String urltemp1=temp1.replace("callid",callid);
							System.out.println("urltemp1="+urltemp1);
							System.out.println("body1="+body1.toString());
							String  result1=Util.post(urltemp1,body1.toString());
							
							JSONObject jsonObject1 = JSONObject.fromObject(result1);
							JSONArray responses1 = jsonObject1.getJSONArray("responses");
							List<Map<String, Object>> list1= JsonUtils.parseJSON2List(responses1.toString());
							for(Map<String, Object> map:list1){
								
								if ((map.get("text")+"").indexOf("lyCode|")!=-1){
									LyCode=(map.get("text")+"").split("\\|")[1];
									Type=(map.get("text")+"").split("\\|")[2];
									Content=(map.get("text")+"").split("\\|")[3];
									System.out.println("Type="+Type);
									System.out.println("Content="+Content);
									System.out.println("LyCode="+LyCode);
									break;
								}
							}						
										
						
							
							
							if(Type.equals("结束")||Type.equals("异常")) {
								
								File fileSrc1 = new File(filePath+LyCode+".wav");
								if(!fileSrc1.exists()){
									Content=Util.replaceTTs(Content, tts);//替换content变量文本
									System.out.printf("Content= " + Content );
									logger.info("Content= " + Content );
									
									int nRet=jTTS.jTTS_PlayToFile(Content, filePath+ThreadName+".wav", 6, null, 0, "", (long)0);
									System.out.printf("jTTS_PlayToFile nRet is " + nRet );
									logger.info("jTTS_PlayToFile nRet is " + nRet);
							
									LyCode=ThreadName;
								}
								
								 break;
								
							}else  if(Type.equals("全局")||Type.equals("模块")){
								
								File fileSrc2= new File(filePath+LyCode+".wav");
								if(fileSrc2.exists()){
								 Speech=client.exec.playAndDetectSpeech(sb+LyCode+".wav", AsrName,grammer,params);					 
								 lastLy=LyCode;
								}else{
									Content=Util.replaceTTs(Content, tts);//替换content变量文本
									System.out.printf("Content= " + Content );
									logger.info("Content= " + Content );
									
									int nRet=jTTS.jTTS_PlayToFile(Content, filePath+ThreadName+".wav", 6, null, 0, "", (long)0);
									System.out.printf("jTTS_PlayToFile nRet is " + nRet );
									logger.info("jTTS_PlayToFile nRet is " + nRet);
									Speech=client.exec.playAndDetectSpeech(filePath+ThreadName+".wav", AsrName,grammer,params);
									lastLy=ThreadName;
								} 
								 Util.SendKafka(callid,projectId,jobnumber, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",LyCode+".wav", producer,"","",CurrentAIId,Evaluation);//向kafka发送拨打过程
								
								 
								 Type="";
								
								 continue;
							}else if(Type.equals("未匹配")){
								
								 if(count>2){
									 LyCode="forceend";
									break; 
								 }
								
								 Speech=client.exec.playAndDetectSpeech(sb+LyCode+".wav", AsrName,grammer,params); //这里需要随机选择三个不同听不清的提示语音
								
								 Util.SendKafka(callid,projectId,jobnumber, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",LyCode+".wav", producer,"","",CurrentAIId,Evaluation);//向kafka发送拨打过程
								
								 Type="";
							
								 count++;
								 continue;
							 }
								
								
								
								
					 }//while
					 
					 client.exec.playback(sb+LyCode+".wav");
					 Util.SendKafka(callid,projectId,jobnumber, "",2,"",LyCode+".wav", producer,"","",CurrentAIId,Evaluation);//向kafka发送拨打过程
				}else{
					 client.exec.playback(sb+"forceend"+".wav");
					 Util.SendKafka(callid,projectId,jobnumber, "",2,"","forceend"+".wav", producer,"","",CurrentAIId,Evaluation);//向kafka发送拨打过程
				}	 
		

		        } catch (Exception e) {
		        	System.out.println("disconnect");
		        	 Writer w = new StringWriter();
		           	 e.printStackTrace(new PrintWriter(w));
		           	  String s=w.toString();
		        	logger.error(s);
		        	
		        } 
				finally{
					try{
						RedisUtils.returnResource(rs);
						client.exec.hangup(null);
						logger.info("onConnected hangup");
						 Util.SendKafka(callid,projectId,jobnumber, "onConnected hangup",3,"","", producer,"","",CurrentAIId,Evaluation);//向kafka发送挂断
					}catch(Exception e){
					System.out.println("disconnect");
		        	logger.info("onConnected could not hangup",e);
		        	 Util.SendKafka(callid,projectId,jobnumber, "onConnected could not hangup",3,"","", producer,"","",CurrentAIId,Evaluation);//向kafka发送挂断
					}
				}
			}

			@Override
			public dialerMessage createMessage() {
				return new clientMessage();
			}
	    	
	    }
	 
	 
	 
	 
	 
	 
	public static void main(String[] args){
		
	//	Thread consumerThread= new Thread(new CallConsumerTask_Kafka());
    //    consumerThread.start();//启动kafka订阅线程
		
		 
		Jedis rs=RedisUtils.getJedis();//获取redis连接池里的连接对象
		
	//	final ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
	//	service.scheduleWithFixedDelay(new TtsConvert(jTTS),0,1000*60,TimeUnit.MILLISECONDS);//启动tts转换线程
      
		final SocketClient outboundServer = new SocketClient(
                new InetSocketAddress("127.0.0.1", 8025),dialer.createClient(new clientMessage())); //8025 8022
        outboundServer.startAsync();//启动呼入等待线程          
        
		
		System.out.println(rs.ping());
	
	/*	Map threadMap = new HashMap<>();
		Map queueMap = new HashMap<>();
		Map flagMap = new HashMap<>();
		Map countMap = new HashMap<>();
		Map periodMap = new  HashMap<>();
		
	//获取拨打对象	
		if(dialer.init("127.0.0.1", 8021,60)){ 
			logger.warn("dialer.init ok");
		}else{
			logger.error("dialer.init failed!");
		}
	*/	
		try{
			while(true){
				
				 Thread.sleep(6000);
				}
		}	catch (Exception ex){
		Writer w = new StringWriter();
       	 ex.printStackTrace(new PrintWriter(w));
       	  String s=w.toString();
		System.out.println(s);	
		logger.warn(s);
	} finally{
		RedisUtils.returnResource(rs);
		outboundServer.stopAsync();
 		logger.warn("呼入等待线程  exit");
 		System.out.println("呼入等待线程  exit");
 		producer.close();
 		logger.warn("kafka生产对象  关闭");
 		logger.warn("jTTS_End="+jTTS.jTTS_End()+"tts对象 关闭");
		System.out.println("kafka生产对象  关闭");
		
		
	}
	
	
	}

}
