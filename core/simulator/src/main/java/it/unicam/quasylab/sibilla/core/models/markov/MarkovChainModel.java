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

package it.unicam.quasylab.sibilla.core.models.markov;

import it.unicam.quasylab.sibilla.core.models.AbstractModel;
import it.unicam.quasylab.sibilla.core.models.StepFunction;
import it.unicam.quasylab.sibilla.core.models.util.MappingState;
import it.unicam.quasylab.sibilla.core.models.util.VariableTable;
import it.unicam.quasylab.sibilla.core.simulator.sampling.Measure;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedLinkedList;
import it.unicam.quasylab.sibilla.core.simulator.util.WeightedStructure;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A module representing a MarkovChain.
 */
public abstract class MarkovChainModel extends AbstractModel<MappingState> {

    private final VariableTable stateVariables;
    protected final List<MappingStateUpdate> rules;

    protected MarkovChainModel(VariableTable stateVariables, List<MappingStateUpdate> rules, Map<String, Measure<? super MappingState>> measuresTable) {
        super(measuresTable);
        this.rules = rules;
        this.stateVariables = stateVariables;
    }

    @Override
    public int stateByteArraySize() {
        return Double.BYTES*stateVariables.size();
    }

    @Override
    public byte[] byteOf(MappingState state) throws IOException {
        return new byte[0];
    }

    @Override
    public MappingState fromByte(byte[] bytes) throws IOException {
        return null;
    }


}
