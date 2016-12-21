/*
 * Copyright (c) Mak Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

public class JsonJavaTypeDescriptor extends AbstractTypeDescriptor<JsonStructure> {

	public static final JsonJavaTypeDescriptor INSTANCE = new JsonJavaTypeDescriptor();
	private static final long serialVersionUID = 4350209361021258277L;

	private final JsonWriterFactory wf = Json.createWriterFactory(Collections.EMPTY_MAP);

	public JsonJavaTypeDescriptor() {
		super(JsonStructure.class);
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
		return Json.createReader(new StringReader(string)).read();
	}

	@Override
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
