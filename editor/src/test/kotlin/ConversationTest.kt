import com.hiperbou.vm.compiler.DefaultProgramWriter
import com.hiperbou.vm.compiler.ProgramWriter
import org.junit.jupiter.api.Test
import java.io.InputStreamReader
import java.io.Reader
import java.io.StreamTokenizer
import java.util.*
import java.util.regex.Pattern
import kotlin.test.assertContentEquals


class ConversationTest {
    interface Token

    data class LabelToken(val name:String):Token{
        override fun toString(): String {
            return """LabelToken("$name")"""
        }
    }

    data class CharacterMarkToken(val name:String):Token{
        override fun toString(): String {
            return """CharacterMarkToken("$name")"""
        }
    }

    data class TextToken(val text:String):Token{
        override fun toString(): String {
            return """TextToken("$text")"""
        }
    }

    data class GotoToken(val label:String):Token{
        override fun toString(): String {
            return """GotoToken("$label")"""
        }
    }

    data class GotoConditionToken(val gotoToken:GotoToken, val condition:String):Token{
        override fun toString(): String {
            return """GotoConditionToken($gotoToken, "$condition")"""
        }
    }

    data class GotoConditionElseToken(val gotoToken:GotoToken, val gotoElseToken:GotoToken, val condition:String):Token{
        override fun toString(): String {
            return """GotoConditionElseToken($gotoToken, $gotoElseToken, "$condition")"""
        }
    }

    class ConversationTokenizer() {
        private val GOTOCONDITIONELSE = """(->)(.+)\s:(.+)\s\|\s(.+)""".toRegex()
        private val GOTOCONDITION = """(->)(.+)\s\|\s(.+)""".toRegex()
        private val GOTO = """(->)(.+)""".toRegex()
        private val LABEL = """(@)(.+)""".toRegex()
        private val CHARACTER = """(-)\s(.+)""".toRegex()
        private val IDENTIFIER = """[a-zA-Z][a-zA-Z0-9_]*""".toRegex()
        private val TEXT = """\s+(.+)""".toRegex()
        private val COMMENT = """//.*""".toRegex()
        private val COMMENT_BLOCK = """(?s)/\*.*?\*/""".toRegex()
        private val WHITESPACE = """\s+""".toRegex()

        fun Regex.tokenize(str:String) = find(str)!!.destructured

        interface Tokenizer {
            fun matches(str:String):Boolean
            fun tokenize(str:String):List<Token>
        }

        inner class LabelTokenizer():Tokenizer {
            override fun matches(str: String) = LABEL.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (_, labelName) = LABEL.tokenize(str)
                return listOf(LabelToken(labelName.trim()))
            }
        }

        inner class CharacterMarkTokenizer():Tokenizer {
            override fun matches(str: String) = CHARACTER.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (_, characterName) = CHARACTER.tokenize(str)
                return listOf(CharacterMarkToken(characterName.trim()))
            }
        }

        inner class TextTokenizer():Tokenizer {
            override fun matches(str: String) = TEXT.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (text) = TEXT.tokenize(str)
                return listOf(TextToken(text.trim()))
            }
        }

        inner class GotoTokenizer():Tokenizer {
            override fun matches(str: String) = GOTO.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (_, labelName) = GOTO.tokenize(str)
                return listOf(GotoToken(labelName.trim()))
            }
        }

        inner class GotoConditionTokenizer():Tokenizer {
            override fun matches(str: String) = GOTOCONDITION.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (_, labelName, condition) = GOTOCONDITION.tokenize(str)
                return listOf(GotoConditionToken(GotoToken(labelName.trim()), condition.trim()))
            }
        }

        inner class GotoConditionElseTokenizer():Tokenizer {
            override fun matches(str: String) = GOTOCONDITIONELSE.matches(str)

            override fun tokenize(str: String): List<Token> {
                val (_, labelName, labelElseName, condition) = GOTOCONDITIONELSE.tokenize(str)
                return listOf(GotoConditionElseToken(GotoToken(labelName.trim()), GotoToken(labelElseName.trim()), condition.trim()))
            }
        }

        fun generateProgram(inputStream: InputStreamReader,
                            programWriter: ProgramWriter = DefaultProgramWriter()
        ):List<Token> {
            val tokens = mutableListOf<Token>()
            inputStream.use {
                it.forEachLine {
                    tokens.addAll(tokenize(it))
                }
            }
            println(tokens)
            return tokens
        }

        val tokenizers = listOf(
            GotoConditionElseTokenizer(),
            GotoConditionTokenizer(),
            GotoTokenizer(),
            LabelTokenizer(),
            CharacterMarkTokenizer(),
            TextTokenizer()
        )
        private fun tokenize(inputLine:String):List<Token> {
            val line = inputLine.replace(COMMENT_BLOCK, "").replace(COMMENT, "")
            if (line.isBlank()) return emptyList()

            return tokenizers.firstOrNull { it.matches(line) }?.tokenize(line) ?: emptyList()
        }

        private fun parseLine(programWriter:ProgramWriter, inputLine:String, currentLine:Int) = with(programWriter) {
            fun String.isCharacter() = CHARACTER.matches(this)
            fun String.isText() = TEXT.matches(this)

            /*tokenize(inputLine).forEach {
                when {
                    it.isNumber() -> addLiteral(it.toInt())
                    it.tryProcessOpcode() -> {}
                    it.isLabel() -> {
                        labelsAddresses[it.removeLabelColon()] = currentAddress()
                    }
                    it.isIdentifier() -> {
                        labelsToResolve.add(Compiler.UnresolvedAddress(it, currentAddress()))
                        addLiteral(UNRESOLVED_JUMP_ADDRESS)
                    }
                    else -> throw InvalidProgramException("Unknown token: '$it' in line: $currentLine\n'$inputLine'")
                }
            }*/
        }

    }

    @Test
    fun convTest() {
        val conv = """
- . 
    One Ring to rule them all, One Ring to find them,
    One Ring to bring them all, and in the darkness bind them,
    In the Land of Mordor where the Shadows lie.
        """.trimStart()

        val conv2= """
- Bob 
    Hi Alice. How are you?
- Alice
    Hi Bob! I'm fine.
    Thank you!
""".trim()

        val conversationTokenizer = ConversationTokenizer()

        val result = conversationTokenizer.generateProgram(conv.byteInputStream().reader())
        assertContentEquals(
            arrayOf(
                CharacterMarkToken("."),
                TextToken("One Ring to rule them all, One Ring to find them,"),
                TextToken("One Ring to bring them all, and in the darkness bind them,"),
                TextToken("In the Land of Mordor where the Shadows lie.")),
            result.toTypedArray()
            )
        println("----")
        val result2 = conversationTokenizer.generateProgram(conv2.byteInputStream().reader())
        assertContentEquals(
            arrayOf(
                CharacterMarkToken("Bob"),
                TextToken("Hi Alice. How are you?"),
                CharacterMarkToken("Alice"),
                TextToken("Hi Bob! I'm fine."),
                TextToken("Thank you!")
            ),
            result2.toTypedArray()
        )
    }

    @Test
    fun labelsTest() {
        val conv = """
@ MyLabel 

- .
  This dialog is considered belonging to 'MyLabel'.

- .
  This one is also considered belonging to 'MyLabel'.

@OtherLabel

- .
  This dialog is considered belonging to 'OtherLabel'.
        """.trim()

        val tokenizer = ConversationTokenizer()
        val result = tokenizer.generateProgram(conv.byteInputStream().reader())
        assertContentEquals(
            arrayOf(
                LabelToken("MyLabel"),
                CharacterMarkToken("."),
                TextToken("This dialog is considered belonging to 'MyLabel'."),
                CharacterMarkToken("."),
                TextToken("This one is also considered belonging to 'MyLabel'."),
                LabelToken("OtherLabel"),
                CharacterMarkToken("."),
                TextToken("This dialog is considered belonging to 'OtherLabel'.")
            ),
            result.toTypedArray()
        )
    }

    @Test
    fun gotoTest() {
        val conv = """
- .
  Start
-> Exit

@WillNotExecute
- .
  This dialogue will not get executed
  
@Exit
- .
  Good bye!
        """.trim()

        val tokenizer = ConversationTokenizer()
        val result = tokenizer.generateProgram(conv.byteInputStream().reader())
        assertContentEquals(
            arrayOf(
                CharacterMarkToken("."),
                TextToken("Start"),
                GotoToken("Exit"),
                LabelToken("WillNotExecute"),
                CharacterMarkToken("."),
                TextToken("This dialogue will not get executed"),
                LabelToken("Exit"),
                CharacterMarkToken("."),
                TextToken("Good bye!")
            ),
            result.toTypedArray()
        )
    }

    @Test
    fun gotoIfTest() {
        val conv = """
- .
  Start
-> Exit | alreadyTalked

- .
  This should be said only when alreadyTalked is false 
  
@Exit
- .
  Good bye!
        """.trim()

        val tokenizer = ConversationTokenizer()
        val result = tokenizer.generateProgram(conv.byteInputStream().reader())
        assertContentEquals(
            arrayOf(
                CharacterMarkToken("."),
                TextToken("Start"),
                GotoConditionToken(
                    GotoToken("Exit"),
                    "alreadyTalked"
                ),
                CharacterMarkToken("."),
                TextToken("This should be said only when alreadyTalked is false"),
                LabelToken("Exit"),
                CharacterMarkToken("."),
                TextToken("Good bye!")
            ),
            result.toTypedArray()
        )
    }

    @Test
    fun gotoIfElseTest() {
        val conv = """
- .
  Start
-> Exit : Hello | alreadyTalked

- .
  This will never be said.

@Hello
- .
    Hello, nice to meet you!
  
@Exit
- .
  Good bye!
        """.trim()

        val tokenizer = ConversationTokenizer()
        val result = tokenizer.generateProgram(conv.byteInputStream().reader())

        assertContentEquals(
            arrayOf(
                CharacterMarkToken("."),
                TextToken("Start"),
                GotoConditionElseToken(
                    GotoToken("Exit"),
                    GotoToken("Hello"),
                    "alreadyTalked"
                ),
                CharacterMarkToken("."),
                TextToken("This will never be said."),
                LabelToken("Hello"),
                CharacterMarkToken("."),
                TextToken("Hello, nice to meet you!"),
                LabelToken("Exit"),
                CharacterMarkToken("."),
                TextToken("Good bye!")
            ),
            result.toTypedArray()
        )
    }

    @Test
    fun regexNestedTest() {
        val str = """
            //this will be ignored
            /* so this must be ignored too */
            /* And maybe
            this multiline comment
            is ignored too*/
           oh / but "this isn't a comment"
           1 2 3 1.1 2.2 3.3 -1 -2 -3
           "this isn't 
           a string"
            - .
    "Start"
-> Exit : Hello | alreadyTalked

- .
    "This will \"never\" be said."

@Hello
- .
    "Hello, nice to meet you!"
  
@Exit
- .
    "Good bye!"
        """.trimIndent()

        var st = StringTokenizer(str,
            " \t\n\r\u000c", true)
        while (st.hasMoreTokens()) {
            System.out.println(st.nextToken())
        }

        val p = Pattern.compile("(.+?)(\\s+(?=(?:(?:[^\"]*\"){2})*[^\"]*$)|$)")
        val m = p.matcher(str)
        var i = 0
        while (m.find()) {
            System.out.printf("Scol[%d]: [%s]%n", i, m.group(1)
                .replace("\"", "")
                .replace("\n", "NEWLINE"))
            i++
        }



        val operators = """-@.'>:|#/$*""".map { it }.map { it.code }

        fun streamTokenizer(reader: Reader):List<Any> {

            val QUOTE = '"'.code

            val tokens = mutableListOf<Any>()

            val streamTokenizer = StreamTokenizer(reader).apply {
                wordChars('!'.code, '-'.code)

                operators.forEach {
                    ordinaryChar(it)
                }
                quoteChar(QUOTE)
                eolIsSignificant(true)
                slashSlashComments(true)
                slashStarComments(true)
            }

            var currentToken = streamTokenizer.nextToken()
            while (currentToken != StreamTokenizer.TT_EOF) {

                when (streamTokenizer.ttype) {
                    StreamTokenizer.TT_EOL -> {
                        if(tokens.lastOrNull() != "EOL") //Ignore multiple EOL
                            tokens.add("EOL")
                    }
                    StreamTokenizer.TT_NUMBER -> {
                        tokens.add(streamTokenizer.nval)
                    }
                    StreamTokenizer.TT_WORD -> {
                        tokens.add(streamTokenizer.sval)
                    }
                    else -> {
                        when (streamTokenizer.ttype) {
                            QUOTE -> {
                                tokens.add(streamTokenizer.sval)
                            }
                            in operators -> {
                                tokens.add(streamTokenizer.ttype.toChar())
                            }
                            else -> {
                                tokens.add(currentToken)
                            }
                        }
                    }
                }

                currentToken = streamTokenizer.nextToken()
            }

            return tokens;
        }

        val k = streamTokenizer(str.reader())
        println(k)
    }
}