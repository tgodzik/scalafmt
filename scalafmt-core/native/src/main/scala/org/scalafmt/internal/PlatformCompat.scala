package org.scalafmt.internal

import scala.util.matching.Regex
import java.util.regex.Pattern

object PlatformCompat {
  @inline
  private def fixRegex(reg: String) = {
    reg
      .replace(
        "\\h",
        "[\t \u00A0\u1680\u180E\u2000-\u200A\u202F\u205F\u3000]"
      )
  }

  @inline
  val trailingSpace =
    Pattern.compile(PlatformCompat.fixRegex("\\h+$"), Pattern.MULTILINE)

  @inline
  val slcDelim = Pattern.compile(PlatformCompat.fixRegex("\\h+"))

  @inline
  val mlcHeader =
    Pattern.compile(
      PlatformCompat.fixRegex("^/\\*\\h*(?:\n\\h*[*]*\\h*)?")
    )
  @inline
  val mlcLineDelim =
    Pattern.compile(PlatformCompat.fixRegex("\\h*\n\\h*[*]*\\h+"))
  @inline
  val mlcParagraphEnd = Pattern.compile("[.:!?=]$")
  @inline
  val mlcParagraphBeg = Pattern.compile("^(?:[-*@=]|\\d+[.:])")

  
  @inline
  val leadingAsteriskSpace =
    Pattern.compile(
      PlatformCompat.fixRegex("^\\h*([*][^*])"),
      Pattern.MULTILINE// | Pattern.UNIX_LINES
    )
  @inline
  val docstringLine =
    Pattern.compile(
      PlatformCompat.fixRegex("^(?:\\h*\\*)?(\\h*)([^\\h\n]*?)\\h*$"),
      Pattern.MULTILINE //| Pattern.UNIX_LINES
    )

  @inline
  val onelineDocstring = {
    val empty = PlatformCompat.fixRegex("\\h*(\n\\h*(?:\\*\\h*)?)*")
    Pattern.compile(
      PlatformCompat.fixRegex(
        s"^/\\*\\*$empty([^*\n\\h](?:[^\n]*[^\n\\h])?)$empty\\*/$$"
      )
    )
  }
  @inline
  val docstringLeadingSpace =
    Pattern.compile(PlatformCompat.fixRegex("^\\h+"))

  @inline
  def compileStripMarginPattern(pipe: Char) =
    Pattern.compile(
      PlatformCompat.fixRegex(s"^\\h*"),
      Pattern.MULTILINE // | Pattern.UNIX_LINES
    )

  // see: https://ammonite.io/#Save/LoadSession
  @inline
  val ammonitePattern: Regex =
    // lookahead is not support in native
    "(?:\\s*\\n@\\s)+".r

  val stripMarginPattern =
    Pattern.compile(PlatformCompat.fixRegex("\n(\\h*\\|)?([^\n]*)"))
}
