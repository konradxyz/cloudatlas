package pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class NoNullsC extends Nulls {

  public NoNullsC() { }

  public <R,A> R accept(pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.Nulls.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof pl.edu.mimuw.cloudatlas.agent.interpreter.query.Absyn.NoNullsC) {
      return true;
    }
    return false;
  }

  public int hashCode() {
    return 37;
  }


}
