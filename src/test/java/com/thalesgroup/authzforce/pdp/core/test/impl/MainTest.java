package com.thalesgroup.authzforce.pdp.core.test.impl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Romain Ferrari
 * 
 * class to use for the testSuite
 		ConformanceV3.class,
		BasicV3.class,
		BasicFunctionV3.class,
		BasicMultipleRequestV3.class
 */
@RunWith(Suite.class)
@SuiteClasses(value={
		ConformanceV3.class,
		BasicV3.class,
		BasicFunctionV3.class
})
public class MainTest {
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MainTest.class);
	
	@BeforeClass 
    public static void setUpClass() {      
        LOGGER.info("Beginning Tests");

    }

    @AfterClass public static void tearDownClass() { 
    	LOGGER.info("Finishing Tests");
    }
}
