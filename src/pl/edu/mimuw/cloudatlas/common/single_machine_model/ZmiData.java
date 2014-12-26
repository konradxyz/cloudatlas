package pl.edu.mimuw.cloudatlas.common.single_machine_model;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

public class ZmiData<T> {
	private PathName path;

	private T attrs;

	public ZmiData(PathName path, T attrs) {
		super();
		this.path = path;
		this.attrs = attrs;
	}

	public PathName getPath() {
		return path;
	}

	public T getContent() {
		return attrs;
	}

}
