# Keyboard Warrior Developer Guide

## Acknowledgements

The project uses the [CS2113 tP starter template](https://github.com/NUS-CS2113-AY2627-S1/tp).
Typing feedback uses the Java standard library; its automated tests use the template's JUnit Jupiter dependency.

## Design and Implementation

### Typing feedback: tasks 3.1 and 3.2

`KeyboardWarrior.java` contains the application entry point and prints the introductory greeting.
Typing features are separate classes in the `seedu.keyboardwarrior.typing` package,
under `src/main/java/seedu/keyboardwarrior/typing`.

| File | Responsibility |
| --- | --- |
| `WpmCalculator.java` | Calculates gross WPM from character count and elapsed seconds |
| `AccuracyCalculator.java` | Calculates accuracy from matching positions and total positions |
| `WordErrorFinder.java` | Finds wrong, missing and extra words |
| `TypingEvaluator.java` | Validates the four inputs and combines the typing metrics with a time limit |
| `TypingResultFormatter.java` | Formats metrics, time limits, damage eligibility and word errors for the CLI |
| `TypingResult.java` | Stores immutable metrics, the time limit and word errors; exposes damage eligibility |
| `WordError.java` | Stores one positional word error |
| `ErrorType.java` | Defines the wrong, missing and extra error categories |

The calculators and finder are package-private implementation helpers. Other game components use
the public evaluator and formatter; result types are public top-level classes rather than nested in the entry class.

The two integration methods are:

```java
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.typing.TypingResultFormatter;

TypingResult result = TypingEvaluator.evaluateTyping(expectedText, typedText, elapsedSeconds, requiredSeconds);
String report = TypingResultFormatter.formatTypingResult(result);
```

The scenario component supplies `expectedText` and `requiredSeconds`, and the input/timer component
supplies `typedText` and `elapsedSeconds`. Both time values are in seconds. The CLI can print `report`.
Combat code must check `result.canDealDamage()` and apply zero damage when it returns `false`.
This method checks the time limit only; combat code determines damage for attempts within the limit.
Evaluating and formatting do not read input, measure time, print output,
change gameplay state or save files. The current `main` still runs only the introductory greeting.

#### Input and output contract

| Input | Requirement |
| --- | --- |
| `expectedText` | Non-null and must contain a word after normalization |
| `typedText` | Non-null; empty or whitespace-only submissions are allowed |
| `elapsedSeconds` | A positive, finite `double` in seconds, supplied by the timer |
| `requiredSeconds` | A positive, finite `double` defining the challenge's time limit in seconds |

The timer should supply typing time only, excluding scenario reading, countdown, feedback and saving.
The evaluator consumes the supplied value without performing timing itself.

| Result getter | Meaning |
| --- | --- |
| `getWpm()` | Gross WPM as a full-precision `double` |
| `getAccuracyPercent()` | Word accuracy between 0 and 100 |
| `getElapsedSeconds()` | The supplied typing duration |
| `getRequiredSeconds()` | The supplied challenge time limit |
| `canDealDamage()` | `true` when elapsed time is less than or equal to required time; otherwise damage must be zero |
| `getWordErrors()` | An unmodifiable list of errors in ascending position order |

Each error exposes `getPosition()`, `getExpectedWord()`, `getTypedWord()` and `getType()`.
Positions start at 1. Types are `WRONG`, `MISSING` and `EXTRA`; an absent word is represented by `""`.
Result fields and error fields are immutable, and the result retains a defensive copy of the error list.

#### Evaluation rules

1. Normalize both texts with `trim()` and `replaceAll("\\s+", " ")`. Spaces, tabs and line breaks are
   collapsed; capitalization and punctuation are preserved.
2. Calculate gross WPM as `(normalizedTyped.length() / 5.0) / (elapsedSeconds / 60.0)`.
   Normalized internal spaces and incorrect characters are included. The current character count
   uses Java `String.length()` (UTF-16 code units), appropriate for the intended English challenges.
3. Split normalized text into words. An empty submission has zero words.
4. Compare words at the same index, counting exact matches and recording wrong, missing or extra words.
5. Calculate accuracy as `matchingPositions * 100.0 / max(expectedWordCount, typedWordCount)`.
   The nonempty challenge guarantees that the denominator is positive.
6. Allow damage only when `elapsedSeconds <= requiredSeconds`. Finishing exactly at the time limit
   is allowed. Exceeding it blocks all damage, even for a perfectly typed submission, while preserving
   WPM, accuracy and word feedback.

This deliberately uses positional matching. Missing or inserted middle words can cause later errors;
the component does not attempt to realign the text. Only final submitted text is evaluated, so
previously corrected keystrokes are not available to this component.

Null text inputs cause `NullPointerException`. Blank challenges and zero, negative, NaN or infinite
elapsed or required times cause `IllegalArgumentException`. These indicate invalid integration data; empty player
input is a valid result instead of an exception.

`TypingResultFormatter.formatTypingResult` uses `Locale.ROOT` for consistent decimal points and the system line separator
for console output. It displays WPM, accuracy and both times to two decimal places, followed by
`Damage allowed: Yes` or `Damage: 0 (time limit exceeded)`. It then lists the word errors,
or `None` when the submission is correct. Formatting leaves the stored numeric values unchanged;
the time limit is checked using full precision rather than rounded display values.

## Product Scope

### Target user profile

Players who want English typing practice with an adventure-game setting and feedback on speed and mistakes.

### Value proposition

Keyboard Warrior aims to make typing practice engaging through roguelike scenarios. This component
provides the speed, accuracy and word feedback needed by those planned scenarios.

## User Stories

| Version | As a ... | I want to ... | So that I can ... |
| --- | --- | --- | --- |
| v1.0 | player | see my WPM and accuracy after a typing test | understand my typing performance |
| v1.0 | player | see which words I typed incorrectly | identify mistakes to improve on |

## Non-Functional Requirements

* Use Java 25 when building, running and testing the application.
* Given the same texts and time values, evaluation must return the same metrics, errors and damage eligibility.
* Keep feedback independent of console input, timing, scenario selection and file storage.

## Glossary

* **Gross WPM**: Typing speed based on five submitted characters per word, including incorrect characters.
* **Word accuracy**: The percentage of matching word positions relative to the longer word list.
* **Positional matching**: Comparing the first expected word to the first submitted word, and so on.

## Testing

### Automated testing

Run `./gradlew check` on macOS/Linux or `.\gradlew.bat check` in Windows PowerShell, using Java 25.
This runs JUnit tests and Checkstyle. Tests are in `src/test/java/seedu/keyboardwarrior/typing`:

* `TypingEvaluatorTest.java` covers correct input, fractional WPM, wrong/missing/extra words, empty
  submissions, whitespace normalization, case and punctuation, positional matching, repeated words,
  invalid inputs, immutable errors and damage eligibility before, at and after the time limit.
* `TypingResultFormatterTest.java` covers metrics formatting, word errors and the zero-damage report.

### Manual component testing

The gameplay CLI is not connected yet. To exercise the component directly:

1. Compile with `./gradlew classes` or `.\gradlew.bat classes`.
2. Start Java 25's JShell from the project folder:

   ```text
   jshell --class-path build/classes/java/main
   ```

3. Enter:

   ```java
   import seedu.keyboardwarrior.typing.TypingEvaluator;
   import seedu.keyboardwarrior.typing.TypingResultFormatter;
   var result = TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
   System.out.print(TypingResultFormatter.formatTypingResult(result));
   ```

   Expected: 34.00 WPM, 66.67% accuracy, damage allowed, and word 2 expected `the` but typed `teh`.

4. Try `"Strike the"` as the submitted text: expect 20.00 WPM, 66.67% accuracy, and missing word 3 `goblin`.
5. Try `"Strike the goblin now"`: expect 42.00 WPM, 75.00% accuracy, and extra word 4 `now`.
6. Try `""`: expect 0.00 WPM, 0.00% accuracy, and all three expected words listed as missing.
7. Change the required time to `5.0`: expect `Damage: 0 (time limit exceeded)` and
   `result.canDealDamage()` to return `false`, with typing feedback still available.
8. Set both times to `6.0`: expect damage to be allowed at the exact limit.
9. Exit JShell with `/exit`.
