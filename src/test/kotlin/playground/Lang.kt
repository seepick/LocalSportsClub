package playground

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class Interpretor(code: String) {

    private val output = Output()
    val lines = code.lines()
    val variables = mutableMapOf<String, Int>()

    fun execute() =
        try {
            throwyExecute()
            Interpretation.Success(output.toCollectedString())
        } catch (e: Exception) {
            Interpretation.Failure(e.message ?: "Unknown error", e)
        }

    private fun throwyExecute() {
        lines.forEach { line ->
            val command = line.takeWhile { it != ' ' }
            val argument = line.dropWhile { it != ' ' }.trim()
            when (command) {
                "print" -> {
                    if (argument.startsWith("\"")) {
                        require(argument.endsWith("\""))
                        output.print(argument.trim('"'))
                    } else {
                        val value = variables[argument] ?: error("Unknown variable: $argument")
                        output.print(value.toString())
                    }
                }

                "set" -> {
                    val (name, value) = argument.split(' ')
                    variables[name] = value.toInt()
                }
                // add, sub, mul, div, mod
                // if-then-else
                // go-to label
                // I/O long&short term memory
                // address management (memory allocation?)
                // ... add complexity: functions, stack, heap, scope, ...

                else -> error("Unknown command: $command")
            }
        }
    }
}

sealed interface Interpretation {
    data class Success(val output: String) : Interpretation
    data class Failure(val errorMessage: String, val exception: Exception) : Interpretation
}

private class Output {
    private val texts = mutableListOf<String>()
    fun print(text: String) {
        texts += text
    }

    fun toCollectedString() = texts.joinToString("\n")
}


class InterpretorTest : DescribeSpec({
    lateinit var interpretor: Interpretor
    fun interpret(code: String) =
        Interpretor(code).execute()

    fun interpretSuccess(code: String) =
        interpret(code).shouldBeInstanceOf<Interpretation.Success>()

    fun interpretFailure(code: String) =
        interpret(code).shouldBeInstanceOf<Interpretation.Failure>()

    describe("print") {
        it("fail ref") {
            interpretFailure("print fail")
                .errorMessage shouldContain "fail"
        }
        it("simple") {
            interpretSuccess("print \"foo\"")
                .output shouldBeEqual "foo"
        }
    }
    describe("variables") {
        it("set") {
            interpretSuccess("set x 42")
        }
        it("set and print") {
            interpretSuccess("set x 42\nprint x")
                .output shouldBeEqual "42"
        }
    }
})
