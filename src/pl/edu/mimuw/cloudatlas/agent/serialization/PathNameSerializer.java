package pl.edu.mimuw.cloudatlas.agent.serialization;

import pl.edu.mimuw.cloudatlas.common.model.PathName;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class PathNameSerializer extends Serializer<PathName> {

	@Override
	public PathName read(Kryo arg0, Input arg1, Class<PathName> arg2) {
		String str = arg0.readObject(arg1, String.class);
		return new PathName(str);
	}

	@Override
	public void write(Kryo arg0, Output arg1, PathName arg2) {
		arg0.writeObject(arg1, arg2.getName());
		
	}

}
