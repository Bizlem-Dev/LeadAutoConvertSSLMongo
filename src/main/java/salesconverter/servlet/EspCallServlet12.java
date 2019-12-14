package salesconverter.servlet;

import static com.mongodb.client.model.Filters.eq;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jxl.*;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.bson.conversions.Bson;
import org.jsoup.Jsoup;
import org.osgi.service.http.HttpService;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

import Dashboard.mongodbdata;
import Dashboard.impl.ReadExcel_OutCome;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

import salesconverter.freetrail.FreetrialShoppingCartUpdate;
import salesconverter.freetrail.GetValidityInfoFromEmail;
import salesconverter.mongo.ConnectionHelper;
import salesconverter.mongo.FunnelDetailsMongoDAO;
import salesconverter.mongo.GetGADataForMonitoringLeads;
import salesconverter.mongo.MongoDAO;
import services.CallPostService;

@Component(immediate = true, metatype = false)
@Service(value = javax.servlet.Servlet.class)
@Properties({ @Property(name = "service.description", value = "Save product Servlet"),
		@Property(name = "service.vendor", value = "VISL Company"),
		@Property(name = "sling.servlet.paths", value = { "/servlet/service/Salesconverter" }),
		@Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default"),
		@Property(name = "sling.servlet.extensions", value = { "hotproducts", "cat", "latestproducts", "brief",
				"prodlist", "catalog", "viewcart", "productslist", "addcart", "createproduct", "checkmodelno",
				"productEdit" }) })
@SuppressWarnings("serial")
public class EspCallServlet12 extends SlingAllMethodsServlet {
//Salesconverter.WelcomLeadsales
	// Salesconverter.InsertRuleURLS
	@Reference
	private SlingRepository repo;
	final String FILEEXTENSION[] = { "csv" };

	final int NUMBEROFRESULTSPERPAGE = 10;
	private static final long serialVersionUID = 1L;
	String fileType = "file";
	JSONObject mainjsonobject = null;
	Session session = null;
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = response.getWriter();
//		out.println("Get");
		//Salesconverter.checkValidUser?email=jobs@bizlem.com
		// http://development.bizlem.io:8082/portal/servlet/service/ui.shopping45?email=viki@gmail.com&group=G1&SubscriberCount=10
		//http://development.bizlem.io:8082/portal/servlet/service/ui.shopping45?email=viki@gmail.comcat=Explore&fun=today302019&campid=1628
	
		if (request.getRequestPathInfo().getExtension().equals("InsertRuleURLS")) {
				try {
					
					out.println("In monitor Data If");
					session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
					String hj78="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";
					//readexcel("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");
				//	new ReadExcel_OutCome().readexcel(hj78);
					out.println(new ReadExcel_OutCome().readexcel(hj78));
//					Mongo mongo = new Mongo("localhost", 27017);
//					DB db = mongo.getDB("salesautoconvert");
//					
//					DBCollection collection = db.getCollection("RuleEngineCalledForSubscriberData");
//
//				String remoteuser=request.getParameter("email").replace("@", "_");
//				System.out.println("Remote user :"+remoteuser);
//				JSONObject rulejssave=new JSONObject();
//				DBObject dbObject = (DBObject)JSON.parse(rulejssave.toString());
//				
//				collection.insert(dbObject);
				
			}
			
		catch (Exception e) {

		e.printStackTrace();
		}
		}

		
		if (request.getRequestPathInfo().getExtension().equals("funedit")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ffunnel-edit").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("texts-only-template")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ftexts-only-template").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("text-photos-template")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ftext-photos-template").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("strat-from-scratch")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fstrat-from-scratch").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("smtp-setup")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fsmtp-setup").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("simpleLayout")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fsimpleLayout").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("set-up-campaign")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fset-up-campaign").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("set-up-campaign-complete")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fset-up-campaign-complete").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("select-tamplate")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fselect-tamplate").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("select-tamplate-file")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fselect-tamplate-file").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("news-letter-tamplate")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fnews-letter-tamplate").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("list")) {
			try {
				request.getRequestDispatcher("/content/mainui/.flist").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("index")) {
			try {
			//	request.getRequestDispatcher("/content/mainui/.findex").forward(request, response);
				 request.getRequestDispatcher("/content/static/.SalesConvertMainPage").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("gallery-template")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fgallery-template").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("funnel")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ffunnel").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("configuration")) {
			try {
				request.getRequestDispatcher("/content/mainui/.fconfiguration").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("googleanalytics")) {
			try {
				request.getRequestDispatcher("/content/mainui/.googleAnalytics").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		}
		else if (request.getRequestPathInfo().getExtension().equals("LACwebservice")) {
			try {
				request.getRequestDispatcher("/content/static/.LACWebservice").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		}
		else if (request.getRequestPathInfo().getExtension().equals("loginuser")) {
			try {
				request.getRequestDispatcher("/content/mainui/.loginuser").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("getloginuser")) {
			try {
				request.getRequestDispatcher("/content/mainui/.getloginuser").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("datasource")) {
			try {
				request.getRequestDispatcher("/content/static/.DataSourceFile").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("test-rule-engine")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ftest-rule-engine").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("free-trail-expire")) {
			try {
				request.getRequestDispatcher("/content/mainui/.ffree-trail-expire").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		}else if (request.getRequestPathInfo().getExtension().equals("MailTemplSetup")) {
			try {
				request.getRequestDispatcher("/content/static/.MailTemplateSetupSales").forward(request, response);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("get_subscriber_status")) {
			// out.println("in callservice");
			String logged_in_user_email = request.getParameter("rm_email");
			// FreeTrialandCart cart = new FreeTrialandCart();
			// String freetrialstatus = cart.checkfreetrial(logged_in_user_email);
			// mailtangynode = cart.getMailtangyNode(freetrialstatus, logged_in_user_email,
			// "", session, response);

			MongoDAO mdao = new MongoDAO();
			long subscribers_count = mdao.getSubscriberCountForLoggedInUserForFreeTrail("subscribers_details",
					logged_in_user_email);
			String free_trail_status = new FreetrialShoppingCartUpdate().checkfreetrial(logged_in_user_email);
			// long subscribers_count=2000;
			// String free_trail_status="0";
			// out.println("logged_in_user_email : "+logged_in_user_email);
			// out.println("subscribers_count : "+subscribers_count);
			// out.println("free_trail_status : "+free_trail_status);
			if (subscribers_count <= 2000 && free_trail_status.equals("0")) {
				System.out.println("Free Train is Active");
				out.println("Free Train is Active");
			} else if (free_trail_status.equals("1")) {
				System.out.println("Free Train Date Expired");
				out.println("Free Train Date Expired");
			} else if (subscribers_count > 2000) {
				System.out.println("Subscriber Count is More");
				out.println("Subscriber Count is More");
			}

		} else if (request.getRequestPathInfo().getExtension().equals("grouplist")) {
			try {
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));

				String email = request.getParameter("email").replace("@", "_");
				JSONObject grjsa = new FreetrialShoppingCartUpdate().getLeadAutoConverterGroupList(email, session,
						response);
				out.println(grjsa);
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		} else if (request.getRequestPathInfo().getExtension().equals("checkValidUser")) {

			try {
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
				String email = request.getParameter("email").replace("@", "_");
				String jsresp;
				try {
					jsresp = new GetValidityInfoFromEmail().getValidityInfo(email, session, response);
					out.println(jsresp);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if (request.getRequestPathInfo().getExtension().equals("getSKU")) {

			try {
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
				String email = request.getParameter("email").replace("@", "_");
				JSONObject  jsresp=null;;
				try {
					jsresp = new GetValidityInfoFromEmail().getSKUInfo(email, session, response);
					out.println(jsresp);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		else if (request.getRequestPathInfo().getExtension().equals("testlac1")) {
			try {
//				request.getRequestDispatcher("/content/static/.LeadAutoConvert").forward(request, response);
				RequestDispatcher dis = request.getRequestDispatcher("/content/static/.Home");
				dis.forward(request, response);
				
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		}else if (request.getRequestPathInfo().getExtension().equals("LAC")) {
			try {
				//out.println("get");
//				request.getRequestDispatcher("/content/static/.LeadAutoConvert").forward(request, response);
				RequestDispatcher dis = request.getRequestDispatcher("/content/static/.LACMAIN");
				dis.forward(request, response);
				
			} catch (Exception ex) {
				out.print(ex.getMessage());
			}
		}  else if(request.getRequestPathInfo().getExtension().equals("googleanalytics")) {
		     try {
				  request.getRequestDispatcher("/content/mainui/.googleAnalytics").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 } else if(request.getRequestPathInfo().getExtension().equals("moveMonit")) {
		     try {
				  request.getRequestDispatcher("/content/static/.MoveFunnelMonitertest").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }else if(request.getRequestPathInfo().getExtension().equals("provisioning")) {
		     try {
				  request.getRequestDispatcher("/content/static/.SalesSetupProvisioning").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }else if(request.getRequestPathInfo().getExtension().equals("webservice")) {
		     try {
				  request.getRequestDispatcher("/content/static/.WebServiceIntegration").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }else if(request.getRequestPathInfo().getExtension().equals("LACCart")) {
		     try {
				  request.getRequestDispatcher("/content/static/.LACAndSalesCon").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }else if(request.getRequestPathInfo().getExtension().equals("BoucedEmails")) {
		     try {
				  request.getRequestDispatcher("/content/static/.UploadBouncedEmail").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }
		 else if(request.getRequestPathInfo().getExtension().equals("MarketingDashboard")) {
		     try {
				  request.getRequestDispatcher("/content/static/.SalesDashboard").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }
		 else if(request.getRequestPathInfo().getExtension().equals("MarketingWelcom")) {
		     try {
				  request.getRequestDispatcher("/content/static/.SalesDashboard").forward(request, response);
			     } catch (Exception ex) {
				     out.print(ex.getMessage());
			     }
		 }
		
		
//LACAndSalesCon  
		 else if(request.getRequestPathInfo().getExtension().equals("MonitorData")) {
				try {
					
					System.out.println("In monitor Data If");
					session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
				
				//remoteuser=viki@gmail.com&category=Explore&funnel=&campid=
				String remoteuser=request.getParameter("email").replace("@", "_");
				System.out.println("Remote user :"+remoteuser);
				String jj = GetGADataForMonitoringLeads.findGAdatapersubscriber(remoteuser, request.getParameter("category"), request.getParameter("funnel"), request.getParameter("campid"),response);
				out.println(jj);
//				String jj12 = GetGADataForMonitoringLeads.findGAdatapersubscriber("viki_gmail.com", "Explore", "today302019", "1628");
//				out.println("jj== ========= " + jj12);
			
			}
			
		catch (Exception e) {

		System.out.println("Exception ex : "+e.getMessage());
		}
		} else if(request.getRequestPathInfo().getExtension().equals("Move")) {
			try {
				
				System.out.println("In monitor Data If");
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
			
			
			String remoteuser=request.getParameter("email").replace("@", "_");
			String group=request.getParameter("group").replace("@", "_");
			System.out.println("Remote user :"+remoteuser);
			String expstatus= new FreetrialShoppingCartUpdate().checkFreeTrialExpirationStatus(remoteuser);
		 	int quantity=0;
		 	out.println("Remote expstatus :"+expstatus+"= group::"+group);
		 	String checkquantity="";
	 		Node shoppingnode=new FreetrialShoppingCartUpdate().getLeadAutoConverterNode(expstatus, remoteuser.replace("@", "_"), group, session, response);
	 		out.println("Remote shoppingnode :"+shoppingnode);
//			String jj = GetGADataForMonitoringLeads.findGAdatapersubscriber(remoteuser, request.getParameter("category"), request.getParameter("funnel"), request.getParameter("campid"),response);
//			out.println(jj);
//			String jj12 = GetGADataForMonitoringLeads.findGAdatapersubscriber("viki_gmail.com", "Explore", "today302019", "1628");
//			out.println("jj== ========= " + jj12);
		
		}
		
	catch (Exception e) {

	System.out.println("Exception ex : "+e.getMessage());
	}
	}else if(request.getRequestPathInfo().getExtension().equals("AddRuleSales")) {
	     try {
			  request.getRequestDispatcher("/content/static/.RuleCreationIframeSales").forward(request, response);
		     } catch (Exception ex) {
			     out.print(ex.getMessage());
		     }
	 }//welcomesales
	else if(request.getRequestPathInfo().getExtension().equals("welcomesalespage")) {
	     try {
			  request.getRequestDispatcher("/content/static/.welcomesales").forward(request, response);
		     } catch (Exception ex) {
			     out.print(ex.getMessage());
		     }
	 }//Welcomepageforsales
	else if(request.getRequestPathInfo().getExtension().equals("WelcomLeadsales")) {
		// out.print("GET WelcomeToLeadsales ");
	     try {
			  request.getRequestDispatcher("/apps/static/.Welcomepageforsales").forward(request, response);
		     } catch (Exception ex) {
			    ex.printStackTrace();
		     }
	 }
	else if (request.getRequestPathInfo().getExtension().equals("DashboardSales")) {
		try {
		//	out.println("get");
			String email=request.getParameter("email");
			out.println(new mongodbdata().Funnellist(email,response));
			
			
			JSONObject dashboardjson=new JSONObject("{\"data\":[{\"convLeads\":250,\"Geolocation\":[[\"Country\",\"HotLeads\",\"Converted\" ,\"Explore\"],[\"India\",\"900\",\"100\",\"10\"],[\"USA\",\"700\",\"200\",\"100\"],[\"Spain\",\"700\",\"200\",\"100\"],[\"France\",\"500\",\"50\",\"100\"],[\"Australia\",\"200\",\"20\",\"100\"]],\"standout\":[{\"C3\":{\"upgrade\":1000,\"session time\":3000,\"traffic\":300},\"C1\":{\"upgrade\":300,\"session time\":1000,\"traffic\":200},\"C2\":{\"upgrade\":500,\"session time\":2000,\"traffic\":400}}],\"upcomingCampaign\":[{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"ABC\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"DEF\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"GHI\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"JKL\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"MNO\",\"leads\":10000}],\"funnelName\":\"Funnel1\",\"dripRate\":{\"rate\":\"80\",\"convRate\":\"30%\",\"rawLead\":1000,\"conv\":300},\"upgradationRate\":{\"data\":[{\"rate\":\"50%\",\"subFunnel\":\"SubFunnel 1\",\"tableData\":[{\"rate\":\"2%\",\"source\":\"facebook\"},{\"rate\":\"4%\",\"source\":\"twitter\"},{\"rate\":\"2%\",\"source\":\"insta\"}]},{\"rate\":\"50%\",\"subFunnel\":\"SubFunnel 2\",\"tableData\":[{\"rate\":\"2%\",\"source\":\"facebook\"},{\"rate\":\"4%\",\"source\":\"twitter\"},{\"rate\":\"2%\",\"source\":\"insta\"}]}],\"subFunnel\":[\"SubFunnel 1\",\"SubFunnel 2\"]},\"funnelLeadCategory\":[{\"week1\":[{\"warm\":200,\"inform\":300,\"explore\":1000,\"entice\":500,\"connect\":100}],\"week2\":[{\"warm\":400,\"inform\":500,\"explore\":2000,\"entice\":1000,\"connect\":100}],\"week3\":[{\"warm\":300,\"inform\":1000,\"explore\":3000,\"entice\":1500,\"connect\":200}],\"week4\":[{\"warm\":500,\"inform\":1000,\"explore\":4000,\"entice\":2000,\"connect\":500}],\"week5\":[{\"warm\":10,\"inform\":30,\"explore\":100,\"entice\":50,\"connect\":10}]}],\"hotLeads\":250,\"convRate\":50,\"missedOppurtinities\":{\"Others\":15,\"Price\":10,\"Quality\":20,\"Competitiors\":40,\"Specs\":30},\"campaignFunnel\":[{\"warm\":200,\"inform\":300,\"explore\":1000,\"entice\":500,\"connect\":100}],\"active user\":{\"chartData\":[{\"dec-18\":{\"funnel end\":900,\"unsubscribe\":300,\"headroom\":400,\"active user\":300,\"spam\":1500},\"jan-18\":{\"funnel end\":600,\"unsubscribe\":300,\"headroom\":600,\"active user\":400,\"spam\":2000},\"nov-18\":{\"funnel end\":800,\"unsubscribe\":200,\"headroom\":300,\"active user\":200,\"spam\":1000},\"feb-18\":{\"funnel end\":1500,\"unsubscribe\":2000,\"headroom\":50,\"active user\":100,\"spam\":20}}],\"tableData\":{\"funnel end\":600,\"unsubscribe\":400,\"headroom\":1200,\"active user\":100,\"spam\":500}},\"campaignFunneldata\":[{\"rate\":5,\"leads\":200,\"source\":\"facebook\"},{\"rate\":1,\"leads\":50,\"source\":\"instagram\"},{\"rate\":3,\"leads\":250,\"source\":\"mail\"}],\"outcome\":[{\r\n" + 
					"\"Parameter\":\"Reveneue\",\"Actual\":85,\"planned\":92},\r\n" + 
					"{\"Parameter\":\"Conversation\",\"Actual\":82,\"planned\":95},\r\n" + 
					"{\"Parameter\":\"Free trial\",\"Actual\":75,\"planned\":80}]\r\n" + 
					",\"rawLeads\":500},{\"convLeads\":150,\"Geolocation\":[[\"Country\",\"HotLeads\",\"Converted\"],[\"Algeria\",\"900\",\"100\"],[\"Angola\",\"700\",\"200\"],[\"Cameroon\",\"700\",\"200\"],[\"Brazil\",\"500\",\"50\"],[\"China\",\"200\",\"20\"]],\"standout\":[{\"C3\":{\"upgrade\":1000,\"session time\":3000,\"traffic\":300},\"C1\":{\"upgrade\":300,\"session time\":1000,\"traffic\":200},\"C2\":{\"upgrade\":500,\"session time\":2000,\"traffic\":400}}],\"upcomingCampaign\":[{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"ABC\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"DEF\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"GHI\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"JKL\",\"leads\":10000},{\"date\":\"dd-MM-yyyy\",\"subFunnelName\":\"FUnnel2\",\"leads\":10000}],\"funnelName\":\"Funnel2\",\"dripRate\":{\"rate\":\"80\",\"convRate\":\"30%\",\"rawLead\":1000,\"conv\":300},\"mostActiveLeads\":{\"visit\":[{\"lessthan\":2,\"greaterthan\":4,\"lead\":100},\r\n" + 
					"	{\"lessthan\":5,\"greaterthan\":9,\"lead\":130},\r\n" + 
					"	{\"lessthan\":10,\"greaterthan\":15,\"lead\":185},\r\n" + 
					"	{\"lessthan\":15,\"greaterthan\":20,\"lead\":235}],\r\n" + 
					"\"session\":[{\"lessthan\":29,\"greaterthan\":30,\"lead\":200},\r\n" + 
					"	{\"lessthan\":30,\"greaterthan\":59,\"lead\":180},\r\n" + 
					"	{\"lessthan\":60,\"greaterthan\":119,\"lead\":115},\r\n" + 
					"	{\"lessthan\":120,\"greaterthan\":180,\"lead\":15}]\r\n" + 
					"},\"upgradationRate\":{\"data\":[{\"rate\":\"50%\",\"subFunnel\":\"SubFunnel 1\",\"tableData\":[{\"rate\":\"2%\",\"source\":\"facebook\"},{\"rate\":\"4%\",\"source\":\"twitter\"},{\"rate\":\"2%\",\"source\":\"insta\"}]},{\"rate\":\"50%\",\"subFunnel\":\"SubFunnel 2\",\"tableData\":[{\"rate\":\"2%\",\"source\":\"facebook\"},{\"rate\":\"4%\",\"source\":\"twitter\"},{\"rate\":\"2%\",\"source\":\"insta\"}]}],\"subFunnel\":[\"SubFunnel 1\",\"SubFunnel 2\"]},\"funnelLeadCategory\":[{\"week1\":[{\"warm\":100,\"inform\":300,\"explore\":1000,\"entice\":500,\"connect\":100}],\"week2\":[{\"warm\":200,\"inform\":500,\"explore\":2000,\"entice\":1000,\"connect\":100}],\"week3\":[{\"warm\":400,\"inform\":1000,\"explore\":3000,\"entice\":1500,\"connect\":200}],\"week4\":[{\"warm\":600,\"inform\":2000,\"explore\":4000,\"entice\":5000,\"connect\":500}],\"week5\":[{\"warm\":10,\"inform\":30,\"explore\":100,\"entice\":50,\"connect\":10}]}],\"hotLeads\":250,\"convRate\":50,\"missedOppurtinities\":{\"Others\":15,\"Price\":10,\"Quality\":20,\"Competitiors\":40,\"Specs\":30},\"campaignFunnel\":[{\"warm\":200,\"inform\":300,\"explore\":1000,\"entice\":500,\"connect\":100}],\"active user\":{\"chartData\":[{\"dec-18\":{\"funnel end\":900,\"unsubscribe\":300,\"headroom\":400,\"active user\":300,\"spam\":1500},\"jan-18\":{\"funnel end\":600,\"unsubscribe\":300,\"headroom\":600,\"active user\":400,\"spam\":2000},\"nov-18\":{\"funnel end\":800,\"unsubscribe\":200,\"headroom\":300,\"active user\":200,\"spam\":1000},\"feb-18\":{\"funnel end\":1500,\"unsubscribe\":2000,\"headroom\":50,\"active user\":100,\"spam\":20}}],\"tableData\":{\"funnel end\":600,\"unsubscribe\":400,\"headroom\":1200,\"active user\":100,\"spam\":500}},\"campaignFunneldata\":[{\"rate\":15,\"leads\":20,\"source\":\"facebook\"},{\"rate\":12,\"leads\":50,\"source\":\"instagram\"},{\"rate\":3,\"leads\":250,\"source\":\"mail\"}],\"outcome\":[{\r\n" + 
					"\"Parameter\":\"Reveneue\",\"Actual\":85,\"planned\":92},\r\n" + 
					"{\"Parameter\":\"Conversation\",\"Actual\":82,\"planned\":95},\r\n" + 
					"{\"Parameter\":\"Free trial\",\"Actual\":75,\"planned\":80}]\r\n" + 
					",\"rawLeads\":100}],\"funnelList\":[\"Funnel1\",\"Funnel2\"]}\r\n" + 
					"");
//			out.println(dashboardjson);
		} catch (Exception ex) {
			out.print(ex.getMessage());
		}
	} 
		 else {
			out.print("Rrquested extension is not an ESP resource");
			String remoteuser = request.getRemoteUser();
			out.print("Logged In User Is (remoteuser): " + remoteuser);
		}

		
	}
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out=response.getWriter();
		 if(request.getRequestPathInfo().getExtension().equals("movedata")) {
			try {
				
				System.out.println("In move Data If");
				session = repo.login(new SimpleCredentials("admin", "admin".toCharArray()));

				StringBuilder builder = new StringBuilder();
				BufferedReader bufferedReaderCampaign = request.getReader();
				String line;
				while ((line = bufferedReaderCampaign.readLine()) != null) {
				builder.append(line + "\n");
				}
				JSONObject jsondata = new JSONObject(builder.toString());
				JSONArray arrJson = jsondata.getJSONArray("FunnelList");
				String[] farr = new String[arrJson.length()];
				for(int i = 0; i < arrJson.length(); i++) {
				farr[i] = arrJson.getString(i);
				String remoteuser=jsondata.getString("username").replace("@", "_");
				String funnelname=jsondata.getString("FunnelName");
				String jj = GetGADataForMonitoringLeads.moveuidata(remoteuser,farr[i], funnelname,response);
				out.println(jj);

				}

//					String jj12 = GetGADataForMonitoringLeads.findGAdatapersubscriber("viki_gmail.com", "Explore", "today302019", "1628");
//					out.println("jj== ========= " + jj12);

				}

				catch (Exception e) {

				System.out.println("Exception ex : "+e.getMessage());
				}
				}
		 
		 if(request.getRequestPathInfo().getExtension().equals("dashboardexcelread")) {
				try {
					
					StringBuilder builder = new StringBuilder();
					BufferedReader bufferedReaderCampaign = request.getReader();
					String line;
					while ((line = bufferedReaderCampaign.readLine()) != null) {
					builder.append(line + "\n");
					}
					String base64excel=builder.toString();
					out.print(ReadExcel_OutCome.readexcel(base64excel));
					String urlcall="http://"+request.getScheme()+":8087/ParseEmailId/ReadExcelServ";
				//	new CallPostService().ReadExcel(urlcall, base64excel);
					}catch (Exception e) {
				}
					// TODO: handle exception
				
	}
		
	}
}
