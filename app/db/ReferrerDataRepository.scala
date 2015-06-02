package db

import java.sql.Connection

import anorm._
import org.joda.time.DateTime

/**
 * Created by bryan on 6/1/15.
 */
class ReferrerDataRepository() {
  def UpdateData(data:Seq[ReferrerData])(implicit conn:Connection): Unit ={
    data.foreach{
      data =>
      val id: Option[Long] = SQL("insert into ReferrerData (data_time,website,referrer,visitors) values ({date}, {website}, {referrer}, {visitors})" +
        " on duplicate key update visitors = {visitors} ")
        .on('date -> data.date, 'website -> data.website, 'referrer->data.referrer, 'visitors->data.value)
        .executeInsert()
    }
  }

  def GetData()(implicit conn:Connection) = {
    val query = SQL("select * from ReferrerData").executeQuery()
    query().map(row =>
      ReferrerData(row[DateTime]("data_time"),row[String]("website"),row[String]("referrer"),row[Int]("visitors"))
    ).toList
  }
}

class VisitDataRepository() {
  def UpdateData(data:Seq[VisitData])(implicit conn:Connection): Unit ={
    data.foreach {
      data =>
        val id: Option[Long] = SQL("insert into VisitorData (data_time,website,visitors) values ({date}, {website}, {visitors})" +
          " on duplicate key update visitors = {visitors} ")
          .on('date -> data.date, 'website -> data.website, 'visitors -> data.value)
          .executeInsert()
    }
  }

  def GetData()(implicit conn:Connection) = {
    val query = SQL("select * from VisitorData").executeQuery()
    query().map(row =>
      VisitData(row[DateTime]("data_time"),row[String]("website"),row[Int]("visitors"))
    ).toList
  }
}