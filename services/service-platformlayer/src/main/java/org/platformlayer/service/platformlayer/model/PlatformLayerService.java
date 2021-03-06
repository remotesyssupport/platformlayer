package org.platformlayer.service.platformlayer.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.platformlayer.ops.PlatformLayerServiceController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(PlatformLayerServiceController.class)
public class PlatformLayerService extends ItemBase {
    public String dnsName;

    public PlatformLayerKey database;
}
