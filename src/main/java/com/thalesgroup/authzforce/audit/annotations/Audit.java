/**
 * Copyright (C) 2011-2014 Thales Services - All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.thalesgroup.authzforce.audit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author Romain Ferrari
 * 
 *         Theses annotations are used to generate audit log based on the aspect
 *         <code>com.thalesgroup.authzforce.audit.aspect.AuditAspect</code>. You
 *         can use theses annotations on combination algorithm for: 
 *         - PolicySet (TODO: Not Implemented)
 *         - Policy
 *         - Rules
 *         on: 
 *         - Attribute retrieving status(TODO: Not Implemented)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

	Type type();

	/**
	 * 
	 * @author Romain Ferrari
	 * 
	 *         POLICYSET is to be used on a evaluate method for a policyset
	 *         POLICY is to be used on a evaluate method for a policy 
	 *         RULE is to be used on a evaluate method for a rule 
	 *         ATTRIBUTE
	 * 
	 *         DISPLAY is a little different, is to be used on a method who is
	 *         the entry point of the PDP. I use it over the
	 *         </code>PDP.evaluate</code> method
	 */
	public static enum Type {
		POLICYSET, POLICY, RULE, ATTRIBUTE, DISPLAY;
	};
}