import com.typesafe.startscript.StartScriptPlugin

seq(StartScriptPlugin.startScriptForClassesSettings: _*)

organization := "gt4s"

name := "gt4s"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies ++= {
	val unfilteredVersion = "0.5.1"	
	Seq(
    "ch.qos.logback" % "logback-classic" % "0.9.25",
    "org.slf4j" % "jcl-over-slf4j" % "1.6.2" withSources(),
    "net.databinder" %% "unfiltered-filter" % unfilteredVersion,
    "net.databinder" %% "unfiltered-netty" % unfilteredVersion,
    "net.databinder" %% "unfiltered-netty-server" % unfilteredVersion,
    "net.databinder" %% "unfiltered-json" % unfilteredVersion
	)}

