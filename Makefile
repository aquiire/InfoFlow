# a "vanilla" spark submit framework

.PHONY: run
default: build.timestamp

build.timestamp: $(wildcard src/main/scala/*.scala)
	sbt clean assembly
	touch build.timestamp

run: build.timestamp
	@echo spark-submit target/scala-2.11/infoflow_2.11-`/usr/bin/grep version build.sbt | cut -d = -f 2 | cut -d \" -f 2`.jar
	@spark-submit --class InfoFlowMain --master yarn --conf spark.driver.memoryOverhead=8g --conf spark.executor.memoryOverhead=3g --conf spark.driver.memory=24g --conf --conf spark.driver.maxResultSize=24g --deploy-mode client <path/to/jar> <path/to/config>
