package stanhebben.zenscript.expression.partial;

import stanhebben.zenscript.compiler.*;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.symbols.*;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeNative;
import stanhebben.zenscript.util.ZenPosition;

/**
 * @author Stan
 */
public class PartialType implements IPartialExpression {

    private final ZenPosition position;
    private final ZenType type;

    public PartialType(ZenPosition position, ZenType type) {
        this.position = position;
        this.type = type;
    }

    @Override
    public Expression eval(IEnvironmentGlobal environment) {
        environment.error(position, "cannot use type as expression");
        return new ExpressionInvalid(position, type);
    }

    @Override
    public Expression assign(ZenPosition position, IEnvironmentGlobal environment, Expression other) {
        environment.error(position, "cannot assign to a type");
        return new ExpressionInvalid(position, type);
    }

    @Override
    public IPartialExpression getMember(ZenPosition position, IEnvironmentGlobal environment, String name) {
        return type.getStaticMember(position, environment, name);
    }

    @Override
    public Expression call(ZenPosition position, IEnvironmentMethod environment, Expression... values) {
        if(type instanceof ZenTypeNative)
            return type.call(position, environment, null, values);
        environment.error(position, "cannot call a type");
        return new ExpressionInvalid(position, type);
    }

    @Override
    public IZenSymbol toSymbol() {
        return new SymbolType(type);
    }

    @Override
    public ZenType getType() {
        return null; // not an expression
    }

    @Override
    public ZenType[] predictCallTypes(int numArguments) {
        return new ZenType[numArguments];
    }

    @Override
    public ZenType toType(IEnvironmentGlobal environment) {
        return type;
    }
}
