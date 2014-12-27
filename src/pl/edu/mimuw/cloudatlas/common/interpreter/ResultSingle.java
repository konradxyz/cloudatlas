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

package pl.edu.mimuw.cloudatlas.common.interpreter;

import pl.edu.mimuw.cloudatlas.common.model.Type;
import pl.edu.mimuw.cloudatlas.common.model.Value;
import pl.edu.mimuw.cloudatlas.common.model.ValueList;

public class ResultSingle extends Result {
	private final Value value;

	public ResultSingle(Value value) {
		this.value = value;
	}

	@Override
	protected ResultSingle binaryOperationTyped(BinaryOperation operation,
			ResultSingle right) {
		return new ResultSingle(operation.perform(value, right.value));
	}

	@Override
	protected Result binaryOperationTyped(BinaryOperation operation,
			ResultColumn right) {
		return new ResultColumn(binaryOperationTyped(this, operation,
				right.getValues()));
	}

	@Override
	public Result binaryOperationTyped(BinaryOperation operation,
			ResultList right) {
		return new ResultList(binaryOperationTyped(this, operation,
				right.getValues()));

	}

	@Override
	public ResultSingle unaryOperation(UnaryOperation operation) {
		operation.getResultType(value.getType());
		return new ResultSingle(operation.perform(value));
	}

	@Override
	protected Result callMe(BinaryOperation operation, Result left) {
		return left.binaryOperationTyped(operation, this);
	}

	@Override
	public Value getValue() {
		return value;
	}

	@Override
	public Type getType() {
		return value.getType();
	}

	@Override
	public ValueList getValues() {
		throw new UnsupportedOperationException(
				"Cannot aggregate on OneResult.");
	}

}
