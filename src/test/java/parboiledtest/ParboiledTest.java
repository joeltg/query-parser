package parboiledtest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ParboiledTest {

	public static Map<String, String> TEST_CASES;
	
	public static Map<String, String> INTERMEDIATE_FORM_TEST_CASES;
	
	static {
		INTERMEDIATE_FORM_TEST_CASES = new LinkedHashMap<String,String>();
		INTERMEDIATE_FORM_TEST_CASES.put("\"cisco systems\"", "SPAN_NEAR(cisco, systems, slop=0, in_order=1)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco$", "SPAN_MULTI(cisco*,wildcard)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco?", "SPAN_MULTI(cisco?,wildcard)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco$5", "SPAN_MULTI(cisco[a-zA-Z0-9]{0,5},regex)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco NEAR router","SPAN_NEAR(cisco, router, slop=0, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco ADJ router","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco SAME router","SPAN_NEAR(cisco, router, slop=200, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco OR router","SPAN_OR(cisco, router, slop=0, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco ONEAR router","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco~5","SPAN_MULTI(cisco, FUZZY=5)");
		INTERMEDIATE_FORM_TEST_CASES.put("cisco AND router NEAR vpn","SPAN_NEAR(cisco, SPAN_NEAR(router, vpn, slop=0, in_order=0), slop=2147483647, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("VPN or virtual adj private adj network or tunnel4","SPAN_OR(VPN, SPAN_NEAR(SPAN_NEAR(virtual, private, slop=0, in_order=1), network, slop=0, in_order=1), tunnel4, slop=0, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("VPN and virtual adj private adj network and tunnel4","SPAN_NEAR(VPN, SPAN_NEAR(SPAN_NEAR(virtual, private, slop=0, in_order=1), network, slop=0, in_order=1), tunnel4, slop=2147483647, in_order=0)");
		//INTERMEDIATE_FORM_TEST_CASES.put("split$4 with (DNS or domain adj name adj serv$3) with (VPN or virtual adj private adj network or tunnel$4)","SPAN_NEAR(VPN, SPAN_NEAR(SPAN_NEAR(virtual, private, slop=0, in_order=1), network, slop=0, in_order=1), tunnel4, slop=2147483647, in_order=0)");
		INTERMEDIATE_FORM_TEST_CASES.put("((connection or session or socket) near2 (error or failure or (time$4 near3 out) or fail$4)) and ((connection or session or socket) near2 (reclaim$5 or (re adj claim$5) or recover$4 or recycl$5))","SPAN_NEAR(VPN, SPAN_NEAR(SPAN_NEAR(virtual, private, slop=0, in_order=1), network, slop=0, in_order=1), tunnel4, slop=2147483647, in_order=0)");
		
	
	}
	
	/*static {
		TEST_CASES = new LinkedHashMap<String, String>();

		
		  // TEST_CASES.put("cisco NEAR router","SPAN_NEAR(cisco, router, slop=0, in_order=0)");
		 // TEST_CASES.put("cisco AND router NEAR vpn","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		 TEST_CASES.put("VPN~5 or virtual adj private adj network or tunnel4","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		  //TEST_CASES.put("split4 with (DNS or domain adj name adj serv3) with (VPN or virtual adj private adj network or tunnel4)","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		 //TEST_CASES.put("cisco AND router","SPAN_NEAR(cisco, router, slop=2147483647, in_sorder=0)");
		 //TEST_CASES.put("cisco SAME router","SPAN_NEAR(cisco, router, slop=200, in_order=0)");
		// TEST_CASES.put("cisco OR router","SPAN_OR(cisco, router, slop=0, in_order=0)");
		//TEST_CASES.put("cisco ONEAR router","SPAN_NEAR(cisco, router, slop=0, in_order=1)");
		//TEST_CASES.put("cisco~5","SPAN_MULTI(cisco, FUZZY=5)");

	}
	
	@Test

	public void test(){
		ParseMain parseMain = new ParseMain();
		for (Map.Entry<String, String> testCase : TEST_CASES.entrySet()) {
			//System.out.println(testCase.getValue());
			//System.out.println("==>");
			System.out.println(parseMain.formatSearchText(testCase.getKey(), "AND"));
			Assert.assertEquals(testCase.getValue(), parseMain.formatSearchText(testCase.getKey(), "AND"));
		}
		Assert.assertTrue(true);
	}



*/

	@Test
	
public void testIntermediate(){
	ParseMain parseMain = new ParseMain();
	//System.out.println("hi");
	for (Map.Entry<String, String> testCase : INTERMEDIATE_FORM_TEST_CASES.entrySet()) {
		System.out.println(testCase.getKey());
		System.out.println(parseMain.formatSearchText(testCase.getKey(), "AND"));
		System.out.println();
		
		Assert.assertEquals(testCase.getValue(), parseMain.formatSearchText(testCase.getKey(), "AND"));
	}
	Assert.assertTrue(true);
}

}

