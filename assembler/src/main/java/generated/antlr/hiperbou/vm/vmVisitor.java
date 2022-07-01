// Generated from D:/hiperbou/StackVMasm/src/main/antlr\vm.g4 by ANTLR 4.10.1
package generated.antlr.hiperbou.vm;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link vmParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface vmVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link vmParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(vmParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLine(vmParser.LineContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#emptyLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyLine(vmParser.EmptyLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(vmParser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#instruction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstruction(vmParser.InstructionContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#halt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHalt(vmParser.HaltContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#push}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPush(vmParser.PushContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#add}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd(vmParser.AddContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#sub}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSub(vmParser.SubContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#mul}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMul(vmParser.MulContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#div}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiv(vmParser.DivContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(vmParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(vmParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(vmParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#pop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPop(vmParser.PopContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#dup}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDup(vmParser.DupContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#iseq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIseq(vmParser.IseqContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#isge}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsge(vmParser.IsgeContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#isgt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsgt(vmParser.IsgtContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#jmp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJmp(vmParser.JmpContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#jif}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJif(vmParser.JifContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#load}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoad(vmParser.LoadContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#store}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStore(vmParser.StoreContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(vmParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link vmParser#ret}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRet(vmParser.RetContext ctx);
}