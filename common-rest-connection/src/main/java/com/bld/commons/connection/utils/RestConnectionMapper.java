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
 * Utility class for mapping objects to and from JSON, and for building URI query parameters
 * and multi-value maps from a {@code Map<String,Object>}.
 */
@SuppressWarnings({"unchecked"})
public class RestConnectionMapper {

	/**
	 * Serialises an object to a pretty-printed JSON string.
	 *
	 * @param obj the object to serialise
	 * @return the JSON string
	 * @throws JsonProcessingException if serialisation fails
	 */
	public static String fromObjectToJson(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonObj = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		return jsonObj;
	}

	/**
	 * Deserialises a JSON string to an instance of the given class.
	 *
	 * @param <T>      the target type
	 * @param json     the JSON string
	 * @param classObj the target class
	 * @return the deserialised object
	 * @throws JsonParseException   if the JSON is malformed
	 * @throws JsonMappingException if the JSON cannot be mapped to the target type
	 * @throws IOException          if an I/O error occurs
	 */
	public static <T> T fromJsonToEntity(String json, Class<T> classObj)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		T obj = mapper.readValue(json, classObj);
		return obj;
	}

	/**
	 * Converts an object to a {@code Map<String,Object>}.
	 * If the object is already a Map it is returned as-is; otherwise it is first
	 * serialised to JSON and then deserialised to a Map.
	 *
	 * @param obj the object to convert
	 * @return the resulting map
	 * @throws JsonProcessingException if serialisation fails
	 */
	public static Map<String, Object> fromObjectToMap(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<>();
		if (obj != null) {
			if (obj instanceof Map)
				map = (Map<String, Object>) obj;
			else
				map = (Map<String, Object>) mapper.readValue(fromObjectToJson(obj), Map.class);
		}
		return map;
	}

	/**
	 * Appends all entries of the given params map as query parameters on the builder.
	 *
	 * @param builder the URI components builder
	 * @param params  the query parameters
	 */
	public static void builderQuery(UriComponentsBuilder builder, Map<String, Object> params) {
		builder.uriVariables(params);
		for (Entry<String, Object> entry : params.entrySet())
			builder.queryParam(entry.getKey(), entry.getValue());
	}

	/**
	 * Converts a flat {@code Map<String,Object>} to a {@link MultiValueMap}.
	 *
	 * @param params the source map
	 * @return the multi-value map
	 */
	public static MultiValueMap<String, Object> mapToMultiValueMap(Map<String, Object> params) {
		MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
		for (Entry<String, Object> entry : params.entrySet())
			multiValueMap.add(entry.getKey(), entry.getValue());
		return multiValueMap;
	}

	/**
	 * Recursively appends all entries of the given params map as query parameters,
	 * prefixing nested keys with {@code nameObj}.
	 *
	 * @param builder the URI components builder
	 * @param params  the query parameters
	 * @param nameObj the key prefix for nested objects
	 */
	public static void builderQuery(UriComponentsBuilder builder, Map<String, Object> params, String nameObj) {
		builder.uriVariables(params);
		for (Entry<String, Object> entry : params.entrySet()) {
			if (entry.getValue() instanceof Map)
				builderQuery(builder, (Map<String, Object>) entry.getValue(), entry.getKey() + ".");
			else if (entry.getValue() instanceof Collection) {
				List<Object> list = new ArrayList<>((Collection<Object>) entry.getValue());
				for (int i = 0; i < list.size(); i++) {
					Object item = list.get(i);
					if (item instanceof Map)
						builderQuery(builder, (Map<String, Object>) item, entry.getKey() + "[" + i + "].");
					else
						builder.queryParam(nameObj + entry.getKey() + "[" + i + "]", ((List<Object>) entry.getValue()).get(i));
				}
			} else
				builder.queryParam(nameObj + entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Recursively populates a {@link MultiValueMap} from a params map,
	 * prefixing nested keys with {@code nameObj}.
	 *
	 * @param multiValueMap the target multi-value map
	 * @param params        the source map
	 * @param nameObj       the key prefix for nested objects
	 * @return the populated multi-value map
	 */
	public static MultiValueMap<String, Object> mapToMultiValueMap(MultiValueMap<String, Object> multiValueMap, Map<String, Object> params, String nameObj) {
		for (Entry<String, Object> entry : params.entrySet()) {
			if (entry.getValue() instanceof Map)
				mapToMultiValueMap(multiValueMap, (Map<String, Object>) entry.getValue(), entry.getKey() + ".");
			else if (entry.getValue() instanceof Collection) {
				Collection<Object> collection = (Collection<Object>) entry.getValue();
				List<Object> list = new ArrayList<>(collection);
				for (int i = 0; i < list.size(); i++) {
					Object item = list.get(i);
					if (item instanceof Map)
						mapToMultiValueMap(multiValueMap, (Map<String, Object>) item, entry.getKey() + "[" + i + "].");
					else
						multiValueMap.add(nameObj + entry.getKey() + "[" + i + "]", ((List<Object>) entry.getValue()).get(i));
				}
			} else
				multiValueMap.add(nameObj + entry.getKey(), entry.getValue());
		}
		return multiValueMap;
	}

	/**
	 * Removes entries with empty string values from the given map.
	 *
	 * @param map the map to clean
	 */
	public static void removeEmptyValue(Map<String, Object> map) {
		List<String> keyRemove = new ArrayList<>();
		for (Entry<String, Object> item : map.entrySet()) {
			if (item.getValue() instanceof String && StringUtils.isEmpty((String) item.getValue()))
				keyRemove.add(item.getKey());
			else if (item.getValue() instanceof List) {

			}
		}
	}

}
