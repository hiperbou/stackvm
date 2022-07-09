grammar vm;

// A program is a sequence of lines
program: line*;

// A line is either a label, or an instruction, followed by a newline
line: (label | instruction | emptyLine) NEWLINE;

emptyLine: ;

// Labels are simply identifiers, followed by colons
label: IDENTIFIER ':';

// An instruction can be of many kinds
instruction: nop |
             halt |
             push |
             pop |
             dup |

             add |
             sub |
             mul |
             div |
             mod |
             min |
             max |

             not |
             b_not |
             abs |

             and |
             or |
             b_and |
             b_or |
             b_xor |

             eq |
             ne |
             gte |
             lte |
             gt |
             lt |

             jmp |
             jif |

             load |
             store |

             gload |
             gstore |

             read |
             write |

             call |
             ret
             ;
nop: 'NOP';
halt: 'HALT';
push: 'PUSH' NUMBER;
pop: 'POP';
dup: 'DUP';

add: 'ADD';
sub: 'SUB';
mul: 'MUL';
div: 'DIV';
mod: 'MOD';
min: 'MIN';
max: 'MAX';

not: 'NOT';
b_not: 'B_NOT';
abs: 'ABS';

and: 'AND';
or: 'OR';
b_and: 'B_AND';
b_or: 'B_OR';
b_xor: 'B_XOR';

eq: 'EQ';
ne: 'NE';
gte: 'GTE';
lte: 'LTE';
gt: 'GT';
lt: 'LT';

jmp: 'JMP' IDENTIFIER;
jif: 'JIF' IDENTIFIER;

load: 'LOAD' NUMBER;
store: 'STORE' NUMBER;

gload: 'GLOAD' NUMBER;
gstore: 'GSTORE' NUMBER;

read: 'READ' NUMBER;
write: 'WRITE' NUMBER;

call: 'CALL' IDENTIFIER;
ret: 'RET';


IDENTIFIER: [a-zA-Z][a-zA-Z0-9_]*;
NUMBER: '-'*[0-9]+;
NEWLINE: '\r'? '\n';

// Skip all whitespaces
WHITESPACE: [ \t]+ -> skip;

// Comments
COMMENT: '//' ~('\r' | '\n')* -> skip;