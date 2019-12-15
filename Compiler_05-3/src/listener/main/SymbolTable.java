package listener.main;

import java.util.HashMap;
import java.util.Map;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.Var_declContext;
import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR, DOUBLE ,FLOAT
		//double ,,,
	}
	
	static public class VarInfo {
		Type type; 
		int id;
		Object initVal;
		
		public VarInfo(Type type,  int id, Object object) {
			this.type = type;
			this.id = id;
			this.initVal = object;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}
	
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function 
	
		
	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;		
	}
	
	void putLocalVar(String varname, Type type){
		//<Fill here 0>
		//hash as (name, type)
		this._lsymtable.put(varname, new VarInfo(type, _localVarID++));
	}
	
	void putGlobalVar(String varname, Type type){
		//<Fill here 1>
		//hash as (name, type)
		this._gsymtable.put(varname, new VarInfo(type, _globalVarID++));
	}
	
	void putLocalVarWithInitVal(String varname, Type type, Object object){
		//<Fill here 2>
		//hash as (name, type) with init value
		this._lsymtable.put(varname, new VarInfo(type, _localVarID++, object));
	}
	void putGlobalVarWithInitVal(String varname, Type type, Object object){
		//<Fill here 3>
		//hash as (name, type) with init value
		this._gsymtable.put(varname, new VarInfo(type, _globalVarID++, object));
	
	}
	
	void putParams(MiniCParser.ParamsContext params) {
		for(int i = 0; i < params.param().size(); i++) {
		//<Fill here 4>
			//for each parameters
			String varname = getParamName(params.param(i));
			Type type = getParamType(params.param(i));
			//hash as (name, type)
			this._lsymtable.put(varname, new VarInfo(type, _localVarID++));

		}
	}
	
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}
	
	public String getFunSpecStr(String fname) {		
		// <Fill here 5>
		//add / _print
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here 6>	
		//identity of function spec
		return getFunSpecStr(ctx.IDENT().getText());
	}
	
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";	
		String rtype = "";
		String res = "";
		
		// <Fill here 7>	
		
		if(ctx.type_spec().getText().equals("void"))
			rtype += "V";//add()V
		else if (ctx.type_spec().getText().equals("double"))
			rtype += "D";
		else if (ctx.type_spec().getText().equals("float"))
			rtype += "F";
		else
			rtype += "I";//add()I

		
		for(int i = 0; i < ctx.params().param().size(); i++) {
			if(ctx.params().param(i).type_spec().getText().equals("void"))
				argtype += "V";//add(V)
			else if (ctx.params().param(i).type_spec().getText().equals("double"))
				argtype += "D";
			else if (ctx.type_spec().getText().equals("float"))
				rtype += "F";
			
			else//only void or integers at this time
				argtype += "I";//add(I)
		}
		res =  fname + "(" + argtype + ")" + rtype; 
		FInfo finfo = new FInfo();//
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);
		
		return res;
	}
	
	String getVarId(String name){
		// <Fill here 8>	
		if(_lsymtable.containsKey(name)) //hashmap search in locals
			return Integer.toString(_lsymtable.get(name).id);
		else if(_gsymtable.containsKey(name)) //hashmap search in globals
			return Integer.toString(_gsymtable.get(name).id);
		return "";//return nothing if string not in either table
	}
	
	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}
		
		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}
		
		return Type.ERROR;	
	}
	String newLabel() {
		return "label" + _labelID++;
	}
	
	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here 9>
		//same as locals
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}
	
}
