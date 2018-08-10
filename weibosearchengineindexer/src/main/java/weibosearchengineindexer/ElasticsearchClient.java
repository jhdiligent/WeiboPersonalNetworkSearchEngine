package weibosearchengineindexer;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ElasticsearchClient {
	
	RestClient restClient;
	
	public ElasticsearchClient() {
		restClient = RestClient.builder(
		        new HttpHost("localhost", 9200, "http")).build();			
	}
	
	public void CreateIndexandMapping(String index,String type) throws IOException {
		Map<String, String> params = Collections.emptyMap();
		Response response;
		response = restClient.performRequest("PUT", "/"+index, params);

		//set analyzer
		String jsonString="{ \"properties\": {\"NickName\": {\"type\": \"text\",\"analyzer\": \"ik_max_word\",\"search_analyzer\": \"ik_max_word\"}}}";
		HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
		response = restClient.performRequest("POST", "/"+index+"/"+type+"/_mapping",params,entity);

	}
	/**
	 * Upload json data to elasticsearch
	 * @param ja
	 * @param index
	 * @param type
	 * @throws IOException
	 */
	public void Upload(JSONArray ja, String index, String type) throws IOException{

		//Create index
		Map<String, String> params = Collections.emptyMap();
		Response response;		
		
		//Upload json independently
		for(int i=0,size=ja.size();i<size;i++) {
			JSONObject jsonobject = (JSONObject) ja.get(i);
			String jsonString = jsonobject.toString();
			params = Collections.emptyMap();		
			HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
			try {
				response = restClient.performRequest("PUT", "/"+index+"/"+type+"/"+i, params, entity);
			} catch (IOException e) {
				System.err.println("reponse err");
				e.printStackTrace();
			} 
		}
	}
	public void close() throws IOException {
		restClient.close();
	}

}
