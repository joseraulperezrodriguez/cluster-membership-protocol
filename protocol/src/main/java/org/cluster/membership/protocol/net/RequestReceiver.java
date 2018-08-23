package org.cluster.membership.protocol.net;

import java.util.ArrayList;
import java.util.List;

import org.cluster.membership.protocol.core.ClusterView;
import org.cluster.membership.protocol.core.MessageType;
import org.cluster.membership.protocol.model.Message;
import org.cluster.membership.protocol.model.MessageResponse;
import org.cluster.membership.protocol.model.Node;
import org.cluster.membership.protocol.util.MathOp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;

@Component
public class RequestReceiver {
	
	private long lastMessage;
	
	@Autowired
	private RequestMessageHandler messageHandler;
	
	@Autowired
	private ClusterView clusterView;
	
	public RequestReceiver() {
		this.lastMessage = System.currentTimeMillis();
	}
	
	public long getLastMessage() {
		return lastMessage;
	}

	public List<MessageResponse<?>> receive(Node from, List<Message> messages, ChannelHandlerContext ctx) {
		lastMessage = System.currentTimeMillis();
		
		List<MessageResponse<?>> ans = new ArrayList<MessageResponse<?>>();
		
		if(clusterView.isFailing(from)) clusterView.removeFailing(from);
		
		if(clusterView.isSuspectedDead(from)) {
			Message keepAlive = new Message(MessageType.KEEP_ALIVE, from, MathOp.log2n(clusterView.getClusterSize()));
			clusterView.keepAlive(keepAlive);
		}
		
		for(Message m : messages) {
			MessageType mt = m.getType();
		
			/**If message is SUSPECT_DEAD we insert again to allow the minimal time will
			 * be prioritized, in this way we ensure all the nodes have the same 
			 * expiration time for a node*/
			if(clusterView.isRumor(m) && !mt.equals(MessageType.SUSPECT_DEAD)) continue;
			
			switch (mt) {
				case SUBSCRIPTION: {
					ans.add(messageHandler.handlerSubscription(m));
					break;
				}
				case UNSUBSCRIPTION: {
					messageHandler.handlerUnsubscription(m);
					break;
				}
				case PROBE: {
					MessageResponse<Boolean> mr = messageHandler.handlerProbe(m, ctx);
					if(mr != null) ans.add(mr);
					break;
				} 
				case SUSPECT_DEAD: {
					messageHandler.handlerSuspectDead(m);
					break;
				}
				case KEEP_ALIVE: {
					messageHandler.handlerKeepAlive(m);
					break;
				}
				case ADD_TO_CLUSTER: {
					messageHandler.handlerAddToCluster(m);
					break;
				}
				case REMOVE_FROM_CLUSTER: {
					messageHandler.handlerRemoveFromCluster(m);
					break;
				} 
				case UPDATE: {
					messageHandler.handlerUpdateNode(m);
					break;
				}
				
			}
			
		}
		
		return ans;
		
	}

	public ClusterView getClusterView() {
		return clusterView;
	}

}
