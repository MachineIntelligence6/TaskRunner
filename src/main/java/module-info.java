module TaskRunner {
	exports com.mi6.task;
	exports com.mi6.task.swing;

	requires transitive com.google.common;
	requires transitive java.desktop;
	requires transitive lombok;
	requires transitive slf4j.api;
}