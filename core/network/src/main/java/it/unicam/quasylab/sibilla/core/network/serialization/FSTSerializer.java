/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *             Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unicam.quasylab.sibilla.core.network.serialization;

import org.nustaq.serialization.FSTConfiguration;

import java.io.Serializable;

public class FSTSerializer implements Serializer {

    /**
     * This class defines the encoders/decoders used during FST serialization.
     * Usually you just create one global singleton (instantiation of this class is very expensive).
     */
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public FSTSerializer() {
    }

    @Override
    public byte[] serialize(Serializable toSerialize) {
        return conf.asByteArray(toSerialize);
    }

    @Override
    public Serializable deserialize(byte[] toDeserialize) {
        return (Serializable) conf.asObject(toDeserialize);
    }

    @Override
    public SerializerType getType() {
        return SerializerType.FST;
    }
}
