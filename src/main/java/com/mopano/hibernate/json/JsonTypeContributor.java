/*
 * Copyright (c) Mak Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.json;

import java.lang.reflect.Method;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.AbstractStandardBasicType;
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
		typeContributions.contributeType(jtype, jtype.getRegistrationKeys());

		ConfigurationService config = serviceRegistry.getService(ConfigurationService.class);
		ClassLoaderService cls = serviceRegistry.getService(ClassLoaderService.class);

		final boolean addArrays = config.getSetting("hibernate.arrays.json", StandardConverters.BOOLEAN, Boolean.FALSE);

		LOGGER.infof("JSON array type contribution %s", (addArrays ? "enabled" : "disabled"));

		if (addArrays) {
			try {
				Class<?> arrayTypes = cls.classForName("com.mopano.hibernate.array.ArrayTypes");
				Method get = arrayTypes.getDeclaredMethod("get", AbstractStandardBasicType.class, ServiceRegistry.class);
				AbstractSingleColumnStandardBasicType<?> jsonArrayType = (AbstractSingleColumnStandardBasicType<?>) get.invoke(null, jtype, serviceRegistry);
				JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jsonArrayType.getJavaTypeDescriptor());
				typeContributions.contributeType(jsonArrayType);
			}
			catch (Throwable t) {
				LOGGER.error("JSON array type contribution failed!", t);
			}
		}
	}

}
