package org.cluster.membership.protocol.services;

import java.util.List;
import java.util.logging.Logger;

import org.cluster.membership.common.debug.StateInfo;
import org.cluster.membership.common.model.Node;
import org.cluster.membership.protocol.ClusterNodeEntry;
import org.cluster.membership.protocol.Config;
import org.cluster.membership.protocol.core.ClusterView;
import org.cluster.membership.protocol.core.Global;
import org.cluster.membership.protocol.model.ClusterData;
import org.cluster.membership.protocol.net.MembershipServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/membership")
public class MembershipServiceController {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Autowired
	private ClusterView clusterView;
	
	@Autowired
	private MembershipServer membershipServer;
	
	@Autowired
	private Config config;
			
	@GetMapping("/size")	
	public int getSize() {
		return clusterView.getClusterSize();
	}
	
	@PostMapping("/unsubscribe")	
	public boolean unSubscribe() {
		logger.info("unsubscribe message received");
		clusterView.unsubscribe();
		return true;
	}
	
	
	@PostMapping("/subscribe")
	public ClusterData subscribe(@RequestBody(required = true)Node node) {
		logger.info("enter subscribe endpoint: " + node);
		clusterView.subscribe(node);
		return clusterView.myView(node);
	}
	
	@GetMapping("/nodes")
	public List<Node> nodes() {		
		return clusterView.nodes();
	}
	
	@PostMapping("/debug/shutdown")	
	public boolean shutdown(@RequestBody(required = true) int seconds) {
		logger.info("shutdown message received: " + config.getThisPeer());
		Global.shutdown(ClusterNodeEntry.applicationContexts.get(config.getThisPeer()), seconds);
		return true;
	}
	
	@PostMapping("/debug/pause")	
	public boolean pause(@RequestBody(required = true) long millis) {
		logger.info("pause message received " + millis);
		membershipServer.pause(millis);
		return true;
	}
	
	@GetMapping("/debug/state-info")
	public StateInfo nodesDebug() {
		return clusterView.getStateInfo();
	}
		
			
}
