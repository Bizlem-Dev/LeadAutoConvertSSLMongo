package leadconverter.servlet;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import leadconverter.doctiger.LogByFileWriter;
import leadconverter.freetrail.FreetrialShoppingCartUpdate;
import leadconverter.mongo.ConnectionHelper;
import leadconverter.mongo.MongoDAO;

@Component(immediate = true, metatype = false)
@Service(value = javax.servlet.Servlet.class)
@Properties({ @Property(name = "service.description", value = "Save product Servlet"),
		@Property(name = "service.vendor", value = "VISL Company"),
		@Property(name = "sling.servlet.paths", value = { "/servlet/service/CampaignStatisticsApi" }),
		@Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default"),
		@Property(name = "sling.servlet.extensions", value = { "hotproducts", "cat", "latestproducts", "brief",
				"prodlist", "catalog", "viewcart", "productslist", "addcart", "createproduct", "checkmodelno",
				"productEdit" }) })
@SuppressWarnings("serial")
public class CampaignStatisticsServlet extends SlingAllMethodsServlet {

	@Reference
	private SlingRepository repo;
	final String FILEEXTENSION[] = { "csv" };
 
	final int NUMBEROFRESULTSPERPAGE = 10;
	private static final long serialVersionUID = 1L;
	String fileType = "file";
	JSONObject mainjsonobject=null;
	
	String list_campaign_url=null;
	String get_campaign_url=null;
	String get_campaign_subscribers_url=null;
	
	
	String urlParametersForCampign=null;
	String urlParametersForSubscribers=null;
	String urlParametersForCampignDetails=null;
	
	JSONArray fullsubscribersArray = null;
	List<Document> fullsubscribersArrayDoc = null;
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
	}
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		    PrintWriter out = response.getWriter();
		    if (request.getRequestPathInfo().getExtension().equals("runBatch")) {
		    	out.println("--------Get Campaign Statistics Data From PHPLIST Started !---------");
		    	LogByFileWriter.logger_info("CampaignStatisticsServlet : " + "--------Get Campaign Statistics Data From PHPLIST Started !---------");
		
		    	Query query;
				Node campaignNode = null;
				String campaignNodeName = null;
				Node rootFunnelNode = null;
				String rootFunnelNodeName = null;
				Node funnelNode = null;
				String funnelNodeName = null;
				Node subFunnelNode = null;
				String subFunnelNodeName = null;
				JSONObject responseJson=null;
				JSONArray responseArr=null;
				
				//Property of campaign node
				String Body = null;
				String Campaign_Id = null;
				String CreatedBy = null;
				String List_Id = null;
				String Subject = null;
				String Type = null;
				//Property of Sub Funnel node
				String subFunnelCounter = null;
				String Current_Campaign = null;
				
				//Property of Funnel node
				String funnelCounter =null;
				try {
					Session session = null;
					String user=request.getRemoteUser().replace("@", "_");
					String group="";
					group=request.getParameter("group");
					Node shoppingnode = null;
			    	
			 		String expstatus= new FreetrialShoppingCartUpdate().checkFreeTrialExpirationStatus(user);
			 	
			 		shoppingnode=new FreetrialShoppingCartUpdate().getLeadAutoConverterNode(expstatus, user, group, session, response);
			 	
					if(shoppingnode!=null) {
					
					user="viki_gmail.com";
				    out.println("Getting Data For User  : "+user);
				    LogByFileWriter.logger_info("CampaignStatisticsServlet : " + "Getting Data For User  : "+user);
				    session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
					Node content = session.getRootNode().getNode("content");
				    
				    String slingqery = "select * from [nt:base] where Campaign_Id is not null "
							+ "and ISDESCENDANTNODE('"+shoppingnode+"/Lead_Converter/Email/Funnel')";
		            Workspace workspace = session.getWorkspace();
					query = workspace.getQueryManager().createQuery(slingqery, Query.JCR_SQL2);
					QueryResult queryResult = query.execute();
					NodeIterator iterator = queryResult.getNodes();
					//RowIterator row_iterator=queryResult.getRows(); //RangeIterator
					LogByFileWriter.logger_info("CampaignStatisticsServlet : " + "iterator  : "+"Total Campaign Found : "+iterator.getSize());
					responseArr=new JSONArray();
					MongoDAO mdao=new MongoDAO();
					mdao.dropCollection("funnel");// Before inserting new records to collection we need to drop the collection
					int count=0;
					while (iterator.hasNext()) {
						++count;
						responseJson=new JSONObject();
						campaignNode = iterator.nextNode();
						campaignNodeName=campaignNode.getName();
						subFunnelNode=campaignNode.getParent();
						subFunnelNodeName=subFunnelNode.getName();
						
						if(!subFunnelNodeName.equals("Campaign")){
							if(campaignNode.hasProperty("Body")){
								Body=campaignNode.getProperty("Body").getString();
								responseJson.put("Body", Body);
							}else{
								responseJson.put("Body", "null");
							}
							if(campaignNode.hasProperty("Campaign_Id")){
								Campaign_Id=campaignNode.getProperty("Campaign_Id").getString();
								responseJson.put("Sling_Campaign_Id", Campaign_Id);
							}else{
								responseJson.put("Sling_Campaign_Id", "null");
							}
							if(campaignNode.hasProperty("CreatedBy")){
								CreatedBy=campaignNode.getProperty("CreatedBy").getString();
								responseJson.put("CreatedBy", CreatedBy);
							}else{
								responseJson.put("CreatedBy", "null");
							}
							if(campaignNode.hasProperty("List_Id")){
								List_Id=campaignNode.getProperty("List_Id").getString();
								responseJson.put("List_Id", List_Id);
							}else{
								responseJson.put("List_Id", "null");
							}
							if(campaignNode.hasProperty("Subject")){
								Subject=campaignNode.getProperty("Subject").getString();
								responseJson.put("Sling_Subject", Subject);
							}else{
								responseJson.put("Sling_Subject", "null");
							}
							if(campaignNode.hasProperty("Type")){
								Type=campaignNode.getProperty("Type").getString();
								responseJson.put("Type", Type);
							}else{
								responseJson.put("Type", "null");
							}
							responseJson.put("subFunnelNodeName", subFunnelNodeName);
							if(subFunnelNode.hasProperty("Counter")){
								subFunnelCounter=subFunnelNode.getProperty("Counter").getString();
								responseJson.put("subFunnelCounter", subFunnelCounter);
							}else{
								responseJson.put("subFunnelCounter", "null");
							}
							if(subFunnelNode.hasProperty("Current_Campaign")){
								Current_Campaign=subFunnelNode.getProperty("Current_Campaign").getString();
								responseJson.put("Current_Campaign", Current_Campaign);
							}else{
								responseJson.put("Current_Campaign", "null");
							}
							
							funnelNode=subFunnelNode.getParent();
							funnelNodeName=funnelNode.getName();
							responseJson.put("funnelNodeName", funnelNodeName);
							if(funnelNode.hasProperty("Counter")){
								funnelCounter=funnelNode.getProperty("Counter").getString();
								responseJson.put("funnelCounter", funnelCounter);
							}else{
								responseJson.put("funnelCounter", "null");
							}
							out.println("Funnel Node Name : "+ funnelNodeName+"\nSub Funnel Node Name : "+ subFunnelNodeName+"\nCampaign Node Name : "+ campaignNodeName+"\nCampaign Id : "+ Campaign_Id+"\nList Id : "+ List_Id);
							LogByFileWriter.logger_info("CampaignStatisticsServlet : " + "Funnel Node Name : "+ funnelNodeName+"\nSub Funnel Node Name : "+ subFunnelNodeName+"\nCampaign Node Name : "+ campaignNodeName+"\nCampaign Id : "+ Campaign_Id+"\nList Id : "+ List_Id);
							String mongoResponse=saveCampaignInMongoDB(funnelNodeName,subFunnelNodeName,Campaign_Id,responseJson,response);
							break;
							/*
							if(count==1){
								break;
							}
							*/
						}
					}
					out.println("--------Get Campaign Statistics Data From PHPLIST Ended !---------");
			    	LogByFileWriter.logger_info("CampaignStatisticsServlet : " + "--------Get Campaign Statistics Data From PHPLIST Ended !---------");
				}else {
					out.println("user not valid ");
					
				}
				} catch (Exception e) {
					out.print(e.getMessage());
				}finally{
					try {
						   MongoDAO mdao=new MongoDAO();
						   mdao.dropCollection("subscribers");
						   mdao.findAllSubscriberByFilter("funnel");
						   mdao.dropCollection("url");
						   mdao.findAllUrlByFilter("funnel");
						   mdao.dropCollection("temp_subscribers");
						   mdao.findAllTempSubscriberByFilter("funnel");
						   
			        	} catch (Exception e) {
			        		out.print(e.getMessage());
					    }
				}
				
			}else if (request.getRequestPathInfo().getExtension().equals("chkCampaignInFunnel")) {
				
				String remoteuser = request.getRemoteUser();
				try {
					//out.println("CampaignStatistic Extension called");
					Session session = null;
					session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
					String campaign_id=request.getParameter("campaign_id");
					String user=request.getRemoteUser().replace("@", "_");
				    out.println("campaign_id  : "+campaign_id);
				    out.println("user  : "+user);
					String res=this.chkCampaignInFunnel(campaign_id,user,response);
					out.println("res  : "+res);
				} catch (Exception e) {
					out.print(e.getMessage());
				}
				
			}else if (request.getRequestPathInfo().getExtension().equals("test_mongo_conn")) {
				MongoClient mongoClient = null;
			    MongoDatabase database  = null;
			    MongoCollection<org.bson.Document> collection = null;
			    
		        try {
		        	mongoClient=ConnectionHelper.getConnection();
		            database=mongoClient.getDatabase("phplisttest");
		            collection=database.getCollection("test_coll");
		            org.bson.Document doc = new org.bson.Document("campaign_info", "campaign_info");
		            collection.insertOne(doc);
		           } catch (Exception e) {
		            e.printStackTrace();
		            throw new RuntimeException(e);
				} finally {
					ConnectionHelper.closeConnection(mongoClient);
				}
		        out.print("collection Name : ");
				
			}else if (request.getRequestPathInfo().getExtension().equals("createCollectionOfUrlStatistics")) {
				try {
					   MongoDAO mdao=new MongoDAO();
					   mdao.dropCollection("url");
					   out.print(mdao.findAllUrlByFilter("funnel"));
		        	} catch (Exception e) {
		            e.printStackTrace();
		            throw new RuntimeException(e);
				}
		        out.print("Collection Created Successfully !");
				
			}else if (request.getRequestPathInfo().getExtension().equals("createCollectionOfSubscribersStatistics")) {
				try {
					   MongoDAO mdao=new MongoDAO();
					   mdao.dropCollection("subscribers");
					   mdao.findAllSubscriberByFilter("funnel");
		        	} catch (Exception e) {
		            e.printStackTrace();
		            throw new RuntimeException(e);
				}
				out.print("Collection Created Successfully !");
				
			}else{
				try {
					out.print("You Should Provide campaignStatistic as Extension Name");
				} catch (Exception e) {
					out.print(e.getMessage());
				}
			}
		}
        
	public static JSONObject mergeDocObjects(JSONObject json1, JSONObject json2) {
		JSONObject mergedJSON =null;
		System.out.println("mergedJSON : 1");
		try {
			mergedJSON = new JSONObject(json2.toString());
			Iterator<String> itr=json1.keys();
			while (itr.hasNext()) {
				String key=itr.next();
				mergedJSON.put(key,json1.getString(key));
	            
			}
			System.out.println("mergedJSON : "+mergedJSON);
		} catch (Exception e) {
			throw new RuntimeException("JSON Exception" + e);
		}
		return mergedJSON;
	}
	public String saveCampaignInMongoDB(String funnelName,String subFunnelName,String Campaign_Id,JSONObject campaignObject,SlingHttpServletResponse response){
		
		try {
			PrintWriter out = response.getWriter();
			JSONObject campignDetailsJsonObj=null;
			JSONObject subscribersDetailsJsonObj=null;
			JSONObject funnelJsonObj=null;
			String page="1";
		    String per_page="30";
		    String start_date="2018-08-01%2008:59:53";
		    String end_date="2018-11-24%2008:59:53";
		    
			urlParametersForCampignDetails="?campaign_id=";
			String finalUrlParametersForCampignDetails=urlParametersForCampignDetails+Campaign_Id;
			get_campaign_url=ResourceBundle.getBundle("config").getString("get_campaign");
			
			urlParametersForSubscribers="?page="+page+"&per_page="+per_page+"&campaign_id=";
			String finalUrlParametersForSubscriber=urlParametersForSubscribers+Campaign_Id;
			get_campaign_subscribers_url=ResourceBundle.getBundle("config").getString("get_campaign_subscribers");
			
			
			
			String campignDetailsResponse = this
					.sendpostdata(get_campaign_url, finalUrlParametersForCampignDetails.replace(" ", "%20"), response);
			fullsubscribersArray=new JSONArray();
			if(!campignDetailsResponse.equals("404")){
				campignDetailsJsonObj=new JSONObject(campignDetailsResponse);
				out.println("Campaign Details Found For Campaign Subject : "+campignDetailsJsonObj.getString("subject"));
				funnelJsonObj=mergeJSONObjects(campaignObject,campignDetailsJsonObj);
				//out.println("urlParametersForSubscribers : "+finalUrlParametersForSubscriber);
				//fullsubscribersArray=new JSONArray();
				this.processSubscribers(get_campaign_subscribers_url, finalUrlParametersForSubscriber.replace(" ", "%20"), response);
				out.println("Total Number Of Subscriber Found For Above Campaign = "+fullsubscribersArray.length());
				subscribersDetailsJsonObj=new JSONObject();
				subscribersDetailsJsonObj.put("total", fullsubscribersArray.length());
				subscribersDetailsJsonObj.put("data", fullsubscribersArray);
				funnelJsonObj.put("viewed_subscribers", subscribersDetailsJsonObj);
				MongoDAO mdao=new MongoDAO();
				String funnel_coll_record_id=mdao.createOne("funnel", funnelJsonObj);
				out.println("Mongo DB Id of Inserted Record In funnel Collection : "+funnel_coll_record_id);
			}else{
				out.println("Total Number Of Subscriber Found For Above Campaign = "+fullsubscribersArray.length());
			}
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fullsubscribersArray.toString();
     }
	
	public static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
		JSONObject mergedJSON =null;
		System.out.println("mergedJSON : 1");
		try {
			mergedJSON = new JSONObject(json2.toString());
			Iterator<String> itr=json1.keys();
			while (itr.hasNext()) {
				String key=itr.next();
				mergedJSON.put(key,json1.getString(key));
	            
			}
			System.out.println("mergedJSON : "+mergedJSON);
		} catch (Exception e) {
			throw new RuntimeException("JSON Exception" + e);
		}
		return mergedJSON;
	}
	public String chkCampaignInFunnel(String Campaign_Id,String user,SlingHttpServletResponse response)
			throws ServletException, IOException {
			PrintWriter out = response.getWriter();
			
			Query query;
			Node campaignNode = null;
			String campaignNodeName = null;
			Node rootFunnelNode = null;
			String rootFunnelNodeName = null;
			Node funnelNode = null;
			String funnelNodeName = null;
			Node subFunnelNode = null;
			String subFunnelNodeName = null;
			JSONObject responseJson=null;
			try {
				Session session = null;
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
				
				String slingqery = "select * from [nt:base] where (contains('Campaign_Id','"+ Campaign_Id + "'))"
						+ "and ISDESCENDANTNODE('/content/user/"+user+"/Lead_Converter/Email/Funnel')";
	            //output of above query : /content/user/viki_gmail.com/Lead_Converter/Email/Funnel/1/Inform/viki_gmail.com_Inform_1
				Workspace workspace = session.getWorkspace();
				
				query = workspace.getQueryManager().createQuery(slingqery, Query.JCR_SQL2);
				QueryResult queryResult = query.execute();
				NodeIterator iterator = queryResult.getNodes();
				long size=iterator.getSize();
				if (iterator.hasNext()) {
					responseJson=new JSONObject();
					campaignNode = iterator.nextNode();
					campaignNodeName=campaignNode.getName();
					
					subFunnelNode=campaignNode.getParent();
					subFunnelNodeName=subFunnelNode.getName();
					
					funnelNode=subFunnelNode.getParent();
					funnelNodeName=funnelNode.getName();
					
					responseJson.put("campaignNodeName", campaignNodeName);
					responseJson.put("subFunnelNodeName", subFunnelNodeName);
					responseJson.put("funnelNodeName", funnelNodeName);
					responseJson.put("Size", size);
						
				}
			} catch (RepositoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return responseJson.toString();
	}
	
	
	public String processSubscribers(String callurl, String urlParameters, SlingHttpServletResponse response)
			throws ServletException, IOException {
			PrintWriter out = response.getWriter();
			String next=null;
			int total;
	
			
		    JSONArray subscribersArray = null;
		    JSONObject subscribersObject=null;
		    JSONObject subscriberObject=null;
		    String subscriberCampaignid=null;
		    String userid=null;
			JSONObject paginationJson=null;
			
			String postresponse = this
					.sendpostdata(get_campaign_subscribers_url, urlParameters.replace(" ", "%20"), response);
			//out.println("postresponse : "+postresponse);
			try {
				subscribersObject = new JSONObject(postresponse);
				paginationJson=subscribersObject.getJSONObject("paging");
				next=paginationJson.getString("next");
				subscribersArray=subscribersObject.getJSONArray("data");
				total=subscribersObject.getInt("total");
				//out.println("subscribersArray : "+subscribersArray);
				if(total>0){
					for(int i=0;i<subscribersArray.length();i++){
						subscriberObject=subscribersArray.getJSONObject(i);
						//out.println("subscriberObject : "+subscriberObject);
						userid=subscriberObject.getString("userid");
						subscriberCampaignid=subscriberObject.getString("campaignid");
						out.println("userid : "+userid);
						fullsubscribersArray.put(subscriberObject);
						if(i==subscribersArray.length()-1){
							if(!next.equals("null")){
							    this.processSubscribers(get_campaign_subscribers_url, next.replace(" ", "%20"), response);
							}else{
								//out.print("next equal 2 null that why execution stop for subscribers");
								break;
							}
						}
					}
				}else{
					out.println("There is no records for subscribers");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return urlParameters;
	}

	
    
    public static String checkCampaignInSubfunnel(String campignId,JSONObject campaignObject){
		return campignId;
    	
    	
    }


	public String sendpostdata(String callurl, String urlParameters, SlingHttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		//out.println("Url :" + callurl);
		URL url = new URL(callurl + urlParameters.replace("\\", ""));
		
		//out.println("urlParameters :" + urlParameters);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");

		//
		OutputStream writer = conn.getOutputStream();

		writer.write(urlParameters.getBytes());
		// out.println("Writer Url : "+writer);
		int responseCode = conn.getResponseCode();
		out.println("POST Response Code :: " + responseCode);
		StringBuffer buffer = new StringBuffer();
		//
		if (responseCode == 200) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				buffer.append(inputLine);
			}
			in.close();
			//
			// out.println(buffer.toString());
		} else {
			//out.println("POST request not worked! responseCode is : "+responseCode);
			buffer.append(responseCode);
		}
		writer.flush();
		writer.close();
		return buffer.toString();

	}
	public  static Document convertSubscribersJson2Doc(JSONObject subscriber_jsonObj) {

	    JSONObject dataJsonObj=null;
	    String data_campaignid=null;
	    String data_userid=null;
	    String data_viewed=null;
	    String data_email=null;
	    String data_uuid=null;
	    
	    //campaignclickstatistics
	    JSONArray campaignclickstatistics=null;
	    JSONObject campaignclickstatisticsJsonObj=null;
	    String campaignclickstatistics_firstclick=null;
		String campaignclickstatistics_latestclick=null;
		String campaignclickstatistics_clicked=null;
		
		//urlclickstatistics
		JSONArray urlclickstatistics=null;
		JSONObject urlclickstatisticsJsonObj=null;
		String urlclickstatistics_firstclick=null;
		String urlclickstatistics_latestclick=null;
		String urlclickstatistics_clicked=null;
		String urlclickstatistics_messageid=null;
		String urlclickstatistics_url=null;
	    
	    //linkcount viewed
	    //viewed_subscribers --data--  urlclickstatistics
	    
	   
        	Document subscriberJsonObjDoc=new Document();
    		try {
                    
    			    String strDate="27 Sep 2018 17:06";
	    		    SimpleDateFormat dateFormatter=new SimpleDateFormat("dd MMM yyyy HH:mm"); 
		    	    dataJsonObj=subscriber_jsonObj;
			    	data_campaignid=dataJsonObj.getString("campaignid"); 
			    	subscriberJsonObjDoc.put("campaignid", data_campaignid);
			    	data_userid=dataJsonObj.getString("userid"); 
			    	subscriberJsonObjDoc.put("userid", data_userid);
			    	data_viewed=dataJsonObj.getString("viewed"); 
			    	subscriberJsonObjDoc.put("viewed", data_viewed);
			    	subscriberJsonObjDoc.put("viewed_date",dateFormatter.parse(data_viewed));
			    	data_email=dataJsonObj.getString("email"); 
			    	subscriberJsonObjDoc.put("email", data_email);
			    	data_uuid=dataJsonObj.getString("uuid"); 
			    	subscriberJsonObjDoc.put("uuid", data_uuid);
			    	
			    	List<Document> campaignclickstatisticsAyyDoc = new ArrayList<Document>();
			    	Document campaignclickstatisticsDoc=new Document();
			    	campaignclickstatistics=dataJsonObj.getJSONArray("campaignclickstatistics");
			    	      campaignclickstatisticsJsonObj=campaignclickstatistics.getJSONObject(0);
				    	      campaignclickstatistics_firstclick=campaignclickstatisticsJsonObj.getString("firstclick"); 
				    	      campaignclickstatisticsDoc.put("firstclick", campaignclickstatistics_firstclick);
				    	      campaignclickstatistics_latestclick=campaignclickstatisticsJsonObj.getString("latestclick"); 
				    	      campaignclickstatisticsDoc.put("latestclick", campaignclickstatistics_latestclick);
				    	      campaignclickstatistics_clicked=campaignclickstatisticsJsonObj.getString("clicked"); 
				    	      campaignclickstatisticsDoc.put("clicked", campaignclickstatistics_clicked);
				    	      campaignclickstatisticsAyyDoc.add(campaignclickstatisticsDoc);
				    subscriberJsonObjDoc.put("campaignclickstatistics", campaignclickstatisticsAyyDoc);
				    
				    List<Document> urlclickstatisticsAyyDoc = new ArrayList<Document>();
			    	Document urlclickstatisticsDoc=null;
				    if(!campaignclickstatistics_firstclick.equals("null")){
				    	
				    	urlclickstatistics=dataJsonObj.getJSONArray("urlclickstatistics");
				    	for(int j=0;j<urlclickstatistics.length();j++){
				    		urlclickstatisticsJsonObj=urlclickstatistics.getJSONObject(j);
					    		urlclickstatistics_firstclick=urlclickstatisticsJsonObj.getString("firstclick"); 
					    		urlclickstatistics_latestclick=urlclickstatisticsJsonObj.getString("latestclick"); 
					    		urlclickstatistics_clicked=urlclickstatisticsJsonObj.getString("clicked");
					    		urlclickstatistics_messageid=urlclickstatisticsJsonObj.getString("messageid");
					    		urlclickstatistics_url=urlclickstatisticsJsonObj.getString("url");
					    		System.out.println("-----urlclickstatistics Found");
					    		System.out.println("UrL : "+urlclickstatistics_url);
					    		
					    		urlclickstatisticsDoc = new Document();

					    		urlclickstatisticsDoc.put("firstclick",urlclickstatistics_firstclick);
					    		urlclickstatisticsDoc.put("firstclick_date",dateFormatter.parse(urlclickstatistics_firstclick));
					    		urlclickstatisticsDoc.put("latestclick",urlclickstatistics_latestclick);
					    		urlclickstatisticsDoc.put("latestclick_date",dateFormatter.parse(urlclickstatistics_latestclick));
					    		urlclickstatisticsDoc.put("clicked",urlclickstatistics_clicked);
					    		urlclickstatisticsDoc.put("messageid",urlclickstatistics_messageid);
					    		urlclickstatisticsDoc.put("url",urlclickstatistics_url);
					    		
					    		urlclickstatisticsAyyDoc.add(urlclickstatisticsDoc);
				    	}
				    	subscriberJsonObjDoc.put("urlclickstatistics", urlclickstatisticsAyyDoc);
				    	
				    }else{
				    	System.out.println("No urlclickstatistics Found");
				    	subscriberJsonObjDoc.put("urlclickstatistics", urlclickstatisticsAyyDoc);
				    }
				    
		    
    		} catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
    		}
        return subscriberJsonObjDoc;
    }

}