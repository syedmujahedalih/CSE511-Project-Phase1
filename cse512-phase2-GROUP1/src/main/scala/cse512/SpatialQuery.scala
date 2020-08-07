package cse512

import org.apache.spark.sql.SparkSession

object SpatialQuery extends App{
  def ST_Contains(queryRectangle: String, pointString: String): Boolean = {

    var rectangle = new Array[String](4)
    rectangle = queryRectangle.split(",")
    val x1 = rectangle(0).trim.toDouble
    val y1 = rectangle(1).trim.toDouble
    val x2 = rectangle(2).trim.toDouble
    val y2 = rectangle(3).trim.toDouble

    var point = new Array[String](2)
    point = pointString.split(",")
    val x = point(0).trim.toDouble
    val y = point(1).trim.toDouble

    val x_low = math.min(x1, x2)
    val x_high = math.max(x1, x2)
    val y_low = math.min(y1, y2)
    val y_high = math.max(y1, y2)

    if (x < x_low || x > x_high || y < y_low || y > y_high)
      return false
    else
      return true
  }

  def ST_Within(pointString1: String, pointString2: String, distance: Double): Boolean = {

    var point1 = new Array[String](2)
    point1 = pointString1.split(",")
    val x1 = point1(0).trim.toDouble
    val y1 = point1(1).trim.toDouble

    var point2 = new Array[String](2)
    point2 = pointString2.split(",")
    val x2 = point2(0).trim.toDouble
    val y2 = point2(1).trim.toDouble

    val d = math.sqrt(math.pow((x1 - x2), 2) + math.pow((y1 - y2), 2))

    if (d <= distance)
      return true
    else
      return false
  }

  def runRangeQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((ST_Contains(queryRectangle, pointString))))

    val resultDf = spark.sql("select * from point where ST_Contains('"+arg2+"',point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runRangeJoinQuery(spark: SparkSession, arg1: String, arg2: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    val rectangleDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    rectangleDf.createOrReplaceTempView("rectangle")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Contains",(queryRectangle:String, pointString:String)=>((ST_Contains(queryRectangle, pointString))))

    val resultDf = spark.sql("select * from rectangle,point where ST_Contains(rectangle._c0,point._c0)")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((ST_Within(pointString1, pointString2, distance))))

    val resultDf = spark.sql("select * from point where ST_Within(point._c0,'"+arg2+"',"+arg3+")")
    resultDf.show()

    return resultDf.count()
  }

  def runDistanceJoinQuery(spark: SparkSession, arg1: String, arg2: String, arg3: String): Long = {

    val pointDf = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg1);
    pointDf.createOrReplaceTempView("point1")

    val pointDf2 = spark.read.format("com.databricks.spark.csv").option("delimiter","\t").option("header","false").load(arg2);
    pointDf2.createOrReplaceTempView("point2")

    // YOU NEED TO FILL IN THIS USER DEFINED FUNCTION
    spark.udf.register("ST_Within",(pointString1:String, pointString2:String, distance:Double)=>((ST_Within(pointString1, pointString2, distance))))
    val resultDf = spark.sql("select * from point1 p1, point2 p2 where ST_Within(p1._c0, p2._c0, "+arg3+")")
    resultDf.show()

    return resultDf.count()
  }
}
