/*
 * Copyright © 2017 xujun@bupt and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.bupt.arptracker.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opendaylight.controller.liblldp.BitBufferHelper;
import org.opendaylight.controller.liblldp.BufferException;
import org.opendaylight.controller.liblldp.HexEncode;
import org.opendaylight.controller.liblldp.Packet;
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownHardwareType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.KnownOperation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.ethernet.rev140528.KnownEtherType;

public class ArpPacket extends Packet {
    private String _destinationHardwareAddress;
    private String _destinationProtocolAddress;
    private Short _hardwareLength;
    private KnownHardwareType _hardwareType;
    private KnownOperation _operation;
    private Integer _payloadLength;
    private Integer _payloadOffset;
    private Short _protocolLength;
    private KnownEtherType _protocolType;
    private String _sourceHardwareAddress;
    private String _sourceProtocolAddress;

    public String get_destinationHardwareAddress() {
        return _destinationHardwareAddress;
    }

    public ArpPacket set_destinationHardwareAddress(String _destinationHardwareAddress) {
        this._destinationHardwareAddress = _destinationHardwareAddress;
        return this;
    }

    public String get_destinationProtocolAddress() {
        return _destinationProtocolAddress;
    }

    public ArpPacket set_destinationProtocolAddress(String _destinationProtocolAddress) {
        this._destinationProtocolAddress = _destinationProtocolAddress;
        return this;
    }

    public Short get_hardwareLength() {
        return _hardwareLength;
    }

    public ArpPacket set_hardwareLength(Short _hardwareLength) {
        this._hardwareLength = _hardwareLength;
        return this;
    }

    public KnownHardwareType get_hardwareType() {
        return _hardwareType;
    }

    public ArpPacket set_hardwareType(KnownHardwareType _hardwareType) {
        this._hardwareType = _hardwareType;
        return this;
    }

    public KnownOperation get_operation() {
        return _operation;
    }

    public ArpPacket set_operation(KnownOperation _operation) {
        this._operation = _operation;
        return this;
    }

    public Integer get_payloadLength() {
        return _payloadLength;
    }

    public ArpPacket set_payloadLength(Integer _payloadLength) {
        this._payloadLength = _payloadLength;
        return this;
    }

    public Integer get_payloadOffset() {
        return _payloadOffset;
    }

    public ArpPacket set_payloadOffset(Integer _payloadOffset) {
        this._payloadOffset = _payloadOffset;
        return this;
    }

    public Short get_protocolLength() {
        return _protocolLength;
    }

    public ArpPacket set_protocolLength(Short _protocolLength) {
        this._protocolLength = _protocolLength;
        return this;
    }

    public KnownEtherType get_protocolType() {
        return _protocolType;
    }

    public ArpPacket set_protocolType(KnownEtherType _protocolType) {
        this._protocolType = _protocolType;
        return this;
    }

    public String get_sourceHardwareAddress() {
        return _sourceHardwareAddress;
    }

    public ArpPacket set_sourceHardwareAddress(String _sourceHardwareAddress) {
        this._sourceHardwareAddress = _sourceHardwareAddress;
        return this;
    }

    public String get_sourceProtocolAddress() {
        return _sourceProtocolAddress;
    }

    public ArpPacket set_sourceProtocolAddress(String _sourceProtocolAddress) {
        this._sourceProtocolAddress = _sourceProtocolAddress;
        return this;
    }

    @Override
    public byte[] serialize() throws PacketException {
        byte[] Arppacket = new byte[42];
        int hardwareTypeLength = 16;
        //ProtocolType to byte length 16
        try {
            BitBufferHelper.setBytes(Arppacket, shortToByteArray((short) _hardwareType.getIntValue()), 0, hardwareTypeLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int protolTypeLength = 16;
        try {
            BitBufferHelper.setBytes(Arppacket, shortToByteArray((short) _protocolType.getIntValue()), 16, protolTypeLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int hardwareLength = 8;
        try {
            BitBufferHelper.setBytes(Arppacket, byteToByteArray((short) _hardwareLength), 32, hardwareLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int protolLength = 8;
        try {
            BitBufferHelper.setBytes(Arppacket, byteToByteArray((short) _protocolLength), 40, protolLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int operationLength = 16;
        try {
            BitBufferHelper.setBytes(Arppacket, shortToByteArray((short) _operation.getIntValue()), 48, operationLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int srcHwLength = 48;
        try {
            BitBufferHelper.setBytes(Arppacket, HexEncode.bytesFromHexString(_sourceHardwareAddress), 64, srcHwLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int srcProLength = 32;
        try {
            BitBufferHelper.setBytes(Arppacket, InetAddress.getByName(_sourceProtocolAddress).getAddress(), 112, srcProLength);
        } catch (BufferException | UnknownHostException e) {
            e.printStackTrace();
        }

        int destHwLength = 48;
        try {
            BitBufferHelper.setBytes(Arppacket, HexEncode.bytesFromHexString(_destinationHardwareAddress), 144, destHwLength);
        } catch (BufferException e) {
            e.printStackTrace();
        }
        int destProLength = 32;
        try {
            BitBufferHelper.setBytes(Arppacket, InetAddress.getByName(_destinationProtocolAddress).getAddress(), 192, destProLength);
        } catch (BufferException | UnknownHostException e) {
            e.printStackTrace();
        }

        return Arppacket;
    }

    private byte[] shortToByteArray(short i) {
        byte[] result = new byte[2];
        //由高位到低位
        result[0] = (byte) ((i >> 8) & 0xFF);
        result[1] = (byte) (i & 0xFF);

        return result;
    }

    private byte[] byteToByteArray(short i) {
        byte[] result = new byte[1];
        //由高位到低位
        result[0] = (byte) (i & 0xFF);
        return result;
    }


}
