# Rules.md — Weather App (Kotlin + Jetpack Compose)

Настанови для AI-асистента під час роботи над проєктом.

## Стек
- Мова: Kotlin (2.0+), без Java.
- UI: Jetpack Compose + Material 3. XML-розмітку не використовувати.
- Архітектура: MVVM (UI → ViewModel → Repository → DataSource).
- Асинхронність: Kotlin Coroutines + Flow / StateFlow.
- Навігація: Navigation Compose 2.8+ з @Serializable маршрутами (Type-Safe).
- Локальна БД: Room + Flow. Секрети: EncryptedSharedPreferences.
- Мережа: Retrofit + kotlinx.serialization, OpenWeatherMap API.
- Фон: WorkManager. Тести: JUnit4 + MockK + kotlinx-coroutines-test.

## Дизайн
- Material 3, світла й темна тема; за замовчуванням тема за системою.
- Палітра «небо»: primary — блакитний (SkyBlue), secondary — сонячний (Sun), фон темної теми — «нічне небо».
- Закруглення полів і кнопок: 16.dp. Відступи по краях екрана: 24.dp.
- Не хардкодити кольори в composable, лише MaterialTheme.colorScheme.
- Edge-to-edge (enableEdgeToEdge()), відступи через Scaffold innerPadding.

## Код
- Усі тексти інтерфейсу — українською.
- Стан екрана, що має пережити поворот, зберігати через rememberSaveable (до появи ViewModel).
- Composable-функції без побічних ефектів; дії передавати лямбдами.
- Кожен екран має @Preview.
- API-ключі ніколи не комітити в код і не логувати.
- Пояснювати згенерований код коротко, українською.