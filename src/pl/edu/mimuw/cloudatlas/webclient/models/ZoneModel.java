package pl.edu.mimuw.cloudatlas.webclient.models;

import java.util.List;

public class ZoneModel {
	public String name;
	public List<String> attrs;
	public ZoneModel(String name, List<String> attrs) {
		super();
		this.name = name;
		this.attrs = attrs;
	}

}
