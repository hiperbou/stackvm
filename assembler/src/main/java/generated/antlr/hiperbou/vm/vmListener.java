// Generated from D:/hiperbou/stackVM/assembler/src/main/antlr\vm.g4 by ANTLR 4.10.1
package generated.antlr.hiperbou.vm;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link vmParser}.
 */
public interface vmListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link vmParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(vmParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(vmParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(vmParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(vmParser.LineContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#emptyLine}.
	 * @param ctx the parse tree
	 */
	void enterEmptyLine(vmParser.EmptyLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#emptyLine}.
	 * @param ctx the parse tree
	 */
	void exitEmptyLine(vmParser.EmptyLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(vmParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(vmParser.LabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#instruction}.
	 * @param ctx the parse tree
	 */
	void enterInstruction(vmParser.InstructionContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#instruction}.
	 * @param ctx the parse tree
	 */
	void exitInstruction(vmParser.InstructionContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#nop}.
	 * @param ctx the parse tree
	 */
	void enterNop(vmParser.NopContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#nop}.
	 * @param ctx the parse tree
	 */
	void exitNop(vmParser.NopContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#halt}.
	 * @param ctx the parse tree
	 */
	void enterHalt(vmParser.HaltContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#halt}.
	 * @param ctx the parse tree
	 */
	void exitHalt(vmParser.HaltContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#push}.
	 * @param ctx the parse tree
	 */
	void enterPush(vmParser.PushContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#push}.
	 * @param ctx the parse tree
	 */
	void exitPush(vmParser.PushContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#pop}.
	 * @param ctx the parse tree
	 */
	void enterPop(vmParser.PopContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#pop}.
	 * @param ctx the parse tree
	 */
	void exitPop(vmParser.PopContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#dup}.
	 * @param ctx the parse tree
	 */
	void enterDup(vmParser.DupContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#dup}.
	 * @param ctx the parse tree
	 */
	void exitDup(vmParser.DupContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#add}.
	 * @param ctx the parse tree
	 */
	void enterAdd(vmParser.AddContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#add}.
	 * @param ctx the parse tree
	 */
	void exitAdd(vmParser.AddContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#sub}.
	 * @param ctx the parse tree
	 */
	void enterSub(vmParser.SubContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#sub}.
	 * @param ctx the parse tree
	 */
	void exitSub(vmParser.SubContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#mul}.
	 * @param ctx the parse tree
	 */
	void enterMul(vmParser.MulContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#mul}.
	 * @param ctx the parse tree
	 */
	void exitMul(vmParser.MulContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#div}.
	 * @param ctx the parse tree
	 */
	void enterDiv(vmParser.DivContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#div}.
	 * @param ctx the parse tree
	 */
	void exitDiv(vmParser.DivContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#mod}.
	 * @param ctx the parse tree
	 */
	void enterMod(vmParser.ModContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#mod}.
	 * @param ctx the parse tree
	 */
	void exitMod(vmParser.ModContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#min}.
	 * @param ctx the parse tree
	 */
	void enterMin(vmParser.MinContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#min}.
	 * @param ctx the parse tree
	 */
	void exitMin(vmParser.MinContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#max}.
	 * @param ctx the parse tree
	 */
	void enterMax(vmParser.MaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#max}.
	 * @param ctx the parse tree
	 */
	void exitMax(vmParser.MaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(vmParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(vmParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#b_not}.
	 * @param ctx the parse tree
	 */
	void enterB_not(vmParser.B_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#b_not}.
	 * @param ctx the parse tree
	 */
	void exitB_not(vmParser.B_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#abs}.
	 * @param ctx the parse tree
	 */
	void enterAbs(vmParser.AbsContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#abs}.
	 * @param ctx the parse tree
	 */
	void exitAbs(vmParser.AbsContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(vmParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(vmParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(vmParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(vmParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#b_and}.
	 * @param ctx the parse tree
	 */
	void enterB_and(vmParser.B_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#b_and}.
	 * @param ctx the parse tree
	 */
	void exitB_and(vmParser.B_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#b_or}.
	 * @param ctx the parse tree
	 */
	void enterB_or(vmParser.B_orContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#b_or}.
	 * @param ctx the parse tree
	 */
	void exitB_or(vmParser.B_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#b_xor}.
	 * @param ctx the parse tree
	 */
	void enterB_xor(vmParser.B_xorContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#b_xor}.
	 * @param ctx the parse tree
	 */
	void exitB_xor(vmParser.B_xorContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#eq}.
	 * @param ctx the parse tree
	 */
	void enterEq(vmParser.EqContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#eq}.
	 * @param ctx the parse tree
	 */
	void exitEq(vmParser.EqContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#ne}.
	 * @param ctx the parse tree
	 */
	void enterNe(vmParser.NeContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#ne}.
	 * @param ctx the parse tree
	 */
	void exitNe(vmParser.NeContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#gte}.
	 * @param ctx the parse tree
	 */
	void enterGte(vmParser.GteContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#gte}.
	 * @param ctx the parse tree
	 */
	void exitGte(vmParser.GteContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#lte}.
	 * @param ctx the parse tree
	 */
	void enterLte(vmParser.LteContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#lte}.
	 * @param ctx the parse tree
	 */
	void exitLte(vmParser.LteContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#gt}.
	 * @param ctx the parse tree
	 */
	void enterGt(vmParser.GtContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#gt}.
	 * @param ctx the parse tree
	 */
	void exitGt(vmParser.GtContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#lt}.
	 * @param ctx the parse tree
	 */
	void enterLt(vmParser.LtContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#lt}.
	 * @param ctx the parse tree
	 */
	void exitLt(vmParser.LtContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#jmp}.
	 * @param ctx the parse tree
	 */
	void enterJmp(vmParser.JmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#jmp}.
	 * @param ctx the parse tree
	 */
	void exitJmp(vmParser.JmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#jif}.
	 * @param ctx the parse tree
	 */
	void enterJif(vmParser.JifContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#jif}.
	 * @param ctx the parse tree
	 */
	void exitJif(vmParser.JifContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#load}.
	 * @param ctx the parse tree
	 */
	void enterLoad(vmParser.LoadContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#load}.
	 * @param ctx the parse tree
	 */
	void exitLoad(vmParser.LoadContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#store}.
	 * @param ctx the parse tree
	 */
	void enterStore(vmParser.StoreContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#store}.
	 * @param ctx the parse tree
	 */
	void exitStore(vmParser.StoreContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(vmParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(vmParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#ret}.
	 * @param ctx the parse tree
	 */
	void enterRet(vmParser.RetContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#ret}.
	 * @param ctx the parse tree
	 */
	void exitRet(vmParser.RetContext ctx);
}