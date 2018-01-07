package org.duanbn.salix.demo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import com.salix.demo.entity.TestEntity;

public class BaseTest {

	protected Random r = new Random();

	public TestEntity createEntity() {
		TestEntity testEntity = new TestEntity();
		testEntity.setTestBool(r.nextBoolean());
		testEntity.setTestByte((byte) r.nextInt(255));
		testEntity.setTestChar((char) r.nextInt(97));
		testEntity.setTestDate(new Date());
		testEntity.setTestDouble(r.nextDouble());
		testEntity.setTestFloat(r.nextFloat());
		testEntity.setTestInt(r.nextInt());
		testEntity.setTestLong(r.nextLong());
		testEntity.setTestShort((short) r.nextInt(30000));
		testEntity.setTestString(getContent(1000));
		testEntity.setTestTime(new Timestamp(System.currentTimeMillis()));
		return testEntity;
	}

	String[] seeds = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" };

	public String getContent(int len) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < len; i++) {
			content.append(seeds[r.nextInt(9)]);
		}
		return content.toString();
	}

}
