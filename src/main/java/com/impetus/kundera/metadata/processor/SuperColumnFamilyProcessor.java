/*
 * Copyright 2010 Impetus Infotech.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.impetus.kundera.metadata.processor;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.impetus.kundera.api.SuperColumn;
import com.impetus.kundera.api.SuperColumnFamily;
import com.impetus.kundera.ejb.EntityManagerFactoryImpl;
import com.impetus.kundera.metadata.EntityMetadata;

/**
 * The Class SuperColumnFamilyMetadataProcessor.
 * 
 * @author animesh.kumar
 */
public class SuperColumnFamilyProcessor extends AbstractEntityFieldProcessor {

	/** The Constant log. */
	private static final Log LOG = LogFactory
			.getLog(SuperColumnFamilyProcessor.class);

	/** The em. */
	private EntityManagerFactoryImpl em;

	/**
	 * Instantiates a new super column family processor.
	 * 
	 * @param em
	 *            the em
	 */
	public SuperColumnFamilyProcessor(EntityManagerFactory em) {
		this.em = (EntityManagerFactoryImpl) em;
	}

	/*
	 * @see
	 * com.impetus.kundera.metadata.MetadataProcessor#process(java.lang.Class,
	 * com.impetus.kundera.metadata.EntityMetadata)
	 */
	@Override
	public final void process(Class<?> clazz, EntityMetadata metadata) {

		if (!clazz.isAnnotationPresent(SuperColumnFamily.class)) {
			return;
		}

		LOG.debug("Processing @Entity " + clazz.getName()
				+ " for SuperColumnFamily.");

		metadata.setType(EntityMetadata.Type.SUPER_COLUMN_FAMILY);

		// check for SuperColumnFamily annotation.
		SuperColumnFamily scf = clazz.getAnnotation(SuperColumnFamily.class);

		// set columnFamily
		metadata.setColumnFamilyName(scf.family());

		// set keyspace
		String keyspace = scf.keyspace().length() != 0 ? scf.keyspace() : em
				.getKeyspace();
		metadata.setKeyspaceName(keyspace);

		// scan for fields
		for (Field f : clazz.getDeclaredFields()) {

			// if @Id
			if (f.isAnnotationPresent(Id.class)) {
				LOG.debug(f.getName() + " => Id");
				metadata.setIdProperty(f);
				populateIdAccessorMethods(metadata, clazz, f);
			}
			// if @SuperColumn
			else if (f.isAnnotationPresent(SuperColumn.class)) {
				SuperColumn sc = f.getAnnotation(SuperColumn.class);
				String superColumnName = sc.column();

				String columnName = getValidJPAColumnName(clazz, f);
				if (null == columnName) {
					continue;
				}
				LOG.debug(f.getName() + " => Column:" + columnName
						+ ", SuperColumn:" + superColumnName);

				EntityMetadata.SuperColumn superColumn = metadata
						.getSuperColumn(superColumnName);
				if (null == superColumn) {
					superColumn = metadata.new SuperColumn(superColumnName);
				}
				superColumn.addColumn(columnName, f);
				metadata.addSuperColumn(superColumnName, superColumn);

			}
		}
	}
}
