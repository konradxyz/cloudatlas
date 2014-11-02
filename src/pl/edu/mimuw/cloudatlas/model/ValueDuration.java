/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.model;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.mimuw.cloudatlas.model.Type.PrimaryType;

/**
 * A class representing duration in milliseconds. The duration can be negative.
 * This is a simple wrapper of a Java <code>Long</code> object.
 */
public class ValueDuration extends ValueSimple<Long> {
	/**
	 * Constructs a new <code>ValueDuration</code> object wrapping the specified
	 * <code>value</code>.
	 * 
	 * @param value
	 *            the value to wrap
	 */
	public ValueDuration(Long value) {
		super(value);
	}
	
	private static class StringHolder {
		String string;
		public StringHolder(String string) {
			this.string = string;
		}

		public void setString(String string) {
			this.string = string;
		}
		
		public String getString() {
			return string;
		}
	}

	@Override
	public Type getType() {
		return TypePrimitive.DURATION;
	}

	@Override
	public Value getDefaultValue() {
		return new ValueDuration(0L);
	}

	/**
	 * Constructs a new <code>ValueDuration</code> object from the specified
	 * amounts of different time units.
	 * 
	 * @param seconds
	 *            a number of full seconds
	 * @param milliseconds
	 *            a number of milliseconds (an absolute value does not have to
	 *            be lower than 1000)
	 */
	public ValueDuration(long seconds, long milliseconds) {
		this(seconds * 1000l + milliseconds);
	}

	/**
	 * Constructs a new <code>ValueDuration</code> object from the specified
	 * amounts of different time units.
	 * 
	 * @param minutes
	 *            a number of full minutes
	 * @param seconds
	 *            a number of full seconds (an absolute value does not have to
	 *            be lower than 60)
	 * @param milliseconds
	 *            a number of milliseconds (an absolute value does not have to
	 *            be lower than 1000)
	 */
	public ValueDuration(long minutes, long seconds, long milliseconds) {
		this(minutes * 60l + seconds, milliseconds);
	}

	/**
	 * Constructs a new <code>ValueDuration</code> object from the specified
	 * amounts of different time units.
	 * 
	 * @param hours
	 *            a number of full hours
	 * @param minutes
	 *            a number of full minutes (an absolute value does not have to
	 *            be lower than 60)
	 * @param seconds
	 *            a number of full seconds (an absolute value does not have to
	 *            be lower than 60)
	 * @param milliseconds
	 *            a number of milliseconds (an absolute value does not have to
	 *            be lower than 1000)
	 */
	public ValueDuration(long hours, long minutes, long seconds,
			long milliseconds) {
		this(hours * 60l + minutes, seconds, milliseconds);
	}

	/**
	 * Constructs a new <code>ValueDuration</code> object from the specified
	 * amounts of different time units.
	 * 
	 * @param days
	 *            a number of full days
	 * @param hours
	 *            a number of full hours (an absolute value does not have to be
	 *            lower than 24)
	 * @param minutes
	 *            a number of full minutes (an absolute value does not have to
	 *            be lower than 60)
	 * @param seconds
	 *            a number of full seconds (an absolute value does not have to
	 *            be lower than 60)
	 * @param milliseconds
	 *            a number of milliseconds (an absolute value does not have to
	 *            be lower than 1000)
	 */
	public ValueDuration(long days, long hours, long minutes, long seconds,
			long milliseconds) {
		this(days * 24l + hours, minutes, seconds, milliseconds);
	}

	/**
	 * Constructs a new <code>ValueDuration</code> object from its textual
	 * representation. The representation has format:
	 * <code>sd hh:mm:ss.lll</code> where:
	 * <ul>
	 * <li><code>s</code> is a sign (<code>+</code> or <code>-</code>),</li>
	 * <li><code>d</code> is a number of days,</li>
	 * <li><code>hh</code> is a number of hours (between <code>00</code> and
	 * <code>23</code>),</li>
	 * <li><code>mm</code> is a number of minutes (between <code>00</code> and
	 * <code>59</code>),</li>
	 * <li><code>ss</code> is a number of seconds (between <code>00</code> and
	 * <code>59</code>),</li>
	 * <li><code>lll</code> is a number of milliseconds (between
	 * <code>000</code> and <code>999</code>).</li>
	 * </ul>
	 * <p>
	 * All fields are obligatory.
	 * 
	 * @param value
	 *            a textual representation of a duration
	 * @throws IllegalArgumentException
	 *             if <code>value</code> does not meet described rules
	 */
	public ValueDuration(String value) {
		this(parseDuration(value));
	}

	private static long getNumber(StringHolder stringHolder, Integer length) {
		String value = stringHolder.getString();
		Matcher matcher = Pattern.compile("\\d+").matcher(value);
		matcher.find();
		if (!value.startsWith(matcher.group())) {
			throw new UnsupportedOperationException("Cannot parse this string");
		}
		stringHolder.setString(value.substring(matcher.group().length()));
		long result = Integer.valueOf(matcher.group());
		if (length != null && matcher.group().length()!=length) {
			throw new UnsupportedOperationException("Cannot parse this string");
		}
		return result;
	}
	
	private static Long parseDuration(String value) {
		try {
		long millis = 0;
		String rest;
		if (value.equals("+0"))
			return 0l;
		if (value.startsWith("+") || value.startsWith("-")) {
			rest = value.substring(1);
			StringHolder stringHolder = new StringHolder(rest);
			long days = getNumber(stringHolder, null);
			millis += TimeUnit.DAYS.toMillis(days);
			rest = stringHolder.getString();
			if (rest.startsWith(" ")) {
				int i = 0;
				long time;
				while (i < 4) {
					if (i > 0 && i < 3 && !(rest.startsWith(":")))
						throw new UnsupportedOperationException("Cannot parse this string");
					if (i == 3 && !(rest.startsWith(".")))
						throw new UnsupportedOperationException("Cannot parse this string");
					rest = rest.substring(1);
					stringHolder.setString(rest);
					if (i < 3) 
						time = getNumber(stringHolder, 2);
					else
						time = getNumber(stringHolder, 3);
					if (i == 0) 
						millis += TimeUnit.HOURS.toMillis(time);
					if (i == 1) 
						millis += TimeUnit.MINUTES.toMillis(time);
					if (i == 2) 
						millis += TimeUnit.SECONDS.toMillis(time);
					if (i == 3) 
						millis += time;
					rest = stringHolder.getString();
					i++;
				}
				
			}
			else {
				throw new UnsupportedOperationException("Cannot parse this string");
			}
		}
		else {
			throw new UnsupportedOperationException("Cannot parse this string");
		}
		if (!rest.equals(""))
			throw new UnsupportedOperationException("Cannot parse this string");
		if (value.startsWith("+")) 
			return millis;
		else 
			return -millis;
		}
		catch (Exception e){
			return null;
		}
	}
	
	private static String formatPositiveDuration(Long value) {
		assert(value > 0);
		long change;
		long days = TimeUnit.MILLISECONDS.toDays(value);
		change = value-TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(change);
		change = change - TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(change);
		change = change - TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(change);
		change = change - TimeUnit.SECONDS.toMillis(seconds);
		long millis = change;
		String result = String.format("%d %02d:%02d:%02d.%03d", days, hours,
				minutes,seconds,millis);
		return result;
	}

	@Override
	public ValueBoolean isLowerThan(Value value) {
		if(isNull() || value.isNull())
			return new ValueBoolean(null);
		if (value.getType().getPrimaryType() == PrimaryType.DURATION) {
			if (getValue() < ((ValueDuration) value).getValue())
				return new ValueBoolean(true);
			else 
				return new ValueBoolean(false);
		} else {
			throw new UnsupportedOperationException(
					"Wrong type of value in isLowerThan");
		}
	}

	@Override
	public ValueDuration addValue(Value value) {
		sameTypesOrThrow(value, Operation.ADD);
		if (isNull() || value.isNull())
			return new ValueDuration((Long) null);
		return new ValueDuration(getValue() + ((ValueDuration) value).getValue());
	}

	@Override
	public ValueDuration subtract(Value value) {
		sameTypesOrThrow(value, Operation.ADD);
		if (isNull() || value.isNull())
			return new ValueDuration((Long) null);
		return new ValueDuration(getValue() - ((ValueDuration) value).getValue());
	}

	@Override
	public ValueDuration multiply(Value value) {
		if (value.getType().getPrimaryType() == PrimaryType.INT) {
			if (value.isNull() || isNull())
				return new ValueDuration((Long) null);
			return new ValueDuration((Long) getValue()
					* ((ValueInt) value).getValue());
		} else {
			throw new UnsupportedOperationException(
					"Wrong type of value in multiply duration");
		}
	}

	@Override
	public Value divide(Value value) {
		if (value.getType().getPrimaryType() == PrimaryType.INT) {
			if (value.isNull())
				return new ValueDuration((Long) null);
			if (((ValueInt) value).getValue() == 0l)
				throw new ArithmeticException("Division by zero.");
			if (isNull())
				return new ValueDuration((Long) null);
			return new ValueDuration((Long) getValue()
					/ ((ValueInt) value).getValue());

		} else {
			throw new UnsupportedOperationException(
					"Wrong type of value in divide duration");
		}
	}

	@Override
	public ValueDuration modulo(Value value) {
		throw new UnsupportedOperationException("No modulo in duration");
	}

	@Override
	public ValueDuration negate() {
		return new ValueDuration(getValue() == null ? null : -getValue());
	}

	public Value convertTo(Type type) {
		switch (type.getPrimaryType()) {
		case DOUBLE:
			return this;
		case INT:
			return new ValueInt(getValue() == null ? null : getValue()
					.longValue());
		case STRING:
			if ( getValue() == null ) {
				return ValueString.NULL_STRING;
			}
			if ( getValue() == 0 ) {
				return new ValueString("+0");
			}
			if ( getValue() > 0 ) {
				return new ValueString("+" + formatPositiveDuration(getValue()));
			}
			if ( getValue() < 0 ) {
				return new ValueString("-" + formatPositiveDuration(-getValue()));
			}
		default:
			throw new UnsupportedConversionException(getType(), type);
		}
	}
}
