package com.example.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.pojo.NodeCpu;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfluxDbApplicationTests {

	@Test
	public void contextLoads() throws ParseException {
		String  	infulxUrl = "http://172.19.100.4:30001";
   		//1）得到cPU_count ready 字段
   		InfluxDB influxDB = InfluxDBFactory.connect(infulxUrl);
   		Query query = new Query("SELECT * FROM \"cpu/node_capacity\" WHERE time > now() - 3m AND time<now()-2m GROUP BY nodename", "k8s");
   		QueryResult queryResult = influxDB.query(query);
          
   		List<Result> results = queryResult.getResults();
   		List<Series> series = results.get(0).getSeries();
   		for(Series se : series) {
   			
   			System.out.println(se.getTags().get("nodename"));
   			System.out.println(se.getValues().get(0).get(3));
   			System.out.println(se.getValues().get(0).get(5));
   		}
     	 
     	 
      }
    
		
	
		
		
	
	public  String  UTCToCST(String UTCStr, String format) throws ParseException  {
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
}