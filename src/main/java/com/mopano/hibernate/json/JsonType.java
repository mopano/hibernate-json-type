/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class JsonType extends AbstractSingleColumnStandardBasicType<JsonStructure> {

	private static final long serialVersionUID = -7456425337156846489L;

	private final String[] regKeys;
	private final String name;

	private static final String[] DEFAULT_KEYS = new String[]{
		"JSON",
		"json",
		"jsonb",
		Json.createArrayBuilder().add("val").build().getClass().getName(),
		Json.createObjectBuilder().add("name", "value").build().getClass().getName(),
		JsonArray.class.getName(),
		JsonObject.class.getName(),
		JsonStructure.class.getName()
	};
	private static final String DEFAULT_NAME = "JSON";

	public static final JsonType INSTANCE = new JsonType();

	public JsonType() {
		// default values
		this(JsonJavaTypeDescriptor.INSTANCE, Handling.PGOBJECT, DEFAULT_KEYS, DEFAULT_NAME);
	}

	public JsonType(String[] regKeys, String name) {
		this(JsonJavaTypeDescriptor.INSTANCE, Handling.PGOBJECT, regKeys, name);
	}

	@SuppressWarnings("unchecked")
	public JsonType(JavaTypeDescriptor desc, Handling sqlHandler, String[] regKeys, String name) {
		super(sqlHandler == Handling.STRING ? JsonSqlStringHandler.INSTANCE : JsonSqlPGObjectHandler.INSTANCE,
				desc == null ? JsonJavaTypeDescriptor.INSTANCE : desc);
		this.regKeys = regKeys == null || regKeys.length == 0 ? DEFAULT_KEYS : regKeys;
		this.name = name == null || name.isEmpty() ? DEFAULT_NAME : name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String[] getRegistrationKeys() {
		return (String[]) regKeys.clone();
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}

}
