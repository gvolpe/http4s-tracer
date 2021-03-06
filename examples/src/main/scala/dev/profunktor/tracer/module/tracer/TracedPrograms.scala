/*
 * Copyright 2018-2020 ProfunKtor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.profunktor.tracer.module.tracer

import cats.Parallel
import cats.effect.Sync
import cats.syntax.apply._
import dev.profunktor.tracer.Trace.Trace
import dev.profunktor.tracer.TracerLog
import dev.profunktor.tracer.algebra.UserAlgebra
import dev.profunktor.tracer.model.user.{User, Username}
import dev.profunktor.tracer.module.{LivePrograms, Programs}

case class TracedPrograms[F[_]: Parallel: Sync: λ[T[_] => TracerLog[Trace[T, ?]]]](
    repos: TracedRepositories[F],
    clients: TracedHttpClients[F]
) extends Programs[Trace[F, ?]] {
  private val programs = LivePrograms[Trace[F, ?]](repos, clients)

  override val users: UserAlgebra[Trace[F, ?]] = new UserTracer[F](programs.users)
}

private[tracer] final class UserTracer[F[_]: Sync](
    users: UserAlgebra[Trace[F, ?]]
)(implicit L: TracerLog[Trace[F, ?]])
    extends UserAlgebra[Trace[F, ?]] {

  override def find(username: Username): Trace[F, User] =
    L.info[UserAlgebra[F]](s"Find user by username: ${username.value}") *> users.find(username)

  override def persist(user: User): Trace[F, Unit] =
    L.info[UserAlgebra[F]](s"About to persist user: ${user.username.value}") *> users.persist(user)

}
