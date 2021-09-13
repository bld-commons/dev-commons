package com.bld.commons.connection.interceptor;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.bld.commons.connection.constant.CommonRestConnectionCostant;
import com.bld.commons.connection.utils.RestConnectionMapper;
import com.bld.commons.connection.utils.ValidatorUtils;

@SuppressWarnings("unchecked")
public class RestConnectionInterceptor implements HandlerInterceptor {

	private final static Log logger = LogFactory.getLog(RestConnectionInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		logger.debug("Intercept request");
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;

			String commonRestConnection = request.getHeader(CommonRestConnectionCostant.COMMON_REST_CONNECTION);
			Map<String, Object> map = request.getParameterMap();
			Method method = handlerMethod.getMethod();
			if (method.getParameters().length == 1) {
				Parameter parameter = method.getParameters()[0];
				Class<?> classParam = parameter.getType();
				RequestAttribute requestAttribute = parameter.getAnnotation(RequestAttribute.class);
				Valid valid = parameter.getAnnotation(Valid.class);
				if (requestAttribute != null && StringUtils.isNotEmpty(commonRestConnection)) {
					Map<String, Object> newElement = new LinkedHashMap<>();
					for (Entry<String, Object> item : map.entrySet()) {
						String[] keys = item.getKey().split("\\.");
						getMapObject(newElement, keys, 0, item.getValue());
					}

					String json = RestConnectionMapper.fromObjectToJson(newElement);
					logger.debug(json);
					Object objApp = RestConnectionMapper.fromJsonToEntity(json, classParam);

					String nameAttribute = StringUtils.isNotEmpty(requestAttribute.name()) ? requestAttribute.name() : parameter.getName();
					if (valid != null)
						ValidatorUtils.checkValidatrBuildClass(objApp);
					request.setAttribute(nameAttribute, objApp);

					logger.debug("Finish");

				}

			}

		}
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	private Object getMapObject(Map<String, Object> map, String[] keys, int i, Object value) throws UnsupportedEncodingException {
		Object object = null;
		if (i < keys.length) {
			String key = keys[i];
			key = URLDecoder.decode(key, "UTF-8");
			List<Object> list = null;
			Integer index = null;
			Pattern p = Pattern.compile(".*\\[.*?\\]$");// . represents single character
			Matcher m = p.matcher(key);
			boolean isList = m.matches();
			if (isList) {
				index = Integer.valueOf(key.substring(key.indexOf("[")).replace("[", "").replace("]", ""));
				key = key.substring(0, key.indexOf("[")).replace("[", "");
			}
			object = map.get(key);
			if (object == null) {
				if (isList) {
					list = new ArrayList<>();
					list.add(getMapObject(new LinkedHashMap<>(), keys, ++i, value));
					map.put(key, list);
					object = list;
				} else {
					map.put(key, getMapObject(new LinkedHashMap<>(), keys, ++i, value));
					object = map;
				}

			} else {
				if (isList) {
					list = (List<Object>) object;
					if (index >= list.size())
						list.add(getMapObject(new LinkedHashMap<>(), keys, ++i, value));
					else
						getMapObject((Map<String, Object>) list.get(index), keys, ++i, value);
					object = list;
				} else
					map.put(key, getMapObject((Map<String, Object>) object, keys, ++i, value));

			}

		} else if (value instanceof Object[]) {
			Object[] array = ((Object[]) value);
			if (array.length == 1)
				return StringUtils.isNotEmpty((String) array[0]) ? array[0] : null;
			else
				return array.length == 0 ? null : value;
		} else
			return value;

		return object;
	}

}
