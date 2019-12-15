package listener.main;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ParamsContext;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();
	
	int tab = 0;
	int label = 0;
	
	// program	: decl+
	
	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		symbolTable.initFunDecl();
		
		String fname = getFunName(ctx);
		ParamsContext params;
		
		if (fname.equals("main")) {
			symbolTable.putLocalVar("args", Type.INTARRAY);
		} else {
			symbolTable.putFunSpecStr(ctx);
			params = (MiniCParser.ParamsContext) ctx.getChild(3);
			symbolTable.putParams(params);
		}		
	}

	
	// var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		
		if (isArrayDecl(ctx)) {
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			
			if (ctx.type_spec().getText().equals("double")) {
				symbolTable.putGlobalVarWithInitVal(varName, Type.DOUBLE, initVal(ctx ,Type.DOUBLE ));
			} 
			else if (ctx.type_spec().getText().equals("float")) {
				symbolTable.putGlobalVarWithInitVal(varName, Type.FLOAT, initVal(ctx, Type.FLOAT));
			} 
			else {
				symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx, Type.INT));
			}
		}
		else  { // simple decl
			if (ctx.type_spec().getText().equals("double")) {
				symbolTable.putGlobalVar(varName, Type.DOUBLE);
			} 
			else if (ctx.type_spec().getText().equals("float")) {
				symbolTable.putGlobalVar(varName, Type.FLOAT);
			} 
			else {
				symbolTable.putGlobalVar(varName, Type.INT);
			}
		}
	}

	
	@Override
	public void enterLocal_decl(MiniCParser.Local_declContext ctx) {			
		if (isArrayDecl(ctx)) {
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {
			if (ctx.type_spec().getText().equals("double")) {
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.DOUBLE, initVal(ctx, Type.DOUBLE));	
			} 
			else if (ctx.type_spec().getText().equals("float")) {
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.FLOAT, initVal(ctx, Type.FLOAT));	
			} 
			else {
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx, Type.INT));	
			}
			
		}
		else  { // simple decl
			
			if (ctx.type_spec().getText().equals("double")) {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.DOUBLE);
			}
			else if (ctx.type_spec().getText().equals("float")) {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.FLOAT);
			}
			else {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
			}
			
			
		}	
	}

	
	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {
		String classProlog = getFunProlog();
		
		String fun_decl = "", var_decl = "";
		
		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(isFunDecl(ctx, i))
				fun_decl += newTexts.get(ctx.decl(i));
			else
				var_decl += newTexts.get(ctx.decl(i));
		}
		
		newTexts.put(ctx, classProlog + var_decl + fun_decl);
		
		System.out.println(newTexts.get(ctx));
	}	
	
	
	// decl	: var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if(ctx.getChildCount() == 1)
		{
			if(ctx.var_decl() != null)				//var_decl
				decl += newTexts.get(ctx.var_decl());
			else							//fun_decl
				decl += newTexts.get(ctx.fun_decl());
		}
		newTexts.put(ctx, decl);
	}
	
	// stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() > 0)
		{
			if(ctx.expr_stmt() != null)				// expr_stmt
				stmt += newTexts.get(ctx.expr_stmt());
			else if(ctx.compound_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			// <(0) Fill here>	
			// same as the code above
			else if(ctx.if_stmt() != null)
				stmt += newTexts.get(ctx.if_stmt());
			else if(ctx.while_stmt() != null)
				stmt += newTexts.get(ctx.while_stmt());
			else if(ctx.for_stmt() != null)
				stmt += newTexts.get(ctx.for_stmt());
			else if(ctx.return_stmt() != null)
				stmt += newTexts.get(ctx.return_stmt());
			
	}
		newTexts.put(ctx, stmt);
	}
	
	// expr_stmt	: expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() == 2)
		{
			stmt += newTexts.get(ctx.expr());	// expr
		}
		newTexts.put(ctx, stmt);
	}
	
	
	// while_stmt	: WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
			// <(1) Fill here!>
		
		String stmt = "";
		String expr = newTexts.get(ctx.expr());
		String bodystmt = newTexts.get(ctx.stmt());
		
		String lend = symbolTable.newLabel(); //exit
		String lelse = symbolTable.newLabel(); //goto 
		
			
		if (ctx.getChildCount() == 5) {
			// while_stmt	: WHILE '(' expr ')' stmt
			
			stmt += lelse //spot l2
					+ ": \n" 
					+ expr //check
					+ "ifeq " 
					+ lend //exit if eq
					+ "\n" 
					+ bodystmt //do
					+ "goto " 
					+ lelse //goto l2
					+ "\n"
					+ lend // exit
					+ ": \n";
		}
		newTexts.put(ctx, stmt);
		newTexts.put(ctx, stmt);
	}
	
	// for_stmt	: FOR '(' cond_stmt ')' stmt
	@Override
	public void exitFor_stmt(MiniCParser.For_stmtContext ctx) {
			// <(1) Fill here!>
        MiniCParser.Cond_stmtContext condStmt = ctx.cond_stmt();

		String stmt = "";
		String initExpr = "";
		String condExpr = "";
		String incrExpr = "";
		
		int conds = 0;
		if (condStmt.getChild(0).getText().equals(";"))
			conds = 1;

		if (conds == 0)
			initExpr = newTexts.get(condStmt.expr(0-conds)) + "\n";
		condExpr = newTexts.get(condStmt.expr(1-conds)) + "\n";
		if (2-conds < condStmt.getChildCount())
			incrExpr = newTexts.get(condStmt.expr(2-conds)) + "\n";

		String bodystmt = newTexts.get(ctx.compound_stmt());
		
		String lend = symbolTable.newLabel(); //exit
		String lelse = symbolTable.newLabel(); //goto 
		
		// for_stmt	: FOR '(' cond_stmt ')' stmt
		stmt += initExpr
				+ lelse //spot l2
				+ ": \n" 
				+ condExpr //check
				+ "ifeq " 
				+ lend //exit if eq
				+ "\n" 
				+ bodystmt //do
				+ incrExpr
				+ "goto " 
				+ lelse //goto l2
				+ "\n"
				+ lend // exit
				+ ": \n";
		
		newTexts.put(ctx, stmt);
	}
	
	// type_spec IDENT '(' params ')' compound_stmt
	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
			// <(2) Fill here!>
		String stmt = "";
		
		if(ctx.getChildCount() == 6) {
			// type_spec IDENT '(' params ')' compound_stmt
			
			stmt += funcHeader(ctx, ctx.IDENT().getText())
//					".method public static " + symbolTable.getFunSpecStr(fname) + "\n"	
//					+ "\t" + ".limit stack " 	+ getStackSize(ctx) + "\n"
//					+ "\t" + ".limit locals " 	+ getLocalVarSize(ctx) + "\n";				
					
					+ newTexts.get(ctx.compound_stmt()) 
					//get { }
					+ ".end method\n\n";
		}
		newTexts.put(ctx, stmt);
	}
	

	private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) {
		return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"	
				+ "\t" + ".limit stack " 	+ getStackSize(ctx) + "\n"
				+ "\t" + ".limit locals " 	+ getLocalVarSize(ctx) + "\n";
				 	
	}
	
	
	
	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		String varDecl = "";
		
		if (isDeclWithInit(ctx)) {
			varDecl += "putfield " + varName + "\n";  
			// v. initialization => Later! skip now..: 
		}
		newTexts.put(ctx, varDecl);
	}
	
	
	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String varDecl = "";
		if(isDeclWithInit(ctx)) {
			if (ctx.getChild(0).getText().equals("int")) {
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx, Type.INT));
				String vId = symbolTable.getVarId(ctx);
				varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
						+ "istore_" + vId + "\n"; 			
			}
			else if (ctx.getChild(0).getText().equals("float")) {
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.FLOAT, initVal(ctx, Type.FLOAT));
				String vId = symbolTable.getVarId(ctx);
				varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
						+ "fstore_" + vId + "\n"; 			
			}
			else if (ctx.getChild(0).getText().equals("double")){
				symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.DOUBLE, initVal(ctx, Type.DOUBLE));
				String vId = symbolTable.getVarId(ctx);
				varDecl += "ldc " + ctx.LITERAL().getText() + "\n"
						+ "dstore_" + vId + "\n"; 			
			}
//			else {
//				System.out.println("<ERROR: init value not valid>");
//			}
		}
		else {
			if (ctx.type_spec().getText().equals("int")) {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
				String vId = symbolTable.getVarId(ctx);
				varDecl += "istore_" + vId + "\n";
			}
			else if (ctx.type_spec().getText().equals("float")) {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.FLOAT);
				String vId = symbolTable.getVarId(ctx);
				varDecl += "fstore_" + vId + "\n";
			}
			else if (ctx.type_spec().getText().equals("double")) {
				symbolTable.putLocalVar(getLocalVarName(ctx), Type.DOUBLE);
				String vId = symbolTable.getVarId(ctx);
				varDecl += "dstore_" + vId + "\n";
			}
		}
		
		newTexts.put(ctx, varDecl);
	}

	
	// compound_stmt	: '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// <(3) Fill here>
		String stmt  = "";
		for(int i = 0; i < ctx.local_decl().size(); i++) {
			stmt += newTexts.get(ctx.local_decl(i));
			//load locals
		}
		for(int i = 0; i < ctx.stmt().size(); i++) {
			stmt += newTexts.get(ctx.stmt(i));
			//do stuff
		}
		
		newTexts.put(ctx, stmt );
	}

	// if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());
		String thenStmt = newTexts.get(ctx.stmt(0));
		
		String lend = symbolTable.newLabel();
		String lelse = symbolTable.newLabel();
		
		
		if(noElse(ctx)) {		
			stmt += condExpr + "\n"
				+ "ifeq " + lend + "\n"
				+ thenStmt + "\n"
				+ lend + ":"  + "\n";	
		}
		else {
			String elseStmt = newTexts.get(ctx.stmt(1));
			stmt += condExpr + "\n"
					+ "ifeq " + lelse + "\n"
					+ thenStmt + "\n"
					+ "goto " + lend + "\n"
					+ lelse + ": " + elseStmt + "\n"
					+ lend + ":"  + "\n";	
		}
		
		newTexts.put(ctx, stmt);
	}
	
	
	// return_stmt	: RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
			// <(4) Fill here>
		if(isIntReturn(ctx)) {
			//only int return here
			
			if (symbolTable.getVarType(ctx.expr().getText()) == Type.DOUBLE) {
				String stmt = "";
				stmt += newTexts.get(ctx.expr());
				stmt += "dreturn\n";
				newTexts.put(ctx, stmt);
				
			} 
			else if (symbolTable.getVarType(ctx.expr().getText()) == Type.FLOAT) {
				String stmt = "";
				stmt += newTexts.get(ctx.expr());
				stmt += "freturn\n";
				newTexts.put(ctx, stmt);
				
			} 
			else {
				String stmt = "";
				stmt += newTexts.get(ctx.expr());
				stmt += "ireturn\n";
				newTexts.put(ctx, stmt);

			}
			
			//print ireturn after return expr
		}
//		else if(isDoubleReturn(ctx)){
//			String stmt = "";
//			stmt += newTexts.get(ctx.expr());
//			stmt += "dreturn\n";
//			newTexts.put(ctx, stmt);
//		}
		else if(isVoidReturn(ctx))
			newTexts.put(ctx, "return\n");
		//print return
	}

	
	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String expr = "";

		if(ctx.getChildCount() <= 0) {
			newTexts.put(ctx, ""); 
			return;
		}		
		
		if(ctx.getChildCount() == 1) { // IDENT | LITERAL
			
			if(ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();
				if(symbolTable.getVarType(idName) == Type.INT) {
					expr += "iload_" + symbolTable.getVarId(idName) + " \n";
				}
				else if(symbolTable.getVarType(idName) == Type.DOUBLE) {
					expr += "dload_" + symbolTable.getVarId(idName) + " \n";
				}
				else if(symbolTable.getVarType(idName) == Type.FLOAT) {
					expr += "fload_" + symbolTable.getVarId(idName) + " \n";
				}
				//else	// Type int array => Later! skip now..
				//	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
				
			} else if (ctx.LITERAL() != null) {
				String literalStr = ctx.LITERAL().getText();
				expr += "ldc " + literalStr + " \n";
			}
				
				
		} 
		else if(ctx.getChildCount() == 2) { // UnaryOperation
			expr += handleUnaryExpr(ctx, newTexts.get(ctx) + expr);			
		}
		else if(ctx.getChildCount() == 3) {	 
			if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
				expr += newTexts.get(ctx.expr(0));
				
			} else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
				
				String idName = ctx.IDENT().getText();
				
				if (symbolTable.getVarType(idName) == Type.DOUBLE) {
					expr += newTexts.get(ctx.expr(0))
							+ "dstore_" + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";
//					System.out.println(expr);
					
				} 
				else if (symbolTable.getVarType(idName) == Type.FLOAT) {
					expr += newTexts.get(ctx.expr(0))
							+ "fstore_" + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";
//					System.out.println(expr);
				}
				else {
					expr += newTexts.get(ctx.expr(0))
							+ "istore_" + symbolTable.getVarId(ctx.IDENT().getText()) + " \n";
//					System.out.println(expr);
				}
				
				
			} else { 											// binary operation
				expr += handleBinExpr(ctx, expr);
				
			}
		}
		// IDENT '(' args ')' |  IDENT '[' expr ']'
		else if(ctx.getChildCount() == 4) {
			if(ctx.args() != null){		// function calls
				expr += handleFunCall(ctx, expr);
			} else { // expr
				// Arrays: TODO  
				
				expr += "iload_" + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
						+ "iload_" + symbolTable.getVarId(ctx.expr(0).getText()) + "\n"
						+ "iaload" + "\n";
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays: TODO			*/
			expr += "iload_" + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
                    + newTexts.get(ctx.getChild(2))
                    + newTexts.get(ctx.getChild(5))
                    + "iastore" + "\n";
		}
		//int a[];
		//a[0] = 10;
		
		newTexts.put(ctx, expr);
	}


	private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();
		
		expr += newTexts.get(ctx.expr(0));
		
		String idName = ctx.IDENT().getText();
		//indent?
		
		if (symbolTable.getVarType(idName) == Type.DOUBLE) {
			
			switch(ctx.getChild(0).getText()) {
			
			
			case "-":
				expr += "           dneg \n"; break;
			case "--":
				expr += "ldc 1" + "\n"
						+ "dsub" + "\n";
				break;
			case "++":
				expr += "ldc 1" + "\n"
						+ "dadd" + "\n";
				break;
			case "!":
				expr += "ifeq " + l2 + "\n"
						+ l1 + ": " + "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
				
			}
		} else if (symbolTable.getVarType(idName) == Type.FLOAT) {
			
			switch(ctx.getChild(0).getText()) {
			
			
			case "-":
				expr += "           fneg \n"; break;
			case "--":
				expr += "ldc 1" + "\n"
						+ "fsub" + "\n";
				break;
			case "++":
				expr += "ldc 1" + "\n"
						+ "fadd" + "\n";
				break;
			case "!":
				expr += "ifeq " + l2 + "\n"
						+ l1 + ": " + "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
				
			}
		}
		else {
			switch(ctx.getChild(0).getText()) {
			
			
			case "-":
				expr += "           ineg \n"; break;
			case "--":
				expr += "ldc 1" + "\n"
						+ "isub" + "\n";
				break;
			case "++":
				expr += "ldc 1" + "\n"
						+ "iadd" + "\n";
				break;
			case "!":
				expr += "ifeq " + l2 + "\n"
						+ l1 + ": " + "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
				
			}
		}
		
		return expr;
	}


	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();
		
		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));
		
		String idName = "";
		if (ctx.expr(0).IDENT() != null) {
			 idName = ctx.expr(0).IDENT().getText();
		}
		
		//indent?
		
		if (symbolTable.getVarType(idName) == Type.DOUBLE) {
			switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "dmul \n"; break;
			case "/":
				expr += "ddiv \n"; break;
			case "%":
				expr += "drem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "dadd \n"; break;
			case "-":
				expr += "dsub \n"; break;
				
			case "==":
				expr += "dsub " + "\n"
						+ "ifeq l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "dsub " + "\n"
						+ "ifne l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<=":
				// <(5) Fill here>
				expr += "dsub " + "\n"
						+ "ifle " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
			case "<":
				// <(6) Fill here>
				expr += "dsub " + "\n"
						+ "iflt " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">=":
				// <(7) Fill here>
				expr += "dsub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";

				break;

			case ">":
				// <(8) Fill here>
				expr += "dsub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case "and":
				expr +=  "ifne "+ lend + "\n"
						+ "pop" + "\n" + "ldc 0" + "\n"
						+ lend + ": " + "\n"; break;
			case "or":
				// <(9) Fill here>
				//ldc 1
				expr +=  "ifeq "+ lend + "\n"
						+ "pop" + "\n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

		}
		} else if (symbolTable.getVarType(idName) == Type.FLOAT) {
			switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "fmul \n"; break;
			case "/":
				expr += "fdiv \n"; break;
			case "%":
				expr += "frem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "fadd \n"; break;
			case "-":
				expr += "fsub \n"; break;
				
			case "==":
				expr += "fsub " + "\n"
						+ "ifeq l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "fsub " + "\n"
						+ "ifne l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<=":
				// <(5) Fill here>
				expr += "fsub " + "\n"
						+ "ifle " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
			case "<":
				// <(6) Fill here>
				expr += "fsub " + "\n"
						+ "iflt " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">=":
				// <(7) Fill here>
				expr += "fsub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">":
				// <(8) Fill here>
				expr += "fsub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case "and":
				expr +=  "ifne "+ lend + "\n"
						+ "pop" + "\n" + "ldc 0" + "\n"
						+ lend + ": " + "\n"; break;
			case "or":
				// <(9) Fill here>
				//ldc 1
				expr +=  "ifeq "+ lend + "\n"
						+ "pop" + "\n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

		}
		}
		else {
			switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "imul \n"; break;
			case "/":
				expr += "idiv \n"; break;
			case "%":
				expr += "irem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "iadd \n"; break;
			case "-":
				expr += "isub \n"; break;
				
			case "==":
				expr += "isub " + "\n"
						+ "ifeq l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "isub " + "\n"
						+ "ifne l2"+ "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;
			case "<=":
				// <(5) Fill here>
				expr += "isub " + "\n"
						+ "ifle " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				
				break;
			case "<":
				// <(6) Fill here>
				expr += "isub " + "\n"
						+ "iflt " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">=":
				// <(7) Fill here>
				expr += "isub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";

				break;

			case ">":
				// <(8) Fill here>
				expr += "isub " + "\n"
						+ "ifge " + l2 + "\n"
						+ "ldc 0" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": \n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

			case "and":
				expr +=  "ifne "+ lend + "\n"
						+ "pop" + "\n" + "ldc 0" + "\n"
						+ lend + ": " + "\n"; break;
			case "or":
				// <(9) Fill here>
				//ldc 1
				expr +=  "ifeq "+ lend + "\n"
						+ "pop" + "\n" + "ldc 1" + "\n"
						+ lend + ": " + "\n";
				break;

		}
		}
		
		return expr;
	}
	private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
		String fname = getFunName(ctx);		

		if (fname.equals("_print")) {		// System.out.println	
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
			  		+ newTexts.get(ctx.args()) 
			  		+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {	
			expr = newTexts.get(ctx.args()) 
					+ "invokestatic " + getCurrentClassName()+ "/" + symbolTable.getFunSpecStr(fname) + "\n";
		}	
		
		return expr;
			
	}

	// args	: expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "\n";
		
		for (int i=0; i < ctx.expr().size() ; i++) {
			argsStr += newTexts.get(ctx.expr(i)) ; 
		}		
		newTexts.put(ctx, argsStr);
	}

	//for debug
	@Override 
	public void enterEveryRule(ParserRuleContext ctx) {

	}

	@Override 
	public void exitEveryRule(ParserRuleContext ctx) { 
		
	}

}
