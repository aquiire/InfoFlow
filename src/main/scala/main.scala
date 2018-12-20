/*****************************************************************************
 * Main function
 *****************************************************************************/

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object InfoFlowMain {
  def main( args: Array[String] ): Unit = {

  /***************************************************************************
   * read in config file
   ***************************************************************************/

    // check argument size
    if( args.size > 1 ) {
      println("InfoFlow: requires 0-1 arguments:")
      println("./InfoFlow [alternative config file]")
      return
    }

    // use default or alternative config file name
    val configFileName =
      if( args.size == 0 ) "config.json"
      else /*args.size==1*/ args(0)
    val config = ConfigFile(configFileName)

    // initialize community detection algorithm
    val communityDetection = CommunityDetection.choose( config.algorithm )

  /***************************************************************************
   * Initialize Spark Context
   ***************************************************************************/
    val conf = new SparkConf()
      .setAppName("InfoFlow")
      .setMaster( config.master )
    val sc = new SparkContext(conf)
    //sc.setLogLevel("OFF")

    // create log file object
    val logFile = new LogFile(
      sc,
      config.logFile.pathLog,
      config.logFile.pathParquet,
      config.logFile.pathRDD,
      config.logFile.pathTxt,
      config.logFile.pathFullJson,
      config.logFile.pathReducedJson,
      config.logFile.debug
    )

    // log app version, platform specifications
    {
      // log spark, hdfs versions
      val jar = sc.jars.head.split('/').last
      val version = jar.split('-').last.split('.').dropRight(1).mkString(".")
      logFile.write(s"Running ${sc.appName}, version: $version\n",false)
      logFile.write(s"Jar: $jar\n",false)
      logFile.write(s"Spark version: ${sc.version}\n",false)
    }

    /***************************************************************************
      * read file, solve, save
      ***************************************************************************/

    logFile.write(s"Reading ${config.graphFile}\n",false)
    val graph0: Graph = GraphReader( sc, config.graphFile )
    logFile.write(s"Read in network with ${graph0.vertices.count} nodes"
      +s" and ${graph0.edges.count} edges\n",false)

    logFile.write(s"Initializing partitioning, calculating PageRank\n",false)
    val part0: Partition = Partition.init( graph0, config.tele )
    logFile.write(s"Finished initialization calculations\n",false)

    logFile.write(s"Using ${config.algorithm} algorithm:\n",false)
    val (graph1,part1) = communityDetection( graph0, part0, logFile )

    logFile.write( s"Save final graph with"
      +s" ${part1.vertices.count} modules"
      +s" and ${part1.edges.count} connections\n",
    false)
    logFile.save( graph1, part1, false, "" )

    logFile.write("InfoFlow Terminate\n",false)
    logFile.close

  /***************************************************************************
   * Stop Spark Context
   ***************************************************************************/
    sc.stop
  }
}
