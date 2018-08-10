package weibosearchengineindexer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Pipeline {
	
	@SuppressWarnings({ "static-access", "deprecation", "unchecked" })
	public static void main(String[] args) throws IOException {
		//Read Json File
		System.out.println("Prepare to read json file...");
			
		JSONArray userinfo = new JSONReader().readUserInfo("C:\\Users\\jiaca\\align2018\\CS6200\\project\\Information.json");
		JSONArray tweets = new JSONReader().readTweets("C:\\Users\\jiaca\\align2018\\CS6200\\project\\Tweets.json");
		
		//Connect with ElasticSearch
		System.out.println("Uploading");
//		
		ElasticsearchClient ec = new ElasticsearchClient();
		ec.CreateIndexandMapping("weibo", "userinfo");
		ec.Upload(userinfo, "weibo", "userinfo");
		ec.CreateIndexandMapping("sina", "tweets");
		ec.Upload(tweets, "sina", "tweets");
		ec.close();
	}
	
}
