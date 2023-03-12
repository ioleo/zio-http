/*
 * Copyright 2021 - 2023 Sporta Technologies PVT LTD & the ZIO HTTP contributors.
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

package zio.http.html

import zio.stacktracer.TracingImplicits.disableAutoTrace // scalafix:ok;

/**
 * Light weight DOM implementation that can be rendered as a html string.
 *
 * @see
 *   <a
 *   href="https://html.spec.whatwg.org/multipage/syntax.html#void-elements">Void
 *   elements</a> only have a start tag; end tags must not be specified for void
 *   elements.
 */
sealed trait Dom { self =>
  def encode: CharSequence = self match {
    case Dom.Element(name, children) =>
      val attributes = children.collect { case self: Dom.Attribute => self.encode }

      val elements = children.collect {
        case self: Dom.Element => self.encode
        case self: Dom.Text    => self.encode
      }

      val noElements   = elements.isEmpty
      val noAttributes = attributes.isEmpty
      val isVoid       = Element.isVoid(name)

      if (noElements && noAttributes && isVoid) s"<$name/>"
      else if (noElements && isVoid) s"<$name ${attributes.mkString(" ")}/>"
      else if (noAttributes) s"<$name>${elements.mkString("")}</$name>"
      else s"<$name ${attributes.mkString(" ")}>${elements.mkString}</$name>"

    case Dom.Text(data)             => data
    case Dom.Attribute(name, value) => s"""$name="$value""""
    case Dom.Empty                  => ""
  }
}

object Dom {
  def attr(name: CharSequence, value: CharSequence): Dom = Dom.Attribute(name, value)

  def element(name: CharSequence, children: Dom*): Dom = Dom.Element(name, children)

  def empty: Dom = Empty

  def text(data: CharSequence): Dom = Dom.Text(data)

  private[zio] final case class Element(name: CharSequence, children: Seq[Dom]) extends Dom

  private[zio] final case class Text(data: CharSequence) extends Dom

  private[zio] final case class Attribute(name: CharSequence, value: CharSequence) extends Dom

  private[zio] object Empty extends Dom
}
