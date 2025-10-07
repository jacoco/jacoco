/*******************************************************************************
 * Copyright (c) 2009, 2025 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.core.test.validation.kotlin.targets

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Test target with [Serializable] class.
 */
object KotlinSerializableTarget {

    @Serializable // assertFullyCovered()
    data class Example( // assertFullyCovered()
        @SerialName("d") val data: String // assertFullyCovered()
    ) // assertEmpty()

    @Serializable(with = CustomSerializer::class) // assertFullyCovered()
    data class ExampleWithCustomSerializer( // assertFullyCovered()
        val data: String // assertFullyCovered()
    ) // assertEmpty()

    object CustomSerializer : KSerializer<ExampleWithCustomSerializer> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Example", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: ExampleWithCustomSerializer) = encoder.encodeString(value.data)
        override fun deserialize(decoder: Decoder): ExampleWithCustomSerializer =
            ExampleWithCustomSerializer(decoder.decodeString())
    }

    data class ExampleWithHandWrittenCompanion(
        val data: String
    ) {
        companion object {
            fun serializer(): KSerializer<ExampleWithCustomSerializer> = CustomSerializer // assertNotCovered()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Example("").data
        ExampleWithCustomSerializer("").data
        ExampleWithHandWrittenCompanion("")
    }

}
