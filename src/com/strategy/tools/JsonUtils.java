package com.strategy.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import javax.servlet.http.HttpServletResponse;




import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.functors.ForClosure;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import redis.clients.jedis.Jedis;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
















import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.strategy.vo.CallResultModel;

/**
 * json数据转换类
 * 
 * @author Administrator
 * 
 */
public class JsonUtils {
	/**
	 * 
	 * @param object
	 * @return
	 */
//	public static String beanToJson(Object object) {
//		Gson gson=new Gson();  
//	    return gson.toJson(object);  
//	}
	public static String beanToJson(Object o) {
		String str = null;
		GsonBuilder bulider = new GsonBuilder();
		bulider.setDateFormat("yyyy-MM-dd HH:mm:ss");
		Gson gson = bulider.create();
		str = gson.toJson(o);
		return str;
	}
	
	/***
	 * 
	 * @param src
	 * @param typeOfSrc
	 * @return
	 */
	public static   String beanToJson(Object obj,Type type) {
		  Gson gson=new Gson();  
		  return gson.toJson(obj,type);  
	}
	
	public static String beanWithGsonToJson(Object obj){
		String str = null;
		GsonBuilder bulider = new GsonBuilder();
		bulider.excludeFieldsWithoutExposeAnnotation(); //不转换没有 @Expose 注解的字段
		Gson gson = bulider.create();
		str = gson.toJson(obj); 
		System.out.println(str);
		return str;
	}
	
	public static String beanWithGsonToJson(Object obj,Type type){
		String str = null;
		GsonBuilder bulider = new GsonBuilder();
		bulider.excludeFieldsWithoutExposeAnnotation(); //不转换没有 @Expose 注解的字段
		Gson gson = bulider.create();
		str = gson.toJson(obj,type); 
		System.out.println(str);
		return str;
	}
	
	public static String listToJson(List list){
		Gson gson=new Gson();  
		return gson.toJson(list);  
	}
	
	public static  String listToJson(List list, Type type) {
		Gson gson=new Gson();  
		return gson.toJson(list,type); 
	}
	
	public static String listWithGsonToJson(List list, Type type){
		String str = null;
		GsonBuilder bulider = new GsonBuilder();
		bulider.excludeFieldsWithoutExposeAnnotation(); //不转换没有 @Expose 注解的字段
		Gson gson = bulider.create();
		str = gson.toJson( list,  type);
		System.out.println(str);
		return str; 
	}
	
	/**
	 * action中json数据转换
	 * 
	 * @param result
	 *            需要转换成json数据的目标
	 */
/*	public static  void toGson(Object result) {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setCharacterEncoding("utf-8");
		PrintWriter out = null;
		try {
			Gson gson = new Gson();
			String results = gson.toJson(result);
			out = response.getWriter();
			// 用于页面分布时的数据提取
			// result = "{rowCont:" + count + ",result:" + result + "}";
			out.print(results);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	@SuppressWarnings("unchecked")
	public static  Object jsonToBean(String json, Class clzz) {

		GsonBuilder bulider = new GsonBuilder();
		// buliderc.excludeFieldsWithoutExposeAnnotation();
		Gson gson = bulider.create();
		return gson.fromJson(json, clzz);
	}

	public static  Object jsonToBean(String json, Type typeof) {
		GsonBuilder bulider = new GsonBuilder();
		Gson gson = bulider.create();
		return gson.fromJson(json, typeof);
	}
	
	/**
	 * 
	 * @param json
	 */
/*	public static void outWriter(String json) {
		PrintWriter printWriter = null;
		try {
			HttpServletResponse response = ServletActionContext.getResponse();
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			  printWriter = response.getWriter();
			printWriter.write(json);

		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			 printWriter.close();
		}
	}
	*/
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> parseJSON2List(String jsonStr) {
		JSONArray jsonArr = JSONArray.fromObject(jsonStr);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Iterator<JSONObject> it = jsonArr.iterator();
		while (it.hasNext()) {
			JSONObject json2 = it.next();
			list.add(parseJSON2Map(json2.toString()));
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			// 如果内层还是数组的话，继续解析
			if (v instanceof JSONArray) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Iterator<JSONObject> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					JSONObject json2 = it.next();
					list.add(parseJSON2Map(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}
	
	public static List<Map<String, Object>> getListByUrl(String url) {
		try {
			// 通过HTTP获取JSON数据
			InputStream in = new URL(url).openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return parseJSON2List(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> getMapByUrl(String url) {
		try {
			// 通过HTTP获取JSON数据
			InputStream in = new URL(url).openStream();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

			return parseJSON2Map(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Object> parseJSON2ObjectList(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			// 如果内层还是数组的话，继续解析
			if (v instanceof JSONArray) {
				List<Object> list = new ArrayList<Object>();
				Iterator<JSONObject> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					Object json2 = it.next();
					list.add(json2.toString());
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}
	
	public  static void  main(String[] args) throws InterruptedException{
		Map map1=	JsonUtils.parseJSON2Map("null");
		System.out.println(map1.get("speech")+"");
		 Matcher m = Pattern.compile("123").matcher(map1.get("speech")+"");
		System.out.print(m.find());
	
	/*	String StrategyId="bd31f295-626e-4017-8a3f-752af55ba77e";
	
		 List<Map<String, Object>> list = JsonUtils.parseJSON2List((String)rs.get("weviking_callcenter_StrategySubject_"+StrategyId));
		 for(Map<String, Object> map:list){
			 if((map.get("Type")+"").equals("开始")){
				 System.out.println(map.get("LyCode"));
			 }
		 }
		 List<Map<String, Object>> listback = JsonUtils.parseJSON2List((String)rs.get("weviking_callcenter_StrategySubjectBack_"+StrategyId));
		 for(Map<String, Object> map:listback){
	
			 String ttString=map.get("FeedBackTxt")+"";
			 String tt=ttString.replace("\"", "");
			 Matcher m = Pattern.compile(tt).matcher("你们是从哪里打电话过来的");
			 if(m.find()){
				 System.out.println(map.get("ResultWay"));
				 for(Map<String, Object> map1:list){
					 if((map1.get("SubjectId")+"").equals(map.get("ResultWay")+"")){
						 System.out.println(map1.get("LyCode"));
						 break;
					 } 
				 }
				 break;
			 }
		 }*/
	}
}
