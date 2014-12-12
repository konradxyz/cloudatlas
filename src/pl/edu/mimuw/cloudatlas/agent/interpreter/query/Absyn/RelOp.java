package pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public abstract class RelOp implements java.io.Serializable {
  public abstract <R,A> R accept(RelOp.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpGtC p, A arg);
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpEqC p, A arg);
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpNeC p, A arg);
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpLtC p, A arg);
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpLeC p, A arg);
    public R visit(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.RelOpGeC p, A arg);

  }

}