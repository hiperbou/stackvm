// Generated from D:/hiperbou/stackVM/assembler/src/main/antlr\vm.g4 by ANTLR 4.10.1
package generated.antlr.hiperbou.vm;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class vmParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, IDENTIFIER=38, 
		NUMBER=39, NEWLINE=40, WHITESPACE=41, COMMENT=42;
	public static final int
		RULE_program = 0, RULE_line = 1, RULE_emptyLine = 2, RULE_label = 3, RULE_instruction = 4, 
		RULE_nop = 5, RULE_halt = 6, RULE_push = 7, RULE_pop = 8, RULE_dup = 9, 
		RULE_add = 10, RULE_sub = 11, RULE_mul = 12, RULE_div = 13, RULE_mod = 14, 
		RULE_min = 15, RULE_max = 16, RULE_not = 17, RULE_b_not = 18, RULE_abs = 19, 
		RULE_and = 20, RULE_or = 21, RULE_b_and = 22, RULE_b_or = 23, RULE_b_xor = 24, 
		RULE_eq = 25, RULE_ne = 26, RULE_gte = 27, RULE_lte = 28, RULE_gt = 29, 
		RULE_lt = 30, RULE_jmp = 31, RULE_jif = 32, RULE_load = 33, RULE_store = 34, 
		RULE_gload = 35, RULE_gstore = 36, RULE_read = 37, RULE_write = 38, RULE_call = 39, 
		RULE_ret = 40;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "line", "emptyLine", "label", "instruction", "nop", "halt", 
			"push", "pop", "dup", "add", "sub", "mul", "div", "mod", "min", "max", 
			"not", "b_not", "abs", "and", "or", "b_and", "b_or", "b_xor", "eq", "ne", 
			"gte", "lte", "gt", "lt", "jmp", "jif", "load", "store", "gload", "gstore", 
			"read", "write", "call", "ret"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "':'", "'NOP'", "'HALT'", "'PUSH'", "'POP'", "'DUP'", "'ADD'", 
			"'SUB'", "'MUL'", "'DIV'", "'MOD'", "'MIN'", "'MAX'", "'NOT'", "'B_NOT'", 
			"'ABS'", "'AND'", "'OR'", "'B_AND'", "'B_OR'", "'B_XOR'", "'EQ'", "'NE'", 
			"'GTE'", "'LTE'", "'GT'", "'LT'", "'JMP'", "'JIF'", "'LOAD'", "'STORE'", 
			"'GLOAD'", "'GSTORE'", "'READ'", "'WRITE'", "'CALL'", "'RET'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "IDENTIFIER", "NUMBER", "NEWLINE", "WHITESPACE", "COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "vm.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public vmParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ProgramContext extends ParserRuleContext {
		public List<LineContext> line() {
			return getRuleContexts(LineContext.class);
		}
		public LineContext line(int i) {
			return getRuleContext(LineContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << T__25) | (1L << T__26) | (1L << T__27) | (1L << T__28) | (1L << T__29) | (1L << T__30) | (1L << T__31) | (1L << T__32) | (1L << T__33) | (1L << T__34) | (1L << T__35) | (1L << T__36) | (1L << IDENTIFIER) | (1L << NEWLINE))) != 0)) {
				{
				{
				setState(82);
				line();
				}
				}
				setState(87);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LineContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(vmParser.NEWLINE, 0); }
		public LabelContext label() {
			return getRuleContext(LabelContext.class,0);
		}
		public InstructionContext instruction() {
			return getRuleContext(InstructionContext.class,0);
		}
		public EmptyLineContext emptyLine() {
			return getRuleContext(EmptyLineContext.class,0);
		}
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitLine(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitLine(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_line);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(91);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(88);
				label();
				}
				break;
			case T__1:
			case T__2:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case T__12:
			case T__13:
			case T__14:
			case T__15:
			case T__16:
			case T__17:
			case T__18:
			case T__19:
			case T__20:
			case T__21:
			case T__22:
			case T__23:
			case T__24:
			case T__25:
			case T__26:
			case T__27:
			case T__28:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
			case T__33:
			case T__34:
			case T__35:
			case T__36:
				{
				setState(89);
				instruction();
				}
				break;
			case NEWLINE:
				{
				setState(90);
				emptyLine();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(93);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EmptyLineContext extends ParserRuleContext {
		public EmptyLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_emptyLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterEmptyLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitEmptyLine(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitEmptyLine(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EmptyLineContext emptyLine() throws RecognitionException {
		EmptyLineContext _localctx = new EmptyLineContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_emptyLine);
		try {
			enterOuterAlt(_localctx, 1);
			{
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LabelContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(vmParser.IDENTIFIER, 0); }
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			match(IDENTIFIER);
			setState(98);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InstructionContext extends ParserRuleContext {
		public NopContext nop() {
			return getRuleContext(NopContext.class,0);
		}
		public HaltContext halt() {
			return getRuleContext(HaltContext.class,0);
		}
		public PushContext push() {
			return getRuleContext(PushContext.class,0);
		}
		public PopContext pop() {
			return getRuleContext(PopContext.class,0);
		}
		public DupContext dup() {
			return getRuleContext(DupContext.class,0);
		}
		public AddContext add() {
			return getRuleContext(AddContext.class,0);
		}
		public SubContext sub() {
			return getRuleContext(SubContext.class,0);
		}
		public MulContext mul() {
			return getRuleContext(MulContext.class,0);
		}
		public DivContext div() {
			return getRuleContext(DivContext.class,0);
		}
		public ModContext mod() {
			return getRuleContext(ModContext.class,0);
		}
		public MinContext min() {
			return getRuleContext(MinContext.class,0);
		}
		public MaxContext max() {
			return getRuleContext(MaxContext.class,0);
		}
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public B_notContext b_not() {
			return getRuleContext(B_notContext.class,0);
		}
		public AbsContext abs() {
			return getRuleContext(AbsContext.class,0);
		}
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public B_andContext b_and() {
			return getRuleContext(B_andContext.class,0);
		}
		public B_orContext b_or() {
			return getRuleContext(B_orContext.class,0);
		}
		public B_xorContext b_xor() {
			return getRuleContext(B_xorContext.class,0);
		}
		public EqContext eq() {
			return getRuleContext(EqContext.class,0);
		}
		public NeContext ne() {
			return getRuleContext(NeContext.class,0);
		}
		public GteContext gte() {
			return getRuleContext(GteContext.class,0);
		}
		public LteContext lte() {
			return getRuleContext(LteContext.class,0);
		}
		public GtContext gt() {
			return getRuleContext(GtContext.class,0);
		}
		public LtContext lt() {
			return getRuleContext(LtContext.class,0);
		}
		public JmpContext jmp() {
			return getRuleContext(JmpContext.class,0);
		}
		public JifContext jif() {
			return getRuleContext(JifContext.class,0);
		}
		public LoadContext load() {
			return getRuleContext(LoadContext.class,0);
		}
		public StoreContext store() {
			return getRuleContext(StoreContext.class,0);
		}
		public GloadContext gload() {
			return getRuleContext(GloadContext.class,0);
		}
		public GstoreContext gstore() {
			return getRuleContext(GstoreContext.class,0);
		}
		public ReadContext read() {
			return getRuleContext(ReadContext.class,0);
		}
		public WriteContext write() {
			return getRuleContext(WriteContext.class,0);
		}
		public CallContext call() {
			return getRuleContext(CallContext.class,0);
		}
		public RetContext ret() {
			return getRuleContext(RetContext.class,0);
		}
		public InstructionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instruction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterInstruction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitInstruction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitInstruction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstructionContext instruction() throws RecognitionException {
		InstructionContext _localctx = new InstructionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_instruction);
		try {
			setState(136);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(100);
				nop();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 2);
				{
				setState(101);
				halt();
				}
				break;
			case T__3:
				enterOuterAlt(_localctx, 3);
				{
				setState(102);
				push();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 4);
				{
				setState(103);
				pop();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 5);
				{
				setState(104);
				dup();
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 6);
				{
				setState(105);
				add();
				}
				break;
			case T__7:
				enterOuterAlt(_localctx, 7);
				{
				setState(106);
				sub();
				}
				break;
			case T__8:
				enterOuterAlt(_localctx, 8);
				{
				setState(107);
				mul();
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 9);
				{
				setState(108);
				div();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 10);
				{
				setState(109);
				mod();
				}
				break;
			case T__11:
				enterOuterAlt(_localctx, 11);
				{
				setState(110);
				min();
				}
				break;
			case T__12:
				enterOuterAlt(_localctx, 12);
				{
				setState(111);
				max();
				}
				break;
			case T__13:
				enterOuterAlt(_localctx, 13);
				{
				setState(112);
				not();
				}
				break;
			case T__14:
				enterOuterAlt(_localctx, 14);
				{
				setState(113);
				b_not();
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 15);
				{
				setState(114);
				abs();
				}
				break;
			case T__16:
				enterOuterAlt(_localctx, 16);
				{
				setState(115);
				and();
				}
				break;
			case T__17:
				enterOuterAlt(_localctx, 17);
				{
				setState(116);
				or();
				}
				break;
			case T__18:
				enterOuterAlt(_localctx, 18);
				{
				setState(117);
				b_and();
				}
				break;
			case T__19:
				enterOuterAlt(_localctx, 19);
				{
				setState(118);
				b_or();
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 20);
				{
				setState(119);
				b_xor();
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 21);
				{
				setState(120);
				eq();
				}
				break;
			case T__22:
				enterOuterAlt(_localctx, 22);
				{
				setState(121);
				ne();
				}
				break;
			case T__23:
				enterOuterAlt(_localctx, 23);
				{
				setState(122);
				gte();
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 24);
				{
				setState(123);
				lte();
				}
				break;
			case T__25:
				enterOuterAlt(_localctx, 25);
				{
				setState(124);
				gt();
				}
				break;
			case T__26:
				enterOuterAlt(_localctx, 26);
				{
				setState(125);
				lt();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 27);
				{
				setState(126);
				jmp();
				}
				break;
			case T__28:
				enterOuterAlt(_localctx, 28);
				{
				setState(127);
				jif();
				}
				break;
			case T__29:
				enterOuterAlt(_localctx, 29);
				{
				setState(128);
				load();
				}
				break;
			case T__30:
				enterOuterAlt(_localctx, 30);
				{
				setState(129);
				store();
				}
				break;
			case T__31:
				enterOuterAlt(_localctx, 31);
				{
				setState(130);
				gload();
				}
				break;
			case T__32:
				enterOuterAlt(_localctx, 32);
				{
				setState(131);
				gstore();
				}
				break;
			case T__33:
				enterOuterAlt(_localctx, 33);
				{
				setState(132);
				read();
				}
				break;
			case T__34:
				enterOuterAlt(_localctx, 34);
				{
				setState(133);
				write();
				}
				break;
			case T__35:
				enterOuterAlt(_localctx, 35);
				{
				setState(134);
				call();
				}
				break;
			case T__36:
				enterOuterAlt(_localctx, 36);
				{
				setState(135);
				ret();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NopContext extends ParserRuleContext {
		public NopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterNop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitNop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitNop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NopContext nop() throws RecognitionException {
		NopContext _localctx = new NopContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_nop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HaltContext extends ParserRuleContext {
		public HaltContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_halt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterHalt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitHalt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitHalt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HaltContext halt() throws RecognitionException {
		HaltContext _localctx = new HaltContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_halt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PushContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public PushContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_push; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterPush(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitPush(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitPush(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PushContext push() throws RecognitionException {
		PushContext _localctx = new PushContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_push);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(T__3);
			setState(143);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PopContext extends ParserRuleContext {
		public PopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterPop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitPop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitPop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PopContext pop() throws RecognitionException {
		PopContext _localctx = new PopContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_pop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(T__4);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DupContext extends ParserRuleContext {
		public DupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterDup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitDup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitDup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DupContext dup() throws RecognitionException {
		DupContext _localctx = new DupContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_dup);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(T__5);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AddContext extends ParserRuleContext {
		public AddContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_add; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterAdd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitAdd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitAdd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AddContext add() throws RecognitionException {
		AddContext _localctx = new AddContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_add);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubContext extends ParserRuleContext {
		public SubContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sub; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterSub(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitSub(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitSub(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SubContext sub() throws RecognitionException {
		SubContext _localctx = new SubContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_sub);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(T__7);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MulContext extends ParserRuleContext {
		public MulContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mul; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterMul(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitMul(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitMul(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MulContext mul() throws RecognitionException {
		MulContext _localctx = new MulContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_mul);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(T__8);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DivContext extends ParserRuleContext {
		public DivContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_div; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterDiv(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitDiv(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitDiv(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DivContext div() throws RecognitionException {
		DivContext _localctx = new DivContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_div);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(T__9);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModContext extends ParserRuleContext {
		public ModContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mod; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterMod(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitMod(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitMod(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModContext mod() throws RecognitionException {
		ModContext _localctx = new ModContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_mod);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(T__10);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MinContext extends ParserRuleContext {
		public MinContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_min; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterMin(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitMin(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitMin(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinContext min() throws RecognitionException {
		MinContext _localctx = new MinContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_min);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MaxContext extends ParserRuleContext {
		public MaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_max; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitMax(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitMax(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaxContext max() throws RecognitionException {
		MaxContext _localctx = new MaxContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_max);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			match(T__12);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotContext extends ParserRuleContext {
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitNot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_not);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(T__13);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_notContext extends ParserRuleContext {
		public B_notContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterB_not(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitB_not(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitB_not(this);
			else return visitor.visitChildren(this);
		}
	}

	public final B_notContext b_not() throws RecognitionException {
		B_notContext _localctx = new B_notContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_b_not);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			match(T__14);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AbsContext extends ParserRuleContext {
		public AbsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_abs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterAbs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitAbs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitAbs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AbsContext abs() throws RecognitionException {
		AbsContext _localctx = new AbsContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_abs);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(T__15);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndContext extends ParserRuleContext {
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			match(T__16);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrContext extends ParserRuleContext {
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			match(T__17);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_andContext extends ParserRuleContext {
		public B_andContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterB_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitB_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitB_and(this);
			else return visitor.visitChildren(this);
		}
	}

	public final B_andContext b_and() throws RecognitionException {
		B_andContext _localctx = new B_andContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_b_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(T__18);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_orContext extends ParserRuleContext {
		public B_orContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterB_or(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitB_or(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitB_or(this);
			else return visitor.visitChildren(this);
		}
	}

	public final B_orContext b_or() throws RecognitionException {
		B_orContext _localctx = new B_orContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_b_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			match(T__19);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_xorContext extends ParserRuleContext {
		public B_xorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_xor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterB_xor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitB_xor(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitB_xor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final B_xorContext b_xor() throws RecognitionException {
		B_xorContext _localctx = new B_xorContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_b_xor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			match(T__20);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EqContext extends ParserRuleContext {
		public EqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitEq(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitEq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqContext eq() throws RecognitionException {
		EqContext _localctx = new EqContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_eq);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			match(T__21);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NeContext extends ParserRuleContext {
		public NeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ne; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterNe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitNe(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitNe(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NeContext ne() throws RecognitionException {
		NeContext _localctx = new NeContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_ne);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			match(T__22);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GteContext extends ParserRuleContext {
		public GteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gte; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterGte(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitGte(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitGte(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GteContext gte() throws RecognitionException {
		GteContext _localctx = new GteContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_gte);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(T__23);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LteContext extends ParserRuleContext {
		public LteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lte; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterLte(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitLte(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitLte(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LteContext lte() throws RecognitionException {
		LteContext _localctx = new LteContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_lte);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(T__24);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GtContext extends ParserRuleContext {
		public GtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterGt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitGt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitGt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GtContext gt() throws RecognitionException {
		GtContext _localctx = new GtContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_gt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(T__25);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LtContext extends ParserRuleContext {
		public LtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterLt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitLt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitLt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LtContext lt() throws RecognitionException {
		LtContext _localctx = new LtContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_lt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(T__26);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JmpContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(vmParser.IDENTIFIER, 0); }
		public JmpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jmp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterJmp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitJmp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitJmp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JmpContext jmp() throws RecognitionException {
		JmpContext _localctx = new JmpContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_jmp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191);
			match(T__27);
			setState(192);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JifContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(vmParser.IDENTIFIER, 0); }
		public JifContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jif; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterJif(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitJif(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitJif(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JifContext jif() throws RecognitionException {
		JifContext _localctx = new JifContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_jif);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			match(T__28);
			setState(195);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LoadContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public LoadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_load; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterLoad(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitLoad(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitLoad(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoadContext load() throws RecognitionException {
		LoadContext _localctx = new LoadContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_load);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			match(T__29);
			setState(198);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StoreContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public StoreContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_store; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterStore(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitStore(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitStore(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StoreContext store() throws RecognitionException {
		StoreContext _localctx = new StoreContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_store);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(T__30);
			setState(201);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GloadContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public GloadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gload; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterGload(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitGload(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitGload(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GloadContext gload() throws RecognitionException {
		GloadContext _localctx = new GloadContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_gload);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			match(T__31);
			setState(204);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GstoreContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public GstoreContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gstore; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterGstore(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitGstore(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitGstore(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GstoreContext gstore() throws RecognitionException {
		GstoreContext _localctx = new GstoreContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_gstore);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			match(T__32);
			setState(207);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReadContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public ReadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_read; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterRead(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitRead(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitRead(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReadContext read() throws RecognitionException {
		ReadContext _localctx = new ReadContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_read);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			match(T__33);
			setState(210);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WriteContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(vmParser.NUMBER, 0); }
		public WriteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_write; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterWrite(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitWrite(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitWrite(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WriteContext write() throws RecognitionException {
		WriteContext _localctx = new WriteContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_write);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			match(T__34);
			setState(213);
			match(NUMBER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CallContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(vmParser.IDENTIFIER, 0); }
		public CallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallContext call() throws RecognitionException {
		CallContext _localctx = new CallContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_call);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			match(T__35);
			setState(216);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RetContext extends ParserRuleContext {
		public RetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ret; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).enterRet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof vmListener ) ((vmListener)listener).exitRet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof vmVisitor ) return ((vmVisitor<? extends T>)visitor).visitRet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RetContext ret() throws RecognitionException {
		RetContext _localctx = new RetContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_ret);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
			match(T__36);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001*\u00dd\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0001\u0000\u0005\u0000T\b\u0000\n\u0000\f\u0000W\t\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\\\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u0089\b\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n"+
		"\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001"+
		"\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001"+
		"\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001"+
		"\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0001\"\u0001\"\u0001"+
		"\"\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0001%\u0001%\u0001%\u0001"+
		"&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0001(\u0001(\u0001(\u0000\u0000"+
		")\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a"+
		"\u001c\u001e \"$&(*,.02468:<>@BDFHJLNP\u0000\u0000\u00d9\u0000U\u0001"+
		"\u0000\u0000\u0000\u0002[\u0001\u0000\u0000\u0000\u0004_\u0001\u0000\u0000"+
		"\u0000\u0006a\u0001\u0000\u0000\u0000\b\u0088\u0001\u0000\u0000\u0000"+
		"\n\u008a\u0001\u0000\u0000\u0000\f\u008c\u0001\u0000\u0000\u0000\u000e"+
		"\u008e\u0001\u0000\u0000\u0000\u0010\u0091\u0001\u0000\u0000\u0000\u0012"+
		"\u0093\u0001\u0000\u0000\u0000\u0014\u0095\u0001\u0000\u0000\u0000\u0016"+
		"\u0097\u0001\u0000\u0000\u0000\u0018\u0099\u0001\u0000\u0000\u0000\u001a"+
		"\u009b\u0001\u0000\u0000\u0000\u001c\u009d\u0001\u0000\u0000\u0000\u001e"+
		"\u009f\u0001\u0000\u0000\u0000 \u00a1\u0001\u0000\u0000\u0000\"\u00a3"+
		"\u0001\u0000\u0000\u0000$\u00a5\u0001\u0000\u0000\u0000&\u00a7\u0001\u0000"+
		"\u0000\u0000(\u00a9\u0001\u0000\u0000\u0000*\u00ab\u0001\u0000\u0000\u0000"+
		",\u00ad\u0001\u0000\u0000\u0000.\u00af\u0001\u0000\u0000\u00000\u00b1"+
		"\u0001\u0000\u0000\u00002\u00b3\u0001\u0000\u0000\u00004\u00b5\u0001\u0000"+
		"\u0000\u00006\u00b7\u0001\u0000\u0000\u00008\u00b9\u0001\u0000\u0000\u0000"+
		":\u00bb\u0001\u0000\u0000\u0000<\u00bd\u0001\u0000\u0000\u0000>\u00bf"+
		"\u0001\u0000\u0000\u0000@\u00c2\u0001\u0000\u0000\u0000B\u00c5\u0001\u0000"+
		"\u0000\u0000D\u00c8\u0001\u0000\u0000\u0000F\u00cb\u0001\u0000\u0000\u0000"+
		"H\u00ce\u0001\u0000\u0000\u0000J\u00d1\u0001\u0000\u0000\u0000L\u00d4"+
		"\u0001\u0000\u0000\u0000N\u00d7\u0001\u0000\u0000\u0000P\u00da\u0001\u0000"+
		"\u0000\u0000RT\u0003\u0002\u0001\u0000SR\u0001\u0000\u0000\u0000TW\u0001"+
		"\u0000\u0000\u0000US\u0001\u0000\u0000\u0000UV\u0001\u0000\u0000\u0000"+
		"V\u0001\u0001\u0000\u0000\u0000WU\u0001\u0000\u0000\u0000X\\\u0003\u0006"+
		"\u0003\u0000Y\\\u0003\b\u0004\u0000Z\\\u0003\u0004\u0002\u0000[X\u0001"+
		"\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000[Z\u0001\u0000\u0000\u0000"+
		"\\]\u0001\u0000\u0000\u0000]^\u0005(\u0000\u0000^\u0003\u0001\u0000\u0000"+
		"\u0000_`\u0001\u0000\u0000\u0000`\u0005\u0001\u0000\u0000\u0000ab\u0005"+
		"&\u0000\u0000bc\u0005\u0001\u0000\u0000c\u0007\u0001\u0000\u0000\u0000"+
		"d\u0089\u0003\n\u0005\u0000e\u0089\u0003\f\u0006\u0000f\u0089\u0003\u000e"+
		"\u0007\u0000g\u0089\u0003\u0010\b\u0000h\u0089\u0003\u0012\t\u0000i\u0089"+
		"\u0003\u0014\n\u0000j\u0089\u0003\u0016\u000b\u0000k\u0089\u0003\u0018"+
		"\f\u0000l\u0089\u0003\u001a\r\u0000m\u0089\u0003\u001c\u000e\u0000n\u0089"+
		"\u0003\u001e\u000f\u0000o\u0089\u0003 \u0010\u0000p\u0089\u0003\"\u0011"+
		"\u0000q\u0089\u0003$\u0012\u0000r\u0089\u0003&\u0013\u0000s\u0089\u0003"+
		"(\u0014\u0000t\u0089\u0003*\u0015\u0000u\u0089\u0003,\u0016\u0000v\u0089"+
		"\u0003.\u0017\u0000w\u0089\u00030\u0018\u0000x\u0089\u00032\u0019\u0000"+
		"y\u0089\u00034\u001a\u0000z\u0089\u00036\u001b\u0000{\u0089\u00038\u001c"+
		"\u0000|\u0089\u0003:\u001d\u0000}\u0089\u0003<\u001e\u0000~\u0089\u0003"+
		">\u001f\u0000\u007f\u0089\u0003@ \u0000\u0080\u0089\u0003B!\u0000\u0081"+
		"\u0089\u0003D\"\u0000\u0082\u0089\u0003F#\u0000\u0083\u0089\u0003H$\u0000"+
		"\u0084\u0089\u0003J%\u0000\u0085\u0089\u0003L&\u0000\u0086\u0089\u0003"+
		"N\'\u0000\u0087\u0089\u0003P(\u0000\u0088d\u0001\u0000\u0000\u0000\u0088"+
		"e\u0001\u0000\u0000\u0000\u0088f\u0001\u0000\u0000\u0000\u0088g\u0001"+
		"\u0000\u0000\u0000\u0088h\u0001\u0000\u0000\u0000\u0088i\u0001\u0000\u0000"+
		"\u0000\u0088j\u0001\u0000\u0000\u0000\u0088k\u0001\u0000\u0000\u0000\u0088"+
		"l\u0001\u0000\u0000\u0000\u0088m\u0001\u0000\u0000\u0000\u0088n\u0001"+
		"\u0000\u0000\u0000\u0088o\u0001\u0000\u0000\u0000\u0088p\u0001\u0000\u0000"+
		"\u0000\u0088q\u0001\u0000\u0000\u0000\u0088r\u0001\u0000\u0000\u0000\u0088"+
		"s\u0001\u0000\u0000\u0000\u0088t\u0001\u0000\u0000\u0000\u0088u\u0001"+
		"\u0000\u0000\u0000\u0088v\u0001\u0000\u0000\u0000\u0088w\u0001\u0000\u0000"+
		"\u0000\u0088x\u0001\u0000\u0000\u0000\u0088y\u0001\u0000\u0000\u0000\u0088"+
		"z\u0001\u0000\u0000\u0000\u0088{\u0001\u0000\u0000\u0000\u0088|\u0001"+
		"\u0000\u0000\u0000\u0088}\u0001\u0000\u0000\u0000\u0088~\u0001\u0000\u0000"+
		"\u0000\u0088\u007f\u0001\u0000\u0000\u0000\u0088\u0080\u0001\u0000\u0000"+
		"\u0000\u0088\u0081\u0001\u0000\u0000\u0000\u0088\u0082\u0001\u0000\u0000"+
		"\u0000\u0088\u0083\u0001\u0000\u0000\u0000\u0088\u0084\u0001\u0000\u0000"+
		"\u0000\u0088\u0085\u0001\u0000\u0000\u0000\u0088\u0086\u0001\u0000\u0000"+
		"\u0000\u0088\u0087\u0001\u0000\u0000\u0000\u0089\t\u0001\u0000\u0000\u0000"+
		"\u008a\u008b\u0005\u0002\u0000\u0000\u008b\u000b\u0001\u0000\u0000\u0000"+
		"\u008c\u008d\u0005\u0003\u0000\u0000\u008d\r\u0001\u0000\u0000\u0000\u008e"+
		"\u008f\u0005\u0004\u0000\u0000\u008f\u0090\u0005\'\u0000\u0000\u0090\u000f"+
		"\u0001\u0000\u0000\u0000\u0091\u0092\u0005\u0005\u0000\u0000\u0092\u0011"+
		"\u0001\u0000\u0000\u0000\u0093\u0094\u0005\u0006\u0000\u0000\u0094\u0013"+
		"\u0001\u0000\u0000\u0000\u0095\u0096\u0005\u0007\u0000\u0000\u0096\u0015"+
		"\u0001\u0000\u0000\u0000\u0097\u0098\u0005\b\u0000\u0000\u0098\u0017\u0001"+
		"\u0000\u0000\u0000\u0099\u009a\u0005\t\u0000\u0000\u009a\u0019\u0001\u0000"+
		"\u0000\u0000\u009b\u009c\u0005\n\u0000\u0000\u009c\u001b\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0005\u000b\u0000\u0000\u009e\u001d\u0001\u0000\u0000"+
		"\u0000\u009f\u00a0\u0005\f\u0000\u0000\u00a0\u001f\u0001\u0000\u0000\u0000"+
		"\u00a1\u00a2\u0005\r\u0000\u0000\u00a2!\u0001\u0000\u0000\u0000\u00a3"+
		"\u00a4\u0005\u000e\u0000\u0000\u00a4#\u0001\u0000\u0000\u0000\u00a5\u00a6"+
		"\u0005\u000f\u0000\u0000\u00a6%\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005"+
		"\u0010\u0000\u0000\u00a8\'\u0001\u0000\u0000\u0000\u00a9\u00aa\u0005\u0011"+
		"\u0000\u0000\u00aa)\u0001\u0000\u0000\u0000\u00ab\u00ac\u0005\u0012\u0000"+
		"\u0000\u00ac+\u0001\u0000\u0000\u0000\u00ad\u00ae\u0005\u0013\u0000\u0000"+
		"\u00ae-\u0001\u0000\u0000\u0000\u00af\u00b0\u0005\u0014\u0000\u0000\u00b0"+
		"/\u0001\u0000\u0000\u0000\u00b1\u00b2\u0005\u0015\u0000\u0000\u00b21\u0001"+
		"\u0000\u0000\u0000\u00b3\u00b4\u0005\u0016\u0000\u0000\u00b43\u0001\u0000"+
		"\u0000\u0000\u00b5\u00b6\u0005\u0017\u0000\u0000\u00b65\u0001\u0000\u0000"+
		"\u0000\u00b7\u00b8\u0005\u0018\u0000\u0000\u00b87\u0001\u0000\u0000\u0000"+
		"\u00b9\u00ba\u0005\u0019\u0000\u0000\u00ba9\u0001\u0000\u0000\u0000\u00bb"+
		"\u00bc\u0005\u001a\u0000\u0000\u00bc;\u0001\u0000\u0000\u0000\u00bd\u00be"+
		"\u0005\u001b\u0000\u0000\u00be=\u0001\u0000\u0000\u0000\u00bf\u00c0\u0005"+
		"\u001c\u0000\u0000\u00c0\u00c1\u0005&\u0000\u0000\u00c1?\u0001\u0000\u0000"+
		"\u0000\u00c2\u00c3\u0005\u001d\u0000\u0000\u00c3\u00c4\u0005&\u0000\u0000"+
		"\u00c4A\u0001\u0000\u0000\u0000\u00c5\u00c6\u0005\u001e\u0000\u0000\u00c6"+
		"\u00c7\u0005\'\u0000\u0000\u00c7C\u0001\u0000\u0000\u0000\u00c8\u00c9"+
		"\u0005\u001f\u0000\u0000\u00c9\u00ca\u0005\'\u0000\u0000\u00caE\u0001"+
		"\u0000\u0000\u0000\u00cb\u00cc\u0005 \u0000\u0000\u00cc\u00cd\u0005\'"+
		"\u0000\u0000\u00cdG\u0001\u0000\u0000\u0000\u00ce\u00cf\u0005!\u0000\u0000"+
		"\u00cf\u00d0\u0005\'\u0000\u0000\u00d0I\u0001\u0000\u0000\u0000\u00d1"+
		"\u00d2\u0005\"\u0000\u0000\u00d2\u00d3\u0005\'\u0000\u0000\u00d3K\u0001"+
		"\u0000\u0000\u0000\u00d4\u00d5\u0005#\u0000\u0000\u00d5\u00d6\u0005\'"+
		"\u0000\u0000\u00d6M\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005$\u0000\u0000"+
		"\u00d8\u00d9\u0005&\u0000\u0000\u00d9O\u0001\u0000\u0000\u0000\u00da\u00db"+
		"\u0005%\u0000\u0000\u00dbQ\u0001\u0000\u0000\u0000\u0003U[\u0088";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}