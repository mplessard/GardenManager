package ca.uqam.latece.rest.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ca.uqam.latece.rest.database.Database;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGardenService {
	WebClient webClient;
	
	URL url1;
	URL url2;
	URL url3;
	URL url4;
	URL url5;
	
	WebRequest requestSettings1;
	WebRequest requestSettings2;
	WebRequest requestSettings3;
	WebRequest requestSettings4;
	WebRequest requestSettings5;
	
	String appendUrl = "http://localhost:8081/GardenManager/api/v1/gardenservice/";
	
	ArrayList<NameValuePair> parameters;

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Database.getConnection();		
		Database.operationOnTable("DELETE FROM garden WHERE name='Test Garden'");
	}
	
	@Before
	public void setUp() throws Exception {
		webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		
		url1 = new URL(appendUrl + "gardens");
		url2 = new URL(appendUrl + "gardens?access_token=N0th1ngG00d");
		url3 = new URL(appendUrl + "gardens?access_token=Ceb4gTBhyr2e8mgILkOWjcWjIoRuub-khV4DAgEQpEfhvHWUtcwedb8c-Z8S7eTg");
		url4 = new URL(appendUrl + "gardens?access_token=sVmThUIdLblP1oVOvDB6eHpBIollsO3NNmuSq-dOrlCsYZWMmRVhI8i_aXTyzQIB");
		url5 = new URL(appendUrl + "gardens/1?access_token=sVmThUIdLblP1oVOvDB6eHpBIollsO3NNmuSq-dOrlCsYZWMmRVhI8i_aXTyzQIB");
		
		requestSettings1 = new WebRequest(url1, HttpMethod.POST);
		requestSettings2 = new WebRequest(url2, HttpMethod.POST);
		requestSettings3 = new WebRequest(url3, HttpMethod.POST);
		requestSettings4 = new WebRequest(url4, HttpMethod.POST);
		requestSettings5 = new WebRequest(url5, HttpMethod.DELETE);
		
		parameters = new ArrayList<NameValuePair>();
	}

	@After
	public void tearDown() throws Exception {
		webClient.close();
		webClient = null;
		url1 = null;
		url2 = null;
		url3 = null;
		url4 = null;
		url5 = null;
		requestSettings1 = null;
		requestSettings2 = null;
		requestSettings3 = null;
		requestSettings4 = null;
		requestSettings5 = null;
		parameters = null;
	}

	@Test
	public void post1GardenNoToken() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "TestGarden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings1.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings1);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(401, statusCode);
		assertEquals("Access denied: the access token is invalid.", currentPage.asText());
	}
	
	@Test
	public void post2GardenInvalidToken() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "TestGarden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings2.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings2);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(401, statusCode);
		assertEquals("Access denied: the access token is invalid.", currentPage.asText());
	}
	
	@Test
	public void post3GardenNotAnAdmin() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "TestGarden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings3.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings3);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(401, statusCode);
		assertEquals("Access denied: you are not authorized to add a garden.", currentPage.asText());
	}

	@Test
	public void post4Garden() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "Test Garden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		System.out.println(currentPage.asText());
		assertEquals(200, statusCode);
		assertEquals("The garden has been created.", currentPage.asText());
	}
	
	@Test
	public void post5GardenInvalidNameNoChar() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", ""));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The name is not valid.", currentPage.asText());
	}
	
	@Test
	public void post5GardenInvalidNameNull() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", null));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The name is not valid.", currentPage.asText());
	}
	
	@Test
	public void post5GardenInvalidNameSpecialChar() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "TheGarden1%"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The name is not valid.", currentPage.asText());
	}
	
	@Test
	public void post5GardenAlreadyExistingGarden() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "Test Garden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("A garden with this name already exist.", currentPage.asText());
	}
	
	@Test
	public void post6GardenInvalidCategory() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "Other Garden"));
		parameters.add(new NameValuePair("category", "0"));
		parameters.add(new NameValuePair("address", "123 Test Garden"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The category is not valid.", currentPage.asText());
	}
	
	@Test
	public void post7GardenInvalidAddress() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "Other Garden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "ABC 123"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The address is not valid.", currentPage.asText());
	}
	
	@Test
	public void post8GardenInvalidAddressSpecialChar() throws FailingHttpStatusCodeException, IOException{
		parameters.add(new NameValuePair("name", "Other Garden"));
		parameters.add(new NameValuePair("category", "1"));
		parameters.add(new NameValuePair("address", "123 Ave?"));
		
		requestSettings4.setRequestParameters(parameters);
		
		HtmlPage currentPage = webClient.getPage(requestSettings4);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The address is not valid.", currentPage.asText());
	}
	
	@Test
	public void testDelete1Garden() throws FailingHttpStatusCodeException, IOException{
		HtmlPage currentPage = webClient.getPage(requestSettings5);
		int statusCode = currentPage.getWebResponse().getStatusCode();
		
		assertEquals(200, statusCode);
		assertEquals("The garden has been deleted.", currentPage.asText());
	}
//	@Test
//	public void testDelete1GardenInvalidToken() throws FailingHttpStatusCodeException, IOException{
//		HtmlPage currentPage = webClient.getPage(requestSettings5);
//		int statusCode = currentPage.getWebResponse().getStatusCode();
//		
//		assertEquals(200, statusCode);
//		assertEquals("The garden has been deleted.", currentPage.asText());
//	}
}
