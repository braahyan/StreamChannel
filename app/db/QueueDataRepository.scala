package db

import java.sql.Connection

import anorm._
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}

/**
 * Created by bryan on 5/20/15.
 */
class QueueDataRepository extends QueueDataRepositoryTrait {
  override def addDataToQueue(data: QueueData)(implicit conn: Connection): Option[Long] = {
    val id: Option[Long] = SQL("insert into Queue (gathered_time, data, server_time) values ({gathered_time}, {data}, {server_time})")
      .on('gathered_time -> data.time, 'data -> Json.stringify(data.data), 'server_time -> data.serverTime)
      .executeInsert()
    return id
  }

  override def getDataFromQueue()(implicit conn: Connection): List[QueueData] = {
    val query = SQL("select * from Queue").executeQuery()
    query().map(row =>
      QueueData(Json.parse(row[String]("data"))
        .validate[JsValue].fold(x => throw new Exception, x => x),
        row[DateTime]("gathered_time"),
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
