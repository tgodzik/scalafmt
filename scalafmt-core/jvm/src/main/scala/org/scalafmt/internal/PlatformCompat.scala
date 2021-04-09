package org.scalafmt.internal

import java.util.regex.Pattern
import scala.util.matching.Regex

object PlatformCompat {
  @inline
  def fixRegex(reg: String) = reg

  @inline
  val trailingSpace =
    Pattern.compile("\\h++$", Pattern.MULTILINE)

  @inline
  val slcDelim = Pattern.compile("\\h++")

  @inline
  val mlcHeader =
    Pattern.compile("^/\\*\\h*+(?:\n\\h*+[*]*+\\h*+)?")
  @inline
  val mlcLineDelim =
    Pattern.compile(PlatformCompat.fixRegex("\\h*+\n\\h*+[*]*+\\h*+"))
  @inline
  val mlcParagraphEnd = Pattern.compile("[.:!?=]$")
  @inline
  val mlcParagraphBeg = Pattern.compile("^(?:[-*@=]|\\d++[.:])")

  @inline
  val leadingAsteriskSpace =
    Pattern.compile("(?<=\n)\\h*+(?=[*][^*])")
  @inline
  val docstringLine =
    Pattern.compile(
      "^(?:\\h*+\\*)?(\\h*+)(.*?)\\h*+$",
      Pattern.MULTILINE
    )
  @inline
  val onelineDocstring = {
    val empty = "\\h*+(\n\\h*+\\*?\\h*+)*"
    Pattern.compile(
      s"^/\\*\\*$empty([^*\n\\h](?:[^\n]*[^\n\\h])?)$empty\\*/$$"
    )
  }
  @inline
  val docstringLeadingSpace = Pattern.compile("^\\h++")

  @inline
  def compileStripMarginPattern(pipe: Char) =
    Pattern.compile(PlatformCompat.fixRegex(s"(?<=\n)\\h*+(?=\\${pipe})"))

  // see: https://ammonite.io/#Save/LoadSession
  @inline
  val ammonitePattern: Regex = "(?:\\s*\\n@(?=\\s))+".r

  @inline
  val stripMarginPattern =
    Pattern.compile("\n(\\h*+\\|)?([^\n]*+)")
}
