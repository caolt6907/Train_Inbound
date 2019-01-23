package com.ttsconvert;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.sinovoice.jTTS.jTTS_ML;
import com.strategy.tools.JsonUtils;
import com.strategy.tools.RedisUtils;

public class TtsConvert implements Runnable{
	private static Logger logger = Logger.getLogger(TtsConvert.class);
	Jedis rs;
	jTTS_ML jTTS;
	String filePath="/usr/local/freeswitch/sounds/en/us/callie/crm/" ;
	public  TtsConvert(jTTS_ML jTTS){
		this.jTTS=jTTS;
		
	}

	@Override
	public void run() {
	try{
		rs=RedisUtils.getJedis();//获取redis连接池里的连接对象
		if(!(rs.get("TTSConvert")+"").equals("")){
		List<Map<String, Object>> list = JsonUtils.parseJSON2List((String)rs.get("TTSConvert"));
		
		 for(Map<String, Object> map:list){
			 String directory = (map.get("ProjectId")+"").split("-")[((map.get("ProjectId")+"").split("-")).length-1]+"/";
			 System.out.println(filePath+directory);
			 int nRet=this.jTTS.jTTS_PlayToFile(map.get("Content")+"", filePath+directory+map.get("LyCode")+".wav", 6, null, 0, "", (long)0); 
			 System.out.println(map.get("Content")+""+"jTTS_PlayToFile nRet is " + nRet );
			 logger.info(map.get("Content")+""+"jTTS_PlayToFile nRet is " + nRet );
			 
		 }
		}
	 }catch(Exception e){
		 Writer w = new StringWriter();
       	 e.printStackTrace(new PrintWriter(w));
       	  String s=w.toString();
       	logger.error(s);
	 }
	finally{
		 rs.set("TTSConvert", "");
		 RedisUtils.returnResource(rs);
	}
	}
	
	

}
