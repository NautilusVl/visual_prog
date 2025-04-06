package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Calculator : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calculator)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val operation: TextView = findViewById(R.id.operation) as TextView
        val result: TextView = findViewById(R.id.result) as TextView
        val b_multiple: TextView = findViewById(R.id.b_multiple) as TextView
        val b_decrease: TextView = findViewById(R.id.b_decrease) as TextView
        val b_sqrt: TextView = findViewById(R.id.b_sqrt) as TextView
        val b_back: TextView = findViewById(R.id.b_back) as TextView
        val b_plus: TextView = findViewById(R.id.b_plus) as TextView
        val b_minus: TextView = findViewById(R.id.b_minus) as TextView
        val b_0: TextView = findViewById(R.id.b_0) as TextView
        val b_1: TextView = findViewById(R.id.b_1) as TextView
        val b_2: TextView = findViewById(R.id.b_2) as TextView
        val b_3: TextView = findViewById(R.id.b_3) as TextView
        val b_4: TextView = findViewById(R.id.b_4) as TextView
        val b_5: TextView = findViewById(R.id.b_5) as TextView
        val b_6: TextView = findViewById(R.id.b_6) as TextView
        val b_7: TextView = findViewById(R.id.b_7) as TextView
        val b_8: TextView = findViewById(R.id.b_8) as TextView
        val b_9: TextView = findViewById(R.id.b_9) as TextView
        val b_left: TextView = findViewById(R.id.b_left) as TextView
        val b_right: TextView = findViewById(R.id.b_right) as TextView
        val b_enter: TextView = findViewById(R.id.b_enter) as TextView
        b_sqrt.setOnClickListener { operation.append("sqrt(") }
        b_plus.setOnClickListener { operation.append("+") }
        b_minus.setOnClickListener { operation.append("-") }
        b_decrease.setOnClickListener { operation.append("/") }
        b_multiple.setOnClickListener { operation.append("*") }
        b_0.setOnClickListener { operation.append("0") }
        b_1.setOnClickListener { operation.append("1") }
        b_2.setOnClickListener { operation.append("2") }
        b_3.setOnClickListener { operation.append("3") }
        b_4.setOnClickListener { operation.append("4") }
        b_5.setOnClickListener { operation.append("5") }
        b_6.setOnClickListener { operation.append("6") }
        b_7.setOnClickListener { operation.append("7") }
        b_8.setOnClickListener { operation.append("8") }
        b_9.setOnClickListener { operation.append("9") }
        b_left.setOnClickListener { operation.append("(") }
        b_right.setOnClickListener { operation.append(")") }
        b_back.setOnClickListener {
            val s = operation.text.toString()
            if (s != "") {
                operation.text = s.substring(0, s.length - 1)
            }
        }
        b_enter.setOnClickListener {
            val optext = operation.text.toString()
            if (optext.isNotEmpty()) {
                try {
                    val res = evaluateExpression(optext)
                    result.text = if (res == res.toLong().toDouble()) res.toLong().toString() else res.toString()
                } catch (e: Exception) {
                    result.text = "Error"
                }
            }
        }
    }
    // ENTER FUNC
    fun evaluateExpression(expression: String): Double {
        val values = mutableListOf<Double>()
        val ops = mutableListOf<Char>()
        var i = 0
        while (i < expression.length) {
            if (expression[i] == ' ') {
                i++
                continue
            }
            if (expression[i].isDigit() || expression[i] == '.') {
                val start = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    i++
                }
                values.add(expression.substring(start, i).toDouble())
                continue
            }
            if (expression[i] == '(') {
                ops.add(expression[i])
            } else if (expression[i] == ')') {
                while (ops.isNotEmpty() && ops.last() != '(') {
                    values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
                }
                ops.removeAt(ops.size - 1)
            } else if (isOperator(expression[i])) {
                while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(expression[i])) {
                    values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
                }
                ops.add(expression[i])
            } else if (expression.startsWith("sqrt", i)) {
                i += 4
                if (i < expression.length && expression[i] == '(') {
                    i++
                    val start = i
                    while (i < expression.length && expression[i] != ')') {
                        i++
                    }
                    val number = expression.substring(start, i).toDouble()
                    values.add(Math.sqrt(number))
                    i++
                    continue
                }
            }
            i++
        }
        while (ops.isNotEmpty()) {
            values.add(applyOp(ops.removeAt(ops.size - 1), values.removeAt(values.size - 1), values.removeAt(values.size - 1)))
        }
        return values.last()
    }
    fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/'
    }
    fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> 0
        }
    }
    fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            else -> 0.0
        }
    }
}

