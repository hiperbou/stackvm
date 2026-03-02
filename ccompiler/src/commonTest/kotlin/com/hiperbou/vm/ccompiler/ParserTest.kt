package com.hiperbou.vm.ccompiler

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.ast.BinaryOperator
import com.hiperbou.vm.ccompiler.lexer.CLexer
import com.hiperbou.vm.ccompiler.parser.CParser
import com.hiperbou.vm.ccompiler.parser.ParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class ParserTest {

    private fun parse(source: String): AstNode.Program {
        val tokens = CLexer(source).tokenize()
        return CParser(tokens).parse()
    }

    // -------------------------------------------------------------------------
    // Phase 1: minimal main + print + return
    // -------------------------------------------------------------------------

    @Test
    fun `parse minimal main function`() {
        val program = parse("int main() { return 0; }")
        assertEquals(1, program.functions.size)
        val fn = program.functions[0]
        assertEquals("main", fn.name)
        assertEquals(0, fn.params.size)
        assertEquals(1, fn.body.statements.size)
    }

    @Test
    fun `parse print statement`() {
        val program = parse("int main() { print(42); return 0; }")
        val fn = program.functions[0]
        assertEquals(2, fn.body.statements.size)
        val printStmt = fn.body.statements[0]
        assertIs<AstNode.PrintStatement>(printStmt)
        assertIs<AstNode.NumberLiteral>((printStmt as AstNode.PrintStatement).expr)
        assertEquals(42, (printStmt.expr as AstNode.NumberLiteral).value)
    }

    @Test
    fun `parse return statement`() {
        val program = parse("int main() { return 0; }")
        val fn = program.functions[0]
        val ret = fn.body.statements[0]
        assertIs<AstNode.ReturnStatement>(ret)
        assertIs<AstNode.NumberLiteral>((ret as AstNode.ReturnStatement).expr)
        assertEquals(0, (ret.expr as AstNode.NumberLiteral).value)
    }

    // -------------------------------------------------------------------------
    // Phase 2: local variables
    // -------------------------------------------------------------------------

    @Test
    fun `parse var decl with initializer`() {
        val program = parse("int main() { int x = 10; return 0; }")
        val fn = program.functions[0]
        val decl = fn.body.statements[0]
        assertIs<AstNode.VarDecl>(decl)
        assertEquals("x", (decl as AstNode.VarDecl).name)
        assertIs<AstNode.NumberLiteral>(decl.initializer)
        assertEquals(10, (decl.initializer as AstNode.NumberLiteral).value)
    }

    @Test
    fun `parse var decl without initializer`() {
        val program = parse("int main() { int x; return 0; }")
        val fn = program.functions[0]
        val decl = fn.body.statements[0] as AstNode.VarDecl
        assertEquals("x", decl.name)
        assertNull(decl.initializer)
    }

    // -------------------------------------------------------------------------
    // Phase 3: arithmetic expressions
    // -------------------------------------------------------------------------

    @Test
    fun `parse addition expression`() {
        val program = parse("int main() { int x = 3 + 4; return 0; }")
        val decl = program.functions[0].body.statements[0] as AstNode.VarDecl
        val expr = decl.initializer
        assertIs<AstNode.BinaryOp>(expr)
        assertEquals(BinaryOperator.ADD, (expr as AstNode.BinaryOp).op)
        assertEquals(3, (expr.left as AstNode.NumberLiteral).value)
        assertEquals(4, (expr.right as AstNode.NumberLiteral).value)
    }

    @Test
    fun `multiplication has higher precedence than addition`() {
        val program = parse("int main() { int x = 3 + 4 * 2; return 0; }")
        val decl = program.functions[0].body.statements[0] as AstNode.VarDecl
        val expr = decl.initializer as AstNode.BinaryOp
        // Should be: 3 + (4 * 2)
        assertEquals(BinaryOperator.ADD, expr.op)
        assertIs<AstNode.NumberLiteral>(expr.left)
        assertEquals(3, (expr.left as AstNode.NumberLiteral).value)
        val right = expr.right as AstNode.BinaryOp
        assertEquals(BinaryOperator.MUL, right.op)
        assertEquals(4, (right.left as AstNode.NumberLiteral).value)
        assertEquals(2, (right.right as AstNode.NumberLiteral).value)
    }

    @Test
    fun `parentheses override precedence`() {
        val program = parse("int main() { int x = (3 + 4) * 2; return 0; }")
        val decl = program.functions[0].body.statements[0] as AstNode.VarDecl
        val expr = decl.initializer as AstNode.BinaryOp
        // Should be: (3 + 4) * 2
        assertEquals(BinaryOperator.MUL, expr.op)
        val left = expr.left as AstNode.BinaryOp
        assertEquals(BinaryOperator.ADD, left.op)
    }

    // -------------------------------------------------------------------------
    // Phase 4: if/else
    // -------------------------------------------------------------------------

    @Test
    fun `parse if statement without else`() {
        val program = parse("int main() { if (1) { print(1); } return 0; }")
        val fn = program.functions[0]
        val ifStmt = fn.body.statements[0]
        assertIs<AstNode.IfStatement>(ifStmt)
        assertNull((ifStmt as AstNode.IfStatement).elseBlock)
    }

    @Test
    fun `parse if-else statement`() {
        val program = parse("int main() { if (1) { print(1); } else { print(0); } return 0; }")
        val fn = program.functions[0]
        val ifStmt = fn.body.statements[0] as AstNode.IfStatement
        assertNotNull(ifStmt.elseBlock)
    }

    // -------------------------------------------------------------------------
    // Phase 5: while loop
    // -------------------------------------------------------------------------

    @Test
    fun `parse while statement`() {
        val program = parse("int main() { while (1) { print(1); } return 0; }")
        val fn = program.functions[0]
        val whileStmt = fn.body.statements[0]
        assertIs<AstNode.WhileStatement>(whileStmt)
    }

    // -------------------------------------------------------------------------
    // Phase 6: for loop
    // -------------------------------------------------------------------------

    @Test
    fun `parse for statement`() {
        val program = parse("int main() { for (int i = 0; i < 10; i++) { print(i); } return 0; }")
        val fn = program.functions[0]
        val forStmt = fn.body.statements[0]
        assertIs<AstNode.ForStatement>(forStmt)
        assertNotNull((forStmt as AstNode.ForStatement).init)
        assertNotNull(forStmt.condition)
        assertNotNull(forStmt.update)
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    fun `missing closing brace throws ParseException`() {
        assertFailsWith<ParseException> {
            parse("int main() { return 0;")
        }
    }

    @Test
    fun `missing semicolon throws ParseException`() {
        assertFailsWith<ParseException> {
            parse("int main() { return 0 }")
        }
    }
}
