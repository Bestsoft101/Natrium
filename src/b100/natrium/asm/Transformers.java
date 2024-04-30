package b100.natrium.asm;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import b100.asmloader.ClassTransformer;
import b100.natrium.asm.utils.ASMHelper;
import b100.natrium.asm.utils.FindInstruction;
import b100.utils.StringReader;

public class Transformers {
	
	private static String listenerClass = "b100/natrium/asm/Listeners";
//	private static InjectHelper injectHelper = new InjectHelper(listenerClass, "b100/natrium/asm/utils/CallbackInfo");
	private static final String GL11 = "org/lwjgl/opengl/GL11";
	
	class MinecraftClientTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraft/client/Minecraft");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode startGame = ASMHelper.findMethod(classNode, "startGame", null);
			MethodNode changeWorld = ASMHelper.findMethod(classNode, "changeWorld", "(Lnet/minecraft/core/world/World;Ljava/lang/String;Lnet/minecraft/core/entity/player/EntityPlayer;)V");
			
			List<AbstractInsnNode> returnNodes = ASMHelper.findAllInstructions(startGame.instructions, (n) -> n.getOpcode() == Opcodes.RETURN);
			for(int i=0; i < returnNodes.size(); i++) {
				startGame.instructions.insertBefore(returnNodes.get(i), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onStartGame", "()V"));	
			}
			
			changeWorld.instructions.insertBefore(changeWorld.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onWorldChange", "()V"));
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
//			MethodInsnNode chunkRendererInitNode = (MethodInsnNode) ASMHelper.findInstruction(loadRenderers, false, (n) -> n.getOpcode() == Opcodes.INVOKESPECIAL && FindInstruction.methodInsn(n, "net/minecraft/client/render/ChunkRenderer", "<init>", null));
//			TypeInsnNode chunkRendererNewNode = (TypeInsnNode) ASMHelper.findInstruction(chunkRendererInitNode, true, (n) -> n.getOpcode() == Opcodes.NEW);
//			chunkRendererInitNode.owner = "b100/natrium/ChunkRendererMultiDraw";
//			chunkRendererNewNode.desc = "b100/natrium/ChunkRendererMultiDraw";
			
			loadRenderers.instructions.insertBefore(loadRenderers.instructions.getFirst(), new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onReloadChunks", "()V"));
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
			
			classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "renderListEntries", "[Lb100/natrium/VBOPool$Entry;", null, null));

			MethodNode updateRenderer = ASMHelper.findMethod(classNode, "updateRenderer", "()V");
			MethodNode reset = ASMHelper.findMethod(classNode, "reset", "()V");
			MethodNode updateInFrustum = ASMHelper.findMethod(classNode, "updateInFrustum", null);
			
			{
				InsnList insert = new InsnList();
				insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "resetChunkRenderer", "(Lnet/minecraft/client/render/ChunkRenderer;)V"));
				reset.instructions.insertBefore(reset.instructions.getFirst(), insert);
			}
			
			{
				/*
				AbstractInsnNode node = ASMHelper.findInstruction(updateRenderer, false, (aaa) -> {
					System.out.println(ASMHelper.toString(aaa));
					if(aaa instanceof FieldInsnNode) {
						FieldInsnNode fieldInsnNode = (FieldInsnNode) aaa;
						System.out.println("METHOD " + fieldInsnNode.owner + fieldInsnNode.name + fieldInsnNode.desc);
						if(fieldInsnNode.name.equals("isLit")) {
							return true;
						}else {
							System.out.println(fieldInsnNode.name + " != isLit");
						}
					}
					
					return false;
				});
				*/
				AbstractInsnNode node = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.fieldInsn(n, null, "isLit", null)).getPrevious();
				InsnList insert = new InsnList();
				insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "beforeRenderChunk", "(Lnet/minecraft/client/render/ChunkRenderer;)V"));
				updateRenderer.instructions.insertBefore(node, insert);
			}
			{
				AbstractInsnNode start = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.methodInsn(n, "net/minecraft/client/render/tessellator/Tessellator", "startDrawingQuads", null));
				InsnList insert = new InsnList();
				insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insert.add(new VarInsnNode(Opcodes.ILOAD, 11)); // renderPass
				insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "startRenderingChunk", "(Lnet/minecraft/client/render/ChunkRenderer;I)V"));
				updateRenderer.instructions.insert(start, insert);
			}
			{
				AbstractInsnNode start = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.methodInsn(n, "net/minecraft/client/render/tessellator/Tessellator", "draw", null));
				InsnList insert = new InsnList();
				insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
				insert.add(new VarInsnNode(Opcodes.ILOAD, 11)); // renderPass
				insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "stopRenderingChunk", "(Lnet/minecraft/client/render/ChunkRenderer;I)V"));
				updateRenderer.instructions.insert(start, insert);
			}
			
			// Before rendering
			AbstractInsnNode n0 = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.methodInsn(n, GL11, "glNewList", null));
			AbstractInsnNode n1 = ASMHelper.findInstruction(n0, false, (n) -> FindInstruction.methodInsn(n, GL11, "glPushMatrix", null));
			AbstractInsnNode n2 = ASMHelper.findInstruction(n1, false, (n) -> FindInstruction.methodInsn(n, GL11, "glTranslatef", null));
			AbstractInsnNode n3 = ASMHelper.findInstruction(n2, false, (n) -> FindInstruction.methodInsn(n, GL11, "glScalef", null));
			AbstractInsnNode n4 = ASMHelper.findInstruction(n3, false, (n) -> FindInstruction.methodInsn(n, GL11, "glTranslatef", null));
			AbstractInsnNode n5 = ASMHelper.findInstruction(n4, false, (n) -> FindInstruction.methodInsn(n, "net/minecraft/client/render/tessellator/Tessellator", "setTranslation", null));
			if(n5 == null) {
				throw new NullPointerException();
			}
			
			// After rendering
			AbstractInsnNode n6 = ASMHelper.findInstruction(updateRenderer, false, (n) -> FindInstruction.methodInsn(n, "net/minecraft/client/render/tessellator/Tessellator", "draw", null));
			AbstractInsnNode n7 = ASMHelper.findInstruction(n6, false, (n) -> FindInstruction.methodInsn(n, GL11, "glPopMatrix", null));
			AbstractInsnNode n8 = ASMHelper.findInstruction(n7, false, (n) -> FindInstruction.methodInsn(n, GL11, "glEndList", null));
			if(n8 == null) {
				throw new NullPointerException();
			}
			
			removeMethodCall(updateRenderer.instructions, n0);
			removeMethodCall(updateRenderer.instructions, n1);
			removeMethodCall(updateRenderer.instructions, n2);
			removeMethodCall(updateRenderer.instructions, n3);
			removeMethodCall(updateRenderer.instructions, n4);
			removeMethodCall(updateRenderer.instructions, n5);
			removeMethodCall(updateRenderer.instructions, n6);
			removeMethodCall(updateRenderer.instructions, n7);
			removeMethodCall(updateRenderer.instructions, n8);

			MethodNode setupGLTranslation = ASMHelper.findMethod(classNode, "setupGLTranslation", "()V");
			removeMethodCall(setupGLTranslation.instructions, ASMHelper.findInstruction(setupGLTranslation, false, (n) -> FindInstruction.methodInsn(n, GL11, "glTranslatef", null)));
			
			{
				ASMHelper.findAllInstructions(updateInFrustum.instructions, (n) -> n.getOpcode() == Opcodes.RETURN).forEach((n) -> {
					InsnList insert = new InsnList();
					insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
					insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "onUpdateInFrustum", "(Lnet/minecraft/client/render/ChunkRenderer;)V"));
					updateInFrustum.instructions.insertBefore(n, insert);
				});
			}
		}
	}
	
	public static void removeMethodCall(InsnList instructions, AbstractInsnNode node) {
		MethodInsnNode methodInsnNode = (MethodInsnNode) node;
		
		if(methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC) {
			instructions.insert(node, new InsnNode(Opcodes.POP));
		}
		
		StringReader reader = new StringReader(methodInsnNode.desc);
		reader.expectAndSkip('(');
		while(true) {
			char c = reader.getAndSkip();
			if(c == ')') {
				break;
			}else if(c == 'L') {
				instructions.insert(node, new InsnNode(Opcodes.POP));
				reader.readUntilCharacter(';');
				reader.next();
			}else if(c == 'D'){
				instructions.insert(node, new InsnNode(Opcodes.POP2));
			}else {
				instructions.insert(node, new InsnNode(Opcodes.POP));
			}
		}
		
		instructions.remove(node);
	}
	
}
