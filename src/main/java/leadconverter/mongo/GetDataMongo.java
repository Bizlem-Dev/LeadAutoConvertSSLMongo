package leadconverter.mongo;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.sling.commons.json.JSONObject;
import org.json.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class GetDataMongo {

	public static void main(String[] args) {

		// phplistdata("viki_gmail.com", "today302019");
		campaignlist("viki_gmail.com", "today302019", "Explore");
	}

	public static JSONObject phplistdata(String username, String funnelname) {

		JSONObject mainjson = new JSONObject();
		JSONArray arr = new JSONArray();
		JSONObject datajson = new JSONObject();
		ArrayList<String> funnellist = new ArrayList<String>();

		try {
			// String
			// urlreturn=sendGet("http://development.bizlem.io:8082/portal/servlet/service/createCampaign.getListFunnelDetail?userName="+username.replace("@",
			// "_")+"&funnelName="+funnelname.replace(" ", "%20"));
//			String urlreturn = sendGet(
//					"http://development.bizlem.io:8082/portal/servlet/service/createCampaign.getListFunnelDetail?userName="
//							+ username + "&funnelName="+funnelname);
		
			JSONObject urljson=FunnelDetailsMongoDAO.getListFunnelDetail(username.replace("@", "_"),funnelname,"");
//			JSONObject urljson = new JSONObject(urlreturn);
			System.out.println(urljson);

			JSONObject campaignjson = urljson.getJSONObject("Campaign");
			Iterator<String> iterator = campaignjson.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if(!key.equals("funnelName")) {
					if(campaignjson.getJSONArray(key).length()>0) {
				funnellist.add(key);
					}
				}
			}
			datajson.put("FunnelList", funnellist);
			datajson.put("username", username);
			datajson.put("FunnelName", funnelname);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datajson;

	}

	public static JSONObject campaignlist(String username, String funnelname, String subfunnelname) {

		JSONArray arr = new JSONArray();
		JSONObject listadd = new JSONObject();

		try {
			// String
			// urlreturn=sendGet("http://development.bizlem.io:8082/portal/servlet/service/createCampaign.getListFunnelDetail?userName="+username.replace("@",
			// "_")+"&funnelName="+funnelname.replace(" ", "%20"));
//			String urlreturn = sendGet(
//					"http://development.bizlem.io:8082/portal/servlet/service/createCampaign.getListFunnelDetail?userName="
//							+ username + "&funnelName=" + funnelname);
//			System.out.println(urlreturn);
//			JSONObject urljson = new JSONObject(urlreturn);
			JSONObject urljson=FunnelDetailsMongoDAO.getListFunnelDetail(username.replace("@", "_"),funnelname,"");
			JSONObject campaignjson = urljson.getJSONObject("Campaign");
			System.out.println("campaignjson : " + campaignjson);
			org.apache.sling.commons.json.JSONArray subfunnelarray = campaignjson.getJSONArray(subfunnelname);
			System.out.println("subfunnelarray : " + subfunnelname);
			for (int i = 0; i < subfunnelarray.length(); i++) {
				JSONObject listobj = subfunnelarray.getJSONObject(i);
				listadd.put("CampaignName", listobj.getString("CampaignName"));
				listadd.put("Campaign_Id", listobj.getString("Campaign_Id"));

			}
			System.out.println(listadd);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listadd;

	}

	private static String sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		return response.toString();

	}

	public static String uploadToServer(String urlstr, org.json.JSONObject datajson) throws IOException, JSONException {
		StringBuffer response = null;

		try {

			int responseCode = 0;
			String urlParameters = "";

			URL url = new URL(urlstr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(datajson.toString());
			wr.flush();
			wr.close();

			responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			System.out.println(response.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();

	}

}
