package com.github.rohansuri.art;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections4.BulkTest;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.collections4.map.AbstractSortedMapTest;

public abstract class AbstractNavigableMapTest<K, V> extends AbstractSortedMapTest<K, V> {
	public AbstractNavigableMapTest(String testName) {
		super(testName);
	}

	public NavigableMap<K, V> makeFullMap() {
		return (NavigableMap<K, V>) super.makeFullMap();
	}

	@Override
	public abstract NavigableMap<K, V> makeObject();

	@Override
	public NavigableMap<K, V> getMap() {
		return (NavigableMap<K, V>) super.getMap();
	}

	@Override
	public NavigableMap<K, V> getConfirmed() {
		return (NavigableMap<K, V>) super.getConfirmed();
	}

	@Override
	public void verify() {
		super.verify();
		// just as org.apache.commons.collections4.set.AbstractNavigableSetTest
		Set<Map.Entry<K, V>> entrySet = this.getMap().entrySet();
		for (Map.Entry<K, V> entry : entrySet) {
			assertEquals(this.getConfirmed().higherEntry(entry.getKey()),
					this.getMap().higherEntry(entry.getKey()));
			assertEquals(this.getConfirmed().higherKey(entry.getKey()),
					this.getMap().higherKey(entry.getKey()));
			assertEquals(this.getConfirmed().lowerEntry(entry.getKey()),
					this.getMap().lowerEntry(entry.getKey()));
			assertEquals(this.getConfirmed().lowerKey(entry.getKey()),
					this.getMap().lowerKey(entry.getKey()));
			assertEquals(this.getConfirmed().lowerKey(entry.getKey()),
					this.getMap().lowerKey(entry.getKey()));
			assertEquals(this.getConfirmed().ceilingEntry(entry.getKey()),
					this.getMap().ceilingEntry(entry.getKey()));
			assertEquals(this.getConfirmed().ceilingKey(entry.getKey()),
					this.getMap().ceilingKey(entry.getKey()));
			assertEquals(this.getConfirmed().floorEntry(entry.getKey()),
					this.getMap().floorEntry(entry.getKey()));
			assertEquals(this.getConfirmed().floorKey(entry.getKey()),
					this.getMap().floorKey(entry.getKey()));
		}
	}

	public NavigableMap<K, V> makeConfirmedMap() {
		return new TreeMap<>();
	}

	public void testFirstEntry() {
		assertNull(this.makeObject().firstEntry());
		NavigableMap<K, V> nm = this.makeFullMap();
		assertEquals(nm.entrySet().iterator().next(), nm.firstEntry());
	}

	public void testLastEntry() {
		assertNull(this.makeObject().lastEntry());
		NavigableMap<K, V> nm = this.makeFullMap();
		Map.Entry<K, V> last = null;

		for (Map.Entry<K, V> kvEntry : nm.entrySet()) {
			last = kvEntry;
		}

		assertEquals(last, nm.lastEntry());
	}

	public void testPollFirstEntry() {
		assertNull(this.makeObject().pollFirstEntry());
		resetFull();
		while (!this.getMap().isEmpty()) {
			assertEquals(this.getConfirmed().pollFirstEntry(), this.getMap().pollFirstEntry());
			verify();
		}
	}

	public void testPollLastEntry() {
		assertNull(this.makeObject().pollLastEntry());
		resetFull();
		while (!this.getMap().isEmpty()) {
			assertEquals(this.getConfirmed().pollLastEntry(), this.getMap().pollLastEntry());
			verify();
		}
	}

	public void testCeilingEntry() {
		assertNull(this.makeObject().ceilingEntry(this.getSampleKeys()[0]));
		resetFull();
		for (int i = 0; i < this.getMap().size(); i++) {
			Map.Entry<K, V> removed = removeIth(this.getMap(), i);
			Map.Entry<K, V> removedFromConfirmed = removeIth(this.getConfirmed(), i);
			assertEquals(removed, removedFromConfirmed);
			assertEquals(this.getMap().ceilingEntry(removed.getKey()),
					this.getConfirmed().ceilingEntry(removedFromConfirmed.getKey()));
			verify();
			this.getMap().put(removed.getKey(), removed.getValue());
			this.getConfirmed().put(removedFromConfirmed.getKey(), removedFromConfirmed.getValue());
			verify();
		}
	}

	public void testFloorEntry() {
		assertNull(this.makeObject().floorEntry(this.getSampleKeys()[0]));
		resetFull();
		for (int i = 0; i < this.getMap().size(); i++) {
			Map.Entry<K, V> removed = removeIth(this.getMap(), i);
			Map.Entry<K, V> removedFromConfirmed = removeIth(this.getConfirmed(), i);
			assertEquals(removed, removedFromConfirmed);
			assertEquals(this.getMap().floorEntry(removed.getKey()),
					this.getConfirmed().floorEntry(removedFromConfirmed.getKey()));
			verify();
			this.getMap().put(removed.getKey(), removed.getValue());
			this.getConfirmed().put(removedFromConfirmed.getKey(), removedFromConfirmed.getValue());
			verify();
		}
	}

	/*
		copy of Entry is needed because Map.Entry is undefined after the map is modified.
		(see Javadoc of Map.Entry)
		https://stackoverflow.com/questions/45863470/treemap-iterator-remove-modifies-the-last-entry
	 */
	private Map.Entry<K, V> removeIth(Map<K, V> m, int pos) {
		Iterator<Map.Entry<K, V>> it = m.entrySet().iterator();
		Map.Entry<K, V> toRemove = null;
		for (int j = 0; j <= pos; j++) {
			toRemove = it.next();
		}
		DefaultMapEntry<K, V> removed = new DefaultMapEntry<>(toRemove);
		it.remove();
		return removed;
	}


	public BulkTest bulkTestDescendingMap() {
		return new TestDescendingMap<>(this);
	}

	public static class TestDescendingMap<K, V> extends AbstractNavigableMapTest<K, V> {
		private final AbstractNavigableMapTest<K, V> main;

		public TestDescendingMap(AbstractNavigableMapTest<K, V> main) {
			super("TestDescendingMap");
			this.main = main;
		}

		public void resetFull() {
			this.main.resetFull();
			super.resetFull();
		}

		public void verify() {
			super.verify();
			this.main.verify();
		}

		@Override
		public void resetEmpty() {
			this.main.resetEmpty();
			super.resetEmpty();
		}

		@Override
		public NavigableMap<K, V> makeObject() {
			return this.main.makeObject().descendingMap();
		}

		@Override
		public NavigableMap<K, V> makeFullMap() {
			return this.main.makeFullMap().descendingMap();
		}

		@Override
		public NavigableMap<K, V> makeConfirmedMap() {
			return this.main.makeConfirmedMap().descendingMap();
		}

		@Override
		public BulkTest bulkTestDescendingMap() {
			return null;
		}

		@Override
		public BulkTest bulkTestHeadMap() {
			return new TestDescendingMap.TestHeadMap<>(this);
		}

		@Override
		public BulkTest bulkTestTailMap() {
			return new TestDescendingMap.TestTailMap<>(this);
		}

		@Override
		public BulkTest bulkTestSubMap() {
			return new TestDescendingMap.TestSubMap<>(this);
		}

		// TODO: explain the need (TestHeadMap doesn't override makeConfirmedMap to main's impl)
		public static class TestHeadMap<K, V> extends AbstractSortedMapTest.TestHeadMap<K, V> {
			private final AbstractNavigableMapTest<K, V> main;

			public TestHeadMap(AbstractNavigableMapTest<K, V> main) {
				super(main);
				this.main = main;
			}

			@Override
			public NavigableMap<K, V> makeConfirmedMap() {
				return this.main.makeConfirmedMap();
			}
		}

		public static class TestSubMap<K, V> extends AbstractSortedMapTest.TestSubMap<K, V> {
			private final AbstractNavigableMapTest<K, V> main;

			public TestSubMap(AbstractNavigableMapTest<K, V> main) {
				super(main);
				this.main = main;
			}

			@Override
			public NavigableMap<K, V> makeConfirmedMap() {
				return this.main.makeConfirmedMap();
			}
		}

		public static class TestTailMap<K, V> extends AbstractSortedMapTest.TestTailMap<K, V> {
			private final AbstractNavigableMapTest<K, V> main;

			public TestTailMap(AbstractNavigableMapTest<K, V> main) {
				super(main);
				this.main = main;
			}

			@Override
			public NavigableMap<K, V> makeConfirmedMap() {
				return this.main.makeConfirmedMap();
			}
		}
	}

}