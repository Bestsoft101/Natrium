package b100.natrium.asm;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
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
	
	class MinecraftClientTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/Minecraft");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode startGame = ASMHelper.findMethod(classNode, "startGame", null);
			List<AbstractInsnNode> returnNodes = ASMHelper.findAllInstructions(startGame.instructions, (n) -> n.getOpcode() == Opcodes.RETURN);
			for(int i=0; i < returnNodes.size(); i++) {
				startGame.instructions.insertBefore(returnNodes.get(i), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onStartGame", "()V"));	
			}
		}
	}
	
	class WorldRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/WorldRenderer");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode renderWorld = ASMHelper.findMethod(classNode, "renderWorld", null);
			
			List<AbstractInsnNode> sortAndRenderNodes = ASMHelper.findAllInstructions(renderWorld.instructions, (n) -> FindInstruction.methodInsn(n, "sortAndRender"));
			for(int i=0; i < sortAndRenderNodes.size(); i++) {
				AbstractInsnNode oldInstruction = sortAndRenderNodes.get(i);
				AbstractInsnNode newInstruction = new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onSortAndRender", "(Lnet/minecraft/client/render/RenderGlobal;Lnet/minecraft/client/render/camera/ICamera;ID)I");
				ASMHelper.replaceInstruction(renderWorld, oldInstruction, newInstruction);
			}
			
			List<AbstractInsnNode> callAllDisplayListsNodes = ASMHelper.findAllInstructions(renderWorld.instructions, (n) -> FindInstruction.methodInsn(n, "callAllDisplayLists"));
			for(int i=0; i < callAllDisplayListsNodes.size(); i++) {
				AbstractInsnNode oldInstruction = callAllDisplayListsNodes.get(i);
				AbstractInsnNode newInstruction = new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onCallAllDisplayLists", "(Lnet/minecraft/client/render/RenderGlobal;ID)V");
				ASMHelper.replaceInstruction(renderWorld, oldInstruction, newInstruction);
			}
		}
	}
	
	class RenderGlobalTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/RenderGlobal");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			ASMHelper.findField(classNode, "renderDistance").access = Opcodes.ACC_PUBLIC;
			ASMHelper.findMethod(classNode, "markRenderersForNewPosition", null).access = Opcodes.ACC_PUBLIC;
			
			MethodNode loadRenderers = ASMHelper.findMethod(classNode, "loadRenderers", null);
			
			// Create ChunkRendererMultiDraw instead of ChunkRenderer
			MethodInsnNode chunkRendererInitNode = (MethodInsnNode) ASMHelper.findInstruction(loadRenderers, false, (n) -> n.getOpcode() == Opcodes.INVOKESPECIAL && FindInstruction.methodInsn(n, "net/minecraft/client/render/ChunkRenderer", "<init>", null));
			TypeInsnNode chunkRendererNewNode = (TypeInsnNode) ASMHelper.findInstruction(chunkRendererInitNode, true, (n) -> n.getOpcode() == Opcodes.NEW);
			chunkRendererInitNode.owner = "b100/natrium/ChunkRendererMultiDraw";
			chunkRendererNewNode.desc = "b100/natrium/ChunkRendererMultiDraw";
		}
	}
	
	class TessellatorTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/Tessellator");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode constructor = ASMHelper.findMethod(classNode, "<init>", null);
			InsnList insert = injectHelper.createMethodCallInject(classNode, constructor, "onTessellatorConstructed");
			
			AbstractInsnNode createDirectByteBufferNode = ASMHelper.findInstruction(constructor.instructions.getFirst(), false, (n) -> FindInstruction.methodInsn(n, "createDirectByteBuffer"));
			createDirectByteBufferNode = ASMHelper.findInstruction(createDirectByteBufferNode, true, (n) -> FindInstruction.varInsn(n, 0));
			
			constructor.instructions.insertBefore(createDirectByteBufferNode, insert);
			
			// Make everything public
			for(int i=0; i < classNode.fields.size(); i++) {
				FieldNode fieldNode = classNode.fields.get(i);
				fieldNode.access = makePublic(fieldNode.access); 
			}
			
			for(int i=0; i < classNode.methods.size(); i++) {
				MethodNode methodNode = classNode.methods.get(i);
				methodNode.access = makePublic(methodNode.access);
			}
		}
	}
	
	public static int makePublic(int access) {
		access = access | Opcodes.ACC_PUBLIC;
		access = access & ~Opcodes.ACC_PRIVATE;
		access = access & ~Opcodes.ACC_PROTECTED;
		return access;
	}
	
	class ChunkRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/render/ChunkRenderer");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			ASMHelper.findField(classNode, "tessellator").access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
			ASMHelper.findField(classNode, "tileEntities").access = Opcodes.ACC_PUBLIC;
			ASMHelper.findField(classNode, "isInitialized").access = Opcodes.ACC_PUBLIC;
		}
		
	}
	
}
