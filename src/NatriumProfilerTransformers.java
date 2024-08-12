import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import b100.asmloader.ClassTransformer;
import b100.natrium.utils.ASMHelper;
import b100.natrium.utils.FindInstruction;

public class NatriumProfilerTransformers {
	
	private static final String LISTENER_CLASS = "NatriumProfiler";
	
	class MinecraftTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/Minecraft");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode runMethod = ASMHelper.findMethod(classNode, "run");
			AbstractInsnNode updateMethodCall = ASMHelper.findInstruction(runMethod, false, (n) -> {
				return FindInstruction.methodInsn(n, "net/minecraft/client/Minecraft", "J", "()V");
			});
			runMethod.instructions.insertBefore(updateMethodCall, new MethodInsnNode(Opcodes.INVOKESTATIC, LISTENER_CLASS, "beforeUpdate", "()V"));
			runMethod.instructions.insert(updateMethodCall, new MethodInsnNode(Opcodes.INVOKESTATIC, LISTENER_CLASS, "afterUpdate", "()V"));
			
			MethodNode updateMethod = ASMHelper.findMethod(classNode, "J");
			AbstractInsnNode tickMethodCall = ASMHelper.findInstruction(updateMethod, false, (n) -> {
				return FindInstruction.methodInsn(n, "net/minecraft/client/Minecraft", "l", "()V");
			});
			runMethod.instructions.insertBefore(tickMethodCall, new MethodInsnNode(Opcodes.INVOKESTATIC, LISTENER_CLASS, "beforeTick", "()V"));
			runMethod.instructions.insert(tickMethodCall, new MethodInsnNode(Opcodes.INVOKESTATIC, LISTENER_CLASS, "afterTick", "()V"));
		}
	}
}
