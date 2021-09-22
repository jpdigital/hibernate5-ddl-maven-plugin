package de.jpdigital.maven.plugins.hibernate5ddl.tests.packageannotations.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

import java.util.BitSet;

/**
 * Copied from the Hibernate Documentation:
 * https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#basic-custom-type
 */
public class BitSetType
    extends AbstractSingleColumnStandardBasicType<BitSet>
    implements DiscriminatorType<BitSet> {

    public static final BitSetType INSTANCE = new BitSetType();
    
    public static final String BIT_SET_TYPE = BitSetType.class.getName();

    private static final long serialVersionUID = 1L;

    public BitSetType() {
        super(VarcharTypeDescriptor.INSTANCE, BitSetTypeDescriptor.INSTANCE);
    }

    @Override
    public BitSet stringToObject(final String xml) throws Exception {
        return fromString(xml);
    }

    @Override
    public String objectToSQLString(
        final BitSet value, final Dialect dialect)
        throws Exception {
        return toString(value);
    }

    @Override
    public String getName() {
        return "bitset";
    }

}
