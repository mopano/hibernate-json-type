/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 *
 * License: BSD 3-Clause license.
 * See the LICENSE.md file in the root directory or <https://opensource.org/licenses/BSD-3-Clause>.
 * See also <https://tldrlegal.com/license/bsd-3-clause-license-(revised)>.
 */
package com.mopano.hibernate.json.spi;

import com.mopano.hibernate.json.Handling;

import java.util.Map;
import javax.json.spi.JsonProvider;

public interface JsonSettings {

	/**
	 * Choose which SQL handler type to use. String (Mysql-compatible) or PostgreSQL-specific.
	 *
	 * @return default is PostgreSQL-specific
	 */
	public default Handling getSqlHandlerType() {
		return Handling.PGOBJECT;
	}

	/**
	 * Only the last JsonProvider override will be taken into account. If the last one is null, then the default is used.
	 *
	 * @return a provider override or null
	 */
	public default JsonProvider useProvider() {
		return null;
	}

	/**
	 * Only the last TypeName override will be taken into account. If the last one is null, then the default is used.
	 *
	 * @return a TypeName override or null
	 */
	public default String getTypeName() {
		return null;
	}

	/**
	 * Keys from all JsonSettings are accumulated in a Set for configuration.
	 *
	 * @return type registration keys. These are important to Hibernate.
	 */
	public default String[] getRegistrationKeys() {
		return null;
	}

	/**
	 * Values from all JsonSettings are accumulated in a Map for configuration.
	 *
	 * @return settings for the JSON Writer Factory
	 */
	public default Map<String, Object> getWriterFactorySettings() {
		return null;
	}

	/**
	 * Values from all JsonSettings are accumulated in a Map for configuration.
	 *
	 * @return settings for the JSON Reader Factory
	 */
	public default Map<String, Object> getReaderFactorySettings() {
		return null;
	}

}
