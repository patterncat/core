package com.thalesgroup.authzforce.pdp.core.test.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.support.finder.FilePolicyModule;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestConstants;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * This would test multiple decision profile that is introduced with XACML 3.0.
 */
public class BasicMultipleRequestV3 {

	/**
	 * directory name that states the test type
	 */
	private final static String ROOT_DIRECTORY = "basic";

	/**
	 * directory name that states XACML version
	 */
	private final static String VERSION_DIRECTORY = "3";

	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BasicMultipleRequestV3.class);

	/**
	 * The map of results
	 */
	private static Map<String, String> results = new TreeMap<String, String>();

	@BeforeClass
	public static void setUp() throws Exception {
		LOGGER.info("Launching multi requests tests");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		showResults();
	}

	@Test
	public void testBasicTest0001() throws Exception {

		String reqResNo;
		Set<String> policies = new HashSet<String>();
		policies.add("TestPolicy_0014.xml");
//		PDP pdp = getPDPNewInstance(policies);
		LOGGER.info("Basic Test 0014 is started");
		ResponseCtx response = null;
		Response expectedResponse = null;
		Request request = null;

		for (int i = 1; i < 3; i++) {
			if (i < 10) {
				reqResNo = "0" + i;
			} else {
				reqResNo = Integer.toString(i);
			}

			request = TestUtils.createRequest(ROOT_DIRECTORY,
					VERSION_DIRECTORY, "request_0014_" + reqResNo + ".xml");
			if (request != null) {
				LOGGER.debug("Request that is sent to the PDP :  "
						+ TestUtils.printRequest(request));
				response = getPDPNewInstance(policies).evaluate(request);
				if (response != null) {
					LOGGER.info("Response that is received from the PDP :  "
							+ response.getEncoded());
					expectedResponse = TestUtils.createResponse(ROOT_DIRECTORY,
							VERSION_DIRECTORY, "response_0014_" + reqResNo
									+ ".xml");
					LOGGER.info("Response expected:  "
							+ TestUtils.printResponse(expectedResponse));
					if (expectedResponse != null) {
						boolean assertion = TestUtils.match(response,
								expectedResponse);
						if (assertion) {
							LOGGER.debug("Assertion SUCCESS for: IIIA"
									+ "response_0014_" + reqResNo);
							results.put("response_0014_" + reqResNo, "SUCCESS");
						} else {
							LOGGER.error("Assertion FAILED for: TestPolicy_0014.xml and response_0014_"
									+ reqResNo);
							results.put("response_0014_" + reqResNo, "FAILED");
						}
						assertTrue(assertion);
					} else {
						assertTrue("Response read from file is Null", false);
					}
				} else {
					assertFalse("Response received PDP is Null", false);
				}
			} else {
				assertTrue("Request read from file is Null", false);
			}
			LOGGER.info("Basic Test 0014 is finished");
		}
	}

	/**
	 * Returns a new PDP instance with new XACML policies
	 * 
	 * @param policies
	 *            Set of XACML policy file names
	 * @return a PDP instance
	 */
	private static PDP getPDPNewInstance(Set<String> policies) {

		PolicyFinder finder = new PolicyFinder();
		List<String> policyLocations = new ArrayList<String>();

		for (String policy : policies) {
			try {
				String policyPath = (new File(".")).getCanonicalPath()
						+ File.separator + TestConstants.RESOURCE_PATH.value()
						+ File.separator + ROOT_DIRECTORY + File.separator
						+ VERSION_DIRECTORY + File.separator
						+ TestConstants.POLICY_DIRECTORY.value()
						+ File.separator + policy;
				policyLocations.add(policyPath);
			} catch (IOException e) {
				LOGGER.error("Error getting path to policy", e);
			}
		}

		FilePolicyModule testPolicyFinderModule = new FilePolicyModule(
				policyLocations);
		Set<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
		policyModules.add(testPolicyFinderModule);
		finder.setModules(policyModules);

		PDP authzforce = PDP.getInstance();
		PDPConfig pdpConfig = authzforce.getPDPConfig();
		pdpConfig = new PDPConfig(pdpConfig.getAttributeFinder(), finder,
				pdpConfig.getResourceFinder(), null);
		return new PDP(pdpConfig);

	}

	private static void showResults() throws Exception {
		for (String key : results.keySet()) {
			LOGGER.info(key + ":" + results.get(key));
		}
	}
}