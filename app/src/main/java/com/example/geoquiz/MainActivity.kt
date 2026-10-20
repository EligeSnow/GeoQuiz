package com.example.geoquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geoquiz.ui.theme.GeoQuizTheme

data class Question(
    @param:StringRes val textResId: Int,
    val answer: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoQuizTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GeoQuizScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GeoQuizScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val questionBank = remember {
        listOf(
            Question(R.string.question_australia, true),
            Question(R.string.question_ocean, true),
            Question(R.string.question_suez, false),
            Question(R.string.question_nile, false),
            Question(R.string.question_amazon, true),
            Question(R.string.question_baikal, true)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }

    val isLastQuestionAnswered = currentIndex == questionBank.size - 1 && isAnswered

    fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questionBank[currentIndex].answer
        val isCorrect = userAnswer == correctAnswer
        if (isCorrect) {
            correctAnswersCount++
        }

        val messageResId = if (isCorrect) {
            R.string.correct_toast
        } else {
            R.string.incorrect_toast
        }
        isAnswered = true
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()

        if (currentIndex == questionBank.size - 1) {
            val scorePercent = (correctAnswersCount * 100) / questionBank.size
            val resultMsg = context.resources.getString(
                R.string.quiz_result_message,
                correctAnswersCount,
                questionBank.size,
                scorePercent
            )
            Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
        }
    }

    fun restartQuiz() {
        currentIndex = 0
        isAnswered = false
        correctAnswersCount = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(
                id = R.string.question_progress,
                currentIndex + 1,
                questionBank.size
            ),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(id = questionBank[currentIndex].textResId),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isAnswered) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { checkAnswer(true) }) {
                    Text(text = stringResource(id = R.string.true_button))
                }

                Button(onClick = { checkAnswer(false) }) {
                    Text(text = stringResource(id = R.string.false_button))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLastQuestionAnswered) {
            Button(
                onClick = {
                    if (currentIndex < questionBank.size - 1) {
                        currentIndex++
                        isAnswered = false
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.next_button))
            }
        }
    }

    if (isLastQuestionAnswered) {
        val scorePercent = (correctAnswersCount * 100) / questionBank.size
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(id = R.string.quiz_finished_title))
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.quiz_result_message,
                        correctAnswersCount,
                        questionBank.size,
                        scorePercent
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { restartQuiz() }) {
                    Text(text = stringResource(id = R.string.restart_button))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GeoQuizScreenPreview() {
    GeoQuizTheme {
        GeoQuizScreen()
    }
}
