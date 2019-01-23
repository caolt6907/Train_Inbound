package com.strategy.tools;



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.strategy.vo.SubjectBack;
















import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.strategy.call.TaskTzManager;
import com.strategy.vo.CallResultModel;





public class Util {
	 private static Logger logger = Logger.getLogger(Util.class);	
	 public static SimpleDateFormat sdf1 = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
	 
	 public static String getNowTime() {
			String time = sdf1.format(new Date());
			return time;
		}	 
	 

public static boolean compare_date(String stDATE, String endDATE) {
        
	//return true;
	
        DateFormat df = new SimpleDateFormat("HH:mm");
        String DATE=df.format(new Date());
       
        try {
            Date dt1 = df.parse(stDATE);
            Date dt2 = df.parse(endDATE);
            Date dt = df.parse(DATE);
          
            if (dt.getTime()>=dt1.getTime() && dt.getTime()<=dt2.getTime()) {
               
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
		return false;
        
    }

public static String  InsertMobiles (String mobiles,Queue queue){
	int i;
	mobiles=mobiles.replace("\"", "");
	if(queue.size()>=10) return mobiles;
	if (!mobiles.equals("")){
		
		String[] mobile = mobiles.split(",");
	
	/*	for(String str:mobile){
		queue.offer(str);
		}	
		*/
		for(i=0;i<mobile.length&&queue.size()<10;i++){
			queue.offer(mobile[i]);
		}
		if(i>=mobile.length){
			mobiles="";
		}else{
			mobiles="";
			for(i=i;i<mobile.length;i++){
				mobiles=mobiles+mobile[i]+",";
			}
			mobiles=mobiles.substring(0, mobiles.length()-1);
		}
	}
	return mobiles;
}

public static String  ReturnMobiles(String mobiles,Queue queue){
	String strs="";
	mobiles=mobiles.replace("\"", "");
	 while (queue.peek() != null ) {
		 strs=strs+queue.poll()+",";
		}
	 if(strs.equals("")){
		 if (mobiles.equals("")){
			 strs="";
		 }else{
			 strs=mobiles;
		 }
	 }else{
		 strs=strs.substring(0, strs.length()-1);
		 if (mobiles.equals("")){
			 
		 }else{
			 strs=mobiles+","+strs;
		 } 
	 }
	
	 return strs;
}

public static boolean Regular(String txt,String Speech){
	Matcher m = Pattern.compile(txt).matcher(Speech);
	boolean flag=false;
	if(m.find()){
		flag=true;
	}else{
		flag=false;
	}
	return flag;
}

public static String CallReturnModelJson(CallResultModel callResultModel){
	List list = new ArrayList();
	list.add(callResultModel);
	String str=JsonUtils.listToJson(list);
	
	return str;
}

public static void SendKafka(String CallId,String ProjectId,String Phone,String Content,int Type,String UserSpeackText,String PalyRecordinCode,Producer<String, String> producer,String Intention){
	 CallResultModel callResultModel= new CallResultModel();
	   callResultModel.setCallId(CallId);
	//   callResultModel.setUuid(Uuid);
	   callResultModel.setProjectId(ProjectId);
	   callResultModel.setPhone(Phone);
	   callResultModel.setContent(Content);		   
	   callResultModel.setType(Type);
	   callResultModel.setUserSpeackText(UserSpeackText);
	   callResultModel.setPalyRecordinCode(PalyRecordinCode);
	   callResultModel.setIntention(Intention);
	   callResultModel.setTime(getNowTime());
	  String str=Util.CallReturnModelJson(callResultModel);
	producer.send(new ProducerRecord<String, String>("test", str));
	logger.info(str);
}
public static String getAuth(String ak,String sk){
	String authHost="https://aip.baidubce.com/oauth/2.0/token?";
	String getAccessToKenUrl=authHost
			// 1. grant_type为固定参数
            + "grant_type=client_credentials"
            // 2. 官网获取的 API Key
            + "&client_id=" + ak
            // 3. 官网获取的 Secret Key
            + "&client_secret=" + sk;
	try{
		URL realUrl=new URL(getAccessToKenUrl);
		HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection(); 
		connection.setRequestMethod("GET");
		connection.connect();
		 // 获取所有响应头字段
		Map<String, List<String>> map = connection.getHeaderFields();
		 // 遍历所有的响应头字段
		for(String key : map.keySet()){
			System.err.println(key + "--->" + map.get(key));
		}
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String result="";
		String line="";
		while ((line=in.readLine())!=null){
			result += line;
		}
		   /**
         * 返回结果示例
         */
        System.err.println("result:" + result);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("access_token", result);
        String access_token = jsonObject.getString("access_token");
        return access_token;
	
	}catch(Exception e){
		System.out.println("获取token失败！");
		logger.error("获取token失败！",e);
	}
	
	
	return null;
	
}
public static String getAuth(){
	String clientId="fb9c83e710db4e6293895005f18cf48f";	
	String clientSecret="e3482fb6afae49e39546d15b86c35dfe";
	
	
	return getAuth(clientId, clientSecret);
}

public static String escapeExprSpecialWord(String keyword) {
    if (StringUtils.isNotBlank(keyword)) {
        String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
        for (String key : fbsArr) {
            if (keyword.contains(key)) {
                keyword = keyword.replace(key, "\\" + key);
            }
        }
    }
    return keyword;
}

public static Map sortSubjectBack(List<SubjectBack> list){
	Map map = new HashMap();
    Collections.sort(list);  
    for (SubjectBack subjectBack : list) {  
    	map.put("ResultWay", subjectBack.getResultWay());
    	map.put("Intention", subjectBack.getIntention());
       break;
    }  
    return map;
}  

public static String replaceTTs(String content,String ttsString){
	String tts[] = ttsString.split("&");
	for(int i=0;i<tts.length;i++){
		String[] map =tts[i].split("=");
		content=content.replace("{"+map[0]+"}", map[1]);
		
	}
	
	return content;
}


 

	public static void main(String[] args){
	String tts="姓名=曹连霆";
	String content="您好{姓名}，我们是一家做电气自动化的工业品网上便利店，叫易买工品。能开16%的增值税发票。请问您公司平常会用到PLC、伺服、触摸屏、变频器或电气辅料吗？";
	System.out.println(replaceTTs(content,tts));
	
	}
}
