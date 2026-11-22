# GradeCalc

A simple, offline Android app that helps university students calculate their **semester GPA** and **cumulative GPA (CGPA)**. Students enter marks out of 100 and the app works out the letter grade, grade points and GPA automatically, so no one needs to memorise the grade scale.

Built with Kotlin and Jetpack Compose. All data stays on the phone: no account, no server, no internet needed.

## Features

- **Semester GPA and CGPA** from courses, credit hours and marks
- **Marks to grades automatically**: enter a mark out of 100 (rounded half up, so 74.5 becomes 75) and the app finds the grade
- **Year and semester pickers** (Year I to V, Semester One or Two), with duplicate prevention
- **Course mark calculator**: add quizzes, assignments, midterm and final to get a course mark
- **Retake handling**: only the latest graded attempt of a course counts in the CGPA
- **Target calculator ("What if")**: find the GPA you need next semester to reach a target CGPA
- **GPA trend chart** across semesters
- **Grade scale presets** chosen on first launch: Ethiopian 4.0 (default), US plus/minus, US simple, 5-point. Points can be edited in Settings
- **Share a report** as a PDF or an image
- **English and Amharic**, switchable inside the app
- **Light and dark theme** in a portal-inspired green style
- **ECTS display** next to credit hours

## How the GPA is calculated

```
Quality points = grade points x credit hours
GPA            = sum of quality points / sum of credit hours
```

- Courses without a grade, or with zero credit hours, are left out.
- CGPA uses the same formula over all counted courses. It is not an average of semester GPAs.
- Retakes: only the latest graded attempt (matched by course name, ignoring case and spaces) counts toward the CGPA. Semester GPAs keep their original values.
- Target calculator: `needed GPA = (target x (credits + next credits) - quality points) / next credits`

### Default grade scale (Ethiopian 4.0)

| Grade | Points | Marks |
| --- | --- | --- |
| A+ | 4.00 | 90-100 |
| A | 4.00 | 85-89 |
| A- | 3.75 | 80-84 |
| B+ | 3.50 | 75-79 |
| B | 3.00 | 70-74 |
| B- | 2.75 | 65-69 |
| C+ | 2.50 | 60-64 |
| C | 2.00 | 50-59 |
| C- | 1.75 | 45-49 |
| D | 1.00 | 40-44 |
| F | 0.00 | 0-39 |

Check this against your own university's grading policy.

## Tech stack

| Area | Technology |
| --- | --- |
| Language | Kotlin 2.0.20 |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + StateFlow) |
| Storage | Room (SQLite) with migrations |
| Export | PdfDocument, Canvas, FileProvider |
| Tests | JUnit |
| Build | Gradle 8.7, Android Gradle Plugin 8.5.2, KSP |
| Android | minSdk 24, targetSdk and compileSdk 34 |

## Project structure

```
app/src/main/java/com/example/gradecalc/
  data/      Room entities, DAOs, repository, grade scales and presets
  domain/    Pure Kotlin rules: GPA, retakes, course mark, semester labels
  ui/        Screens, components, view model, theme, navigation
  export/    PDF and image report builder
  MainActivity.kt
app/src/test/   Unit tests for the domain rules
```

Screens call the ViewModel, the ViewModel calls the repository, and the repository reads and writes the Room database. The calculation rules live in `domain/` and know nothing about Android, so they are easy to test.

## Getting started

### Requirements
- Android Studio (Koala or newer)
- JDK 17 (included with Android Studio)
- Internet on the first Gradle sync

### Run
1. Clone the repository:
   ```
   git clone https://github.com/NATIOPIA10/GradeCalc.git
   ```
2. Open the folder in Android Studio and wait for the Gradle sync to finish.
3. Select an emulator or a phone with USB debugging, then press **Run**.

### Test
Right-click `app/src/test` and choose **Run Tests**, or run `./gradlew test`.

### Build an APK
**Build, then Generate Signed App Bundle / APK, then APK, then release.** Create a keystore the first time and keep it safe, outside this repository. Never commit your keystore or its passwords.

## Roadmap

- Backup and restore
- More grade scales
- Home screen widget
- Course name suggestions for retakes

## Contributing

Issues and pull requests are welcome. Please keep calculation changes covered by unit tests.

## Author

Made by [NATIOPIA10](https://github.com/NATIOPIA10).
