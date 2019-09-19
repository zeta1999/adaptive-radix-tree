package com.github.rohansuri.art;

// TODO: better design to avoid LeafNode not having those UnsupportedExceptions?

import java.util.Map;

/*
    avoid this LeafNode extra hop? rather use child pointers as Object? (read notes below)
    i.e using the same pointer for both pointing to child Nodes as well as value

    currently we use what the paper mentions as "Single-value" leaves
 */
class LeafNode<K, V> extends AbstractNode implements Map.Entry<K, V> {
	private V value;

	private static final String EXCEPTION_MSG = "should not be called on LeafNode";

	// we have to save the keyBytes, because leaves are lazy expanded at times (most of the times)
	// confirm this is just a reference?
	private byte[] keyBytes;
	// TODO: is it a good idea to use the BinaryComparable when needed to convert key to keyBytes?
	// I don't think so, memory is cheap, we could keep it stored.

	private K key;

	LeafNode(byte[] keyBytes, K key, V value) {
		this.value = value;
		this.keyBytes = keyBytes;
		this.key = key;
	}

	// we have setters, to make LeafNode instances reusable
	// could we do something better?
	// or is it okie to do this here?
	// anyways LeafNode is internal pojo
	// it's constraints are to be kept in check by internal
	// developers

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public V getValue() {
		return value;
	}

	byte[] getKeyBytes() {
		return keyBytes;
	}

	public K getKey() {
		return key;
	}

	@Override
	public Node findChild(byte partialKey) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public boolean addChild(byte partialKey, Node child) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public void replace(byte partialKey, Node newChild) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public void removeChild(byte partialKey) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public Node grow() {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public boolean shouldShrink() {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public Node shrink() {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	// dev note: first(), last() are implemented to detect end of the firstKey(), lastKey() search
	@Override
	public Node first() {
		return null;
	}

	@Override
	public Node last() {
		return null;
	}

	@Override
	public Node greater(byte partialKey) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public Node lesser(byte partialKey) {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public short size() {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	@Override
	public boolean isFull() {
		throw new UnsupportedOperationException(EXCEPTION_MSG);
	}

	/**
	 * Compares this <code>Map.Entry</code> with another <code>Map.Entry</code>.
	 * <p>
	 * Implemented per API documentation of {@link java.util.Map.Entry#equals(Object)}
	 *
	 * @param obj  the object to compare to
	 * @return true if equal key and value
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Map.Entry)) {
			return false;
		}
		final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
		return
				(getKey() == null ? other.getKey() == null : getKey().equals(other.getKey())) &&
						(getValue() == null ? other.getValue() == null : getValue().equals(other.getValue()));
	}

	/**
	 * Gets a hashCode compatible with the equals method.
	 * <p>
	 * Implemented per API documentation of {@link java.util.Map.Entry#hashCode()}
	 *
	 * @return a suitable hash code
	 */
	@Override
	public int hashCode() {
		return (getKey() == null ? 0 : getKey().hashCode()) ^
				(getValue() == null ? 0 : getValue().hashCode());
	}
}



    /*
        lazily expanded prefix keys
        for now, I don't think we need to store it from the start
        we could store it from where the lazy expansion started

        in case of a single string FOO in tree

        node -> F (follow)
        leafnode that has someValue, "OO"
        rather than prefixKeys storing entire "FOO"

        would this be a fixed array or have to be dynamically growing?

        I think fixed
        since the moment we lazy expand, we have the size of the prefix keys
        that we are lazily going to expand

        but the question is, if ever the prefixKeys expansion changes?
        or has to be appended?

        root -> F (follow)
        leafNode with some value and prefixKeys "OO"

        now if I add FCB?

        root -> F (follow)
        ah I reached a leafNode, must be lazy expanded, I need to expand it now

        node -> O --> leafNode with O
             \--> C --> leafNode with B

        resume:
        we're thinking of cases where the prefixKeys array could be expanded
        why would that happen? when a new keyBytes is added with the same prefix?
        no that would mean two different leaves

        when a new keyBytes is added with a character different from the prefix?
        again that would be a different branch entirely
        it's like adding FOO then adding FOS having prefix keyBytes middle "O" common

        I think lets execute and see if we'd require a growing array


     */
    /*
        should we store pointer to keyBytes? rather than prefixKeys only?
        does the paper specifically take/suggest a side?
     */

/*
    when would we create a lazily expanded LeafNode?
    whenever the next child pointer for any node is null
    i.e it's the first ever branch for these 8 bits we are ever creating
    hence lets lazy expand
 */

/*
    acc to paper this node shouldn't exist? causes extra indirection?
    rather use child pointer to hold reference to value directly?
    in future I think I can imagine using V as the generic type to typecast
    Object while returning the value.
    i.e all our tree Nodes would have Object child = new Node[4];
    pointers and at all places we'd have to do instanceof Node
    when working with child pointers

    question though:
    if we directly point to value, lets say FOO is our search keyBytes
    we're in the node for the last O and our keyBytes ends, so we find the next pointer
    which is a Leaf? or is not instanceof Node?
    that'd be bad/slow

    FOO, FOOS, FOOZ
    node in which I looked up for O and that gave me the next pointer
    if this next pointer is a leafNode
    then we read the value pointer contained in it

    if it is not
    then we lookup the next byte which could be S or Z
    node -> O (follow)
    node -> S
         \--> Z
      or

    node -> O (follow)
    leafNode

    so how'd we accommodate both leaf and next child pointers?
    since it seems the follow pointer could either be a leaf one or a child one

    how would we store keys that are prefixes of each other?

    how about if we modify the keyset by making sure they are never prefixes or each other?

    we do this by ending keyBytes sets by a character that is non existent in it's entire set

    null byte is one such character (for the string keyBytes set space)

    so that means while inserting we need strings to be null byte appended?

    0 is the byte value for strings representing the null byte

    so our keys our now FOOnull FOOSnull FOOZnull

    node -> O (follow)
    node -> S
         \--> Z
         \--> null
    since FOO ends with nul (last byte)
    we take the null path

    which means, we can internally handle this null byte
    we can internally create a null next child pointer when inserting strings
    no need for the user to explicitly add a null byte when he is inserting a keyBytes

    I wonder how we'd handle this in a compositekey case?
    how'd we know if when converting the Object into bytes (ObjectInputStream)
    if the next incoming byte is to be interpreted as a char or a raw byte?

    moreover chars are 2bytes in Java, is that a problem?
    shouldn't be since the table (ascii/unicode) is ordered lexicographically
    so we're good

    btw, this null put would only be needed in cases where such prefixes are possible
    for strings, always needed

    what about composite keys?
    what if I'm serializing a Person object
    whose last field is a string
    and other two are integers
    and all fields are the same but the name is a prefix?
    in such cases we should append a zero
    I think of all primitive types, if string (I know it's not primitive but you know
    what I mean) is the last field to be serialized of an Object
    then we append a null
    (how we'd know if the field we're serializing is a string?
    that's an implementation detail to be figured out later.
    If need be we'd write our own serializer if ObjectInputStream
    doesn't tell us much)
 */