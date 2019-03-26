package com.strategy.tools;





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
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

import com.strategy.vo.Ai;








































import com.strategy.vo.PostStart;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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
	if(queue.size()>=50) return mobiles;
	if (!mobiles.equals("")){
		
		String[] mobile = mobiles.split(",");
	
	/*	for(String str:mobile){
		queue.offer(str);
		}	
		*/
		for(i=0;i<mobile.length&&queue.size()<100;i++){
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

public static String CallReturnModelJson(Object vo){
	List list = new ArrayList();
	list.add(vo);
	String str=JsonUtils.listToJson(list);
	
	return str;
}

public static void SendKafka(String CallId,String ProjectId,String Phone,String Content,int Type,String UserSpeackText,String PalyRecordinCode,Producer<String, String> producer,String Deduc,String Amount,String aiid,String Evaluation){
	 CallResultModel callResultModel= new CallResultModel();
	   callResultModel.setCallId(CallId);
	//   callResultModel.setUuid(Uuid);
	   callResultModel.setProjectId(ProjectId);
	   callResultModel.setPhone(Phone);
	   callResultModel.setContent(Content);		   
	   callResultModel.setType(Type);
	   callResultModel.setUserSpeackText(UserSpeackText);
	   callResultModel.setPalyRecordinCode(PalyRecordinCode);
	   callResultModel.setDeduc(Deduc);
	   callResultModel.setAmount(Amount);
	   callResultModel.setTime(getNowTime());
	   callResultModel.setAiid(aiid);
	   callResultModel.setEvaluation(Evaluation);
	  String str=Util.CallReturnModelJson(callResultModel);
	producer.send(new ProducerRecord<String, String>("train", str));
	logger.info(str);
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



public static String replaceTTs(String content,String ttsString){
	String tts[] = ttsString.split("&");
	for(int i=0;i<tts.length;i++){
		String[] map =tts[i].split("=");
		content=content.replace("{"+map[0]+"}", map[1]);
		
	}
	
	return content;
}

public static boolean compareCurrentAIId(String map ,String aid){
	if(map==null) return false;
	if(map.equals(""))return false;
	if(aid==null) return false;
	if(aid.equals("")) return false;
	String[] currentAIIDs = map.split("\\|");
	for(int i=0;i<currentAIIDs.length;i++){
		if(currentAIIDs[i].equals(aid)) return true;
	}
	return false;
}


public static  String post(String url, String reportData) {
	
	HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(url);
  //  JSONObject response = null;
    String result="";
    try {
	
    	post.addHeader("Content-type","application/json; charset=utf-8");
    	post.setHeader("Accept", "application/json");
    	post.setEntity(new StringEntity(reportData, Charset.forName("UTF-8")));
    	

        HttpResponse res = client.execute(post);
        System.out.println(res.getStatusLine().getStatusCode());
        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = res.getEntity();
             result = EntityUtils.toString(entity);
        //    response = JSONObject.parseObject(result);
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    return result;

}
 

	public static void main(String[] args) throws UnsupportedEncodingException{
		
			JSONObject body = new JSONObject();
		body.put("type", "text");
		body.put("speech",null);
		
		Map map1=	JsonUtils.parseJSON2Map(body.toString());
		System.out.println("speech="+(map1.get("speech")+"").length());
	//	String speech="";
		String speech = (map1.get("speech")+"").equals("")|| (map1.get("speech")+"").equals("null") ? "abcdefg":map1.get("speech")+"";
		System.out.println(speech);
	/*	if((map1.get("speech")+"").equals("")){
			 speech="abcdefg";
		}else{
			 speech=map1.get("speech")+"";
		}
	//	String speech = (map1.get("speech")+"")==""|| (map1.get("speech")+"")==null ? "abcdefg":map1.get("speech")+"";
		System.out.println(speech);
	*/
		String bostname="trainingcustomer";
		String callid="1c6c6719-3e30-44b1-bc70-d3b10bc4d71b";
		String url="http://112.64.145.218:3086/api/v1/bots/bostname/converse/callid";
		String temp=url.replace("bostname",bostname);
		url=temp.replace("callid",callid);
	
		JSONObject body1 = new JSONObject();
		body1.put("type", "text");
		body1.put("text",speech);
	
		String  result=post(url,body1.toString());
		System.out.println(result);
		
	/*	String temp1=url.replace("bostname",bostname);
		url=temp1.replace("callid",callid);
	
		JSONObject body1 = new JSONObject();
		body1.put("type", "start");
		body1.put("text","收到");
		String  result1=post(url,body1.toString());
		System.out.println(result1);
	//	PostStart postStart= new PostStart();
	/*	postStart.setType("start");
		postStart.setTxt("开始");
		String str=Util.CallReturnModelJson(postStart);
		System.out.println(post(url,str));*/
	/*	JSONObject body = new JSONObject();
		body.put("type", "start");
		body.put("text","开始");
		String  result=post(url,body.toString());
		
		System.out.println(result);
		 JSONObject jsonObject = JSONObject.fromObject(result);
		 JSONArray responses = jsonObject.getJSONArray("responses");
	
		List<Map<String, Object>> list= JsonUtils.parseJSON2List(responses.toString());
				for(Map<String, Object> map:list){
					System.out.println(map.get("type"));
					System.out.println(map.get("text"));
				}
		
	*/
		
		
	
	}	
	
}
