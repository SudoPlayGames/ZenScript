package stanhebben.zenscript.expression;

import org.objectweb.asm.*;
import stanhebben.zenscript.compiler.*;
import stanhebben.zenscript.definitions.ParsedFunctionArgument;
import stanhebben.zenscript.statements.Statement;
import stanhebben.zenscript.symbols.SymbolArgument;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.util.*;

import java.lang.reflect.Method;
import java.util.List;

import static stanhebben.zenscript.util.ZenTypeUtil.*;

/**
 * @author Stanneke
 */
public class ExpressionJavaLambda extends Expression {
    
    private final Class interfaceClass;
    private final List<ParsedFunctionArgument> arguments;
    private final List<Statement> statements;
    
    private final ZenType type;
    
    public ExpressionJavaLambda(ZenPosition position, Class interfaceClass, List<ParsedFunctionArgument> arguments, List<Statement> statements, ZenType type) {
        super(position);
        
        this.interfaceClass = interfaceClass;
        this.arguments = arguments;
        this.statements = statements;
        
        this.type = type;
    }
    
    @Override
    public ZenType getType() {
        return type;
    }
    
    @Override
    public void compile(boolean result, IEnvironmentMethod environment) {
        if(!result)
            return;
        
        Method method = interfaceClass.getMethods()[0];
        
        // generate class
        String clsName = environment.makeClassNameWithMiddleName(getPosition().getFile().getClassName());
        
        ClassWriter cw = new ZenClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, clsName, null, "java/lang/Object", new String[]{internal(interfaceClass)});
        
        MethodOutput constructor = new MethodOutput(cw, Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.start();
        constructor.loadObject(0);
        constructor.invokeSpecial("java/lang/Object", "<init>", "()V");
        constructor.ret();
        constructor.end();
        
        MethodOutput output = new MethodOutput(cw, Opcodes.ACC_PUBLIC, method.getName(), descriptor(method), null, null);
        
        IEnvironmentClass environmentClass = new EnvironmentClass(cw, environment);
        IEnvironmentMethod environmentMethod = new EnvironmentMethod(output, environmentClass);
        
        for(int i = 0, j = 0; i < arguments.size(); i++) {
            environmentMethod.putValue(arguments.get(i).getName(), new SymbolArgument(i + 1, environment.getType(method.getGenericParameterTypes()[i])), getPosition());
            if(environment.getType(method.getGenericParameterTypes()[i]).isLarge())
                j++;
        }
        
        output.start();
        for(Statement statement : statements) {
            statement.compile(environmentMethod);
        }
        output.ret();
        output.end();
        
        environment.putClass(clsName, cw.toByteArray());
        
        // make class instance
        environment.getOutput().newObject(clsName);
        environment.getOutput().dup();
        environment.getOutput().construct(clsName);
    }
}
