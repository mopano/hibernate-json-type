/*
 * Copyright (c) Mak-Si Management Ltd. Varna, Bulgaria
 * All rights reserved.
 *
 */
package com.mopano.hibernate.test;

import java.util.Arrays;
import java.util.Objects;
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
import javax.persistence.Table;

import org.jboss.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonArrayContributionTest {

	private static EntityManagerFactory emf;

	private static final Logger LOGGER = Logger.getLogger(JsonArrayContributionTest.class);

	@BeforeClass
	public static void setupJPA() {
		emf = Persistence.createEntityManagerFactory("com.mopano.hibernate");
	}

	@AfterClass
	public static void closeJPA() {
		emf.close();
	}

	@Test
	public void confirmProvider() {
		JsonProvider jp = JsonProvider.provider();
		String expected = System.getProperty("expect.provider");
		assertEquals("Wrong JSON provider", expected, jp.getClass().getCanonicalName());
	}

	@Test
//	@Ignore
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
			entity.jba = new JsonStructure[]{ entity.js, entity.jo, entity.ja };
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
			entity1.jba = new JsonStructure[]{ entity1.js, entity1.jo, entity1.ja };
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

	@Entity
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
		@Column(columnDefinition = "jsonb ARRAY")
		private JsonStructure[] jba;

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
					&& (jo == it.jo || Objects.equals(String.valueOf(jo), String.valueOf(it.jo)))
					&& (jba == it.jba || Objects.equals(Arrays.toString(jba), Arrays.toString(it.jba)));
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
					.append(", jba = ")
					.append(jba)
					.append(']');
			return sb.toString();
		}
	}
}
