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

package pl.edu.mimuw.cloudatlas.interpreter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import pl.edu.mimuw.cloudatlas.interpreter.Result.AggregationOperation;
import pl.edu.mimuw.cloudatlas.interpreter.Result.TransformOperation;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.Type.PrimaryType;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueTime;

public class Functions {
	private static Functions instance = null;

	public static final AggregationOperation COUNT = new AggregationOperation() {
		@Override
		public ValueInt perform(ValueList values) {
			if(values.getValue() == null) {
				return new ValueInt(null);
			}
			return new ValueInt((long)values.size());
		}
	};

	public static final AggregationOperation SUM = new AggregationOperation() {
		@Override
		public Value perform(ValueList values) {
			if ( values.getElementType().isCollection()){
				throw new IllegalArgumentException("SUM doesn't support type: " + values.getElementType().toString() + ".");
			}
			TypePrimitive type = (TypePrimitive) values.getElementType();
			if ( type.equals(TypePrimitive.NULL))
				return ValueNull.getInstance();
			if (!type.equals(TypePrimitive.DURATION)
					&& !type.equals(TypePrimitive.INTEGER)
					&& !type.equals(TypePrimitive.DOUBLE))
				throw new IllegalArgumentException(
						"SUM doesn't support type: "
								+ values.getElementType().toString() + ".");
			Value typedNull = type.getNull();
			if ( values.size() == 0 )
				return typedNull;
			Value result = typedNull.getDefaultValue();
			for ( Value v : values )
				result = result.addValue(v);
			return result;
		}
	};

	public static final AggregationOperation AVERAGE = new AggregationOperation() {
		@Override
		public Value perform(ValueList values) {
			if (values.getElementType().isCollection()) {
				throw new IllegalArgumentException(
						"AVERAGE doesn't support type: "
								+ values.getElementType() + ".");
			}
			Type elementType = values.getElementType();

			PrimaryType primaryType = elementType.getPrimaryType();

			if (primaryType != PrimaryType.INT
					&& primaryType != PrimaryType.DOUBLE
					&& primaryType != PrimaryType.DURATION
					&& primaryType != PrimaryType.NULL) {
				throw new IllegalArgumentException(
						"AVERAGE doesn't support type: " + elementType + ".");
			}
			if (primaryType == PrimaryType.NULL)
				return ValueNull.getInstance();

			ValueList nlist = values;
			TypePrimitive returnType = elementType.equals(TypePrimitive.DURATION) ? TypePrimitive.DURATION : TypePrimitive.DOUBLE;
			if(nlist.isEmpty()) {
				return returnType.getNull();
			}

			Value result = nlist.get(0).getDefaultValue();

			for(Value v : nlist) {
				result = result.addValue(v);
			}
			if (elementType.equals(TypePrimitive.INTEGER)) {
				result = result.convertTo(TypePrimitive.DOUBLE);
			}
			Value size = new ValueDouble((double)nlist.size());
			if (elementType.equals(TypePrimitive.DURATION)) {
				size = new ValueInt((long)nlist.size());
			}
			return result.divide(size);
		}
	};

	public static final AggregationOperation AND = new AggregationOperation() {
		@Override
		public ValueBoolean perform(ValueList values) { // lazy
			if (!values.getElementType().isCompatible(TypePrimitive.BOOLEAN)) {
				throw new IllegalArgumentException("AND doesn't support type: "
						+ values.getElementType() + ".");
			}
			if (values.getValue() == null) {
				return new ValueBoolean(null);
			} else if (values.isEmpty()) {
				return new ValueBoolean(null);
			}
			for (Value v : values) {
				if (!((ValueBoolean) v).getValue())
					return new ValueBoolean(false);
			}
			return new ValueBoolean(true);
		}
	};

	public static final AggregationOperation OR = new AggregationOperation() {
		@Override
		public ValueBoolean perform(ValueList values) { // lazy
			if (!values.getElementType().isCompatible(TypePrimitive.BOOLEAN)) {
				throw new IllegalArgumentException("OR doesn't support type: "
						+ values.getElementType() + ".");
			}
			if (values.getValue() == null) {
				return new ValueBoolean(null);
			} else if (values.isEmpty()) {
				return new ValueBoolean(null);
			}

			if (values.getValue() == null) {
				return new ValueBoolean(null);
			} else if (values.isEmpty()) {
				return new ValueBoolean(null);
			}
			for (Value v : values) {

				if (((ValueBoolean) v).getValue())
					return new ValueBoolean(true);

			}
			return new ValueBoolean(false);
		}
	};

	private static final AggregationOperation MIN = new AggregationOperation() {
		@Override
		public Value perform(ValueList values) {
			// Make sure that objects in our list are comparable
			BinaryOperation.IS_LOWER_THAN.getResultType(values.getElementType(), values.getElementType());
			TypePrimitive type = (TypePrimitive) values.getElementType();
			if(values.getValue() == null || values.isEmpty()) {
				return type.getNull();
			}
			Value result = values.get(0);
			for(Value v : values) {
				if(((ValueBoolean)v.isLowerThan(result)).getValue()) {
					result = v;
				}
			}
			return result;
		}
	};

	private static final AggregationOperation MAX = new AggregationOperation() {
		@Override
		public Value perform(ValueList values) {
			BinaryOperation.IS_LOWER_THAN.getResultType(values.getElementType(), values.getElementType());
			TypePrimitive type = (TypePrimitive) values.getElementType();
			if(values.getValue() == null || values.isEmpty()) {
				return type.getNull();
			}
			Value result = values.get(0);
			for(Value v : values) {
				if(((ValueBoolean)v.isLowerThan(result)).negate().and(v.isEqual(result).negate()).getValue()) {
					result = v;
				}
			}
			return result;
		}
	};

	private static final TransformOperation UNFOLD = new TransformOperation() {
		@Override
		public ValueList perform(ValueList values) {
			if(!((TypeCollection)values.getType()).getElementType().isCollection()) {
				throw new IllegalArgumentException("All elements must have a collection compatible type.");
			}
			ValueList nlist = Result.filterNullsList(values);
			if(nlist.getValue() == null) {
				return new ValueList(null,
						((TypeCollection)((TypeCollection)values.getType()).getElementType()).getElementType());
			} else if(nlist.isEmpty()) {
				return new ValueList(
						((TypeCollection)((TypeCollection)values.getType()).getElementType()).getElementType());
			}
			List<Value> ret = new ArrayList<Value>();
			for(Value v : nlist) {
				if(v.getType().getPrimaryType() == Type.PrimaryType.SET) {
					ret.addAll((ValueSet)v);
				} else if(v.getType().getPrimaryType() == Type.PrimaryType.LIST) {
					ret.addAll((ValueList)v);
				}
			}
			return new ValueList(ret,
					((TypeCollection)((TypeCollection)values.getType()).getElementType()).getElementType());
		}
	};

	private static final TransformOperation DISTINCT = new TransformOperation() {
		@Override
		public ValueList perform(ValueList values) {
			if(values.isEmpty())
				return new ValueList(((TypeCollection)values.getType()).getElementType());
			List<Value> ret = new ArrayList<Value>();
			for(Value v : values) {
				if(!ret.contains(v)) {
					ret.add(v);
				}
			}
			return new ValueList(ret, ((TypeCollection)values.getType()).getElementType());
		}
	};
	
	private static abstract class ListAggregation implements AggregationOperation {
		protected int count;
		
		public ListAggregation(Value count) {
			if (count.getType().getPrimaryType() != PrimaryType.INT) {
				throw new InternalInterpreterException(
						"First argument to first, last and random functions should be INT");
			}
			this.count = ((ValueInt) count).getValue().intValue();
			if (this.count < 0) {
				throw new InternalInterpreterException(
						"First argument to first, last and random functions should be greater or equal 0");
			}
		}
		
	}
	
	private static class FirstAggregation extends ListAggregation {
		public FirstAggregation(Value count) {
			super(count);
		}

		@Override
		public Value perform(ValueList values) {
			List<Value> res = values.subList(0, Math.min(count, values.size()));
			return new ValueList(res, TypeCollection.computeElementType(res));
		}
	}
	
	
	private static class LastAggregation extends ListAggregation {
		public LastAggregation(Value count) {
			super(count);
		}

		@Override
		public Value perform(ValueList values) {
			List<Value> res = values.subList(Math.max(0, values.size() - count), values.size());
			return new ValueList(res, TypeCollection.computeElementType(res));
		}
	}

	

	private static class RandomAggregation extends ListAggregation {
		public RandomAggregation(Value count) {
			super(count);
		}

		@Override
		public Value perform(ValueList values) {
			List<Value> tmp = new ArrayList<Value>(values);
			Collections.shuffle(tmp);
			List<Value> res = tmp.subList(0, Math.min(count, tmp.size()));
			return new ValueList(res, TypeCollection.computeElementType(res));
		}
	}

	
	private final ValueTime EPOCH;

	private Functions() {
		try {
			EPOCH = new ValueTime("2000/01/01 00:00:00.000");
		} catch(ParseException exception) {
			throw new InternalInterpreterException("Cannot parse time when creating an EPOCH object.\n"
					+ exception.getMessage());
		}
	}

	public static Functions getInstance() {
		if(instance == null)
			instance = new Functions();
		return instance;
	}

	public Result evaluate(String name, List<Result> arguments) {
		switch(name) {
			case "round":
				if(arguments.size() == 1)
					return arguments.get(0).unaryOperation(UnaryOperation.ROUND);
				break;
			case "floor":
				if(arguments.size() == 1)
					return arguments.get(0).unaryOperation(UnaryOperation.FLOOR);
				break;
			case "ceil":
				if(arguments.size() == 1)
					return arguments.get(0).unaryOperation(UnaryOperation.CEIL);
				break;
			case "now":
				if(arguments.size() == 0)
					return new ResultSingle(new ValueTime(Calendar.getInstance().getTimeInMillis()));
				break;
			case "epoch":
				if(arguments.size() == 0)
					return new ResultSingle(EPOCH);
				break;
			case "count":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(COUNT);
				break;
			case "size":
				if(arguments.size() == 1)
					return arguments.get(0).unaryOperation(UnaryOperation.VALUE_SIZE);
				break;
			case "sum":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(SUM);
				break;
			case "avg":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(AVERAGE);
				break;
			case "land":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(AND);
				break;
			case "lor":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(OR);
				break;
			case "min":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(MIN);
				break;
			case "max":
				if(arguments.size() == 1)
					return arguments.get(0).aggregationOperation(MAX);
				break;
			case "unfold":
				if(arguments.size() == 1)
					return arguments.get(0).transformOperation(UNFOLD);
				break;
			case "distinct":
				if(arguments.size() == 1)
					return arguments.get(0).transformOperation(DISTINCT);
				break;
			case "first":
				if(arguments.size() == 2) {
					return arguments.get(1).aggregationOperation(new FirstAggregation(arguments.get(0).getValue()));
				}
				break;
			case "last":
				if(arguments.size() == 2) {
					return arguments.get(1).aggregationOperation(new LastAggregation(arguments.get(0).getValue()));
				}
				break;
			case "random":
				if(arguments.size() == 2) {
					return arguments.get(1).aggregationOperation(new RandomAggregation(arguments.get(0).getValue()));
				}
				break;
			case "to_boolean":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.BOOLEAN);
				break;
			case "to_contact":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.CONTACT);
				break;
			case "to_double":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.DOUBLE);
				break;
			case "to_duration":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.DURATION);
				break;
			case "to_integer":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.INTEGER);
				break;
			case "to_string":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.STRING);
				break;
			case "to_time":
				if(arguments.size() == 1)
					return arguments.get(0).convertTo(TypePrimitive.TIME);
				break;
			case "to_set":
				if(arguments.size() == 1) {
					Type t = arguments.get(0).getType();
					if(t.isCollection()) {
						Type elementType = ((TypeCollection)t).getElementType();
						return arguments.get(0).convertTo(new TypeCollection(Type.PrimaryType.SET, elementType));
					}
					throw new IllegalArgumentException("First argument must be a collection.");
				}
				break;
			case "to_list":
				if(arguments.size() == 1) {
					Type t = arguments.get(0).getType();
					if(t.isCollection()) {
						Type elementType = ((TypeCollection)t).getElementType();
						return arguments.get(0).convertTo(new TypeCollection(Type.PrimaryType.LIST, elementType));
					}
					throw new IllegalArgumentException("First argument must be a collection.");
				}
				break;
			case "isNull":
				if(arguments.size() == 1)
					return arguments.get(0).unaryOperation(UnaryOperation.IS_NULL);
				break;
			default:
				throw new IllegalArgumentException("Illegal function name.");
		}
		throw new IllegalArgumentException("Illegal number of arguments.");
	}
}
