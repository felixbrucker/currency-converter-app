package com.felixbrucker.currencyconverter.util

object MathEvaluator {

    /**
     * Evaluates a simple mathematical expression containing +, -, *, / and numbers.
     * Supports basic operator precedence.
     */
    fun evaluate(expression: String): Double? {
        if (expression.isBlank()) return null
        val sb = StringBuilder(expression.length)
        for (i in 0 until expression.length) {
            when (val ch = expression[i]) {
                ',', ' ' -> {}
                '×' -> sb.append('*')
                '÷' -> sb.append('/')
                else -> sb.append(ch)
            }
        }
        if (sb.isEmpty()) return null

        return try {
            ExpressionParser(sb.toString()).parse()
        } catch (e: Exception) {
            null
        }
    }

    private class ExpressionParser(private val expression: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < expression.length) expression[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) x /= parseFactor()
                else return x
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = expression.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }
}
