/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.MaxConsecutiveSpaceDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.bld.commons.utils.json.annotations.CleanExcessSpaces;
import com.bld.commons.utils.json.annotations.deserialize.data.CleanExcessSpacesProps;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

// TODO: Auto-generated Javadoc
/**
 * The Class MaxConsecutiveSpaceDeserializer.
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class CleanExcessSpacesDeserialize extends StdScalarDeserializer<String> implements ContextualDeserializer{

	/** The max consecutive space props. */
	private CleanExcessSpacesProps cleanExcessSpacesProps;
	
	/**
	 * Instantiates a new max consecutive space deserializer.
	 */
	public CleanExcessSpacesDeserialize() {
		super(String.class);
	}


	/**
	 * Instantiates a new max consecutive space deserializer.
	 *
	 * @param src the src
	 * @param maxConsecutiveSpaceProps the max consecutive space props
	 */
	protected CleanExcessSpacesDeserialize(Class<String> src,CleanExcessSpacesProps maxConsecutiveSpaceProps) {
		super(src);
		this.cleanExcessSpacesProps=maxConsecutiveSpaceProps;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param ctxt the ctxt
	 * @param property the property
	 * @return the json deserializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		CleanExcessSpaces cleanExcessSpaces = property.getAnnotation(CleanExcessSpaces.class);
		CleanExcessSpacesProps cleanExcessSpacesProps = new CleanExcessSpacesProps(cleanExcessSpaces.consecutive(), cleanExcessSpaces.trim(),cleanExcessSpaces.removeEndline(),cleanExcessSpaces.removeAllSpaceType(),cleanExcessSpaces.upperLowerType(),cleanExcessSpaces.removeTab());
		return new CleanExcessSpacesDeserialize(String.class, cleanExcessSpacesProps);
	}

	/**
	 * Deserialize.
	 *
	 * @param p the p
	 * @param ctxt the ctxt
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JsonProcessingException the json processing exception
	 */
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String text=p.getText();
		if(StringUtils.isNotEmpty(text)) {
			if(cleanExcessSpacesProps.isRemoveAllSpaceType()) 
				text=text.replaceAll("\\s+", "");
			else {
				if(cleanExcessSpacesProps.isRemoveEndline())
					text=text.replace("\n", "");
				if(cleanExcessSpacesProps.isRemoveTab())
					text=text.replace("\t", "");
				if(cleanExcessSpacesProps.isTrim())
					text=text.trim();
				String space="";
				for(int i=0;i<cleanExcessSpacesProps.getConsecutive();i++)
					space+=" ";
				text=removeSpace(space+" ", space, text);
			}
			
			switch(this.cleanExcessSpacesProps.getUpperLowerType()) {
			case LOWER:
				text=text.toLowerCase();
				break;
			case UPPER:
				text=text.toUpperCase();
				break;
			case NONE:
			default:
				break;
			
			}
		}
		return text;
	}

	
	/**
	 * Removes the space.
	 *
	 * @param remveText the remve text
	 * @param replaceText the replace text
	 * @param text the text
	 * @return the string
	 */
	private String removeSpace(String remveText,String replaceText,String text) {
		if(text.contains(remveText))
			text=removeSpace(remveText, replaceText, text.replace(remveText, replaceText));
		return text;
	}
	
	
}
