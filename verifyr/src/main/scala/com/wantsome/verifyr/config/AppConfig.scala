package com.wantsome

package verifyr

package config

import zio._
import doobie.util.transactor.Transactor

case class AppConfig(transactor: Transactor[Task])
