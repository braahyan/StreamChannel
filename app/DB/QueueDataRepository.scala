package DB
import anorm._
import java.sql.{ Connection}

import org.joda.time.DateTime

/**
 * Created by bryan on 5/20/15.
 */
class QueueDataRepository {
  def addDataToQueue(timestamp: DateTime, data:String)(implicit conn:Connection): Option[Long] ={
    val id:Option[Long] = SQL("insert into Queue (gathered_time, data) values ({gathered_time}, {data})")
             .on('gathered_time -> timestamp, 'data -> data)
             .executeInsert()
    return id
  }

  def getDataFromQueue()(implicit conn:Connection): Unit = {
    val query = SQL("select * from Queue").executeQuery()
    query().map(row =>
      row[DateTime]("gathered_time") -> row[String]("data")
    ).toList
  }

}
