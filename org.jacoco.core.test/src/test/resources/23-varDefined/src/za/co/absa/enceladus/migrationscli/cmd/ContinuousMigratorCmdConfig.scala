/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.migrationscli.cmd

import scopt.OptionParser

/**
  * This is a class for configuration provided by the command line parameters
  * to the command line Continuous Migration Tool
  *
  * Note: scopt requires all fields to have default values.
  * Even if a field is mandatory it needs a default value.
  */
case class ContinuousMigratorCmdConfig(mongoDbUrlSrc: String = "",
                                       mongoDbUrlTrg: String = "",
                                       databaseSrc: String = "",
                                       databaseTrg: String = "")



object ContinuousMigratorCmdConfig {

  def apply(args: Array[String]): ContinuousMigratorCmdConfig = {
    val parser = new CmdParser("java -cp enceladus-migrations-cli.jar " +
      "za.co.absa.enceladus.migrationscli.ContinuousMigratorApp " +
      "--src-mongodb-url <MongoDb URL> " +
      "--trg-mongodb-url <MongoDb URL> " +
      "--src-database <Database Name> " +
      "--trg-database <Database Name>")

    val optionCmd = parser.parse(args, ContinuousMigratorCmdConfig())
    if (optionCmd.isEmpty) {
      // Wrong arguments provided, the message is already displayed
      System.exit(1)
    }
    optionCmd.get
  }

  private class CmdParser(programSyntax: String) extends OptionParser[ContinuousMigratorCmdConfig](programSyntax) {
    head("\nContinuous Migration", "")
    var rawFormat: Option[String] = None

    opt[String]("src-mongodb-url").required().action((value, config) =>
      config.copy(mongoDbUrlSrc = value)).text("A source MongoDB URL")

    opt[String]("trg-mongodb-url").required().action((value, config) =>
      config.copy(mongoDbUrlTrg = value)).text("A target MongoDB URL")

    opt[String]("src-database").required().action((value, config) =>
      config.copy(databaseSrc = value)).text("A source database name")

    opt[String]("trg-database").required().action((value, config) =>
      config.copy(databaseTrg = value)).text("A target database name")

    help("help").text("prints this usage text")
  }

}
