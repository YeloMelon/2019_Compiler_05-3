grammar MiniC;


@header { 
package generated;
}
program	: decl+			;
decl		: var_decl		
		| fun_decl		;
var_decl	:  type_spec IDENT ';' 
		| type_spec IDENT '=' LITERAL ';'	
		| type_spec IDENT '[' LITERAL ']' ';'	;
type_spec	: VOID				
		| DOUBLE
		| FLOAT		
		| INT		;
fun_decl	: type_spec IDENT '(' params ')' compound_stmt ;
params		: param (',' param)*		
		| VOID				
		|
			;
param		: type_spec IDENT		
		| type_spec IDENT '[' ']'	;
stmt		: expr_stmt			
		| compound_stmt			
		| if_stmt			
		| while_stmt			
		| for_stmt
		| return_stmt			;
expr_stmt	: expr ';'			;
while_stmt	: WHILE '(' expr ')' stmt	;
//for_stmt	: FOR '(' type_spec? expr ';' expr ';' expr ')' stmt	;
for_stmt : FOR '(' cond_stmt ')' compound_stmt       ;
cond_stmt : expr? ';' expr ';' expr?	;
//       | ';' expr ';' expr
//       | ';' expr ';'
//       | expr ';' expr ';';
compound_stmt: '{' local_decl* stmt* '}'	;
local_decl	: type_spec IDENT ';'
		| type_spec IDENT '=' LITERAL ';'	
		| type_spec IDENT '[' LITERAL ']' ';'	;
if_stmt		: IF '(' expr ')' stmt		
		| IF '(' expr ')' stmt ELSE stmt 		;
return_stmt	: RETURN ';'			
		| RETURN expr ';'				;
expr	:  LITERAL				
	| '(' expr ')'				 
	| IDENT				 
	| IDENT '[' expr ']'			 
	| IDENT '(' args ')'			
	| '-' expr				 
	| '+' expr				 
	| '--' expr				 
	| '++' expr				 
	| expr '*' expr				 
	| expr '/' expr				 
	| expr '%' expr				 
	| expr '+' expr				 
	| expr '-' expr				 
	| expr EQ expr				
	| expr NE expr				 
	| expr LE expr				 
	| expr '<' expr				 
	| expr GE expr				 
	| expr '>' expr				 
	| '!' expr					 
	| expr AND expr				 
	| expr OR expr		
	| IDENT '=' expr		
//	| type_spec IDENT '=' expr
	| IDENT '[' expr ']' '=' expr		;
args	: expr (',' expr)*			 
	|					 ;

VOID: 'void';
INT: 'int';
DOUBLE: 'double';
FLOAT: 'float';

WHILE: 'while';
FOR: 'for';
IF: 'if';
ELSE: 'else';
RETURN: 'return';
OR: 'or';
AND: 'and';
LE: '<=';
GE: '>=';
EQ: '==';
NE: '!=';

IDENT  : [a-zA-Z_]
        (   [a-zA-Z_]
        |  [0-9]
        )*;


LITERAL:	FloatConstant	|	DecimalConstant	|	OctalConstant	|	HexadecimalConstant	;

FloatConstant
 	: [0-9]+ '.' [0-9]+ 
 	;
 	
DecimalConstant
    :   '0'
	|   [1-9] [0-9]*
    ;

OctalConstant
    :   '0'[0-7]*
    ;

HexadecimalConstant
    :   '0' [xX] [0-9a-fA-F] +
    ;
    

    

WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
	-> channel(HIDDEN)	 
    ;
