/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 *
 * License: BSD 3-Clause license.
 * See the LICENSE.md file in the root directory or <https://opensource.org/licenses/BSD-3-Clause>.
 * See also <https://tldrlegal.com/license/bsd-3-clause-license-(revised)>.
 */
package com.mopano.hibernate.json;

import com.mopano.hibernate.json.spi.JsonSettings;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.json.spi.JsonProvider;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.engine.jdbc.spi.JdbcServices;
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
		ClassLoaderService cls = serviceRegistry.getService(ClassLoaderService.class);
		ConfigurationService config = serviceRegistry.getService(ConfigurationService.class);
		Dialect dialect = serviceRegistry.getService( JdbcServices.class ).getDialect();
		String dialectStr = dialect.toString().toLowerCase();
		boolean useMysql = dialectStr.contains("mysql") || dialectStr.contains("mariadb");
		boolean addArrays = config.getSetting("hibernate.arrays.json", StandardConverters.BOOLEAN, Boolean.FALSE);

		try {

			Collection<JsonSettings> settingsList = cls.loadJavaServices( JsonSettings.class );
			if (settingsList.isEmpty()) {
				if (useMysql) {
					jdesc = JsonJavaTypeDescriptor.INSTANCE;
					jtype = new JsonType(jdesc, Handling.STRING, null, null);
					JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jdesc);
					typeContributions.contributeType(jtype, jtype.getRegistrationKeys());
				}
				else {
					jtype = JsonType.INSTANCE;
					jdesc = JsonJavaTypeDescriptor.INSTANCE;
					JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jdesc);
					typeContributions.contributeType(jtype, jtype.getRegistrationKeys());
				}
			}
			else {
				Map<String, Object> rsettings = new TreeMap<>();
				Map<String, Object> wsettings = new TreeMap<>();
				TreeSet<String> regKeys = new TreeSet<>();
				String lastTypeName = null;
				JsonProvider provider = null;
				Handling handler = Handling.PGOBJECT;

				for (JsonSettings jset : settingsList) {
					String[] rk = jset.getRegistrationKeys();
					Map<String, Object> rs = jset.getReaderFactorySettings();
					Map<String, Object> ws = jset.getWriterFactorySettings();
					lastTypeName = jset.getTypeName();
					provider = jset.useProvider();
					handler = jset.getSqlHandlerType();

					if (rk != null && rk.length > 0) {
						for (String key : rk) {
							regKeys.add(key);
						}
					}

					if (rs != null && !rs.isEmpty()) {
						rsettings.putAll(rs);
					}

					if (ws != null && !ws.isEmpty()) {
						wsettings.putAll(ws);
					}
				}

				if (handler == null) {
					throw new NullPointerException("Why did you override the getSqlHandlerType to return null?");
				}

				jdesc = new JsonJavaTypeDescriptor(provider, wsettings, rsettings);
				jtype = new JsonType(jdesc, handler, regKeys.toArray(new String[0]), lastTypeName);
				JavaTypeDescriptorRegistry.INSTANCE.addDescriptor(jdesc);
				typeContributions.contributeType(jtype, jtype.getRegistrationKeys());
			}
		}
		catch (Throwable t) {
			LOGGER.error("JSON type contribution failed!", t);
			return;
		}

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
