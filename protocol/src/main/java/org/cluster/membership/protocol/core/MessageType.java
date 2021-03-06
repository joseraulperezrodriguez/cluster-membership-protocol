package org.cluster.membership.protocol.core;

import java.io.Serializable;

public enum MessageType implements Serializable {
	
	/**request from external system to remove some node from the cluster, generates a gossip message
	 * */
	UNSUBSCRIPTION(3, MessageCategory.CLUSTER), 
	
	
	/**a node A sends a message to node B to check if its alive
	 * */
	PROBE(6, MessageCategory.PROBE),
	
	/**a gossip message about a node that is suspected dead, the message includes the time failing probe it
	 * */
	SUSPECT_DEAD(2, MessageCategory.RUMOR),
	
	
	/**a gossip message to avoid deletion of some node because of SUSPECT_DEAD timeout
	 * */
	KEEP_ALIVE(4, MessageCategory.RUMOR),
	
	
	/**a gossip message for including a node in the cluster
	 * */
	ADD_TO_CLUSTER(5, MessageCategory.RUMOR),
	
	
	/**a gossip message for remove a node from the cluster
	 * */
	REMOVE_FROM_CLUSTER(1, MessageCategory.RUMOR);
	
	
	/**A priority to sort message, a way to know the importance of message type, has no effective implications until now 
	 * because of the asynchronous nature of communication
	 * */
	private final int priority;
	
	
	private final MessageCategory category;
	
	MessageType(int priority, MessageCategory category) {
		this.priority = priority;
		this.category = category;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public MessageCategory getCategory() {
		return category;
	}
	
	public static MessageType getMinPriority() {
		MessageType min = null;
		for(MessageType mt : MessageType.values()) {
			if(min == null) min = mt;
			
			if(mt.priority < min.priority) min = mt;			
		}
		return min;
	}
	
	public static MessageType getMaxPriority() {
		MessageType max = null;
		for(MessageType mt : MessageType.values()) {
			if(max == null) max = mt;
			
			if(mt.priority > max.priority) max = mt;			
		}
		return max;
	}

}
