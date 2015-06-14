package db

import java.sql.Connection

import anorm._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

/**
 * Created by bryan on 5/20/15.
 */
class DataEventRepository extends QueueDataRepositoryTrait {
  override def addDataToQueue(data: DataEvent)(implicit conn: Connection): Option[Long] = {
    val id: Option[Long] = SQL("insert into Queue (data, server_time) values ({data}, {server_time})")
      .on('data -> Json.stringify(data.data), 'server_time -> data.serverTime)
      .executeInsert()
    return id
  }

  override def getDataFromQueue()(implicit conn: Connection): List[DataEvent] = {
    val query = SQL("select * from Queue").executeQuery()
    query().map(row =>
      DataEvent(Json.parse(row[String]("data"))
        .validate[JsValue].fold(x => throw new Exception, x => x),
        row[Option[DateTime]]("server_time"))
    ).toList
  }

  override def purgeDataFromQueue()(implicit conn: Connection): Int = {
    val query = SQL("delete from Queue")
    val deletedRows = query.executeUpdate()
    deletedRows
  }

  override def getQueueLength()(implicit conn: Connection): Int = {
    val query = SQL("select count(*) as c from queue")
    // First retrieve the first row
    val firstRow = query.apply().head
    // Next get the content of the 'c' column as Long
    firstRow[Int]("c")
  }
}
