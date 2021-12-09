package com.docotel.ki.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public final class ObjectMapperUtil {
	private static final ObjectMapper JACKSON;

	static {
		JACKSON = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static <T> T fromJson(String str, Class<T> clazz) throws IOException {
		return str != null && !str.equalsIgnoreCase("") ? JACKSON.readValue(str, clazz) : null;
	}

	public static <T> T fromJson(String str, TypeReference<T> typeReference) throws IOException {
		return str != null && !str.equalsIgnoreCase("") ? (T) JACKSON.readValue(str, typeReference) : null;
	}

	public static String toJson(Object o) throws JsonProcessingException {
		return JACKSON.writeValueAsString(o);
	}

	public static <T> T toObject(Map<String, Object> map, Class<T> clazz) {
		return JACKSON.convertValue(map, clazz);
	}

	public static <T> T toObject(Map<String, Object> map, TypeReference<T> typeReference) {
		return JACKSON.convertValue(map, typeReference);
	}

	public static Map<String, Object> toMap(Object object) {
		return (Map) JACKSON.convertValue(object, Map.class);
	}
}
