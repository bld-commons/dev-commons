/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.utils.RestConnectionMapper.java
 */
package com.bld.commons.connection.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class RestConnectionMapper.
 */
@SuppressWarnings({"unchecked"})
public class RestConnectionMapper {

 
	/**
	 * From object to json.
	 *
	 * @param obj the obj
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public static String fromObjectToJson(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonObj = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		return jsonObj;
	}

	/**
	 * From json to entity.
	 *
	 * @param <T> the generic type
	 * @param json the json
	 * @param classObj the class obj
	 * @return the t
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static <T> T fromJsonToEntity(String json, Class<T> classObj)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		T obj = mapper.readValue(json, classObj);
		return obj;
	}

	/**
	 * From object to map.
	 *
	 * @param obj the obj
	 * @return the map
	 * @throws JsonProcessingException the json processing exception
	 */
	public static Map<String, Object> fromObjectToMap(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map=new HashMap<>();
		if(obj!=null) {
			if(obj instanceof Map)
				map=(Map<String,Object>)obj;
			else 
				map=(Map<String, Object>) mapper.readValue(fromObjectToJson(obj), Map.class);
		}
		return map;
	}
	
	/**
	 * Builder query.
	 *
	 * @param builder the builder
	 * @param params the params
	 */
	public static void builderQuery(UriComponentsBuilder builder,Map<String,Object>params) {
		builder.uriVariables(params);
		for(Entry<String,Object>entry:params.entrySet())
			builder.queryParam(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Map to multi value map.
	 *
	 * @param params the params
	 * @return the multi value map
	 */
	public static MultiValueMap<String, Object> mapToMultiValueMap(Map<String,Object>params){
		MultiValueMap<String, Object> multiValueMap=new LinkedMultiValueMap<>();
		for(Entry<String,Object>entry:params.entrySet())
			multiValueMap.add(entry.getKey(), entry.getValue());
		return multiValueMap;
	}
	
	/**
	 * Builder query.
	 *
	 * @param builder the builder
	 * @param params the params
	 * @param nameObj the name obj
	 */
	public static void builderQuery(UriComponentsBuilder builder,Map<String,Object>params,String nameObj) {
		builder.uriVariables(params);
		for (Entry<String, Object> entry : params.entrySet()) {
			if(entry.getValue() instanceof Map)
				builderQuery(builder, (Map<String,Object>)entry.getValue(), entry.getKey()+".");
			else if(entry.getValue() instanceof Collection) {
				List<Object> list=new ArrayList<>((Collection<Object>)entry.getValue());
				for(int i=0;i<list.size();i++) {
					Object item=list.get(i);
					if(item instanceof Map) 
						builderQuery(builder, (Map<String,Object>)item, entry.getKey()+"["+i+"].");
					else
						builder.queryParam(nameObj+entry.getKey()+"["+i+"]", ((List<Object>)entry.getValue()).get(i));
						
				}
			} else
		    builder.queryParam(nameObj+entry.getKey(), entry.getValue());
		}
	}
	
	
	/**
	 * Map to multi value map.
	 *
	 * @param multiValueMap the multi value map
	 * @param params the params
	 * @param nameObj the name obj
	 * @return the multi value map
	 */
	public static MultiValueMap<String, Object> mapToMultiValueMap(MultiValueMap<String, Object> multiValueMap,Map<String,Object>params,String nameObj){
		
		for (Entry<String, Object> entry : params.entrySet()) {
			if(entry.getValue() instanceof Map)
				mapToMultiValueMap(multiValueMap, (Map<String,Object>)entry.getValue(), entry.getKey()+".");
			else if(entry.getValue() instanceof Collection) {
				Collection<Object> collection=(Collection<Object>)entry.getValue();
				List<Object> list=new ArrayList<>(collection);
				for(int i=0;i<list.size();i++) {
					Object item=list.get(i);
					if(item instanceof Map)
						mapToMultiValueMap(multiValueMap, (Map<String,Object>)item, entry.getKey()+"["+i+"].");
					else
						multiValueMap.add(nameObj+entry.getKey()+"["+i+"]", ((List<Object>)entry.getValue()).get(i));
						
				}
			} else
				multiValueMap.add(nameObj+entry.getKey(), entry.getValue());
		}
		return multiValueMap;
	}
	
	
	
	/**
	 * Removes the empty value.
	 *
	 * @param map the map
	 */
	public static void removeEmptyValue(Map<String,Object>map) {
		List<String>keyRemove=new ArrayList<>();
		for(Entry<String, Object> item:map.entrySet()) {
			if(item.getValue() instanceof String && StringUtils.isEmpty((String)item.getValue()))
				keyRemove.add(item.getKey());
			else if(item.getValue() instanceof List) {
				
			}
		}
	}
	
	
	
}
