/**
 *
 *  Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistribution of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *    2. Redistribution in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of Sun Microsystems, Inc. or the names of contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  This software is provided "AS IS," without a warranty of any kind. ALL
 *  EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 *  ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 *  OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 *  AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 *  AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 *  DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 *  REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 *  INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 *  OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 *  EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 *  You acknowledge that this software is not designed or intended for use in
 *  the design, construction, operation or maintenance of any nuclear facility.
 */
package com.sun.xacml.support.finder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicySet;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.combine.CombiningAlgFactory;
import com.sun.xacml.combine.PolicyCombinerElement;
import com.sun.xacml.combine.PolicyCombiningAlgorithm;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;
import com.sun.xacml.xacmlv3.Policy;

/**
 * This is a simple implementation of <code>PolicyFinderModule</code> that
 * supports retrieval based on context, and is designed for use with a run-time
 * configuration. Its constructor accepts a <code>List</code> of
 * <code>String</code>s that represent URLs or files, and these are resolved to
 * policies when the module is initialized. Beyond this, there is no modifying
 * or re-loading the policies represented by this class. This class will
 * optionally wrap multiple applicable policies into a dynamic PolicySet.
 * <p>
 * Note that this class is designed to complement
 * <code>StaticRefPolicyFinderModule</code>. It would be easy to support both
 * kinds of policy retrieval in a single class, but the functionality is instead
 * split between two classes. The reason is that when you define a configuration
 * for your PDP, it's easier to specify the two sets of policies by using two
 * different finder modules. Typically, there aren't many policies that exist in
 * both sets, so loading the sets separately isn't a problem. If this is a
 * concern to you, simply create your own class and merge the two existing
 * classes.
 * <p>
 * This module is provided as an example, but is still fully functional, and
 * should be useful for many simple applications. This is provided in the
 * <code>support</code> package rather than the core codebase because it
 * implements non-standard behavior.
 * 
 * @since 2.0
 * @author Seth Proctor
 */
public class StaticPolicyFinderModule extends PolicyFinderModule {

	// the list of policy URLs passed to the constructor
	private List policyList;

	// the map of policies
	private PolicyCollection policies;

	// the optional schema file
	private File schemaFile = null;

	// the policy identifier for any policy sets we dynamically create
	private static final String POLICY_ID = "urn:com:sun:xacml:support:finder:dynamic-policy-set";
	private static URI policyId = null;

	// the LOGGER we'll use for all messages
	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticPolicyFinderModule.class.getName());

	static {
		try {
			policyId = new URI(POLICY_ID);
		} catch (Exception e) {
			// this can't actually happen, but just in case...
			
				LOGGER.error("couldn't assign default policy id", e);
		}
	};

	/**
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to
	 * the given collection of policies and returns an error when more than one
	 * policy matches a given context. Any policy that cannot be loaded will be
	 * noted in the log, but will not cause an error. The schema file used to
	 * validate policies is defined by the property
	 * <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the retrieved
	 * property is null, then no schema validation will occur.
	 * 
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent
	 *            URLs or files pointing to XACML policies
	 */
	public StaticPolicyFinderModule(List policyList) {
		this.policyList = policyList;
		this.policies = new PolicyCollection();

		String schemaName = System
				.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);
		if (schemaName != null)
			schemaFile = new File(schemaName);
	}

	/**
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to
	 * the given collection of policies and returns an error when more than one
	 * policy matches a given context. Any policy that cannot be loaded will be
	 * noted in the log, but will not cause an error.
	 * 
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent
	 *            URLs or files pointing to XACML policies
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if
	 *            schema validation is not desired
	 */
	public StaticPolicyFinderModule(List policyList, String schemaFile) {
		this.policyList = policyList;
		this.policies = new PolicyCollection();

		if (schemaFile != null)
			this.schemaFile = new File(schemaFile);
	}

	/**
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to
	 * the given collection of policies. The given combining algorithm is used
	 * to create new PolicySets when more than one policy applies. Any policy
	 * that cannot be loaded will be noted in the log, but will not cause an
	 * error. The schema file used to validate policies is defined by the
	 * property <code>PolicyReader.POLICY_SCHEMA_PROPERTY</code>. If the
	 * retrieved property is null, then no schema validation will occur.
	 * 
	 * @param combiningAlg
	 *            the algorithm to use in a new PolicySet when more than one
	 *            policy applies
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent
	 *            URLs or files pointing to XACML policies
	 * 
	 * @throws URISyntaxException
	 *             if the combining algorithm is not a well-formed URI
	 * @throws UnknownIdentifierException
	 *             if the combining algorithm identifier isn't known
	 */
	public StaticPolicyFinderModule(String combiningAlg, List policyList)
			throws URISyntaxException, UnknownIdentifierException {
		PolicyCombiningAlgorithm alg = (PolicyCombiningAlgorithm) (CombiningAlgFactory
				.getInstance().createAlgorithm(new URI(combiningAlg)));

		this.policyList = policyList;
		this.policies = new PolicyCollection(alg, policyId);

		String schemaName = System
				.getProperty(PolicyReader.POLICY_SCHEMA_PROPERTY);
		if (schemaName != null)
			schemaFile = new File(schemaName);
	}

	/**
	 * Creates a <code>StaticPolicyFinderModule</code> that provides access to
	 * the given collection of policies. The given combining algorithm is used
	 * to create new PolicySets when more than one policy applies. Any policy
	 * that cannot be loaded will be noted in the log, but will not cause an
	 * error.
	 * 
	 * @param combiningAlg
	 *            the algorithm to use in a new PolicySet when more than one
	 *            policy applies
	 * @param policyList
	 *            a <code>List</code> of <code>String</code>s that represent
	 *            URLs or files pointing to XACML policies
	 * @param schemaFile
	 *            the schema file to validate policies against, or null if
	 *            schema validation is not desired
	 * 
	 * @throws URISyntaxException
	 *             if the combining algorithm is not a well-formed URI
	 * @throws UnknownIdentifierException
	 *             if the combining algorithm identifier isn't known
	 */
	public StaticPolicyFinderModule(String combiningAlg, List policyList,
			String schemaFile) throws URISyntaxException,
			UnknownIdentifierException {
		PolicyCombiningAlgorithm alg = (PolicyCombiningAlgorithm) (CombiningAlgFactory
				.getInstance().createAlgorithm(new URI(combiningAlg)));

		this.policyList = policyList;
		this.policies = new PolicyCollection(alg, policyId);

		if (schemaFile != null)
			this.schemaFile = new File(schemaFile);
	}

	/**
	 * Always returns <code>true</code> since this module does support finding
	 * policies based on context.
	 * 
	 * @return true
	 */
	public boolean isRequestSupported() {
		return true;
	}

	/**
	 * Initialize this module. Typically this is called by
	 * <code>PolicyFinder</code> when a PDP is created. This method is where the
	 * policies are actually loaded.
	 * 
	 * @param finder
	 *            the <code>PolicyFinder</code> using this module
	 */
	public void init(PolicyFinder finder) {
		// now that we have the PolicyFinder, we can load the policies
		PolicyReader reader = new PolicyReader(finder, LOGGER, schemaFile);

		Iterator it = policyList.iterator();
		while (it.hasNext()) {
			String str = (String) (it.next());
			Policy policy = null;

			try {
				try {
					// first try to load it as a URL
					URL url = new URL(str);
					policy = (Policy) reader.readPolicy(url);
				} catch (MalformedURLException murle) {
					// assume that this is a filename, and try again
					policy = (Policy) reader.readPolicy(new File(str));
				}

				// we loaded the policy, so try putting it in the collection
				if (!policies.addPolicy((Policy) policy))
						LOGGER.warn("tried to load the same policy multiple times: {}", str);
			} catch (ParsingException pe) {
					LOGGER.warn("Error reading policy: {}", str,
							pe);
			}
		}
	}

	/**
	 * TODO: Handle policySet
	 * 
	 * Finds a policy based on a request's context. If more than one policy
	 * matches, then this either returns an error or a new policy wrapping the
	 * multiple policies (depending on which constructor was used to construct
	 * this instance).
	 * 
	 * @param context
	 *            the representation of the request data
	 * 
	 * @return the result of trying to find an applicable policy
	 */
	// public PolicyFinderResult findPolicy(EvaluationCtx context) {
	// try {
	// Policy policy = (Policy)policies.getPolicy(context);
	//
	// if (policy == null) {
	// return new PolicyFinderResult();
	// } else {
	// return new PolicyFinderResult(policy);
	// }
	// } catch (TopLevelPolicyException tlpe) {
	// return new PolicyFinderResult(tlpe.getStatus());
	// }
	// }
	public PolicyFinderResult findPolicy(EvaluationCtx context) {
		try {
			Object myPolicies = this.policies.getPolicy(context);
			if (myPolicies == null) {
				myPolicies = this.policies.getPolicySet(context);
			}
			if (myPolicies instanceof PolicySet) {
				PolicySet policySet = (PolicySet) myPolicies;
				// Retrieving combining algorithm
				PolicyCombiningAlgorithm myCombiningAlg = (PolicyCombiningAlgorithm) policySet
						.getCombiningAlg();
				PolicyCollection myPolcollection = new PolicyCollection(
						myCombiningAlg, URI.create(policySet.getPolicySetId()));
				for (Object elt : policySet
						.getPolicySetsAndPoliciesAndPolicySetIdReferences()) {
					if (elt instanceof PolicyCombinerElement) {
						myPolcollection
								.addPolicy((Policy) ((PolicyCombinerElement) elt)
										.getElement());
					}
				}
				Object policy = myPolcollection.getPolicy(context);
				// The finder found more than one applicable policy so it build
				// a new PolicySet
				if (policy instanceof PolicySet) {
					return new PolicyFinderResult((PolicySet) policy,
							myCombiningAlg);
				}
				// The finder found only one applicable policy
				else if (policy instanceof Policy) {
					return new PolicyFinderResult((Policy) policy);
				}
			} else if (myPolicies instanceof Policy) {
				Policy policies = (Policy) myPolicies;
				return new PolicyFinderResult((Policy) policies);
			}
			// None of the policies/policySets matched
			return new PolicyFinderResult();
		} catch (TopLevelPolicyException tlpe) {
			return new PolicyFinderResult(tlpe.getStatus());
		}
	}

}
