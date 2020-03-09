package com.wantsome.verifyr.auth

package store

import com.wantsome.common.data._
import doobie._
import doobie.implicits._
import eu.timepit.refined.auto._
import doobie.refined._
import cats.implicits._
import doobie.implicits.javasql._
import doobie.refined.implicits._

trait StoreBackend {
  def sqlInsertInvitation(invitation: Invitation): ConnectionIO[Int]
  def sqlInsertUser(user: User): ConnectionIO[Int]
  def sqlInsertUserGroups(userGroups: List[UserGroup]): ConnectionIO[Int]
  def sqlGetEnglishCombo: ConnectionIO[List[ComboValue]]
  def sqlGetOccupationCombo: ConnectionIO[List[ComboValue]]
  def sqlGetFieldOfWorkCombo: ConnectionIO[List[ComboValue]]
  def sqlGetAllGroups: ConnectionIO[List[Group]]
}

trait LiveStoreBackend extends StoreBackend {

  override def sqlInsertUser(user: User): ConnectionIO[Int] =
    sql"""insert into users(id,email,first_name,last_name,birthday,city,phone,
            occupation,field_of_work,english_level,it_experience,experience_description,heard_from)
            values(${user.id},${user.email},${user.firstName},${user.lastName},${user.birthday},
            ${user.city},${user.phone},${user.occupation},${user.fieldOfWork},${user.englishLevel},
            ${user.itExperience},${user.experienceDescription},${user.heardFrom})""".update.run

  override def sqlInsertUserGroups(userGroups: List[UserGroup]): ConnectionIO[Int] =
    Update[UserGroup]("insert into groups_users(user_id, group_id) values(?,?)").updateMany(userGroups)

  override def sqlInsertInvitation(invitation: Invitation): ConnectionIO[Int] =
    sql"insert into invitations(id,user_id,expires_at) values(${invitation.id}, ${invitation.userId}, ${invitation.expiresAt})".update.run

  override def sqlGetEnglishCombo: ConnectionIO[List[ComboValue]] =
    sql"""select id,value,label from english_level_c""".query[ComboValue].to[List]
  override def sqlGetOccupationCombo: ConnectionIO[List[ComboValue]] =
    sql"""select id,value,label from occupation_c""".query[ComboValue].to[List]
  override def sqlGetFieldOfWorkCombo: ConnectionIO[List[ComboValue]] =
    sql"""select id,value,label from field_of_work_c""".query[ComboValue].to[List]

  override def sqlGetAllGroups: ConnectionIO[List[Group]] =
    sql"select id, description from groups".query[Group].to[List]

}
