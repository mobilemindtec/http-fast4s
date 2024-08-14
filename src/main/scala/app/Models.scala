package app

import br.com.mobilemind.json.codec.converter.{Decoder, Encoder}
import br.com.mobilemind.json.codec.converter.Decoder.given
import br.com.mobilemind.json.codec.converter.Encoder.given
import br.com.mobilemind.json.codec.defs.{given, *}
import br.com.mobilemind.json.codec.converter.auto.JsonConverter

object Models:

  case class Person(id: Int = 0, name: String = "")derives JsonConverter


  object Person:
    val encoder: Encoder[Person] =
      Encoder.typ[Person]
        |> Encoder.field("id", _.id)
        |> Encoder.field("name", _.name)

    val decoder: Decoder[Person] =
      Decoder.typ[Person]
        |> Decoder.field("id", (p, i: Int) => p.copy(id = i))
        |> Decoder.field("name", (p, s: String) => p.copy(name = s))


