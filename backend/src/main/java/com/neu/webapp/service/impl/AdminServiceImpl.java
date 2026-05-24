package com.neu.webapp.service.impl;

import com.neu.webapp.entity.InboundPackage;
import com.neu.webapp.entity.Package;
import com.neu.webapp.mapper.InboundPackageMapper;
import com.neu.webapp.mapper.PackageMapper;
import com.neu.webapp.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final PackageMapper packageMapper;
    private final InboundPackageMapper inboundPackageMapper;

    public AdminServiceImpl(PackageMapper packageMapper, InboundPackageMapper inboundPackageMapper) {
        this.packageMapper = packageMapper;
        this.inboundPackageMapper = inboundPackageMapper;
    }

    @Override
    @Transactional
    public Map<String, Object> packageInit(Map<String, Object> body) {
        Package pkg = new Package();
        pkg.setTrackingNumber((String) body.get("trackingNumber"));
        pkg.setPackageName((String) body.get("packageName"));
        pkg.setWeight(body.get("weight") != null ? Double.valueOf(body.get("weight").toString()) : null);
        pkg.setVolume(body.get("volume") != null ? Double.valueOf(body.get("volume").toString()) : null);
        pkg.setSenderName((String) body.get("senderName"));
        pkg.setSenderPhone((String) body.get("senderPhone"));
        pkg.setSenderAddress((String) body.get("senderAddress"));
        pkg.setReceiverName((String) body.get("receiverName"));
        pkg.setReceiverPhone((String) body.get("receiverPhone"));
        pkg.setReceiverAddress((String) body.get("receiverAddress"));
        pkg.setNotes((String) body.get("notes"));
        packageMapper.insert(pkg);

        boolean createInbound = Boolean.TRUE.equals(body.get("createInbound"));
        InboundPackage inbound = null;
        if (createInbound) {
            inbound = new InboundPackage();
            inbound.setPackageId(pkg.getId());
            inbound.setStatus("PENDING");
            if (pkg.getVolume() != null) {
                if (pkg.getVolume() < 12000) {
                    inbound.setCabinetType("SMALL");
                } else if (pkg.getVolume() > 125000) {
                    inbound.setCabinetType("LARGE");
                } else {
                    inbound.setCabinetType("MEDIUM");
                }
            }
            inboundPackageMapper.insert(inbound);
        }

        return Map.of(
            "id", pkg.getId(),
            "trackingNumber", pkg.getTrackingNumber(),
            "inboundId", inbound != null ? inbound.getId() : null
        );
    }
}
