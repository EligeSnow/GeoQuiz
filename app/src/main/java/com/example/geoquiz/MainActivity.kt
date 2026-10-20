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

/**
 * Класс модели данных Question (Вопрос).
 * Используется для хранения одного вопроса викторины GeoQuiz.
 *
 * @property textResId Идентификатор ресурса строки (R.string...), содержащего текст вопроса.
 *                     Аннотация @param:StringRes гарантирует, что передаваться будет только валидный ID строки.
 * @property answer Булево значение, обозначающее правильный ответ (true — Правда, false — Ложь).
 */
data class Question(
    @param:StringRes val textResId: Int,
    val answer: Boolean
)

/**
 * Главный класс Activity приложения GeoQuiz.
 * Наследуется от ComponentActivity для поддержки Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {
    /**
     * Точка входа при создании Activity.
     * Вызывается при запуске экрана приложения.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включает режим от края до края (Edge-to-Edge), делая интерфейс современным и растянутым под системные бары
        enableEdgeToEdge()

        // Устанавливает Jetpack Compose в качестве UI-контента для текущей Activity
        setContent {
            // Применяет фирменную тему оформления GeoQuizTheme
            GeoQuizTheme {
                // Scaffold предоставляет стандартную структуру макета с отступами под системные элементы
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Вызов главного Compose-экрана приложения с передачей внутренних отступов (innerPadding)
                    GeoQuizScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Основной Composable-компонент экрана викторины GeoQuiz.
 * Содержит в себе всю бизнес-логику, состояние викторины и UI-элементы.
 *
 * @param modifier Модификатор оформления макета Compose (отступы, размеры).
 */
@Composable
fun GeoQuizScreen(modifier: Modifier = Modifier) {
    // Получение текущего Android-контекста (необходим для вызова Toast-уведомлений и загрузки ресурсов)
    val context = LocalContext.current

    // Список всех вопросов викторины GeoQuiz.
    // Функция remember { ... } кеширует список во время рекомпозиции, предотвращая его пересоздание.
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

    // СОСТОЯНИЯ ЭКРАНА (State Management in Compose):

    // 1. Индекс текущего вопроса в списке questionBank (начиная с 0)
    var currentIndex by remember { mutableIntStateOf(0) }

    // 2. Флаг, определяющий, дал ли пользователь ответ на текущий вопрос.
    // Если true — кнопки "True" и "False" скрываются (Требование №2 из ЛР).
    var isAnswered by remember { mutableStateOf(false) }

    // 3. Счетчик количества правильных ответов для подсчета итогового результата (Требование №4 из ЛР).
    var correctAnswersCount by remember { mutableIntStateOf(0) }

    // Вычисляемый флаг: ответил ли пользователь на самый последний вопрос викторины.
    // Если true — кнопка "Next" заблокирована и невидима (Требование №3 из ЛР).
    val isLastQuestionAnswered = currentIndex == questionBank.size - 1 && isAnswered

    /**
     * Функция обработки ответа пользователя.
     * Проверяет правильность ответа, увеличивает счетчик верных ответов и выводит Toast.
     *
     * @param userAnswer Выбранный пользователем ответ (true = True, false = False).
     */
    fun checkAnswer(userAnswer: Boolean) {
        // Получаем верный ответ из текущего вопроса
        val correctAnswer = questionBank[currentIndex].answer
        val isCorrect = userAnswer == correctAnswer

        // При совпадении ответа с правильным увеличиваем счетчик очков
        if (isCorrect) {
            correctAnswersCount++
        }

        // Подбираем текст для всплывающего уведомления Toast (Correct! / Incorrect!)
        val messageResId = if (isCorrect) {
            R.string.correct_toast
        } else {
            R.string.incorrect_toast
        }

        // Фиксируем, что ответ на текущий вопрос получен
        isAnswered = true

        // Показываем короткий Toast с результатом ответа на текущий вопрос
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()

        // Если это был последний вопрос, также показываем итоговый Toast с результатом
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

    /**
     * Функция перезапуска викторины заново.
     * Сбрасывает индекс вопроса, флаг ответа и счетчик верных ответов в исходные значения.
     */
    fun restartQuiz() {
        currentIndex = 0
        isAnswered = false
        correctAnswersCount = 0
    }

    // ВЕРСТКА ИНТЕРФЕЙСА (Compose UI Layout):
    // Column выстраивает дочерние элементы вертикально по центру экрана
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Отображение прогресса прохождения вопросов (например: "Question 1 of 6")
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

        // 2. Отображение текста текущего вопроса
        Text(
            text = stringResource(id = questionBank[currentIndex].textResId),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        // Вертикальный отступ перед кнопками
        Spacer(modifier = Modifier.height(24.dp))

        // 3. Кнопки ответов «True» и «False».
        // Требование №2: Кнопки скрываются (!isAnswered), как только пользователь ответил на вопрос.
        if (!isAnswered) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Кнопка выбора ответа "True"
                Button(onClick = { checkAnswer(true) }) {
                    Text(text = stringResource(id = R.string.true_button))
                }

                // Кнопка выбора ответа "False"
                Button(onClick = { checkAnswer(false) }) {
                    Text(text = stringResource(id = R.string.false_button))
                }
            }
        }

        // Вертикальный отступ перед кнопкой перехода
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Кнопка «Next» (Следующий вопрос).
        // Требование №3: Заблокировать и сделать невидимой кнопку Next после ответа на последний вопрос.
        if (!isLastQuestionAnswered) {
            Button(
                onClick = {
                    // Переход к следующему вопросу и сброс флага ответа
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

    // 5. Всплывающая диалоговая панель (AlertDialog).
    // Требование №4: Отображение итогового результата после последнего вопроса на всплывающей панели.
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
                // Кнопка перезапуска теста заново
                TextButton(onClick = { restartQuiz() }) {
                    Text(text = stringResource(id = R.string.restart_button))
                }
            }
        )
    }
}

/**
 * Функция предпросмотра (Preview) для отображения макета в дизайнере Android Studio без запуска на устройстве.
 */
@Preview(showBackground = true)
@Composable
fun GeoQuizScreenPreview() {
    GeoQuizTheme {
        GeoQuizScreen()
    }
}
