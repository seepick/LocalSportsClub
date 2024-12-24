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
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import java.io.File

fun reconfigureLog(useFileAppender: Boolean) {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.detachAndStopAllAppenders()

    if (useFileAppender) {
        rootLogger.addFileAppender(context)
    }
    rootLogger.addConsoleAppender(context)
    rootLogger.level = Level.WARN

    mapOf(
        "seepick.localsportsclub" to Level.TRACE,
        "liquibase" to Level.INFO,
        "Exposed" to Level.INFO,
    ).forEach { (packageName, logLevel) ->
        context.getLogger(packageName).level = logLevel
    }
}

private fun Logger.addFileAppender(context: LoggerContext) {
    val logsDir = FileResolver.resolve(DirectoryEntry.ApplicationLogs)
    val targetLogFile = File(logsDir, "lsc.log")
    println("[LSC] Writing logs to: ${targetLogFile.absolutePath}")
    addAppender(RollingFileAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "LSCFileAppender"
        appender.encoder = buildPattern(context)
        appender.file = targetLogFile.absolutePath
        appender.isAppend = true
        appender.isImmediateFlush = true
        appender.rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().also { policy ->
            policy.context = context
            policy.setParent(appender)
            policy.fileNamePattern = "${logsDir.absolutePath}/lsc-%d{yyyy-MM-dd}.log"
            policy.maxHistory = 3
            policy.start()
        }
        appender.start()
        appender.addFilter(MyThresholdFilter(Level.TRACE))
    })
}

private fun Logger.addConsoleAppender(context: LoggerContext) {
    addAppender(ConsoleAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "LSCConsoleAppender"
        appender.encoder = buildPattern(context)
        appender.start()
        appender.addFilter(MyThresholdFilter(Level.TRACE))
    })
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
