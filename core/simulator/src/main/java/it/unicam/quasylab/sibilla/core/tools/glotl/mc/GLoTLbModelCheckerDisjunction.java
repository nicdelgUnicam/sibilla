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

package it.unicam.quasylab.sibilla.core.tools.glotl.mc;

import it.unicam.quasylab.sibilla.core.models.lio.LIOCountingState;
import it.unicam.quasylab.sibilla.core.models.lio.LIOState;

import java.util.HashMap;

public class GLoTLbModelCheckerDisjunction extends GLoTLbModelCheckerAbstract {
    private final GLoTLbModelChecker prop1;
    private final GLoTLbModelChecker prop2;

    public GLoTLbModelCheckerDisjunction(GLoTLbModelChecker prop1, GLoTLbModelChecker prop2) {
        super();
        this.prop1 = prop1;
        this.prop2 = prop2;
    }

    @Override
    protected boolean compute(LIOCountingState state) {
        return prop1.sat(state)|| prop2.sat(state);
    }
}
