import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import b100.asmloader.ClassTransformer;
import b100.natrium.utils.ASMHelper;
import b100.natrium.utils.FindInstruction;
import b100.natrium.utils.InjectHelper;

public class NatriumTransformers {

	public static final String class_GuiIngame = "atr";
	public static final String class_EntityRenderer = "ban";
	public static final String class_RenderGlobal = "bav";
	public static final String class_Tessellator = "baz";
	public static final String class_RenderEngine = "bba";
	public static final String class_EntityLiving = "md";
	public static final String class_World = "yc";
	public static final String class_RenderBlocks = "bbb";
	public static final String class_GL11 = "org/lwjgl/opengl/GL11";
	
	private static final String listenerClass = "NatriumMod";
	
	private static final InjectHelper injectHelper = new InjectHelper(listenerClass);
	
	class FMLRelaunchLogTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("cpw/mods/fml/relauncher/FMLRelaunchLog");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode configureLogging = ASMHelper.findMethod(classNode, "configureLogging");
			List<AbstractInsnNode> nodes = ASMHelper.findAllInstructions(configureLogging, (n) -> {
				return n.getOpcode() == Opcodes.INVOKESTATIC && (
						FindInstruction.methodInsn(n, "java/lang/System", "setOut", null) ||
						FindInstruction.methodInsn(n, "java/lang/System", "setErr", null));
			});
			for(AbstractInsnNode node : nodes) {
				InsnList insert = new InsnList();
				insert.add(new InsnNode(Opcodes.POP));
				ASMHelper.replaceInstruction(configureLogging, node, insert);
			}
		}
	}
	
	class ChunkRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("baj");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode constructor = ASMHelper.findMethod(classNode, "<init>");
			InsnList insert = new InsnList();
			insert.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insert.add(new MethodInsnNode(Opcodes.INVOKESTATIC, listenerClass, "checkChunkRenderer", "(Lbaj;)V"));
			ASMHelper.insertBeforeLastReturn(constructor, insert);
		}
		
	}
	
	class TessellatorTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals(class_Tessellator);
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			for(FieldNode field : classNode.fields) {
				makePublic(field);
			}
			for(MethodNode method : classNode.methods) {
				makePublic(method);
			}
			MethodNode staticInit = ASMHelper.findMethod(classNode, "<clinit>");
			
			TypeInsnNode node1 = (TypeInsnNode) ASMHelper.findInstruction(staticInit, false, (n) -> {
				if(n instanceof TypeInsnNode) {
					TypeInsnNode typeInsnNode = (TypeInsnNode) n;
					return typeInsnNode.desc.equals("baz");
				}
				return false;
			});
			node1.desc = "CustomTessellator";
			
			MethodInsnNode node2 = (MethodInsnNode) ASMHelper.findInstruction(staticInit, false, (n) -> {
				return n.getOpcode() == Opcodes.INVOKESPECIAL && FindInstruction.methodInsn(n, "baz", "<init>", "(I)V");
			});
			node2.owner = "CustomTessellator";
		}
		
	}
	
	class RenderGlobalTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals(class_RenderGlobal);
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findMethod(classNode, "c", "(III)V")); // markRenderersForNewPosition
			makePublic(ASMHelper.findField(classNode, "N")); // visible chunk count
			makePublic(ASMHelper.findField(classNode, "K")); // total chunk count
		}
		
	}
	
	class WrUpdatesTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("WrUpdates");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode staticInit = ASMHelper.findMethod(classNode, "makeWorldRenderer");
			
			TypeInsnNode node1 = (TypeInsnNode) ASMHelper.findInstruction(staticInit, false, (n) -> {
				if(n instanceof TypeInsnNode) {
					TypeInsnNode typeInsnNode = (TypeInsnNode) n;
					return typeInsnNode.desc.equals("baj");
				}
				return false;
			});
			node1.desc = "ChunkRendererMultiDraw";
			
			MethodInsnNode node2 = (MethodInsnNode) ASMHelper.findInstruction(staticInit, false, (n) -> {
				return n.getOpcode() == Opcodes.INVOKESPECIAL && FindInstruction.methodInsn(n, "baj", "<init>", null);
			});
			node2.owner = "ChunkRendererMultiDraw";
		}
	}
	
	class EntityRendererTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals(class_EntityRenderer);
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode renderWorld = ASMHelper.findMethod(classNode, "a", "(FJ)V");
			
			AbstractInsnNode renderTerrainNode1 = ASMHelper.findInstruction(renderWorld, false, (n) -> {
				return n.getOpcode() == Opcodes.INVOKEVIRTUAL && FindInstruction.methodInsn(n, class_RenderGlobal, "a", "(L" + class_EntityLiving + ";ID)I");
			});
			ASMHelper.replaceInstruction(renderWorld.instructions, renderTerrainNode1,
					new MethodInsnNode(Opcodes.INVOKESTATIC, "NatriumMod", "onRenderTerrain", "(L" + class_RenderGlobal + ";L" + class_EntityLiving + ";ID)I"));
			
			List<AbstractInsnNode> renderWaterNodes = ASMHelper.findAllInstructions(renderWorld, (n) -> {
				return n.getOpcode() == Opcodes.INVOKEVIRTUAL && FindInstruction.methodInsn(n, class_RenderGlobal, "renderAllSortedRenderers", null);
			});
			
			for(int i=0; i < renderWaterNodes.size(); i++) {
				AbstractInsnNode renderWaterNode = renderWaterNodes.get(i);
				ASMHelper.replaceInstruction(renderWorld, renderWaterNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "NatriumMod", "nop", "(L" + class_RenderGlobal + ";ID)I"));
			}
			
			AbstractInsnNode beforeWaterRender = ASMHelper.findInstruction(renderWorld, false, (n) -> {
				return n.getOpcode() == Opcodes.INVOKESTATIC && FindInstruction.methodInsn(n, "Shaders", "beginWater", "()V");
			});
			AbstractInsnNode afterWaterRender;
			if(beforeWaterRender != null) {
				// Shaders installed
				afterWaterRender = ASMHelper.findInstruction(renderWorld, false, (n) -> {
					return n.getOpcode() == Opcodes.INVOKESTATIC && FindInstruction.methodInsn(n, "Shaders", "endWater", "()V");
				});
				
			}else {
				// Shaders not installed
				beforeWaterRender = ASMHelper.findInstruction(renderWorld, true, (n) -> {
					return n.getOpcode() == Opcodes.INVOKESTATIC && FindInstruction.methodInsn(n, class_GL11, "glBindTexture", null);
				});
				afterWaterRender = ASMHelper.findInstruction(beforeWaterRender, false, (n) -> {
					return n.getOpcode() == Opcodes.INVOKESTATIC && FindInstruction.methodInsn(n, class_GL11, "glDepthMask", null);
				});
			}
			
			renderWorld.instructions.insertBefore(afterWaterRender, new MethodInsnNode(Opcodes.INVOKESTATIC, "NatriumMod", "onRenderWater", "()V"));
			
			// Disable OptiFine's lagometer
			MethodNode showLagometer = ASMHelper.findMethod(classNode, "showLagometer");
			InsnList insert = new InsnList();
			insert.add(new InsnNode(Opcodes.RETURN));
			ASMHelper.insertAtStart(showLagometer, insert);
		}
	}
	
	class ForgeHooksClientTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("net/minecraftforge/client/ForgeHooksClient");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode bindTexture = ASMHelper.findMethod(classNode, "bindTexture");
			ASMHelper.insertAtStart(bindTexture, injectHelper.createMethodCallInject(classNode, bindTexture, "onForgeBindTexture"));
		}
		
	}
	
	class GuiIngameTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals(class_GuiIngame);
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode renderGui = ASMHelper.findMethod(classNode, "a", "(FZII)V");
			InsnList insert = injectHelper.createMethodCall(classNode, renderGui, "onRenderGui");
			ASMHelper.insertBeforeLastReturn(renderGui, insert);
		}
	}
	
	class RenderEngineTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("bba");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			makePublic(ASMHelper.findField(classNode, "c"));
			makePublic(ASMHelper.findField(classNode, "h"));
			
			MethodNode registerCustomTexturesFX = ASMHelper.findMethod(classNode, "registerCustomTexturesFX");
			InsnList insert = injectHelper.createMethodCall(classNode, registerCustomTexturesFX, "onRegisterCustomAnimations");
			ASMHelper.insertBeforeLastReturn(registerCustomTexturesFX, insert);
		}
		
	}
	
	class LoadControllerTransformer extends ClassTransformer {

		@Override
		public boolean accepts(String className) {
			return className.equals("cpw/mods/fml/common/LoadController");
		}

		@Override
		public void transform(String className, ClassNode classNode) {
			MethodNode methodNode = ASMHelper.findAllMethods(classNode, (m) -> m.name.equals("distributeStateMessage")).get(0);
			InsnList insert = injectHelper.createMethodCall(classNode, methodNode, "beforeStateMessage");
			ASMHelper.insertAtStart(methodNode, insert);
			insert = injectHelper.createMethodCall(classNode, methodNode, "afterStateMessage");
			ASMHelper.insertBeforeLastReturn(methodNode, insert);
		}
	}
	
	public static void makePublic(MethodNode node) {
		node.access = makePublic(node.access);
	}
	
	public static void makePublic(FieldNode node) {
		node.access = makePublic(node.access);
	}
	
	public static int makePublic(int access) {
		access &= ~Opcodes.ACC_PRIVATE;
		access &= ~Opcodes.ACC_PROTECTED;
		access |= Opcodes.ACC_PUBLIC;
		return access;
	}

}
