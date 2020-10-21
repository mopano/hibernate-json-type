/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.test;

import com.mopano.hibernate.json.Handling;
import com.mopano.hibernate.json.spi.JsonSettings;
import java.util.Map;
import java.util.TreeMap;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import org.apache.johnzon.core.JsonParserFactoryImpl;
import org.apache.johnzon.core.JsonProviderImpl;

public class JsonSettingsImpl implements JsonSettings {

	private final JsonProvider jp = new JsonProviderImpl();;

	@Override
	public Map<String, Object> getReaderFactorySettings() {
		Map<String, Object> readerSettings = new TreeMap<>();
		readerSettings.put(JsonParserFactoryImpl.SUPPORTS_COMMENTS, Boolean.TRUE);
		return readerSettings;
	}

	@Override
	public Map<String, Object> getWriterFactorySettings() {
		Map<String, Object> writerSettings = new TreeMap<>();
		writerSettings.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		return writerSettings;
	}

	@Override
	public String[] getRegistrationKeys() {
		return new String[]{
			"JSON",
			"json",
			jp.createArrayBuilder().add("val").build().getClass().getName(),
			jp.createObjectBuilder().add("name", "value").build().getClass().getName(),
			JsonArray.class.getName(),
			JsonObject.class.getName(),
			JsonStructure.class.getName()
		};
	}

	@Override
	public String getTypeName() {
		return "json";
	}

	@Override
	public JsonProvider useProvider() {
		return jp;
	}

	@Override
	public Handling getSqlHandlerType() {
		return Handling.STRING;
	}

}
