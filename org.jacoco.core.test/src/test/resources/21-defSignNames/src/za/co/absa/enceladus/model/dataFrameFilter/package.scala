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

package za.co.absa.enceladus.model

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonSubTypes, JsonTypeInfo}
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.{col, lit, not => columnNot}
import org.apache.spark.sql.types._

package object dataFrameFilter {

  /**
    * DataFrameFilter is the basic trait for all filters. Introduces the interface to get the filter column a boolean
    * column that is expected to be used in a DataFrame filter.
    * Several operators are defined to be able to combine filter instances
    */  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
  @JsonSubTypes(Array(
    new Type(value = classOf[OrJoinedFilters], name = "OrJoinedFilters"),
    new Type(value = classOf[AndJoinedFilters], name = "AndJoinedFilters"),
    new Type(value = classOf[NotFilter], name = "NotFilter"),
    new Type(value = classOf[EqualsFilter], name = "EqualsFilter"),
    new Type(value = classOf[DiffersFilter], name = "DiffersFilter"),
    new Type(value = classOf[IsNullFilter], name= "IsNullFilter")
  ))
  sealed trait DataFrameFilter {
    @JsonIgnore def filter: Column
    @JsonIgnore def or(otherFilter: DataFrameFilter): DataFrameFilter = {
      (this, otherFilter) match {
        case (a: OrJoinedFilters, b: OrJoinedFilters) => OrJoinedFilters(a.filterItems union b.filterItems)
        case (a: OrJoinedFilters, b) => a.copy(filterItems = a.filterItems + b)
        case (a, b: OrJoinedFilters) => b.copy(filterItems = b.filterItems + a)
        case (a, b) => OrJoinedFilters(Set(a, b))
      }
    }
    @JsonIgnore def and(otherFilter: DataFrameFilter): DataFrameFilter = {
      (this, otherFilter) match {
        case (a: AndJoinedFilters, b: AndJoinedFilters) => AndJoinedFilters(a.filterItems union b.filterItems)
        case (a: AndJoinedFilters, b) => a.copy(filterItems = a.filterItems + b)
        case (a, b: AndJoinedFilters) => b.copy(filterItems = b.filterItems + a)
        case (a, b) => AndJoinedFilters(Set(a, b))
      }
    }

    def +(otherFilter: DataFrameFilter): DataFrameFilter = or(otherFilter) //scalastyle:ignore method.name function used as operator
    def *(otherFilter: DataFrameFilter): DataFrameFilter = and(otherFilter) //scalastyle:ignore method.name function used as operator
    def unary_!(): DataFrameFilter = not(this) //scalastyle:ignore method.name function used as operator
  }

  def not(filter: DataFrameFilter): DataFrameFilter = {
    filter match {
      case a: NotFilter => a.inputFilter
      case x => NotFilter(x)
    }
  }

  /**
    * Trait JoinFilters is a foundation on how to join several filters together using a logical operator
    */
  sealed trait JoinFilters extends DataFrameFilter {
    @JsonIgnore protected def operator: (Column, Column) => Column

    def filterItems: Set[DataFrameFilter]
    def filter: Column = {
      if (filterItems.isEmpty) {
        lit(true)
      } else {
        filterItems.tail.foldLeft(filterItems.head.filter) { case(col, filterDef) =>
          operator(col, filterDef.filter)
        }
      }
    }
  }

  /**
    * Trait SingleColumnAndValueFilter is a foundation to compare a column value with a constant using an operator.
    */
  sealed trait SingleColumnAndValueFilter extends DataFrameFilter {
    def columnName: String
    def value: String
    def valueType: String

    @JsonIgnore def dataType: DataType = SingleColumnAndValueFilter.nameToType(valueType)
    @JsonIgnore protected def operator: (Column, Column) => Column
    override def filter: Column = {
      if (dataType == StringType) {
        // no need to cast from string, simpler expression
        operator(col(columnName), lit(value))
      } else {
        operator(col(columnName), lit(value) cast dataType)
      }
    }
  }

  object SingleColumnAndValueFilter {
    private val nonDecimalNameToType = {
      Seq(NullType, DateType, TimestampType, BinaryType, IntegerType, BooleanType, LongType,
        DoubleType, FloatType, ShortType, ByteType, StringType, CalendarIntervalType)
        .map(t => t.typeName -> t).toMap
    }

    private val FIXED_DECIMAL = """decimal\(\s*(\d+)\s*,\s*(-?\d+)\s*\)""".r

    /** Given the string representation of a type, return its DataType */
    private def nameToType(name: String): DataType = {
      name match {
        case null => StringType //scalastyle:ignore null (to make valueType optional)
        case "decimal" => DecimalType.USER_DEFAULT
        case FIXED_DECIMAL(precision, scale) => DecimalType(precision.toInt, scale.toInt)
        case other => nonDecimalNameToType.getOrElse(
          other,
          throw new IllegalArgumentException(
            s"Failed to convert the JSON string '$name' to a data type."))
      }
    }
  }

  /**
    * Joins a set of filters with an "or" operator. Therefore at least one of the filter items has to be evaluated as true
    * the filter to be itself true.
    * JSON representation: {"_t":"OrJoinedFilters","filterItems":[FILTER_ITEMS]}
    * FILTER_ITEMS is a comma-separated list of other filters JSON entries
    * @param filterItems the filters that will be joined with the or operator
    */
  case class OrJoinedFilters(filterItems: Set[DataFrameFilter]) extends JoinFilters {
    protected val operator: (Column, Column) => Column = (a: Column, b: Column) => { a or b }
  }

  /**
    * Joins a set of filters with an "and" operator. Therefore all the filter items has to be evaluated as true
    * the filter to be itself true.
    * JSON representation: {"_t":"AndJoinedFilters","filterItems":[FILTER_ITEMS]}
    * FILTER_ITEMS is a comma-separated list of other filters JSON entries
    * @param filterItems the filters that will be joined with the and operator
    */
  case class AndJoinedFilters(filterItems: Set[DataFrameFilter]) extends JoinFilters {
    protected val operator: (Column, Column) => Column = (a: Column, b: Column) => { a and b }
  }

  /**
    * Reverses the boolean value of the input filter
    * JSON representation: {"_t":"NotFilter","inputFilter":[FILTER_ITEM]}
    * FILTER_ITEM JSON representation of the filter to reverse
    * @param inputFilter The filter which boolean evaluation is to be reversed true->false, false->true
    */
  case class NotFilter(inputFilter: DataFrameFilter) extends DataFrameFilter {
    override def filter: Column = columnNot(inputFilter.filter)
  }

  /**
    * EqualsFilter compares values of the specified column to the provided constant. Only rows where the column value
    * equals the constant are kept.
    * JSON representation: {"_t":"EqualsFilter","columnName":"COLUMN_NAME","value":"VALUE","valueType":"VALUE_TYPE"}
    * JSON representation: {"_t":"EqualsFilter","columnName":"COLUMN_NAME","value":"VALUE"}
    * COLUMN_NAME       name of the column which values are taken in by the filter
    * VALUE             value to compare to
    * VALUE_TYPE        data type of the value, valid  entries are any of  Spark DataType.typeName strings, if valueType
    *                   is null (missing) StringType is used as default
    * @param columnName name of the column which values are taken in by the filter
    * @param value      value to compare to
    * @param valueType  data type of the value, valid  entries are any of  Spark DataType.typeName strings, if valueType
    *                   is null (missing) StringType is used as default
    */
  case class EqualsFilter(columnName: String, value: String, valueType: String) extends SingleColumnAndValueFilter {
    protected val operator: (Column, Column) => Column = (column: Column, valueColumn: Column) => {
      column === valueColumn
    }
  }

  object EqualsFilter {
    def apply(columnName: String, value: String, dataType: DataType = StringType): EqualsFilter = {
      new EqualsFilter(columnName, value, dataType.typeName)
    }
  }

  /**
    * DiffersFilter compares values of the specified column to the provided constant. Only rows where the column value
    * differs from the constant are kept.
    * JSON representation: {"_t":"DiffersFilter","columnName":"COLUMN_NAME","value":"VALUE","valueType":"VALUE_TYPE"}
    * JSON representation: {"_t":"DiffersFilter","columnName":"COLUMN_NAME","value":"VALUE"}
    * COLUMN_NAME       name of the column which values are taken in by the filter
    * VALUE             value to compare to
    * VALUE_TYPE        data type of the value, valid  entries are any of  Spark DataType.typeName strings, if valueType
    *                   is null (missing) StringType is used as default
    * @param columnName name of the column which values are taken in by the filter
    * @param value      value to compare to
    * @param valueType  data type of the value, valid  entries are any of  Spark DataType.typeName strings, if valueType
    *                   is null (missing) StringType is used as default
    */
  case class DiffersFilter(columnName: String, value: String, valueType: String) extends SingleColumnAndValueFilter {
    protected val operator: (Column, Column) => Column = (column: Column, valueColumn: Column) =>  {
      column =!= valueColumn
    }
  }

  object DiffersFilter {
    def apply(columnName: String, value: String, dataType: DataType = StringType): DiffersFilter = {
      new DiffersFilter(columnName, value, dataType.typeName)
    }
  }

  /**
    * IsNullFilter evalutes to true if the content of the column identifed by column name is NULL.
    * JSON representation: {"_t":"IsNullFilter","columnName":"COLUMN_NAME"}
    * COLUMN_NAME       name of the column which values are checked for null
    * @param columnName name of the column which values are checked for null
    */
  case class IsNullFilter(columnName: String) extends DataFrameFilter {
    override def filter: Column = col(columnName).isNull
  }
}

