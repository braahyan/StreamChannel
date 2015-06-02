package upload

import awscala.s3._
import db.{VisitData, ReferrerData}
import play.api.Play
import play.api.libs.json.Json

import scala.io.Source


/**
 * Created by bryan on 5/29/15.
 */
class S3Downloader {


  val bucketName = Play.current.configuration.getString("streamchannel.s3.bucketName").get
  val keyRoot = Play.current.configuration.getString("streamchannel.s3.writeTarget").get

  def GetBucket(bucketName:String)(implicit s3:S3) : Bucket = {
    s3.bucket(bucketName) match{
      case None => throw new Exception()
      case Some(y) => y
    }
  }

  private def HadoopSuccess(files:Seq[String]) :Boolean = {
    files.filter(x=>x.endsWith("_SUCCESS")).size > 0
  }

  private def GetPartFileKeys(files:Seq[String]):Seq[String] ={
    files.filter(x=>x.contains("part-"))
  }



  def GetReferrerData()(implicit s3:S3): Seq[ReferrerData] = {

    val keys = GetBucket(bucketName).keys("output/referrersByHour")
    if(HadoopSuccess(keys)){
      val filesPartKeys = GetPartFileKeys(keys)
      val bucket = GetBucket(bucketName)
      val files = filesPartKeys.map(x=>(bucket.get(x)))
      val fileContents = files.flatMap(x=>x match{
        case Some(y) => Source.fromInputStream(y.content).getLines().map(x=>Json.parse(x).validate[ReferrerData].get) // we wrote them in as individual strings should abstract this.
        case None    => throw new Exception("OMGOMGOMG")    // i don't think that there should ever be a situation where this is true
      })
      return fileContents
    }
    return Nil
  }

  def GetVisitorData()(implicit s3:S3): Seq[VisitData] = {
    val keys = GetBucket(bucketName).keys("output/webeventsByHour")
    if(HadoopSuccess(keys)){
      val filesPartKeys = GetPartFileKeys(keys)
      val bucket = GetBucket(bucketName)
      val files = filesPartKeys.map(x=>(bucket.get(x)))
      val fileContents = files.flatMap(x=>x match{
        case Some(y) => Source.fromInputStream(y.content).getLines().map(x=>Json.parse(x).validate[VisitData].get) // we wrote them in as individual strings should abstract this.
        case None    => throw new Exception("OMGOMGOMG")    // i don't think that there should ever be a situation where this is true
      })
    return fileContents
    }
    return Nil
  }

}
