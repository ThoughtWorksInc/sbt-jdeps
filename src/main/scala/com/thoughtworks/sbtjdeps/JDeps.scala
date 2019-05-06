import java.io.File

import sbt.{Def, _}
import Defaults._
import Keys._
import com.thoughtworks.dsl.keywords.Each
import sbt.Defaults.forkOptionsTask
import sbt.plugins.JvmPlugin
import org.scaladebugger.SbtJdiTools
import sbt.internal.util.Attributed.data

/**
  * @author 杨博 (Yang Bo)
  */
object JDeps extends AutoPlugin {

  override def trigger = allRequirements

  override def requires = JvmPlugin && SbtJdiTools

  object autoImport {

    sealed trait JDepsVerbose
    object JDepsVerbose {
      case object Class extends JDepsVerbose
      case object Package extends JDepsVerbose
    }
    sealed trait JDepsFilter
    object JDepsFilter {
      case object Package extends JDepsFilter
      case object Archive extends JDepsFilter
    }

    val jdepsFilter = settingKey[Option[JDepsFilter]]("Dependency filter for jdeps")
    val jdepsVerbose = settingKey[JDepsVerbose]("Verbose level for jdeps")
    val jdepsOptions = settingKey[Seq[String]]("Command-line options for the jdeps")
    val jdeps = taskKey[File]("Run jdeps and returns DOT files for details and summary")
    val jdepsClasses = jdeps / products

  }

  private val jdepsFork = new Fork("jdeps", Some("com.sun.tools.jdeps.Main"))

  import autoImport._

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    jdepsFilter := None,
    jdepsVerbose := JDepsVerbose.Class
  )

  override def projectSettings: Seq[Def.Setting[_]] = {

    Seq(Compile, Test, Runtime).flatMap(
      inConfig(_)(
        inTask(jdeps)(
          runnerSettings ++ Seq(
            target := crossTarget.value / (prefix(configuration.value.name) + "jdeps"),
          )
        ) ++ Seq(
          jdepsOptions := Seq(
            "-dotoutput",
            (jdeps / target).value.toString,
            (jdeps / jdepsVerbose).value match {
              case JDepsVerbose.Class   => "-verbose:class"
              case JDepsVerbose.Package => "-verbose:package"
            },
            (jdeps / jdepsFilter).value match {
              case Some(JDepsFilter.Package) => "-filter:package"
              case Some(JDepsFilter.Archive) => "-filter:archive"
              case None                      => "-filter:none"
            }
          ),
          jdeps := {
            (jdeps / runner).value
              .run(
                mainClass = "com.sun.tools.jdeps.Main",
                classpath = data((jdeps / dependencyClasspath).value),
                options = (jdeps / jdepsOptions).value ++ jdepsClasses.value.map(_.toString),
                log = (jdeps / streams).value.log
              )
              .get
            (jdeps / target).value
          },
        )
      )
    )

  }
}
