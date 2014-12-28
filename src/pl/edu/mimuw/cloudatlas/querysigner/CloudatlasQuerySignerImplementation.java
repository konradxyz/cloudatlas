package pl.edu.mimuw.cloudatlas.querysigner;

import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.common.interpreter.MainInterpreter;
import pl.edu.mimuw.cloudatlas.common.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.common.rmi.CloudatlasQuerySigner;
import pl.edu.mimuw.cloudatlas.common.rmi.InstallQueryResult;
import pl.edu.mimuw.cloudatlas.common.rmi.InstallQueryResult.Status;
import pl.edu.mimuw.cloudatlas.common.utils.SecurityUtils;

public class CloudatlasQuerySignerImplementation implements
		CloudatlasQuerySigner {
	private final List<ValueQuery> queries = new ArrayList<ValueQuery>();
	private Long nextUniqueId = 0l;
	private final Cipher signCipher;

	public CloudatlasQuerySignerImplementation(PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException {
		this.signCipher = Cipher
				.getInstance(SecurityUtils.ENCRYPTION_ALGORITHM);
		signCipher.init(Cipher.ENCRYPT_MODE, privateKey);
	}

	public List<ValueQuery> getQueries() {
		synchronized (queries) {
			return new ArrayList<ValueQuery>(queries);
		}
	}

	@Override
	public InstallQueryResult installQuery(String name, String query)
			throws RemoteException {
		synchronized (queries) {
			Program p = null;
			if (!ValueQuery.isValidQueryName(name)) {
				return new InstallQueryResult(Status.INVALID_QUERY, null);
			}
			try {
				p = MainInterpreter.parseProgram(query);
			} catch (Exception e) {
				return new InstallQueryResult(Status.INVALID_QUERY, null);
			}
			for (ValueQuery q : queries) {
				if (q.getName().equals(name)) {
					return new InstallQueryResult(Status.CONFLICT, q);
				}
			}
			byte[] descr = ValueQuery.toBytes(name, query, nextUniqueId);
			Long id = nextUniqueId;
			nextUniqueId++;
			byte[] hash = SecurityUtils.computeHash(descr);
			System.err.println(DatatypeConverter.printHexBinary(hash));
				try {
					byte[] signature = signCipher.doFinal(hash);
					ValueQuery newQuery = new ValueQuery(name, query, id, signature);
					queries.add(newQuery);
					return new InstallQueryResult(Status.OK, newQuery);
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					throw new RuntimeException(e);
					
				}
			
		}
	}

	@Override
	public ValueQuery uninstallQuery(ValueQuery query) throws RemoteException {
		synchronized (queries) {
			for (Iterator<ValueQuery> it = queries.iterator(); it.hasNext();) {
				ValueQuery q = it.next();
				if (q.getUniqueId().equals(query.getUniqueId())) {
					it.remove();
					byte[] descr = ValueQuery.toBytes(q.getName(), null,
							q.getUniqueId());
					byte[] hash = SecurityUtils.computeHash(descr);
					byte[] signature;
					try {
						signature = signCipher.doFinal(hash);
					} catch (IllegalBlockSizeException | BadPaddingException e) {
						throw new RuntimeException(e);
					}
					return new ValueQuery(query.getName(), null,
							q.getUniqueId(), signature);
				}

			}
			return null;
		}
	}

}
