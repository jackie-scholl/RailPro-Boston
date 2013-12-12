package com.tumblr.railproboston.appengine.realtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class MessageList implements Iterable<Message>, Serializable {
	private static final long serialVersionUID = 1912120202364710483L;

	private final List<Message> messages;
	private final String updateDate;
	private final String route;

	public MessageList(Collection<Message> messages, String updateDate, String route) {
		this.messages = new ArrayList<Message>(messages);
		this.updateDate = updateDate;
		this.route = route;
	}

	public List<Message> getMessages() {
		return new ArrayList<>(messages);
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public String getRoute() {
		return route;
	}

	public Iterator<Message> iterator() {
		return getMessages().iterator();
	}

	@Override
	public String toString() {
		return "MessageList [messages=" + messages + ", updateDate=" + updateDate + ", route=" + route + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		result = prime * result + ((updateDate == null) ? 0 : updateDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageList other = (MessageList) obj;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		if (updateDate == null) {
			if (other.updateDate != null)
				return false;
		} else if (!updateDate.equals(other.updateDate))
			return false;
		return true;
	}
}