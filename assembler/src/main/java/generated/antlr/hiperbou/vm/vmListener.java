// Generated from D:/hiperbou/StackVMasm/src/main/antlr\vm.g4 by ANTLR 4.10.1
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
	 * Enter a parse tree produced by {@link vmParser#iseq}.
	 * @param ctx the parse tree
	 */
	void enterIseq(vmParser.IseqContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#iseq}.
	 * @param ctx the parse tree
	 */
	void exitIseq(vmParser.IseqContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#isge}.
	 * @param ctx the parse tree
	 */
	void enterIsge(vmParser.IsgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#isge}.
	 * @param ctx the parse tree
	 */
	void exitIsge(vmParser.IsgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link vmParser#isgt}.
	 * @param ctx the parse tree
	 */
	void enterIsgt(vmParser.IsgtContext ctx);
	/**
	 * Exit a parse tree produced by {@link vmParser#isgt}.
	 * @param ctx the parse tree
	 */
	void exitIsgt(vmParser.IsgtContext ctx);
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