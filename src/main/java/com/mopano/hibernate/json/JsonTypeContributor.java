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

public class JsonTypeContributor implements TypeContributor {

	@Override
	public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
		JsonType jtype;
		JavaTypeDescriptor jdesc;
		try {
			jtype = JsonType.INSTANCE;
			jdesc = JsonJavaTypeDescriptor.INSTANCE;
		}
		catch (Throwable t) {
			// Avoid logging system for a project so small
			System.err.println("JSON type contribution failed! Message: " + t.getMessage());
			t.printStackTrace(System.err);
			return;
		}
		JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jdesc);
		typeContributions.contributeType(jtype);
	}

}
