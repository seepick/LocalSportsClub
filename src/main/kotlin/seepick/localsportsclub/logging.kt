package seepick.localsportsclub

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.LoggerFactory
import java.io.File

fun prelog(message: String) {
    println("[LSC] $message")
}

fun reconfigureLog(logsDirForFileAppender: File?, packageSettings: Map<String, Level>) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.detachAndStopAllAppenders()
    rootLogger.level = Level.WARN

    rootLogger.addAppender(buildConsoleAppender(context, Level.TRACE))

    if (logsDirForFileAppender != null) {
        rootLogger.addAppender(buildFileAppender(logsDirForFileAppender, context, Level.DEBUG))
        rootLogger.addAppender(buildFileAppender(logsDirForFileAppender, context, Level.WARN, "-warn"))
    }

    packageSettings.forEach { (packageName, logLevel) ->
        context.getLogger(packageName).level = logLevel
    }
}

fun readRecentLogEntries(
    logsDir: File, // = FileResolver.resolve(DirectoryEntry.Logs)
    linesToRead: Int = 30,
): String? {
    val targetLogFile = File(logsDir, "app_logs.log") // TODO extract string constant
    if (!targetLogFile.exists()) return null

    return buildString {
        targetLogFile.bufferedReader().use {
            val lines = it.readLines()
            val linesCount = lines.count()
            appendLine("LOG:")
            lines.drop(if (linesCount > linesToRead) linesCount - linesToRead else 0)
                .forEach {
                    appendLine(it)
                }
        }
    }
}

private fun buildFileAppender(
    logsDir: File, // = FileResolver.resolve(DirectoryEntry.Logs)
    context: LoggerContext,
    level: Level,
    suffix: String = "",
): RollingFileAppender<ILoggingEvent> {
    val targetLogFile = File(logsDir, "app_logs$suffix.log")
    prelog("Writing logs to: ${targetLogFile.absolutePath}")
    return RollingFileAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "CustomFileAppender$suffix"
        appender.encoder = buildPattern(context)
        appender.file = targetLogFile.absolutePath
        appender.isAppend = true
        appender.isImmediateFlush = true
        appender.rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().also { policy ->
            policy.context = context
            policy.setParent(appender)
            policy.fileNamePattern = "${logsDir.absolutePath}/app_logs$suffix-%d{yyyy-MM-dd}.log"
            policy.maxHistory = 3
            policy.start()
        }
        appender.start()
        appender.addFilter(MyThresholdFilter(level))
    }
}

private fun buildConsoleAppender(
    context: LoggerContext,
    level: Level,
): ConsoleAppender<ILoggingEvent> {
    return ConsoleAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "CustomConsoleAppender"
        appender.encoder = buildPattern(context)
        appender.start()
        appender.addFilter(MyThresholdFilter(level))
    }
}

private fun buildPattern(context: LoggerContext) = PatternLayoutEncoder().also { encoder ->
    encoder.context = context
    encoder.pattern = "%d{HH:mm:ss.SSS} %logger{5}.%line@[%-4.30thread] %-5level-%msg %xException{full} %n"
    encoder.start()
}

private class MyThresholdFilter(private val level: Level) : Filter<ILoggingEvent>() {
    init {
        start()
    }

    /** Strangely has to be implemented yourself... */
    override fun decide(event: ILoggingEvent): FilterReply {
        if (event.level.isGreaterOrEqual(level)) {
            return FilterReply.ACCEPT
        }
        return FilterReply.DENY
    }
}
