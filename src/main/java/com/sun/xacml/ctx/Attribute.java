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
package com.sun.xacml.ctx;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.Marshaller;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xacml.BindingUtility;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.PolicyMetaData;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.DateTimeAttribute;
import com.sun.xacml.attr.xacmlv3.AttributeValue;
import com.thalesgroup.authzforce.xacml.schema.XACMLAttributeId;
import com.thalesgroup.authzforce.xacml.schema.XACMLVersion;

/**
 * Represents the AttributeType XML type found in the context schema.
 * 
 * @since 1.0
 * @author Seth Proctor
 */
public class Attribute extends oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute implements Serializable
{

	/**
	 * Generated Serial ID
	 */
	private static final long serialVersionUID = -2976070663545305371L;

	// required meta-data attributes
	// private URI id;
	private URI type;
	private URI category;

	// optional meta-data attributes
	// private String issuer = null;
	private DateTimeAttribute issueInstant = null;

	// the single value associated with this attribute
	// private List<AttributeValue> attributeValues;

	/**
	 * whether to include this attribute in the result. This is useful to correlate requests with
	 * their responses in case of multiple requests. optional one defined only in XACML3
	 */
	// private boolean includeInResult;

	/*
	 * FIXME: This is never used 
	 */
	private int xacmlVersion;

	/**
	 * Logger used for all classes
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Attribute.class);

	/**
	 * Creates a new <code>Attribute</code> of the type specified in the given
	 * <code>AttributeValue</code>.for XACML 3 with one <code>AttributeValue</code>
	 * 
	 * @param id
	 *            the id of the attribute
	 * @param issuer
	 *            the attribute's issuer or null if there is none
	 * @param issueInstant
	 *            the moment when the attribute was issued, or null if it's unspecified
	 * @param value
	 *            the actual value associated with the attribute meta-data
	 * @param includeInResult
	 *            whether to include this attribute in the result.
	 * @param version
	 *            XACML version
	 */
	public Attribute(URI id, String issuer, DateTimeAttribute issueInstant, AttributeValueType value, boolean includeInResult, int version)
	{
		this(id, URI.create(value.getDataType()), issuer, issueInstant, Arrays.asList(value), includeInResult, version);
	}

	/**
	 * Creates a new <code>Attribute</code> of the type specified in the given
	 * <code>AttributeValue</code>.for XACML 3 with one <code>AttributeValue</code>
	 * 
	 * @param id
	 *            the id of the attribute
	 * @param issuer
	 *            the attribute's issuer or null if there is none
	 * @param category
	 *            the attribute's category (XACML 3.0)
	 * @param issueInstant
	 *            the moment when the attribute was issued, or null if it's unspecified
	 * @param value
	 *            the actual value associated with the attribute meta-data
	 * @param includeInResult
	 *            whether to include this attribute in the result.
	 * @param version
	 *            XACML version
	 */
	public Attribute(URI id, String issuer, URI category, DateTimeAttribute issueInstant, AttributeValue value, boolean includeInResult,
			int version)
	{
		this(id, URI.create(value.getDataType()), category, issuer, issueInstant, Arrays.asList(value), includeInResult, version);
	}

	/**
	 * Creates a new <code>Attribute</code> for XACML 2 and XACML 1.X with one
	 * <code>AttributeValue</code>
	 * 
	 * @param id
	 *            the id of the attribute
	 * @param issuer
	 *            the attribute's issuer or null if there is none
	 * @param issueInstant
	 *            the moment when the attribute was issued, or null if it's unspecified
	 * @param value
	 *            actual <code>List</code> of <code>AttributeValue</code> associated with
	 * @param version
	 *            XACML version
	 */
	public Attribute(URI id, String issuer, DateTimeAttribute issueInstant, AttributeValueType value, int version)
	{

		this(id, URI.create(value.getDataType()), issuer, issueInstant, Arrays.asList(value), false, version);
	}

	/**
	 * Creates a new <code>Attribute</code>
	 * 
	 * @param id
	 *            the id of the attribute
	 * @param type
	 *            the type of the attribute
	 * @param issuer
	 *            the attribute's issuer or null if there is none
	 * @param issueInstant
	 *            the moment when the attribute was issued, or null if it's unspecified
	 * @param attributeValues
	 *            actual <code>List</code> of <code>AttributeValue</code> associated with
	 * @param includeInResult
	 *            whether to include this attribute in the result.
	 * @param xacmlVersion
	 *            xacml version
	 */
	public Attribute(URI id, URI type, String issuer, DateTimeAttribute issueInstant, List<AttributeValueType> attributeValues,
			boolean includeInResult, int xacmlVersion)
	{
		this.attributeId = id.toASCIIString();
		this.attributeValues = attributeValues;
		this.type = type;
		this.issuer = issuer;
		this.issueInstant = issueInstant;
		this.includeInResult = includeInResult;
		this.xacmlVersion = xacmlVersion;
		this.category = null;
	}

	/**
	 * Creates a new <code>Attribute</code>
	 * 
	 * @param id
	 *            the id of the attribute
	 * @param type
	 *            the type of the attribute
	 * @param category 
	 * @param issuer
	 *            the attribute's issuer or null if there is none
	 * @param issueInstant
	 *            the moment when the attribute was issued, or null if it's unspecified
	 * @param attributeValues
	 *            actual <code>List</code> of <code>AttributeValue</code> associated with
	 * @param includeInResult
	 *            whether to include this attribute in the result.
	 * @param xacmlVersion
	 *            xacml version
	 */
	public Attribute(URI id, URI type, URI category, String issuer, DateTimeAttribute issueInstant, List<AttributeValue> attributeValues,
			boolean includeInResult, int xacmlVersion)
	{
		this.attributeId = id.toASCIIString();
		this.type = type;
		this.category = category;
		this.issuer = issuer;
		this.issueInstant = issueInstant;
		this.attributeValues = new ArrayList<AttributeValueType>(attributeValues);
		this.includeInResult = includeInResult;
		this.xacmlVersion = xacmlVersion;
	}

	/**
	 * Creates an instance of an <code>Attribute</code> based on the root DOM node of the XML data.
	 * 
	 * @param root
	 *            the DOM root of the AttributeType XML type
	 * @param version
	 *            XACML version
	 * 
	 * @return the attribute
	 * 
	 *         throws ParsingException if the data is invalid
	 * @throws ParsingException
	 */
	public static Attribute getInstance(Node root, int version) throws ParsingException
	{
		URI id = null;
		URI type = null;
		URI category = null;
		String issuer = null;
		DateTimeAttribute issueInstant = null;
		List<AttributeValue> values = new ArrayList<>();
		boolean includeInResult = false;

		AttributeFactory attrFactory = AttributeFactory.getInstance();

		// First check that we're really parsing an Attribute
		if (!root.getNodeName().equals("Attribute"))
		{
			throw new ParsingException("Attribute object cannot be created " + "with root node of type: " + root.getNodeName());
		}

		NamedNodeMap attrs = root.getAttributes();

		try
		{
			id = new URI(attrs.getNamedItem("AttributeId").getNodeValue());
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing required attribute " + "AttributeId in AttributeType", e);
		}

		/**
		 * XACML 2.0 AttributeType has a DataType attribute. For XACML v3.0, no such attribute,
		 * DataType defined in each child AttributeValue.
		 */
		try
		{
			type = new URI(attrs.getNamedItem("DataType").getNodeValue());
		} catch (Exception e)
		{
			throw new ParsingException("Error parsing required attribute " + "DataType in AttributeType", e);
		}

		if (version == PolicyMetaData.XACML_VERSION_3_0)
		{
			try
			{
				String includeInResultString = attrs.getNamedItem("IncludeInResult").getNodeValue();
				if ("true".equals(includeInResultString))
				{
					includeInResult = true;
				}
			} catch (Exception e)
			{
				throw new ParsingException("Error parsing required attribute " + "IncludeInResult in AttributeType", e);
			}
		}

		try
		{
			Node issuerNode = attrs.getNamedItem("Issuer");
			if (issuerNode != null)
				issuer = issuerNode.getNodeValue();
			if (!(version == PolicyMetaData.XACML_VERSION_3_0))
			{
				Node instantNode = attrs.getNamedItem("IssueInstant");
				if (instantNode != null)
				{
					issueInstant = DateTimeAttribute.getInstance(instantNode.getNodeValue());
				}
			}
		} catch (Exception e)
		{
			// shouldn't happen, but just in case...
			throw new ParsingException("Error parsing optional AttributeType" + " attribute", e);
		}

		// now we get the attribute value
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeName().equals("AttributeValue"))
			{
				if (version == PolicyMetaData.XACML_VERSION_3_0)
				{
					NamedNodeMap dataTypeAttribute = node.getAttributes();
					try
					{
						type = new URI(dataTypeAttribute.getNamedItem("DataType").getNodeValue());
						category = new URI(dataTypeAttribute.getNamedItem("Category").getNodeValue());
					} catch (Exception e)
					{
						throw new ParsingException("Error parsing required attribute " + "DataType in AttributeType", e);
					}
				}
				// now get the value
				try
				{
					values.add(attrFactory.createValue(node, type));
				} catch (UnknownIdentifierException uie)
				{
					throw new ParsingException("Unknown AttributeId", uie);
				}
			}
		}

		// make sure we got a value
		if (values.size() < 1)
		{
			throw new ParsingException("Attribute must contain a value");
		}

		return new Attribute(id, type, category, issuer, issueInstant, values, includeInResult, version);
	}

	/**
	 * Creates Attribute handler from Attribute element, as defined in OASIS XACML model
	 * 
	 * @param attrElt
	 * @param xacmlVersion
	 *            XACML version
	 * @return attribute handler
	 * @throws ParsingException if one of the attribute value cannot be parsed according to specified value datatype
	 * @throws UnknownIdentifierException if one of the attribute value datatype is unknown/not supported
	 */
	public static Attribute getInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.Attribute attrElt, int xacmlVersion) throws ParsingException, UnknownIdentifierException
	{
		final URI id = URI.create(attrElt.getAttributeId());
		/*
		 * Attribute element has no datatype attribute in XACML v3. Defined by each child
		 * AttributeValue. So type arg is null.
		 * Category is defined in parent Attributes element not at the Attribute level in XACML v3. So category arg is null.
		 */
		final AttributeFactory attrValueFactory = AttributeFactory.getInstance();
		final List<AttributeValue> values = new ArrayList<>();
		for(final AttributeValueType attrValElt: attrElt.getAttributeValues()) {
			final AttributeValue attrVal = attrValueFactory.createValue(attrValElt);
			values.add(attrVal);
		}
		
		return new Attribute(id, null, null, attrElt.getIssuer(), null, values, attrElt.isIncludeInResult(), xacmlVersion);
	}

	/**
	 * Returns whether attribute must be present in response or not
	 * 
	 * @return true/false
	 */
	@Override
	public boolean isIncludeInResult()
	{
		return includeInResult;
	}

	/**
	 * Returns the id of this attribute
	 * 
	 * @return the attribute id
	 */
	public URI getId()
	{
		if (attributeId != null)
		{
			return URI.create(attributeId);
		}
		return null;
	}

	/**
	 * Returns the data type of this attribute
	 * 
	 * @return the attribute's data type
	 */
	public URI getType()
	{
		return type;
	}

	/**
	 * Returns the issuer of this attribute, or null if no issuer was named
	 * 
	 * @return the issuer or null
	 */
	@Override
	public String getIssuer()
	{
		return issuer;
	}

	/**
	 * Returns the category of this attribute
	 * 
	 * @return the issuer or null
	 */
	public URI getCategory()
	{
		if (category != null)
		{
			return category;
		}
		return URI.create("");
	}

	/**
	 * Returns the moment at which the attribute was issued, or null if no issue time was provided
	 * 
	 * @return the time of issuance or null
	 */
	public DateTimeAttribute getIssueInstant()
	{
		return issueInstant;
	}

	/**
	 * The value of this attribute, or null if no value was included
	 * 
	 * @return the attribute's value or null
	 */
	public List<AttributeValueType> getValues()
	{
		return attributeValues;
	}

	/**
	 * The value of this attribute, or null if no value was included
	 * 
	 * @return the attribute's value or null
	 */
	public AttributeValueType getValue()
	{
		if (attributeValues != null && attributeValues.size() > 0)
		{
			return attributeValues.get(0);
		}

		return null;
	}

	/**
	 * Encodes this attribute into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with no indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 */
	public void encode(OutputStream output)
	{
		encode(output, new Indenter(0));
	}

	/**
	 * Encodes this attribute into its XML representation and writes this encoding to the given
	 * <code>OutputStream</code> with indentation.
	 * 
	 * @param output
	 *            a stream into which the XML-encoded data is written
	 * @param indenter
	 *            an object that creates indentation strings
	 */
	public void encode(OutputStream output, Indenter indenter)
	{
		PrintStream out = new PrintStream(output);
		try
		{
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, out);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Attribute", e);
		}
	}

	/**
	 * Simple encoding method that returns the text-encoded version of this attribute with no
	 * formatting.
	 * 
	 * @return the text-encoded XML
	 */
	public String encode()
	{
		StringWriter attribute = new StringWriter();
		String str = "";
		try
		{
			Marshaller u = BindingUtility.XACML3_0_JAXB_CONTEXT.createMarshaller();
			u.marshal(this, attribute);
		} catch (Exception e)
		{
			LOGGER.error("Error marshalling Attribute to String", e);
		}
		attribute.write(str);

		return str;
	}

}
