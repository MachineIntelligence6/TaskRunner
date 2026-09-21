# TaskRunner

TaskRunner is a small Java library for running synchronous and asynchronous tasks with a consistent callback and cleanup lifecycle. It also includes optional Swing support for blocking a window with a loading overlay while a foreground task is running.

## Features

- Run tasks synchronously on the current thread
- Queue tasks sequentially on a single background thread
- Run independent background tasks concurrently
- Return results or errors through a typed callback
- Execute cleanup logic after each task
- Publish progress messages to listeners
- Block a Swing window with a loading overlay during foreground work
- Create named worker threads for easier debugging

## Requirements

- JDK 11 or later
- Apache Maven 3.6 or later

## Build

Clone the repository and run the tests:

```bash
git clone https://github.com/MachineIntelligence6/TaskRunner.git
cd TaskRunner
mvn clean test
```

Create the JAR:

```bash
mvn clean package
```

The generated JAR will be available in the `target/` directory.

## Use in another Maven project

TaskRunner is not currently published to a public Maven repository. Install it in your local Maven repository first:

```bash
mvn clean install
```

Then add the dependency to your application's `pom.xml`:

```xml
<dependency>
    <groupId>com.mi6</groupId>
    <artifactId>taskrunner</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Quick start

Create a task by extending `Task<T>`. Put the work in `execute()` and release task-specific resources in `cleanup()`.

```java
import com.mi6.task.Task;

Task<String> task = new Task<>("Load customer") {
    @Override
    protected String execute() {
        return "Customer loaded";
    }

    @Override
    protected void cleanup() {
        // Close or release task-specific resources here.
    }
};

task.runConcurrentlyInBackground((result, error) -> {
    if (error != null) {
        error.printStackTrace();
        return;
    }

    System.out.println(result);
});
```

The callback receives either:

- the task result and `null` for the error, or
- `null` for the result and the thrown error.

## Execution modes

| Method or runner | Execution model | Suitable for |
| --- | --- | --- |
| `task.runHere(callback)` | Current thread | Work that must finish before execution continues |
| `task.runSequentiallyInBackground(callback)` | One shared worker thread | Database operations, ordered work, or memory-intensive tasks |
| `task.runConcurrentlyInBackground(callback)` | Shared fixed pool of 20 threads | Independent or blocking operations such as HTTP requests |
| `task.runSequentiallyInForeground(callback)` | One shared worker thread with a Swing loading overlay | Tasks that should temporarily block user interaction |
| `ConcurrentForegroundTaskRunner.submit(runnable)` | Shared fixed pool of 20 threads | Direct submission of concurrent named foreground runnables |

A callback is required for `runHere`, `runSequentiallyInBackground(Then)`, and `runConcurrentlyInBackground`. A no-callback overload is also available for sequential background and foreground execution.

## Progress messages

A task can publish a status message by calling `setText(...)` from inside `execute()`:

```java
Task<Integer> importTask = new Task<>("Import records") {
    @Override
    protected Integer execute() {
        setText("Reading input");
        // Read input...

        setText("Saving records");
        // Save records...

        return 42;
    }

    @Override
    protected void cleanup() {
        // Release resources.
    }
};
```

For a foreground Swing task, these messages update the text displayed by the loading overlay. Other callers can observe the same messages with a property-change listener:

```java
importTask.addListener(event -> {
    if ("message".equals(event.getPropertyName())) {
        System.out.println(event.getNewValue());
    }
});
```

## Swing foreground tasks

Register the application's main frame before submitting a foreground task:

```java
import com.mi6.task.SingleForegroundTaskRunner;
import com.mi6.task.Task;

SingleForegroundTaskRunner.setMainRootPane(mainFrame);

Task<String> refreshTask = new Task<>("Refresh data") {
    @Override
    protected String execute() {
        setText("Refreshing data");
        return loadData();
    }

    @Override
    protected void cleanup() {
        // Release resources.
    }
};

refreshTask.runSequentiallyInForeground((result, error) -> {
    javax.swing.SwingUtilities.invokeLater(() -> {
        if (error != null) {
            showError(error);
        } else {
            updateScreen(result);
        }
    });
});
```

Use `setRootPane(JFrame)` or `setRootPane(JDialog)` when a specific window should be blocked for the next foreground task. Otherwise, the registered main root pane is used.

Callbacks run on the calling or worker thread selected by the execution mode. Swing component updates should therefore be moved to the Event Dispatch Thread with `SwingUtilities.invokeLater(...)`.

## Direct runner usage

The shared executors can also accept plain `Runnable` instances:

```java
SingleBackgroundTaskRunner.submit(() -> runOrderedJob());
ConcurrentBackgroundTaskRunner.submit(() -> callRemoteService());
```

## Shutdown

The runners use shared executor services. Shut them down when the application exits:

```java
SingleBackgroundTaskRunner.shutdown();
ConcurrentBackgroundTaskRunner.shutdown();
SingleForegroundTaskRunner.shutdown();
ConcurrentForegroundTaskRunner.shutdown();
```

Shutdown first waits up to three seconds for queued work, requests cancellation if required, and then waits up to two more seconds.

## Project structure

```text
src/main/java/com/mi6/task/
├── Task.java                              Task lifecycle and execution methods
├── Then.java                              Result/error callback
├── AsyncUtils.java                        Named thread factory
├── TaskUtils.java                         Executor shutdown helper
├── SingleBackgroundTaskRunner.java        Single-thread background executor
├── ConcurrentBackgroundTaskRunner.java    20-thread background executor
├── SingleForegroundTaskRunner.java        Swing UI-blocking executor
├── ConcurrentForegroundTaskRunner.java    20-thread executor
└── swing/
    └── TpcDisabledGlassPane.java           Swing loading overlay
```

## Main dependencies

- Google Guava
- SLF4J and Log4j 2
- Lombok
- JUnit 3 (tests)

## Notes

- `cleanup()` is always called by the callback-based task wrappers, whether the task succeeds or fails.
- The executors are shared singletons.
- Foreground task support requires a registered Swing root pane.
- The project does not currently include a license file.
