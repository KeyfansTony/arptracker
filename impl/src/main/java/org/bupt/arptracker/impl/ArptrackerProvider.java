/*
 * Copyright © 2017 bupt.dtj and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.arptracker.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.l2switch.arphandler.inventory.InventoryReader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arptracker.config.rev140528.ArptrackerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.arptracker.rev150105.ArptrackerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArptrackerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ArptrackerProvider.class);
    private final DataBroker dataBroker;
    private final PacketProcessingService packetProcessingService;
    private final ArptrackerConfig arpTrackerConfig;
    private final RpcProviderRegistry registry;

    private BindingAwareBroker.RpcRegistration<ArptrackerService> serviceRegistration;

    public ArptrackerProvider(final DataBroker dataBroker, final PacketProcessingService packetProcessingService, final ArptrackerConfig arpTrackerConfig, final RpcProviderRegistry registry) {
        this.dataBroker = dataBroker;
        this.packetProcessingService = packetProcessingService;
        this.arpTrackerConfig = arpTrackerConfig;
        this.registry = registry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        InventoryReader inventoryReader = new InventoryReader(dataBroker);
        ArptrackerService service = new ArpTrackerImpl(inventoryReader, packetProcessingService, arpTrackerConfig);
        serviceRegistration = registry.addRpcImplementation(ArptrackerService.class, service);
        LOG.info("ArptrackerProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        serviceRegistration.close();
        LOG.info("ArptrackerProvider Closed");
    }
}