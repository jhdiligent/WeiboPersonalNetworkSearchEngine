package weibosearchengineindexer;

import java.util.Date;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class JSONReader {
	public JSONArray readUserInfo(String path) {
		JSONArray ja = new JSONArray();
		BufferedReader reader = null;
		StringBuilder laststr = new StringBuilder();
		int i=0;
		try{
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			
			while((tempString = reader.readLine()) != null){			
				if(tempString.contains("ISO")||tempString.contains("_id")||
						tempString.contains("Province")||tempString.contains("City")||
						tempString.contains("Signature"))continue;
				if(tempString.equals("},")) {
					laststr.append("}");
					JSONObject jo = JSONObject.fromObject(laststr.toString());
					ja.add(jo);
					laststr = new StringBuilder();
					i++;
				}
				else
					laststr.append(tempString);

			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Total Users: "+i);
		return ja;			
	}
	public JSONArray readTweets(String path) {
		JSONArray ja = new JSONArray();
		BufferedReader reader = null;
		StringBuilder laststr = new StringBuilder();
		int i=0;
		try{
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			long epoch=0;
			ZonedDateTime zdt = null;
			LocalDateTime thedate =null;
			while((tempString = reader.readLine()) != null){				
				if(tempString.contains("_id")) {
					tempString=tempString.replace("_id", "id");
				}
				else if(tempString.contains("PubTime")) {
					int year = 0, month = 0, date = 0, hrs = 0, min = 0, sec = 0;
					if(tempString.contains("今")) {
						year=2016;
						month=12;
						date=12;
						sec=0;
						int count=0;
						for(int j=tempString.length()-1;j>=0;j--) {
							char c =tempString.charAt(j);
							if(c<='9'&&c>='0') {
								int time = (c-'0')+(tempString.charAt(--j)-'0')*10;
								if(count==0) {
									min=time;
									count++;
								}									
								else {
									hrs= time;
									break;
								}
							}
						}

					}else if(tempString.contains("月")) {
						year=2016;
						sec=0;
						int count=0;
						for(int j=tempString.length()-1;j>=0;j--) {
							char c =tempString.charAt(j);
							if(c<='9'&&c>='0') {
								int time = (c-'0')+(tempString.charAt(--j)-'0')*10;
								if(count==0) {
									min=time;
									count++;
								}									
								else if(count==1){
									hrs= time;
									count++;
								}
								else if(count==2) {
									date=time;
									count++;
								}
								else {
									month=time;
									break;
								}
							}
						}
						
					}
					else if(tempString.contains("分钟前")) {
						year=2016;
						month=12;
						date=12;
					}
					else {
						sec=0;
						int count=0;
						for(int j=tempString.length()-1;j>=0;j--) {
							char c =tempString.charAt(j);
							if(c<='9'&&c>='0') {
								if(count<=4) {
									int time = (c-'0')+(tempString.charAt(--j)-'0')*10;
									if(count==0) {
										sec=time;
										count++;
									}									
									else if(count==1){
										min= time;
										count++;
									}
									else if(count==2) {
										hrs=time;
										count++;
									}
									else if(count==3){
										date=time;
										count++;
									}	
									else {
										month=time;
										count++;
									}
								}
								else {
									int y=0;
									int mul=1;
									for(int k=1;k<=4;k++) {
										y+=(tempString.charAt(j)-'0')*mul;
										mul*=10;
										j--;
									}
									year=y;
									break;
								}
							}
							
						}
					}
					thedate = LocalDateTime.of(year,month,date,hrs,min,sec);
					zdt = thedate.atZone(ZoneId.of("America/Los_Angeles"));
					epoch = zdt.toInstant().toEpochMilli()/(1000*3600);
					continue;
				}
				else if(tempString.contains("Tools")) {
					continue;
				}
				if(tempString.equals("},")) {
					laststr.append("}");
					JSONObject jo = JSONObject.fromObject(laststr.toString());
					jo.put("epoch", epoch);
					jo.put("PubTime", thedate.toString());
					epoch=0;
					ja.add(jo);
					System.out.println(jo.toString());
					i++;
					laststr = new StringBuilder();
				}
				else
					laststr.append(tempString);

			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Total posts: "+i);
		return ja;			
	}
}
