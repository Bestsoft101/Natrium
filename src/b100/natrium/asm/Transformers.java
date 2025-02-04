package b100.natrium.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import b100.asmloader.ClassTransformer;
import b100.natrium.asm.utils.ASMHelper;
import b100.natrium.asm.utils.FindInstruction;
import b100.natrium.asm.utils.InjectHelper;

public class Transformers {
	
	private static String listenerClass = "b100/natrium/asm/Listeners";
	private static InjectHelper injectHelper = new InjectHelper(listenerClass, "b100/natrium/asm/utils/CallbackInfo");
	private static final String GL11 = "org/lwjgl/opengl/GL11";
	
	class MinecraftClientTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/Minecraft");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode startGame = ASMHelper.findMethod(classNode, "startGame", null);
			TypeInsnNode newNode = (TypeInsnNode) ASMHelper.findInstruction(startGame, false, n -> FindInstruction.opcode(n, Opcodes.NEW) && FindInstruction.typeInsn(n, "net/minecraft/client/render/terrain/TerrainRendererLegacy"));
			replaceClassInstantiation(startGame.instructions, newNode, "b100/natrium/TerrainRendererMultiDrawNatrium");
		}
	}
	
	class TessellatorTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/tessellator/Tessellator");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode staticInit = ASMHelper.findMethod(classNode, "<clinit>", null);
			TypeInsnNode newNode = (TypeInsnNode) ASMHelper.findInstruction(staticInit, false, n -> FindInstruction.opcode(n, Opcodes.NEW) && FindInstruction.typeInsn(n, "net/minecraft/client/render/tessellator/TessellatorStandard"));
			replaceClassInstantiation(staticInit.instructions, newNode, "b100/natrium/TessellatorNatrium");
		}
	}
	
	private static void replaceClassInstantiation(InsnList instructions, TypeInsnNode newNode, String newClass) {
		MethodInsnNode initNode = (MethodInsnNode) ASMHelper.findInstruction(newNode, false, n -> FindInstruction.methodInsn(n, newNode.desc, "<init>", null));
		
		newNode.desc = newClass;
		initNode.owner = newClass;
	}
}
