package org.pm4j.core.pm.impl.expr;

import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.expr.parser.ParseCtxt;

public class PmVariableExpr
    extends ExprBase<PmExprExecCtxt>
    implements OptionalExpression {

  private final NameWithModifier nameWithModifier;

  public PmVariableExpr(NameWithModifier nameWithModifier) {
    this.nameWithModifier = nameWithModifier.clone();
    // Ensure that the flag is set, to provide explicit modifiers for
    // debugging and logging.
    this.nameWithModifier.setVariable(true);
  }

  @Override
  protected Object execImpl(PmExprExecCtxt ctxt) {
    PmObject pm = ctxt.getPm();
    if (pm == null) {
      throw new ExprExecExeption(ctxt, "PM in expression context is 'null'.");
    }

    return PmExpressionApi.findNamedObject(pm, nameWithModifier.getName());
  }

  @Override
  public boolean isOptional() {
    return nameWithModifier.isOptional();
  }

  @Override
  public String toString() {
    return nameWithModifier.toString();
  }

  public static OptionalExpression parse(ParseCtxt ctxt) {
    NameWithModifier n = NameWithModifier.parseNameAndModifier(ctxt);
    return (n != null)
              ? new PmVariableExpr(n)
              : ThisExpr.INSTANCE;
  }

}
