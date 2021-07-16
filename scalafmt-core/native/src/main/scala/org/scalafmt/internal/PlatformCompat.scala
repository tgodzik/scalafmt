package org.scalafmt.internal

import scala.util.matching.Regex
import java.util.regex.Pattern

/* Before text matching (?=re), after text matching (?<=re) 
   and more are incompatible in Scala Native, so custom functions 
   have to be used. 
   https://github.com/google/re2/wiki/Syntax  */

object PlatformCompat {

  /* Replaces '\\h', which is incompatible in Scala Native.
     Does not check the correctness of the input regex string. */
  private def fixHorizontalSpaceInRegex(reg: String) = {

    @inline
    def replacingInsideClass =
      "\t \u00A0\u1680\u180E\u2000-\u200A\u202F\u205F\u3000"
    
    @inline
    def replacingOutsideClass =
      s"[$replacingInsideClass]"

    val sb = new StringBuilder()
    var isInClass = false
    var isEscaped = false

    for (char <- reg) {
      char match {
        case '\\' if !isEscaped =>
          isEscaped = true
        case 'h' if isEscaped =>
          sb.append(
            if (isInClass) replacingInsideClass
            else replacingOutsideClass
          )
          isEscaped = false
        case '[' if !isEscaped =>
          sb.append('[')
          isInClass = true
        case ']' if !isEscaped =>
          sb.append(']')
          isInClass = false
        case other =>
          if (isEscaped) {
            isEscaped = false
            sb.append('\\')
          }
          sb.append(other)
      }
    }
    sb.toString()
  }

  @inline
  val trailingSpace =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("\\h+$"),
      Pattern.MULTILINE
    )

  @inline
  val slcDelim =
    Pattern.compile(PlatformCompat.fixHorizontalSpaceInRegex("\\h+"))

  @inline
  val mlcHeader =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("^/\\*\\h*(?:\n\\h*[*]*\\h*)?")
    )
  @inline
  val mlcLineDelim =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("\\h*\n\\h*[*]*\\h*")
    )
  @inline
  val mlcParagraphEnd = Pattern.compile("[.:!?=]$")
  @inline
  val mlcParagraphBeg = Pattern.compile("^(?:[-*@=]|\\d+[.:])")

  @inline
  val leadingAsteriskSpace =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("\n\\h*[*][^*]"),
      Pattern.MULTILINE
    )
  @inline
  val docstringLine =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("^(?:\\h*\\*)?(\\h*)(.*?)\\h*$"),
      Pattern.MULTILINE
    )

  @inline
  val onelineDocstring = {
    val empty =
      PlatformCompat.fixHorizontalSpaceInRegex("\\h*(\n\\h*(?:\\*\\h*)?)*")
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex(
        s"^/\\*\\*$empty([^*\n\\h](?:[^\n]*[^\n\\h])?)$empty\\*/$$"
      )
    )
  }
  @inline
  val docstringLeadingSpace =
    Pattern.compile(PlatformCompat.fixHorizontalSpaceInRegex("^\\h+"))

  @inline
  def compileStripMarginPattern(pipe: Char) =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex(s"\n+\\h*?\\${pipe}"),
      Pattern.MULTILINE
    )

  // see: https://ammonite.io/#Save/LoadSession
  @inline
  val ammonitePattern: Regex =
    // lookahead is not support in native
    "(?:\\s*\\n@)".r

  @inline
  val stripMarginPattern =
    Pattern.compile(
      PlatformCompat.fixHorizontalSpaceInRegex("\n(\\h*\\|)?([^\n]*)"),
      Pattern.MULTILINE
    )

  // startAfterPattern and endBeforePattern should be unique in basePattern
  // basePattern = startAfterPattern + matched pattern + endBeforePattern
  private def replaceAll(
      basePattern: Pattern,
      startAfterPattern: Pattern,
      endBeforePattern: Pattern,
      baseText: String,
      replacingText: String
  ): String = {
    val sb = new StringBuilder()
    val matcher = basePattern.matcher(baseText)
    var currPosition = 0
    while (matcher.find()) {
      val start = matcher.start()
      val end = matcher.end()

      sb.append(baseText.substring(currPosition, start))

      val subtext = baseText.substring(start, end)
      val startAfterMatcher = startAfterPattern.matcher(subtext)
      val endBeforeMatcher = endBeforePattern.matcher(subtext)

      startAfterMatcher.find()
      endBeforeMatcher.find()

      sb.append(startAfterMatcher.group())
      sb.append(replacingText)
      sb.append(endBeforeMatcher.group())

      currPosition = end
    }

    sb.append(baseText.substring(currPosition))
    sb.toString()
  }

  def replaceAllStripMargin(
      stripMarginPattern: Pattern,
      text: String,
      spaces: String,
      pipe: Char
  ): String = {
    val startAfter = Pattern.compile("\n+")
    val endBefore = Pattern.compile(s"\\${pipe}")

    replaceAll(stripMarginPattern, startAfter, endBefore, text, spaces)
  }

  def replaceAllLeadingAsterisk(
      leadingAsteriskSpace: Pattern,
      trimmed: String,
      spaces: String
  ): String = {
    val startAfter = Pattern.compile("\n")
    val endBefore = Pattern.compile("([*][^*])")

    replaceAll(leadingAsteriskSpace, startAfter, endBefore, trimmed, spaces)
  }

  def splitByAmmonitePattern(code: String): Array[String] = {
    def whitespacePattern = Pattern.compile("\\s")
    val actualMatches = ammonitePattern
      .findAllMatchIn(code)
      .filter(regexMatch =>
        regexMatch.end < code.length && whitespacePattern
          .matcher(Character.toString(code.charAt(regexMatch.end)))
          .find()
      )
      .toArray

    val res = new scala.collection.mutable.ArrayBuffer[String]()
    var currPosition = 0
    for (actualMatch <- actualMatches) {
      if (currPosition != actualMatch.start)
        res.append(code.substring(currPosition, actualMatch.start))
      currPosition = actualMatch.end
    }
    res.append(code.substring(currPosition))
    res.toArray
  }
}
