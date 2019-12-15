package listener.main;


import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;

import listener.main.SymbolTable.Type;

public class BytecodeGenListenerHelper {
	
	// <boolean functions>
	
	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof MiniCParser.Fun_declContext;
	}
	
	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}
	
	// global vars
	static Object initVal(Var_declContext ctx, SymbolTable.Type type) {
		 String value = ctx.LITERAL().getText();
	        switch (type) {
	            case INT:
	                return Integer.parseInt(value);
	            case DOUBLE:
	                return Double.parseDouble(value);
	            case FLOAT:
	                return Float.parseFloat(value);
	            default:
//	            	System.out.println("<ERROR: init value not valid>");
	                return value;
	       }
		
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static Object initVal(Local_declContext ctx,  SymbolTable.Type type) {
		
		 String value = ctx.LITERAL().getText();
	
		switch (type) {
        case INT:
            return Integer.parseInt(value);
        case DOUBLE:
            return Double.parseDouble(value);
        
        case FLOAT:
            return Float.parseFloat(value);
        default:
            return value;
		}
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}
	
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	
	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in 0>
		//get first child of ctx
		//int add(int a , int b)
		//-> int 
		//returns 1 if is 'void'
		return ctx.getChild(0).getText().equals("void");
	}
	
	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}
	
	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
			// <Fill in 1>
		//if type string equals int 
		String type = typespec.getText(); 
		if(type.compareTo("int") == 0)
			return "I";
		
		else if(type.compareTo("double") == 0)
			return "D";
		else if(type.compareTo("float") == 0)
			return "F";
		
		//other cases left for else statements 
		return "";
	}

	// params
	static String getParamName(ParamContext param) {
		// <Fill in 2>
		//get identifier text
		return param.IDENT().getText();
	}
	
	static Type getParamType(ParamContext param) {
		if(param.getChild(0).getText().equals("int"))
			return Type.INT;
		else if(param.getChild(0).getText().equals("double"))
			return Type.DOUBLE;
		else if(param.getChild(0).getText().equals("float"))
			return Type.FLOAT;
		
		return null;
	}
	
	
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			MiniCParser.Type_specContext typespec = (MiniCParser.Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}
	
	
	static String getLocalVarName(Local_declContext local_decl) {
		// <Fill in 3>
		//get identifier text
		return local_decl.IDENT().getText();
	}
	
	static String getFunName(Fun_declContext ctx) {
		// <Fill in 4>
		//get identifier text
		return ctx.IDENT().getText();
	}
	
	static String getFunName(ExprContext ctx) {
		// <Fill in 5>
		//get identifier text
		return ctx.IDENT().getText();
	}
	
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() < 5;
	}
	
	static String getFunProlog() {
		// return ".class public Test .....
		// ...
		// invokenonvirtual java/lang/Object/<init>()
		// return
		// .end method"
		
		// <Fill in 6>
		String prolog = "\n\n";
		

		prolog += ".class public "
				
			+ getCurrentClassName()
			+ "\n"
			//.class public Test
			+ ".super java/lang/Object\n\n"
			//.super java/lang/Object
			+".method public <init>()V\n"
			//.method public <init>()V
			+ "aload_0\n"
			//aload_0
			+ "invokenonvirtual java/lang/Object/<init>()V\n"
			//invokenonvirtual java/lang/Object/<init>()V
			+ "return\n"
			//return
			+ ".end method\n\n";
			//.end method
		return prolog;
	}
	
	static String getCurrentClassName() {
		return "Test";
	}
}
