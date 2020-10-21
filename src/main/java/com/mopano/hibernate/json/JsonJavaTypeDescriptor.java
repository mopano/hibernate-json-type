/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

public class JsonJavaTypeDescriptor extends AbstractTypeDescriptor<JsonStructure> {

	public static final JsonJavaTypeDescriptor INSTANCE = new JsonJavaTypeDescriptor();
	private static final long serialVersionUID = 4350209361021258277L;

	private final JsonWriterFactory wf;
	private final JsonReaderFactory rf;

	public JsonJavaTypeDescriptor() {
		this(null, Collections.emptyMap(), Collections.emptyMap());
	}

	/**
	 * Any null parameter is replaced by default.
	 *
	 * @param provider
	 * @param writerConfig
	 * @param readerConfig 
	 */
	public JsonJavaTypeDescriptor(JsonProvider provider, Map<String, ?> writerConfig, Map<String, ?> readerConfig) {
		super(JsonStructure.class);
		if (provider == null) {
			provider = JsonProvider.provider();
		}
		wf = provider.createWriterFactory(writerConfig == null ? Collections.emptyMap() : writerConfig);
		rf = provider.createReaderFactory(readerConfig == null ? Collections.emptyMap() : readerConfig);
	}

	@Override
	public String toString(JsonStructure value) {
		StringWriter w = new StringWriter();
		JsonWriter jw = wf.createWriter(w);
		jw.write(value);
		return w.toString();
	}

	@Override
	public JsonStructure fromString(String string) {
		return rf.createReader(new StringReader(string)).read();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <X> X unwrap(JsonStructure value, Class<X> type, WrapperOptions options) {
		if (value == null) {
			return null;
		}

		if (String.class.isAssignableFrom(type)) {
			return (X) this.toString(value);
		}

		throw unknownUnwrap(type);
	}

	@Override
	public <X> JsonStructure wrap(X value, WrapperOptions options) {
		if (value == null) {
			return null;
		}

		Class type = value.getClass();

		if (String.class.isAssignableFrom(type)) {
			String s = (String) value;
			return this.fromString(s);
		}

		throw unknownWrap(type);
	}

}
