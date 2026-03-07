package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.ast.BinaryOperator
import com.hiperbou.vm.ccompiler.ast.UnaryOperator
import com.hiperbou.vm.ccompiler.lexer.CLexer
import com.hiperbou.vm.ccompiler.parser.CParser
import com.hiperbou.vm.ccompiler.parser.ParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParserTest {

    private fun parse(source: String): AstNode.Program {
        val tokens = CLexer(source).tokenize()
        return CParser(tokens).parse()
    }

    @Test
    fun parseMinimalMainFunctionTest() {
        val program = parse("int main() { return 0; }")
        assertEquals(1, program.functions.size)
        assertEquals(0, program.globals.size)
        val fn = program.functions[0]
        assertEquals("main", fn.name)
        assertEquals(0, fn.params.size)
        assertEquals(1, fn.body.statements.size)
    }

    @Test
    fun parseGlobalDeclarationBeforeFunctionTest() {
        val program = parse("int g = 7; int main() { return g; }")
        assertEquals(1, program.globals.size)
        assertEquals("g", program.globals[0].name)
        assertEquals(1, program.functions.size)
    }

    @Test
    fun parsePrintStatementTest() {
        val program = parse("int main() { print(42); return 0; }")
        val printStmt = program.functions[0].body.statements[0]
        assertIs<AstNode.PrintStatement>(printStmt)
        assertEquals(42, ((printStmt as AstNode.PrintStatement).expr as AstNode.NumberLiteral).value)
    }
    @Test
    fun parseDebugprintStatementTest() {
        val program = parse("int main() { debugPrint(7); return 0; }")
        val stmt = program.functions[0].body.statements[0]
        assertIs<AstNode.DebugPrintStatement>(stmt)
    }
    @Test
    fun parseVarDeclWithoutInitializerTest() {
        val program = parse("int main() { int x; return 0; }")
        val decl = program.functions[0].body.statements[0] as AstNode.VarDecl
        assertEquals("x", decl.name)
        assertNull(decl.initializer)
    }

    @Test
    fun multiplicationHasHigherPrecedenceThanAdditionTest() {
        val program = parse("int main() { int x = 3 + 4 * 2; return 0; }")
        val expr = (program.functions[0].body.statements[0] as AstNode.VarDecl).initializer as AstNode.BinaryOp
        assertEquals(BinaryOperator.ADD, expr.op)
        val right = expr.right as AstNode.BinaryOp
        assertEquals(BinaryOperator.MUL, right.op)
    }
    @Test
    fun bitwisePrecedenceAndUnaryBitwiseNotTest() {
        val program = parse("int main() { int x = ~1 | 2 & 3 ^ 4; return 0; }")
        val init = (program.functions[0].body.statements[0] as AstNode.VarDecl).initializer as AstNode.BinaryOp
        assertEquals(BinaryOperator.BIT_OR, init.op)
        assertIs<AstNode.UnaryOp>(init.left)
        assertEquals(UnaryOperator.BIT_NOT, (init.left as AstNode.UnaryOp).op)
    }
    @Test
    fun parseIfElseStatementTest() {
        val program = parse("int main() { if (1) { print(1); } else { print(0); } return 0; }")
        val ifStmt = program.functions[0].body.statements[0] as AstNode.IfStatement
        assertNotNull(ifStmt.elseBlock)
    }

    @Test
    fun parseWhileStatementTest() {
        val program = parse("int main() { while (1) { print(1); } return 0; }")
        assertIs<AstNode.WhileStatement>(program.functions[0].body.statements[0])
    }

    @Test
    fun parseForStatementTest() {
        val program = parse("int main() { for (int i = 0; i < 10; i++) { print(i); } return 0; }")
        val forStmt = program.functions[0].body.statements[0] as AstNode.ForStatement
        assertNotNull(forStmt.init)
        assertNotNull(forStmt.condition)
        assertNotNull(forStmt.update)
    }
    
    @Test
    fun parseForStatementWithEmptyPartsTest() {
        val program = parse("int main() { for (;;) { break; } return 0; }")
        val forStmt = program.functions[0].body.statements[0] as AstNode.ForStatement
        assertNull(forStmt.init)
        assertNull(forStmt.condition)
        assertNull(forStmt.update)
    }

    @Test
    fun parseDoBlockScopeStatementTest() {
        val program = parse("int main() { do { int x = 1; print(x); } return 0; }")
        val doStmt = program.functions[0].body.statements[0]
        assertIs<AstNode.DoStatement>(doStmt)
        assertEquals(2, (doStmt as AstNode.DoStatement).body.statements.size)
    }

    @Test
    fun parseDoWhileStatementTest() {
        val program = parse("int main() { do { print(1); } while (0); return 0; }")
        val doWhileStmt = program.functions[0].body.statements[0]
        assertIs<AstNode.DoWhileStatement>(doWhileStmt)
    }

    @Test
    fun parseBreakStatementTest() {
        val program = parse("int main() { while (1) { break; } return 0; }")
        val whileStmt = program.functions[0].body.statements[0] as AstNode.WhileStatement
        assertIs<AstNode.BreakStatement>(whileStmt.body.statements[0])
    }

    @Test
    fun parseContinueStatementTest() {
        val program = parse("int main() { while (1) { continue; } return 0; }")
        val whileStmt = program.functions[0].body.statements[0] as AstNode.WhileStatement
        assertIs<AstNode.ContinueStatement>(whileStmt.body.statements[0])
    }

    @Test
    fun parseTernaryExpressionTest() {
        val program = parse("int main() { int x = 1 ? 10 : 20; return 0; }")
        val init = (program.functions[0].body.statements[0] as AstNode.VarDecl).initializer
        assertIs<AstNode.TernaryOp>(init)
    }
    
    @Test
    fun parseMultiVariableDeclarationTest() {
        val program = parse("int main() { int a = 1, b = 2, c; return 0; }")
        val stmt = program.functions[0].body.statements[0]
        assertIs<AstNode.VarDeclList>(stmt)
        assertEquals(3, (stmt as AstNode.VarDeclList).declarations.size)
    }

    @Test
    fun parseArrayDeclarationAndIndexAssignmentTest() {
        val program = parse("int main() { int arr[3]; arr[1] = 7; return arr[1]; }")
        val body = program.functions[0].body.statements
        assertIs<AstNode.ArrayDecl>(body[0])
        assertIs<AstNode.ArrayAssignStatement>(body[1])
    }    @Test
    fun parseArrayPostfixIncrementExpressionTest() {
        val program = parse("int main() { int arr[2]; arr[0]++; return 0; }")
        val exprStmt = program.functions[0].body.statements[1] as AstNode.ExpressionStatement
        assertIs<AstNode.PostIncDecArray>(exprStmt.expr)
    }
    @Test
    fun parseSwitchStatementWithCaseAndDefaultTest() {
        val program = parse("int main() { switch (x) { case 1: print(1); break; default: print(0); } return 0; }")
        val switchStmt = program.functions[0].body.statements[0]
        assertIs<AstNode.SwitchStatement>(switchStmt)
        switchStmt as AstNode.SwitchStatement
        assertEquals(1, switchStmt.cases.size)
        assertNotNull(switchStmt.defaultStatements)
    }
    @Test
    fun missingClosingBraceThrowsParseexceptionTest() {
        assertFailsWith<ParseException> {
            parse("int main() { return 0;")
        }
    }

    @Test
    fun missingSemicolonThrowsParseexceptionTest() {
        assertFailsWith<ParseException> {
            parse("int main() { return 0 }")
        }
    }
}

