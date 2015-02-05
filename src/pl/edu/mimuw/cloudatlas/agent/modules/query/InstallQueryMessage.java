package pl.edu.mimuw.cloudatlas.agent.modules.query;

import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.cloudatlas.agent.modules.framework.SimpleMessage;
import pl.edu.mimuw.cloudatlas.common.Certificate;

public class InstallQueryMessage extends SimpleMessage<List<Certificate>> {
	boolean needsValidation = true;

	public InstallQueryMessage(Certificate queryCertificate) {
		super(Arrays.asList(queryCertificate));
	}

	public InstallQueryMessage(List<Certificate> queryCertificate) {
		super(queryCertificate);
	}

	public boolean needsValidation() {
		return needsValidation;
	}
}
