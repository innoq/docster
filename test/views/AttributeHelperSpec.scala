package views

import org.scalatest.FlatSpec
import services.{JString, JArray, JObject}

class AttributeHelperSpec extends FlatSpec {

  behavior of "AttributeHelper"

  it should "transform an array of strings into a definition list" in {

    val arrayAttribute = JObject(
      Map(("key",
        JArray(List(
          JString("prop1"),
          JString("prop2"),
          JString("prop3"),
          JString("prop4"))))))

    val result = AttributeHelper.renderAttribute(arrayAttribute)

    val expected =
      """<dl class="dl-horizontal">
        |<dt>key</dt>
        |<dd>prop1</dd>
        |<dd>prop2</dd>
        |<dd>prop3</dd>
        |<dd>prop4</dd>
        |</dl>
        |""".stripMargin


    assert(result == expected)
  }

  it should "transform an object of strings into a definition list" in {

    val arrayAttribute = JObject(Map(
      ("attribute1", JString("value1")),
      ("attribute2", JString("value2")),
      ("attribute3", JString("value3")),
      ("attribute4", JString("value4"))
    ))

    val result = AttributeHelper.renderAttribute(arrayAttribute)

    val expected =
      """<dl class="dl-horizontal">
        |<dt>attribute1</dt>
        |<dd>value1</dd>
        |<dt>attribute2</dt>
        |<dd>value2</dd>
        |<dt>attribute3</dt>
        |<dd>value3</dd>
        |<dt>attribute4</dt>
        |<dd>value4</dd>
        |</dl>
        |""".stripMargin

    assert(result == expected)
  }

  it should "transform an object of objects, strings and arrays into a definition list" in {

    val arrayAttribute = JObject(Map(
      ("arrayKey", JArray(List(
        JString("arrayValue1"),
        JString("arrayValue2"),
        JString("arrayValue3")))),
      ("stringKey", JString("stringValue1")),
      ("objectKey", JObject(Map(
        ("arrayKey", JArray(List(
          JString("arrayValue1"),
          JString("arrayValue2"),
          JString("arrayValue3")))),
        ("stringKey", JString("stringValue1")))))))

    val result = AttributeHelper.renderAttribute(arrayAttribute)

    val expected =
      """<dl class="dl-horizontal">
        |<dt>arrayKey</dt>
        |<dd>arrayValue1</dd>
        |<dd>arrayValue2</dd>
        |<dd>arrayValue3</dd>
        |<dt>stringKey</dt>
        |<dd>stringValue1</dd>
        |<dt>objectKey</dt>
        |<dl class="dl-horizontal">
        |<dt>arrayKey</dt>
        |<dd>arrayValue1</dd>
        |<dd>arrayValue2</dd>
        |<dd>arrayValue3</dd>
        |<dt>stringKey</dt>
        |<dd>stringValue1</dd>
        |</dl>
        |</dl>
        |""".stripMargin

    assert(result == expected)
  }


}
