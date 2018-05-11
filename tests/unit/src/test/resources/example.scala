/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foo.bar

import scala.collection.Seq

sealed abstract class CS {

  def value: Byte

  def foo: Option[String] =
    if(true) Some("foo") else None

  protected def myParam: Boolean = true
}

object CS {

  // Leading Comment
  case object CS0 extends CS {
    def value = 0
    override protected def myParam: Boolean = false
  }

  case object CS1 extends CS {
    def value = 1
  }

  case object CS2 extends CS {
    def value = 2
    // Leading Comment 1
    // Leading Comment 2
    override def foo = Some("MS")
  }

  case object CS3 extends CS {
    def value = 3
  }

  case object CS4 extends CS {
    def value = 4
  }

  case object CS5 extends CS {
    def value = 5
  }

  case object CS6 extends CS {
    def value = 6
  }

  case object CS7 extends CS {
    def value = 7
  }

  case object CS8 extends CS {
    def value = 8
  }

  case object CS9 extends CS {
    def value = 9
  }

  case object CS10 extends CS {
    def value = 10
  }

  case object CS11 extends CS {
    def value = 11
  }

  case object CS12 extends CS {
    def value = 12
  }

  case object CS13 extends CS {
    def value = 13
  }

  val values: Seq[CS] = Seq(CS0, CS1, CS2, CS3, CS4, CS5, CS6, CS7, 
    CS8, CS9, CS10, CS11, CS12, CS13)
}