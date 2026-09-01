# TutorLink

Desktop platform that connects university students with tutors. A tutor
publishes the time slots in which he is available, a student books one of them
and pays it, both join the lesson through a generated meeting link, the tutor
shares the teaching material and records the outcome, and the student follows
his own progress over time.

Software engineering project (Ingegneria del Software e Progettazione Web),
University of Rome Tor Vergata.

| | |
|---|---|
| Language | Java 17 |
| GUI | JavaFX 17, two interchangeable layouts |
| Build | Maven (wrapper included) |
| Persistence | in-memory, CSV files, MySQL |
| Tests | JUnit 5 — 39 test cases |
| Size | 151 classes, ~7.300 lines of code |

---

## Build and run

Maven is not required: the repository ships the Maven wrapper.

```bash
./mvnw clean verify        # compiles and runs the whole test suite
./mvnw javafx:run          # starts the application
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

### Standalone jar

`package` also builds a self-contained jar that carries every dependency inside,
so it runs without Maven:

```bash
./mvnw clean package
java -jar target/tutorlink-1.0.0.jar
```

Its main class is `Launcher` and not `Main`, because JavaFX refuses to start when
the class booting the JVM extends `Application` and the JavaFX libraries sit on
the class path; a plain class in front of it avoids the check.

The jar embeds the native JavaFX libraries of the platform it was built on: a jar
built on Windows runs on Windows, on another operating system it has to be built
again there.

### Execution modes

The application starts in **demo-version** with the **desktop** layout. Both
choices can be changed from the command line or in
`src/main/resources/config.properties`.

| Command | Data | GUI |
|---|---|---|
| `./mvnw javafx:run` | in memory, lost on exit | desktop |
| `./mvnw javafx:run -Djavafx.args="--compact"` | in memory | compact |
| `./mvnw javafx:run -Djavafx.args="--full --file"` | CSV files under `data/` | desktop |
| `./mvnw javafx:run -Djavafx.args="--full --dbms"` | MySQL | desktop |

The MySQL mode needs the schema to be created once:

```bash
mysql -u root -p < db/schema.sql
```

The title bar always shows the active configuration, for example
`TutorLink - Student area [demo-version / MEMORY / DESKTOP layout]`.

### Demo accounts

Created automatically the first time the application finds no data. The
password is `tutorlink` for all of them.

| Role | E-mail | Subjects |
|---|---|---|
| Student | `mario.rossi@students.uniroma2.eu` | — |
| Tutor | `giulia.bianchi@uniroma2.eu` | Mathematics, Physics |
| Tutor | `luca.verdi@uniroma2.eu` | Computer Science, Mathematics |

---

## What the application does

* A tutor publishes his availabilities; overlapping slots are refused.
* Every student who already took a lesson with that tutor is notified.
* A student picks a subject and gets the list of tutors ordered by an adaptive
  criterion: the subjects he is weaker in, how experienced the tutor is, whether
  they already worked together, and the hourly rate. The free slots are ordered
  by the time band in which the student historically scores best.
* Booking reserves the slot, charges the card and generates the meeting link. A
  refused payment releases the slot; an unreachable calendar service leaves the
  lesson valid and defers the link.
* The tutor attaches files to a lesson and publishes them; the student is
  notified and can download them.
* At the end of the lesson the tutor records a score, which updates the progress
  chart of the student.

---

## Project layout

```
TutorLink
├── db/schema.sql              MySQL schema used by the full-version
├── docs/                      project report and demo video
├── src/main/resources/        config.properties: execution mode and layout
├── src/main/java/it/uniroma2/tutorlink/
│   ├── Main.java              entry point, picks the GUI layout
│   ├── Launcher.java          entry point of the executable jar
│   ├── boundary/              views and graphic controllers
│   │   ├── common/            navigation and error handling shared by both layouts
│   │   ├── desktop/           wide layout, one tab per use case
│   │   └── compact/           narrow layout, single column
│   ├── bean/                  data exchanged between GUI and controllers
│   ├── control/               application controllers, one per use case
│   ├── model/                 entities and business rules
│   │   ├── state/             lifecycle of a lesson
│   │   ├── matching/          ordering of tutors and slots
│   │   └── progress/          statistics on the scores of a student
│   ├── dao/                   data access
│   │   ├── memory/            demo-version
│   │   ├── filesystem/        full-version on CSV files
│   │   ├── jdbc/              full-version on MySQL
│   │   └── cache/             RAM cache in front of the other implementations
│   ├── external/              payment service and meeting link service
│   ├── notification/          notification channels
│   ├── observer/              notification of a newly published availability
│   ├── exception/             application exceptions
│   ├── session/               logged user
│   ├── config/                runtime configuration
│   ├── bootstrap/             start-up wiring and demo data
│   └── util/                  password digest and identifier generator
└── src/test/java/             JUnit 5 test suite
```

The layering is the one of the MVC pattern with boundary, control and entity
classes. Two rules hold everywhere in the code:

* a graphic controller never touches a model object, it exchanges beans only —
  this is what allows the two layouts to share the whole application logic;
* the persistence layer performs no validation, it only reads and writes.

---

## Tests

```bash
./mvnw test
```

39 test cases over the domain rules, the two main use cases with their
alternative flows, the notification mechanism and the persistence on files.
Each test class states in a comment who is responsible for it.

## Continuous integration

`.github/workflows/ci.yml` runs on every push and pull request on `main`: it
installs JDK 17 and executes `mvn clean verify`, so compilation and the whole
test suite are checked on a clean machine. The SonarCloud step is skipped until
the `SONAR_TOKEN` secret is configured.

## Static analysis

The project is ready for SonarCloud but needs two identifiers that depend on the
account, declared in `pom.xml` and in `sonar-project.properties`:

```xml
<sonar.organization>REPLACE-WITH-YOUR-SONAR-ORG</sonar.organization>
<sonar.projectKey>REPLACE-WITH-YOUR-SONAR-PROJECT-KEY</sonar.projectKey>
```

Setup, once the repository is on GitHub:

1. sign in on [sonarcloud.io](https://sonarcloud.io) with the GitHub account and
   import this repository; SonarCloud shows the organization key and the project key;
2. write those two values in `pom.xml` and in `sonar-project.properties`;
3. in the SonarCloud project, under *Administration > Analysis Method*, turn
   **Automatic Analysis off**: it cannot coexist with the analysis run by the CI;
4. generate a token in *My Account > Security* and store it on GitHub as the
   repository secret `SONAR_TOKEN`.

From then on every push is analysed by the CI. The same analysis can be run
locally:

```bash
./mvnw clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.token=YOUR_TOKEN
```

## Documentation

The `docs/` directory holds the project report and the demo video.

## Team

* Ascenzi Gianmarco
* Cicerchia Nicolas
