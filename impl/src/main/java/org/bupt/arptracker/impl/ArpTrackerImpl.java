/*
 * Copyright © 2017 xujun@bupt and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.arptracker.impl;

import java.util.HashMap;
import java.util.concurrent.Future;

import org.opendaylight.controller.liblldp.ConstructionException;
import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.liblldp.EthernetAddress;
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.l2switch.arphandler.core.PacketDispatcher;
import org.opendaylight.l2switch.arphandler.inventory.InventoryReader;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownHardwareType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownOperation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arptracker.config.rev140528.ArptrackerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.KnownEtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.arptracker.rev150105.ArpTrackerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.arptracker.rev150105.ArpTrackerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.arptracker.rev150105.ArpTrackerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.arptracker.rev150105.ArptrackerService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

public class ArpTrackerImpl implements ArptrackerService {
    private final ArptrackerConfig config;
    private final InventoryReader inventoryReader;
    private final PacketProcessingService packetProcessingService;

    public ArpTrackerImpl(final InventoryReader inventoryReader, final PacketProcessingService packetProcessingService, final ArptrackerConfig config) {
        this.inventoryReader = inventoryReader;
        this.packetProcessingService = packetProcessingService;
        this.config = config;
    }

    @Override
    public Future<RpcResult<ArpTrackerOutput>> arpTracker(ArpTrackerInput input) {
        PacketDispatcher packetDispatcher = new PacketDispatcher();
        packetDispatcher.setInventoryReader(inventoryReader);
        packetDispatcher.setPacketProcessingService(packetProcessingService);
        String srcIp = input.getSrcIp().getIpv4Address().getValue();

        for (IpAddress ipAddress : input.getIpList()) {
            ArpPacket arp = new ArpPacket();
            String destip = ipAddress.getIpv4Address().getValue();
            arp.set_destinationProtocolAddress(destip)
                    .set_sourceHardwareAddress(config.getArptrackerMac().getValue())
                    .set_sourceProtocolAddress(srcIp)
                    .set_destinationHardwareAddress("ff:ff:ff:ff:ff:ff")
                    .set_protocolType(KnownEtherType.Ipv4)
                    .set_hardwareType(KnownHardwareType.Ethernet)
                    .set_hardwareLength((short) 6)
                    .set_operation(KnownOperation.Request)
                    .set_protocolLength((short) 4);
            Ethernet ethPkt = new Ethernet();
            EthernetAddress destMac = EthernetAddress.BROADCASTMAC;
            EthernetAddress srcMac = destMac;
            try {
                srcMac = new EthernetAddress(new byte[]{
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0xFF});
            } catch (ConstructionException e) {
                e.printStackTrace();
            }
            ethPkt.setSourceMACAddress(srcMac.getValue())
                    .setDestinationMACAddress(destMac.getValue())
                    .setEtherType(EtherTypes.ARP.shortValue())
                    .setPayload(arp);
            inventoryReader.setRefreshData(true);
            inventoryReader.readInventory();
            HashMap<String, NodeConnectorRef> map = inventoryReader.getControllerSwitchConnectors();
            for (String node : map.keySet()) {
                try {
                    packetDispatcher.dispatchPacket(ethPkt.serialize(), map.get(node), null, null);
                } catch (PacketException e) {
                    e.printStackTrace();
                }
            }
        }
        RpcResultBuilder<ArpTrackerOutput> rpcResultBuilder = RpcResultBuilder.success();
        ArpTrackerOutputBuilder builder = new ArpTrackerOutputBuilder();
        rpcResultBuilder.withResult(builder.setSuccess(true).build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
