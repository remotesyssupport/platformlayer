package org.platformlayer.service.solr.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.solr.model.SolrServer;

public class SolrServerController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(SolrServerController.class);

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		SolrServer model = OpsContext.get().getInstance(SolrServer.class);

		int port = SolrConstants.API_PORT;

		InstanceBuilder vm;

		{
			vm = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.publicPorts.add(port);

			vm.hostPolicy.allowRunInContainer = true;

			// TODO: This needs to be configurable...
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		{
			SolrInstall install = injected(SolrInstall.class);
			vm.addChild(install);
		}

		{
			SolrInstance service = injected(SolrInstance.class);
			vm.addChild(service);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			vm.addChild(endpoint);
		}
	}
}
