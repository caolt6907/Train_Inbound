package com.strategy.call;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

import com.strategy.vo.SubjectBack;
import com.ttsconvert.TtsConvert;




public class TaskTzManager  {
	 private static Logger logger = Logger.getLogger(TaskTzManager.class);
//	
	 private static  Producer<String, String> producer= KafkaUtils.getProducer();//kafka生产对象
	 private static  jTTS_ML jTTS = TtsUtils.getjTTS();//获取tts对象
	
	// private static 	String filePath="/usr/local/freeswitch/sounds/en/us/callie/crm/" ;
//创建外拨响应消息，实现两个函数：
//onConnected呼叫建立后的处理
//createMessage创建class自己
	 static class clientMessage implements dialerMessage
	    {

			@Override
			public void onConnected(dialerClient client) {
				String Speech="";
				String Type="";
				String NextSubjectId="";
				String LyCode="";
				String lastLy="";
				int count=0;
				String projectId="";
				String StrategyId="";
				String callid="";
				String mobile="";
				String sb="";
				String Intention="";
				String [] sbs=null;
				String SubjectId="";
				String uuid="";
				String tts="";
				String Content="";
				String ThreadName="";
				String AsrName="";
				String CanBroke="";
				String grammer="";
				String params="";
				String filePath="/usr/local/freeswitch/sounds/en/us/callie/crm/" ;
				String FlagNextSubjectId="";
				Jedis rs=RedisUtils.getJedis();
				 BlockingQueue<String> queue = new LinkedBlockingQueue<String>();//存放不能判别要播放的语音
				try {
					client.exec.answer(); 
					String userDate=client.getUserData();
					logger.warn("$$$$$$$"+userDate+"&&&&&&&");
			//		System.out.println("$$$$$$$"+userDate+"&&&&&&&");
					uuid=client.getUuid();
					 projectId=userDate.split("\\|")[0];
					 StrategyId=userDate.split("\\|")[1];
					 callid=userDate.split("\\|")[2];
					 mobile=userDate.split("\\|")[3];
					 AsrName=userDate.split("\\|")[4];
					 sbs=projectId.split("-");
					 sb="crm/"+sbs[sbs.length-1]+"/";
					 filePath=filePath+sbs[sbs.length-1]+"/";
					 if(userDate.split("\\|").length>5){
						 tts=userDate.split("\\|")[5];
						 ThreadName=callid.split("-")[0];
					 }
					
					System.out.println("$$$$$$$"+StrategyId+"&&&&&&&");
				//	System.out.println("StrategyId="+"weviking_callcenter_StrategySubject_"+StrategyId);
					 List<Map<String, Object>> list = JsonUtils.parseJSON2List((String)rs.get("weviking_callcenter_StrategySubject_"+StrategyId));
					 for(Map<String, Object> map:list){
						 if((map.get("Type")+"").equals("未匹配")){
							 queue.offer(map.get("LyCode")+"");
							
						 }
					 }
					 for(Map<String, Object> map:list){
						 if((map.get("Type")+"").equals("开始")){
							 System.out.println(map.get("LyCode"));
							/* for(int i=0;i<1;++i){
								  Speech=client.exec.playAndDetectSpeech(sb+map.get("LyCode")+".wav", AsrName,"",""); //不允许打断
								  Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",map.get("LyCode")+".wav", producer,"");//向kafka发送拨打过程
							 }
							 */
							 
							 File fileSrc = new File(filePath+map.get("LyCode")+".wav");
								if(fileSrc.exists()){ //查找当前的录音文件存在
									Speech=client.exec.playAndDetectSpeech(sb+map.get("LyCode")+".wav", AsrName,grammer,params);
									lastLy=map.get("LyCode")+"";
								}else{
									System.out.println(map.get("Content")+"");
									System.out.println(tts);
									System.out.println(filePath+ThreadName+".wav");
									Content=Util.replaceTTs(map.get("Content")+"", tts);//替换content变量文本
									System.out.printf("Content= " + Content );
									logger.info("Content= " + Content );
									
									int nRet=jTTS.jTTS_PlayToFile(Content, filePath+ThreadName+".wav", 6, null, 0, "", (long)0);
									System.out.printf("jTTS_PlayToFile nRet is " + nRet );
									logger.info("jTTS_PlayToFile nRet is " + nRet);
									Speech=client.exec.playAndDetectSpeech(filePath+ThreadName+".wav", AsrName,grammer,params);
									lastLy=ThreadName;
								}
							 
							 Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",lastLy, producer,"");//向kafka发送拨打过程
							 
							 SubjectId=map.get("SubjectId")+"";
							 break;
						 }
						 
					 }
					 
					
					 List<Map<String, Object>> listback = JsonUtils.parseJSON2List((String)rs.get("weviking_callcenter_StrategySubjectBack_"+StrategyId));
					 while(true){
					   if(FlagNextSubjectId.equals("")){
						Map map1=	JsonUtils.parseJSON2Map(Speech);
						System.out.println("Speech:"+Speech );
						logger.warn("Speech:"+Speech);
						for(Map<String, Object> map:listback){
						 String FeedBackTxt=map.get("FeedBackTxt")+"";
						 FeedBackTxt=FeedBackTxt.replace("\"", "");
						 FeedBackTxt=FeedBackTxt.replaceAll("\\\\\\\\", "\\\\");
						 boolean subjectidFlag=false;//判别话术里的subjectid是否包含于back里的subjectid里
						 if((map.get("SubjectId")+"").equals("")){ //back 里的subjectid为空，就不判
							 subjectidFlag=true;
						 }else{
							 subjectidFlag=Pattern.compile(map.get("SubjectId")+"").matcher(SubjectId).find();
						 }
						 Matcher m = Pattern.compile(FeedBackTxt).matcher(map1.get("speech")+"");
						 
						boolean mflag=m.find();
						
						
						
						 
						 if(mflag&&subjectidFlag&&!((map.get("SubjectId")+"").equals(""))){  //判断subjectid非空情况下有几个back,这个是主流程
							List<SubjectBack> subjectidList = new ArrayList<SubjectBack>();
							 for(Map<String, Object> map3:listback){
								 String FeedBackTxt3=map3.get("FeedBackTxt")+"";
								 FeedBackTxt=FeedBackTxt3.replace("\"", "");
								 FeedBackTxt=FeedBackTxt.replaceAll("\\\\\\\\", "\\\\");
								 if(Pattern.compile(FeedBackTxt).matcher(map1.get("speech")+"").find()
										 &&Pattern.compile(map3.get("SubjectId")+"").matcher(SubjectId).find()){
									System.out.println("if:"+SubjectId);
									
									 SubjectBack subjectBack= new SubjectBack();
									 subjectBack.setResultWay(map3.get("ResultWay")+"");
									 subjectBack.setIntention(map3.get("Intention")+"");
									 subjectBack.setPriority(Integer.valueOf(map3.get("Priority")+""));									
									 subjectidList.add(subjectBack);
								 }
							 }
							 System.out.println(subjectidList.size());
							 Map map4=Util.sortSubjectBack(subjectidList);
							
							 map.put("ResultWay",map4.get("ResultWay"));
							 map.put("Intention",map4.get("Intention"));
							 System.out.println("ResultWay:="+map4.get("ResultWay"));
							 System.out.println("Intention:="+map4.get("Intention"));
							 System.out.println("Priority:="+map4.get("Priority"));
							 
						 }
						 
						 
						 
						 if(mflag&&subjectidFlag){
							 for(Map<String, Object> map2:list){
								 if((map2.get("SubjectId")+"").equals(map.get("ResultWay")+"")){
									 System.out.println(map2.get("LyCode"));
									 System.out.println(map2.get("Type"));
									 System.out.println(map2.get("SubjectId"));
									 LyCode=map2.get("LyCode")+"";
									 Type=map2.get("Type")+"";
									 NextSubjectId=map2.get("NextSubjectId")+"";
									 Intention=map.get("Intention")+"";
									 Content=map2.get("Content")+"";
									 CanBroke=map2.get("CanBroke")+"";
									 if(CanBroke.equals("false")){
										 grammer="";
										 params="";
									 }else{
										 grammer="break";
										 params="break=2";
									 }
									 if(Type.equals("开始")||Type.equals("内容")){
										 SubjectId=map2.get("SubjectId")+"";
									 }
									 break;
								 } 
							 }
							
						 }
						 if(!Type.equals("")){
							 break;
						 }
					 }//for
					}
					if(Type.equals("内容")){
						File fileSrc = new File(filePath+LyCode+".wav");
						if(fileSrc.exists()){
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
						 Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",lastLy+".wav", producer,Intention);//向kafka发送拨打过程
						 Type="";
						 FlagNextSubjectId="";
						 continue;
					}else  if(Type.equals("全局")){
						File fileSrc = new File(filePath+LyCode+".wav");
						if(fileSrc.exists()){
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
						 Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",lastLy+".wav", producer,Intention);//向kafka发送拨打过程
						 Type="";
						 FlagNextSubjectId="";
						 continue;
					}else if(Type.equals("结束")){
						File fileSrc = new File(filePath+LyCode+".wav");
						if(!fileSrc.exists()){
							Content=Util.replaceTTs(Content, tts);//替换content变量文本
							System.out.printf("Content= " + Content );
							logger.info("Content= " + Content );
							
							int nRet=jTTS.jTTS_PlayToFile(Content, filePath+ThreadName+".wav", 6, null, 0, "", (long)0);
							System.out.printf("jTTS_PlayToFile nRet is " + nRet );
							logger.info("jTTS_PlayToFile nRet is " + nRet);
					
							LyCode=ThreadName;
						}
						 break;
					}else if(Type.equals("")){
						 System.out.println("count="+count);
						 if(count>2){
							 LyCode="forceend";
							break; 
						 }
						 String ly=queue.poll();
						 Speech=client.exec.playAndDetectSpeech(sb+ly+".wav", AsrName,grammer,params); //这里需要随机选择三个不同听不清的提示语音
						 queue.offer(ly);
						 Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",ly+".wav", producer,"未识别");//向kafka发送拨打过程
						
						 Type="";
						 FlagNextSubjectId="";
						 count++;
						 continue;
					 }else if(Type.equals("跳转")){
						 File fileSrc = new File(filePath+LyCode+".wav");
						 if(fileSrc.exists()){
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
						
						 Util.SendKafka(callid,projectId,mobile, "",2,JsonUtils.parseJSON2Map(Speech).get("speech")+"",lastLy+".wav", producer,Intention);//向kafka发送拨打过程
						
						
							
							 for(Map<String, Object> map2:list){
								 if((map2.get("SubjectId")+"").equals(NextSubjectId)){
									 System.out.println("NextSubjectId="+NextSubjectId);
									 System.out.println("SubjectId="+map2.get("SubjectId")+"");
									 FlagNextSubjectId=NextSubjectId;
									 LyCode=map2.get("LyCode")+"";
									 Type=map2.get("Type")+"";
									 NextSubjectId=map2.get("NextSubjectId")+"";
									 Content=map2.get("Content")+"";
									 CanBroke=map2.get("CanBroke")+"";
									 if(CanBroke.equals("false")){
										 grammer="";
										 params="";
									 }else{
										 grammer="break";
										 params="break=2";
									 }
									 if(Type.equals("开始")||Type.equals("内容")){
										 SubjectId=map2.get("SubjectId")+"";
									 }
									 break;
									 }
								 }
				
						 continue;
					 }
				 }
				
					 
					client.exec.playback(sb+LyCode+".wav");
					 Util.SendKafka(callid,projectId,mobile, "",2,"",LyCode+".wav", producer,Intention);//向kafka发送拨打过程

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
						 Util.SendKafka(callid,projectId,mobile, "onConnected hangup",3,"","", producer,"");//向kafka发送挂断
					}catch(Exception e){
					System.out.println("disconnect");
		        	logger.info("onConnected could not hangup",e);
		        	 Util.SendKafka(callid,projectId,mobile, "onConnected could not hangup",3,"","", producer,"");//向kafka发送挂断
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
		
		final ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
		service.scheduleWithFixedDelay(new TtsConvert(jTTS),0,1000*60,TimeUnit.MILLISECONDS);//启动tts转换线程
      
		final SocketClient outboundServer = new SocketClient(
                new InetSocketAddress("127.0.0.1", 8022),dialer.createClient(new clientMessage()));
        outboundServer.startAsync();//启动呼入等待线程          
        
		
		System.out.println(rs.ping());
	
		Map threadMap = new HashMap<>();
		Map queueMap = new HashMap<>();
		Map flagMap = new HashMap<>();
	//	Map threadSizeMap = new  HashMap<>();
		
	//获取拨打对象	
		if(dialer.init("127.0.0.1", 8021,30)){
			logger.warn("dialer.init ok");
		}else{
			logger.error("dialer.init failed!");
		}
		
	try{
		while(true){
		 List<Map<String, Object>> list = JsonUtils.parseJSON2List((String)rs.get("weviking_callcenter_callsetting"));
		 for(Map<String, Object> map:list){			
			 if(queueMap.get(map.get("ProjectId")+"_queue")==null && Util.compare_date(map.get("StartTime").toString(), map.get("EndTime").toString()) ) {	//该项目没有在拨打同时时间到了			
			// if(rs.get(map.get("ProjectId")+"_status")==null && Util.compare_date(map.get("StartTime").toString(), map.get("EndTime").toString()) ) {	//该项目没有在拨打同时时间到了			
				 BlockingQueue<String> queue = new LinkedBlockingQueue<String>(); //生成一个项目的拨打号码队列
			
				 for(int i=0;i<Integer.valueOf(map.get("RobotNum").toString());i++){
					 flagMap.put(map.get("ProjectId")+"_thread"+i, "0"); //设置拨打线程运行标志
					 Thread thread = new Thread(new CallTableTask_Robot(queue,map.get("ProjectNumber")+"",flagMap,map.get("ProjectId")+"_thread"+i,map.get("StrategyId")+"",map.get("AsrName")+"",producer)); //生成拨打线程
					 thread.start();
					 threadMap.put(map.get("ProjectId")+"_thread"+i, thread); //把拨打线程对象放入线程map中，让主程序可以控制拨打线程
					 Thread.sleep(30);
				 }//先生成拨打线程，再往队列里添加数据。
			
				 if(rs.get(map.get("MobilesId")+"").equals("")||rs.get(map.get("MobilesId")+"")==null){
					 Util.SendKafka("",map.get("ProjectId")+"","", "",0,"","", producer,"");//向kafka请求添加手机号
				 }else{
					String mobiles =Util.InsertMobiles(rs.get(map.get("MobilesId")+"")+"",queue); //往队列里添加从redis读取的手机号
					 rs.set(map.get("MobilesId")+"", mobiles);//返回redis里手机号码
				 }
				 queueMap.put(map.get("ProjectId")+"_queue", queue); //把队列对象放入队列map中，让主线成可以往里添加手机号
				 
			
				 
			 }
			 if(!(queueMap.get(map.get("ProjectId")+"_queue")==null)){ 
			 		if(Util.compare_date(map.get("StartTime").toString(), map.get("EndTime").toString())){//该项目已开始拨打，同时在工作时间内
			 			 if(rs.get(map.get("MobilesId")+"").equals("")||rs.get(map.get("MobilesId")+"")==null){
								Util.SendKafka("",map.get("ProjectId")+"","", "",0,"","", producer,"");//向kafka请求添加手机号
						}else{
							String mobiles =Util.InsertMobiles(rs.get(map.get("MobilesId")+"")+"", (Queue)queueMap.get(map.get("ProjectId")+"_queue"));
							rs.set(map.get("MobilesId")+"", mobiles);//返回redis里手机号码
							 }
			 			
			 			
			 		}else{
			 			 for(int i=0;i<Integer.valueOf(map.get("RobotNum").toString());i++){
			 				flagMap.put(map.get("ProjectId")+"_thread"+i, "1"); //设置拨打线程结束标志
			 				threadMap.remove(map.get("ProjectId")+"_thread"+i);//在线程map咯删除线程对象
			 			
			 			 }	
			 			 System.out.println(rs.get(map.get("MobilesId")+"")+"");
			 		
			 			 
			 			rs.set(map.get("MobilesId")+"",Util.ReturnMobiles(rs.get(map.get("MobilesId")+"")+"",(Queue)queueMap.get(map.get("ProjectId")+"_queue"))); 
			 			queueMap.remove(map.get("ProjectId")+"_queue");
			 			System.out.println("线程map长度："+threadMap.size());
			 			System.out.println("队列map长度："+queueMap.size());
			 		
			 		
			 		}
			 } 
		 }//for
		 Thread.sleep(6000);
		}
	} catch (Exception ex){
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
