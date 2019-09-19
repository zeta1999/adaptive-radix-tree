package com.github.rohansuri.art;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BinaryComparableUtilsTest {

	@Test
	public void testTerminator() {
		// empty string case
		assertArray("");

		// normal case, test last portion copy
		assertArray("barca");
	}

	@Test
	public void testTerminatorNullCase() {
		String s = new String(new byte[] {0}); // null
		byte[] bytes = BinaryComparables.UTF8.get(s);
		int terminatorLen = BinaryComparableUtils.TERMINATOR.length;
		assertEquals(s.length() + terminatorLen + 1, bytes.length);
		byte[] expected = new byte[] {0, 1, 0, 0};
		assertArrayEquals(expected, bytes);
	}

	@Test
	public void testTerminatorMultipleNullsCase() {
		String s = new String(new byte[] {0, 76, 0, 69, 0, 79, 0}); // null, L, null, E, null, O, null
		byte[] bytes = BinaryComparables.UTF8.get(s);
		int terminatorLen = BinaryComparableUtils.TERMINATOR.length;
		// 3 because of the three "1" bytes added for each of the nulls
		assertEquals(s.length() + terminatorLen + 4, bytes.length);
		byte[] expected = new byte[] {0, 1, 76, 0, 1, 69, 0, 1, 79, 0, 1, 0, 0};
		assertArrayEquals(expected, bytes);
	}

	private void assertArray(String s) {
		int terminatorLen = BinaryComparableUtils.TERMINATOR.length;
		byte[] bytes = BinaryComparables.UTF8.get(s);
		assertEquals(s.length() + terminatorLen, bytes.length);
		ByteArrayOutputStream expected = new ByteArrayOutputStream(s.length() + terminatorLen);
		expected.write(s.getBytes(StandardCharsets.US_ASCII), 0, s.length());
		expected.write(BinaryComparableUtils.TERMINATOR, 0, terminatorLen);
		assertArrayEquals(expected.toByteArray(), bytes);
	}

	/*
		-128 to 127 available signed range
		but we want to interpret it as unsigned (since Java does not have unsigned types)
		so that we can order the bytes right.
		in the unsigned form, bytes with MSB set would come later and be "greater"
		than ones with MSB unset.
		Therefore we need to treat all bytes as unsigned.
		And for that, we need to map "1111 1111" as the largest byte.
		But since we have signed bytes, we'd need to map "1111 1111" to 127
		(since that's the highest positive value we can store)
		"1111 1111" in the 2s complement (signed form) is -1.
		But we don't care about the interpretation.
		We want the mapping right.

		0000 0000 (0) = -128 (least possible byte value that can be assigned)
		0000 0001 = -127
		....
		0111 1111 (127) = -1
		1000 0000 (-128) = 0
		1000 0001 (-127) = 1
		...
		1111 1110 = 126
		1111 1111 (-1) = 127
	 */
	@Test
	public void testInterpretUnsigned() {
		// forget the byte being supplied to unsigned method
		// think of the lexicographic bit representation
		assertTrue((byte) 0 == BinaryComparableUtils.unsigned((byte) -128)); // 1000 0000
		assertTrue((byte) 127 == BinaryComparableUtils.unsigned((byte) -1)); // 1111 1111
		assertTrue((byte) -128 == BinaryComparableUtils.unsigned((byte) 0)); // 0000 0000
		assertTrue((byte) -127 == BinaryComparableUtils.unsigned((byte) 1)); // 0000 0001
		assertTrue((byte) -1 == BinaryComparableUtils.unsigned((byte) 127)); // 0111 1111
		assertTrue((byte) 1 == BinaryComparableUtils.unsigned((byte) -127)); // 1000 0001
	}
}