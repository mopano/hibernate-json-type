/*
 * Copyright (c) Mak Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;

public class JsonType extends AbstractSingleColumnStandardBasicType<JsonStructure> {

	private static final long serialVersionUID = -7456425337156846489L;

	private final String[] regKeys;
	private final String name;

	public static final JsonType INSTANCE = new JsonType();

	public JsonType() {
		super(JsonSqlTypeDescriptor.INSTANCE, JsonJavaTypeDescriptor.INSTANCE);
		regKeys = new String[]{
			"JSON",
			"json",
			"jsonb",
			Json.createArrayBuilder().add("val").build().getClass().getName(),
			Json.createObjectBuilder().add("name", "value").build().getClass().getName(),
			JsonArray.class.getName(),
			JsonObject.class.getName(),
			JsonStructure.class.getName()
		};
		name = "JSON";
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String[] getRegistrationKeys() {
		return (String[]) regKeys.clone();
	}

	@Override
	protected boolean registerUnderJavaType() {
		return true;
	}

}
