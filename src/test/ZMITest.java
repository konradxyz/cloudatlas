package test;

import static org.junit.Assert.*;

import org.junit.Test;

import pl.edu.mimuw.cloudatlas.model.ZMI;

public class ZMITest {

	@Test
	public void test() {
		ZMI zmi = new ZMI();
		assertNull(zmi.getFather());

	}

}
