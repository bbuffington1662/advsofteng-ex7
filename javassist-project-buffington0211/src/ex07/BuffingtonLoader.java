package ex07;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.System;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class BuffingtonLoader extends ClassLoader {
	
	static final String WORK_DIR = System.getProperty("user.dir");
	static final String INPUT_PATH = WORK_DIR + File.separator + "classfiles";
	static String TARGET_MY_APP = null;
	static  String TARGET_MY_METHOD = null;
	static String TARGET_MY_PARAM = null;
	static  String TARGET_MY_VALUE = null;
	static String _L_ = System.lineSeparator();
	private ClassPool pool;
	
	public static void main(String[] args) throws Throwable {
		Scanner input = new Scanner(System.in);
		String[] values = { "hi" };
		ArrayList<String> modified = new ArrayList<String>();
		
		while (!values[0].equals("x")) {
		
			System.out.print("Please enter the classname, methodname, parameter number, and parameter value separated by commas or 'x' to exit: ");
			values = input.nextLine().split(",");
			
			while (values.length != 4 || modified.contains(values[1])) {
				if(values[0].equals("x")) {
					System.exit(0);
				}
				if (!modified.contains(values[1])) {
					System.out.println("[WRN] Invalid input size!!");
				}
				else {
					System.out.printf("[WRN] This method '%s' has been modified!!\n", values[1]);
				}
				System.out.print("Please enter the classname, methodname, parameter number, and parameter value separated by commas or 'x' to exit: ");
				values = input.nextLine().split(",");
			}
			
			modified.add(values[1]);
			TARGET_MY_APP = values[0];
			TARGET_MY_METHOD = values[1];
			TARGET_MY_PARAM = values[2];
			TARGET_MY_VALUE = values[3];
			
			BuffingtonLoader s = new BuffingtonLoader();
			TARGET_MY_APP = values[0];
			TARGET_MY_METHOD = values[1];
			TARGET_MY_PARAM = values[2];
			TARGET_MY_VALUE = values[3];
		    Class<?> c = s.loadClass(TARGET_MY_APP);
		    Method mainMethod = c.getDeclaredMethod("main", new Class[] { String[].class });
		    mainMethod.invoke(null, new Object[] { args });
		}
		
		input.close();
	}
	
	public BuffingtonLoader() throws NotFoundException {
	      pool = new ClassPool();
	      pool.insertClassPath(new ClassClassPath(new java.lang.Object().getClass()));
	      pool.insertClassPath(INPUT_PATH); // "target" must be there.
	      System.out.println("[DBG] Class Pathes: " + pool.toString());
	   }

	   /*
	    * Finds a specified class. The bytecode for that class can be modified.
	    */
	   protected Class<?> findClass(String name) throws ClassNotFoundException {
	      CtClass cc = null;
	      try {
	         cc = pool.get(name);
	         if (!cc.getName().equals(TARGET_MY_APP)) {
	            return defineClass(name, cc.toBytecode(), 0, cc.toBytecode().length);
	         }

	         cc.instrument(new ExprEditor() {
	            public void edit(MethodCall call) throws CannotCompileException {
	               String className = call.getClassName();
	               String methodName = call.getMethodName();

	               if (methodName.contentEquals(TARGET_MY_METHOD)) {
		              System.out.println("[Edited by ClassLoader] method name: " + methodName + ", line: " + call.getLineNumber());
		              String block1 = String.format("\n\t\tSystem.out.println(\"\tReset param %s to %s.\");\n\t\t$%s = %s;\n\t\t$proceed($$);", TARGET_MY_PARAM, TARGET_MY_VALUE, TARGET_MY_PARAM, TARGET_MY_VALUE);
		              System.out.println("[DBG] BLOCK1: " + block1);
		              System.out.println("------------------------");
		              call.replace(block1);
	               }
	            }
	         });
	         byte[] b = cc.toBytecode();
	         return defineClass(name, b, 0, b.length);
	      } catch (NotFoundException e) {
	         throw new ClassNotFoundException();
	      } catch (IOException e) {
	         throw new ClassNotFoundException();
	      } catch (CannotCompileException e) {
	         e.printStackTrace();
	         throw new ClassNotFoundException();
	      }
	}

}
