import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PrintMessageMethodVisitor extends MethodVisitor {
	private boolean isImportant = false;
	ArrayList<String> parameterIndexes;
	//private String methodName;

	public PrintMessageMethodVisitor(MethodVisitor mv, String name, String className) {
		super(Opcodes.ASM5, mv);
		parameterIndexes = new ArrayList<>();
		//methodName = name;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if ("LImportantLog;".equals(desc)) {
			isImportant = true;
			return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
				public AnnotationVisitor visitArray(String name) {
					if ("fields".equals(name)) {
						return new AnnotationVisitor(Opcodes.ASM5, super.visitArray(name)) {
							public void visit(String name, Object value) {
								parameterIndexes.add((String) value);
								super.visit(name, value);
							}
						};
					} else {
						return super.visitArray(name);
					}
				}
			};
	    }
	    return super.visitAnnotation(desc, visible);
	}
	
	@Override
	public void visitCode() {
		super.visitCode();
		//System.out.println(methodName);
		//Ziye Xing adding code (bytecode) that working nice
		if (isImportant) {
			for (String index : parameterIndexes) {
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitVarInsn(Opcodes.ALOAD, Integer.valueOf(index) + 1);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
			}
		}
		/*
		starting older code not working...
		if (isImportant) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn(parameterIndexes.toString());
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
			/*
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", 
			        "out","Ljava/io/PrintStream;");
			mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
			mv.visitInsn(Opcodes.DUP);
			
			mv.visitLdcInsn("A call was made to method \"");
		    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
		        "java/lang/StringBuilder", "",
		        "(Ljava/lang/String;)V", false);
		    mv.visitLdcInsn(methodName);
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
		        "java/lang/StringBuilder", "append",
		        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
		    */
		}
	 	ending older code not working
		*/
	}
}
