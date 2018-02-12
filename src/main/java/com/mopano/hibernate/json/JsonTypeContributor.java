/*
 * Copyright (c) Mak Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

import org.jboss.logging.Logger;

public class JsonTypeContributor implements TypeContributor {

	private static final Logger LOGGER = Logger.getLogger(org.hibernate.type.BasicType.class);

	@Override
	public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		JsonType jtype;
		JavaTypeDescriptor jdesc;
		try {
			jtype = JsonType.INSTANCE;
			jdesc = JsonJavaTypeDescriptor.INSTANCE;
		}
		catch (Throwable t) {
			LOGGER.error("JSON type contribution failed!", t);
			return;
		}
		JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jdesc);
		typeContributions.contributeType(jtype);
	}

}
