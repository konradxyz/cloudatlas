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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypeCollection;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueNull;

abstract class Result {

	public interface AggregationOperation {
		public Value perform(ValueList values);
	}

	public interface TransformOperation {
		public ValueList perform(ValueList values);
	}

	protected abstract Result binaryOperationTyped(BinaryOperation operation, ResultSingle right);
	protected abstract Result binaryOperationTyped(BinaryOperation operation, ResultColumn right);
	protected abstract Result binaryOperationTyped(BinaryOperation operation,
			ResultList right);
	public Result binaryOperation(BinaryOperation operation, Result right) {
		return right.callMe(operation, this);
	}

	public abstract Result unaryOperation(UnaryOperation operation);

	protected abstract Result callMe(BinaryOperation operation, Result left);

	public abstract Value getValue();
	public abstract ValueList getValues();

	public Result aggregationOperation(AggregationOperation operation) {
		return new ResultSingle(operation.perform(filterNullsList(getValues())));
	}


	public Result transformOperation(TransformOperation operation) {
		return new ResultList(operation.perform(getValues()));
	}

	protected static ValueList filterNullsList(ValueList list) {
		List<Value> result = new ArrayList<Value>();
		if(list.isEmpty())
			return new ValueList(result, ((TypeCollection)list.getType()).getElementType());
		for(Value v : list)
			if(!v.isNull())
				result.add(v);
		return new ValueList(result.isEmpty()? null : result, ((TypeCollection)list.getType()).getElementType());
	}


	protected static ValueList binaryOperationTypedValueList(ValueList left, BinaryOperation operation,
			ResultSingle right) {
		Type resultType = operation.getResultType(left.getElementType(), right.getValue().getType());
		List<Value> result = new ArrayList<Value>();
		for ( Value v : left ) {
			result.add(operation.perform(v, right.getValue()));
		}
		return new ValueList(result, resultType);
	}
	
	protected static ValueList binaryOperationTyped(ResultSingle left, BinaryOperation operation,
			ValueList right) {
		Type resultType = operation.getResultType(left.getValue().getType(), right.getElementType());
		List<Value> result = new ArrayList<Value>();
		for (Value v : right) {
			result.add(operation.perform(left.getValue(), v));
		}
		return new ValueList(result, resultType);
	}
	
	public static ValueList unaryOperation(ValueList list, UnaryOperation operation) {
		Type type = operation.getResultType(list.getElementType());
		List<Value> result = new ArrayList<Value>();
		for ( Value v : list ) {
			result.add(operation.perform(v));
		}
		return new ValueList(result, type);
	}
	
	public static ValueList convertTo(ValueList list, Type to) {
		List<Value> result = new ArrayList<Value>();
		for ( Value v : list ) {
			result.add(v.convertTo(to));
		}
		return new ValueList(result, to);
	}

	public  Result convertTo(Type to) {
		return this.unaryOperation(new UnaryOperation.ConversionOperation(to));
	}

	public abstract Type getType();


}
