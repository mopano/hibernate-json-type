/*
 * Copyright (c) Mak Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.test;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.spi.JsonProvider;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonContributionTest {

	private static EntityManagerFactory emf;

	private static final Logger LOGGER = LogManager.getLogger(JsonContributionTest.class);

	@BeforeClass
	public static void setupJPA() {
		emf = Persistence.createEntityManagerFactory("com.mopano.hibernate");
	}

	@AfterClass
	public static void closeJPA() {

	}

	@Test
	public void confirmProvider() {
		JsonProvider jp = JsonProvider.provider();
		String expected = System.getProperty("expect.provider");
		assertEquals("Wrong JSON provider", expected, jp.getClass().getCanonicalName());
	}

	@Test
	public void testWriteRead() {

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		JsonProvider jp = JsonProvider.provider();

		try {
			MyEntity entity = new MyEntity();
			entity.id = 1l;
			entity.js = jp.createArrayBuilder().add("str").add(false).add(0l).build();
			entity.jo = jp.createObjectBuilder().add("thearray", jp.createArrayBuilder().add("str").add(false).add(0l).build()).build();
			entity.ja = jp.createArrayBuilder().add(4).add("word").build();
			LOGGER.info("Persisting entity: " + entity);
			em.persist(entity);
			entity = new MyEntity();
			entity.id = 2l;
			// leave nulls because postgres is being shitty with those
			LOGGER.info("Persisting entity: " + entity);
			em.persist(entity);
			em.getTransaction().commit();
		}
		finally {
			if (em.getTransaction() != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}

		em = emf.createEntityManager();
		em.getTransaction().begin();

		try {
			MyEntity entity1 = new MyEntity();
			entity1.id = 1l;
			entity1.js = jp.createArrayBuilder().add("str").add(false).add(0l).build();
			entity1.jo = jp.createObjectBuilder().add("thearray", jp.createArrayBuilder().add("str").add(false).add(0l).build()).build();
			entity1.ja = jp.createArrayBuilder().add(4).add("word").build();
			MyEntity entity2 = new MyEntity();
			entity2.id = 2l;
			MyEntity me1 = em.find(MyEntity.class, new Long(1));
			LOGGER.info("Extracted entity: " + me1);
			MyEntity me2 = em.find(MyEntity.class, new Long(2));
			LOGGER.info("Extracted entity: " + me2);
			assertEquals(entity1, me1);
			assertEquals(entity2, me2);
		}
		finally {
			if (em.getTransaction() != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testParameter() {
		// jsonb_contains(jsonb, jsonb)

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		JsonProvider jp = JsonProvider.provider();

		try {
			//version check
			Query vq = em.createNativeQuery("SELECT version()");
			String versionString = vq.getSingleResult().toString();
			Pattern pat = Pattern.compile("^PostgreSQL (\\d{1,2})\\.(\\d+)\\.");
			Matcher mat = pat.matcher(versionString);
			if (!mat.find()) {
				// incompatible database
				return;
			}
			int majorVersion = Integer.parseInt(mat.group(1));
			int minorVersion = Integer.parseInt(mat.group(2));
			if (majorVersion == 9 && minorVersion < 4) {
				// this version does not support jsonb type, which is the only one which we can test
				// binding parameters on.
				return;
			}

			MyEntity entity = new MyEntity();
			entity.id = 5l;
			entity.js = jp.createArrayBuilder().add("nothing").add("else").add("matters").build();
			entity.jo = null;
			entity.ja = null;
			LOGGER.info("Persisting entity: " + entity);
			em.persist(entity);

			entity = new MyEntity();
			entity.id = 6l;
			entity.js = jp.createArrayBuilder().add("str").add(false).add(0l).build();
			entity.jo = jp.createObjectBuilder().add("thearray", jp.createArrayBuilder().add("str").add("unique").add(false).add(0l).build()).build();
			entity.ja = jp.createArrayBuilder().add(4).add("word").build();
			LOGGER.info("Persisting entity: " + entity);
			em.persist(entity);
			em.flush();
			vq = null; // release pointer
			em.getTransaction().commit();
			// end setup-data

			em.getTransaction().begin();
			Query q = em.createNativeQuery("SELECT * FROM json_entity WHERE jsonb_contains( CAST(jo AS jsonb), :js )", MyEntity.class);
			// Cannot bind JsonArray values, because they have all chosen to extend java.util.AbstractList
			JsonStructure js = jp.createObjectBuilder().add("thearray", jp.createArrayBuilder().add("unique").build()).build();
			q.setParameter("js", js);
			List<MyEntity> results = q.getResultList();
			assertEquals("Wrong number of entities returned", 1, results.size());
			MyEntity fetched = results.get(0);
			assertEquals("Wrong entity returned", 6L, fetched.id.longValue());

			// this part of the test requires a proposed feature change to Hibernate 5.2
			q = em.createNativeQuery("SELECT * FROM json_entity WHERE jsonb_contains( CAST(js AS jsonb), :js )", MyEntity.class);
			js = jp.createArrayBuilder().add("nothing").build();
			q.setParameter("js", js);
			results = q.getResultList();
			assertEquals("Wrong number of entities returned", 1, results.size());
			fetched = results.get(0);
			assertEquals("Wrong entity returned", 5L, fetched.id.longValue());
		}
		finally {
			if (em.getTransaction() != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}

	@Entity(name = "JSON_ENTITY")
	@Table(name = "json_entity")
	public static class MyEntity {

		@Id
		public Long id;
		@Column(columnDefinition = "json")
		public JsonStructure js;
		@Column(columnDefinition = "json")
		public JsonArray ja;
		@Column(columnDefinition = "json")
		public JsonObject jo;

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof MyEntity)) {
				return false;
			}
			MyEntity it = (MyEntity) other;
			// assuming (possibly incorrectly) that the implementations take care of their equals() properly
			return Objects.equals(id, it.id)
					&& (js == it.js || Objects.equals(String.valueOf(js), String.valueOf(it.js)))
					&& (ja == it.ja || Objects.equals(String.valueOf(ja), String.valueOf(it.ja)))
					&& (jo == it.jo || Objects.equals(String.valueOf(jo), String.valueOf(it.jo)));
		}

		@Override
		public int hashCode() {
			// assuming (possibly incorrectly) that the implementations take care of their hashCode() properly
			return Objects.hash(id, js, ja, jo);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[')
					.append("id = ")
					.append(id)
					.append(", js = ")
					.append(js)
					.append(", ja = ")
					.append(ja)
					.append(", jo = ")
					.append(jo)
					.append(']');
			return sb.toString();
		}
	}
}
