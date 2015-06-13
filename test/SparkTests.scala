
package test

import db.WebEvent
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mutable.{BeforeAfter, SpecificationWithJUnit}
import org.specs2.runner.JUnitRunner
import org.specs2.specification.{Fragments, Step}
import spark.AnalyticsApp

/**
 * Created by bryan on 6/9/15.
 */
abstract class SparkJobSpecification extends SpecificationWithJUnit with BeforeAfter {

  @transient var sc: SparkContext = _

  override def before = {
    System.clearProperty("spark.driver.port")
    System.clearProperty("spark.hostPort")

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("test")
    sc = new SparkContext(conf)
  }

  override def after = {
    if (sc != null) {
      sc.stop()
      sc = null
      System.clearProperty("spark.driver.port")
      System.clearProperty("spark.hostPort")
    }
  }

  override def map(fs: => Fragments) = Step(before) ^ super.map(fs) ^ Step(after)

}

@RunWith(classOf[JUnitRunner])
class SparkJobSpec extends SparkJobSpecification {

  "Spark Analytics" should {
    val input = Seq(
      (new DateTime(2015, 6, 10,1,0), new WebEvent("http://one.com/foo",Some("http://reddit.com"),"page-view",1,false)),
      (new DateTime(2015, 6, 10,2,0), new WebEvent("http://one.com/bar",Some("http://thrivehive.com"),"page-view",1,false)),
      (new DateTime(2015, 6, 10,2,0), new WebEvent("http://two.com/baz",Some("http://xkcd.com"),"page-view",1,false)),
      (new DateTime(2015, 6, 10,2,0), new WebEvent("http://two.com/quux",None,"page-view",2,false))
    )



    "groups by hour and site correctly" in {
      val inputRdd = sc.parallelize(input)
      val rdd = AnalyticsApp.groupByHourAndSite(inputRdd)
      val grouped = rdd.collect()

      grouped.length shouldEqual 3
      grouped.count(x => x._1 ==(new DateTime(2015, 6, 10, 1, 0), "one.com")) shouldEqual 1
      grouped.count(x => x._1 ==(new DateTime(2015, 6, 10, 2, 0), "one.com")) shouldEqual 1
      grouped.count(x => x._1 ==(new DateTime(2015, 6, 10, 2, 0), "two.com") && x._2.size == 2) shouldEqual 1
    }

    "counts hour and site correctly" in {
      val inputRdd = sc.parallelize(input)
      val rdd = AnalyticsApp.eventsByHourAndSite(AnalyticsApp.groupByHourAndSite(inputRdd))
      val grouped = rdd.collect()

      grouped.length shouldEqual(3)
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 1, 0) && x.website == "one.com" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) && x.website == "one.com") shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) && x.website == "two.com") shouldEqual 1
    }

    "counts page views by hour and site correctly" in {
      val inputRdd = sc.parallelize(input)
      val rdd = AnalyticsApp.pageViewsByHourAndSite(AnalyticsApp.groupByHourAndSite(inputRdd))
      val grouped = rdd.collect()

      grouped.length shouldEqual 4
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 1, 0) &&
        x.website == "one.com" &&
        x.page == "/foo" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "one.com" &&
        x.page == "/bar" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "two.com" &&
        x.page == "/baz" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "two.com" &&
        x.page == "/quux" && x.value == 1) shouldEqual 1

    }


    "counts referrers by hour and site correctly" in {
      val inputRdd = sc.parallelize(input)
      val rdd = AnalyticsApp.referrersByHourAndSite(AnalyticsApp.groupByHourAndSite(inputRdd))
      val grouped = rdd.collect()

      grouped.length shouldEqual(4)
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 1, 0) &&
        x.website == "one.com" &&
        x.referrer == "http://reddit.com" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "one.com" &&
        x.referrer == "http://thrivehive.com" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "two.com" &&
        x.referrer == "http://xkcd.com" && x.value == 1) shouldEqual 1
      grouped.count(x => x.date == new DateTime(2015, 6, 10, 2, 0) &&
        x.website == "two.com" &&
        x.referrer == "direct" && x.value == 1) shouldEqual 1

    }
  }
}