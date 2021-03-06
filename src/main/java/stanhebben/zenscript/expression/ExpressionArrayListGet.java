package stanhebben.zenscript.expression;

import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.compiler.IEnvironmentMethod;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeArrayList;
import stanhebben.zenscript.util.ZenPosition;
import stanhebben.zenscript.util.ZenTypeUtil;

import java.util.List;

public class ExpressionArrayListGet extends Expression {

    private final Expression array;
    private final Expression index;
    private final ZenType type;
    private final ZenPosition position;

    public ExpressionArrayListGet(ZenPosition position, Expression array, Expression index) {
        super(position);
        this.array = array;
        this.index = index;
        this.type = ((ZenTypeArrayList) array.getType()).getBaseType();
        this.position = position;
    }

    @Override
    public ZenType getType() {
        return type;
    }

    @Override
    public void compile(boolean result, IEnvironmentMethod environment) {
        if (result) {
            array.compile(result, environment);
            index.compile(result, environment);
            environment.getOutput().invokeInterface(List.class, "get", Object.class, int.class);


            environment.getOutput().checkCast(ZenTypeUtil.checkPrimitive(type).toASMType().getInternalName());
            if (ZenTypeUtil.isPrimitive(type)) {
                ZenTypeUtil.checkPrimitive(type).getCastingRule(type, environment).compile(environment);
            }
        }

    }

    @Override
    public Expression assign(ZenPosition position, IEnvironmentGlobal environment, Expression other) {
        return new ExpressionArrayListSet(position, array, index, other);
    }

}
