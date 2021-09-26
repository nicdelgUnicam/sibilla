grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constant_declaration
        | parameter_declaration
        | type_declaration
        | agent_declaration
        | world_declaration
        | system_declaration;

//INITIALISATION PARAMETERS

constant_declaration: 'const' name=ID '=' expr ';';

parameter_declaration: 'param' name=ID '=' expr ';';

type_declaration:'type' name=ID '{'
                        type var_name=ID (';'type var_name=ID)*
                    '}'
                ;

//AGENT GRAMMAR

agent_declaration:
    'agent' name=ID '(' constr_params (','constr_params)* ')' '{'
    'state' '{'state_declaration (';' state_declaration)* '}'
    'observations' '{'observation_declaration (';' observation_declaration)* '}'
    'actions' '{' action_declaration ('|' action_declaration)*'}'
    'behaviour' '{'behaviour_declaration'}'
    '}'
    ;

constr_params: type? name=ID;

state_declaration: type name=ID ('<-' expr)? ;

observation_declaration: type name=ID ;

action_declaration: name=ID '{'
    action_body(';'action_body)*
    '}'
    ;

action_body: state_name=ID '<-' expr
           | agent_reference=ID'{'action_body'}'
           ;

behaviour_declaration: name=ID '{'
    (behaviour_rule)*
    def_behaviour_rule
    '}'
    ;

behaviour_rule:
    '['guardExpr=expr('|' guardExpr=expr)*']'
    '->'
    action_name=ID ':' times=expr ';';

def_behaviour_rule: 'default' action_name=ID ':' times=expr ';';

//WORLD GRAMMAR

world_declaration:
    'world' name=ID '(' constr_params (','constr_params)* ')' '{'
    'global' '{' global_state_declaration '}'
    'sensing' '{' sensing_declaration '}'
    'actions' '{' action_declaration '}'
    'environment' '{'ev_environment_declaration'}'
    '};'
    ;

global_state_declaration: '{'
    (global_field_declaration)+
    '};'
    ;

global_field_declaration:generic_field
                        |agent_field
                        ;

generic_field: type field_name=ID ';';

agent_field: agent_name=ID '{'
               type name_var=ID ';' (type name_var=ID';')*
             '};' ;

sensing_declaration:'sensing''{'
    (agent_sensing)*
    '}';

agent_sensing: agent_name=ID'{'
    (sensing_name=ID '=' expr )*
    '}' ;

ev_environment_declaration: 'environment' name=ID '=' '{'
    //(environment_rule)?
    def_environment_rule
    '};'
    ;

//environment_rule:;

def_environment_rule:'default' env_rule=ID ':' times=expr ';';

//SYSTEM CONFIG

system_declaration: 'system' name=ID '{'
    (assignment_declaration)?
    collective_declaration
    '}';

assignment_declaration:
    'let' name=ID '=' func
    ('and' name=ID '=' func)*
    'in'
    ;

collective_declaration:
    collective_name=ID ('{'collective_body'}')*;

collective_body: collection_name=ID '<-' expr ';'
               | collection_name=ID '{'collective_body*'}'
               | 'for' name=ID 'in' group_name=ID '{'collective_body'}'
            //   | 'if' expr_bool=expr '{'collective_body'}'('if' expr_bool=expr '{'collective_body'}')* ('else''{'collective_body'}')?
               ;

//UTIL

expr    : INTEGER                                                   # integerValue
        | REAL                                                      # realValue
        | 'false'                                                   # false
        | 'true'                                                    # true
        | reference=ID                                              # reference
        | '(' expr ')'                                              # exprBrackets
      //  | gexpr                                                     # gexprCall
        | leftOp=expr oper=('+'|'-') rightOp=expr                   # addsubOperation
        | leftOp=expr oper=('*'|'/') rightOp=expr                   # multdivOperation
        | leftOp=expr oper=('%'|'//') rightOp=expr                  # additionalOperation
        | leftOp=expr '^' rightOp=expr                              # exponentOperation
        | '!' argument=expr                                         # negation
        | leftOp=expr oper=('&'|'&&') rightOp=expr                  # andExpression
        | leftOp=expr oper=('|'|'||') rightOp=expr                  # orExpression
        | leftOp=expr oper=('<'|'<='|'=='|'>='|'>') rightOp=expr    # relationExpression
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr    # ifthenelseExpression
        | '[' fieldAssignment (',' fieldAssignment)* ']'            # recordExpression
        | 'U''['min=expr',' max=expr']'                             # weightedRandomExpression
        | 'rnd'                                                     # randomExpression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall' name=ID 'in' group_name=ID ':' expr              # forallExpr
        | 'exists' name=ID 'in' group_name=ID ':' expr              # existsExpr
        | 'min'    name=ID 'in' group_name=ID ':' expr              # minimumExpr
        | 'max'    name=ID 'in' group_name=ID ':' expr              # maximumExpr
        | 'it.' ID                                                  # itself
        ;

fieldAssignment : name=ID '=' expr;
/*
gexpr   : expr                                                      # expression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall' name=ID 'in' group_name=ID ':' gexpr             # forallExpr
        | 'exists' name=ID 'in' group_name=ID ':' gexpr             # existsExpr
        | 'min'    name=ID 'in' group_name=ID ':' gexpr             # minimumExpr
        | 'max'    name=ID 'in' group_name=ID ':' gexpr             # maximumExpr
        | 'it.' ID                                                  # itself
        ;
*/

type    : 'int'                                                     # integerNumber
        | 'double'                                                  # doubleNumber
        | 'real'                                                    # realNumber
        | 'bool'                                                    # boolean
        | 'char'                                                    # character
        | 'String'                                                  # string
        | 'array[' type (',' type)* ']'                             # arrayMultipleTypes
      //  | type_declaration                                          # newType
        ;

func    : 'generate''('')'
        | 'distinct' '('
            init_number=expr ',' '[' name_value=ID '=' expr (',' name_value=ID '=' expr)* ']'
            ')'
        | 'distinctFrom''('
            init_number=expr ',' '[' name_value=ID '=' expr (',' name_value=ID '=' expr)* ']' ','  name=ID
            ')'
        ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);