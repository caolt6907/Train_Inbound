package com.strategy.tools;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
/*	private static String IP="localhost";
//	private static String IP="112.64.145.218";
	private static int port=56379;
	private static String  passWord="6EhSiGpsmSMRyZieglUImkTr-eoNRNBgRk397mVyu66MHYuZDsepCeZ8A-MHdLBQwQQVQiHBufZbPb";
	private static int dataBase=6;
	private static Jedis rs;
	
	static{
		rs= new Jedis(IP, port);
		rs.auth(passWord);
		rs.select(dataBase);
	}

	public static Jedis getRs() {
		return rs;
	}
	*/
	private static String  passWord="6EhSiGpsmSMRyZieglUImkTr-eoNRNBgRk397mVyu66MHYuZDsepCeZ8A-MHdLBQwQQVQiHBufZbPb";
	private static int dataBase=8;//6
	private RedisUtils(){		    
	    }
	 private static  JedisPool jedisPool = null;
	  //获取链接
	    public static synchronized Jedis getJedis(){
	        if(jedisPool==null){
	            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
	            //指定连接池中最大空闲连接数
	            jedisPoolConfig.setMaxIdle(10);
	            //链接池中创建的最大连接数
	            jedisPoolConfig.setMaxTotal(100);
	            
	           
	            
	           
	            //设置创建链接的超时时间
	            jedisPoolConfig.setMaxWaitMillis(2000);
	            //表示连接池在创建链接的时候会先测试一下链接是否可用，这样可以保证连接池中的链接都可用的。
	            jedisPoolConfig.setTestOnBorrow(false);
	            
	            
	            
	            
	            jedisPool = new JedisPool(jedisPoolConfig, "localhost", 56379);
	        }
	        Jedis jedis=jedisPool.getResource();
	        jedis.auth(passWord);
	       jedis.select(dataBase);
	      //  return jedisPool.getResource();
	        return jedis;
	    }
	    //返回链接
	    public static void returnResource(Jedis jedis){
	        jedisPool.returnResourceObject(jedis);
	    }
	    public static void main(String[] arg){
	    	Jedis rs=RedisUtils.getJedis();
	    //	rs.auth(passWord);
	    //	rs.select(dataBase);
	    	System.out.println(rs.ping());
	    	RedisUtils.returnResource(rs);
	    }
}
