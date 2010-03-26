/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.exxcellent.enterprise.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Immutable Object use as holder for monetary values.
 *
 * @author stauss
 *
 */
@Embeddable
public class Money implements Comparable<Money>, Serializable {

    // --------------------------------- CLASS FIELDS --------------------------
    public static final String DEFAULT_CURRENCY = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
    /**
     * Zero Value.
     */
    public static final Money ZERO = new Money(BigDecimal.ZERO, DEFAULT_CURRENCY);
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    // --------------------------------- OBJECT FIELDS -------------------------
    /**
     * Money Value. initialized ZERO
     */
    @Basic
    @Column(name = "value", nullable = false, unique = false)
    private BigDecimal cash = BigDecimal.ZERO;
    /**
     * Money currency. initialized {@link DEFAULT_CURRENCY}
     */
    @Basic
    @Column(name = "currency", nullable = false, unique = false)
    private String currency = DEFAULT_CURRENCY;

    // --------------------------------- CLASS CONSTRUCTORS --------------------
    /**
     * Copy Constructor.
     *
     * @param money
     *            Geldwert
     */
    public Money(Money money) {
        super();
        if (money != null) {
            this.setCash(money.getCash());
            this.setCurrency(money.getCurrency());
        }
    }

    /**
     * Parameter Constructor.
     *
     * @param cash
     *            wert
     * @param currency
     *            waehrung
     */
    public Money(String cash, String currency) {
        super();
        if (cash != null) {
            this.setCash(new BigDecimal(cash));
        }
        this.setCurrency(currency);
    }

    /**
     * Parameter Constructor.
     *
     * @param cash
     *            wert
     * @param currency
     *            waehrung
     */
    public Money(BigDecimal cash, String currency) {
        super();
        this.setCash(cash);
        this.setCurrency(currency);
    }

    /**
     * Default Constrctor.
     */
    public Money() {
        super();
    }

    // --------------------------------- PROPERTY METHODS ----------------------
    /**
     * Wert.
     *
     * @param cash
     *            wert
     */
    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    /**
     * Wert.
     *
     * @return the value
     */
    public BigDecimal getCash() {
        return cash;
    }

    /**
     * Waehrung.
     *
     * @param currency
     *            the currency to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Waehrung.
     *
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    // --------------------------------- OBJECT METHODS ------------------------
    /**
     * Object Method.
     *
     * @see Comparable#compareTo(Object)
     * @param that
     *            the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException
     *             if the specified object's type prevents it from being compared to this object.
     * @throws IllegalStateException
     *             if the currency are not the same
     */
    @Override
    public int compareTo(Money that) throws ClassCastException, IllegalStateException {
        // ZERO is zero in every currency :-)
        if (BigDecimal.ZERO.equals(this.getCash()) && BigDecimal.ZERO.equals(that.getCash())) {
            return 0;
        }
        Money one = ensureZeroWithValuableCurrency(this, that);
        return one.getCash().compareTo(that.getCash());
    }

    /**
     * Object Method.
     *
     * @see java.util.Hashtable
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getCash() == null ? null : getCash().stripTrailingZeros()).append(getCurrency()).toHashCode();
    }

    /**
     * Object Method.
     *
     * @see Object#equals(Object)
     * @param o
     *            other object
     * @return boolean true when objects are equals
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Money other = (Money) o;
        if (this.zero() && other.zero()) {
            return true;
        }
        return new EqualsBuilder() // build equals
                .appendSuper(getCash().compareTo(other.getCash()) == 0) // equality of BigDecimal is best checked with compareTo
                .append(getCurrency(), (other).getCurrency()) // Check for same currency
                .isEquals();
    }

    /**
     * Object Method.
     *
     * @see Object#toString()
     * @return {@link String}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(getCash()).append(getCurrency()).toString();
    }

    // --------------------------------- BUSINESS METHODS ----------------------
    /**
     * Normalize the value of money by rounding the fraction digits in dependency of the currency.
     *
     * @param roundingMode
     *            the {@link RoundingMode}
     * @return Money normalized
     */
    public Money normalize(RoundingMode roundingMode) {
        return new Money(getCash().setScale(Currency.getInstance(getCurrency()).getDefaultFractionDigits(), roundingMode), this.getCurrency());
    }

    /**
     * Normalize the value of money by rounding the fraction digits in dependency of the currency. Default {@link RoundingMode} is {@link RoundingMode#HALF_UP}
     *
     * @return Money normalized
     */
    public Money normalize() {
        return normalize(RoundingMode.HALF_UP);
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is the absolute value of this <tt>Money</tt>, and whose scale is <tt>this.scale()</tt>.
     *
     * @return <tt>abs(this)</tt>
     */
    public Money abs() {
        return new Money(this.getCash().abs(), this.getCurrency());
    }

    // Arithmetic Operations
    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this +
     * augend)</tt>, and whose scale is <tt>max(this.scale(),
     * augend.scale())</tt>.
     *
     * @param augend
     *            value to be added to this <tt>Money</tt>.
     * @return <tt>this + augend</tt>
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money add(Money augend) throws IllegalArgumentException {
        Money one = ensureZeroWithValuableCurrency(this, augend);
        return new Money(one.getCash().add(augend.getCash()), one.getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this -
     * subtrahend)</tt>, and whose scale is <tt>max(this.scale(),
     * subtrahend.scale())</tt>.
     *
     * @param subtrahend
     *            value to be subtracted from this <tt>Money</tt>.
     * @return <tt>this - subtrahend</tt>
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money subtract(Money subtrahend) throws IllegalArgumentException {
        Money one = ensureZeroWithValuableCurrency(this, subtrahend);
        return new Money(one.getCash().subtract(subtrahend.getCash()), one.getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this &times;
     * multiplicand)</tt>, and whose scale is <tt>(this.scale() +
     * multiplicand.scale())</tt>.
     *
     * @param multiplicand
     *            value to be multiplied by this <tt>Money</tt>.
     * @return <tt>this * multiplicand</tt>
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money multiply(Money multiplicand) throws IllegalArgumentException {
        Money one = ensureZeroWithValuableCurrency(this, multiplicand);
        return new Money(one.getCash().multiply(multiplicand.getCash()), one.getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this /
     * divisor)</tt>, and whose scale is as specified. If rounding must be performed to generate a result with the specified scale, the specified rounding mode
     * is applied.
     *
     * @param divisor
     *            value by which this <tt>Money</tt> is to be divided.
     * @param scale
     *            scale of the <tt>Money</tt> quotient to be returned.
     * @param roundingMode
     *            rounding mode to apply.
     * @return <tt>this / divisor</tt>
     * @throws ArithmeticException
     *             if <tt>divisor</tt> is zero, <tt>roundingMode==RoundingMode.UNNECESSARY</tt> and the specified scale is insufficient to represent the result
     *             of the division exactly.
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money divide(Money divisor, int scale, RoundingMode roundingMode) throws ArithmeticException, IllegalArgumentException {
        Money one = ensureZeroWithValuableCurrency(this, divisor);
        return new Money(one.getCash().divide(divisor.getCash(), scale, roundingMode), one.getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this /
     * divisor)</tt>, and whose scale is <tt>this.scale()</tt>. If rounding must be performed to generate a result with the given scale, the specified rounding
     * mode is applied.
     *
     * @param divisor
     *            value by which this <tt>Money</tt> is to be divided.
     * @param roundingMode
     *            rounding mode to apply.
     * @return <tt>this / divisor</tt>
     * @throws ArithmeticException
     *             if <tt>divisor==0</tt>, or <tt>roundingMode==ROUND_UNNECESSARY</tt> and <tt>this.scale()</tt> is insufficient to represent the result of the
     *             division exactly.
     * @throws IllegalArgumentException
     *             if <tt>roundingMode</tt> does not represent a valid rounding mode.
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money divide(Money divisor, RoundingMode roundingMode) throws ArithmeticException, IllegalArgumentException {
        return this.divide(divisor, getCash().scale(), roundingMode);
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this /
     * divisor)</tt>, and whose preferred scale is <tt>(this.scale() -
     * divisor.scale())</tt>; if the exact quotient cannot be represented (because it has a non-terminating decimal expansion) an <tt>ArithmeticException</tt>
     * is thrown.
     *
     * @param divisor
     *            value by which this <tt>Money</tt> is to be divided.
     * @return <tt>this / divisor</tt>
     * @throws ArithmeticException
     *             if the exact quotient does not have a terminating decimal expansion
     * @throws IllegalArgumentException
     *             currency mismatch
     */
    public Money divide(Money divisor) throws ArithmeticException, IllegalArgumentException {
        Money one = ensureZeroWithValuableCurrency(this, divisor);
        return new Money(one.getCash().divide(divisor.getCash()), one.getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(this<sup>n</sup>)</tt> , The power is computed exactly, to unlimited precision.
     *
     * <p>
     * The parameter <tt>n</tt> must be in the range 0 through 999999999, inclusive. <tt>ZERO.pow(0)</tt> returns ONE.
     *
     * Note that future releases may expand the allowable exponent range of this method.
     *
     * @param n
     *            power to raise this <tt>Money</tt> to.
     * @return <tt>this<sup>n</sup></tt>
     * @throws ArithmeticException
     *             if <tt>n</tt> is out of range.
     */
    public Money pow(int n) throws ArithmeticException {
        return new Money(getCash().pow(n), getCurrency());
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(-this)</tt>, and whose scale is <tt>this.scale()</tt>.
     *
     * @return <tt>-this</tt>.
     */
    public Money negate() {
        return new Money(getCash().negate(), getCurrency());
    }

    /**
     * Math. isNegative?
     *
     * @return boolean true when negative
     */
    public boolean negative() {
        return (getCash().compareTo(BigDecimal.ZERO) < 0);
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is <tt>(+this)</tt>, and whose scale is <tt>this.scale()</tt>.
     *
     * <p>
     * This method, which simply returns this <tt>Money</tt> is included for symmetry with the unary minus method {@link #negate()}.
     *
     * @return <tt>this</tt>.
     * @see #negate()
     */
    public Money plus() {
        return new Money(this);
    }

    /**
     * Math. isPositive?
     *
     * @return boolean true when positive
     */
    public boolean positive() {
        return (getCash().compareTo(BigDecimal.ZERO) >= 0);
    }

    /**
     * Math. isZero?
     *
     * @return boolean true when ZERO
     */
    public boolean zero() {
        if (this == ZERO) {
            return true;
        }
        return this.getCash() == null || BigDecimal.ZERO.compareTo(this.getCash()) == 0;
    }

    /**
     * Math. Returns the signum function of this <tt>Money</tt>.
     *
     * @return -1, 0, or 1 as the value of this <tt>Money</tt> is negative, zero, or positive.
     */
    public int signum() {
        return this.getCash().signum();
    }

    /**
     * Math. Returns the <i>scale</i> of this <tt>Money</tt>. If zero or positive, the scale is the number of digits to the right of the decimal point. If
     * negative, the unscaled value of the number is multiplied by ten to the power of the negation of the scale. For example, a scale of <tt>-3</tt> means the
     * unscaled value is multiplied by 1000.
     *
     * @return the scale of this <tt>Money</tt>.
     */
    public int scale() {
        return this.getCash().scale();
    }

    /**
     * Math. Returns the <i>precision</i> of this <tt>Money</tt>. (The precision is the number of digits in the unscaled value.)
     *
     * <p>
     * The precision of a zero value is 1.
     *
     * @return the precision of this <tt>Money</tt>.
     */
    public int precision() {
        return this.getCash().precision();
    }

    /**
     * Math. Returns a <tt>Money</tt> whose value is the <i>unscaled value</i> of this <tt>Money</tt>. (Computes <tt>(this *
     * 10<sup>this.scale()</sup>)</tt>.)
     *
     * @return the unscaled value of this <tt>Money</tt>.
     */
    public BigInteger unscaledValue() {
        return this.getCash().unscaledValue();
    }

    // --------------------------------- CLASS METHODS -------------------------
    /**
     * Method allow to parse Strings like "10.00 EUR" or "EUR 10.00".
     *
     * @param str
     *            String to parse
     * @param delim
     *            Delimiter used for tokenize.
     * @return {@link Money} created instance.
     */
    public static Money parse(String str, String delim) {
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        if (tokenizer.countTokens() != 2) {
            throw new IllegalStateException("Value [" + str + "] is illegal. Should contain 2 token separated with [" + delim + "] delim");
        }
        String one = tokenizer.nextToken();
        String two = tokenizer.nextToken();
        BigDecimal value = isBigDecimal(one);
        if (value != null) {
            return new Money(value, two);
        }
        value = isBigDecimal(two);
        if (value != null) {
            return new Money(value, one);
        }
        return new Money(one, two);
    }

    /**
     * Helper Method. Ensures that the currency handled in a correct mannor, when you combine (add, substract, ...) two values with different currencies. It is
     * allowed to combine these Values, when one of them is ZERO.
     *
     * @param one
     *            Master Money
     * @param two
     *            Slave Money
     * @return one or new Value with corrected currency.
     * @throws IllegalArgumentException
     *             when non is ZERO and Currencies are different.
     */
    private static Money ensureZeroWithValuableCurrency(Money one, Money two) throws IllegalArgumentException {
        if (one.zero() && two.zero()) {
            return one;
        }
        if (one.getCurrency().equals(two.getCurrency())) {
            return one;
        }
        if (one.zero()) {
            return new Money(BigDecimal.ZERO, two.getCurrency());
        }
        if (two.zero()) {
            return one;
        }
        throw new IllegalArgumentException("Arguments one=[" + one + "] and two=[" + two + "] are illegal. Currency missmatch.");
    }

    /**
     * Local helper handle exception.
     *
     * @param value
     *            String to initialize {@link BigDecimal}
     * @return the Created {@link BigDecimal}
     */
    private static BigDecimal isBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException t) {
            return null;
        }
    }
}
