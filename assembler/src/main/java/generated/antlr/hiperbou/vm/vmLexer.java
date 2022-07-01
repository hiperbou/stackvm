// Generated from D:/hiperbou/StackVMasm/src/main/antlr\vm.g4 by ANTLR 4.10.1
package generated.antlr.hiperbou.vm;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class vmLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, IDENTIFIER=22, NUMBER=23, NEWLINE=24, 
		WHITESPACE=25, COMMENT=26;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "IDENTIFIER", "NUMBER", "NEWLINE", 
			"WHITESPACE", "COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "':'", "'HALT'", "'PUSH'", "'ADD'", "'SUB'", "'MUL'", "'DIV'", 
			"'NOT'", "'AND'", "'OR'", "'POP'", "'DUP'", "'ISEQ'", "'ISGE'", "'ISGT'", 
			"'JMP'", "'JIF'", "'LOAD'", "'STORE'", "'CALL'", "'RET'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "IDENTIFIER", 
			"NUMBER", "NEWLINE", "WHITESPACE", "COMMENT"
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


	public vmLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "vm.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u001a\u00b2\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0005\u0015"+
		"\u0092\b\u0015\n\u0015\f\u0015\u0095\t\u0015\u0001\u0016\u0004\u0016\u0098"+
		"\b\u0016\u000b\u0016\f\u0016\u0099\u0001\u0017\u0003\u0017\u009d\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0018\u0004\u0018\u00a2\b\u0018\u000b\u0018"+
		"\f\u0018\u00a3\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0005\u0019\u00ac\b\u0019\n\u0019\f\u0019\u00af\t\u0019\u0001"+
		"\u0019\u0001\u0019\u0000\u0000\u001a\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015"+
		"\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012"+
		"%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a\u0001\u0000"+
		"\u0005\u0002\u0000AZaz\u0004\u000009AZ__az\u0001\u000009\u0002\u0000\t"+
		"\t  \u0002\u0000\n\n\r\r\u00b6\u0000\u0001\u0001\u0000\u0000\u0000\u0000"+
		"\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000"+
		"\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b"+
		"\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001"+
		"\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001"+
		"\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001"+
		"\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001"+
		"\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001"+
		"\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000"+
		"\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000"+
		"\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000\u0000\u0000-"+
		"\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u00001\u0001\u0000"+
		"\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00015\u0001\u0000\u0000\u0000"+
		"\u00037\u0001\u0000\u0000\u0000\u0005<\u0001\u0000\u0000\u0000\u0007A"+
		"\u0001\u0000\u0000\u0000\tE\u0001\u0000\u0000\u0000\u000bI\u0001\u0000"+
		"\u0000\u0000\rM\u0001\u0000\u0000\u0000\u000fQ\u0001\u0000\u0000\u0000"+
		"\u0011U\u0001\u0000\u0000\u0000\u0013Y\u0001\u0000\u0000\u0000\u0015\\"+
		"\u0001\u0000\u0000\u0000\u0017`\u0001\u0000\u0000\u0000\u0019d\u0001\u0000"+
		"\u0000\u0000\u001bi\u0001\u0000\u0000\u0000\u001dn\u0001\u0000\u0000\u0000"+
		"\u001fs\u0001\u0000\u0000\u0000!w\u0001\u0000\u0000\u0000#{\u0001\u0000"+
		"\u0000\u0000%\u0080\u0001\u0000\u0000\u0000\'\u0086\u0001\u0000\u0000"+
		"\u0000)\u008b\u0001\u0000\u0000\u0000+\u008f\u0001\u0000\u0000\u0000-"+
		"\u0097\u0001\u0000\u0000\u0000/\u009c\u0001\u0000\u0000\u00001\u00a1\u0001"+
		"\u0000\u0000\u00003\u00a7\u0001\u0000\u0000\u000056\u0005:\u0000\u0000"+
		"6\u0002\u0001\u0000\u0000\u000078\u0005H\u0000\u000089\u0005A\u0000\u0000"+
		"9:\u0005L\u0000\u0000:;\u0005T\u0000\u0000;\u0004\u0001\u0000\u0000\u0000"+
		"<=\u0005P\u0000\u0000=>\u0005U\u0000\u0000>?\u0005S\u0000\u0000?@\u0005"+
		"H\u0000\u0000@\u0006\u0001\u0000\u0000\u0000AB\u0005A\u0000\u0000BC\u0005"+
		"D\u0000\u0000CD\u0005D\u0000\u0000D\b\u0001\u0000\u0000\u0000EF\u0005"+
		"S\u0000\u0000FG\u0005U\u0000\u0000GH\u0005B\u0000\u0000H\n\u0001\u0000"+
		"\u0000\u0000IJ\u0005M\u0000\u0000JK\u0005U\u0000\u0000KL\u0005L\u0000"+
		"\u0000L\f\u0001\u0000\u0000\u0000MN\u0005D\u0000\u0000NO\u0005I\u0000"+
		"\u0000OP\u0005V\u0000\u0000P\u000e\u0001\u0000\u0000\u0000QR\u0005N\u0000"+
		"\u0000RS\u0005O\u0000\u0000ST\u0005T\u0000\u0000T\u0010\u0001\u0000\u0000"+
		"\u0000UV\u0005A\u0000\u0000VW\u0005N\u0000\u0000WX\u0005D\u0000\u0000"+
		"X\u0012\u0001\u0000\u0000\u0000YZ\u0005O\u0000\u0000Z[\u0005R\u0000\u0000"+
		"[\u0014\u0001\u0000\u0000\u0000\\]\u0005P\u0000\u0000]^\u0005O\u0000\u0000"+
		"^_\u0005P\u0000\u0000_\u0016\u0001\u0000\u0000\u0000`a\u0005D\u0000\u0000"+
		"ab\u0005U\u0000\u0000bc\u0005P\u0000\u0000c\u0018\u0001\u0000\u0000\u0000"+
		"de\u0005I\u0000\u0000ef\u0005S\u0000\u0000fg\u0005E\u0000\u0000gh\u0005"+
		"Q\u0000\u0000h\u001a\u0001\u0000\u0000\u0000ij\u0005I\u0000\u0000jk\u0005"+
		"S\u0000\u0000kl\u0005G\u0000\u0000lm\u0005E\u0000\u0000m\u001c\u0001\u0000"+
		"\u0000\u0000no\u0005I\u0000\u0000op\u0005S\u0000\u0000pq\u0005G\u0000"+
		"\u0000qr\u0005T\u0000\u0000r\u001e\u0001\u0000\u0000\u0000st\u0005J\u0000"+
		"\u0000tu\u0005M\u0000\u0000uv\u0005P\u0000\u0000v \u0001\u0000\u0000\u0000"+
		"wx\u0005J\u0000\u0000xy\u0005I\u0000\u0000yz\u0005F\u0000\u0000z\"\u0001"+
		"\u0000\u0000\u0000{|\u0005L\u0000\u0000|}\u0005O\u0000\u0000}~\u0005A"+
		"\u0000\u0000~\u007f\u0005D\u0000\u0000\u007f$\u0001\u0000\u0000\u0000"+
		"\u0080\u0081\u0005S\u0000\u0000\u0081\u0082\u0005T\u0000\u0000\u0082\u0083"+
		"\u0005O\u0000\u0000\u0083\u0084\u0005R\u0000\u0000\u0084\u0085\u0005E"+
		"\u0000\u0000\u0085&\u0001\u0000\u0000\u0000\u0086\u0087\u0005C\u0000\u0000"+
		"\u0087\u0088\u0005A\u0000\u0000\u0088\u0089\u0005L\u0000\u0000\u0089\u008a"+
		"\u0005L\u0000\u0000\u008a(\u0001\u0000\u0000\u0000\u008b\u008c\u0005R"+
		"\u0000\u0000\u008c\u008d\u0005E\u0000\u0000\u008d\u008e\u0005T\u0000\u0000"+
		"\u008e*\u0001\u0000\u0000\u0000\u008f\u0093\u0007\u0000\u0000\u0000\u0090"+
		"\u0092\u0007\u0001\u0000\u0000\u0091\u0090\u0001\u0000\u0000\u0000\u0092"+
		"\u0095\u0001\u0000\u0000\u0000\u0093\u0091\u0001\u0000\u0000\u0000\u0093"+
		"\u0094\u0001\u0000\u0000\u0000\u0094,\u0001\u0000\u0000\u0000\u0095\u0093"+
		"\u0001\u0000\u0000\u0000\u0096\u0098\u0007\u0002\u0000\u0000\u0097\u0096"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u0097"+
		"\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a.\u0001"+
		"\u0000\u0000\u0000\u009b\u009d\u0005\r\u0000\u0000\u009c\u009b\u0001\u0000"+
		"\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000"+
		"\u0000\u0000\u009e\u009f\u0005\n\u0000\u0000\u009f0\u0001\u0000\u0000"+
		"\u0000\u00a0\u00a2\u0007\u0003\u0000\u0000\u00a1\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000\u0000"+
		"\u0000\u00a5\u00a6\u0006\u0018\u0000\u0000\u00a62\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a8\u0005/\u0000\u0000\u00a8\u00a9\u0005/\u0000\u0000\u00a9\u00ad"+
		"\u0001\u0000\u0000\u0000\u00aa\u00ac\b\u0004\u0000\u0000\u00ab\u00aa\u0001"+
		"\u0000\u0000\u0000\u00ac\u00af\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001"+
		"\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00b0\u0001"+
		"\u0000\u0000\u0000\u00af\u00ad\u0001\u0000\u0000\u0000\u00b0\u00b1\u0006"+
		"\u0019\u0000\u0000\u00b14\u0001\u0000\u0000\u0000\u0006\u0000\u0093\u0099"+
		"\u009c\u00a3\u00ad\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}