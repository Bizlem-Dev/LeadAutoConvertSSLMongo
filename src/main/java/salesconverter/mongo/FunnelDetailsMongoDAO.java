package salesconverter.mongo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

public class FunnelDetailsMongoDAO {

	public static void main(String[] args) throws JSONException {
		// TODO Auto-generated method stub
		Map<String,String> map=new HashMap<String,String>();   
	    //Adding elements to map  
	    map.put("remoteuser","viki@gmail.com");  
	    map.put("funnelName","July18F01");  
	    map.put("fromName","Akhilesh Yadav");
	    map.put("fromEmailAddress","viki@gmail.com");
	    map.put("trackOpens","false");
	    map.put("trackClicks","false");
	    map.put("trackPlainTextClicks","false");
	    map.put("googleAnalyticssLinkTracking","false");
	    map.put("autoTweetAfterSending","true");
	    map.put("autoPost2SocialMedia","false");
	    //addFunnelDetails(map);
	    /*
	    getFunnelList("viki_gmail.com");
	    JSONObject category_json_obj=getFunnelDetail("viki_gmail.com","FunnelJune0701");
	    JSONArray explore_json_arr=category_json_obj.getJSONArray("Explore");
	    for(int i=0;i<explore_json_arr.length();i++){
	    	System.out.println("explore_json_obj : "+explore_json_arr.getJSONObject(i));
	    }
	    */
	    String CreatedBy = "viki_gmail.com";
        String FunnelName = "FunnelJune0701";
        String SubFunnelName = "Connect";
        
        getListFunnelDetail(CreatedBy,FunnelName,SubFunnelName);
        
	}
	enum Category 
    { 
		Explore,Entice,Inform,Warm,Connect;
    }
	
	public static JSONObject getListFunnelDetail(String CreatedBy,String FunnelName,String SubFunnelName){
	    JSONObject category_json_obj=new JSONObject();
		
	    JSONArray explore_json_arr=new JSONArray();
		JSONArray entice_json_arr=new JSONArray();
		JSONArray inform_json_arr=new JSONArray();
		JSONArray warm_json_arr=new JSONArray();
		JSONArray connect_json_arr=new JSONArray();
		
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    MongoCollection<Document> campaign_details_collection = null;
	    MongoCursor<Document> funnelDetailsCursor=null;
	    Document campaign_list_doc=null;
	    
	    
	    String ListId=null;
	    String ListName=null;
	    int ListSubscriberCount=0;
	    
	    int ListSubscriberCountEx=0;
	    int ListSubscriberCountEnt=0;
	    int ListSubscriberCountInf=0;
	    int ListSubscriberCountWarm=0;
	    int ListSubscriberCountConnect=0;
	    
	    String ListStatus=null;
	    String ListActivateDate=null;
	    String ListActivateDateStr=null;
	    
	    String ListSubscribersArr=null;
	    JSONArray ListSubscribersJsonArr=null;
	    ArrayList<String> subscribers_doc_list = null;
	    Iterator subitr=null;
	    String Subscriber_Id=null;
	    
	    String ListCampaignArr=null;
	    JSONArray ListCampaignJsonArr=null;
	    ArrayList<Document> campaigns_doc_list =null;
	    Iterator campaignitr=null;
	    Document campaign_doc=null;
	    		
	    String Campaign_Id=null;
	    String List_Id=null;
	    String campaign_status=null;
	    String Campaign_Date=null;
	    
	    
	    
	    JSONArray subscriber_json_arr=null;
	    JSONObject list_campaign_json_obj=null;
	    JSONArray list_campaign_json_arr=null;
	    
	    JSONObject category_final_json_obj=null;
	    
	    try {
	    	mongoClient=ConnectionHelper.getConnection();
            database=mongoClient.getDatabase("salesautoconvert");
            collection=database.getCollection("campaign_list_details");
            campaign_details_collection=database.getCollection("campaign_details");
            
            //Bson filter =and(eq("CreatedBy", CreatedBy),eq("FunnelName", FunnelName),eq("SubFunnelName", SubFunnelName));
            Bson filter =and(eq("CreatedBy", CreatedBy),eq("FunnelName", FunnelName));
            funnelDetailsCursor = collection.find(filter).iterator();
            System.out.println("Subscriber List Found Status : "+funnelDetailsCursor.hasNext());
				while(funnelDetailsCursor.hasNext()) {
						campaign_list_doc=funnelDetailsCursor.next();
						
						ListId=campaign_list_doc.getString("ListId");
					    ListName=campaign_list_doc.getString("ListName");
					    ListSubscriberCount=campaign_list_doc.getInteger("ListSubscriberCount");
					    ListStatus=campaign_list_doc.getString("ListStatus");
					    ListActivateDateStr=campaign_list_doc.getString("ListActivateDateStr");
					  //ListActivateDate=campaign_list_doc.getString("ListActivateDate");
					    
					    
					    subscribers_doc_list = (ArrayList<String>) campaign_list_doc.get("ListSubscribersArr");
					    //System.out.println("subscribers_doc_list : "+subscribers_doc_list);
					    campaigns_doc_list = (ArrayList<Document>) campaign_list_doc.get("ListCampaignArr");
					    //System.out.println("campaigns_doc_list : "+campaigns_doc_list);
					    
					    /*
					    subitr= subscribers_doc_list.listIterator();
						while(subitr.hasNext()){
							Subscriber_Id=(String) subitr.next();
							System.out.println("Subscriber_Id : "+Subscriber_Id);
						}
						campaignitr= campaigns_doc_list.listIterator();
						while(campaignitr.hasNext()){
							campaign_doc=(Document) campaignitr.next();
							System.out.println("campaign_doc : "+campaign_doc);
						}
						*/
					    
						if(campaign_list_doc.getString("SubFunnelName").equals(Category.Explore.toString())){
							System.out.println("Category : "+Category.Explore);
							ListSubscriberCountEx=ListSubscriberCountEx+ListSubscriberCount;
							//System.out.println("ListSubscriberCountEx : "+ListSubscriberCountEx);
							
							category_final_json_obj = new JSONObject();
							subscriber_json_arr = new JSONArray();
							subitr= subscribers_doc_list.listIterator();
							while(subitr.hasNext()){
								Subscriber_Id=(String) subitr.next();
								//System.out.println("Subscriber_Id : "+Subscriber_Id);
								subscriber_json_arr.put(Subscriber_Id);
							}
							list_campaign_json_arr = new JSONArray();
							campaignitr= campaigns_doc_list.listIterator();
							while(campaignitr.hasNext()){
								campaign_doc=(Document) campaignitr.next();
								//System.out.println("campaign_doc : "+campaign_doc);
								list_campaign_json_obj = new JSONObject();
								list_campaign_json_obj.put("Campaign_Id", campaign_doc.getString("Campaign_Id"));
								list_campaign_json_obj.put("List_Id", campaign_doc.getString("List_Id"));
								list_campaign_json_obj.put("campaign_status", campaign_doc.getString("campaign_status"));
								list_campaign_json_obj.put("Campaign_Date", campaign_doc.getString("Campaign_Date"));
								list_campaign_json_arr.put(list_campaign_json_obj);
							}
							
							category_final_json_obj.put("ListId", ListId);
							category_final_json_obj.put("ListStatus", ListStatus);
							category_final_json_obj.put("ListActivateDateStr", ListActivateDateStr);
							category_final_json_obj.put("ListSubscriberCountEx", ListSubscriberCountEx);
							category_final_json_obj.put("subscriber_json_arr", subscriber_json_arr);
							category_final_json_obj.put("list_campaign_json_arr", list_campaign_json_arr);
							
							explore_json_arr.put(category_final_json_obj);
							//System.out.println("explore_json_arr : "+explore_json_arr);
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Entice.toString())){
							System.out.println("Category : "+Category.Entice);
							ListSubscriberCountEnt=ListSubscriberCountEnt+ListSubscriberCount;
							//System.out.println("ListSubscriberCountEnt : "+ListSubscriberCountEnt);
							
							category_final_json_obj = new JSONObject();
							subscriber_json_arr = new JSONArray();
							subitr= subscribers_doc_list.listIterator();
							while(subitr.hasNext()){
								Subscriber_Id=(String) subitr.next();
								//System.out.println("Subscriber_Id : "+Subscriber_Id);
								subscriber_json_arr.put(Subscriber_Id);
							}
							list_campaign_json_arr = new JSONArray();
							campaignitr= campaigns_doc_list.listIterator();
							while(campaignitr.hasNext()){
								campaign_doc=(Document) campaignitr.next();
								//System.out.println("campaign_doc : "+campaign_doc);
								list_campaign_json_obj = new JSONObject();
								list_campaign_json_obj.put("Campaign_Id", campaign_doc.getString("Campaign_Id"));
								list_campaign_json_obj.put("List_Id", campaign_doc.getString("List_Id"));
								list_campaign_json_obj.put("campaign_status", campaign_doc.getString("campaign_status"));
								list_campaign_json_obj.put("Campaign_Date", campaign_doc.getString("Campaign_Date"));
								list_campaign_json_arr.put(list_campaign_json_obj);
							}
							
							category_final_json_obj.put("ListId", ListId);
							category_final_json_obj.put("ListStatus", ListStatus);
							category_final_json_obj.put("ListActivateDateStr", ListActivateDateStr);
							category_final_json_obj.put("ListSubscriberCountEnt", ListSubscriberCountEnt);
							category_final_json_obj.put("subscriber_json_arr", subscriber_json_arr);
							category_final_json_obj.put("list_campaign_json_arr", list_campaign_json_arr);
							
							entice_json_arr.put(category_final_json_obj);
							//System.out.println("entice_json_arr : "+entice_json_arr);
							
							
							
							//entice_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Inform.toString())){
							System.out.println("Category : "+Category.Inform);
							ListSubscriberCountInf=ListSubscriberCountInf+ListSubscriberCount;
							//System.out.println("ListSubscriberCountInf : "+ListSubscriberCountInf);
							
							category_final_json_obj = new JSONObject();
							subscriber_json_arr = new JSONArray();
							subitr= subscribers_doc_list.listIterator();
							while(subitr.hasNext()){
								Subscriber_Id=(String) subitr.next();
								//System.out.println("Subscriber_Id : "+Subscriber_Id);
								subscriber_json_arr.put(Subscriber_Id);
							}
							list_campaign_json_arr = new JSONArray();
							campaignitr= campaigns_doc_list.listIterator();
							while(campaignitr.hasNext()){
								campaign_doc=(Document) campaignitr.next();
								//System.out.println("campaign_doc : "+campaign_doc);
								list_campaign_json_obj = new JSONObject();
								list_campaign_json_obj.put("Campaign_Id", campaign_doc.getString("Campaign_Id"));
								list_campaign_json_obj.put("List_Id", campaign_doc.getString("List_Id"));
								list_campaign_json_obj.put("campaign_status", campaign_doc.getString("campaign_status"));
								list_campaign_json_obj.put("Campaign_Date", campaign_doc.getString("Campaign_Date"));
								list_campaign_json_arr.put(list_campaign_json_obj);
							}
							
							category_final_json_obj.put("ListId", ListId);
							category_final_json_obj.put("ListStatus", ListStatus);
							category_final_json_obj.put("ListActivateDateStr", ListActivateDateStr);
							category_final_json_obj.put("ListSubscriberCountInf", ListSubscriberCountInf);
							category_final_json_obj.put("subscriber_json_arr", subscriber_json_arr);
							category_final_json_obj.put("list_campaign_json_arr", list_campaign_json_arr);
							
							inform_json_arr.put(category_final_json_obj);
							//System.out.println("inform_json_arr : "+inform_json_arr);
							
							
							
							//inform_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Warm.toString())){
							System.out.println("Category : "+Category.Warm);
							ListSubscriberCountWarm=ListSubscriberCountWarm+ListSubscriberCount;
							//System.out.println("ListSubscriberCountWarm : "+ListSubscriberCountWarm);
							
							category_final_json_obj = new JSONObject();
							subscriber_json_arr = new JSONArray();
							subitr= subscribers_doc_list.listIterator();
							while(subitr.hasNext()){
								Subscriber_Id=(String) subitr.next();
								//System.out.println("Subscriber_Id : "+Subscriber_Id);
								subscriber_json_arr.put(Subscriber_Id);
							}
							list_campaign_json_arr = new JSONArray();
							campaignitr= campaigns_doc_list.listIterator();
							while(campaignitr.hasNext()){
								campaign_doc=(Document) campaignitr.next();
								//System.out.println("campaign_doc : "+campaign_doc);
								list_campaign_json_obj = new JSONObject();
								list_campaign_json_obj.put("Campaign_Id", campaign_doc.getString("Campaign_Id"));
								list_campaign_json_obj.put("List_Id", campaign_doc.getString("List_Id"));
								list_campaign_json_obj.put("campaign_status", campaign_doc.getString("campaign_status"));
								list_campaign_json_obj.put("Campaign_Date", campaign_doc.getString("Campaign_Date"));
								list_campaign_json_arr.put(list_campaign_json_obj);
							}
							
							category_final_json_obj.put("ListId", ListId);
							category_final_json_obj.put("ListStatus", ListStatus);
							category_final_json_obj.put("ListActivateDateStr", ListActivateDateStr);
							category_final_json_obj.put("ListSubscriberCountWarm", ListSubscriberCountWarm);
							category_final_json_obj.put("subscriber_json_arr", subscriber_json_arr);
							category_final_json_obj.put("list_campaign_json_arr", list_campaign_json_arr);
							
							warm_json_arr.put(category_final_json_obj);
							//System.out.println("warm_json_arr : "+warm_json_arr);
							
							
							
							//warm_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Connect.toString())){
							System.out.println("Category : "+Category.Connect);
							ListSubscriberCountConnect=ListSubscriberCountConnect+ListSubscriberCount;
							//System.out.println("ListSubscriberCountConnect : "+ListSubscriberCountConnect);
							
							category_final_json_obj = new JSONObject();
							subscriber_json_arr = new JSONArray();
							subitr= subscribers_doc_list.listIterator();
							while(subitr.hasNext()){
								Subscriber_Id=(String) subitr.next();
								//System.out.println("Subscriber_Id : "+Subscriber_Id);
								subscriber_json_arr.put(Subscriber_Id);
							}
							list_campaign_json_arr = new JSONArray();
							campaignitr= campaigns_doc_list.listIterator();
							while(campaignitr.hasNext()){
								campaign_doc=(Document) campaignitr.next();
								//System.out.println("campaign_doc : "+campaign_doc);
								list_campaign_json_obj = new JSONObject();
								list_campaign_json_obj.put("Campaign_Id", campaign_doc.getString("Campaign_Id"));
								list_campaign_json_obj.put("List_Id", campaign_doc.getString("List_Id"));
								list_campaign_json_obj.put("campaign_status", campaign_doc.getString("campaign_status"));
								list_campaign_json_obj.put("Campaign_Date", campaign_doc.getString("Campaign_Date"));
								list_campaign_json_arr.put(list_campaign_json_obj);
							}
							
							category_final_json_obj.put("ListId", ListId);
							category_final_json_obj.put("ListStatus", ListStatus);
							category_final_json_obj.put("ListActivateDateStr", ListActivateDateStr);
							category_final_json_obj.put("ListSubscriberCountConnect", ListSubscriberCountConnect);
							category_final_json_obj.put("subscriber_json_arr", subscriber_json_arr);
							category_final_json_obj.put("list_campaign_json_arr", list_campaign_json_arr);
							
							connect_json_arr.put(category_final_json_obj);
							//System.out.println("connect_json_arr : "+connect_json_arr);
							
							
							//connect_json_arr.put(parseDoc2Json(campaign_list_doc));
						}
						//System.out.println(campaign_list_doc);
				}
				
				category_json_obj.put("funnelName", FunnelName);
				category_json_obj.put("Explore", explore_json_arr);
				category_json_obj.put("Entice", entice_json_arr);
				category_json_obj.put("Inform", inform_json_arr);
				category_json_obj.put("Warm", warm_json_arr);
				category_json_obj.put("Connect", connect_json_arr);
				
				// Bson filter =and(eq("CreatedBy", CreatedBy),eq("FunnelName", FunnelName));
				JSONObject campign_details_category_json_obj=getFunnelDetailForMonitoring(campaign_details_collection,CreatedBy,FunnelName);
				
				//JSONObject campign_details_category_json_obj=getFunnelDetail(CreatedBy,FunnelName);
			    JSONArray campign_details_explore_json_arr=campign_details_category_json_obj.getJSONArray("Explore");
			    category_json_obj.put("Campaign", campign_details_category_json_obj);
			    //System.out.println("campign_details_category_json_obj : "+campign_details_category_json_obj);
			    
           } catch (Exception ex) {
            System.out.println("Error : "+ex.getMessage());
           }
	    //System.out.println(category_json_obj);
	    System.out.println(category_json_obj);
	    return category_json_obj;
	}
	public static JSONObject getFunnelDetailForMonitoring(MongoCollection<Document> collection,String CreatedBy,String funnelName){
	    JSONObject category_json_obj=new JSONObject();
		
	    JSONArray explore_json_arr=new JSONArray();
		JSONArray entice_json_arr=new JSONArray();
		JSONArray inform_json_arr=new JSONArray();
		JSONArray warm_json_arr=new JSONArray();
		JSONArray connect_json_arr=new JSONArray();
		
		MongoCursor<Document> funnelDetailsCursor=null;
	    Document campaign_list_doc=null;
	    try {
	    	
            Bson filter =and(eq("funnelName", funnelName),eq("CreatedBy", CreatedBy));
            funnelDetailsCursor = collection.find(filter).iterator();
            System.out.println("Subscriber List Found Status : "+funnelDetailsCursor.hasNext());
				while(funnelDetailsCursor.hasNext()) {
						campaign_list_doc=funnelDetailsCursor.next();
						if(campaign_list_doc.getString("SubFunnelName").equals(Category.Explore.toString())){
							explore_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Entice.toString())){
							entice_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Inform.toString())){
							inform_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Warm.toString())){
							warm_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Connect.toString())){
							connect_json_arr.put(parseDoc2Json(campaign_list_doc));
						}
				}
				
				category_json_obj.put("funnelName", funnelName);
				category_json_obj.put("Explore", explore_json_arr);
				category_json_obj.put("Entice", entice_json_arr);
				category_json_obj.put("Inform", inform_json_arr);
				category_json_obj.put("Warm", warm_json_arr);
				category_json_obj.put("Connect", connect_json_arr);
           } catch (Exception ex) {
            System.out.println("Error : "+ex.getMessage());
           }
	    //System.out.println(category_json_obj);
	    
	    return category_json_obj;
	}
	
public static JSONObject getFunnelDetail(String CreatedBy,String funnelName){
	    JSONObject category_json_obj=new JSONObject();
		
	    JSONArray explore_json_arr=new JSONArray();
		JSONArray entice_json_arr=new JSONArray();
		JSONArray inform_json_arr=new JSONArray();
		JSONArray warm_json_arr=new JSONArray();
		JSONArray connect_json_arr=new JSONArray();
		
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    MongoCursor<Document> funnelDetailsCursor=null;
	    Document campaign_list_doc=null;
	    try {
	    	mongoClient=ConnectionHelper.getConnection();
            database=mongoClient.getDatabase("salesautoconvert");
            collection=database.getCollection("campaign_details");
            
            Bson filter =and(eq("funnelName", funnelName),eq("CreatedBy", CreatedBy));
            funnelDetailsCursor = collection.find(filter).iterator();
            System.out.println("Subscriber List Found Status : "+funnelDetailsCursor.hasNext());
				while(funnelDetailsCursor.hasNext()) {
						campaign_list_doc=funnelDetailsCursor.next();
						if(campaign_list_doc.getString("SubFunnelName").equals(Category.Explore.toString())){
							explore_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Entice.toString())){
							entice_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Inform.toString())){
							inform_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Warm.toString())){
							warm_json_arr.put(parseDoc2Json(campaign_list_doc));
						}else if(campaign_list_doc.getString("SubFunnelName").equals(Category.Connect.toString())){
							connect_json_arr.put(parseDoc2Json(campaign_list_doc));
						}
				}
				
				category_json_obj.put("funnelName", funnelName);
				category_json_obj.put("Explore", explore_json_arr);
				category_json_obj.put("Entice", entice_json_arr);
				category_json_obj.put("Inform", inform_json_arr);
				category_json_obj.put("Warm", warm_json_arr);
				category_json_obj.put("Connect", connect_json_arr);
           } catch (Exception ex) {
            System.out.println("Error : "+ex.getMessage());
           }
	    //System.out.println(category_json_obj);
	    
	    return category_json_obj;
	}
public static JSONObject parseDoc2Json(Document campaign_list_doc) {
	JSONObject category_json_obj=null;
	String totallead="0";
	try {
		category_json_obj=new JSONObject();
		category_json_obj.put("CreatedBy", campaign_list_doc.getString("CreatedBy"));
		category_json_obj.put("funnelName", campaign_list_doc.getString("funnelName"));
		category_json_obj.put("SubFunnelName", campaign_list_doc.getString("SubFunnelName"));
		category_json_obj.put("CampaignNodeNameInSling", campaign_list_doc.getString("CampaignNodeNameInSling"));
		category_json_obj.put("FromName", campaign_list_doc.getString("FromName"));
		category_json_obj.put("FromEmailAddress", campaign_list_doc.getString("FromEmailAddress"));
		category_json_obj.put("CampaignName", campaign_list_doc.getString("CampaignName"));
		category_json_obj.put("Subject", campaign_list_doc.getString("Subject"));
		category_json_obj.put("Body", campaign_list_doc.getString("Body"));
		category_json_obj.put("Type", campaign_list_doc.getString("Type"));
		category_json_obj.put("Campaign_Id", campaign_list_doc.getString("Campaign_Id"));
		category_json_obj.put("List_Id", campaign_list_doc.getString("List_Id"));
		category_json_obj.put("campaign_status", campaign_list_doc.getString("campaign_status"));
		category_json_obj.put("Campaign_Date", campaign_list_doc.getString("Campaign_Date"));
		ArrayList<Document> campaigns_doc_list =(ArrayList<Document>) campaign_list_doc.get("subscribers");
		//out.println("campaigns_doc_list.size() = "+campaigns_doc_list.size());
		category_json_obj.put("Subscribers_Count", campaigns_doc_list.size());
		totallead=campaign_list_doc.getString("TotalUploadedLeads");
		category_json_obj.put("TotalUploadedLeads", totallead);
		//TotalUploadedLeads
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return category_json_obj;
}
public static JSONArray getFunnelList(String CreatedBy) {
	MongoClient mongoClient = null;
    MongoDatabase database  = null;
    MongoCollection<Document> collection = null;
    JSONArray funnelListJsonArr=new JSONArray();
    
    try {
        mongoClient=ConnectionHelper.getConnection();
        database=mongoClient.getDatabase("salesautoconvert");
        collection=database.getCollection("FirstCategoryMails");
        //collection=database.getCollection("funnel_details");
        //Bson filter1 =eq("CreatedBy", CreatedBy);
        Bson filter1 =eq("CreatedBy", CreatedBy);
        DistinctIterable<String> di = collection.distinct("funnelName", filter1,String.class);
        MongoCursor<String> cursor = di.iterator();
        String funnelName=null;
        while(cursor.hasNext()) {
			funnelName=cursor.next().trim();
		//	System.out.println(funnelName);
			if(funnelName.contains("_EC_") || funnelName.contains("_EnC_") ||funnelName.contains("_IC_") ||funnelName.contains("_WC_") ||funnelName.contains("_CC_") ) {
				
			}
			else {
				if(funnelName!=null && funnelName.length()>0  && !funnelName.equals("null") && !funnelName.equals("undefined") ) {
				
			funnelListJsonArr.put(funnelName);
			}}
		}
	    }
        catch (Exception ex) {
          //  System.out.println("Error : "+ex.getMessage());
            funnelListJsonArr.put(ex.getMessage());
		}
    return funnelListJsonArr;
    }
	public static void addFunnelDetails(Map<String,String> funnel_details_map){
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    try {
        	mongoClient=ConnectionHelper.getConnection();
            database=mongoClient.getDatabase("salesautoconvert");
            collection=database.getCollection("funnel_details");
            Document funnel_details_doc = new Document();
            
            funnel_details_doc.put("remoteuser",funnel_details_map.get("remoteuser"));  
            funnel_details_doc.put("funnelName",funnel_details_map.get("funnelName"));  
            funnel_details_doc.put("fromName",funnel_details_map.get("fromName"));
            funnel_details_doc.put("fromEmailAddress",funnel_details_map.get("fromEmailAddress"));
            funnel_details_doc.put("trackOpens",funnel_details_map.get("trackOpens"));
            funnel_details_doc.put("trackClicks",funnel_details_map.get("trackClicks"));
            funnel_details_doc.put("trackPlainTextClicks",funnel_details_map.get("trackPlainTextClicks"));
            funnel_details_doc.put("googleAnalyticssLinkTracking",funnel_details_map.get("googleAnalyticssLinkTracking"));
            funnel_details_doc.put("autoTweetAfterSending",funnel_details_map.get("autoTweetAfterSending"));
            funnel_details_doc.put("autoPost2SocialMedia",funnel_details_map.get("autoPost2SocialMedia"));
            collection.insertOne(funnel_details_doc);;
                     
           } catch (Exception ex) {
            System.out.println("Error : "+ex.getMessage());
		}
	}
	
	public static JSONObject getFunneldetails(String CreatedBy,String funnel) {
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    MongoCursor<Document> funnelDetailsCursor=null;
	    JSONObject funnelListJson=new JSONObject();
	    Document campaign_list_doc=null;
	    
	    try {
	        mongoClient=ConnectionHelper.getConnection();
	        database=mongoClient.getDatabase("salesautoconvert");
	        collection=database.getCollection("FirstCategoryMails");
	        //collection=database.getCollection("funnel_details");
	        //Bson filter1 =eq("CreatedBy", CreatedBy);
	        Bson filter =and(eq("funnelName", funnel),eq("CreatedBy", CreatedBy),eq("Category", "Explore"));
	        funnelDetailsCursor = collection.find(filter).iterator();
	       
	        String funnelName=null;
	        String FromName=null;
	        String Fromemail=null;
	        while(funnelDetailsCursor.hasNext()) {
	        	 
	        	 campaign_list_doc=funnelDetailsCursor.next();
	        	 FromName=campaign_list_doc.getString("FromName");
	        	 Fromemail=campaign_list_doc.getString("FromEmailAddress");
	        	 funnelListJson.put("FromName", FromName);
	        	 funnelListJson.put("Fromemail", Fromemail);
	        	 
			}
		    }
	        catch (Exception ex) {
	          //  System.out.println("Error : "+ex.getMessage());
	         
			}
	    return funnelListJson;
	    }
	
	
	
	public static JSONObject ViewFunneldetails(String CreatedBy,String funnel) {
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    MongoCursor<Document> funnelDetailsCursor=null;
	    JSONArray allcategjson=new JSONArray();
	    
	    Document campaign_list_doc=null;
	    JSONObject viewmainjs=new JSONObject();
	    
	    JSONArray  categarr=new JSONArray();
	    
	    categarr.put("Entice");
	    categarr.put("Inform");
	    categarr.put("Warm");
	    categarr.put("Connect");
	    String res="";
	    String catgor="Explore";
	  JSONObject  newjs=null;

	    try {
	        mongoClient=ConnectionHelper.getConnection();
	        database=mongoClient.getDatabase("salesautoconvert");
	        collection=database.getCollection("FirstCategoryMails");
	        JSONObject funnelListJson=null;
	      
	        	try {
	       
	        Bson filter =and(eq("funnelName", funnel),eq("CreatedBy", CreatedBy),eq("Category", "Explore"));
	        funnelDetailsCursor = collection.find(filter).iterator();
	       	      int rawleads=0;
	        String campaignName=null;
	        String campcreateddate=null;
	        String mailtempname=null;
	        String docobj=null;
	        if(funnelDetailsCursor.hasNext()) {
	        while(funnelDetailsCursor.hasNext()) {
	        	ArrayList<String> subscribers_doc_list = null;
	        	 funnelListJson=new JSONObject();
	        	 campaign_list_doc=funnelDetailsCursor.next();
	        	 campaignName=campaign_list_doc.getString("campaignName");
	        	 campcreateddate=campaign_list_doc.getString("Created_date");
	        	 mailtempname=campaign_list_doc.getString("Campaign_id");
	        		docobj = campaign_list_doc.toJson();
					newjs = new JSONObject(docobj);
				
	        	 if(newjs.has("Contacts") && newjs.getJSONArray("Contacts").length() >0 ) {
						rawleads=newjs.getInt("rawleads");
					}else {
						
						rawleads=newjs.getJSONArray("SentExploreContacts").length();
					}
	        	 
	        //	 subscribers_doc_list = (ArrayList<String>) campaign_list_doc.get("Contacts");
	        	 funnelListJson.put("Subscribers_Count", rawleads);
	        	 
	        	// subscribers_doc_list = (ArrayList<String>) campaign_list_doc.get("Datasource");
	        	
	        	 
	        	 funnelListJson.put("CampaignName", campaignName);
	        	 funnelListJson.put("Campaign_Date", campcreateddate); 
	        
	        	 funnelListJson.put("Campaign_Id", mailtempname);
	        	 funnelListJson.put("Category", catgor);
	        	 
	        	 allcategjson.put(funnelListJson);
	        	 
			}}else  {
				 funnelListJson=new JSONObject();
				 funnelListJson.put("CampaignName", "");
	        	 funnelListJson.put("Campaign_Date", "");
	        	 funnelListJson.put("Subscribers_Count", "");
	        	 funnelListJson.put("Campaign_Id", "");
	        	 funnelListJson.put("Category", catgor);
	        	 allcategjson.put(funnelListJson);
			}
	        	
	        	}catch (Exception e) {
	        		  e.printStackTrace();
					
				}
	    
	        for(int i=0;i<categarr.length();i++) {
	        JSONObject	funnelListJsonnew=  ViewFunneldetailsForOtherCat(CreatedBy, funnel, categarr.getString(i));
	        allcategjson.put(funnelListJsonnew);
	        }	
	        	
	        viewmainjs.put("AllCategory", allcategjson);
	     
		    }
	        catch (Exception ex) {
	           ex.printStackTrace();
	         
			}
	    return viewmainjs;
	    }
	
	public static JSONObject ViewFunneldetailsForOtherCat(String CreatedBy,String funnel,String catgor) {
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	    MongoCursor<Document> funnelDetailsCursor=null;
	    JSONArray allcategjson=new JSONArray();
	    
	    Document campaign_list_doc=null;
	    JSONObject viewmainjs=new JSONObject();
	    JSONObject funnelListJson=null;
	
	    String res="";
	    
	    try {
	        mongoClient=ConnectionHelper.getConnection();
	        database=mongoClient.getDatabase("salesautoconvert");
	        collection=database.getCollection("FirstCategoryMails");
	        
	       
	        	try {
	       
	     
	        Bson filter =and(eq("funnelName", funnel),eq("CreatedBy", CreatedBy),eq("Category", catgor));
	        funnelDetailsCursor = collection.find(filter).iterator();
	       	      
	        String campaignName=null;
	        String campcreateddate=null;
	        String mailtempname=null;
	        if(funnelDetailsCursor.hasNext()) {
	        while(funnelDetailsCursor.hasNext()) {
	        	ArrayList<String> subscribers_doc_list = null;
	        	 funnelListJson=new JSONObject();
	        	 campaign_list_doc=funnelDetailsCursor.next();
	        	 campaignName=campaign_list_doc.getString("campaignName");
	        	 campcreateddate=campaign_list_doc.getString("Created_date");
	        	 mailtempname=campaign_list_doc.getString("Campaign_id");
	        	
	        		 if(campaign_list_doc.containsKey("SentEmailList")) {
	        			 
	        			 subscribers_doc_list = (ArrayList<String>) campaign_list_doc.get("SentEmailList");
	        			 funnelListJson.put("Subscribers_Count", subscribers_doc_list.size());
	        		 }else {
	        			 funnelListJson.put("Subscribers_Count", "0");
	        		 }
	        		 
	        	 
	        	// subscribers_doc_list = (ArrayList<String>) campaign_list_doc.get("Datasource");
       	 
	        	 funnelListJson.put("CampaignName", campaignName);
	        	 funnelListJson.put("Campaign_Date", campcreateddate);
	        
	        	 funnelListJson.put("Campaign_Id", mailtempname);
	        	 funnelListJson.put("Category", catgor);
	        	 
	        	 allcategjson.put(funnelListJson);
	        	 
			}}else  {
				 funnelListJson=new JSONObject();
				 funnelListJson.put("CampaignName", "");
	        	 funnelListJson.put("Campaign_Date", "");
	        	 funnelListJson.put("Subscribers_Count", "");
	        	 funnelListJson.put("Campaign_Id", "");
	        	 funnelListJson.put("Category", catgor);
	        	 allcategjson.put(funnelListJson);
			}
	        	
	        	}catch (Exception e) {
	        		  e.printStackTrace();
					
				}
	    
	        viewmainjs.put("AllCategory", allcategjson);
	        
		    }
	        catch (Exception ex) {
	           ex.printStackTrace();
	         
			}
	    return funnelListJson;
	    }
	
	public static String findGAdatapersubscriber(String CreatedBy, String SubFunnelName, String FunnelName,
			String CampaignId, SlingHttpServletResponse res) throws JSONException, IOException {
		String resp = "";// ,SlingHttpServletResponse res
		JSONObject subscriber_json_obj = null;

	
		MongoClient mongoClient = null;
		MongoDatabase database = null;
		MongoCollection<Document> RuleEngineCalledForSubscriberData = null;
		JSONObject Allsubscrdata = new JSONObject();
		PrintWriter out = res.getWriter();
		try {
			
			mongoClient=ConnectionHelper.getConnection();

			database = mongoClient.getDatabase("salesautoconvert");

			RuleEngineCalledForSubscriberData = database.getCollection("RuleEngineCalledForSubscriberData");

			resp = "start";
			Bson filter2 = null;
			filter2 = and(eq("CreatedBy", CreatedBy), eq("SubFunnelName", SubFunnelName), eq("FunnelName", FunnelName),
					eq("CampaignId", CampaignId));
			
			FindIterable<Document> filerdata = RuleEngineCalledForSubscriberData.find(filter2);
			MongoCursor<Document> monitordata = filerdata.iterator();
			JSONArray subscriberjsarr = new JSONArray();

			double totalSessiontime = 0;
			String Email = null;
			String recentSessiontime = "0";
			double AlltotalSessiontime = 0;
			double allrecentSessiontime = 0;
			double rst = 0;
			int noclick;
			int TotalClicks = 0;
			 Iterator urlitr=null;
			 Document url_doc=null;
			JSONArray url_json_arr=null;
//			resp = resp + "start3 GAEmail: " + GAEmail;
			// out.println("RuleEngineCalledForSubscriberData ");
			while (monitordata.hasNext()) {
			
				Document campaignWisedata = monitordata.next();
				try {
				
					subscriber_json_obj = new JSONObject();
					Email = campaignWisedata.getString("SubscriberEmail");
					subscriber_json_obj.put("Email", Email);
//					System.out.println("Email= " + Email);
					noclick = campaignWisedata.getInteger("NoOfUrlClicks");
//					System.out.println("noclick= " + noclick);
//					TotalClicks =TotalClicks + noclick;
//					TotalClicks = (int) (TotalClicks + noclick);
//					System.out.println("TotalClicks= " + TotalClicks);
					totalSessiontime = campaignWisedata.getInteger("TotalSesionDuration");
//					System.out.println("totalSessiontime= " + totalSessiontime);
					subscriber_json_obj.put("totalSessiontime", totalSessiontime);
					AlltotalSessiontime = AlltotalSessiontime + totalSessiontime;
					subscriber_json_obj.put("totalVisits", "0");
					recentSessiontime = campaignWisedata.getString("Recent_AvgSesionDuration");
//					System.out.print("recentSessiontime " + recentSessiontime);
					rst = Double.parseDouble(recentSessiontime);

					allrecentSessiontime = allrecentSessiontime + rst;
					subscriber_json_obj.put("recentSessiontime", recentSessiontime);
					subscriber_json_obj.put("recentVisits", "0");
					// out.println("subscriber_json_obj= "+subscriber_json_obj);

					try {
						JSONObject url_json_obj=null;
						List<Document> PageUrls = (List<Document>) campaignWisedata.get("PageUrls");
						
						TotalClicks =TotalClicks + PageUrls.size();
						url_json_arr=new JSONArray();
						urlitr= PageUrls.listIterator();
						while(urlitr.hasNext()){
							url_doc=(Document) urlitr.next();
							//System.out.println("campaign_doc : "+campaign_doc);
							url_json_obj = new JSONObject();
							url_json_obj.put("URL", url_doc.getString("url"));
							
							try {
								url_json_obj.put("AvgTime", url_doc.getInteger("AvgTimeOnPage"));
							}catch (Exception e) {
								url_json_obj.put("AvgTime", url_doc.getDouble("AvgTimeOnPage"));
//								out.println("url_json_obj= " + e);
							}
						
								
							
//							out.println("url_json_obj= " + url_json_obj);
						    url_json_arr.put(url_json_obj);
						}
//						out.println("url_json_arr= " + url_json_arr);
						
						subscriber_json_obj.put("PageUrls", url_json_arr);
					} catch (Exception e) {
						// TODO: handle exception
						out.println("exc= " + e);
					}
					// TotalClicks=TotalClicks+NoOfUrlClicks;
					try {
						subscriber_json_obj.put("Name", "NA");
						subscriber_json_obj.put("Source", "NA");
						// SubscriberId
					} catch (Exception e) {
						// TODO: handle exception
						subscriber_json_obj.put("Name", "NA");
						subscriber_json_obj.put("Source", "NA");

					}
					subscriberjsarr.put(subscriber_json_obj);
					System.out.println("ga_user_json_obj= " + subscriber_json_obj);
				} catch (Exception e) {
					out.println("exccc = " + e);
				}
			}
			monitordata.close();

			JSONObject totalllead = new JSONObject();
			totalllead.put("totalSessiontime", AlltotalSessiontime);
			totalllead.put("recentSessiontime", allrecentSessiontime);
			totalllead.put("clicked", TotalClicks);

			// out.println("AlltotalSessiontime= " + AlltotalSessiontime);///
			Allsubscrdata.put("CampaignId", CampaignId);
			Allsubscrdata.put("Category", SubFunnelName);
			Allsubscrdata.put("funnelname", FunnelName);
			Allsubscrdata.put("TotalLeadData", totalllead);
			Allsubscrdata.put("ActiveUsers", subscriberjsarr);

//			logger.info("Data for  GoogleAnalytics User Found ga_user_json_obj : " + ga_user_json_obj);
		} catch (Exception e) {
			// out.println("exc in findGAUserCredentials: " + e);
			Allsubscrdata.put("excmongo", e.toString());
		} finally {

			if (mongoClient != null) {
				mongoClient.close();
				mongoClient = null;
			}
			// ConnectionHelper.closeConnection(mongoClient);

		}
		String resjs="{\"CampaignId\":\"1650\",\"Category\":\"Explore\",\"funnelname\":\"leadfun12Sept\",\"TotalLeadData\":{\"totalSessiontime\":1057,\"recentSessiontime\":10,\"clicked\":16},\"ActiveUsers\":[{\"Email\":\"tejal.jabade10@gmail.com\",\"totalSessiontime\":897,\"totalVisits\":\"0\",\"recentSessiontime\":\"0\",\"recentVisits\":\"0\",\"PageUrls\":[{\"URL\":\"bizlem.com\",\"AvgTime\":32.5},{\"URL\":\"bizlem.com/contactUs.html\",\"AvgTime\":2}],\"Name\":\"tejal\",\"Source\":\"Friend\"},{\"Email\":\"vivek@bizlem.com\",\"totalSessiontime\":160,\"totalVisits\":\"0\",\"recentSessiontime\":\"10.0\",\"recentVisits\":\"0\",\"PageUrls\":[{\"URL\":\"bizlem.com\",\"AvgTime\":3},{\"URL\":\"bizlem.com\",\"AvgTime\":3},{\"URL\":\"bizlem.com/aboutUs.html\",\"AvgTime\":1.67},{\"URL\":\"bizlem.com/aboutUs.html\",\"AvgTime\":1.5},{\"URL\":\"bizlem.com/industries.html\",\"AvgTime\":1.67}],\"Name\":\"vivek\",\"Source\":\"Friend\"}]}";
		JSONObject js=new JSONObject(resjs);
		return Allsubscrdata.toString();
	}
	
	public static ArrayList<String> getcategorybyfunnel(String CreatedBy,String funnel) {
		MongoClient mongoClient = null;
	    MongoDatabase database  = null;
	    MongoCollection<Document> collection = null;
	   
	    ArrayList<String> funnellist = new ArrayList<String>();
	    try {
	        mongoClient=ConnectionHelper.getConnection();
	        database=mongoClient.getDatabase("salesautoconvert");
	        collection=database.getCollection("FirstCategoryMails");
	        //collection=database.getCollection("funnel_details");
	        //Bson filter1 =eq("CreatedBy", CreatedBy);
	        Bson filter1 =  and(eq("CreatedBy", CreatedBy), eq("funnelName", funnel));
	        DistinctIterable<String> di = collection.distinct("Category", filter1,String.class);
	        MongoCursor<String> cursor = di.iterator();
	        String subfunnelName=null;
	        while(cursor.hasNext()) {
	        	subfunnelName=cursor.next().trim();
				funnellist.add(subfunnelName);
			}
		    }
	        catch (Exception ex) {
	          //  System.out.println("Error : "+ex.getMessage());
	        	funnellist.add(ex.getMessage());
			}
	    return funnellist;
	    }
	
	
	public static String moveContacts(String destinationFunnel,String emailobj,String createdby,SlingHttpServletResponse resp) {
		MongoClient mongoClient = null;
		 MongoDatabase database  = null;
		 MongoCollection<Document> collection = null;
		// MongoCursor<Document> cursor  =  null;
		 MongoClientURI connectionString = null;
		 String uri = null;
		 String res=null;
		 destinationFunnel=destinationFunnel.trim();
		 PrintWriter out = null;
		 try {
			 out=resp.getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 try {
				System.setProperty("javax.net.ssl.trustStore","/etc/ssl/firstTrustStore");
				System.setProperty("javax.net.ssl.trustStorePassword","bizlem123");
				System.setProperty ("javax.net.ssl.keyStore","/etc/ssl/MongoClientKeyCert.jks");
				System.setProperty ("javax.net.ssl.keyStorePassword","bizlem123");
				uri = "mongodb://localhost:27017/?ssl=true";
			   connectionString = new MongoClientURI(uri);
			   mongoClient = new MongoClient(connectionString);
			   database = mongoClient.getDatabase("salesautoconvert");
			   collection=database.getCollection("FirstCategoryMails");
			  /* Bson condition = new Document("$eq", sourceFunnel);
			  
				Bson filter = new Document("funnelName", condition).append("Category", "Explore");
				
				FindIterable<Document> fi = collection.find(filter);      
		        cursor = fi.iterator();*/
		        //Bson conditionMove = new Document("$eq", destinationFunnel);
			   Bson query = and(eq("funnelName", destinationFunnel),eq("CreatedBy", createdby), eq("Category", "Explore"));
				//Bson query = new Document("funnelName", destinationFunnel).append("Category", "Explore");
			/*	while (cursor.hasNext()) {*/
				//	JSONArray arrdata= new JSONArray();
					/*JSONObject obj=new JSONObject(cursor.next().toJson());	*/
				JSONObject obj=new JSONObject();
				obj.put("Email", emailobj);
					if(emailobj !=null && emailobj.length()>0){
					
						Document newContact = Document.parse(obj.toString());
						collection.updateOne(query,Updates.addToSet("Contacts", newContact));
						
					
						Document newDocument = new Document();
						newDocument.put("updateflag", "1");

						Document updateObj = new Document();
						updateObj.put("$set", newDocument);

						collection.updateOne(query, updateObj);
						out.println("Moved");
					}
					
					
				/*	}*/
				
				//collection.deleteOne(filter);
				res = "Success";
			   
		} catch (Exception e) {
			res = "Fail";
		}
		finally{
			/*if(null!=cursor){
				cursor.close();
				cursor = null;
				}*/
			if(null!=mongoClient){
			mongoClient.close();
			mongoClient = null;
			}
		}
		
		return res;
	}
	

}
