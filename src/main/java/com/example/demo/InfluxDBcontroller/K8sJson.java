package com.example.demo.InfluxDBcontroller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.Allocation;
import com.example.demo.pojo.MemoryAllocation;
import com.example.demo.pojo.NodeCpu;
import com.example.demo.pojo.NodeMemory;
import com.example.demo.pojo.NodeMessage;
import com.example.demo.pojo.PodMessage;
import com.example.demo.pojo.PodsAllocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.core.pattern.parser.Node;
import redis.clients.jedis.JedisCluster;




@RestController
public class K8sJson {
   /**
    * [
	*{
		"Request": 0.25,
		"Total": 24
	*}
*   ]
*/
	@Autowired
	private ObjectMapper ob;
	
	@Autowired
	private JedisCluster jedisCluster;
  
	@RequestMapping(value = "/k8s/cpu/allocation.json",method = RequestMethod.GET)
	public Allocation cpuCapacity() throws JsonParseException, JsonMappingException, IOException {
		
		InfluxDB influxDB =null;
		try {
			
		
    	Allocation all = new Allocation();
    	
    	   String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）查询requst的值
   		 influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT mean(\"value\") FROM \"cpu/node_reservation\"WHERE  time > now() - 18m  AND time < now()-3m \n" + 
   				"", "k8s");
   		QueryResult queryResult = influxDB.query(query);
   		List<Result> results = queryResult.getResults();
   		Double dou = Double.valueOf(results.get(0).getSeries()
   				.get(0)
   				.getValues()
   				.get(0)
   				.get(1).toString());
//保留4位小数
   		DecimalFormat df = new DecimalFormat("0.0000");
all.setRequest(df.format(dou));
        System.out.println(df.format(dou));
   		
   		//查询total的值
   		Query query1 = new Query("SELECT  mean(\"value\") FROM \"cpu/node_allocatable\" WHERE time > now() - 18m AND time < now()-3m", "k8s");
   		QueryResult queryResult1 = influxDB.query(query1);
   		List<Result> results1 = queryResult1.getResults();
   		Double dou1 = Double.valueOf(results1.get(0).getSeries()
   				.get(0)
   				.getValues()
   				.get(0)
   				.get(1).toString());
   		
   		DecimalFormat df1 = new DecimalFormat("0.0000");
   		all.setTotal(Double.valueOf(df1.format(dou1)));

        System.out.println(df.format(dou));
        
         String asString = ob.writeValueAsString(all);
         jedisCluster.set("cpu_allocation", asString);     
        
         
         
       return all;
        }
        catch (Exception e) {
			String string = jedisCluster.get("cpu_allocation");
			Allocation readValue = ob.readValue(string,Allocation.class);
        	return readValue;
		
            } finally {
	influxDB.close();
			
		}
	}
	/**
	 * [
	{
		"Request": 0,
		"Total": 47.008
	}
]
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = "/k8s/memory/allocation.json",method = RequestMethod.GET)
	public MemoryAllocation memoryCapacity() throws IOException {
		InfluxDB influxDB=null;
		try {
			
		
		MemoryAllocation all = new MemoryAllocation();
    	
    	   String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）查询requst的值
   		influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT mean(\"value\") FROM \"memory/node_reservation\" WHERE time > now() - 18m AND time < now()-3m", "k8s");
   		QueryResult queryResult = influxDB.query(query);
   		List<Result> results = queryResult.getResults();
   		Double dou = Double.valueOf(results.get(0).getSeries()
   				.get(0)
   				.getValues()
   				.get(0)
   				.get(1).toString());
   		
//保留4位小数
   		DecimalFormat df = new DecimalFormat("0.0000");
all.setRequest(df.format(dou));
        System.out.println(df.format(dou));
   		
   		//查询total的值
   		Query query1 = new Query("SELECT mean(\"value\") FROM \"memory/node_capacity\" WHERE time > now() - 18m AND time < now()-3m ", "k8s");
   		QueryResult queryResult1 = influxDB.query(query1);
   		List<Result> results1 = queryResult1.getResults();
   		Double dou1 = Double.valueOf(results1.get(0).getSeries()
   				.get(0)
   				.getValues()
   				.get(0)
   				.get(1).toString());
   		
   		DecimalFormat df1 = new DecimalFormat("0.0000");
   		all.setTotal(Double.valueOf(df1.format(dou1/1048576)));

   		String asString = ob.writeValueAsString(all);
        jedisCluster.set("memory_allocation", asString);  
   		
        
        
       return all;
		}catch (Exception e) {
			
			String string = jedisCluster.get("memory_allocation");
			MemoryAllocation readValue = ob.readValue(string,MemoryAllocation.class);
        	return readValue;
				
		
} finally {
	influxDB.close();
			
		}
	}
	
	
	@RequestMapping(value = "/k8s/cpu/usage_rate/pods_allocation.json",method = RequestMethod.GET)
	public PodsAllocation cpuPodsCapacity() throws IOException {
		InfluxDB influxDB =null;
		try {
			
	 
		
		PodsAllocation all = new PodsAllocation();
    	
    	   String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）查询allocation的值
   		 influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT count(*) FROM \"cpu/usage\" WHERE \"nodename\"='k8s-master' AND \"type\"='pod' AND time > now() - 4m AND time<now()-3m", "k8s");
   		QueryResult queryResult = influxDB.query(query);
   		List<Result> results = queryResult.getResults();
   		Double dou1 = Double.valueOf(results.get(0).getSeries()
   				.get(0)
   				.getValues()
   				.get(0)
   				.get(1).toString());
   		
   		DecimalFormat df1 = new DecimalFormat("0.0");
   		all.setAllocation((df1.format(dou1)));
   		System.out.println(results);
     all.setTotal("110");
     
     String asString = ob.writeValueAsString(all);
     jedisCluster.set("pods_allocation", asString); 
     
    
     return all;
     
}catch (Exception e) {
			
			String string = jedisCluster.get("pods_allocation");
			PodsAllocation readValue = ob.readValue(string,PodsAllocation.class);
        	return readValue;
				
    	} finally {
    		influxDB.close();
			// TODO: handle finally clause
		} 
	}
	//节点cpu使用情况
	@RequestMapping(value = "/k8s/cpu/usage_rate/nodeCpu.json",method = RequestMethod.GET)
	public List<NodeCpu> nodeCpuCapacity() throws ParseException, IOException {
		InfluxDB influxDB =null;
		try {
			
	
		List<NodeCpu> list = new LinkedList<>();
    	
    	   String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）查询allocation的值
   	influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT mean(\"value\") FROM \"cpu/usage_rate\"  WHERE \"type\"='node'  AND  time > now() - 61m  AND time < now()-1m GROUP BY time(5m)", "k8s");
   		QueryResult queryResult = influxDB.query(query);
   		List<Result> results = queryResult.getResults();
   		DecimalFormat df1 = new DecimalFormat("0.0000");
     List<List<Object>> values = results.get(0).getSeries().get(0).getValues();
         for(List<Object> nodelist : values) {
        	NodeCpu node = new NodeCpu();
        	node.setTime(UTCToCST(nodelist.get(0).toString()));
       		
        	node.setcPU_used(Double.valueOf(df1.format(
        			Double.valueOf(nodelist.get(1).toString())/1000
        			)));
        	list.add(node);
         }
         
         String asString = ob.writeValueAsString(list);
         jedisCluster.set("usage_rate_nodeCpu", asString);  
         
        return list;
}catch (Exception e) {
			
			String string = jedisCluster.get("usage_rate_nodeCpu");
			JavaType jt = ob.getTypeFactory().constructParametricType
					(List.class,NodeCpu.class );
			List<NodeCpu> readValue = ob.readValue(string,jt);
        	return readValue;
				
         
		} finally {
			
			influxDB.close();
		}
	}
	//时间转换
	public  String  UTCToCST(String UTCStr) throws ParseException  {
        String format="yyyy-MM-dd'T'HH:mm:ss'Z'";
		Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        date = sdf.parse(UTCStr);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
        String time = String.format("%02d",calendar.get(Calendar.HOUR_OF_DAY))+":"+String.format("%02d",calendar.get(Calendar.MINUTE));
System.out.println(time);
	
return time;

}
	//节点内存使用情况
	@RequestMapping(value = "/k8s/cpu/usage/nodememory.json",method = RequestMethod.GET)
	public List<NodeMemory> nodeMemoryCapacity() throws ParseException, IOException {
		InfluxDB influxDB=null;
		try {
			
		
		List<NodeMemory> list = new LinkedList<>();
    	
    	   String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）查询allocation的值
   		influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT mean(\"value\") FROM \"memory/usage\"  WHERE \"type\"='node' AND time > now() - 61m AND time < now()-1m GROUP BY time(5m)", "k8s");
   		QueryResult queryResult = influxDB.query(query);
   		List<Result> results = queryResult.getResults();
   		DecimalFormat df1 = new DecimalFormat("0.0000");
     List<List<Object>> values = results.get(0).getSeries().get(0).getValues();
         for(List<Object> nodelist : values) {
        	NodeMemory node = new NodeMemory();
        	node.setTime(UTCToCST(nodelist.get(0).toString()));
       		
        	Double double1 = Double.valueOf(nodelist.get(1).toString());
        	String format = df1.format(
        			double1/1048576);
        	node.setMemory_used(Double.valueOf(format));
        	list.add(node);
         }
         String asString = ob.writeValueAsString(list);
         jedisCluster.set("usage_nodememory", asString);  
         
 return list;
}catch (Exception e) {
			
			String string = jedisCluster.get("usage_nodememory");
			JavaType jt = ob.getTypeFactory().constructParametricType
					(List.class,NodeMemory.class );
			List<NodeMemory> readValue = ob.readValue(string,jt);
        	return readValue;
				
         
		
    	} finally {
			// TODO: handle finally clause
    		influxDB.close();
		}
	}
	
	//pod的cpu使用情况
		@RequestMapping(value = "/k8s/cpu/usage_rate/podCpu.json",method = RequestMethod.GET)
		public List<NodeCpu> clusterCpuCapacity() throws Exception {
			InfluxDB influxDB =null;
			try {
			
			List<NodeCpu> list = new LinkedList<>();
	    	
	    	   String  	infulxUrl = "http://172.19.100.4:30001";
	   		//1）查询allocation的值
	   		 influxDB = InfluxDBFactory.connect(infulxUrl);
	   		Query query = new Query("SELECT mean(\"value\") FROM \"cpu/usage_rate\"  WHERE \"type\"='pod'  AND  time > now() - 61m AND time < now()-1m GROUP BY time(5m)", "k8s");
	   		QueryResult queryResult = influxDB.query(query);
	   		List<Result> results = queryResult.getResults();
	   		DecimalFormat df1 = new DecimalFormat("0.0000");
	     List<List<Object>> values = results.get(0).getSeries().get(0).getValues();
	         for(List<Object> nodelist : values) {
	        	NodeCpu node = new NodeCpu();
	        	node.setTime(UTCToCST(nodelist.get(0).toString()));
	       		
	        	node.setcPU_used(Double.valueOf(df1.format(
	        			Double.valueOf(nodelist.get(1).toString())/1000
	        			)));
	        	list.add(node);
	         }
	         
	         
	         String asString = ob.writeValueAsString(list);
	         jedisCluster.set("usage_rate_podCpu", asString);  
	        
	        return list;
		} catch (Exception e) {
			
			String string = jedisCluster.get("usage_rate_podCpu");
			JavaType jt = ob.getTypeFactory().constructParametricType
					(List.class,NodeCpu.class );
			List<NodeCpu> readValue = ob.readValue(string,jt);
		    
			return readValue;
				
		}finally {
			
			influxDB.close();
		}
	   	
	         
	   		
		}
	
		
		//pod的内存使用情况
		//
		@RequestMapping(value = "/k8s/cpu/usage/podmemory.json",method = RequestMethod.GET)
		public List<NodeMemory> clusterMemoryCapacity() throws ParseException, JsonParseException, JsonMappingException, IOException {
	    	
			InfluxDB influxDB =null;
			try {
				
		
			List<NodeMemory> list = new LinkedList<>();
	    	
	    	   String  	infulxUrl = "http://172.19.100.4:30001";
	   		//1）查询allocation的值
	   		influxDB = InfluxDBFactory.connect(infulxUrl);
	   		Query query = new Query("SELECT mean(\"value\") FROM \"memory/usage\"  WHERE \"type\"='pod' AND time > now() - 61m AND time < now()-1m GROUP BY time(5m)", "k8s");
	   		QueryResult queryResult = influxDB.query(query);
	   		List<Result> results = queryResult.getResults();
	   		DecimalFormat df1 = new DecimalFormat("0.0000");
	     List<List<Object>> values = results.get(0).getSeries().get(0).getValues();
	         for(List<Object> nodelist : values) {
	        	NodeMemory node = new NodeMemory();
	        	node.setTime(UTCToCST(nodelist.get(0).toString()));
	       		
	        	Double double1 = Double.valueOf(nodelist.get(1).toString());
	        	String format = df1.format(
	        			double1/1048576);
	        	node.setMemory_used(Double.valueOf(format));
	        	list.add(node);
	         }
	         String asString = ob.writeValueAsString(list);
	         jedisCluster.set("usage_podmemory", asString);  
	         
	    return list;
	         
			}catch (Exception e) {
	 			
	 			String string = jedisCluster.get("usage_podmemory");
	 			JavaType jt = ob.getTypeFactory().constructParametricType
	 					(List.class,NodeMemory.class );
	 			List<NodeMemory> readValue = ob.readValue(string,jt);
	         	return readValue;
	 				
	 		} finally {
				// TODO: handle finally clause
				influxDB.close();
			}
		}
		
	//节点信息	
		@RequestMapping("/k8s/node/message.json")
		public Collection<NodeMessage> getNodeMessage () throws Exception{
			InfluxDB influxDB =null;
			try {
				
		
			Map< String, NodeMessage> nodeMap = new HashMap<>();
			 String  	infulxUrl = "http://172.19.100.4:30001";
		   		//1）得到cPU_count ready 字段
		   		influxDB = InfluxDBFactory.connect(infulxUrl);
		   		Query query = new Query("SELECT * FROM \"cpu/node_capacity\" WHERE time > now() - 3m AND time<now()-2m GROUP BY nodename", "k8s");
		   		QueryResult queryResult = influxDB.query(query);
		          
		   		List<Result> results = queryResult.getResults();
		   		List<Series> series = results.get(0).getSeries();
		   		for(Series se : series) {
		   			NodeMessage node = new NodeMessage();
		   		 node.setNode_Name(se.getTags().get("nodename"));
		   		 node.setReady(Boolean.valueOf(se.getValues().get(0).get(3).toString()));
		   		 node.setcPU_count(Double.valueOf(se.getValues().get(0).get(5).toString()));	
		   			System.out.println(se.getTags().get("nodename"));
		   			nodeMap.put(se.getTags().get("nodename"), node);
		   		}
		      //得到cpu-requst-rate字段
		   		Query query1 = new Query("SELECT mean(\"value\") FROM \"cpu/usage_rate\" WHERE \"type\"='node' AND time > now() - 18m AND time<now()-3m GROUP BY nodename ", "k8s");
		   		QueryResult queryResult1 = influxDB.query(query1);
		   		DecimalFormat df1 = new DecimalFormat("0.0000");
		   		List<Result> results1 = queryResult1.getResults();
		   		List<Series> series1 = results1.get(0).getSeries();
		   		for(Series se : series1) {
		   			NodeMessage node1 = nodeMap.get(se.getTags().get("nodename"));
		   		 node1.setcPU_request_rate(
		   				 (
		   					Double.valueOf(	 df1.format(Double.valueOf(se.getValues().get(0).get(1).toString())/1000)
		   				 )));	
		   		}	
		   		//查询memory——used
		   		Query query2 = new Query("SELECT mean(\"value\") FROM \"memory/usage\" WHERE \"type\"='node' AND time > now() - 18m AND time<now()-3m GROUP BY nodename ", "k8s");
		   		QueryResult queryResult2 = influxDB.query(query2);
		          
		   		List<Result> results2 = queryResult2.getResults();
		   		List<Series> series2 = results2.get(0).getSeries();
		   		for(Series se : series2) {
		   			NodeMessage node1 = nodeMap.get(se.getTags().get("nodename"));
		   		 node1.setMemory_used(
		   				 (
		   					Double.valueOf(	 df1.format(Double.valueOf(se.getValues().get(0).get(1).toString())/1048576)
		   				 )));
		   		}	
		   //查询memory——num
		   		Query query3 = new Query("SELECT mean(\"value\") FROM \"memory/node_capacity\" WHERE time > now() - 18m AND time<now()-3m GROUP BY nodename ", "k8s");
		   		QueryResult queryResult3 = influxDB.query(query3);
		          
		   		List<Result> results3 = queryResult3.getResults();
		   		List<Series> series3 = results3.get(0).getSeries();
		   		for(Series se : series3) {
		   			NodeMessage node1 = nodeMap.get(se.getTags().get("nodename"));
		   		 node1.setMemory_num(
		   				 (
		   					Double.valueOf(	 df1.format(Double.valueOf(se.getValues().get(0).get(1).toString())/1048576
		   							)
		   				 )));
		   		}		
		   	//查找creat——time
		   		Query query4 = new Query("SELECT mean(\"value\") FROM \"uptime\" WHERE \"type\"='node' AND time > now() - 5m AND time<now()-3m GROUP BY nodename ", "k8s");
		   		QueryResult queryResult4 = influxDB.query(query4);
		          
		   		List<Result> results4 = queryResult4.getResults();
		   		List<Series> series4 = results4.get(0).getSeries();
		   		for(Series se : series4) {
		   			NodeMessage node1 = nodeMap.get(se.getTags().get("nodename"));
		   		 node1.setCreated_time(
		   				 (
		   					Double.valueOf(	 df1.format(Double.valueOf(se.getValues().get(0).get(1).toString())/3600000/24
		   							)
		   				 )));
		   		}
		   		
		   		String asString = ob.writeValueAsString(nodeMap.values());
		         jedisCluster.set("node_message", asString);  
		        
		   		return  nodeMap.values();
}catch (Exception e) {
	 			
	 			String string = jedisCluster.get("node_message");
	 			JavaType jt = ob.getTypeFactory().constructParametricType
	 					(Collection.class,NodeMessage.class );
	 			Collection<NodeMessage> readValue = ob.readValue(string,jt);
	 		   
	 			
	 			return readValue;
		
			}finally {
				
				influxDB.close();
			}
		
		   		
		}
		
		@RequestMapping("/k8s/pod/message.json")
		public Collection<PodMessage> getPodMessage () throws Exception{
			InfluxDB influxDB =null;
			try {
			Map< String, PodMessage> nodeMap = new HashMap<>(21);
			 String  	infulxUrl = "http://172.19.100.4:30001";
		   		//1）得到containerGroup_Name  node_Name status memory 字段
		   		influxDB = InfluxDBFactory.connect(infulxUrl);
		   		Query query = new Query("SELECT mean(\"value\") FROM \"memory/usage\" WHERE \"type\"='pod' ANd time > now() - 18m AND time<now() -3m GROUP BY pod_name ,nodename", "k8s");
		   		QueryResult queryResult = influxDB.query(query);
		          
		   		List<Result> results = queryResult.getResults();
		   		List<Series> series = results.get(0).getSeries();
		   		DecimalFormat df1 = new DecimalFormat("0.0000");
		   		for(Series ser : series) {
		   			PodMessage pod = new PodMessage();
		   		 pod.setNode_Name(ser.getTags().get("nodename"));
		   		 pod.setContainerGroup_Name(ser.getTags().get("pod_name"));
		   	
		   		 pod.setStatus("Running");
		   		 pod.setMemory(
		   					Double.valueOf(	df1.format( Double.valueOf(ser.getValues().get(0).get(1).toString())/1048576
		   					)	) ) ;	
		   			
		   		 
		   			nodeMap.put(ser.getTags().get("pod_name"), pod);
		   		}
		      //得到cPU字段
		   		Query query1 = new Query("SELECT mean(\"value\") FROM \"cpu/usage_rate\" WHERE \"type\" ='pod' AND time > now() - 18m AND time<now()-3m GROUP BY pod_name", "k8s");
		   		QueryResult queryResult1 = influxDB.query(query1);
		         List<Result> results1 = queryResult1.getResults();
		   		List<Series> series1 = results1.get(0).getSeries();
		 
		   		for(Series ser : series1) {
		   			PodMessage pod = nodeMap.get(ser.getTags().get("pod_name"));
		   		
		   	
		   		 
		   		 pod.setcPU(
		   					Double.valueOf(	df1.format( Double.valueOf(ser.getValues().get(0).get(1).toString())/1000
		   						) ) );	
		   		 }
		   		//获取uptime
		   		Query query2 = new Query("SELECT mean(\"value\") FROM \"uptime\" WHERE \"type\"='pod' AND time > now() - 5m AND time<now()-3m GROUP BY pod_name", "k8s");
		   		QueryResult queryResult2 = influxDB.query(query2);
		         List<Result> results2 = queryResult2.getResults();
		   		List<Series> series2 = results2.get(0).getSeries();
		 
		   		for(Series ser : series2) {
		   			PodMessage pod = nodeMap.get(ser.getTags().get("pod_name"));
		   		 pod.setCreated_time(
		   					Double.valueOf(	df1.format( Double.valueOf(ser.getValues().get(0).get(1).toString())/3600000/24
		   						) ) );	
		   		 }
		   	//获取restarted	
		   		Query query3 = new Query("SELECT mean(\"value\") FROM \"restart_count\" WHERE \"type\"='pod' AND time > now() - 3m AND time<now()-2m GROUP BY pod_name ", "k8s");
		   		QueryResult queryResult3 = influxDB.query(query3);
		         List<Result> results3 = queryResult3.getResults();
		   		List<Series> series3 = results3.get(0).getSeries();
		 
		   		for(Series ser : series3) {
		   			PodMessage pod = nodeMap.get(ser.getTags().get("pod_name"));
		   		 pod.setRestarted(
		   					Double.valueOf( df1.format( Double.valueOf(ser.getValues().get(0).get(1).toString())
		   						))) ;	
		   		 }
		   		
		   		String asString = ob.writeValueAsString(nodeMap.values());
		         jedisCluster.set("pod_message", asString);  
		  
		   		return nodeMap.values() ;
}catch (Exception e) {
	 			
	 			String string = jedisCluster.get("pod_message");
	 			JavaType jt = ob.getTypeFactory().constructParametricType
	 					(Collection.class, PodMessage.class );
	 			Collection< PodMessage> readValue = ob.readValue(string,jt);
	 		    
	 		    
	 			return readValue;	
		   		
		   		}finally {
				
				influxDB.close();
			
		
		
		   		}
			}
		}
