package org.scalafmt.cli

import java.time.Instant

trait TermUtils {

  // TODO print according to format
  // private val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  protected def formatTimestamp(ts: Long): String ={
    Instant.ofEpochMilli(ts).toString
    
  }
  def noConsole = true
}
