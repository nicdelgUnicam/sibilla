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

package it.unicam.quasylab.sibilla.core.models.lio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentsDefinitionTest {

    @Test
    void emptyAgentsDefinitionShouldHaveNoDeclaredAgent() {
        AgentsDefinition def = new AgentsDefinition();
        assertEquals(0,def.numberOfAgents());
    }

    @Test
    void anAgentShouldBeSuccessfullyCreated() {
        AgentsDefinition def = new AgentsDefinition();
        assertNotNull(def.addAgent("TEST"));
    }

    @Test
    void anAgentShouldBeFoundByItsIndex() {
        AgentsDefinition def = new AgentsDefinition();
        Agent a = def.addAgent("TEST");
        Agent b = def.getAgent(a.getIndex());
        assertEquals(a,b);
    }

    @Test
    void anAgentShouldBeFoundByItsName() {
        AgentsDefinition def = new AgentsDefinition();
        Agent a = def.addAgent("TEST");
        Agent b = def.getAgent("TEST");
        assertEquals(a,b);
    }

    @Test
    void createLIOIndividualState() {
        AgentsDefinition def = new AgentsDefinition();
        Agent agentA = def.addAgent("A");
        Agent agentB = def.addAgent("B");
        Agent agentC = def.addAgent("C");
        Agent agentD = def.addAgent("D");
        LIOIndividualState state = new LIOIndividualState(def, "A" , "A", "A", "A", "D");
        assertEquals(0.8,state.fractionOf(agentA));
        assertEquals(0,state.fractionOf(agentB));
        assertEquals(0,state.fractionOf(agentC));
        assertEquals(0.2,state.fractionOf(agentD));
    }


    @Test
    void shouldComputeActionProbability() {
        AgentsDefinition def = new AgentsDefinition();
        Agent agentA = def.addAgent("A");
        Agent agentB = def.addAgent("B");
        Agent agentC = def.addAgent("C");
        Agent agentD = def.addAgent("D");
        AgentAction act1 = def.addAction("act1", s -> 0.25);
        AgentAction act2 = def.addAction("act2", s -> 0.75);
        LIOIndividualState state = new LIOIndividualState(def, "A" , "A", "A", "A", "D");
        ActionsProbability prob = def.getActionProbability(state);
        assertEquals(0.25, prob.probabilityOf(act1));
        assertEquals(0.75, prob.probabilityOf(act2));
    }

}