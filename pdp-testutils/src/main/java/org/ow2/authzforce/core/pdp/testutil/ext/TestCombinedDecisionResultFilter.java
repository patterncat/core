/**
 * Copyright 2012-2017 Thales Services SAS.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.core.pdp.testutil.ext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Result;

import org.ow2.authzforce.core.pdp.api.DecisionResultFilter;
import org.ow2.authzforce.core.pdp.api.ImmutablePepActions;
import org.ow2.authzforce.core.pdp.api.IndividualDecisionRequest;
import org.ow2.authzforce.core.pdp.api.PdpDecisionResult;
import org.ow2.authzforce.core.pdp.api.StatusHelper;

/**
 * XACML Result filter implementing the functionality 'urn:oasis:names:tc:xacml:3.0:profile:multiple:combined-decision' from <a
 * href="http://docs.oasis-open.org/xacml/3.0/multiple/v1.0/xacml-3.0-multiple-v1.0.html">XACML v3.0 Multiple Decision Profile Version 1.0</a>. Edited by Erik Rissanen. 18 May 2014. OASIS Committee
 * Specification 02.
 * <p>
 * Used here for testing Authzforce Result filter extension mechanism, i.e. plugging a custom decision Result filter into the PDP engine.
 * <p>
 * NB: the spec does not mention the inclusion of element {@code <PolicyIdentifierList>}. At least, it does not say to remove it, so in theory there is no reason why we should not include it. However,
 * in this test implementation, we don't include any {@code <PolicyIdentifierList>} in the final result, for the sake of simplicity. So BEWARE!
 *
 */
public class TestCombinedDecisionResultFilter implements DecisionResultFilter
{
	public static final String ID = "urn:ow2:authzforce:feature:pdp:result-filter:multiple:test-combined-decision";

	private static final List<Result> INDETERMINATE_RESULT_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(StatusHelper.STATUS_PROCESSING_ERROR,
			Optional.empty()), null, null, null, null));

	// private static final List<Result> INDETERMINATE_RESULT_SINGLETON_LIST_BECAUSE_NO_INDIVIDUAL = Collections.singletonList(new Result(DecisionType.INDETERMINATE, new StatusHelper(
	// StatusHelper.STATUS_PROCESSING_ERROR, "No <Result> to combine!"), null, null, null, null));

	private static final List<Result> PERMIT_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.PERMIT, StatusHelper.OK, null, null, null, null));
	private static final List<Result> DENY_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.DENY, StatusHelper.OK, null, null, null, null));
	private static final List<Result> NOT_APPLICABLE_SINGLETON_LIST = Collections.singletonList(new Result(DecisionType.NOT_APPLICABLE, StatusHelper.OK, null, null, null, null));

	private static final class ResultCollector implements FilteringResultCollector
	{

		private DecisionType combinedDecision = DecisionType.INDETERMINATE;

		@Override
		public List<Result> addResult(final IndividualDecisionRequest request, final PdpDecisionResult result)
		{
			if (result.getDecision() == DecisionType.INDETERMINATE)
			{
				// either all result must be indeterminate or we return Indeterminate as final result anyway
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			final ImmutablePepActions pepActions = result.getPepActions();
			if (!pepActions.getObligatory().isEmpty() || !pepActions.getAdvisory().isEmpty())
			{
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			final DecisionType individualDecision = result.getDecision();
			// if combinedDecision not initialized yet (indeterminate), set it to the result's decision
			if (combinedDecision == DecisionType.INDETERMINATE)
			{
				combinedDecision = individualDecision;
				return null;
			}

			// combinedDecision != Indeterminate
			if (individualDecision != combinedDecision)
			{
				return INDETERMINATE_RESULT_SINGLETON_LIST;
			}

			// individualDecision == combinedDecision
			return null;
		}

		@Override
		public List<Result> getFilteredResults()
		{
			switch (combinedDecision)
			{
				case PERMIT:
					return PERMIT_SINGLETON_LIST;
				case DENY:
					return DENY_SINGLETON_LIST;
				case NOT_APPLICABLE:
					return NOT_APPLICABLE_SINGLETON_LIST;
				default:
					return INDETERMINATE_RESULT_SINGLETON_LIST;
			}
		}

	}

	@Override
	public String getId()
	{
		return ID;
	}

	@Override
	public boolean supportsMultipleDecisionCombining()
	{
		return true;
	}

	@Override
	public FilteringResultCollector newResultCollector(final int numberOfInputResults)
	{
		return new ResultCollector();
	}

}
