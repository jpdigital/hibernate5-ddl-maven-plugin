package de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.types;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

import java.util.BitSet;

/**
 * Copied from the Hibernate Documentation:
 * https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#basic-custom-type
 */
public class BitSetTypeDescriptor extends AbstractTypeDescriptor<BitSet> {

    private static final String DELIMITER = ",";

    public static final BitSetTypeDescriptor INSTANCE
        = new BitSetTypeDescriptor();

    private static final long serialVersionUID = 1L;

    public BitSetTypeDescriptor() {
        super(BitSet.class);
    }

    @Override
    public String toString(final BitSet value) {
        StringBuilder builder = new StringBuilder();
        for (long token : value.toLongArray()) {
            if (builder.length() > 0) {
                builder.append(DELIMITER);
            }
            builder.append(Long.toString(token, 2));
        }
        return builder.toString();
    }

    @Override
    public BitSet fromString(final String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        String[] tokens = string.split(DELIMITER);
        long[] values = new long[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            values[i] = Long.valueOf(tokens[i], 2);
        }
        return BitSet.valueOf(values);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <X> X unwrap(
        final BitSet value, final Class<X> type, final WrapperOptions options
    ) {
        if (value == null) {
            return null;
        }
        if (BitSet.class.isAssignableFrom(type)) {
            return (X) value;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) toString(value);
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> BitSet wrap(final X value, final WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return fromString((String) value);
        }
        if (BitSet.class.isInstance(value)) {
            return (BitSet) value;
        }
        throw unknownWrap(value.getClass());
    }

}
