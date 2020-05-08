/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 *  Copyright (C) 2020.
 *
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package quasylab.sibilla.core.server;

import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPDefaultNetworkManager;
import quasylab.sibilla.core.server.serialization.ObjectSerializer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DiscoverableBasicSimulationServer extends BasicSimulationServer {

    private static final Logger LOGGER = Logger.getLogger(DiscoverableBasicSimulationServer.class.getName());


    private final int localDiscoveryPort;
    private final Set<NetworkInfo> knownMasters;

    public DiscoverableBasicSimulationServer(int localDiscoveryPort, TCPNetworkManagerType networkManagerType) {
        super(networkManagerType);
        this.localDiscoveryPort = localDiscoveryPort;
        this.knownMasters = new HashSet<>();

        LOGGER.info(String.format("Creating a new DiscoverableBasicSimulationServer - It will respond for discovery messages on port [%d]", localDiscoveryPort));

        new Thread(this::startDiscoveryServer).start();
    }

    /**
     * Starts the server that listens for the masters and sends them info about the owned simulation servers
     */
    private void startDiscoveryServer() {
        try {
            DatagramSocket discoverySocket = new DatagramSocket(localDiscoveryPort);
            LOGGER.info(String.format("Now listening for discovery messages on port: [%d]", localDiscoveryPort));
            UDPDefaultNetworkManager manager = new UDPDefaultNetworkManager(discoverySocket);

            while (true) {
                NetworkInfo masterInfo = (NetworkInfo) ObjectSerializer.deserializeObject(manager.readObject());
                LOGGER.info(String.format("Discovered by the master server - %s", masterInfo.toString()));
                manageDiscoveryMessage(manager, masterInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages a message from the master
     *
     * @param manager    UDPNetworkManager that handles the sending of messages
     * @param masterInfo ServerInfo of the master server
     */
    private void manageDiscoveryMessage(UDPDefaultNetworkManager manager, NetworkInfo masterInfo) {
        try {
            this.knownMasters.add(masterInfo);
            manager.writeObject(ObjectSerializer.serializeObject(this.localServerInfo), masterInfo.getAddress(), masterInfo.getPort());
            LOGGER.info(String.format("Sent the discovery response to the master server - %s", masterInfo.toString()));
            LOGGER.info(String.format("Currently known masters - %s", knownMasters.toString()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Network communication failure during the discovery message management - %s", e.getMessage()));
        }
    }
}
