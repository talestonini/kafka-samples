/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package au.com.eliiza.model

import scala.annotation.switch

final case class UserCreateEvent(var userId: String, var name: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        userId
      }.asInstanceOf[AnyRef]
      case 1 => {
        name
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.userId = {
        value.toString
      }.asInstanceOf[String]
      case 1 => this.name = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = UserCreateEvent.SCHEMA$
}

object UserCreateEvent {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"UserCreateEvent\",\"namespace\":\"au.com.eliiza.model\",\"fields\":[{\"name\":\"userId\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"}]}")
}