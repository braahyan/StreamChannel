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

  def GetData(website:String)(implicit conn:Connection) = {
    val query = SQL("select * from ReferrerData where website like {website}")
      .on('website -> website)
      .executeQuery()
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

  def GetData(website:String)(implicit conn:Connection) = {
    val query = SQL("select * from VisitorData where website like {website}")
      .on('website -> website)
      .executeQuery()
    query().map(row =>
      VisitData(row[DateTime]("data_time"),row[String]("website"),row[Int]("visitors"))
    ).toList
  }
}

class PageDataRepository() {
  def UpdateData(data:Seq[PageData])(implicit conn:Connection): Unit ={
    data.foreach {
      data =>
        val id: Option[Long] = SQL("insert into PageData (data_time,website,page,visitors) values ({date}, {website}, {page}, {visitors})" +
          " on duplicate key update visitors = {visitors} ")
          .on('date -> data.date, 'website -> data.website, 'page -> data.page,'visitors -> data.value)
          .executeInsert()
    }
  }

  def GetData(website:String)(implicit conn:Connection) = {
    val query = SQL("select * from PageData where website like {website}")
      .on('website -> website)
      .executeQuery()
    query().map(row =>
      PageData(row[DateTime]("data_time"),row[String]("website"),row[String]("page"),row[Int]("visitors"))
    ).toList
  }
}

class WebsiteRepository() {
  def UpdateData(data:Seq[String])(implicit conn:Connection): Unit ={
    data.filter(x=>x != "").foreach {
      data =>
        val id: Option[Long] = SQL("insert into Websites (website) values ({website})" +
          "ON DUPLICATE KEY UPDATE website = website")
          .on('website -> data)
          .executeInsert()
    }
  }

  def GetData()(implicit conn:Connection) = {
    val query = SQL("select * from Websites")
      .executeQuery()
    query().map(row =>
      Website(row[String]("website"))
    ).toList
  }
}