package pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public abstract class Statement implements java.io.Serializable {
  public abstract <R,A> R accept(Statement.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.StatementC p, A arg);

  }

}