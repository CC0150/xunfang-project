package com.xunfang.system.tools;

import com.xunfang.system.tools.DateUtils;
import com.xunfang.system.tools.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class RequestUtil {

	@SuppressWarnings("resource")
	public static String requestsGet(String url, String token) {
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("X-Auth-Token", token);
		httpget.setHeader("X-Subject-Token", token);
		httpget.setHeader("Content-Type", "application/json;charset=utf8");
		HttpClient client = new DefaultHttpClient();
		String res = "";
		try {
			HttpResponse response = client.execute(httpget);
			BufferedReader httpinput = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			res = httpinput.lines().collect(Collectors.joining());
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			ClientConnectionManager ku = client.getConnectionManager();
			ku.closeExpiredConnections();
			ku.shutdown();
		}
		return res;
	}
	
	@SuppressWarnings("resource")
	public static String requestsPost(String url, String entity, String token) {
		//打印请求相关信息
		System.err.println("---------------------req-dme-start-------------------------");
		long a = System.currentTimeMillis();//开始时间
		System.err.println("=======请求开始时间："+ DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:sss"));
		System.err.println("=======请求url："+url);
		System.err.println("=======请求token："+token);
		System.err.println("=======请求参数："+entity);
		
		HttpPost httpget = new HttpPost(url);
		httpget.setHeader("X-Auth-Token", token);
		httpget.setHeader("Content-Type", "application/json;charset=utf8");
		httpget.setEntity(new StringEntity(entity,"UTF-8"));
		HttpClient client = new DefaultHttpClient();
		String res = "";
		try {
			HttpResponse response = client.execute(httpget);
			BufferedReader httpinput = null;
			if(response != null && response.getEntity() != null) {
				httpinput = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				res = httpinput.lines().collect(Collectors.joining());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			ClientConnectionManager ku = client.getConnectionManager();
			ku.closeExpiredConnections();
			ku.shutdown();
		}
		System.err.println("=======响应结果："+res);
		System.err.println("=======请求结束时间："+ DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:sss"));
		long b = System.currentTimeMillis();//结束时间
		System.err.println("=======当前请求DME接口总耗时："+(b-a)+"毫秒.");
		System.err.println("---------------------req-dme-end---------------------------");
		return res;
	}
	
	@SuppressWarnings("resource")
	public static String requestsPut(String url, String entity, String token) {
		HttpPut httpget = new HttpPut(url);
		httpget.setHeader("X-Auth-Token", token);
		httpget.setHeader("Content-Type", "application/json;charset=utf8");
		if (StringUtils.isNotEmpty(entity)) {
			httpget.setEntity(new StringEntity(entity,"UTF-8"));
		}
		HttpClient client = new DefaultHttpClient();
		String res = "";
		try {
			HttpResponse response = client.execute(httpget);
			BufferedReader httpinput = null;
			if(response != null && response.getEntity() != null) {
				httpinput = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				res = httpinput.lines().collect(Collectors.joining());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			ClientConnectionManager ku = client.getConnectionManager();
			ku.closeExpiredConnections();
			ku.shutdown();
		}
		return res;
	}
	
	@SuppressWarnings("resource")
	public static String requestsDelete(String url, String token) {
		HttpDelete httpget = new HttpDelete(url);
		httpget.setHeader("Content-Type", "application/json;charset=utf8");
		httpget.setHeader("X-Auth-Token", token);
		HttpClient client = new DefaultHttpClient();
		String res = "";
		try {
			HttpResponse response = client.execute(httpget);
			BufferedReader httpinput = null;
			if(response != null && response.getEntity() != null) {
				httpinput = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				res = httpinput.lines().collect(Collectors.joining());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			ClientConnectionManager ku = client.getConnectionManager();
			ku.closeExpiredConnections();
			ku.shutdown();
		}
		return res;
	}
	
}
