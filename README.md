# ASM-Instrumentation
Living in the Matrix with Bytecode Manipulation
https://blog.newrelic.com/engineering/diving-bytecode-manipulation-creating-audit-log-asm-javassist/

Java ASM bytecode manipulation 

ASM is an all purpose Java bytecode manipulation and analysis framework. It can be used to modify existing classes or dynamically generate classes, directly in binary form. This demo will guide you to have basic understand how ASM works and what will ASM do.

## Environment
The ASM bytecode manipulation framework is written in Java. Tested in Java Runtime Environment 8.

### Download ASM
You can download the latest binary file from the [ObjectWeb Forge](http://forge.ow2.org/projects/asm/). Used ow2 ASM version 5.2.

### Eclipse plugin
Bytecode Outline plugin for Eclipse shows disassembled bytecode of current Java editor or class file. The Bytecode Outline plugin can be installed from the Eclipse Update Manager with the ObjectWeb Eclipse Update Site http://download.forge.objectweb.org/eclipse-update/

## Java Bytecode
Here is a quick review in case you are not familiar with Java Bytecode. Java Bytecode is an intermediate code between Java source code and assembly code. Java source code `.java` file can be compiled into Bytecode `.class` file and run on where any computers have a Java Runtime Environment.

![Compile Java](https://raw.githubusercontent.com/zuloloxi/ASM-Instrumentation/master/ASM/image/21.jpg)

As mentioned before, ASM framework includes tools to help you translate between those codes. Bytecode Outline shows disassembled bytecode of current Java editor or class file. Unlike `javap`, ASMifier on compiled classes allows you to see how any given bytecode could be generated with ASM.

## Reflection and Instrumentation
Reflection means the ability for a program to examine, introspect, and modify its own structure and behavior at runtime.<sup>[[1](http://www2.parc.com/csl/groups/sda/projects/reflection96/docs/malenfant/malenfant.pdf)]</sup> However refelction is not sufficient in many cases such as source in non-Java language. ASM framework uses a visitor-based approach to generate bytecode and drive transformations of existing classes. 

## Visitor Pattern
ASM utilizes [Visitor Pattern](https://en.wikipedia.org/wiki/Visitor_pattern) to accomplish dynamic dispatch on object and its behavior. 
The Core package can be logically divided into two major parts:

* Bytecode producers, such as a ClassReader or a custom class that can fire the proper sequence of calls to the methods of the above visitor classes.

* Bytecode consumers, such as writers (ClassWriter, FieldWriter, MethodWriter, and AnnotationWriter), or any other classes implementing the above visitor interfaces.

ASM ClassReader will call `accept()` to allow visitor to walk through itself. We can define our own visitor to override any methods in order to manipulate bytecode we desire to chanage.

Bytecode is the instruction set of the Java Virtual Machine (JVM), and all languages that run on the JVM must eventually compile down to bytecode. Bytecode is manipulated for a variety of reasons:

##Program analysis:

###find bugs in your application
###examine code complexity
###find classes with a specific annotation
##Class generation:

###lazy load data from a database using proxies
##Security:

###restrict access to certain APIs
###code obfuscation
##Transforming classes without the Java source code:

###code profiling
###code optimization
##And finally, adding logging to applications.

There are several tools that can be used to manipulate bytecode, ranging from very low-level tools such as ASM, which require you to work at the bytecode level, to high level frameworks such as AspectJ, which allow you to write pure Java.

## ASM to create an audit log.
![Agent](https://raw.githubusercontent.com/zuloloxi/ASM-Instrumentation/master/ASM/image/72.jpg)
We will use Java agent to monitor the main process and use ASM to modify the bytecode at running time.
Let us say we are particularly interested in certain methods in main

```java
    BankTransactions bank = new BankTransactions();
    for (int i = 0; i < 100; i++) {
        String accountId = "account" + i;
	bank.login("password", accountId, "Ashley");
	bank.unimportantProcessing(accountId);
	bank.withdraw(accountId, Double.valueOf(i));
    }
```

We want to keep track of those important behaviors such as login and withdraw. We can use Java annotation to mark those methods for later use.

By setting the premain flag in manifest file, our program will now start from premain function. The premain method acts as a setup hook for the agent. It allows the agent to register a class transformer. When a class transformer is registered with the JVM, that transformer will receive the bytes of every class prior to the class being loaded in the JVM.

```java
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
    ClassVisitor visitor = new LogMethodClassVisitor(writer, className);
    reader.accept(visitor, 0);
    return writer.toByteArray();
```

ASM palys role here, when visitor visits any methods with annotation `@Important`, we record the field related to the method and modify any bytecode as we wish. Here we simply print any important methods' index of parameter that we care:

```java
    System.out.println(methodName);
    if (isImportant) {
	mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
	mv.visitLdcInsn(parameterIndexes.toString());
	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }
```
How do we know what to put in the visitor methods? As we mentioned above the ASMifier can let us know what ASM code needed to generate the target code.

Notice that the process only happens when we first time load the method, so that the method name will only print once while the parameter index will print many times, since we have already modified its bytecode in the method, whenever the method is invoked the important parameter index will also be printed. That will be a basic instrumentation by using ASM.

```
$ java -cp lib/asm-5.2.jar -javaagent:myagent.jar BankTransactions
Starting the agent
<init>
main
unimportantProcessing
login
withdraw
[1, 2]
[0, 1]
[1, 2]
[0, 1]
...
```

## Reference
* https://www.infoq.com/articles/Living-Matrix-Bytecode-Manipulation/
* "ASM: a code manipulation tool to implement adaptable systems", E. Bruneton, R. Lenglet and T. Coupaye, Adaptable and extensible component systems, November 2002, Grenoble, France.
* "Using ASM framework to implement common bytecode transformation patterns", E. Kuleshov, AOSD.07, March 2007, Vancouver, Canada.
* [Official Tutorial for ASM 2.0.](http://asm.ow2.org/doc/tutorial-asm-2.0.html)
* [Instrumenting Java Bytecode with ASM](http://web.cs.ucla.edu/~msb/cs239-tutorial/)
* [Diving into Bytecode Manipulation: Creating an Audit Log with ASM and Javassist](https://blog.newrelic.com/2014/09/29/diving-bytecode-manipulation-creating-audit-log-asm-javassist/)
* [An introduction to Java Agent and bytecode manipulation](http://www.tomsquest.com/blog/2014/01/intro-java-agent-and-bytecode-manipulation/)
